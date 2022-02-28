# Hashicorp Vault chart

Check ORIGINAL_README.md. This chart is changed (in values.yaml) to use images and non-root user and also without
ingress. Chart itself was not changed. useful info on:

https://www.vaultproject.io/docs/platform/k8s/helm/run

## Create vault namespace

`kubectl create ns vault`

## Install vault into `vault` namespace using argocd

- Define vault project either
    - using UI and values from file
    - or via kubectl `kubectl -n argocd apply -f vault-argocd-project.yaml`
- Define application either
    - using helm and values `helm install -f values.yaml vault-int . -n vault`
    - or via kubectl `kubectl -n argocd apply -f vault-argocd-application.yaml`

## Initing and unsealing vault

Vault will not start until inited and unsealed.

## Accessing UI and init

`kubectl -n vault port-forward vault-0 8200:8200`

go to `http://localhost:8200/ui/vault/init`

Use UI to init and unseal. Write root token and unseal keys in 1Password

## Configuring Authentication Methods for ArgoCD secrets plugin

- Step 1: Enable AppRole auth method We are using approle method of authentication for argocd-vault-plugin. Enable it in
  UI.
  https://learn.hashicorp.com/tutorials/vault/approle#step-1-enable-approle-auth-method


- Step 2: Create a role with policy attached First, create a policy named `argocd` with following definition.

    - Select the Policies tab, and then select Create ACL policy.

    - Enter `argocd` in the Name text field.

    - Enter the following policy in the Policy field. Can be upraded later to allow user to read other secrets also
    ```
    # Read-only permission on all secrets. For prod use more specific policies
    path "*" {
        capabilities = [ "read" ]
    }
   ```

    - Click the Vault CLI shell icon (>_) to open a command shell. Execute the following command to create a
      new `argocd` role:
    ```
    vault write auth/approle/role/argocd token_policies="argocd" token_ttl=1h token_max_ttl=4h
    ```

    - Obtain role ID and secret id as per instructions
      from https://learn.hashicorp.com/tutorials/vault/approle#step-3-get-roleid-and-secretid
      `vault read auth/approle/role/argocd/role-id`
      `vault write -force auth/approle/role/argocd/secret-id`
      The RoleID is similar to a username; therefore, you will get the same value for a given role. In this case, the
      jenkins role has a fixed RoleID. While SecretID is similar to a password that Vault will generate a new value
      every time you request it.

    - These items are needed for argocd-vault-plugin config

## Creating sample secret entries of KV type in Vault to be read by argo

Go to http://localhost:8200/ui/vault/secrets and enable `kv` engine, then create secret on path `some-secret` with
key `some-secret-key`

Example of secret.yaml created by argo (pay attention to `kv/data` part in path). Any type of secret can be created
including image pull secret.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "nginx-1.fullname" . }}-secret-from-vault
  annotations:
    avp.kubernetes.io/path: "kv/data/some-secret"
  labels:
    app: {{ .Values.appLabel }}
type: Opaque
data:
  password: <some-secret-key | base64encode>
```
