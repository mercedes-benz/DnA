# JupyterHub user_options Overwrite Bug — Root Cause & Fix

## Problem

Workspace `user_options` stored in the JupyterHub database were being silently
reduced to `{"profile": "default"}` after a server restart, causing all custom
workspace configuration (storage, memory, CPU, environment variables,
extra containers) to be lost on subsequent spawns.

**Observed symptom in the DB:**

```sql
SELECT * FROM public.spawners
WHERE user_options::jsonb = '{"profile": "default"}'::jsonb;
```

Returns workspaces that were originally created with full options such as:

```json
{
  "profile": "default",
  "env": {
    "GITHUBREPO_URL": "git.i.mercedes-benz.com/DNA-CodeSpaces/...",
    "SHORTID": "lmacias",
    "isCollaborator": "false",
    "pathCheckout": "",
    "GITHUB_TOKEN": "null"
  },
  "storage_capacity": "2Gi",
  "mem_guarantee": "2000M",
  "mem_limit": "2000M",
  "cpu_limit": 1.0,
  "cpu_guarantee": 1.0,
  "extra_containers": []
}
```

---

## Root Cause

### 1. Java backend sends no body on restart

`startNamedServer()` in `CodeServerClient.java` (called via
`POST /workspaces/startserver/{id}`) sends a bare POST to the JupyterHub API
with **no request body**:

```java
// CodeServerClient.java — startNamedServer()
HttpEntity<String> entity = new HttpEntity<>(getHeaders(cloudServiceProvider));
restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
// ↑ No body — only headers
```

This is intentional for a restart (not a fresh create), but JupyterHub does not
treat a missing body as "keep existing options".

### 2. JupyterHub always calls `get_options_from_form` when body is present

JupyterHub 4.0.0's API handler does:

```python
data = self.get_json_body() or {}   # empty body → {}
options = data.get("options", data) # {} → {}
# options is {} (not None), so JupyterHub calls:
spawner.user_options = await spawner.get_options_from_form(options)
```

An empty dict `{}` is **not** `None`, so `get_options_from_form` is always
invoked — even for a restart with no payload.

### 3. KubeSpawner maps an empty submission to `{"profile": "default"}`

`KubeSpawner.get_options_from_form({})` with a `profile_list` configured
inspects the form data, finds no profile key, and falls back to the first
profile's slug:

```python
# KubeSpawner result for empty formdata when profile_list is set:
return {"profile": "default"}
```

This result is then **written back to the database**, overwriting the full
`user_options` that were stored at workspace creation time.

### 4. Impact on spawn

`pre_spawn_hook set_resource` reads from `spawner.user_options` to apply
resources each time a pod is spawned. Once `user_options` is reduced to
`{"profile": "default"}`, all subsequent spawns apply the global defaults:

| Setting | Custom value (lost) | Default applied |
|---|---|---|
| `storage_capacity` | e.g. `2Gi` | `1Gi` |
| `mem_guarantee` | e.g. `2000M` | `1000M` |
| `mem_limit` | e.g. `2000M` | `2000M` |
| `cpu_guarantee` | e.g. `1.0` | `0.2` |
| `env` (GITHUBREPO_URL, etc.) | custom values | not injected |
| `extra_containers` | custom list | not applied |

---

## Fix

Override `get_options_from_form` in `CodeSpaceKubeSpawner` to detect the
"empty restart" case and return the already-stored `user_options` unchanged:

```python
async def get_options_from_form(self, formdata):
    """
    Preserve rich user_options when the restart API sends no body (empty dict).
    Without this override, KubeSpawner maps an empty form submission to
    {"profile": "default"}, overwriting all custom workspace configuration
    (storage, memory, CPU, env vars, extra_containers) that was stored in
    the JupyterHub DB during the initial server creation.

    Guard logic:
      - If the incoming formdata has substantive keys beyond 'profile',
        forward normally to super() so intentional re-configuration works.
      - If the stored user_options already contains only {'profile': ...},
        forward normally — there is nothing richer to preserve.
      - Otherwise (restart with empty/profile-only body against a workspace
        that has full options stored), return the stored user_options as-is.
    """
    has_substantive_options = any(
        k not in ('profile',) for k in (formdata or {})
    )
    has_rich_stored_options = bool(self.user_options) and any(
        k not in ('profile',) for k in self.user_options
    )
    if not has_substantive_options and has_rich_stored_options:
        self.log.info(
            "[PRESERVE-OPTIONS] Incoming formdata minimal (%s), "
            "preserving stored user_options for server '%s' (keys: %s)",
            list((formdata or {}).keys()),
            self.name,
            list(self.user_options.keys())
        )
        return self.user_options
    return await super().get_options_from_form(formdata)
```

### Files modified

| File | Change |
|---|---|
| `values-prod.yaml` | Added `get_options_from_form` to `CodeSpaceKubeSpawner` |
| `values-dev.yaml` | Same |
| `values-test.yaml` | Same |

The method is placed inside the `CodeSpaceKubeSpawner` class, after `get_env`
and before `# Use the configured spawner`.

---

## Deployment

```bash
# Test environment first
helm upgrade <release-name> ./jupyterhub-codeserver \
  -n <test-namespace> \
  -f values-test.yaml

# Dev
helm upgrade <release-name> ./jupyterhub-codeserver \
  -n <dev-namespace> \
  -f values-dev.yaml

# Prod
helm upgrade <release-name> ./jupyterhub-codeserver \
  -n prod-dna-cs-workspaces \
  -f values-prod.yaml
```

The hub pod will restart automatically and pick up the new config.

---

## Verification

### 1. DB check after stop → start

After a workspace is stopped and restarted via the backend API, query:

```sql
SELECT id, name, user_options
FROM public.spawners
WHERE name = 'wsXXXX';
```

Expected: `user_options` retains the full JSON (storage, mem, cpu, env, etc.)
instead of being reduced to `{"profile": "default"}`.

### 2. Hub pod logs

```bash
kubectl logs -n prod-dna-cs-workspaces deployment/cs-hub | grep PRESERVE-OPTIONS
```

Expected output on restart:

```
[PRESERVE-OPTIONS] Incoming formdata minimal ([]), preserving stored
user_options for server 'wsXXXX' (keys: ['profile', 'env', 'storage_capacity',
'mem_guarantee', 'mem_limit', 'cpu_limit', 'cpu_guarantee', 'extra_containers'])
```

### 3. Pod resource requests

```bash
kubectl describe pod wsXXXX -n prod-dna-cs-workspaces | grep -A5 Requests
```

Expected: resource requests match the original workspace configuration.

---

## Scope / Limitations

- **Already-degraded workspaces** (those whose `user_options` is already
  `{"profile": "default"}` in the DB) cannot be auto-recovered — the stored
  value is the source of truth and the guard will not trigger. These workspaces
  need to be recreated via the backend to restore their full configuration.
- This fix prevents **new** degradations; it does not back-fill historical data.
- The Java `startNamedServer()` method itself is unchanged (no rebuild needed).
- When the backend intentionally passes a full options payload (as in
  `createServer()`), the guard's `has_substantive_options` check is `True` and
  the normal KubeSpawner path is taken — no regression.
