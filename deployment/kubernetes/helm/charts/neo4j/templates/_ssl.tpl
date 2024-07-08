{{- define "neo4j.ssl.volumesFromSecrets" -}}
{{- range $name, $sslSpec := . -}}
{{- if ( or $sslSpec.privateKey.secretName $sslSpec.publicCertificate.secretName ) }}
- name: "{{ $name }}-cert"
  secret:
    secretName: "{{ required "When ssl.{{ $name }}.publicCertificate is set then ssl.{{ $name }}.privateKey.secretName must also be provided" $sslSpec.publicCertificate.secretName }}"
- name: "{{ $name }}-key"
  secret:
    secretName:  "{{ required "When ssl.bolt.privateKey is set then ssl.bolt.publicCertificate.secretName must also be provided" $sslSpec.privateKey.secretName }}"
{{- if $sslSpec.trustedCerts.sources }}
- name: "{{ $name }}-trusted"
  projected:
    defaultMode: 0440
    {{ $sslSpec.trustedCerts | toYaml | nindent 4 }}
{{- end }}
{{- if $sslSpec.revokedCerts.sources -}}
- name: "{{ $name }}-revoked"
  projected:
    defaultMode: 0440
    {{ $sslSpec.revokedCerts | toYaml | nindent 4 }}
{{/* blank line, important! */}}{{ end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "neo4j.ssl.volumeMountsFromSecrets" -}}
{{- range $name, $sslSpec := . -}}
{{- if ( or $sslSpec.privateKey.secretName $sslSpec.publicCertificate.secretName ) }}
- name: "{{ $name }}-cert"
  mountPath: /var/lib/neo4j/certificates/{{ $name }}/public.crt
  subPath: "{{ $sslSpec.publicCertificate.subPath | default "public.crt" }}"
  readOnly: true
- name: "{{ $name }}-key"
  mountPath: "/var/lib/neo4j/certificates/{{ $name }}/private.key"
  subPath: "{{ $sslSpec.privateKey.subPath | default "private.key"  }}"
  readOnly: true
{{- if $sslSpec.trustedCerts.sources }}
- name: "{{ $name }}-trusted"
  mountPath: "/var/lib/neo4j/certificates/{{ $name }}/trusted"
  readOnly: true
{{- end -}}
{{- if $sslSpec.revokedCerts.sources }}
- name: "{{ $name }}-revoked"
  mountPath: "/var/lib/neo4j/certificates/{{ $name }}/revoked"
  readOnly: true
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
