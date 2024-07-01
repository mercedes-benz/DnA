{{- define "neo4j.volumeClaimTemplateSpec"  -}}
{{/*
This template converts a neo4j volume definition into a VolumeClaimTemplate spec based on the specified mode

The desired behaviour is specified by the 'mode' setting. Only the information in the selected 'mode' is used.

All modes except for "volume" are transformed into a "dynamic" spec and then into a plain VolumeClaimTemplate which is then output.
This is to ensure that there aren't dramatically different code paths (all routes ultimately use the same output path)

If "volume" mode is selected nothing is returned.
*/}}
{{- $ignored := required "Values must be passed in to helm volumeTemplate for use with internal templates" .Values -}}
{{- $ignored = required "Template must be passed in to helm volumeTemplate so that tpl function works" .Template -}}
{{- $name := required "name must be passed in to helm volumeTemplate so that tpl function works" .name -}}
{{/*
Deep Copy the provided volume object so that we can mutate it safely in this template
*/}}
{{- $volume := deepCopy .volume -}}

{{- $validModes := "share|selector|defaultStorageClass|dynamic|volume|volumeClaimTemplate" -}}
{{- if not ( $volume.mode | regexMatch $validModes ) -}}
  {{- fail ( cat "\nUnknown volume mode:" $volume.mode "\nValid modes are: " $validModes ) -}}
{{- end -}}
{{- $originalMode := $volume.mode -}}
{{- $ignored = get $volume $volume.mode | required (cat "Volume" $name "is missing field:" $volume.mode ) -}}
{{/*
If defaultStorageClass is chosen overwrite "dynamic" and switch to dynamic mode
*/}}
{{-  if eq $volume.mode "defaultStorageClass"  -}}
  {{- $ignored = set $volume "dynamic" $volume.defaultStorageClass -}}
  {{-  if $volume.dynamic.storageClassName -}}
    {{- fail "If using mode defaultStorageClass then storageClassName should not be set" -}}
  {{- end -}}
  {{- $ignored = set $volume "mode" "dynamic" -}}
{{- end -}}

{{/*
If selector is chosen process the selector template and then overwrite "dynamic" and switch to dynamic mode
*/}}
{{- if eq $volume.mode "selector" -}}
  {{- $ignored = set $volume.selector "selector" ( tpl ( toYaml $volume.selector.selectorTemplate ) . | fromYaml ) -}}
  {{- $ignored = set $volume "dynamic" $volume.selector -}}
  {{- $ignored = set $volume "mode" "dynamic" -}}
{{- end -}}

{{- if eq $volume.mode "dynamic" -}}
    {{- $requests := required ( include "neo4j.volumeClaimTemplate.resourceMissingError" (dict "name" $name "mode" $originalMode) ) $volume.dynamic.requests -}}
    {{- $ignored := required ( include "neo4j.volumeClaimTemplate.resourceMissingError" (dict "name" $name "mode" $originalMode) ) $requests.storage -}}

    {{- $ignored = set $volume "mode" "volumeClaimTemplate" -}}
    {{- $ignored = dict "requests" $requests | set $volume.dynamic "resources" -}}
    {{- $ignored = set $volume "volumeClaimTemplate" ( omit $volume.dynamic "requests" "selectorTemplate" ) -}}
{{- end -}}

{{- if eq $volume.mode "volumeClaimTemplate" -}}
    {{- omit $volume.volumeClaimTemplate "setOwnerAndGroupWritableFilePermissions" | toYaml  -}}
{{- end -}}
{{- end -}}

{{- define "neo4j.volumeSpec" -}}
{{- $ignored := required "Values must be passed in to helm volumeTemplate for use with internal templates" .Values -}}
{{- $ignored = required "Template must be passed in to helm volumeTemplate so that tpl function works" .Template -}}
{{- if eq .volume.mode "volume" -}}
{{ omit .volume.volume "setOwnerAndGroupWritableFilePermissions" | toYaml  }}
{{- end -}}
{{- end -}}

{{- define "neo4j.volumeClaimTemplate.resourceMissingError" -}}
"The storage capacity of volumes.{{ .name }} must be specified when using '{{ .mode }}' mode. Set volumes.{{ .name }}.{{ .mode }}.requests.storage to a suitable value (e.g. 100Gi)"
{{- end }}

{{- define "neo4j.volumeClaimMetadata" -}}
- metadata:
    name: "{{ .name }}"
    {{- if .volume.labels }}
    labels: {{ .volume.labels | toYaml | nindent 6 }}
    {{- end }}
{{- end -}}

{{- define "neo4j.volumeClaimTemplates" -}}
{{- $neo4jName := include "neo4j.name" . }}
{{- $template := .Template -}}
{{- range $name, $spec := .Values.volumes -}}
{{- if $spec -}}
{{- $volumeClaim := dict "Template" $template "Values" $.Values "volume" $spec "name" $name | include "neo4j.volumeClaimTemplateSpec" -}}
{{- if $volumeClaim -}}
  {{- template "neo4j.volumeClaimMetadata" (dict "volume" $spec "name" $name) }}
  spec: {{- $volumeClaim | nindent 4 }}
{{/* blank line, important! */}}{{ end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/* This template doesn't output anything unless the mode is "volume" */}}
{{- define "neo4j.volumes" -}}
{{- $template := .Template -}}
{{- range $name, $spec := .Values.volumes -}}
{{- if $spec -}}
{{- $volumeYaml := dict "Template" $template "Values" $.Values "volume" $spec | include "neo4j.volumeSpec" -}}
{{- if $volumeYaml -}}
- name: "{{ $name }}"
  {{- $volumeYaml | nindent 2 }}
{{/* blank line, important! */}}{{ end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "neo4j.initChmodContainer" }}
{{- $initChmodScript := include "neo4j.initChmodScript" . }}
{{- if $initChmodScript }}
name: "set-volume-permissions"
image: "{{ template "neo4j.image" . }}"
env:
  - name: POD_NAME
    valueFrom:
      fieldRef:
        fieldPath: metadata.name
securityContext:
  runAsNonRoot: false
  runAsUser: 0
  runAsGroup: 0
volumeMounts: {{- include "neo4j.volumeMounts" .Values.volumes | nindent 2 }}
command:
  - "bash"
  - "-c"
  - |
    set -o pipefail -o errtrace -o errexit -o nounset
    shopt -s inherit_errexit
    [[ -n "${TRACE:-}" ]] && set -o xtrace
    {{- $initChmodScript | nindent 4 }}
{{- end }}
{{- end }}

{{- define "neo4j.initChmodScript" -}}
{{- $securityContext := .Values.securityContext -}}
{{- range $name, $spec := .Values.volumes -}}
{{- if (index $spec $spec.mode).setOwnerAndGroupWritableFilePermissions -}}
{{- if $securityContext -}}{{- if $securityContext.runAsUser }}
# change owner
chown -R "{{ $securityContext.runAsUser }}" "/{{ $name }}"
{{- end -}}{{- end -}}
{{- if $securityContext -}}{{- if $securityContext.runAsGroup }}

# change group
chgrp -R "{{ $securityContext.runAsGroup }}" "/{{ $name }}"
{{- end -}}{{- end }}

# make group writable
chmod -R g+rwx "/{{ $name }}"
{{- end -}}
{{- end -}}
{{- end -}}

{{/* Adds volume mount if apoc config specified */}}
{{- define "neo4j.apoc.volumeMount" -}}
    {{- if or $.Values.apoc_config $.Values.apoc_credentials }}
- mountPath: "/config/"
  name: "apoc-conf"
    {{- end }}
{{- end -}}
{{- define "neo4j.apoc.volume" -}}
    {{- if or $.Values.apoc_config $.Values.apoc_credentials }}
- name: apoc-conf
  projected:
    defaultMode: 0440
    sources:
      - configMap:
          name: "{{ .Release.Name }}-apoc-config"
     {{- end }}
{{- end -}}

{{- define "neo4j.additionalVolumes" -}}
    {{- if . }}
{{ toYaml . }}
    {{- end }}
{{- end -}}

{{- define "neo4j.additionalVolumeMounts" -}}
    {{- if . -}}
{{ toYaml . }}
    {{- end -}}
{{- end -}}


{{- define "neo4j.volumeMounts" -}}
{{- range $name, $spec := . }}
- mountPath: "/{{ $name }}"
  name: "{{ if eq $spec.mode "share" }}{{ $spec.share.name }}{{ if eq $name "data" }}{{ fail "data volume does not support mode: 'share'"}}{{ end }}{{ else }}{{ $name }}{{ end }}"
  {{- if not $spec.disableSubPathExpr }}
  subPathExpr: "{{ if $spec.subPathExpr }}{{ $spec.subPathExpr }}{{ else }}{{ $name }}{{ if regexMatch "logs|metrics" $name }}/$(POD_NAME){{ end }}{{ end }}"
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "neo4j.maintenanceVolumeMounts" -}}
{{- range $name, $spec := . }}
{{- if ne $spec.mode "share" | and (ne "data" $name) }}
- mountPath: "/maintenance/{{ $name }}_volume"
  name: "{{ $name }}"
  readOnly: true
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "neo4j.volumes.validation" -}}
{{- $dataVolume := required (include "neo4j.volumes.data.mustBeSetMessage" .) .Values.volumes.data -}}
{{- $dataVolumeMode := required (include "neo4j.volumes.data.modeRequiredMessage" .) .Values.volumes.data.mode -}}
{{- range $name, $spec := .Values.volumes -}}
  {{- range $otherName, $otherSpec := $.Values.volumes -}}
    {{- if ne $otherName $name}}
      {{- if eq $spec.mode $otherSpec.mode }}
        {{- if eq $spec.mode "volume" }}
          {{- if and $spec.volume.persistentVolumeClaim $otherSpec.volume.persistentVolumeClaim }}
            {{- if eq $spec.volume.persistentVolumeClaim.claimName $otherSpec.volume.persistentVolumeClaim.claimName }}
              {{ cat "Cannot mount the same persistent volume claim multiple times:" $name "and" $otherName "use the same persistent volume claim!" | fail }}
            {{- end }}
          {{- end }}
        {{- end }}
      {{- end }}
    {{- end }}
  {{- end }}
{{- end -}}
{{- end }}

{{- define "neo4j.volumes.data.mustBeSetMessage" }}

A data volume for Neo4j is required.

Set volumes.data and try again.

{{ end -}}

{{- define "neo4j.volumes.data.modeRequiredMessage" }}

A volume mode for the Neo4j 'data' volume is required.

Set volumes.data.mode to one of: "share", "selector", "defaultStorageClass", "volume", "volumeClaimTemplate" or "dynamic".

For details of how to configure volume modes see the Neo4j Helm chart documentation at https://neo4j.com/docs/operations-manual/current/kubernetes

To get up-and-running quickly, for development or testing, use "defaultStorageClass" for a dynamically provisioned volume of the default storage class.

E.g. by adding `--set volumes.data.mode=defaultStorageClass`

{{ end -}}
