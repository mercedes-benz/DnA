

{{/* checks if any of the KeyName , SecretName and SecretMountPath are missing or not */}}
{{- define "neo4j.apocCredentials.validation" -}}
    {{- if $.Values.apoc_credentials -}}
        {{- template "neo4j.apocCredentials.checkForEmptyOrMissingFields" $ -}}
        {{- template "neo4j.apocCredentials.checkSecretExistsOrNot" $ -}}
    {{- end -}}
{{- end -}}

{{- define "neo4j.apocCredentials.checkForEmptyOrMissingFields" -}}
    {{- $errors := list -}}
    {{- range tuple "jdbc" "elasticsearch" -}}
        {{- $credentialName := .}}
        {{- if hasKey $.Values.apoc_credentials $credentialName -}}
         {{- $credential := get $.Values.apoc_credentials $credentialName -}}
         {{- range tuple "aliasName" "secretName" "secretMountPath" -}}
            {{- $fieldName := . -}}
            {{- if not (hasKey $credential .) -}}
                {{- $errors = append $errors (printf "%s %s missing" $credentialName $fieldName) -}}
            {{- else if empty (get $credential . | trim) -}}
                {{- $errors = append $errors (printf "%s %s cannot be empty" $credentialName $fieldName) -}}
            {{- end -}}
         {{- end -}}
        {{- end -}}
    {{- end -}}

    {{- $errors = compact $errors -}}
    {{- if gt (len $errors) 0 -}}
        {{- fail (printf "%s" ($errors | join " , ")) -}}
    {{- end -}}
{{- end -}}

{{/* checks if the provided apocCredential secretName exists or not */}}
{{- define "neo4j.apocCredentials.checkSecretExistsOrNot" -}}
    {{- $errors := list -}}
    {{- range tuple "jdbc" "elasticsearch" -}}
        {{- $credentialName := . -}}
        {{- if and (hasKey $.Values.apoc_credentials $credentialName) (not $.Values.disableLookups) -}}
            {{- $credential := get $.Values.apoc_credentials $credentialName -}}
            {{- $secretName := (get $credential "secretName") -}}
            {{- $secret := (lookup "v1" "Secret" $.Release.Namespace $secretName) -}}
            {{- $secretExists := $secret | all -}}

            {{- if (not $secretExists) -}}
                {{- $errors = append $errors (printf "Secret '%s' configured in '.Values.apoc_credentials.%s.secretName' not found" $secretName $credentialName) -}}
            {{- else if not (hasKey $secret.data "URL") -}}
                {{- $errors = append $errors (printf "Secret '%s' must contain key URL" $secretName) -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}

    {{- $errors = compact $errors -}}
    {{- if gt (len $errors) 0 -}}
        {{- fail (printf "%s" ($errors | join " , ")) -}}
    {{- end -}}

{{- end -}}


{{- define "neo4j.apocCredentials.volumeMount" -}}
    {{- if $.Values.apoc_credentials -}}
        {{- range tuple "jdbc" "elasticsearch" -}}
            {{- $credentialName := . -}}
            {{- if (hasKey $.Values.apoc_credentials $credentialName) -}}
                {{- $credential := get $.Values.apoc_credentials $credentialName -}}
                {{- $secretMountPath := (get $credential "secretMountPath") }}
- mountPath: {{ $secretMountPath | quote }}
  readOnly: true
  name: {{ printf "apoc-%s-url" $credentialName | quote }}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}

{{- define "neo4j.apocCredentials.volume" -}}
    {{- if $.Values.apoc_credentials -}}
        {{- range tuple "jdbc" "elasticsearch" -}}
            {{- $credentialName := . -}}
            {{- if (hasKey $.Values.apoc_credentials $credentialName) -}}
                {{- $credential := get $.Values.apoc_credentials $credentialName -}}
                {{- $secretName := (get $credential "secretName") }}
- name: {{ printf "apoc-%s-url" $credentialName | quote }}
  secret:
    secretName: {{ $secretName | quote }}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}

{{- define "neo4j.apocCredentials.generateConfig" -}}
    {{- if $.Values.apoc_credentials -}}
        {{- range tuple "jdbc" "elasticsearch" -}}
            {{- $credentialName := . -}}
            {{- if (hasKey $.Values.apoc_credentials $credentialName) }}
                {{- $credential := get $.Values.apoc_credentials $credentialName }}
                {{- $aliasName := (get $credential "aliasName") }}
                {{- $secretName := (get $credential "secretName") }}
                {{- $secretMountPath := (get $credential "secretMountPath") }}
                {{- printf "apoc.%s.%s.url=$(bash -c 'cat %s/URL')\n" $credentialName $aliasName $secretMountPath }}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
