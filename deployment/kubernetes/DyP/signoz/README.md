# SigNoz

SigNoz is an open-source APM. It helps developers monitor their applications & troubleshoot problems,
an open-source alternative to DataDog, NewRelic, etc. Open source Application Performance Monitoring (APM)
& Observability tool.

### TL;DR;

```sh
helm repo add signoz https://charts.signoz.io
helm install -n platform --create-namespace "my-release" signoz/signoz
```

### Introduction

This chart bootstraps [SigNoz](https://signoz.io) cluster deployment on a
Kubernetes cluster using [Helm](https://helm.sh) package manager.

### Prerequisites

- Kubernetes 1.16+
- Helm 3.0+

### Installing the Chart

To install the chart with the release name `my-release`:

```bash
helm repo add signoz https://charts.signoz.io
helm -n platform --create-namespace install "my-release" signoz/signoz
```

These commands deploy SigNoz on the Kubernetes cluster in the default configuration.
The [Configuration](#configuration) section lists the parameters that can be configured during installation:

> **Tip**: List all releases using `helm list`

### Uninstalling the chart

To uninstall/delete the `my-release` resources:

```bash
helm -n platform uninstall "my-release"
```

See the [Helm docs](https://helm.sh/docs/helm/helm_uninstall/) for documentation on the helm uninstall command.

The command above removes all the Kubernetes components associated
with the chart and deletes the release.

Deletion of the StatefulSet doesn't cascade to deleting associated PVCs. To delete them:

```bash
kubectl -n platform delete pvc --selector app.kubernetes.io/instance=my-release
```

Sometimes everything doesn't get properly removed. If that happens try deleting the namespace:

```bash
kubectl delete namespace platform
```


## Configuration

The following table lists the configurable parameters of the `signoz` chart and their default values.

|              Parameter                   |                             Description                                 |                     Default       |
| ---------------------------------------- | ----------------------------------------------------------------------- | --------------------------------- |
| `fullnameOverride`                       | SigNoz chart full name override (the release name is ignored)           | `""`                              |
| `queryService.name`                      | Query Service component name                                            | `query-service`                   |
| `queryService.image.registry`            | Query Service image registry name                                       | `docker.io`                       |
| `queryService.image.repository`          | Container image name                                                    | `signoz/query-service`            |
| `queryService.image.tag`                 | Container image tag                                                     | `0.55.0`                          |
| `queryService.image.pullPolicy`          | Container pull policy                                                   | `IfNotPresent`                    |
| `queryService.replicaCount`              | Number of query-service nodes                                           | `1`                               |
| `queryService.initContainers.init.enabled`      | Query Service initContainer enabled                              | `true`                            |
| `queryService.initContainers.init.image.registry`   | Query Service initContainer registry name                    | `docker.io`                       |
| `queryService.initContainers.init.image.repository` | Query Service initContainer image name                       | `busybox`                         |
| `queryService.initContainers.init.image.tag`        | Query Service initContainer image tag                        | `1.35`                            |
| `queryService.initContainers.init.image.pullPolicy` | Query Service initContainer pull policy                      | `IfNotPresent`                    |
| `queryService.initContainers.init.command`      | Query Service initContainer command line to execute              | See `values.yaml` for defaults    |
| `queryService.initContainers.init.resources`    | Resources requests and limits                                    | See `values.yaml` for defaults    |
| `queryService.additionalEnvs`            | Additional environment variables for query-service container            | `[]`                              |
| `queryService.configVars`                | Query Service configurations                                            | See `values.yaml` for defaults    |
| `queryService.imagePullSecrets`          | Reference to secrets to be used when pulling images                     | `[]`                              |
| `queryService.serviceAccount.create`     | Service account for query-service nodes enabled                         | `true`                            |
| `queryService.serviceAccount.annotations`    | Service account annotations                                         | `{}`                              |
| `queryService.serviceAccount.name`       | Name of the service account                                             | `nil`                             |
| `queryService.resources`                 | Resources requests and limits                                           | See `values.yaml` for defaults    |
| `queryService.podSecurityContext`        | Pods security context                                                   | `{}`                              |
| `queryService.securityContext`           | Security context for query-service node                                 | `{}`                              |
| `queryService.service.annotations`       | Service annotations                                                     | `{}`                              |
| `queryService.service.labels`            | Service labels                                                          | `{}`                              |
| `queryService.service.type`              | Query Service service type                                              | `ClusterIP`                       |
| `queryService.service.port`              | Query Service service port                                              | `8080`                            |
| `queryService.service.internalPort`      | Query Service service internal port                                     | `8085`                            |
| `queryService.livenessProbe`             | Query Service liveness probes                                           | See `values.yaml` for defaults    |
| `queryService.readinessProbe`            | Query Service readiness probes                                          | See `values.yaml` for defaults    |
| `queryService.customLivenessProbe`       | Custom liveness probes (if `queryService.livenessProbe` not enabled)    | `{}`                              |
| `queryService.customReadinessProbe`      | Custom readiness probes (if `queryService.readinessProbe` not enabled)  | `{}`                              |
| `queryService.ingress.enabled`           | Query Service ingress resource enabled                                  | `false`                           |
| `queryService.ingress.className`         | Query Service ingress class name                                        | `""`                              |
| `queryService.ingress.hosts`             | Query Service ingress virtual hosts                                     | See `values.yaml` for defaults    |
| `queryService.ingress.annotations`       | Query Service ingress annotations                                       | `{}`                              |
| `queryService.ingress.tls`               | Query Service ingress TLS settings                                      | `[]`                              |
| `queryService.nodeSelector`              | Node labels for query-service pod assignment                            | `{}`                              |
| `queryService.tolerations`               | Query Service tolerations                                               | `[]`                              |
| `queryService.nodeAffinity`              | Query Service affinity policy                                           | `{}`                              |
| `frontend.name`                          | Frontend component name                                                 | `frontend`                        |
| `frontend.image.registry`                | Frontend image registry name                                            | `docker.io`                       |
| `frontend.image.repository`              | Container image name                                                    | `signoz/frontend`                 |
| `frontend.image.tag`                     | Container image tag                                                     | `0.55.0`                          |
| `frontend.image.pullPolicy`              | Container pull policy                                                   | `IfNotPresent`                    |
| `frontend.replicaCount`                  | Number of query-service nodes                                           | `1`                               |
| `frontend.initContainers.init.enabled`   | Frontend initContainer enabled                                          | `true`                            |
| `frontend.initContainers.init.image.registry`   | Frontend initContainer registry name                             | `docker.io`                       |
| `frontend.initContainers.init.image.repository` | Frontend initContainer image name                                | `busybox`                         |
| `frontend.initContainers.init.image.tag`        | Frontend initContainer image tag                                 | `1.35`                            |
| `frontend.initContainers.init.image.pullPolicy` | Frontend initContainer pull policy                               | `IfNotPresent`                    |
| `frontend.initContainers.init.command`   | Frontend initContainer command line to execute                          | See `values.yaml` for defaults    |
| `frontend.initContainers.init.resources` | Resources requests and limits                                           | See `values.yaml` for defaults    |
| `frontend.imagePullSecrets`              | Reference to secrets to be used when pulling images                     | `[]`                              |
| `frontend.serviceAccount.create`         | Service account for query-service nodes enabled                         | `true`                            |
| `frontend.serviceAccount.annotations`    | Service account annotations                                             | `{}`                              |
| `frontend.serviceAccount.name`           | Name of the service account                                             | `nil`                             |
| `frontend.resources`                     | Resources requests and limits                                           | See `values.yaml` for defaults    |
| `frontend.podSecurityContext`            | Pods security context                                                   | `{}`                              |
| `frontend.securityContext`               | Security context for query-service node                                 | `{}`                              |
| `frontend.service.annotations`           | Service annotations                                                     | `{}`                              |
| `frontend.service.labels`                | Service labels                                                          | `{}`                              |
| `frontend.service.type`                  | Frontend service type                                                   | `ClusterIP`                       |
| `frontend.service.port`                  | Frontend service port                                                   | `3301`                            |
| `frontend.ingress.enabled`               | Frontend ingress resource enabled                                       | `false`                           |
| `frontend.ingress.className`             | Frontend ingress class name                                             | `""`                              |
| `frontend.ingress.hosts`                 | Frontend ingress virtual hosts                                          | See `values.yaml` for defaults    |
| `frontend.ingress.annotations`           | Frontend ingress annotations                                            | `{}`                              |
| `frontend.ingress.tls`                   | Frontend ingress TLS settings                                           | `[]`                              |
| `frontend.nodeSelector`                  | Node labels for frontend pod assignment                                 | `{}`                              |
| `frontend.tolerations`                   | Frontend tolerations                                                    | `[]`                              |
| `frontend.nodeAffinity`                  | Frontend affinity policy                                                | `{}`                              |
| `alertmanager.name`                      | Alertmanager component name                                             | `alertmanager`                    |
| `alertmanager.image.registry`            | Alertmanager image registry name                                        | `docker.io`                       |
| `alertmanager.image.repository`          | Container image name                                                    | `signoz/alertmanager`             |
| `alertmanager.image.tag`                 | Container image tag                                                     | `0.23.5`                          |
| `alertmanager.image.pullPolicy`          | Container pull policy                                                   | `IfNotPresent`                    |
| `alertmanager.replicaCount`              | Number of Alertmanager nodes                                            | `1`                               |
| `alertmanager.command`                   | Set container command to execute                                        | `[]`                              |
| `alertmanager.extraArgs`                 | Extra arguments for the alertmanager container                          | `{}`                              |
| `alertmanager.additionalEnvs`            | Additional environment variables for alertmanager container             | `[]`                              |
| `alertmanager.initContainers.init.resources` | Resources requests and limits                                       | See `values.yaml` for defaults    |
| `alertmanager.imagePullSecrets`          | Reference to secrets to be used when pulling images                     | `[]`                              |
| `alertmanager.service.annotations`       | Service annotations                                                     | `{}`                              |
| `alertmanager.service.labels`            | Service labels                                                          | `{}`                              |
| `alertmanager.service.type`              | Alertmanager service type                                               | `ClusterIP`                       |
| `alertmanager.service.port`              | Alertmanager service port                                               | `9093`                            |
| `alertmanager.service.nodePort`          | Force specific nodePort                                                 | `nil`                             |
| `alertmanager.serviceAccount.create`           | Service account for alertmanager nodes enabled                    | `true`                            |
| `alertmanager.serviceAccount.annotations`      | Service account annotations                                       | `{}`                              |
| `alertmanager.serviceAccount.name`       | Name of the service account                                             | `nil`                             |
| `alertmanager.podSecurityContext`        | Pods security context                                                   | See `values.yaml` for defaults    |
| `alertmanager.dnsConfig`                 | DNS configuration                                                       | `{}`                              |
| `alertmanager.securityContext`           | Security context for alertmanager node                                  | See `values.yaml` for defaults    |
| `alertmanager.additionalPeers`           | Additional Peers for alertmanager                                       | `[]`                              |
| `alertmanager.ingress.enabled`           | Alertmanager ingress resource enabled                                   | `false`                           |
| `alertmanager.ingress.className`         | Alertmanager ingress class name                                         | `""`                              |
| `alertmanager.ingress.hosts`             | Alertmanager ingress virtual hosts                                      | See `values.yaml` for defaults    |
| `alertmanager.ingress.annotations`       | Alertmanager ingress annotations                                        | `{}`                              |
| `alertmanager.ingress.tls`               | Alertmanager ingress TLS settings                                       | `[]`                              |
| `alertmanager.resources`                 | Resources requests and limits                                           | See `values.yaml` for defaults    |
| `alertmanager.nodeSelector`              | Node labels for alertmanager pod assignment                             | `{}`                              |
| `alertmanager.tolerations`               | Alertmanager tolerations                                                | `[]`                              |
| `alertmanager.nodeAffinity`              | Alertmanager affinity policy                                            | `{}`                              |
| `alertmanager.statefulSet.annotations`   | Set statefulset annotations                                             | `{}`                              |
| `alertmanager.podAnnotations`            | Set pod annotations                                                     | `{}`                              |
| `alertmanager.podLabels`                 | Set pod labels                                                          | `{}`                              |
| `alertmanager.podDisruptionBudget`       | Set pod distruption budget (PDBs)                                       | See `values.yaml` for defaults    |
| `alertmanager.persistence.enabled`       | Enable volume persistence                                               | `true`                            |
| `alertmanager.persistence.storageClass`  | Set storage class for persistent volume                                 | `nil`                             |
| `alertmanager.persistence.accessModes`   | Set access mode for persistent volume                                   | `[ReadWriteOnce]`                 |
| `alertmanager.persistence.size`          | Set storage size                                                        | `100Mi`                           |
| `alertmanager.config`                    | Alertmanager configurations                                             | See `values.yaml` for defaults    |
| `alertmanager.configmapReload`           | Configure ConfigMap reload                                              | See `values.yaml` for defaults    |
| `alertmanager.templates`                 | Set alert templates                                                     | See `values.yaml` for defaults    |
| `schemaMigrator.nodeSelector`    | Node labels for schemaMigrator pod assignment                                   | `{}`                              |
| `schemaMigrator.tolerations`     | schemaMigrator tolerations                                                      | `[]`                              |
| `schemaMigrator.nodeAffinity`    |  schemaMigrator affinity policy                                                 | `{}`                              |
| `schemaMigrator.initContainers.init.enabled`    | Schema migrator initContainer enabled                            | `true`                            |
| `schemaMigrator.initContainers.init.image.registry`   | Schema migrator initContainer registry name                | `docker.io`                       |
| `schemaMigrator.initContainers.init.image.repository` | Schema migrator initContainer image name                   | `busybox`                         |
| `schemaMigrator.initContainers.init.image.tag`        | Schema migrator initContainer image tag                    | `1.35`                            |
| `schemaMigrator.initContainers.init.image.pullPolicy` | Schema migrator initContainer pull policy                  | `IfNotPresent`                    |
| `schemaMigrator.initContainers.init.command`    | Schema migrator initContainer command line to execute            | See `values.yaml` for defaults    |
| `schemaMigrator.initContainers.init.resources`  | Resources requests and limits                                    | See `values.yaml` for defaults    |
| `otelCollector.name`                     | Otel Collector component name                                           | `otel-collector`                  |
| `otelCollector.image.registry`           | Otel Collector image registry name                                      | `docker.io`                       |
| `otelCollector.image.repository`         | Container image name                                                    | `signoz/signoz-otel-collector`    |
| `otelCollector.image.tag`                | Container image tag                                                     | `0.102.10`                         |
| `otelCollector.image.pullPolicy`         | Container pull policy                                                   | `IfNotPresent`                    |
| `otelCollector.replicaCount`             | Number of otel-collector nodes                                          | `1`                               |
| `otelCollector.service.type`             | Otel Collector service type                                             | `ClusterIP`                       |
| `otelCollector.service.annotations`      | Service annotations                                                     | `{}`                              |
| `otelCollector.service.labels`           | Service labels                                                          | `{}`                              |
| `otelCollector.ports`                    | Lists of ports exposed by otel-collector service                        | See `values.yaml` for defaults    |
| `otelCollector.additionalEnvs`           | Additional environment variables for otel-collector container           | `[]`                              |
| `otelCollector.initContainers.init.enabled`    | Otel Collector initContainer enabled                              | `false`                           |
| `otelCollector.initContainers.init.image.registry`   | Otel Collector initContainer registry name                  | `docker.io`                       |
| `otelCollector.initContainers.init.image.repository` | Otel Collector initContainer image name                     | `busybox`                         |
| `otelCollector.initContainers.init.image.tag`        | Otel Collector initContainer image tag                      | `1.35`                            |
| `otelCollector.initContainers.init.image.pullPolicy` | Otel Collector initContainer pull policy                    | `IfNotPresent`                    |
| `otelCollector.initContainers.init.command`    | Otel Collector initContainer command line to execute              | See `values.yaml` for defaults    |
| `otelCollector.initContainers.init.resources`  | Resources requests and limits                                     | See `values.yaml` for defaults    |
| `otelCollector.config`                         | Otel Collector configurations                                     | See `values.yaml` for defaults    |
| `otelCollector.imagePullSecrets`               | Reference to secrets to be used when pulling images               | `[]`                              |
| `otelCollector.serviceAccount.create`          | Service account for otel-collector nodes enabled                  | `true`                            |
| `otelCollector.serviceAccount.annotations`     | Service account annotations                                       | `{}`                              |
| `otelCollector.serviceAccount.name`      | Name of the service account                                             | `nil`                             |
| `otelCollector.resources`                | Resources requests and limits                                           | See `values.yaml` for defaults    |
| `otelCollector.nodeSelector`             | Node labels for Otel Collector pod assignment                           | `{}`                              |
| `otelCollector.tolerations`              | Otel Collector tolerations                                              | `[]`                              |
| `otelCollector.nodeAffinity`             | Otel Collector affinity policy                                          | `{}`                              |
| `otelCollector.livenessProbe`            | Otel Collector liveness probes                                          | See `values.yaml` for defaults    |
| `otelCollector.readinessProbe`           | Otel Collector readiness probes                                         | See `values.yaml` for defaults    |
| `otelCollector.customLivenessProbe`      | Custom liveness probes (if `otelCollector.livenessProbe` not enabled)   | `{}`                              |
| `otelCollector.customReadinessProbe`     | Custom readiness probes (if `otelCollector.readinessProbe` not enabled) | `{}`                              |
| `otelCollector.extraVolumes`             | Extra volumes to be added to the otel-collector pods                    | `[]`                              |
| `otelCollector.extraVolumeMounts`        | Extra volume mounts to be added to the otel-collector pods              | `[]`                              |
| `otelCollector.ingress.enabled`          | Open Telemetry Collector ingress resource enabled                       | `false`                           |
| `otelCollector.ingress.className`        | Open Telemetry Collector ingress class name                             | `""`                              |
| `otelCollector.ingress.hosts`            | Open Telemetry Collector ingress virtual hosts                          | See `values.yaml` for defaults    |
| `otelCollector.ingress.annotations`      | Open Telemetry Collector ingress annotations                            | `{}`                              |
| `otelCollector.ingress.tls`              | Open Telemetry Collector ingress TLS settings                           | `[]`                              |
| `otelCollector.podSecurityContext`       | Pods security context                                                   | `{}`                              |
| `otelCollector.minReadySeconds`          | Minimum seconds for otel-collector pod to be ready without crashing     | `300`                             |
| `otelCollectorMetrics.name`              | Otel Collector Metrics component name                                   | `otel-collector-metrics`          |
| `otelCollectorMetrics.image.registry`    | Otel Collector Metrics image registry name                              | `docker.io`                       |
| `otelCollectorMetrics.image.repository`  | Container image name                                                    | `signoz/signoz-otel-collector`    |
| `otelCollectorMetrics.image.tag`         | Container image tag                                                     | `0.102.10`                         |
| `otelCollectorMetrics.image.pullPolicy`  | Container pull policy                                                   | `IfNotPresent`                    |
| `otelCollectorMetrics.replicaCount`      | Number of otel-collector-metrics nodes                                  | `1`                               |
| `otelCollectorMetrics.service.type`         | Otel Collector service type                                          | `ClusterIP`                       |
| `otelCollectorMetrics.service.annotations`  | Service annotations                                                  | `{}`                              |
| `otelCollectorMetrics.service.labels`    | Service labels                                                          | `{}`                              |
| `otelCollectorMetrics.ports`                    | Lists of ports exposed by otel-collector-metrics service         | See `values.yaml` for defaults    |
| `otelCollectorMetrics.additionalEnvs`    | Additional environment variables for otel-collector-metrics container   | `[]`                              |
| `otelCollectorMetrics.initContainers.init.enabled`    | Otel Collector Metrics initContainer enabled               | `true`                            |
| `otelCollectorMetrics.initContainers.init.image.registry`    | Otel Collector Metrics initContainer registry name  | `docker.io`                       |
| `otelCollectorMetrics.initContainers.init.image.repository`  | Otel Collector Metrics initContainer image name     | `busybox`                         |
| `otelCollectorMetrics.initContainers.init.image.tag`         | Otel Collector Metrics initContainer image tag      | `1.35`                            |
| `otelCollectorMetrics.initContainers.init.image.pullPolicy`  | Otel Collector Metrics initContainer pull policy    | `IfNotPresent`                    |
| `otelCollectorMetrics.initContainers.init.command`  | Otel Collector Metrics initContainer command line to execute | See `values.yaml` for defaults    |
| `otelCollectorMetrics.initContainers.init.resources`| Resources requests and limits                                | See `values.yaml` for defaults    |
| `otelCollectorMetrics.config`            | Otel Collector Metrics configurations                                   | See `values.yaml` for defaults    |
| `otelCollectorMetrics.imagePullSecrets`  | Reference to secrets to be used when pulling images                     | `[]`                              |
| `otelCollectorMetrics.serviceAccount.create`        | Service account for otel-collector-metrics nodes enabled     | `true`                            |
| `otelCollectorMetrics.serviceAccount.annotations`   | Service account annotations                                  | `{}`                              |
| `otelCollectorMetrics.serviceAccount.name`          | Name of the service account                                  | `nil`                             |
| `otelCollectorMetrics.resources`         | Resources requests and limits                                           | See `values.yaml` for defaults    |
| `otelCollectorMetrics.nodeSelector`      | Node labels for Otel Collector Metrics pod assignment                   | `{}`                              |
| `otelCollectorMetrics.tolerations`       | Otel Collector Metrics tolerations                                      | `[]`                              |
| `otelCollectorMetrics.nodeAffinity`      | Otel Collector Metrics affinity policy                                  | `{}`                              |
| `otelCollectorMetrics.livenessProbe`     | Otel Collector Metrics liveness probes                                  | See `values.yaml` for defaults    |
| `otelCollectorMetrics.readinessProbe`    | Otel Collector Metrics readiness probes                                 | See `values.yaml` for defaults    |
| `otelCollectorMetrics.customLivenessProbe`    | Custom liveness probes (if `otelCollectorMetrics.livenessProbe` not enabled)   | `{}`                  |
| `otelCollectorMetrics.customReadinessProbe`   | Custom readiness probes (if `otelCollectorMetrics.readinessProbe` not enabled) | `{}`                  |
| `otelCollectorMetrics.extraVolumes`      | Extra volumes to be added to the otel-collector-metrics pods            | `[]`                              |
| `otelCollectorMetrics.extraVolumeMounts` | Extra volume mounts to be added to the otel-collector-metrics pods      | `[]`                              |
| `otelCollectorMetrics.ingress.enabled`        | Open Telemetry Collector Metrics ingress resource enabled          | `false`                           |
| `otelCollectorMetrics.ingress.className`      | Open Telemetry Collector Metrics ingress class name                | `""`                              |
| `otelCollectorMetrics.ingress.hosts`          | Open Telemetry Collector Metrics ingress virtual hosts             | See `values.yaml` for defaults    |
| `otelCollectorMetrics.ingress.annotations`    | Open Telemetry Collector Metrics ingress annotations               | `{}`                              |
| `otelCollectorMetrics.ingress.tls`            | Open Telemetry Collector Metrics ingress TLS settings              | `[]`                              |
| `otelCollectorMetrics.minReadySeconds`        | Minimum seconds for otel-collector-metrics pod to be ready without crashing  | `300`                   |
| `otelCollectorMetrics.progressDeadlineSeconds`  | Seconds to wait for the deployment to progress before fail reporting   | `120`                       |
