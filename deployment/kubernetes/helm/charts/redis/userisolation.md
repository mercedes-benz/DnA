# Redis User Isolation — ACL Quick Reference

> **Purpose:** Document Redis ACL commands used to isolate workspaces so that each workspace can only access its own key namespace.

---

## 1. Create a New Workspace User

`
ACL SETUSER dna-redis-testing on ><password> ~dna-redis1:* ~dna-redis2:* +@read +@write
`

| Parameter | Meaning |
|---|---|
| `dna-redis-testing` | Username for the new ACL user |
| `on` | Enable the user (`off` would disable it) |
| `<password>` | Set the user's password (replace with a strong secret in production) |
| `~dna-redis1:*` | Allow access **only** to keys prefixed with `dna-redis1:` |
| `~dna-redis2:*` | Also allow access to keys prefixed with `dna-redis2:` |
| `+@read` | Grant all **read** commands (GET, MGET, HGETALL, etc.) |
| `+@write` | Grant all **write** commands (SET, DEL, HSET, etc.) |

### When to use
- Run this command **once** when onboarding a new workspace (or set of workspaces) that needs isolated Redis access.
- Each workspace should use its own unique key prefix (e.g. `dna-redis1:`, `dna-redis2:`) so data stays separated.

### Important notes
- **Key patterns:** The `~` prefix defines an allowlist of key patterns. The user **cannot** read or write keys outside these patterns.
- **Persistence:** Run `ACL SAVE` after creating or modifying users to persist changes across Redis restarts.

---

## 2. List All Workspace Users

`
ACL LIST
`

- Returns **every** ACL user currently configured in the Redis instance.
- Use this to verify that a newly created user exists and has the correct permissions.
- Output format per user: `user <name> on/off [passwords] [key-patterns] [commands]`

### Example output
`
user default on nopass ~* +@all
user dna-redis-testing on #<hashed-password> ~dna-redis1:* ~dna-redis2:* +@read +@write
`

### Related commands
| Command | Description |
|---|---|
| `ACL GETUSER <username>` | Show detailed permissions for a single user |
| `ACL DELUSER <username>` | Remove a user (revokes access immediately) |
| `ACL SAVE` | Persist current ACL rules to disk |
| `ACL LOAD` | Reload ACL rules from the persisted file |
