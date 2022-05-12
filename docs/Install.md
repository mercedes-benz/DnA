## **Install with docker-compose**

Docker Compose will help to start the application locally on your computer and provide support to develop and debug the docker containers in the local machine.

Software Prerequisites:

* Git
* Docker
* Docker Compose

Hardware Prerequisites :

* Recommend 8GB RAM
  
#### **Note**
  
  * *For windows user, enable WSL engine on Docker Desktop. Check [FAQ](./FAQ.md) to enable WSL* *
  * *Make sure your firewall is not restricting the npm and gradle packages of the docker files* *.

#### **Git Cloning** 

As a first step you need to clone the Git Repo in your local computer (this can be done by executing the below command on terminal/command prompt/some visual git client(GithubDesktop))
```
git clone https://github.com/mercedes-benz/DnA.git
```
Once when cloning is finishied , you will have a copy of the entire repository locally.Go to the deployment folder by executing the the below command (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder>>/deployment/
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
## **Install with Helm**

Helm helps you to deploy and manage Kubernetes applications in an easier way.

Prerequisites :

* Kubernetes Cluster
* Helm
* Docker Image Regitsry
* Kafka

#### **Git Cloning** 

As a first step you need to clone the Git Repo in your local computer (this can be done by executing the below command on terminal/command prompt/some_visual_git_client(GithubDesktop)).

```
git clone https://github.com/mercedes-benz/DnA.git
```
Once when cloning is finishied, you will have a copy of the entire repository locally .Go to the deployment folder by executing the the below command  (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder>>/deployment/
```
#### **Build & push images**

Execute the below command to create images of DnA-frontend,Dna-Backend, Bitnami-postgress ,Dashboard , malware , Vault, clamav, Naas-backend , ZooKeeper , Broker and Minio .
```
cd <<Clonned Folder>>/deployment/
docker-compose -f docker-compose-local-basic.yml build
```
Execute the below command to create storage-service images ( Storage-mfe and storage-be)
```
cd <<Clonned Folder>>/deployment/dockerfiles/storageService
docker-compose -f docker-compose-storage.yml build  
```
Refer the below commands for pushing the images to your reposirtory . Replace the contents that are enclosed with <<...>> to the respective values  
```
docker tag <<image_name_that_you_build_with_docker_compose>> <<your_repository_name>/<image_name_of_your_wish>>
docker push <<your_repository_name>/<image_name_of_your_wish>>
```
#### **Namespaces**

Refer the below content to understand which sub-charts belong to which namespace 

  * dna namespace contains "DnA-Backend, DnA-Frontend, Postgres"
  * clamav namespace contains "clamav service and malware-backend"
  * naas namespace contains "Naas-backend"
  * dashboard namespace contains "dashboard-backend"
  * vault namespace contains "vault service"
  * storage namespace contains "storage-service"

Refer the above list and create namespaces according to the services you would like to deploy using helm.
```
Kubectl create ns dna
kubectl create ns clamav
kubectl create ns naas
kubectl create ns dashboard
kubectl create ns vault
kubectl create ns storage
```
#### **values.yaml**

*Before proceeding with the installation, update the image names to the respective services in the    values.yaml

  File is located at 

  **<<Clonned Folder>>deployment\kubernetes\helm\values.yaml**

*For pulling the images from the registry, update the docker.configjson value in the values.yaml

  For more info on kubernetes secret for pulling the images , refer harbor-pull-secret manifest file.

  ```
  cat <clonnedFloder>\deployment\kubernetes\helm\charts\backend\templates\secrets\harbor-pull-secret.yaml
  ```
*List of services that dna application offering:
  Naas ( Notification as a service)
  Dashboard 
  Clamav
  Storage

*Set the below paramter to true for the services that you would like to deploy via sub-chart

  **enabled: true**
  

**After installing the kafka, update the value of "naasBroker" in values.yaml to kafka-service-FQDN.

#### **Install the DnA application using helm

Execute the below commands to deploy application on the kubernetes cluster using helm
```
cd <<Clonned Folder>>\deployment\kubernetes\helm
```
```
helm install dna . -f values.yaml
```
Execute the below command to list out the helm releases
```
helm list
```
To access the application using localhost , port-forward the dna-frontend-service
```
kubectl port-forward service/dna-frontend-service 7179:3000
```

**Vault service**
After installing the vault service , it will throw an error that "Readiness probe error in vault -- Seal Type shamir Initialized true Sealed"

To resolve this , intialize the vault service and unseal the root key .
```
kubectl exec vault-0 -n vault  -- vault operator init
```
After executing the above command , it will give us the root token and 5 keys . Save the root token and mention it in storagebe and backend sections of the values.yaml

We can unseal the vault service with any of the 3 keys out of 5 .
```
kubectl exec vault-0 -n vault  -- vault operator unseal <key_01>
kubectl exec vault-0 -n vault  -- vault operator unseal <key_02>
kubectl exec vault-0 -n vault  -- vault operator unseal <key_03>
```
![This is an image](./images/vault-unsealed.png)
For storing the secrets , go to vault service and enable the KV engine.

**Dashboard Service**
To enable the dashboard service go to the vaules.yaml and set the below parameter to true
```
enabledReports: true
```

**Clamav Service**
To use the clamav service mention the respective values to the below paramters in the values.yaml
```
enableAttachmentScan: true
enableMalwareService: true
avscanUri:    
avscanApiKey:   
avscanAppId: 
```
**Storage and Naas**
To enable the storage and naas service mention the respective values to the below parameters in the values.yaml
```
enableStorageService: true
storageMFEAppURL:
enableNotification: true
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

*If you face any issue with helm installation, refer [FAQ](./FAQ.md)

