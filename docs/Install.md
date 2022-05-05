## **Install with docker-compose**

Docker Compose will help to start the application locally on your computer and support to develop and debugging as a docker container in the local machine.

Prerequisites for this are:

* Git
* Docker
* Docker Compose
* Recommend 8GB RAM
  **Note:** For windows user, enable WSL engine on Docker Desktop. Check [FAQ](./FAQ.md) to enable WSL

As a first step you need to clone the Git Repo to your local computer, this is done by opening terminal/command prompt (or some visual git client you may have) and executing:

git clone https://github.com/mercedes-benz/DnA.git
Once when cloning is finalized you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location on your computer) and now you can simply start the application

```
cd <<Clonned Folder>>/deployment/
```
```
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
```
```
docker-compose -f docker-compose-local-basic.yml build
```

This above command will create images for DnA-frontend,Dna-Backend, Bitnami-postgress ,Dashboard , malware , Vault, clamav, Naas-backend , ZooKeeper , Broker , Minio .

Just like the below storage service command , we can build each service images independently.Check the repo "/deployement/<docker-compose-files>" and "/deployement/dockerfiles/<service-name/" for more info .

```
docker-compose -f docker-compose-storage.yml build  
```

to build the images of storage service

Once the images are build. Push the images to your docker repository.

Before proceeding with the installation, update the image names in the values.yaml

File is located at 

```
./<<Clonned Folder>>deployment\kubernetes\helm\values.yaml
```
For pulling the images from the registry, update the dockerconfigjson value in the values.yaml

For more info refer harbor-pull-secret manifest file

```
cat <clonnedFloder>\deployment\kubernetes\helm\charts\backend\templates\secrets\harbor-pull-secret.yaml
```
Then enable the particular subchart which you would like to deploy using helm-

set
```
enabled: true #setting true will deploy the subchart
```
Create namespace accordingly to the services you would like to deploy using helm .
```
Kubectl create ns dna
kubectl create ns clamav
kubectl create ns naas
kubectl create ns dashboard
kubectl create ns vault
kubectl create ns storage

```
It is mandatory to have kafka service inorder to run Dna-backend and Naas microservices .
Update the values of kafka url in values.yaml wherever required 
```
https://github.com/bitnami/charts/tree/master/bitnami/kafka
```

Once done, Execute the below command to deploy application on the kubernetes cluster using helm

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

