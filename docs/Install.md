## **Install with docker-compose**

Docker Compose will help to start the application locally on your computer and provide support to develop and debug the docker containers in the local machine.

Software Prerequisites:

* Git 2.35.1+
* Docker 20.10.13+
* Docker Compose v2.3.3

Hardware Prerequisites :

* Recommend 8GB RAM
  
#### **Note**
  
  * *For windows user, enable WSL engine on Docker Desktop. Check [FAQ](./FAQ.md) to enable WSL*.
  * *Make sure your firewall is not restricting the npm and gradle packages of the docker files*.

#### **Git Cloning** 

As a first step you need to clone the Git Repo in your local computer (this can be done by executing the below command on terminal/command prompt/some visual git client(GithubDesktop))
```
git clone https://github.com/mercedes-benz/DnA.git
```
Once when cloning is finishied , you will have a copy of the entire repository locally.Go to the deployment folder by executing the the below command (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder Path>>/deployment/
```
#### **Docker Compose**
Execute the below docker-compose command to create the DnA application
```
docker-compose -f docker-compose-local-basic.yml up -d
```
For Reference:

![This is an image](./images/Docker-compose-sucess.png)

Open the website (http://localhost:8080) in your browser. If you have made any changes in the source files add `--build --force-recreate` args to docker-compose command. If you face any issue with docker-compose,refer [FAQ](./FAQ.md)

To stop the application
```
docker-compose -f docker-compose-local-basic.yml down
```
-------------------------------------------------------------------------------------------------------
## **Install with Helm**

Helm helps you to deploy and manage Kubernetes applications in an easier way.

Prerequisites :

* Kubernetes Cluster 1.22+
* Helm v3.8.1
* kubectl 1.22+
* Kafka [Refer here](https://github.com/apache/kafka)
* Docker Image Regitsry

#### **Git Cloning** 

As a first step you need to clone the Git Repo in your local computer (this can be done by executing the below command on terminal/command prompt/some_visual_git_client(GithubDesktop)).

```
git clone https://github.com/mercedes-benz/DnA.git
```
Once when cloning is finishied, you will have a copy of the entire repository locally .Go to the deployment folder by executing the the below command  (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder Path>>/deployment/
```
#### **Build & push images**

Execute the below command to create images of DnA-frontend,Dna-Backend, Bitnami-postgress ,Dashboard , malware , Vault, clamav, Naas-backend , ZooKeeper , Broker and Minio .
```
cd <<Clonned Folder Path>>/deployment/
docker-compose -f docker-compose-local-basic.yml build
```
Execute the below command to create storage-service images ( Storage-mfe and storage-be)
```
cd <<Clonned Folder Path>>/deployment/dockerfiles/storageService
docker-compose -f docker-compose-storage.yml build  
```
Refer the below commands for pushing the images to your reposirtory . Replace the contents that are enclosed in <<...>> to the respective values  
```
docker tag <<image_name_that_were_built_with_docker_compose>> <<your_repository_name/image_name_of_your_wish>>
docker push <<your_repository_name/image_name_of_your_wish>>
```
#### **Namespaces**

Execute the below commands to create namespaces
```
Kubectl create ns dna
kubectl create ns clamav
kubectl create ns naas
kubectl create ns dashboard
kubectl create ns vault
kubectl create ns storage
```
#### **values.yaml**

update the image names of the respective services in the values.yaml
```
<<Clonned Folder Path>>deployment\kubernetes\helm\values.yaml
```
For pulling the images from the registry, update the docker.configjson value in the values.yaml
For more info on kubernetes secret for pulling the images , refer harbor-pull-secret manifest file.
  ```
  cat <clonnedFloderPath>\deployment\kubernetes\helm\charts\backend\templates\secrets\harbor-pull-secret.yaml
  ```
After installing the kafka, update the `naasBroker` parameter value in values.yaml to `kafka-service-FQDN`.

#### **Helm**

Execute the below commands to deploy application on the kubernetes cluster using helm
```
cd <<Clonned Folder Path>>\deployment\kubernetes\helm
helm install dna . -f ./charts/values.yaml
```
Execute the below command to list out the helm releases
```
helm list
```
**Vault service**

After installing the vault service , it will throw an error that `Readiness probe error in vault – Seal Type shamir Initialized true Sealed`
To resolve this , intialize the vault service and unseal the root key .
```
kubectl exec vault-0 -n vault  -- vault operator init
```
After executing the above command , it will give us the root token and 5 keys . Save the root token and mention it in storagebe and backend sections of the values.yaml
We can unseal the vault service with any of the `3 keys out of 5`.
```
kubectl exec vault-0 -n vault  -- vault operator unseal <key_01>
kubectl exec vault-0 -n vault  -- vault operator unseal <key_02>
kubectl exec vault-0 -n vault  -- vault operator unseal <key_03>
```
![This is an image](./images/vault-unsealed.PNG)
Execute the below commands to enable the kv engine for storing the secrets:
```
kubectl exec vault-0 -n vault  -- vault operator login <<Vault_root_token>>
kubectl exec vault-0 -n vault  -- vault secrets enable -version=2 -path=kv kv
```
**Attachment scan**

To scan the attachments in solution , set the respective values to the below paramters in the values.yaml
#Open the http://localhost:7179 and go to `myservices->malwarescan -> Genrate the apikey` and copy the application key and application id , copy the same into the below parameters
```
avscanApiKey:   
avscanAppId: 
```
**Upgrading**

Do Helm Upgrade, if you made changes on helm files
```
helm upgrade dna . -f ./charts/values.yaml
```

**Accessing the application with localhost**

Port-forward the dna-frontend and storage-mfe service to any port_of_your_wish.
Eg:
```
kubectl port-forward service/storage-mfe 7175:80
kubectl port-forward service/dna-frontend-service 7179:3000
```
If you are not using 7175 and 7179 ports then change the below parameter values in values.yaml accordingly 
```
storageMFEAppURL:
PROJECTSMO_CONTAINER_APP_URL:
```

**Uninstalling**

To uninstall the helm app

```
helm uninstall dna
```
-------------------------------------------------------------------------------------------------------
DnA Platform can be configured quite a lot, have a look at possible config parameters:

* [Environment Variables](./APP-ENV-CONFIG.md)

Follow simple instructions on how to use simple and free Open ID Connect identity provider

* [OpenId Connect with OKTA](./OPENID-CONNECT.md)
##### FAQ

* [About GIT](https://git-scm.com/doc)
* [Docker installation.](https://docs.docker.com/get-docker/)
* [About docker-compose.](https://docs.docker.com/compose/)
* [Helm installation](https://helm.sh/docs/intro/install/)
* [About Helm](https://helm.sh/docs/)

**Troubleshooting**

*If you face any issue with helm installation, refer [FAQ](./FAQ.md)

