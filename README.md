<!-- SPDX-License-Identifier: MIT -->

## DnA Platform (Data and Analytics Platform)

Idea of the DnA Platform originated from the common challenge every bigger company has - need for a transparency in area of data and anylytics. Soon after that further features were envisioned and realized, this time with an idea to enable self service, cloud independant platform based fully on open source that will simplify life and speed up work of everybody who is working with data. Lets look on those features DnA Platform enables you out of the box.

[Transparency feature](./docs/DnATransparency.md) helps you get a transparency on different initiatives and enables collaboration between people working on similar challenges

After we have realized transparency feature and rolled it out, we have checked other hurdles organization have to sucessfully realize data and analytics initiatives. Some of them are probably also familiar to you also:

1. Having tech platform to do the work and minimize onboarding time to it using full self service approach not needing anybody to do something for you
2. Availability of data
3. Clearity on data governance/protection todo's and their execution

As already transparency feature enables users to upload certain documents we had to ensure that nobody would upload malitious document so we realized the idea of

[Malware Scan As A Service](./docs/MalwareScanAsAService.md) which provides simple REST API to check all kind of different attachments for malitious code.

## Installation

If you just want to try DnA Platform please follow the [installation guide](./docs/SETUP-DOCKER-COMPOSE.md) to install it. Docker installation has only limited feature set and may not be suitable for production environment deployment. From the feature list only Transparency feature of Data & Analytics initiatives/solutions is available (most of other features require Kubernetes specifics so it is not possible to run it under Docker).

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
