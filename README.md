<!-- SPDX-License-Identifier: MIT -->

## DnA Platform (Data and Analytics Platform) and its features

Idea of the DnA Platform originated from the common challenge every bigger company has - need for a transparency in area of data and analytics. Soon after that, further features were envisioned and realized, this time with an idea to enable self-service, cloud independent platform based fully on open source that will simplify life and speed up work of everybody who is working with data. Let's look on those features DnA Platform enables you out of the box.

[Transparency feature](./docs/DnATransparency.md) helps you get a transparency on different initiatives and enables collaboration between people working on similar challenges

After we have realized transparency feature and rolled it out, we have checked other hurdles organization have to successfully realize data and analytics initiatives. Some of them are probably also familiar to you also:

1. Having tech platform to do the work and minimize onboarding time to it using full self-service approach not needing anybody to do something for you
2. Availability of data
3. Clarity on data governance/protection to-do's and their execution

As already transparency feature enables users to upload certain documents we had to ensure that nobody would upload malitious document so we realized the idea of

[Malware Scan As A Service Feature](./docs/DnAMalwareScanAsAService.md) which provides simple REST API to check all kind of different attachments for malicious code abstracting [ClamAV](https://github.com/Cisco-Talos/clamav) scanner usage and making it ready for web world.

As processing and transforming of data is the core of every data platform we have added also

[Data Pipeline Feature](./docs/DnADataPipeline.md) which integrates [Apache Airflow](https://github.com/apache/airflow) as major component in DnA Platform.

Once when data is prepared you are ready to do the magic with it you will need some kind of environment to perform computation on it, for this reason we have brought

[Jupyter Notebook Workspace Feature](./docs/DnAJupyterNotebookWorkspace.md) based on [JupyterHub](https://github.com/jupyterhub/jupyterhub) enabling you instant start without need to install something on your local computer.

## Upcoming Features

We are currently implementing one very important feature that will simplify life of data scientist - creating model training pipelines with a click of the mouse. This feature is based on Jupyter notebook extension [Kale](https://github.com/kubeflow-kale/kale) which then creates and managed [Kubeflow Pipelines](https://github.com/kubeflow/pipelines) for your project.

Beside this, we are creating Helm chart for the simplified installation of the entire DnA platform features.

Those features will be available in February 2022 together with possibility to enable each of those features through configuration paramether at the time of installation.

Dashboard transparency feature is also very relevant for every organization, it provides transparency on dashboards and reports being planned, build or already in use. Beside as is analysis this enables you strategic steering of what should be built in the future.

This feature will be available in March 2022.

Model serving feature is a kind of normal development of model training - once when you develop and train your ML model you need to make it available for usage - in scalable and secure way - so we are planning to enable model serving in form of REST API with a click of a mouse. For now we are considering to use [Seldon Core](https://github.com/SeldonIO/seldon-core) or [Kserve](https://github.com/kserve/kserve) as basic building block for this feature. In case you have different idea or hint on this please open discussion on this topic [here](https://github.com/Daimler/DnA/issues).

ETA for this feature is sometime second half of Q1 2022

As Data Pipelines, Jupyter notebook, Kubeflow Pipelines and other features require some kind of managed persistence layer we are considering integration of [MinIO](https://github.com/minio/minio) as a S3 storage model and on top of it adding [DeltaLake](https://github.com/delta-io/delta) enabling native Spark processing and ACID compliance.

List of future ideas is big and is being adjusted constantly so simply include yourself into discussion with us, we are looking forward to different ideas.




## Installation

If you just want to try DnA Platform please follow the [installation guide](./docs/SETUP-DOCKER-COMPOSE.md) to install it. Docker installation has only limited feature set and may not be suitable for production environment deployment. From the feature list only Transparency feature of Data & Analytics initiatives/solutions is available (most of other features require Kubernetes specifics so it is not possible to run it under Docker).

For a full production installation, we would recommend Kubernetes installation. We are currently preparing Kubernetes Helm chart (available in January/February 2022) where entire system will be available - including Jupyter notebooks with Kale, Apache Airflow, Kubeflow Pipeline. Stay tuned on this frequency for this :). In the mean time you can use previous docker instruction to deploy images in Kubernetes.



## Contributing

We welcome any contributions. As we are starting in the open source community we may need few weeks to settle and be able to accept your idea or contributions in efficient way. During January we will be bringing other features on the platform and then this repository will become our primary repo and place to collaborate with you, till then we are  still working in our private enterprise repo and syncing code manually.
If you want to contribute to this project, please read the [contributing guide](CONTRIBUTING.md).

## Code of Conduct

Please read our [Code of Conduct](https://github.com/Daimler/daimler-foss/blob/master/CODE_OF_CONDUCT.md) as it is our base for interaction.

## License

This project is licensed under the [MIT LICENSE](LICENSE).

## Provider Information

Please visit <https://www.daimler-tss.com/en/imprint/> for information on the provider.

Notice: Before you use the program in productive use, please take all necessary precautions,
e.g. testing and verifying the program with regard to your specific use.
The program was tested solely for our own use cases, which might differ from yours.
