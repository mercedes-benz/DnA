{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "dhc-monitoring-netpols.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "dhc-monitoring-netpols.fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 -}}
{{- end -}}


{{/*
Create a default chart name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "dhc-monitoring-netpols.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | trunc 63 | trimSuffix "-" | trimSuffix "." -}}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "dhc-monitoring-netpols.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
    {{ default (include "dhc-monitoring-netpols.fullname" .) .Values.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.serviceAccount.name }}
{{- end -}}
{{- end -}}



{{- define "dhc-monitoring-netpols.metadata.labels" -}}
app.kubernetes.io/name: {{ template "dhc-monitoring-netpols.name" . }}
helm.sh/chart: {{ template "dhc-monitoring-netpols.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "dhc-monitoring-netpols.matchLabels" -}}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "dhc-monitoring-netpols.prometheus.matchLabels" -}}
app.kubernetes.io/name: "prometheus"
{{ include "dhc-monitoring-netpols.matchLabels" . }}
{{- end -}}

{{- define "dhc-monitoring-netpols.grafana.matchLabels" -}}
app.kubernetes.io/name: "grafana"
{{ include "dhc-monitoring-netpols.matchLabels" . }}
{{- end -}}

{{- define "dhc-monitoring-netpols.server.matchLabels" -}}
app.kubernetes.io/component: "server"
{{ include "dhc-monitoring-netpols.prometheus.matchLabels" . }}
{{- end -}}

{{- define "dhc-monitoring-netpols.pushgateway.matchLabels" -}}
{{ include "dhc-monitoring-netpols.prometheus.matchLabels" . }}
app.kubernetes.io/name: "prometheus-pushgateway"
{{- end -}}

{{- define "dhc-monitoring-netpols.alertmanager.matchLabels" -}}
{{ include "dhc-monitoring-netpols.prometheus.matchLabels" . }}
app.kubernetes.io/name: "alertmanager"
{{- end -}}