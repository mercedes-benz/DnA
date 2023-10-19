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

The idea of the DnA Platform originated from the common challenge every big company has - the need for transparency in the data and analytics area. In addition, several features were envisioned and realized, this time with the idea to enable a self-service and cloud-independent platform based holistically on open-source software. The goal is to simplify life and speed up the work of everybody who is working with data. Let's look at the [features DnA Platform offers out of the box](https://github.com/mercedes-benz/DnA/wiki).

## Upcoming Features

[Have a look on topics we are working on currently or are planning in the future...](https://github.com/mercedes-benz/DnA/wiki/Upcoming-features)

## Installation

if you want to try the DnA Platform ,please follow the [installation guide](./docs/Install.md). In the installation guide we are providing 2 ways to install this application(Docker-compose and Helm) . Use Docker-compose only for local testing .

For a complete production installation, we recommend the installation using helm.Currently in our helm and docker-compose we are providing multiple service like DnA , Dashboard, Naas , Malware Scan,Vault service,storage-service ,Airflow and Jupyter Notebooks.

Very soon we are going to provide you the docker-compose and helm-chart for kubeflow-pipeline, Model Registry and Trino . So stay tuned to our repo frequently .

## Architecture

If you are interested to get a glimpse on how is this all tied up all together have a look at [this short overview](./docs/DnAArchitecture.md).

## Security

If you are interested to find out what we are doing to enasure security on the platform have a look at [this short overview](./docs/DnASecurity.md).

## Contributing

We welcome any contributions. As we are a freshly created open source community, we may need a few weeks to settle and be able to accept your ideas or contributions efficiently. During January 2022, we will bring other features on the platform, and then this repository will become our primary repo and place to collaborate with you. Until then, we will still work on our private enterprise repo and syncing code manually. If you want to contribute to the project please read the [contributing guide](CONTRIBUTING.md).

## Code of Conduct

Please read our [Code of Conduct](https://github.com/mercedes-benz/foss/blob/master/CODE_OF_CONDUCT.md) as it is our base for interaction.

## License

This project is licensed under the [MIT LICENSE](LICENSE).

## Provider Information

Please visit (https://www.mercedes-benz-techinnovation.com/en/imprint/) for information on the provider.

Notice: Before you use the program in productive use, please take all necessary precautions,
e.g. testing and verifying the program with regard to your specific use.
The program was tested solely for our own use cases, which might differ from yours.
