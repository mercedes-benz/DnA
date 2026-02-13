{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "postgres.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "postgres.fullname" -}}
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
Create a default cluster name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "postgres.clustername" -}}
{{- if .Values.clusternameOverride -}}
{{- .Values.clusternameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- printf "%s-cluster" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s-cluster" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "postgres.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | trimSuffix "." | trimSuffix "_" -}}
{{- end -}}

{{/*
Create the name of the secret to use
*/}}
{{- define "postgres.secretName" -}}
{{ default (include "postgres.fullname" .) .Values.secrets.externalSecretName }}
{{- end -}}

{{/*
Create the name of the GPG private key secret to use
*/}}
{{- define "postgres.gpgSecretName" -}}
{{ default (printf "%s-%s" (include "postgres.fullname" .) "privatekey") .Values.secrets.externalGpgSecretName }}
{{- end -}}

{{/*
Create the name of the service
*/}}
{{- define "postgres.servicename" -}}
{{ default (include "postgres.fullname" .) .Values.service.name }}
{{- end -}}

{{/*
Return the appropriate apiVersion for networkpolicy.
Use networking.k8s.io/v1/NetworkPolicy starting with k8s 1.14
*/}}
{{- define "k8s.networkPolicy.apiVersion" -}}
{{- if semverCompare ">=1.4-0, <1.14-0" .Capabilities.KubeVersion.GitVersion -}}
{{- print "extensions/v1beta1" -}}
{{- else if semverCompare "^1.14-0" .Capabilities.KubeVersion.GitVersion -}}
{{- print "networking.k8s.io/v1" -}}
{{- end -}}
{{- end -}}

{{/*
Return the appropriate apiVersion for statefulSet.
Use apps/v1/StatefulSet starting with k8s 1.14
*/}}
{{- define "k8s.statefulSet.apiVersion" -}}
{{- if semverCompare ">=1.4-0, <1.14-0" .Capabilities.KubeVersion.GitVersion -}}
{{- print "apps/v1beta1" -}}
{{- else if semverCompare "^1.14-0" .Capabilities.KubeVersion.GitVersion -}}
{{- print "apps/v1" -}}
{{- end -}}
{{- end -}}
