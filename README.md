<!-- SPDX-License-Identifier: MIT -->

## DnA Platform (Data and Analytics Platform)

Idea of the DnA Platform originated from the common challenge every bigger company has and this is: need for a transparency. In all parts of the company you have some innitiatives working with data, learning from data and developing services based on this data which is in nature very good, but...
Soon you will start to notice that two parts of the company are doing very similar, if not the same, innitiative but they are not aware about each other and because of this company is focusing on to many things in a not so efficient way (eg. double spending of resources and budget).
DnA Platform provides this kind of transparency accross organization, connecting people and sharing ideas.

After we have realized [Transparency feature](./docs/DnATransparency.md) and rolled it out, we have checked other hurdles organization have to sucessfully realize data and analytics initiatives. Some of them are probably also familiar to you also:

1. Availability of data
2. Clearity on data governance/protection todo's and their execution
3. Having tech platform and onboarding time to it using full self service approach not needing anybody to do something for you



## Installation

If you just want to try DnA Platform please follow the [installation guide](./docs/SETUP-DOCKER-COMPOSE.md) to install it. Docker installation has only limited feature set and may not be suitable for production environment deployment. From the feature list only Transparency of Data & Analytics initiatives/solutions is available (most of other features require Kubernetes specifics so it is not possible to run it under Docker).

For a full production installation we would recomend Kubernetes installation. We are currently preparing Kubernetes Helm chart (available in January 2022) where entire system will be available - including Jupyter notebooks with Kale, Apache Airflow, Kubeflow Pipeline. Stay tuned on this frequency for this :). In the mean time you can use previous docker instruction to deploy images in Kubernetes.

## Upcoming Features

1. Malware Scan as a Service
2. Pipeline as a Service
3. Jupyter Notebook as workspace
4. Kubernetes Deployment

## Contributing

We welcome any contributions.
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
