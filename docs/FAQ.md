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

#### 2.Inavlid Flag value dna-base:frontend

```
2 errors occurred:
* Status: pull access denied for dna-base, repository does not exist or may require 'docker login': denied: requested access to the resource is denied, Code: 1
* Status: invalid from flag value dna-base:frontend: pull access denied for dna-base, repository does not exist or may require 'docker login': denied: requested access to the resource is denied, Code: 1
```

If you are facing the above issue, it could be the reason for the timeout of node modules installed inside the docker image. Please run same the docker-compose up command again. Please run the same docker-compose up command again. 
