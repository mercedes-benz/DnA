# How to serve a ML model

*The DnA platform offers a holistic open-source [MLOps pipeline](https://github.com/mercedes-benz/DnA/blob/kubeflow-tutorial-pipelines/docs/DnAMLOPsArchitecture.md) utilizing existing FOSS solutions. This tutorial demonstrates a) the three simple options provided for serving ML solutions using [KServe](https://github.com/kserve/kserve) directly from the DnA Platform environment and b) an example of how to get predictions from served models.*

# Option 1: Serving a model from the Kubeflow Central Dashboard

Users can serve their stored ML models directly from the Kubeflow Central Dashboard UI.

<img alt="DnA - Launching the KServe UI" src="/docs/images/KServeUI-1.gif" style="max-width:100%">


**Step 1**:

Copy the following yaml file and adjust:

- the *name* of the service
- the *predictor-type* (see supported predictor types [here](https://kserve.github.io/website/0.7/modelserving/v1beta1/serving_runtime/))
- the *storageUri* (see supported storage options [here](https://kserve.github.io/website/0.7/modelserving/storage/azure))

``` yaml
apiVersion: serving.kubeflow.org/v1beta1
kind: InferenceService
metadata:
  annotations:
    sidecar.istio.io/inject: 'false'
  name: name-of-the-service
spec:
 predictor:
  serviceAccountName: minio-models
  predictor-type:
    storageUri: s3://uri/of/the/model
 minReplicas: 0
```

**Step 2**:

Open the Models tab and paste the yaml file:

<img alt="DnA - Launching a Jupyter Notebook instance" src="/docs/images/KServeUI-2.gif" style="max-width:100%">


# Option 2: Serving a stored model from the Jupyter Notebooks


Users should import the *serve_from_uri* function in their Jupyter Notebook and serve their model by specifying:

- the *model_uri* (see supported storage options [here](https://kserve.github.io/website/0.7/modelserving/storage/azure))
- the *name* of the service
- the *predictor* type (see supported predictor types [here](https://kserve.github.io/website/0.7/modelserving/v1beta1/serving_runtime/))

``` py
from kale.common.serveutils import serve_from_uri

kfserver = serve_from_uri(model_uri= "model-uri",
                          name= "service-name",
                          wait= True,
                          predictor= "type")
```

<img alt="DnA - Serve a stored model from Jupyter Notebooks" src="/docs/images/KServe-ServeStoredModel.gif" style="max-width:100%">

# Option 3: Serving a developed model from the Jupyter Notebooks

Users should import the *serve* function in their Jupyter Notebook and serve their model by specifying:

- the *model* they want to serve
- the *name* of the service
- the *version* of the service

The declaration of the predictor type is not required, as the function detects it automatically in the background

``` py
from kale.common.serveutils import serve

kfserver = serve(model, "model-name", "version")
```

<img alt="DnA - Serve a developed model from the Jupyter Notebooks" src="/docs/images/KServeServeModel.gif" style="max-width:100%">


# Getting Predictions

Getting predictions from served ML models is simple as making an HTTP POST request. This can take place either from any CLI or directly from the Jupyter Notebooks as shown in the images below:

<img alt="DnA - Using Kale" src="/docs/images/KServePredictions-1.gif" style="max-width:100%">

<img alt="DnA - Using Kale" src="/docs/images/KServePredictions-2.gif" style="max-width:100%">


**Additional Info**

To explore the benefits of the Kubeflow Pipelines, please visit the official [Kubeflow Documentation](https://www.kubeflow.org/docs/components/pipelines/).

To explore the benefits of KServe, please visit the official [KServe Documentation](https://kserve.github.io/website).
