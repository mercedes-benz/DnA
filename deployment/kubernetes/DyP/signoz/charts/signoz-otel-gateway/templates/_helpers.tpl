{{/*
Expand the name of the chart.
*/}}
{{- define "signoz-otel-gateway.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "signoz-otel-gateway.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "signoz-otel-gateway.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "signoz-otel-gateway.labels" -}}
helm.sh/chart: {{ include "signoz-otel-gateway.chart" . }}
{{ include "signoz-otel-gateway.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "signoz-otel-gateway.selectorLabels" -}}
app.kubernetes.io/name: {{ include "signoz-otel-gateway.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "signoz-otel-gateway.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "signoz-otel-gateway.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create a list of all ports
*/}}
{{- define "signoz-otel-gateway.ports" -}}
{{- $serviceType := deepCopy .Values.service.type -}}
{{- $ports := deepCopy .Values.service.ports -}}
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
Create config map
*/}}
{{- define "signoz-otel-gateway.config" -}}
{{- $config := omit .Values.config "create" -}}
{{- range $key, $value := $config }}
{{- $fmted := $value | toString }}
{{- if not (eq $fmted "") }}
{{ $key }}: {{ $fmted | toYaml }}
{{- end }}
{{- end }}
{{- end -}}


{{- define "secretkeyref" -}}
valueFrom:
  secretKeyRef:
    name: {{ .name }}
    key: {{ .key }}
{{- end -}}

{{- define "fieldkeyref" -}}
valueFrom:
  fieldRef:
    apiVersion: v1
    fieldPath: {{ .path }}
{{- end -}}

{{/*
Create env
*/}}
{{- define "signoz-otel-gateway.env" -}}
{{/*
    ====== GENERATED ENVIRONMENT VARIABLES ======
*/}}
{{- $genEnv := dict -}}
{{- $_ := set $genEnv "SIGNOZ_COMPONENT" "signoz-otel-gateway" -}}
{{- $_ := set $genEnv "OTEL_SERVICE_NAME" "signoz-otel-gateway" -}}
{{- $_ := set $genEnv "OTEL_RESOURCE_ATTRIBUTES" "signoz.component=$(SIGNOZ_COMPONENT),k8s.pod.uid=$(K8S_POD_UID),k8s.pod.ip=$(K8S_POD_IP)" -}}
{{/*
    ====== FIELD ENVIRONMENT VARIABLES ======
*/}}
{{- $fieldEnv := dict  -}}
{{- range $key, $value := (dict "K8S_NODE_NAME" "spec.nodeName" "K8S_POD_IP" "status.podIP" "K8S_POD_NAME" "metadata.name" "K8S_POD_UID" "metadata.uid" "K8S_NAMESPACE" "metadata.namespace") -}}
  {{- $_ := set $fieldEnv $key (include "fieldkeyref" (dict "path" $value)) -}}
{{- end -}}

{{/*
    ====== SECRET ENVIRONMENT VARIABLES ======
*/}}
{{- $prefix := (include "signoz-otel-gateway.fullname" .) }}
{{- $secretEnv := dict -}}
{{- if .Values.externalSecrets.create -}}
  {{- range $key, $value := .Values.externalSecrets.secrets -}}
    {{- if $value.env -}}
      {{- range $ikey, $ivalue := $value.env -}}
        {{- $_ := set $secretEnv (upper (printf "OTELGATEWAY_%s" $ikey)) (include "secretkeyref" (dict "name" (printf "%s-%s" $prefix $key) "key" $ivalue)) -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{/*
    ====== USER ENVIRONMENT VARIABLES ======
*/}}
{{- $userEnv := dict -}}
{{- range $key, $val := .Values.env }}
  {{- $upper := upper $key -}}
  {{- $var := printf "OTELGATEWAY_%s" $upper -}}
  {{- $_ := set $userEnv $var $val -}}
{{- end -}}

{{/*
      ====== MERGE AND RENDER ENV BLOCK ======
*/}}

{{- $completeEnv := mergeOverwrite $genEnv $fieldEnv $userEnv $secretEnv -}}
{{- template "signoz-otel-gateway.renderEnv" $completeEnv -}}

{{- end -}}

{{/*
Given a dictionary of variable=value pairs including value and valueFrom, render a container env block.
Environment variables are sorted alphabetically
*/}}
{{- define "signoz-otel-gateway.renderEnv" -}}

{{- $dict := . -}}

{{- range keys . | sortAlpha }}
{{- $val := pluck . $dict | first -}}
{{- $valueType := printf "%T" $val -}}
{{ if eq $valueType "map[string]interface {}" }}
- name: {{ . }}
{{ toYaml $val | indent 2 -}}
{{- else if eq $valueType "string" }}
{{- if regexMatch "valueFrom" $val }}
- name: {{ . }}
{{ $val | indent 2 }}
{{- else }}
- name: {{ . }}
  value: {{ $val | quote }}
{{- end }}
{{- else }}
- name: {{ . }}
  value: {{ $val | quote }}
{{- end }}
{{- end -}}

{{- end -}}
