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
    $cd <<Clonned Folder>>/deployment/

    $docker-compose -f docker-compose-local-basic.yml up

    ```

Wait for a **2 minutes** and then open the website by going to http://localhost:8080 in your browser. If you made any changes on source files add `--build --force-recreate` args to docker-compose command. If you facing any issue with docker-compose, please refer [FAQ](./FAQ.md)

To stop the application

    ```
    $docker-compose -f docker-compose-local-basic.yml down

    ```

### **Deploy DnA App in Kubernetes using Helm**

Helm helps you to deploy and manage Kubernetes applications in an easier way.

Prerequisites for this are:

    * Kubernetes Cluster
    * Helm

As a first step you need to clone the Git Repo to your local computer, this is done by opening terminal/command prompt (or some visual git client you may have) and executing:

    ```
    $git clone https://github.com/mercedes-benz/DnA.git

    ```

Once when cloning is finalized you will have a copy of the entire repository locally (replace <`<Cloned Folder>`> with actual location on your computer)

    ```
    $cd <<Clonned Folder>>/deployment/

    $docker-compose -f docker-compose-local-basic.yml build

    (# This command will create images for DnA-frontend,Dna-Backend, Bitnami-postgress ,Dashboard , malware , Vault, clamav, Naas-backend , ZooKeeper , Broker , Minio )

    (#Just like the below storage service command , we can build each service images independently.Check the repo "/deployement/<docker-compose-files>" and "/deployement/dockerfiles/<service-name/" for more info)

    $docker-compose -f docker-compose-storage.yml build  (#to build the images of storage service)
        

Once the images are build. Push the images to your docker repository.

Before proceeding with the installation, update the image names in the values.yaml 

    File is located at 

    ```
    $cd <<Clonned Folder>>deployment\kubernetes\helm\values.yaml
    
    ```

For pulling the images from the registry, update the dockerconfigjson value in the values.yaml
    
    For more info refer harbor-pull-secret manifest file

    ```
    $cat <clonnedFloder>\deployment\kubernetes\helm\charts\backend\templates\secrets\harbor-pull-secret.yaml

    ```


Then enable the particular subchart which you would like to deploy using helm-

    set

    ```
    enabled: true #setting true will deploy the subchart

    ```

Create namespace accordingly to the services you would like to deploy using helm . 

    For ref:
        dna namespace contains "Dna-Frontend , DnA backend , Bitnami-postgres"
        clamav namespace contains "malware-backend , clamav service"
        naas namespace contains "naas-backned"
        dashboard namespace contains "dashboar-backend"
        vault namespace contains "vault service"
        storage namespace contains "storag-fronted , storage-backend and minio"


    ```
    $Kubectl create ns dna 
    $kubectl create ns clamav
    $kubectl create ns naas
    $kubectl create ns dashboard
    $kubectl create ns vault
    $kubectl create ns storage

    ```


Once done, Execute the below command to deploy application on the kubernetes cluster using helm

    ```
    $cd <<Clonned Folder>>\deployment\kubernetes\helm

    $helm install dna . -f values.yaml

    ```

To list helm release

    ```
    $helm list

    ```

Do Helm Upgrade, if you made changes on helm files

    ```
    $helm upgrade dna . -f values.yaml

    ```

To uninstall the helm app

    ```
    $helm uninstall dna

    ```

If you want to install kafka you can refer the below kafka chart:

    * [Kafka](https://github.com/bitnami/charts/tree/master/bitnami/kafka)

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

    *hashiCorp vault readiness probe value will fail for first time with the below error 
        "Readiness probe failed: Key Value --- ----- Seal Type shamir Initialized true Sealed true Total Shares 5 Threshold 3 Unseal Progress 0/3 Unseal Nonce n/a Version 1.10.0 Storage Type file HA Enabled false"

        For this you need to unseal the vault.

            $kubectk exec vault-0 -n vault -- vault operator init

                (# The output will contain one root key and 5 sample keys)
                (# use any 3 keys out of the 5 sample keys  in the below commands to unseal the vault)
                (# Update the root key value in vault.secret.roottoken parameter in the values.yaml )

            $kubectl exec vault-0 -n vault  -- vault operator unseal key1
            $kubectl exec vault-0 -n vault  -- vault operator unseal key2
            $kubectl exec vault-0 -n vault  -- vault operator unseal key3

    *For storing the secrets , go to vault service and enable the KV engine 

