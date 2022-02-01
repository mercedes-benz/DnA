## **Install with docker-compose**

Docker Compose will help to start the application locally on your computer and support to develop and debugging as a docker container in the local machine.

Prerequisites for this are:

* Git
* Docker
* Docker Compose

**Note:** For Windows users, enable the WSL engine on Docker Desktop. Check the [FAQ](./FAQ.md) to enable WSL

as a first step you need to clone the Git Repo to your local computer, this is done by opening terminal/command prompt (or some visual git client you may have) and executing:

```
git clone https://github.com/mercedes-benz/DnA.git
```

Once when cloning is finalized you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location on your computer) and now you can simply start the application

```
cd <<Clonned Folder>>/deployment/

docker-compose -f docker-compose-local-basic.yml up
```

Wait for a few seconds and then open the website by going to http://localhost:8080 in your browser. If you made any changes on source files add `--build --force-recreate` args to docker-compose command. If you facing any issue with docker-compose, please refer [FAQ](./FAQ.md)

To stop the application

```
docker-compose -f docker-compose-local-basic.yml down
```

## Install with Helm

Helm helps you to deploy and manage Kubernetes applications in an easier way.

Prerequisites for this are:

* Kubernetes Cluster
* Helm

Once cloning is finalized you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location on your computer) and now you can simply run the below command to install the DnA App

```
cd <<Clonned Folder>>/deployment/helm

helm install dna .
```

Do Helm Upgrade, if you made changes on helm files

```
helm upgrade dna .
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
* [About Helm](https://helm.sh/)
* [About Kubernetes](https://kubernetes.io/)
* [Minikube ](https://minikube.sigs.k8s.io/docs/)- Local Kubernetes development
