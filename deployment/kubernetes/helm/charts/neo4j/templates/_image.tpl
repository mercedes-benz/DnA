{{- define "neo4j.defaultChartImage" -}}
{{- $isEnterprise := required "neo4j.edition must be specified" .Values.neo4j.edition | regexMatch "(?i)enterprise" -}}
 {{- $imageName := "neo4j:" -}}
 {{/* .Chart.AppVersion is set to "-" for headless and loadbalancer service*/}}
 {{- if eq $.Chart.AppVersion "-" -}}
    {{- $imageName = printf "%s%s" $imageName $.Chart.Version -}}
 {{- else -}}
    {{- $imageName = printf "%s%s" $imageName $.Chart.AppVersion -}}
 {{- end -}}
 {{- if $isEnterprise -}}
    {{- $imageName = printf "%s%s" $imageName "-enterprise" -}}
 {{- end -}}
 {{- $imageName -}}
{{- end -}}


{{- define "neo4j.image" -}}
{{- template "neo4j.checkLicenseAgreement" . -}}
{{- $image := include "neo4j.defaultChartImage" . -}}
{{/* Allow override if a custom image has been specified */}}
{{- if .Values.image -}}
  {{- if .Values.image.customImage -}}
    {{- $image = .Values.image.customImage -}}
  {{- end -}}
{{- end -}}
{{ $image }}
{{- end -}}

{{- define "cleanup.image" -}}
{{- with .Values.services.neo4j.cleanup.image -}}
    {{- $registryName := .registry -}}
    {{- $repositoryName := .repository -}}
    {{- $separator := ":" -}}
    {{- $termination := printf "%s.%s" $.Capabilities.KubeVersion.Major (regexReplaceAll "\\D+" $.Capabilities.KubeVersion.Minor "") -}}
    {{- if not (empty (.tag | trim)) -}}
        {{- $termination := .tag | toString -}}
    {{- end -}}
    {{- if .digest }}
        {{- $separator = "@" -}}
        {{- $termination = .digest | toString -}}
    {{- end -}}
    {{- printf "%s/%s%s%s" $registryName $repositoryName $separator $termination -}}
{{- end -}}
{{- end -}}
