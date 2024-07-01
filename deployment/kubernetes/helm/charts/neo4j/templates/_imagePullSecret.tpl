{{/* To see imagePullSecret registrys in the statefulset*/}}
{{- define "neo4j.imagePullSecrets" -}}
     {{/* add imagePullSecrets only when the list of imagePullSecrets is not emtpy and does not have all the entries as empty */}}
    {{- if and (not (empty .)) (ne (len ( . | join "" | trim)) 0) }}
imagePullSecrets:
    {{- range $name := . -}}
        {{/* do not allow empty imagePullSecret registries */}}
        {{- if ne (len ($name | trim)) 0 }}
 - name: "{{ $name }}"
        {{- end -}}
    {{- end -}}
    {{- end -}}
{{- end -}}

{{/* Generates the .dockerconfigjson value for the respective .Values.neo4j.image.imageCredentials */}}
{{- define "neo4j.imagePullSecret.dockerConfigJson" }}
{{- printf "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"auth\":\"%s\"}}}" .registry .username .password .email (printf "%s:%s" .username .password | b64enc) | b64enc }}
{{- end }}

{{/*checkForEmptyFieldsAndDuplicates checks whether the imageCredential fields are empty or have any duplicates or not */}}
{{- define "neo4j.imageCredentials.checkForEmptyFieldsAndDuplicates" -}}

    {{- if .Values.image.imageCredentials -}}
        {{- $errorList := list -}}
        {{- $nameList := list -}}
        {{- range $index, $element := .Values.image.imageCredentials }}

            {{- if kindIs "invalid" $element -}}
                {{- $errorList = append $errorList (printf "Empty ImageCredential. Please provide either secretname or (registry|username|email|password|name) field(s)") -}}

            {{/* check for empty registry,username,email,password,name fields only when secret name is NOT PROVIDED */}}
            {{- else -}}

                {{- $usernameMessage := dict "fieldName" "username" "value" $element.username | include "neo4j.imageCredentials.checkMissingorEmptyField" -}}
                {{- $emailMessage := dict "fieldName" "email" "value" $element.email | include "neo4j.imageCredentials.checkMissingorEmptyField" -}}
                {{- $message := printf "%s,%s" $usernameMessage $emailMessage -}}
                {{- if and (not (empty $usernameMessage)) (not (empty $emailMessage)) -}}
                    {{- $errorList = append $errorList $message -}}
                {{- end -}}

                {{- $message = dict "fieldName" "password" "value" $element.password | include "neo4j.imageCredentials.checkMissingorEmptyField" -}}
                {{- if not (empty $message) -}}
                    {{- $errorList = append $errorList $message -}}
                {{- end -}}

                {{- $message = dict "fieldName" "name" "value" $element.name | include "neo4j.imageCredentials.checkMissingorEmptyField" -}}
                {{- if not (empty $message) -}}
                    {{- $errorList = append $errorList $message -}}
                {{- else -}}
                    {{- $nameList = append $nameList ($element.name | trim) -}}
                {{- end -}}

            {{- end -}}
        {{- end -}}

        {{- if not (empty $errorList) -}}
           {{ fail (printf "%s" ($errorList | join "\n")) }}
        {{- else -}}
            {{- $nameList = $nameList | uniq -}}
            {{- if ne (len $nameList) (len .Values.image.imageCredentials) -}}
                {{ fail (printf "Duplicate \"names\" found in imageCredentials list. Please remove duplicates") }}
            {{- end -}}
        {{- end -}}

    {{- end -}}
{{- end -}}


{{/* getImageCredential returns an imageCredential for the given name */}}
{{- define "neo4j.imageCredentials.getImageCredential" -}}

    {{- $imagePullSecretName := .imagePullSecret -}}
    {{- range $index, $element := .imageCredentials -}}
        {{- if eq $element.name $imagePullSecretName -}}
            {{- $element | toYaml -}}
        {{- end -}}
    {{- end -}}
{{- end -}}

{{/* checkMissingorEmptyField checks if the provided field is empty or missing */}}
{{- define "neo4j.imageCredentials.checkMissingorEmptyField" -}}
    {{- if kindIs "invalid" .value -}}
        {{- printf "%s field is missing for imageCredential" .fieldName -}}
    {{- else if empty (.value | trim) -}}
        {{- printf "%s field cannot be empty for imageCredential" .fieldName -}}
    {{- end -}}
{{- end -}}
