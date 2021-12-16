## DNA Web App

#### **Local Development with docker-compose**

Docker Compose will help to start the application and support to develop and debug as docker container in the local machine.

Prerequisites tool are

* Git
* Docker
* Docker Compose

Clone the Git Repo

```
git clone https://github.com/Daimler/DnA.git
```

To start the application

```
cd <<Clonned Folder>>/deployment/

docker-compose -f docker-compose-local-basic.yml up
```

If you made any changes on source files add `--build --force-recreate` args to docker-compose command.

##### Links-

* [Environment Variables](./APP-ENV-CONFIG.md)
* [OpenId Connect with OKTA](./OPENID-CONNECT.md)

##### FAQ

* [About GIT](https://git-scm.com/doc)
* [Docker installation.](https://docs.docker.com/get-docker/)
* [About docker-compose.](https://docs.docker.com/compose/)
