### **Deploy DnA App in Kubernetes using Helm**

Helm helps you to deploy and manage Kubernetes applications in an easier way.

Prerequisites for this are:

* Kubernetes Cluster
* Helm

As a first step you need to clone the Git Repo to your local computer, this is done by opening terminal/command prompt (or some visual git client you may have) and executing:

```
git clone https://github.com/Daimler/DnA.git
```

Once when cloning is finalized you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location on your computer)

```
cd <<Clonned Folder>>/deployment/

docker-compose -f docker-compose-app.yml build
```

The above same command can be used to build other images  for airflow,dashboard,malware,naas,notebook. Just change the particular file name in the command.

Once the images are build. Push the images to your docker repository.

Before proceeding with the installation, make sure to change the image paramater in values.yaml for particular microservice to pull the image from your own docker repository.

File is located at

```
cd <<Clonned Folder>>deployment\helm\values.yaml
```
Then enable the particular subchart which you would like to deploy using helm-

set 
```
enabled: true #setting true will deploy the subchart
```

Once done, Execute the below command to deploy application on the kubernetes cluster. Make sure the namespace are already created before deploying the application on cluster.If not use the below command-

```
kubectl create ns airflow
```

```
cd <<Clonned Folder>>/deployment/helm

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

