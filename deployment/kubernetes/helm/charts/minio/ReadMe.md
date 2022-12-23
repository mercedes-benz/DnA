### **Setup Minio on Kubernetes Cluster**

1. Download the [Helm Bitnami chart](https://artifacthub.io/packages/helm/bitnami/minio).
2. Update the `values.yaml` based on the requirement. Basic properties need to update if your pull images from private  registry.

   ```

   ##Update below properties
   global:
     imageRegistry: docker.io
   ##Update the image pull secret name as array
     imagePullSecrets:
       name: pull-secret
    #update the storage class 
     storageClass: ""

   ##Update Images name
   image:
     registry: docker.io
     repository: bitnami/minio
     tag: 2022.1.28-debian-10-r0

   clientImage:
     registry: docker.io
     repository: bitnami/minio-client
     tag: 2022.1.25-debian-10-r2

   ##Enable Network Policy for POD communication
   networkPolicy:
     enabled: true

   ## Update the authentication
   ## Password length should be min 10 and strong. If a password is provided as empty, minio create a new password
   auth:
     rootUser: 
     rootPassword: 
   ```
3. To enable distributed(high availability) mode of storing the data. For distributed mode require minimum 2 node cluster setup. Update the following properties in `values.yaml`

   ```
   mode: distributed
   ```
4. Creating a separate namespace is optional. In our case its `minio`.

   ```
   kubectl create namespace minio
   ```
5. Deploy the image pull secret object.

   ```
   kubectl apply -f <<file name>>> -n minio
   ```
6. Deploy the minio on Kubernetes. `cd `to the directory of the helm `chart.yaml`.

   ```
   helm install my-release . -n minio
   ```
7. Verify the minio setup is successful by running the minio client.

   Before that export the user name and password for the minio server. By running the below command.

   ```
   export ROOT_USER=$(kubectl get secret --namespace <<namespaoce>> <<secret object name>> -o jsonpath="{.data.root-user}" | base64 --decode)
   export ROOT_PASSWORD=$(kubectl get secret --namespace <<namespaoce>> <<secret object name>> -o jsonpath="{.data.root-password}" | base64 --decode)
   ```
   In our case:

   ```
   export ROOT_USER=$(kubectl get secret --namespace minio my-release-minio -o jsonpath="{.data.root-user}" | base64 --decode)
   export ROOT_PASSWORD=$(kubectl get secret --namespace minio my-release-minio -o jsonpath="{.data.root-password}" | base64 --decode)
   ```
   Run the below command to verify the minio setup.

   ```
      kubectl run --namespace minio  my-release-minio-client \
        --rm --tty -i --restart='Never' \
        --env MINIO_SERVER_ROOT_USER=$ROOT_USER \
        --env MINIO_SERVER_ROOT_PASSWORD=$ROOT_PASSWORD \
        --env MINIO_SERVER_HOST=my-release-minio \
        --labels="my-release-minio-client=true" \
        --image minio/minio-client:2022.1.25-debian-10-r2 -- admin info minio
   ```
   Once it shows the successful signature then minio server is working.
8. Access the Minio Web View/API Console. Do a port forwarding to access it on local.

   ```
   #Web View
   kubectl port-forward --namespace minio svc/my-release-minio 9001:9001

   #API 
   kubectl port-forward --namespace minio svc/my-release-minio 9000:9000

   ```
   Open the application on [http://127.0.0.1:9001]() use username as the value of `$ROOT_USER` and for password `$ROOT_PASSWORD`
   
9. If you're going to upgrade helm chart. Before that update the `password` property in `values.yaml`.

   ```
   ## Update the authentication
   auth:
     rootUser: 
     rootPassword: 
   ```
10. To uninstall the minio server

    ```
    helm delete my-release
    ```
