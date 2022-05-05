## **Install with docker-compose**

Docker Compose will help to start the application locally on your computer and support to develop and debugging as a docker container in the local machine.

Prerequisites for this are:

* Git
* Docker
* Docker Compose
* Recommend 8GB RAM

**Note:** For windows user, enable WSL engine on Docker Desktop. Check [FAQ](./FAQ.md) to enable WSL

As a first step you need to clone the Git Repo to your local computer, this is done by opening terminal/command prompt (or some visual git client you may have) and executing:

```
git clone https://github.com/mercedes-benz/DnA.git
```

Once when cloning is finalized you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location on your computer) and now you can simply start the application

```
cd <<Clonned Folder>>/deployment/

docker-compose -f docker-compose-local-basic.yml up
```

Wait for a **2 minutes** and then open the website by going to http://localhost:8080 in your browser. If you made any changes on source files add `--build --force-recreate` args to docker-compose command. If you facing any issue with docker-compose, please refer [FAQ](./FAQ.md)

To stop the application

```
docker-compose -f docker-compose-local-basic.yml down
```

### **Deploy DnA App in Kubernetes using Helm**

Helm helps you to deploy and manage Kubernetes applications in an easier way.

Prerequisites for this are:

* Kubernetes Cluster
* Helm

As a first step you need to clone the Git Repo to your local computer, this is done by opening terminal/command prompt (or some visual git client you may have) and executing:

```
git clone https://github.com/mercedes-benz/DnA.git
```

Once when cloning is finalized you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location on your computer)

```
cd <<Clonned Folder>>/deployment/

docker-compose -f docker-compose-app.yml build
```

The above same command can be used to build other images  for dashboard,malware,naas,Minio and storage. Just change the particular file name in the command.

Once the images are build. Push the images to your docker repository.

Before proceeding with the installation, make sure to change the image paramater in values.yaml for particular microservice to pull the image from your own docker repository.

File is located at

```
cd <<Clonned Folder>>deployment\kubernetes\helm\values.yaml
```

Then enable the particular subchart which you would like to deploy using helm-

set

```
enabled: true #setting true will deploy the subchart
```

Once done, Execute the below command to deploy application on the kubernetes cluster. Make sure the namespace are already created before deploying the application on cluster.If not use the below command-

```
 Eg : kubectl create ns airflow
```

```
cd <<Clonned Folder>>\deployment\kubernetes\helm

helm install dna . -f values.yaml
```

To list helm release

```
helm list
```

Do Helm Upgrade, if you made changes on helm files

```
helm upgrade dna . -f values.yaml
```

To uninstall the helm app

```
helm uninstall dna
```

DnA Platform can be configured quite a lot, have a look at possible config parameters:

* [Environment Variables](./APP-ENV-CONFIG.md)

or follow simple instructions on how to use simple and free Open ID Connect identity provider

* [OpenId Connect with OKTA](./OPENID-CONNECT.md)

##### FAQ

* [About GIT](https://git-scm.com/doc)
* [Docker installation.](https://docs.docker.com/get-docker/)
* [About docker-compose.](https://docs.docker.com/compose/)
* [Helm installation](https://helm.sh/docs/intro/install/)
* [About Helm](https://helm.sh/docs/)


**Note:**

    hashiCorp vault readiness probe value will fail for first time with the below error 
        "Readiness probe failed: Key Value --- ----- Seal Type shamir Initialized true Sealed true Total Shares 5 Threshold 3 Unseal Progress 0/3 Unseal Nonce n/a Version 1.10.0 Storage Type file HA Enabled false"

        For this you need to unseal the root key .

        $kubectl exec vault-0 -n vault  -- vault operator unseal QoTLznQB2ZnjUPa+1XMs+jCFJX21lEocnqubQuWR7w1l
        $kubectl exec vault-0 -n vault  -- vault operator unseal pmvLcr5E3UzITy1+bvfFj+hovUI63KWJzQ6rh+/X0wUM
        $kubectl exec vault-0 -n vault  -- vault operator unseal IA5sB27LyzuORbMc9IdRtQ3oKAHgsGsnaWtnl3qKAKAb

    For storing the secrets , go to vault service and enable the KV engine 

