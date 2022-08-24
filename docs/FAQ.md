#### **1. Error Pull Access Denied**

If you are using the Docker Desktop, you may encounter the Pull Access denied issue. To resolve the issue,

**Disable temporarily**

Export the env variable before running the docker-compose up cmd.

Linux Shell

```
export DOCKER_BUILDKIT=0
```

Powershell

```
$Env:DOCKER_BUILDKIT = 0
```

Now run the docker-compose up cmd.

**Disable Permanent**

GoTo Docker-Desktop Dashboard adds buildkit value as false.
Add the following JSON:

```
"features": {
"buildkit": false
}
```

After adding the above JSON Demon.Json look like below:

```
{
"features": {
"buildkit": false
},
"debug": true,
"experimental": false,
"builder": {
"gc": {
"enabled": true,
"defaultKeepStorage": "20GB"
}
}
}
```

Run the below command to clean docker

```
cd deployment/
./clean-docker.sh
```

#### 2.Invalid Flag value dna-base:frontend

```
2 errors occurred:
* Status: pull access denied for dna-base, repository does not exist or may require 'docker login': denied: requested access to the resource is denied, Code: 1
* Status: invalid from flag value dna-base:frontend: pull access denied for dna-base, repository does not exist or may require 'docker login': denied: requested access to the resource is denied, Code: 1
```

If you are facing the above issue, it could be the reason for the timeout of node modules installed inside the docker image. Please run same the docker-compose up command again.

#### 3. Command not found error

```
result-frontend_1 | Checking DNS nameserver ...
result-frontend_1 | docker-start.sh: line 7: $'\r': command not found
result-frontend_1 | docker-start.sh: line 8: $'\r': command not found
result-frontend_1 | PROJECTSMO_DNS_RESOLVER_ADDRESS is not set. Using values from /etc/resolv.conf ...
result-frontend_1 | docker-start.sh: line 9: $'\r': command not found
result-frontend_1 | docker-start.sh: line 10: $'\r': command not found
```

If you are getting the above command not found error then, Open the docker-start.sh file in Notepad++, then look in the bottom right-hand corner. You'll see "Windows (CRLF)" change it to "Unix (LF)" and this will resolve the issue.
And if this method doesn't work then try to enable the WSL2 engine on the docker desktop step.

#### 4. Enable WSL Engine on Docker Desktop

Before enabling the WSL engine, please verify wsl engine is installed on your system. If not present download the WSL2 kernel and install it [WSL2 Package](https://docs.microsoft.com/en-us/windows/wsl/install-manual#step-4---download-the-linux-kernel-update-package).

1. Start Docker Desktop from the Windows Start menu.
2. From the Docker menu, select **Settings** >**General**.
3. Select the **Use WSL 2 based engine** check box.
4. Click **Apply & Restart**.

#### 4. Readiness probe error in vault -- Seal Type shamir Initialized true Sealed

Error: "Readiness probe failed: Key Value --- ----- Seal Type shamir Initialized true Sealed true Total Shares 5 Threshold 3 Unseal Progress 0/3 Unseal Nonce n/a Version 1.10.0 Storage Type file HA Enabled false"

    For this you need to unseal the vault.
        ```
        $kubectk exec vault-0 -n vault -- vault operator init
        ```
            (# The output will contain one root key and 5 sample keys)
            (# use any 3 keys out of the 5 sample keys  in the below commands to unseal the vault)
            (# Update the root key value in vault.secret.roottoken parameter in the values.yaml )
        ```
        $kubectl exec vault-0 -n vault  -- vault operator unseal key1
        $kubectl exec vault-0 -n vault  -- vault operator unseal key2
        $kubectl exec vault-0 -n vault  -- vault operator unseal key3
        ```