{{/* vim: set filetype=mustache: */}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "neo4j.fullname" -}}
    {{- if .Values.fullnameOverride -}}
        {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
    {{- else -}}
        {{- if .Values.nameOverride -}}
            {{- $name := default .Chart.Name .Values.nameOverride -}}
            {{- if contains $name .Release.Name -}}
                {{- .Release.Name | trunc 63 | trimSuffix "-" -}}
            {{- else -}}
                {{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
            {{- end -}}
       {{- else -}}
            {{- printf "%s" .Release.Name | trunc 63 | trimSuffix "-" -}}
       {{- end -}}
    {{- end -}}
{{- end -}}

{{/*
Convert a neo4j.conf properties text into valid yaml
*/}}
{{- define "neo4j.configYaml" -}}
  {{- regexReplaceAll "(?m)^([^=]*?)=" ( regexReplaceAllLiteral "\\s*(#|server\\.jvm\\.additional).*" . "" )  "${1}: " | trim | replace ": true\n" ": 'true'\n" | replace ": true" ": 'true'\n" | replace ": false\n" ": 'false'\n" | replace ": false" ": 'false'\n"  | replace ": yes\n" ": 'yes'\n" | replace ": yes" ": 'yes'\n" | replace ": no" ": 'no'\n" | replace ": no\n" ": 'no'\n" }}
{{- end -}}

{{- define "neo4j.configJvmAdditionalYaml" -}}
  {{- /* This collects together all server.jvm.additional entries */}}
  {{- range ( regexFindAll "(?m)^\\s*(server\\.jvm\\.additional=).+" . -1 ) }}{{ trim . | replace "server.jvm.additional=" "" | trim | nindent 2 }}{{- end }}
{{- end -}}

{{- define "neo4j.name" -}}
  {{- if eq (len (trim $.Values.neo4j.name)) 0 -}}
    {{- fail (printf "neo4j.name is required") -}}
  {{- else -}}
    {{ .Values.neo4j.name }}
  {{- end -}}
{{- end -}}

{{- define "neo4j.isClusterEnabled" -}}
      {{- $minClusterSize := index $.Values.neo4j "minimumClusterSize" | default 1 | int -}}
      {{- if ge $minClusterSize 3 -}}
          {{- if not (eq $.Values.neo4j.edition "enterprise") -}}
               {{- fail (printf "Please use enterprise edition for clustering. You can set edition via --set neo4j.edition=enterprise") -}}
          {{- end -}}
            true
      {{- else -}}
            false
      {{- end -}}
  {{- end -}}

{{/* checkNodeSelectorLabels checks if there is any node in the cluster which has nodeSelector labels */}}
{{- define "neo4j.checkNodeSelectorLabels" -}}
    {{- if and (not (empty $.Values.nodeSelector)) (not $.Values.disableLookups) -}}
        {{- $validNodes := 0 -}}
        {{- $numberOfLabelsRequired := len $.Values.nodeSelector -}}
        {{- range $index, $node := (lookup "v1" "Node" .Release.Namespace "").items -}}
            {{- $nodeLabels :=  $node.metadata.labels -}}
            {{- $numberOfLabelsFound := 0 -}}
            {{/* match all the nodeSelector labels with the existing node labels*/}}
            {{- range $name,$value := $.Values.nodeSelector -}}
                {{- if hasKey $nodeLabels $name -}}
                    {{- if eq ($value | toString) (get $nodeLabels $name | toString ) -}}
                        {{- $numberOfLabelsFound = add1 $numberOfLabelsFound -}}
                    {{- end -}}
                {{- end -}}
            {{- end -}}

            {{/* increment valid nodes if the number of labels required are matching with the number of labels found */}}
            {{- if eq $numberOfLabelsFound $numberOfLabelsRequired -}}
                {{- $validNodes = add1 $validNodes -}}
            {{- end -}}
        {{- end -}}

        {{- if eq $validNodes 0 -}}
            {{- fail (print "No node exists in the cluster which has all the below labels (.Values.nodeSelector) \n %s" ($.Values.nodeSelector | join "\n" | toString) ) -}}
        {{- end -}}
    {{- end -}}
{{- end -}}

{{/*
If no password is set in `Values.neo4j.password` generates a new random password and modifies Values.neo4j so that the same password is available everywhere
*/}}
{{- define "neo4j.password" -}}
  {{- if and (not .Values.neo4j.passwordFromSecret) (not .Values.neo4j.password) }}
    {{- $password :=  randAlphaNum 14 }}
    {{- $secretName := include "neo4j.name" . | printf "%s-auth" }}

    {{- $secret := list }}
    {{- if not .Values.disableLookups -}}
        {{- $secret = (lookup "v1" "Secret" .Release.Namespace $secretName) }}
    {{- end -}}

    {{- if $secret }}
      {{- $password = index $secret.data "NEO4J_AUTH" | b64dec | trimPrefix "neo4j/" -}}
    {{- end -}}
    {{- $ignored := set .Values.neo4j "password" $password }}
  {{- end -}}
  {{- if and (.Values.neo4j.password) (not .Values.neo4j.passwordFromSecret)   -}}
  {{- .Values.neo4j.password }}
{{- end -}}
  {{- if .Values.neo4j.passwordFromSecret  -}}
{{- printf "$(kubectl get secret %s -o go-template='{{.data.NEO4J_AUTH | base64decode }}' | cut -d '/' -f2)" .Values.neo4j.passwordFromSecret -}}
{{- end -}}
{{- end -}}

{{- define "podSpec.checkLoadBalancerParam" }}
{{- $isLoadBalancerValuePresent := required (include "podSpec.loadBalancer.mustBeSetMessage" .) .Values.podSpec.loadbalancer | regexMatch "(?i)include$|(?i)exclude$" -}}
{{- if not $isLoadBalancerValuePresent }}
{{- include "podSpec.loadBalancer.mustBeSetMessage" . | fail -}}
{{- end }}
{{- end }}

{{- define "podSpec.loadBalancer.mustBeSetMessage" }}

Set podSpec.loadbalancer to one of: "include", "exclude".

E.g. by adding `--set podSpec.loadbalancer=include`

{{ end -}}

{{- define "neo4j.checkResources" -}}
    {{- template "neo4j.resources.checkForEmptyResources" . -}}
    {{- template "neo4j.resources.evaluateCPU" . -}}
    {{- template "neo4j.resources.evaluateMemory" . -}}
{{- end -}}

{{/* checks if the resources are empty or not */}}
{{- define "neo4j.resources.checkForEmptyResources" -}}

    {{/* check for missing cpu and memory values */}}
    {{- if and (kindIs "invalid" .Values.neo4j.resources) (kindIs "invalid" .Values.neo4j.resources.requests) -}}
        {{ $message := printf "Missing neo4j.resources.requests.cpu and neo4j.resources.requests.memory values ! \n %s \n %s" (include "neo4j.resources.minCPUMessage" .) (include "neo4j.resources.minMemoryMessage" .) }}
        {{ fail $message }}
    {{- end -}}

    {{- $cpu := "" }}
    {{- $memory := "" }}
    {{- $isMemorySet := false }}
    {{- $isCPUSet := false }}

    {{- if not (kindIs "invalid" .Values.neo4j.resources.requests) }}

        {{- if not (kindIs "invalid" .Values.neo4j.resources.requests.cpu) }}
            {{- dict "val" .Values.neo4j.resources.requests.cpu "errMsg" "neo4j.resources.requests.cpu cannot be set to \"\"" | include "neo4j.resources.checkForEmptyString" -}}
            {{- $cpu =  .Values.neo4j.resources.requests.cpu | toString }}
            {{- $isCPUSet = true }}
        {{- end -}}

        {{- if not (kindIs "invalid" .Values.neo4j.resources.requests.memory) }}
            {{- dict "val" .Values.neo4j.resources.requests.memory "errMsg" "neo4j.resources.requests.memory cannot be set to \"\"" | include "neo4j.resources.checkForEmptyString" -}}
            {{- $memory =  .Values.neo4j.resources.requests.memory | toString }}
            {{- $isMemorySet = true }}
        {{- end -}}

    {{- end -}}


    {{- if not $isCPUSet }}
        {{- if kindIs "invalid" .Values.neo4j.resources.cpu -}}
            {{- fail (printf "Empty or Missing neo4j.resources.cpu value \n %s" (include "neo4j.resources.minCPUMessage" .)) -}}
        {{- end -}}
        {{- dict "val" .Values.neo4j.resources.cpu "errMsg" "neo4j.resources.cpu cannot be set to \"\"" | include "neo4j.resources.checkForEmptyString" -}}
        {{- $cpu = .Values.neo4j.resources.cpu | toString }}
    {{- end -}}

    {{- if not $isMemorySet }}
         {{- if kindIs "invalid" .Values.neo4j.resources.memory -}}
            {{- fail (printf "Empty or Missing neo4j.resources.memory value \n %s" (include "neo4j.resources.minMemoryMessage" .)) -}}
        {{- end -}}
        {{- dict "val" .Values.neo4j.resources.memory "errMsg" "neo4j.resources.memory cannot be set to \"\"" | include "neo4j.resources.checkForEmptyString" -}}
        {{- $memory = .Values.neo4j.resources.memory | toString }}
    {{- end -}}

    {{- $requests := dict "cpu" $cpu "memory" $memory -}}
    {{- $ignored := set .Values.neo4j.resources "requests" $requests -}}

    {{/* set limits as same as cpu and memory if not provided by the user */}}
    {{- if kindIs "invalid" .Values.neo4j.resources.limits -}}
       {{- $ignored := set .Values.neo4j.resources "limits" $requests -}}
    {{- end -}}

{{- end -}}

{{- define "neo4j.resources.evaluateCPU" -}}

    {{/* check regex here :- https://regex101.com/r/wJsFcO/1 */}}
    {{ $cpuRegex := "(^\\d+)((\\.?[^\\.a-zA-Z])?)([0-9]*m?$)" }}

    {{- $cpu := .Values.neo4j.resources.requests.cpu | toString }}

    {{- if not (regexMatch $cpuRegex $cpu) -}}
        {{ fail (printf "Invalid cpu value %s\n%s" $cpu (include "neo4j.resources.minCPUMessage" .)) }}
    {{- end -}}

    {{- $cpuRegexValue := regexFind $cpuRegex $cpu -}}
    {{- $cpuFloat := 0.0 -}}
    {{/* cpu="123m" , convert millicore cpu to cpu */}}
    {{- if contains "m" $cpuRegexValue -}}
        {{ $cpuFloat = $cpuRegexValue | replace "m" "" | float64 -}}
        {{ $cpuFloat = divf $cpuFloat 1000 -}}
    {{- else -}}
        {{ $cpuFloat = $cpuRegexValue | float64 }}
    {{- end -}}

    {{- if lt $cpuFloat 0.5 }}
        {{ fail (printf "Provided cpu value %s is less than minimum. \n %s" $cpu (include "neo4j.resources.invalidCPUMessage" .) ) }}
    {{- end -}}
{{- end -}}


{{- define "neo4j.resources.evaluateMemory" -}}
    {{/* check regex here :- https://regex101.com/r/68NEQV/1 */}}
    {{ $memoryRegex := "(^\\d+)((\\.?[^\\.a-zA-Z\\s])?)(\\d*)(([EkMGTP]?|[EKMGTP]i?|e[+-]?\\d*[EKMGTP]?)$)" -}}

    {{- $memory := .Values.neo4j.resources.requests.memory | toString }}

    {{- if not (regexMatch $memoryRegex $memory) -}}
        {{ fail (printf "Invalid memory value %s\n%s" $memory (include "neo4j.resources.minMemoryMessage" .)) }}
    {{- end -}}

    {{- $memoryOrig := regexFind $memoryRegex $memory -}}
    {{- $memory = $memoryOrig -}}
    {{- $memoryFloat := 0.0 -}}

    {{- if contains "i" $memory -}}
        {{- $memory = $memory | replace "i" "" -}}
    {{- end -}}

    {{/* Mininum 2Gi or 2Gb, Converting the value type to Gb or Gi */}}

    {{/* 1kilo = 0.000001G */}}
    {{- if or (contains "K" $memory) (contains "k" $memory) -}}
        {{ $memoryFloat = divf ($memory | replace "K" "" | float64) 1000000 -}}

    {{/* 1mega = 0.001G */}}
    {{- else if contains "M" $memory -}}
        {{ $memoryFloat = divf ($memory | replace "M" "" | float64) 1000 -}}

    {{/* giga */}}
    {{- else if contains "G" $memory -}}
        {{ $memoryFloat = $memory | replace "G" "" | float64 -}}

    {{/* 1tera = 1000G */}}
    {{- else if contains "T" $memory -}}
        {{ $memoryFloat =  mulf ($memory | replace "T" "" | float64) 1000 -}}

    {{/* 1peta = 1000000G */}}
    {{- else if contains "P" $memory -}}
        {{ $memoryFloat = mulf ($memory | replace "P" "" | float64) 1000000 -}}

    {{/* 1exa = 1000000000G */}}
    {{- else if contains "E" $memory -}}
        {{ $memoryFloat = mulf ($memory | replace "E" "" | float64) 1000000000 -}}

    {{/* 1Byte = 0.000000001G */}}
    {{- else -}}
        {{ $memoryFloat = divf ($memory | float64) 1000000000 -}}
    {{- end -}}

    {{- if lt $memoryFloat 2.0 }}
        {{ fail (printf "Provided memory value %s is less than minimum. \n %s" $memoryOrig "Please set memory to be a minimum of 2Gi or 2G via --set neo4j.resources.memory=2Gi or --set neo4j.resources.memory=2G") }}
    {{- end -}}

{{- end -}}


{{- define "neo4j.resources.minCPUMessage" -}}
Please set cpu to be a minimum of 0.5 or 500m via --set neo4j.resources.cpu=0.5 or --set neo4j.resources.cpu=500m
{{- end -}}

{{- define "neo4j.resources.minMemoryMessage" -}}
Please set memory to be a minimum of 2Gi or 2G via --set neo4j.resources.memory=2Gi or --set neo4j.resources.memory=2G
{{- end -}}

{{- define "neo4j.resources.invalidCPUMessage" -}}
cpu value cannot be less than 0.5 or 500m
{{- end -}}

{{- define "neo4j.resources.invalidMemoryMessage" -}}
memory value cannot be less than 2Gb or 2Gi
{{- end -}}

{{/* Takes a dict with "val" and "message" as input*/}}
{{- define "neo4j.resources.checkForEmptyString" -}}
    {{- if eq (len (trim (.val | toString))) 0 }}
        {{ fail (printf .errMsg) }}
    {{- end -}}
{{- end -}}

{{/* Checks if the provided priorityClassName already exists in the cluster or not*/}}
{{- define "neo4j.priorityClassName" -}}
    {{- if not (empty $.Values.podSpec.priorityClassName) -}}

        {{- $priorityClassName := $.Values.podSpec.priorityClassName -}}

        {{- if not $.Values.disableLookups -}}
            {{- $priorityClassName = (lookup "scheduling.k8s.io/v1" "PriorityClass" .Release.Namespace $.Values.podSpec.priorityClassName) -}}
        {{- end -}}

        {{- if empty $priorityClassName -}}
            {{- fail (printf "PriorityClass %s is missing in the cluster" $.Values.podSpec.priorityClassName) -}}
        {{- else -}}
priorityClassName: "{{ .Values.podSpec.priorityClassName }}"
            {{- end -}}
    {{- end -}}
{{- end -}}

{{- define "neo4j.tolerations" -}}
{{/* Add tolerations only if .Values.podSpec.tolerations contains entries */}}
    {{- if . }}
tolerations:
{{ toYaml . }}
    {{- end }}
{{- end -}}

{{- define "neo4j.affinity" -}}
    {{- if or (.Values.podSpec.nodeAffinity) (.Values.podSpec.podAntiAffinity) }}
affinity:
    {{- if and .Values.podSpec.podAntiAffinity }}
        {{- if eq (typeOf .Values.podSpec.podAntiAffinity) "bool" }}
    podAntiAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        - labelSelector:
            matchLabels:
              app: "{{ template "neo4j.name" . }}"
              helm.neo4j.com/pod_category: "neo4j-instance"
          topologyKey: kubernetes.io/hostname
        {{- else }}
    podAntiAffinity: {{ toYaml .Values.podSpec.podAntiAffinity | nindent 6 }}
        {{- end }}
    {{- end }}
    {{- if .Values.podSpec.nodeAffinity }}
    nodeAffinity:
{{ toYaml .Values.podSpec.nodeAffinity | indent 6 }}
    {{- end }}
    {{- end }}
{{- end -}}

{{- define "neo4j.secretName" -}}
    {{- if .Values.neo4j.passwordFromSecret -}}
        {{- if not .Values.disableLookups -}}
            {{- $secret := (lookup "v1" "Secret" .Release.Namespace .Values.neo4j.passwordFromSecret) }}
            {{- $secretExists := $secret | all }}
            {{- if not ( $secretExists ) -}}
                {{ fail (printf "Secret %s configured in 'neo4j.passwordFromSecret' not found" .Values.neo4j.passwordFromSecret) }}
            {{- else if not (hasKey $secret.data "NEO4J_AUTH") -}}
                {{ fail (printf "Secret %s must contain key NEO4J_AUTH" .Values.neo4j.passwordFromSecret) }}
            {{/*The secret must start with characters 'neo4j/`*/}}
            {{- else if not (index $secret.data "NEO4J_AUTH" | b64dec | regexFind "^neo4j\\/\\w*") -}}
                {{ fail (printf "Password in secret %s must start with the characters 'neo4j/'" .Values.neo4j.passwordFromSecret) }}
            {{- end -}}
        {{- end -}}
        {{- printf "%s" (tpl .Values.neo4j.passwordFromSecret $) -}}
    {{- else -}}
        {{- include "neo4j.name" . | printf "%s-auth" -}}
    {{- end -}}
{{- end -}}


{{- define "neo4j.passwordWarning" -}}
{{- if and (.Values.neo4j.password) (not .Values.neo4j.passwordFromSecret) -}}
WARNING: Passwords set using 'neo4j.password' will be stored in plain text in the Helm release ConfigMap.
Please consider using 'neo4j.passwordFromSecret' for improved security.
{{- end -}}
{{- end -}}

{{- define "neo4j.topologySpreadConstraints" -}}
{{/* Add tolerations only if .Values.podSpec.topologySpreadConstraints contains entries */}}
    {{- if $.Values.podSpec.topologySpreadConstraints }}
topologySpreadConstraints:
{{ toYaml $.Values.podSpec.topologySpreadConstraints }}
    {{- end }}
{{- end -}}
