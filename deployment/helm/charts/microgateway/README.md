# Microgateway Setup For ClamAV

## Charts available at
https://git.com/dag/charts

``` bash
cd deployment/helm

helm repo add dag "specify the name of the repo"
helm repo update
helm pull dag/microgateway --untar
```

To check the files that are created by the helm chart type below

``` bash
helm template --namespace clamav clamav dag/microgateway -f values.yaml
```

To install the helm chart

``` bash
helm install -n clamav clamav . -f values.yaml
```

To upgrade the helm chart

``` bash
helm upgrade clamav . -n clamav --install -f values.yaml
```

You can also generate kubernetes ymls and apply them to your kubernetes cluster

```
helm template --namespace clamav clamav dag/microgateway -f values.yaml | kubectl apply -f -
```

