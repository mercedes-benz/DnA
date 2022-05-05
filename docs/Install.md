## **Install with docker-compose**

Docker Compose will help to start the application locally on your computer and provide support to develop and debug the docker container in the local machine.

Prerequisites for this are:

* Git
* Docker
* Docker Compose
* Recommend 8GB RAM
  
  **Note:** For windows user, enable WSL engine on Docker Desktop. Check [FAQ](./FAQ.md) to enable WSL

As a first step you need to clone the Git Repo to your local computer, this can be done by executing the below command on terminal/command prompt/some visual git client.
```
git clone https://github.com/mercedes-benz/DnA.git
```
Once when cloning is finishied  you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder>>/deployment/
```
Execute the below docker-compose command to create the DnA application
```
docker-compose -f docker-compose-local-basic.yml up
```

Wait for a **2 minutes** and then open the website by going to http://localhost:8080 in your browser. If you made any changes on source files add `--build --force-recreate` args to docker-compose command. If you face any issue with docker-compose, please refer [FAQ](./FAQ.md)

To stop the application

```
docker-compose -f docker-compose-local-basic.yml down
```

### **Deploy DnA App in Kubernetes using Helm**

Helm helps you to deploy and manage Kubernetes applications in an easier way.

Prerequisites for this are:

* Kubernetes Cluster
* Helm
  
As a first step you need to clone the Git Repo to your local computer, this can be done by executing the below command on terminal/command prompt/some_visual_git_client(GithubDesktop).

```
git clone https://github.com/mercedes-benz/DnA.git
```

Once when cloning is finishied  you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location path of your computer)
```
cd <<Clonned Folder>>/deployment/
```
Execute the below command for create images of DnA-frontend,Dna-Backend, Bitnami-postgress ,Dashboard , malware , Vault, clamav, Naas-backend , ZooKeeper , Broker , Minio .
```
docker-compose -f docker-compose-local-basic.yml build
```
Execute the below command for creating storage-service images ( Storage-mfe and storage-be)
```
docker-compose -f docker-compose-storage.yml build  
```
Just like the above storage-service command, we can build each service images independently. For more info refer the repository for docker-compose files of different services ("/deployment/<docker-compose-files>") and dockerfiles for different services ("/deployment/dockerfiles/<service-name>")

Once the images are build. Push the images to your docker repository.

Before proceeding with the installation, update the image names in the values.yaml

File is located at 

```
./<<Clonned Folder>>deployment\kubernetes\helm\values.yaml
```
For pulling the images from the registry, update the dockerconfigjson value in the values.yaml

For more info on kubernetes secret for pulling the images , refer harbor-pull-secret manifest file

```
cat <clonnedFloder>\deployment\kubernetes\helm\charts\backend\templates\secrets\harbor-pull-secret.yaml
```
Then enable the particular subchart which you would like to deploy using helm-

set
```
enabled: true #setting true will deploy the subchart
```
List of services according to the different namespaces are :
```
dna namespace contains "DnA-Backend, DnA-Frontend, Postgres"
clamav namespace contains "clamav service and malware-backend"
naas namespace contains "Naas-backend"
dashboard namespace contains "dashboard-backend"
vault namespace contains "vault service"
storage namespace contains "storage-service"
```
Create namespaces according to the services you would like to deploy using helm .
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

Execute the below command to deploy application on the kubernetes cluster using helm

```
cd <<Clonned Folder>>\deployment\kubernetes\helm
```
```
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

*For storing the secrets , go to vault service and enable the KV engine

