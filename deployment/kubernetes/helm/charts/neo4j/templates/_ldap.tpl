{{- define "neo4j.ldapPasswordFromSecretExistsOrNot" -}}
    {{- template "neo4j.ldapPasswordMountPath" . -}}
    {{- if (.Values.ldapPasswordFromSecret | trim) -}}
        {{- if (not .Values.disableLookups) -}}
            {{- $secret := (lookup "v1" "Secret" .Release.Namespace .Values.ldapPasswordFromSecret) }}
            {{- $secretExists := $secret | all }}

            {{- if (not $secretExists) -}}
                {{ fail (printf "Secret %s configured in 'ldapPasswordFromSecret' not found" .Values.ldapPasswordFromSecret) }}
            {{- else if not (hasKey $secret.data "LDAP_PASS") -}}
                {{ fail (printf "Secret %s must contain key LDAP_PASS" .Values.ldapPasswordFromSecret) }}
            {{- end -}}
        {{- end -}}
        {{- true -}}
    {{- end -}}
{{- end -}}

{{/* checks if ldapPasswordMountPath is set or not when ldapPasswordFromSecret is defined */}}
{{- define "neo4j.ldapPasswordMountPath" -}}
    {{- if or (.Values.ldapPasswordMountPath | trim) (.Values.ldapPasswordFromSecret | trim) -}}
        {{- if not (eq .Values.neo4j.edition "enterprise") -}}
            {{ fail (printf "ldapPasswordFromSecret and ldapPasswordMountPath are Enterprise Edition feature only. Please set edition to enterprise via --set neo4j.edition=\"enterprise\"") }}
        {{- end -}}
    {{- end -}}

    {{- if and (.Values.ldapPasswordFromSecret | trim) (not (.Values.ldapPasswordMountPath | trim)) -}}
        {{ fail (printf "Please define 'ldapPasswordMountPath'") }}
    {{- end -}}

    {{- if and (.Values.ldapPasswordMountPath | trim) (not (.Values.ldapPasswordFromSecret | trim)) -}}
        {{ fail (printf "Please define 'ldapPasswordFromSecret'") }}
    {{- end -}}
{{- end -}}

{{/* checks if ldapPasswordMountPath is set or not when ldapPasswordFromSecret is defined */}}
{{- define "neo4j.ldapVolumeMount" -}}
    {{- if and (.Values.ldapPasswordFromSecret | trim) (.Values.ldapPasswordMountPath | trim) }}
- mountPath: "{{ .Values.ldapPasswordMountPath }}"
  readOnly: true
  name: neo4j-ldap-password
    {{- end }}
{{- end -}}

{{- define "neo4j.ldapVolume" -}}
    {{- if and (.Values.ldapPasswordFromSecret | trim) (.Values.ldapPasswordMountPath | trim) }}
- name: neo4j-ldap-password
  secret:
    secretName: "{{- .Values.ldapPasswordFromSecret -}}"
    {{- end }}
{{- end -}}
