# Vault Helm Chart

This repository contains the official HashiCorp Helm chart for installing
and configuring Vault on Kubernetes. This chart supports multiple use
cases of Vault on Kubernetes depending on the values provided.

For full documentation on this Helm chart along with all the ways you can
use Vault with Kubernetes, please see the
[Vault and Kubernetes documentation](https://www.vaultproject.io/docs/platform/k8s/).

## Prerequisites

To use the charts here, [Helm](https://helm.sh/) must be configured for your
Kubernetes cluster. Setting up Kubernetes and Helm is outside the scope of
this README. Please refer to the Kubernetes and Helm documentation.

The versions required are:

* **Helm 3.0+** - This is the earliest version of Helm tested. It is possible
  it works with earlier versions but this chart is untested for those versions.
* **Kubernetes 1.16+** - This is the earliest version of Kubernetes tested.
  It is possible that this chart works with earlier versions but it is
  untested.

## Install Vault with Injector

Before installing the vault, update the following properties in values.yaml file

| Name                           | Default                   | Description                                                                 |
| ------------------------------ | ------------------------- | --------------------------------------------------------------------------- |
| injector.enabled               | `"-"`                   | Enable `true` to install injector                                         |
| injector.image.repository      | `"hashicorp/vault-k8s"` | Vault-k8s image, update image repo if you are pointing the private registry |
| injector.agentImage.repository | `"hashicorp/vault"`     | Agent image, update image repo if you are pointing the private registry     |
| server.image.repository        | `"hashicorp/vault"`     | Vault image, update image repo if you are pointing the private registry     |

Helm Install

```console
helm install vault hashicorp/vault --namespace vault
```

Unseal the vault once it is installed successfully.

Optional:

If your Kubernetes cluster has the policy to restrict pod communication.

```yml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-all
spec:
  podSelector: {}
  ingress:
    - {}
  policyTypes:
    - Ingress
```

Apply the `network-policy.yaml`

```console
kubectl apply -f network-policy.yaml -n vault
```

## Auth Vault with Kubernetes

1. Shell into the vault pod

   ```console
   kubectl exec -i -t -n vault vault-0 -c vault -- sh
   ```
2. Authenticate vault with root token

   ```console
   vault login
   ```
3. Enable Kubernetes Authentication

   ```console
   vault auth enable kubernetes
   ```
4. Configure Kubernetes properties

   ```console
   vault write auth/kubernetes/config \
   token_reviewer_jwt="$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" \
   kubernetes_host="https://$KUBERNETES_SERVICE_HOST:$KUBERNETES_SERVICE_PORT" \
   kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt 
   ```

Now you Vault and Injector have permission to communicate with Kubernetes.

## Example

This example will help to inject the secret into your container using the sidecar mechanism.

Let's consider an example, I am going to deploy the java application called `dashboard-service` into the `dashboard` namespace. Dashboard service has an environment variable called `API_DB_URL`. This value has to come from the vault secret which is stored in path `DnA\dashboard`.

### On the Vault side

##### Create a secret on vault

1. Shell into the vault pod

   ```console
   kubectl exec -i -t -n vault vault-0 -c vault -- sh
   ```
2. Authenticate vault with root token

   ```console
   vault login
   ```
3. Enable KV secret engine

   ```console
   vault secrets enable -version=2 -path=DnA kv
   ```
4. Add secret to DnA path

   ```console
   vault kv put DnA/dashboard API_DB_URL=jdbc:postgresql://postgresql.dna.svc.cluster.local:64000/dashboard
   ```

##### Create a policy to access the secret

```console
vault policy write dna-policy - <<EOF
path "DnA*" {
capabilities = ["read","list"]
}
EOF
```

Here the policy name is `dna-policy` and has `read`, `list` access to all secret which is present on `DnA` path

##### Create a kubernetes role

```console
vault write auth/kubernetes/role/dna \
bound_service_account_names=vault-auth \
bound_service_account_namespaces=dashboard \
policies=dna-policy
```

Vault k8s role is required to map vault secret policy and provide the access to pods on the particular namespace. In my case,

* `dna` - Role
* `dna-policy` - Policy is associated with vault secret
* `vault-auth` - Service account created on namespace`dashboard`

### On Kubernetes manifest side

This section covers the `dashboard-service` deployment with the vault injector.

##### Create a service account

```console
kubectl create serviceaccount vault-auth --namespace dashboard
```

The service account name should be the same which is used at the time of the Kubernetes role creation step. In my case, the service account name is `vault-auth`.

Optional:

If your Kubernetes cluster has the policy to restrict pod communication.

```yml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-all
spec:
  podSelector: {}
  ingress:
    - {}
  policyTypes:
    - Ingress
```

Apply the `network-policy.yaml`

```console
kubectl apply -f network-policy.yaml -n dashboard
```

##### Add Vault Annotation on pod manifest

Add the following annotation at pod manifest level to read the secret from the vault.

```yaml
      annotations:
        vault.hashicorp.com/agent-inject: 'true'
        vault.hashicorp.com/role: '<<Vault Kubernetes Role>>'
        vault.hashicorp.com/agent-inject-secret-name_of_the_secret: '<<vault secret path>>'
        # Environment variable export template
        vault.hashicorp.com/agent-inject-template-name_of_the_secret: |
          {{- with secret <<vault secret path>> -}}
            {{- range $k, $v := .Data.data }}
                export {{ $k }}="{{ $v }}"
            {{- end }}
          {{- end }}

```

Now the vault initializes the secret as a file and mount it at the pod level.

| Name                      | Description                                                   |
| ------------------------- | ------------------------------------------------------------- |
| `Vault Kubernetes Role` | Name of the Kubernetes role created on vault                  |
|  `name_of_the_secret`   | Name of the secret. It should be unique at the template level |
| `vault secret path`     | Secret path of the vault                                      |

> Note: Sidecar container doesn't modify the template of a pod specification. It will store the secret as a file in the pod. An application that is going to use that secret, has to configure to read it from the file or system environment.

Below, the template export the secret as an environment variable to the Nginx container.

```yaml
    spec:
      serviceAccountName: '<<service-account-name>>'
      containers:
        - name: ngnix
          image: ngnix
          imagePullPolicy: Always
          command: ["/bin/sh", "-c"]
          args: [" . /vault/secrets/name_of_the_secret && <<Docker Entry Point>>"]
```

Example `manifest`:

```yaml
# Source: dashboard/templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dashboard-service
  namespace: dashboard
  labels:
    name: dashboard-service
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      name: dashboard-service
  template:
    metadata:
      labels:
        name: dashboard-service
      annotations:
        vault.hashicorp.com/agent-inject: 'true'
        vault.hashicorp.com/role: 'dna'
        vault.hashicorp.com/agent-inject-secret-dashboard: 'DnA/data/dashboard'
        vault.hashicorp.com/agent-inject-template-config: |
          {{- with secret "DnA/data/dashboard" -}}
            {{- range $k, $v := .Data.data }}
                export {{ $k }}="{{ $v }}"
            {{- end }}
          {{- end }}

    spec:
      serviceAccountName: vault-auth
      containers:
        - name: dashboard-service
          image: dashboard-service
          imagePullPolicy: Always
          command: ["/bin/sh", "-c"]
          args: [" . /vault/secrets/dashboard && java -jar /dashboard-lib-1.0.0.jar"]
          ports:
            - containerPort: 7173
              name: api
              protocol: TCP
```

Reference:

[Vault SideCar Injector](https://www.vaultproject.io/docs/platform/k8s/injector)
