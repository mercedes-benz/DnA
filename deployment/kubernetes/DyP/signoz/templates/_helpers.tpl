{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
*/}}
{{- define "signoz.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name for SigNoz.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "signoz.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "signoz.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Return namespace of the signoz release
*/}}
{{- define "signoz.namespace" -}}
{{- .Release.Namespace -}}
{{- end -}}

{{/*
Create a default fully qualified app name for queryService.
*/}}
{{- define "queryService.fullname" -}}
{{- printf "%s-%s" (include "signoz.fullname" .) .Values.queryService.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "queryService.labels" -}}
helm.sh/chart: {{ include "signoz.chart" . }}
{{ include "queryService.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "queryService.selectorLabels" -}}
app.kubernetes.io/name: {{ include "signoz.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ default "query-service" .Values.queryService.name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "queryService.serviceAccountName" -}}
{{- if .Values.queryService.serviceAccount.create -}}
    {{ default (include "queryService.fullname" .) .Values.queryService.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.queryService.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Return the initContainers image name
*/}}
{{- define "queryService.initContainers.init.image" -}}
{{- $registryName := default .Values.queryService.initContainers.init.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.queryService.initContainers.init.image.repository -}}
{{- $tag := .Values.queryService.initContainers.init.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the initContainers image name for migration
*/}}
{{- define "queryService.initContainers.migration.image" -}}
{{- $registryName := default .Values.queryService.initContainers.migration.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.queryService.initContainers.migration.image.repository -}}
{{- $tag := .Values.queryService.initContainers.migration.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the proper queryService image name
*/}}
{{- define "queryService.image" -}}
{{- $registryName := default .Values.queryService.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.queryService.image.repository -}}
{{- $tag := default .Chart.AppVersion .Values.queryService.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Set query-service port
*/}}
{{- define "queryService.port" -}}
{{- default 8080 .Values.queryService.service.port  -}}
{{- end -}}

{{/*
Set query-service internal port
*/}}
{{- define "queryService.internalPort" -}}
{{- default 8085 .Values.queryService.service.internalPort  -}}
{{- end -}}

{{/*
Set query-service url
*/}}
{{- define "queryService.url" -}}
{{ include "queryService.fullname" . }}:{{ include "queryService.port" . }}
{{- end -}}

{{/*
Set query-service internal url
*/}}
{{- define "queryService.internalUrl" -}}
{{ include "queryService.fullname" . }}:{{ include "queryService.internalPort" . }}
{{- end -}}


{{/*
Create a default fully qualified app name for frontend.
*/}}
{{- define "frontend.fullname" -}}
{{- printf "%s-%s" (include "signoz.fullname" .) .Values.frontend.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "frontend.labels" -}}
helm.sh/chart: {{ include "signoz.chart" . }}
{{ include "frontend.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "frontend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "signoz.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ default "frontend" .Values.frontend.name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "frontend.serviceAccountName" -}}
{{- if .Values.frontend.serviceAccount.create -}}
    {{ default (include "frontend.fullname" .) .Values.frontend.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.frontend.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Return the proper frontend image name
*/}}
{{- define "frontend.image" -}}
{{- $registryName := default .Values.frontend.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.frontend.image.repository -}}
{{- $tag := default .Chart.AppVersion .Values.frontend.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the initContainers image name
*/}}
{{- define "frontend.initContainers.init.image" -}}
{{- $registryName := default .Values.frontend.initContainers.init.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.frontend.initContainers.init.image.repository -}}
{{- $tag := .Values.frontend.initContainers.init.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}



{{/*
Create a default fully qualified app name.
*/}}
{{- define "alertmanager.fullname" -}}
{{- printf "%s-%s" (include "signoz.fullname" .) .Values.alertmanager.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "alertmanager.labels" -}}
helm.sh/chart: {{ include "signoz.chart" . }}
{{ include "alertmanager.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "alertmanager.selectorLabels" -}}
app.kubernetes.io/name: {{ include "signoz.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ default "alertmanager" .Values.alertmanager.name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "alertmanager.serviceAccountName" -}}
{{- if .Values.alertmanager.serviceAccount.create -}}
    {{ default (include "alertmanager.fullname" .) .Values.alertmanager.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.alertmanager.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Set alertmanager port
*/}}
{{- define "alertmanager.port" -}}
{{- default 9093 .Values.alertmanager.service.port  -}}
{{- end -}}

{{/*
Return the initContainers image name
*/}}
{{- define "alertmanager.initContainers.init.image" -}}
{{- $registryName := default .Values.alertmanager.initContainers.init.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.alertmanager.initContainers.init.image.repository -}}
{{- $tag := .Values.alertmanager.initContainers.init.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the proper otelCollector image name
*/}}
{{- define "alertmanager.image" -}}
{{- $registryName := default .Values.alertmanager.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.alertmanager.image.repository -}}
{{- $tag := .Values.alertmanager.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Set query-service url
*/}}
{{- define "alertmanager.url" -}}
{{ include "alertmanager.fullname" . }}:{{ include "alertmanager.port" . }}
{{- end -}}

{{/*
Create a default fully qualified app name for schema migrator.
*/}}
{{- define "schemaMigrator.fullname" -}}
{{- printf "%s-%s" (include "signoz.fullname" .) .Values.schemaMigrator.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "schemaMigrator.labels" -}}
helm.sh/chart: {{ include "signoz.chart" . }}
{{ include "schemaMigrator.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Common Selector labels of schema migrator
*/}}
{{- define "schemaMigrator.selectorLabels" -}}
app.kubernetes.io/name: {{ include "signoz.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Selector labels of init migration job
*/}}
{{- define "schemaMigrator.selectorLabelsInit" -}}
{{ include "schemaMigrator.selectorLabels" . }}
app.kubernetes.io/component: {{ default "schema-migrator" .Values.schemaMigrator.name }}-init
{{- end -}}

{{/*
Selector labels of upgrade migration job
*/}}
{{- define "schemaMigrator.selectorLabelsUpgrade" -}}
{{ include "schemaMigrator.selectorLabels" . }}
app.kubernetes.io/component: {{ default "schema-migrator" .Values.schemaMigrator.name }}-upgrade
{{- end -}}

{{/*
Create a default fully qualified app name for otelCollector.
*/}}
{{- define "otelCollector.fullname" -}}
{{- printf "%s-%s" (include "signoz.fullname" .) .Values.otelCollector.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "otelCollector.labels" -}}
helm.sh/chart: {{ include "signoz.chart" . }}
{{ include "otelCollector.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "otelCollector.selectorLabels" -}}
app.kubernetes.io/name: {{ include "signoz.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ default "otel-collector" .Values.otelCollector.name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "otelCollector.serviceAccountName" -}}
{{- if .Values.otelCollector.serviceAccount.create -}}
    {{ default (include "otelCollector.fullname" .) .Values.otelCollector.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.otelCollector.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Return the initContainers image name
*/}}
{{- define "otelCollector.initContainers.init.image" -}}
{{- $registryName := default .Values.otelCollector.initContainers.init.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.otelCollector.initContainers.init.image.repository -}}
{{- $tag := .Values.otelCollector.initContainers.init.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the schema migrator's image name
*/}}
{{- define "schemaMigrator.image" -}}
{{- $registryName := default .Values.schemaMigrator.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.schemaMigrator.image.repository -}}
{{- $tag := .Values.schemaMigrator.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}
{{/*
Return the schema migrator's wait initContainer image name
*/}}
{{- define "schemaMigrator.initContainers.wait.image" -}}
{{- $registryName := default .Values.schemaMigrator.initContainers.wait.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.schemaMigrator.initContainers.wait.image.repository -}}
{{- $tag := .Values.schemaMigrator.initContainers.wait.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the schema migrator's init initContainer image name
*/}}
{{- define "schemaMigrator.initContainers.init.image" -}}
{{- $registryName := default .Values.schemaMigrator.initContainers.init.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.schemaMigrator.initContainers.init.image.repository -}}
{{- $tag := .Values.schemaMigrator.initContainers.init.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the schema migrator's init initContainer image name
*/}}
{{- define "schemaMigrator.initContainers.chReady.image" -}}
{{- $registryName := default .Values.schemaMigrator.initContainers.chReady.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.schemaMigrator.initContainers.chReady.image.repository -}}
{{- $tag := .Values.schemaMigrator.initContainers.chReady.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the proper otelCollector image name
*/}}
{{- define "otelCollector.image" -}}
{{- $registryName := default .Values.otelCollector.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.otelCollector.image.repository -}}
{{- $tag := .Values.otelCollector.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Create the name of the clusterRole to use
*/}}
{{- define "otelCollector.clusterRoleName" -}}
{{- if .Values.otelCollector.clusterRole.create }}
{{- $clusterRole := printf "%s-%s" (include "otelCollector.fullname" .) (include "signoz.namespace" .) -}}
{{- default $clusterRole .Values.otelCollector.clusterRole.name }}
{{- else }}
{{- default "default" .Values.otelCollector.clusterRole.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the clusterRoleBinding to use
*/}}
{{- define "otelCollector.clusterRoleBindingName" -}}
{{- if .Values.otelCollector.clusterRole.create }}
{{- $clusterRole := printf "%s-%s" (include "otelCollector.fullname" .) (include "signoz.namespace" .) -}}
{{- default $clusterRole .Values.otelCollector.clusterRole.clusterRoleBinding.name }}
{{- else }}
{{- default "default" .Values.otelCollector.clusterRole.clusterRoleBinding.name }}
{{- end }}
{{- end }}


{{/*
Create a default fully qualified app name for otelCollectorMetrics.
*/}}
{{- define "otelCollectorMetrics.fullname" -}}
{{- printf "%s-%s" (include "signoz.fullname" .) .Values.otelCollectorMetrics.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "otelCollectorMetrics.labels" -}}
helm.sh/chart: {{ include "signoz.chart" . }}
{{ include "otelCollectorMetrics.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "otelCollectorMetrics.selectorLabels" -}}
app.kubernetes.io/name: {{ include "signoz.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ default "otel-collector-metrics" .Values.otelCollectorMetrics.name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "otelCollectorMetrics.serviceAccountName" -}}
{{- if .Values.otelCollectorMetrics.serviceAccount.create -}}
    {{ default (include "otelCollectorMetrics.fullname" .) .Values.otelCollectorMetrics.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.otelCollectorMetrics.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Return the initContainers image name
*/}}
{{- define "otelCollectorMetrics.initContainers.init.image" -}}
{{- $registryName := default .Values.otelCollectorMetrics.initContainers.init.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.otelCollectorMetrics.initContainers.init.image.repository -}}
{{- $tag := .Values.otelCollectorMetrics.initContainers.init.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the proper otelCollectorMetrics image name
*/}}
{{- define "otelCollectorMetrics.image" -}}
{{- $registryName := default .Values.otelCollectorMetrics.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.otelCollectorMetrics.image.repository -}}
{{- $tag := .Values.otelCollectorMetrics.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Create the name of the clusterRole to use
*/}}
{{- define "otelCollectorMetrics.clusterRoleName" -}}
{{- if .Values.otelCollectorMetrics.clusterRole.create }}
{{- $clusterRole := printf "%s-%s" (include "otelCollectorMetrics.fullname" .) (include "signoz.namespace" .) -}}
{{- default $clusterRole .Values.otelCollectorMetrics.clusterRole.name }}
{{- else }}
{{- default "default" .Values.otelCollectorMetrics.clusterRole.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the clusterRoleBinding to use
*/}}
{{- define "otelCollectorMetrics.clusterRoleBindingName" -}}
{{- if .Values.otelCollectorMetrics.clusterRole.create }}
{{- $clusterRole := printf "%s-%s" (include "otelCollectorMetrics.fullname" .) (include "signoz.namespace" .) -}}
{{- default $clusterRole .Values.otelCollectorMetrics.clusterRole.clusterRoleBinding.name }}
{{- else }}
{{- default "default" .Values.otelCollectorMetrics.clusterRole.clusterRoleBinding.name }}
{{- end }}
{{- end }}

{{/*
Return the service name of Clickhouse
*/}}
{{- define "clickhouse.servicename" -}}
{{- if .Values.clickhouse.fullnameOverride -}}
{{- .Values.clickhouse.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default "clickhouse" .Values.clickhouse.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- $name = .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else }}
{{- $name = printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- $namespace := .Values.clickhouse.namespace -}}
{{- $clusterDomain := default "cluster.local" .Values.global.clusterDomain -}}
{{- if and $namespace (ne $namespace .Release.Namespace) -}}
{{ printf "%s.%s.svc.%s" $name $namespace $clusterDomain }}
{{- else -}}
{{ $name }}
{{- end -}}
{{- end -}}
{{- end }}

{{/*
Return `nodePort: null` if service type is ClusterIP
*/}}
{{- define "signoz.service.ifClusterIP" -}}
{{- if (eq . "ClusterIP") }}
nodePort: null
{{- end }}
{{- end }}

{{/*
Return structured list of ports config.
*/}}
{{- define "otelCollector.portsConfig" -}}
{{- $serviceType := deepCopy .service.type -}}
{{- $ports := deepCopy .ports -}}
{{- range $key, $port := $ports -}}
{{- if $port.enabled }}
- name: {{ $key }}
  port: {{ $port.servicePort }}
  targetPort: {{ $key }}
  protocol: {{ $port.protocol }}
  {{- if (eq $serviceType "ClusterIP") }}
  nodePort: null
  {{- else if (eq $serviceType "NodePort") }}
  nodePort: {{ $port.nodePort }}
  {{- end }}
{{- end -}}
{{- end -}}
{{- end -}}


{{/*
Return the appropriate apiVersion for ingress.
*/}}
{{- define "ingress.apiVersion" -}}
  {{- if and (.Capabilities.APIVersions.Has "networking.k8s.io/v1") (semverCompare ">= 1.19-0" .Capabilities.KubeVersion.Version) -}}
      {{- print "networking.k8s.io/v1" -}}
  {{- else if .Capabilities.APIVersions.Has "networking.k8s.io/v1beta1" -}}
    {{- print "networking.k8s.io/v1beta1" -}}
  {{- else -}}
    {{- print "extensions/v1beta1" -}}
  {{- end -}}
{{- end -}}

{{/*
Return if ingress supports pathType.
*/}}
{{- define "ingress.supportsPathType" -}}
  {{- or (eq (include "ingress.isStable" .) "true") (and (eq (include "ingress.apiVersion" .) "networking.k8s.io/v1beta1") (semverCompare ">= 1.18-0" .Capabilities.KubeVersion.Version)) -}}
{{- end -}}

{{/*
Return if ingress is stable.
*/}}
{{- define "ingress.isStable" -}}
  {{- eq (include "ingress.apiVersion" .) "networking.k8s.io/v1" -}}
{{- end -}}

{{/*
Return true if Let's Encrypt ClusterIssuer of `cert-manager` should be created.
*/}}
{{- define "ingress.letsencrypt" -}}
{{- $clusterIssuerEnabled := index (index .Values "cert-manager") "letsencrypt" -}}
{{- if ne ($clusterIssuerEnabled | toString) "<nil>" -}}
  {{ $clusterIssuerEnabled }}
{{- else if and (index (index .Values "ingress-nginx") "enabled") (index (index .Values "cert-manager") "enabled") -}}
  true
{{- else -}}
  false
{{- end -}}
{{- end -}}

{{/*
Common K8s environment variables used by SigNoz OtelCollector.
*/}}
{{- define "snippet.k8s-env" }}
- name: K8S_NODE_NAME
  valueFrom:
    fieldRef:
      fieldPath: spec.nodeName
- name: K8S_POD_IP
  valueFrom:
    fieldRef:
      apiVersion: v1
      fieldPath: status.podIP
- name: K8S_HOST_IP
  valueFrom:
    fieldRef:
      fieldPath: status.hostIP
- name: K8S_POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: K8S_POD_UID
  valueFrom:
    fieldRef:
      fieldPath: metadata.uid
- name: K8S_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
{{- end }}

{{/*
Return the proper Image Registry Secret Names.
*/}}
{{- define "signoz.imagePullSecrets" -}}
{{- if or .Values.global.imagePullSecrets .Values.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.global.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- range .Values.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}
