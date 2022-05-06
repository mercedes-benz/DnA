## **Install with docker-compose**

Docker Compose will help to start the application locally on your computer and provide support to develop and debug the docker containers in the local machine.

Software Prerequisites:

* Git
* Docker
* Docker Compose

Hardware Prerequisites :

* Recommend 8GB RAM
  
  **Note:** For windows user, enable WSL engine on Docker Desktop. Check [FAQ](./FAQ.md) to enable WSL

As a first step you need to clone the Git Repo in your local computer (this can be done by executing the below command on terminal/command prompt/some visual git client(GithubDesktop))
```
git clone https://github.com/mercedes-benz/DnA.git
```
Once when cloning is finishied , you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder>>/deployment/
```
Execute the below docker-compose command to create the DnA application
```
docker-compose -f docker-compose-local-basic.yml up -d
```
For Reference:

![image](/images/Docker-compose-sucess.png)

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

As a first step you need to clone the Git Repo in your local computer (this can be done by executing the below command on terminal/command prompt/some_visual_git_client(GithubDesktop)).

```
git clone https://github.com/mercedes-benz/DnA.git
```

Once when cloning is finishied, you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder>>/deployment/
```
Execute the below command to create images of DnA-frontend,Dna-Backend, Bitnami-postgress ,Dashboard , malware , Vault, clamav, Naas-backend , ZooKeeper , Broker and Minio .
```
docker-compose -f docker-compose-local-basic.yml build
```
Execute the below command to create storage-service images ( Storage-mfe and storage-be)
```
docker-compose -f docker-compose-storage.yml build  
```
Once the images are build. Push the images to your docker repository.

Before proceeding with the installation, update the image names and required parameters in the values.yaml

File is located at 

```
<<Clonned Folder>>deployment\kubernetes\helm\values.yaml
```
For pulling the images from the registry, update the docker.configjson value in the values.yaml

For more info on kubernetes secret for pulling the images , refer harbor-pull-secret manifest file.

```
cat <clonnedFloder>\deployment\kubernetes\helm\charts\backend\templates\secrets\harbor-pull-secret.yaml
```
Then enable the particular subchart which you would like to deploy using helm-

set
```
enabled: true #setting true will deploy the subchart
```
List of services in:
```
dna namespace contains "DnA-Backend, DnA-Frontend, Postgres"
clamav namespace contains "clamav service and malware-backend"
naas namespace contains "Naas-backend"
dashboard namespace contains "dashboard-backend"
vault namespace contains "vault service"
storage namespace contains "storage-service"
```
Refer the above list and create namespaces according to the services you would like to deploy using helm.
```
Kubectl create ns dna
kubectl create ns clamav
kubectl create ns naas
kubectl create ns dashboard
kubectl create ns vault
kubectl create ns storage

```
It is mandatory to have kafka service inorder to run Dna-backend and Naas-Backend microservices.

For installaing the kafka , refer the below repo 
```
https://github.com/bitnami/charts/tree/master/bitnami/kafka
```
After installing the kafka, update the value of naasBroker to kafka-service-FQDN in values.yaml

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

*For storing the secrets , go to vault service and enable the KV engine.

*Once when vault is intialized ,update the root key value in the values.yaml .For generating the root   key execute the below command in the vault container.
```
vault operator init
```

*If you face any issue with helm installation, refer [FAQ](./FAQ.md)

