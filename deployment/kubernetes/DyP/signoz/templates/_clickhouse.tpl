{{/*
Common ClickHouse ENV variables and helpers used by SigNoz
*/}}

{{- define "schemamigrator.url" -}}
{{- if .Values.clickhouse.enabled -}}
{{- printf "%v:%v" ( include "clickhouse.servicename" . ) ( include "clickhouse.tcpPort" . ) -}}
{{- else -}}
{{- printf "%v:%v" ( required "externalClickhouse.host is required if not clickhouse.enabled" .Values.externalClickhouse.host ) ( default 9000 .Values.externalClickhouse.tcpPort ) -}}
{{- end -}}
{{- end -}}

{{- define "snippet.clickhouse-env" }}
{{- if .Values.clickhouse.enabled -}}
- name: CLICKHOUSE_HOST
  value: {{ include "clickhouse.servicename" . }}
- name: CLICKHOUSE_PORT
  value: {{ include "clickhouse.tcpPort" . | quote }}
- name: CLICKHOUSE_HTTP_PORT
  value: {{ include "clickhouse.httpPort" . | quote }}
- name: CLICKHOUSE_CLUSTER
  value: {{ .Values.clickhouse.cluster | quote }}
- name: CLICKHOUSE_DATABASE
  value: {{ default "signoz_metrics" .Values.clickhouse.database | quote }}
- name: CLICKHOUSE_TRACE_DATABASE
  value: {{ default "signoz_traces" .Values.clickhouse.traceDatabase | quote }}
- name: CLICKHOUSE_LOG_DATABASE
  value: {{ default "signoz_logs" .Values.clickhouse.logDatabase | quote }}
- name: CLICKHOUSE_USER
  value: {{ .Values.clickhouse.user | quote }}
- name: CLICKHOUSE_PASSWORD
  value: {{ .Values.clickhouse.password | quote }}
- name: CLICKHOUSE_SECURE
  value: {{ .Values.clickhouse.secure | quote }}
- name: CLICKHOUSE_VERIFY
  value: {{ .Values.clickhouse.verify | quote }}
{{- else -}}
- name: CLICKHOUSE_HOST
  value: {{ required "externalClickhouse.host is required if not clickhouse.enabled" .Values.externalClickhouse.host | quote }}
- name: CLICKHOUSE_PORT
  value: {{ default 9000 .Values.externalClickhouse.tcpPort | quote }}
- name: CLICKHOUSE_HTTP_PORT
  value: {{ default 8123 .Values.externalClickhouse.httpPort | quote }}
- name: CLICKHOUSE_CLUSTER
  value: {{ required "externalClickhouse.cluster is required if not clickhouse.enabled" .Values.externalClickhouse.cluster | quote }}
- name: CLICKHOUSE_DATABASE
  value: {{ default "signoz_metrics" .Values.externalClickhouse.database | quote }}
- name: CLICKHOUSE_TRACE_DATABASE
  value: {{ default "signoz_traces" .Values.externalClickhouse.traceDatabase | quote }}
- name: CLICKHOUSE_LOG_DATABASE
  value: {{ default "signoz_logs" .Values.externalClickhouse.logDatabase | quote }}
- name: CLICKHOUSE_USER
  value: {{ .Values.externalClickhouse.user | quote }}
{{- if .Values.externalClickhouse.existingSecret }}
- name: CLICKHOUSE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ include "clickhouse.secretName" . }}
      key: {{ include "clickhouse.secretPasswordKey" . }}
{{- else }}
- name: CLICKHOUSE_PASSWORD
  value: {{ .Values.externalClickhouse.password | quote }}
{{- end }}
- name: CLICKHOUSE_SECURE
  value: {{ .Values.externalClickhouse.secure | quote }}
- name: CLICKHOUSE_VERIFY
  value: {{ .Values.externalClickhouse.verify | quote }}
{{- end }}
{{- end }}

{{/*
Minimized ClickHouse ENV variables for user credentials
*/}}
{{- define "snippet.clickhouse-credentials" }}
{{ if .Values.clickhouse.enabled -}}
- name: CLICKHOUSE_HOST
  value: {{ include "clickhouse.servicename" . }}
- name: CLICKHOUSE_PORT
  value: {{ include "clickhouse.tcpPort" . | quote }}
- name: CLICKHOUSE_HTTP_PORT
  value: {{ include "clickhouse.httpPort" . | quote }}
- name: CLICKHOUSE_CLUSTER
  value: {{ .Values.clickhouse.cluster | quote }}
- name: CLICKHOUSE_USER
  value: {{ .Values.clickhouse.user | quote }}
- name: CLICKHOUSE_PASSWORD
  value: {{ .Values.clickhouse.password | quote }}
- name: CLICKHOUSE_SECURE
  value: {{ .Values.clickhouse.secure | quote }}
{{- else -}}
- name: CLICKHOUSE_HOST
  value: {{ required "externalClickhouse.host is required if not clickhouse.enabled" .Values.externalClickhouse.host | quote }}
- name: CLICKHOUSE_PORT
  value: {{ default 9000 .Values.externalClickhouse.tcpPort | quote }}
- name: CLICKHOUSE_HTTP_PORT
  value: {{ default 8123 .Values.externalClickhouse.httpPort | quote }}
- name: CLICKHOUSE_CLUSTER
  value: {{ required "externalClickhouse.cluster is required if not clickhouse.enabled" .Values.externalClickhouse.cluster | quote }}
- name: CLICKHOUSE_USER
  value: {{ .Values.externalClickhouse.user | quote }}
{{- if .Values.externalClickhouse.existingSecret }}
- name: CLICKHOUSE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ include "clickhouse.secretName" . }}
      key: {{ include "clickhouse.secretPasswordKey" . }}
{{- else }}
- name: CLICKHOUSE_PASSWORD
  value: {{ .Values.externalClickhouse.password | quote }}
{{- end }}
- name: CLICKHOUSE_SECURE
  value: {{ .Values.externalClickhouse.secure | quote }}
{{- end }}
{{- end }}

{*
   ------ CLICKHOUSE ------
*}

{{/*
Set Clickhouse tcp port
*/}}
{{- define "clickhouse.tcpPort" -}}
{{- if .Values.clickhouse.enabled }}
{{- default 9000 .Values.clickhouse.service.tcpPort }}
{{- else }}
{{- default 9000 .Values.externalClickhouse.tcpPort }}
{{- end }}
{{- end -}}

{{/*
Set Clickhouse http port
*/}}
{{- define "clickhouse.httpPort" -}}
{{- if .Values.clickhouse.enabled }}
{{- default 8123 .Values.clickhouse.service.httpPort }}
{{- else }}
{{- default 8123 .Values.externalClickhouse.httpPort }}
{{- end }}
{{- end -}}

{{/*
Return true if a secret object for ClickHouse should be created
*/}}
{{- define "clickhouse.createSecret" -}}
{{- if and (not .Values.clickhouse.enabled) (not .Values.externalClickhouse.existingSecret) .Values.externalClickhouse.password }}
    {{- true -}}
{{- end -}}
{{- end -}}

{{/*
Return the ClickHouse secret name
*/}}
{{- define "clickhouse.secretName" -}}
{{- if .Values.externalClickhouse.existingSecret }}
    {{- .Values.externalClickhouse.existingSecret | quote -}}
{{- else -}}
    {{- printf "%s-external" ( include "clickhouse.servicename" .) -}}
{{- end -}}
{{- end -}}

{{/*
Return the ClickHouse secret key
*/}}
{{- define "clickhouse.secretPasswordKey" -}}
{{- if .Values.externalClickhouse.existingSecret }}
    {{- required "You need to provide existingSecretPasswordKey when an existingSecret is specified in externalClickhouse" .Values.externalClickhouse.existingSecretPasswordKey | quote }}
{{- else -}}
    {{- printf "clickhouse-password" -}}
{{- end -}}
{{- end -}}

{{/*
Return the external ClickHouse password
*/}}
{{- define "clickhouse.externalPasswordKey" -}}
{{- if .Values.externalClickhouse.password }}
  {{- required "externalClickhouse.password is required if using external clickhouse" .Values.externalClickhouse.password -}}
{{- end -}}
{{- end -}}

{{/*
Return the ClickHouse http URL
*/}}
{{- define "clickhouse.httpUrl" -}}
{{- $httpUrl := "" -}}
{{- $httpPrefix := "" -}}
{{- if .Values.clickhouse.enabled }}
  {{- $httpUrl = printf "%s:%s" (include "clickhouse.servicename" .) (include "clickhouse.httpPort" .) }}
  {{- if .Values.clickhouse.secure }}
    {{- $httpPrefix = "https://" }}
  {{- end }}
{{- else }}
  {{- $httpUrl = printf "%s:%s" (required "externalClickhouse.host is required if using external clickhouse" .Values.externalClickhouse.host) ( include "clickhouse.httpPort" .) }}
  {{- if .Values.externalClickhouse.secure }}
    {{- $httpPrefix = "https://" }}
  {{- end }}
{{- end }}
{{- printf "%s%s" $httpPrefix $httpUrl }}
{{- end -}}

{{/*
Return the ClickHouse Metrics URL
*/}}
{{- define "clickhouse.metricsUrl" -}}
{{- if .Values.clickhouse.enabled -}}
  tcp://{{ .Values.clickhouse.user }}:{{ .Values.clickhouse.password }}@{{ include "clickhouse.servicename" . }}:{{ include "clickhouse.tcpPort" . }}/{{ .Values.clickhouse.database -}}
{{- else -}}
  tcp://{{ .Values.externalClickhouse.user }}:{{ include "clickhouse.externalPasswordKey" . }}@{{- required "externalClickhouse.host is required if using external clickhouse" .Values.externalClickhouse.host }}:{{ include "clickhouse.tcpPort" . }}/{{ .Values.externalClickhouse.database -}}
{{- end -}}
{{- end -}}

{{/*
Return the ClickHouse Traces URL
*/}}
{{- define "clickhouse.tracesUrl" -}}
{{- if .Values.clickhouse.enabled -}}
  tcp://{{ .Values.clickhouse.user }}:{{ .Values.clickhouse.password }}@{{ include "clickhouse.servicename" . }}:{{ include "clickhouse.tcpPort" . }}/{{ .Values.clickhouse.traceDatabase -}}
{{- else -}}
  tcp://{{ .Values.externalClickhouse.user }}:$(CLICKHOUSE_PASSWORD)@{{ required "externalClickhouse.host is required if using external clickhouse" .Values.externalClickhouse.host }}:{{ include "clickhouse.tcpPort" . }}/{{ .Values.externalClickhouse.traceDatabase -}}
{{- end -}}
{{- end -}}

{{- define "clickhouse.clickHouseUrl" -}}
{{- if .Values.clickhouse.enabled -}}
  {{- include "clickhouse.servicename" . }}:{{ include "clickhouse.tcpPort" . }}/?username={{ .Values.clickhouse.user }}&password={{ .Values.clickhouse.password -}}
{{- else -}}
  {{- required "externalClickhouse.host is required if using external clickhouse" .Values.externalClickhouse.host }}:{{ include "clickhouse.tcpPort" . }}/?username={{ .Values.externalClickhouse.user }}&password=$(CLICKHOUSE_PASSWORD)
{{- end -}}
{{- end -}}