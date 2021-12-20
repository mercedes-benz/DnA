<!-- SPDX-License-Identifier: MIT -->

## DnA (Data and Analytics Platform) - Intended Usage

Data & Analytics platform is a end to end solution/project mangement application. It provides capability to capture project metadata which can be further used for analysis and project tracking.

Project metadata includes-

1. Project description
2. Team member information
3. Milestone
4. Datasources
5. Analytics
6. Sharing
7. Digital Value

## Installation

Please follow the [installation guide](./docs/SETUP-DOCKER-COMPOSE.md) to install DnA. Under docker only limited feature set is available - meaning only Transparency of Data & Analytics initiatives/solutions (most of other features require Kubernetes specifics so it is not possible to run it under Docker).

We are currently preparing Kubernetes Helm chart (available in January 2022) where entire system will be available - including Jupyter notebooks with Kale, Apache Airflow, Kubeflow Pipeline). Stay tuned on this frequency for this :)

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
