#### **Set up local DnA Platform instance with docker-compose**

Docker Compose will help to start the application locally on your computer and support to develop and debugging as a docker container in the local machine.

Prerequisites for this are:

* Git
* Docker
* Docker Compose

**Note:** For windows user, enable WSL engine on Docker Desktop. Check [FAQ](./FAQ.md) to enable WSL

as a first step you need to clone the Git Repo to your local computer, this is done by opening terminal/command prompt (or some visual git client you may have) and executing:

```
git clone https://github.com/Daimler/DnA.git
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

DnA Platform can be configured quite a lot, have a look at possible config parameters:

* [Environment Variables](./APP-ENV-CONFIG.md)

or follow simple instructions on how to use simple and free Open ID Connect identity provider

* [OpenId Connect with OKTA](./OPENID-CONNECT.md)

##### FAQ

* [About GIT](https://git-scm.com/doc)
* [Docker installation.](https://docs.docker.com/get-docker/)
* [About docker-compose.](https://docs.docker.com/compose/)
