<!-- SPDX-License-Identifier: MIT -->

<p align="center">
<img alt="DnA Logo" src="./packages/frontend/public/images/solutionLogoImages/thumbnails/default.jpg" height="150" style="max-width:100%">
</p>
<h1 align="center">DnA Platform
<p align="center">
<a href="https://github.com/Daimler/DnA/blob/master/LICENSE"><img alt="GitHub license" src="https://img.shields.io/github/license/Daimler/DnA?color=blue"></a>
<img alt="GitHub contributors" src="https://img.shields.io/github/contributors/Daimler/DnA?color=blue">
<a href="https://github.com/Daimler/DnA/stargazers"><img alt="GitHub stars" src="https://img.shields.io/github/stars/Daimler/DnA?color=blue"></a>
<a href="https://github.com/Daimler/DnA/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/Daimler/DnA?color=blue"></a>
<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/Daimler/DnA?color=blue">  
</h1>
</p>

## DnA Platform (Data and Analytics Platform) and its features

The idea of the DnA Platform originated from the common challenge every big company has - the need for transparency in the data and analytics area. In addition, several features were envisioned and realized, this time with the idea to enable a self-service and cloud-independent platform based holistically on open-source software. The goal is to simplify life and speed up the work of everybody who is working with data. Let's look at the features DnA Platform offers out of the box.

| Feature Name  | Description  | Availability in Github  | 
|---|---|---|
| [DnA Portal Feature](./docs/DnAPortal.md)    | Wrapper feature that packages all features into one streamlined user experience.  | Since 24.12.2021  | 
| [Solution Transparency feature](./docs/DnATransparency.md)    | Feature that enables you getting transparency on different initiatives around Data/ML/AI and collaboration between people working on similar activities.  | Since 24.12.2021  | 
| [Malware Scan As A Service Feature](./docs/DnAMalwareScanAsAService.md)  |  Feature that provides a simple REST API to check all kinds of attachments for malicious code, abstracting the [ClamAV](https://github.com/Cisco-Talos/clamav) scanner usage and making it ready for web world.  | End of February 2022   | 
|  [Data Pipeline Feature](./docs/DnADataPipeline.md)  | This feature integrates [Apache Airflow](https://github.com/apache/airflow) as major component in DnA Platform, enabling processing and transforming of data  |  End of February 2022  | 
|  [Jupyter Notebook Workspace Feature](./docs/DnAJupyterNotebookWorkspace.md)  | Feature based on [JupyterHub](https://github.com/jupyterhub/jupyterhub) enables users to start working on their models without any additional software installation.   |  End of February 2022   |  
| [Dataiku integration feature](./docs/DnADataikuWorkspace.md)   | Is only feature that does not follow open source principle completely as [Dataiku](https://doc.dataiku.com/dss/latest/concepts/index.html) is a licensed product. Still even for Dataiku license product we are offering integration into DnA Platform in case you are operating it also (if not simply disable it in configuration).  |   End of February 2022  |  


 

## Upcoming Features

We are currently implementing a crucial MLOPs feature that will simplify the life of data scientists - creating model training pipelines with a click of the button. The overall DnA MLOPs Architecutre can be found [here](https://github.com/Daimler/DnA/blob/master/docs/DnAMLOPsArchitecture.md). This feature is based on the Jupyter Notebook extension [Kale](https://github.com/kubeflow-kale/kale) which, automatically generates, uploads, and runs  [Kubeflow Pipelines](https://github.com/kubeflow/pipelines) for your project. As a result, this will reduce the technical dept between data scientists and software engineers, eliminate their interdependencies, and enhance the user experience in a simple, transparent, compliant to data privacy regulations, and free of cost way.

In addition, we are working on creating a Helm chart for the simplified installation of the entire DnA platform features.

Those features will be available in February 2022,  together with the possibility to manually enable or disable each of those features via configuration parameters at the time of installation.

Next, the Dashboard transparency feature is considered crucial for every organization. It provides transparency on dashboards and reports planned, built, or already in use. This analysis enables strategic steering of what organizations should develop in the future.

This feature will be available in March 2022.

Once you develop and train your ML model, you often need to make it available for production usage - in a scalable and secure way. We plan to enable model serving in the form of a REST API and in a configuration-free way. Soon, we are thinking of integrating [Seldon Core](https://github.com/SeldonIO/seldon-core) or [Kserve](https://github.com/kserve/kserve) as the main building blocks for this feature. If you have some ideas or hints on this topic please let us know [here](https://github.com/Daimler/DnA/issues).

ETA for this feature is the second half of Q1 2022.

As Data Pipelines, Jupyter notebook, Kubeflow Pipelines, and other features require some kind of managed persistence layer, we are considering the integration of  [MinIO](https://github.com/minio/minio) as an S3 storage model along with the addition of [DeltaLake](https://github.com/delta-io/delta) to enable native Spark processing and ACID compliance.

The list of future ideas is big and it is constantly being adjusted, so please include yourself in the discussion! We are looking forward to different ideas!

## Installation

Please follow the [installation guide](./docs/SETUP-DOCKER-COMPOSE.md), if you want to try the DnA Platform. The Docker installation has a limited feature set and may not be suitable for production environments. Since most of the other features require Kubernetes, only the Transparency feature of Data & Analytics initiatives/solutions is available from the feature list. Docker).

For a complete production installation, we recommend the Kubernetes installation. We are currently preparing a Kubernetes Helm chart (available in January/February 2022) where the entire system will be available - including Jupyter notebooks with Kale, Apache Airflow, Kubeflow Pipelines. Stay tuned on this frequency for this :). In the meantime, you can use the previously mentioned docker instruction to deploy images in Kubernetes.

## Contributing

We welcome any contributions. As we are a freshly created open source community, we may need a few weeks to settle and be able to accept your ideas or contributions efficiently. During January 2022, we will bring other features on the platform, and then this repository will become our primary repo and place to collaborate with you. Until then, we will still work on our private enterprise repo and syncing code manually. If you want to contribute to the project please read the [contributing guide](CONTRIBUTING.md).

## Code of Conduct

Please read our [Code of Conduct](https://github.com/Daimler/daimler-foss/blob/master/CODE_OF_CONDUCT.md) as it is our base for interaction.

## License

This project is licensed under the [MIT LICENSE](LICENSE).

## Provider Information

Please visit <https://www.daimler-tss.com/en/imprint/> for information on the provider.

Notice: Before you use the program in productive use, please take all necessary precautions,
e.g. testing and verifying the program with regard to your specific use.
The program was tested solely for our own use cases, which might differ from yours.
