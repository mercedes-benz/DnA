{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "airflow.name" -}}
{{- printf .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "airflow.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-config.name" -}}
{{- printf "%s-%s" .Chart.Name "airflow-config" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-git-sync.name" -}}
{{- printf "%s-%s" .Chart.Name "git-sync" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-git-sync.secret" -}}
{{- printf "%s-%s" .Chart.Name "git-sync-secret" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-client.secret" -}}
{{- printf "client-secret.json" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow.secret" -}}
{{- printf "airflow-secrets" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-service.name" -}}
{{- printf "airflow" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-db-service.name" -}}
{{- printf "postgresql" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow.namespace" -}}
{{- printf .Values.namespace | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-service-account.name" -}}
{{- printf "airflow" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-service-role.name" -}}
{{- printf "airflow" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-git-sync-known-hosts.configmap" -}}
{{- printf "known-hosts-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-logs" -}}
{{- printf "airflow-logs" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "airflow-webserver-config.name" -}}
{{- printf "webserver-config" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "image-pull.secret.name" -}}
{{- printf "%s-%s" .Chart.Name "image-pull" | trunc 63 | trimSuffix "-" -}}
{{- end -}}