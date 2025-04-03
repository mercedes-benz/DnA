{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
*/}}
{{- define "clickhouse.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "clickhouse.fullname" -}}
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
{{- define "clickhouse.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "clickhouse.labels" -}}
helm.sh/chart: {{ include "clickhouse.chart" . }}
{{ include "clickhouse.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "clickhouse.selectorLabels" -}}
app.kubernetes.io/name: {{ include "clickhouse.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ default "clickhouse" .Values.name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "clickhouse.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
    {{ default (include "clickhouse.fullname" .) .Values.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Set zookeeper host
*/}}
{{- define "clickhouse.zookeeper.servicename" -}}
{{- if .Values.zookeeper.fullnameOverride -}}
{{- .Values.zookeeper.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else if .Values.zookeeper.nameOverride -}}
{{- printf "%s-%s" .Release.Name .Values.zookeeper.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name "zookeeper" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Set zookeeper port
*/}}
{{- define "clickhouse.zookeeper.port" -}}
{{- default 2181 }}
{{- end }}

{{/*
Return suffix part of the headless service
*/}}
{{- define "clickhouse.zookeeper.headlessSvcSuffix" -}}
{{- $namespace := .Values.zookeeper.namespaceOverride }}
{{- $clusterDomain := default "cluster.local" .Values.global.clusterDomain }}
{{- $name := printf "%s-headless" (include "clickhouse.zookeeper.servicename" .) }}
{{- if and $namespace (ne $namespace .Values.namespace) }}
{{- printf "%s.%s.svc.%s" $name $namespace $clusterDomain }}
{{- else -}}
{{- $name }}
{{- end }}
{{- end }}

{{/*
Return the initContainers image name
*/}}
{{- define "clickhouse.initContainers.init.image" -}}
{{- $registryName := default .Values.initContainers.init.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.initContainers.init.image.repository -}}
{{- $tag := .Values.initContainers.init.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the initContainers image name for UDF
*/}}
{{- define "clickhouse.initContainers.udf.image" -}}
{{- $registryName := default .Values.initContainers.udf.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.initContainers.udf.image.repository -}}
{{- $tag := .Values.initContainers.udf.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return the proper clickhouse image name
*/}}
{{- define "clickhouse.image" -}}
{{- $registryName := default .Values.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.image.repository -}}
{{- $tag := .Values.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return `nodePort: null` if service type is ClusterIP
*/}}
{{- define "service.ifClusterIP" -}}
{{- if (eq . "ClusterIP") -}}
nodePort: null
{{- end -}}
{{- end -}}

{{/*
Return the proper Image Registry Secret Names.
*/}}
{{- define "clickhouse.imagePullSecrets" -}}
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

{{/*
Return service account annotations of ClickHouse instance.
*/}}
{{- define "clickhouse.serviceAccountAnnotations" -}}
{{- $annotations := dict }}
{{- if .Values.serviceAccount.create }}
{{- $annotations = merge $annotations .Values.serviceAccount.annotations }}
{{- end }}
{{- if and .Values.coldStorage.enabled .Values.coldStorage.role.enabled }}
{{- $annotations = merge $annotations .Values.coldStorage.role.annotations }}
{{- end -}}
annotations:
  {{- toYaml $annotations | nindent 2 }}
{{- end }}

{{/*
Create a default fully qualified app name for clickhouseOperator.
*/}}
{{- define "clickhouseOperator.fullname" -}}
{{- printf "%s-%s" (include "clickhouse.fullname" .) .Values.clickhouseOperator.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Return namespace of clickhouse
*/}}
{{- define "clickhouse.namespace" -}}
{{- default .Release.Namespace .Values.namespace -}}
{{- end -}}

{{/*
Return list of files and contents.
*/}}
{{- define "clickhouse.files" -}}
{{- range $key,$value := .Values.files }}
{{ $key }}: {{ $value | toYaml }}
{{- end }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "clickhouseOperator.labels" -}}
helm.sh/chart: {{ include "clickhouse.chart" . }}
{{ include "clickhouseOperator.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
clickhouse.altinity.com/chop: {{ .Values.clickhouseOperator.version }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "clickhouseOperator.selectorLabels" -}}
app.kubernetes.io/name: {{ include "clickhouse.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: {{ .Values.clickhouseOperator.name }}
{{- end -}}

{{/*
Return the proper clickhouseOperator image name
*/}}
{{- define "clickhouseOperator.image" -}}
{{- $registryName := default .Values.clickhouseOperator.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.clickhouseOperator.image.repository -}}
{{- $tag := default .Values.clickhouseOperator.version .Values.clickhouseOperator.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "clickhouseOperator.serviceAccountName" -}}
{{- if .Values.clickhouseOperator.serviceAccount.create -}}
    {{ default (include "clickhouseOperator.fullname" .) .Values.clickhouseOperator.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.clickhouseOperator.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Return the proper Image Registry Secret Names.
*/}}
{{- define "clickhouseOperator.imagePullSecrets" -}}
{{- if or .Values.global.imagePullSecrets .Values.clickhouseOperator.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.global.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- range .Values.clickhouseOperator.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create a default fully qualified app name for metricsExporter.
*/}}
{{- define "metricsExporter.fullname" -}}
{{- printf "%s-%s" (include "clickhouse.fullname" .) .Values.clickhouseOperator.metricsExporter.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Return the proper metricsExporter image name
*/}}
{{- define "metricsExporter.image" -}}
{{- $registryName := default .Values.clickhouseOperator.metricsExporter.image.registry .Values.global.imageRegistry -}}
{{- $repositoryName := .Values.clickhouseOperator.metricsExporter.image.repository -}}
{{- $tag := default .Values.clickhouseOperator.version .Values.clickhouseOperator.metricsExporter.image.tag | toString -}}
{{- if $registryName -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}

{{/*
Return common environment variables for ClickHouse Operator
*/}}
{{- define "clickhouseOperator.commonEnv" -}}
# Pod-specific
# spec.nodeName: ip-172-20-52-62.ec2.internal
- name: OPERATOR_POD_NODE_NAME
  valueFrom:
    fieldRef:
        fieldPath: spec.nodeName
# metadata.name: clickhouse-operator-6f87589dbb-ftcsf
- name: OPERATOR_POD_NAME
  valueFrom:
    fieldRef:
        fieldPath: metadata.name
# metadata.namespace: kube-system
- name: OPERATOR_POD_NAMESPACE
  valueFrom:
    fieldRef:
        fieldPath: metadata.namespace
# status.podIP: 100.96.3.2
- name: OPERATOR_POD_IP
  valueFrom:
    fieldRef:
        fieldPath: status.podIP
# spec.serviceAccount: {{ include "clickhouseOperator.fullname" . }}
# spec.serviceAccountName: {{ include "clickhouseOperator.fullname" . }}
- name: OPERATOR_POD_SERVICE_ACCOUNT
  valueFrom:
    fieldRef:
        fieldPath: spec.serviceAccountName

# Container-specific
- name: OPERATOR_CONTAINER_CPU_REQUEST
  valueFrom:
    resourceFieldRef:
        containerName: {{ include "clickhouseOperator.fullname" . }}
        resource: requests.cpu
- name: OPERATOR_CONTAINER_CPU_LIMIT
  valueFrom:
    resourceFieldRef:
        containerName: {{ include "clickhouseOperator.fullname" . }}
        resource: limits.cpu
- name: OPERATOR_CONTAINER_MEM_REQUEST
  valueFrom:
    resourceFieldRef:
        containerName: {{ include "clickhouseOperator.fullname" . }}
        resource: requests.memory
- name: OPERATOR_CONTAINER_MEM_LIMIT
  valueFrom:
    resourceFieldRef:
        containerName: {{ include "clickhouseOperator.fullname" . }}
        resource: limits.memory
{{- end }}
