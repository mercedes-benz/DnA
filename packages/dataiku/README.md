# AD-integrated dataiku installation scripts

## Environment variables

Before provisioning infrastructure or configuring, it is important to set the following environment variables:

| Name                           | Description       |
| ------------------------------ | ----------------- |
| DSS_LICENSE_FILE               | Path to JSON file |
| SAML_IDP_METADATA_FILE         | Path to XML file  |
| SAML_ENTITY_ID                 | Identifier, found in your Enterprise App, Single sign-on section of Azure                  |
| AZURE_APP_ID                   | ID of Enterprise App in Azure AD                  |
| AZURE_APP_SECRET               | Secret of corresponding App in Azure App Registration                  |
| AZURE_TENANT_ID                | ID of your used AD tenant |
| AZURE_DSS_LICENSE_GROUP_PREFIX | Description of user group in DSS                  |
| DSS_INSTANCE_ID                | Description of instance                  |
| DSS_EXTOLLO_SSL_KEY_FILE       | extollo specific |
| DSS_EXTOLLO_SSL_CRT_FILE       | extollo specific |
| VM_ADMIN_USER                  | name of the user used to access the svm via ssh eg. devadmin for dev instance (used for UIF)|
| IS_TRAINING_ENV                | if 'true', 'True' or '1' use the logic for the training environment. If not set then behaves as false|
| DNA_PLATFORM_URL               | url for the platform eg. https://my-platform.com |
| GRAFANA_STREAM_ID              | name of the stream from dataiku instance to grafana eg. dataiku_dev|
| GRAFANA_API_KEY                | admin api key for Grafana|
| IS_AUTOMATION_NODE             | if 'true', 'True' or '1' use the logic for automation node. If not set then behaves as false|
| AUT_NODE_SSL_CHAIN             | Path to pem file with all ssl certificates for automation node|



## Azure provisioning

From the azure/ directory:
```
terraform init
```
```
terraform apply
```
This will provision infrastructure and run ansible scripts in the following order:

    .
    ├── ...
    ├── azure                               # main folder to interact with infrastructure and configs on Azure         
    │   ├── main.tf                         # provisions infrastructure and the dataiku module
    │   ├── modules                         # terraform modules 
    │     │    ├── dataiku
    │     │       ├── main.tf               # provisioning of DSS instance infrastructure and ansible playbooks
    │   ├── ansible
    │       ├── playbook.yml                # playbook to configure dataiku and letsencrypt 
    │       ├── roles
    │            ├── dataiku                # ansible role for dataiku 
    │                ├── tasks
    │                    ├── main.yml       # main ansible playbook to configure dataiku
    │            ├── letsencrypt            # ansible role to manage certificates
    │                ├── tasks
    │                    ├── main.yml       # main playbook to configure cert management
    │                   
    └── ...



## VM provisioning

```
export DSS_VM_PUBLIC_IP=$(terraform output -raw dataiku_vm_public_ip)
export DSS_VM_SSH_HOSTNAME=$(terraform output -raw dataiku_vm_ssh_hostname)

cd ansible/
ansible-playbook playbook.yml -i $DSS_VM_SSH_HOSTNAME, --private-key $SSH_KEY_PATH
```

