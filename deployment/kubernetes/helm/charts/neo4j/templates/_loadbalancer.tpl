{{- define "neo4j.services.neo4j.defaultSpec" -}}
ClusterIP:
  sessionAffinity: None
NodePort:
  sessionAffinity: None
  externalTrafficPolicy: Local
LoadBalancer:
  sessionAffinity: None
  externalTrafficPolicy: Local
{{- end }}


{{- define "neo4j.services.extraSpec" -}}
{{- if hasKey . "type" }}{{ fail "field 'type' is not supported in Neo4j Helm Chart service.*.spec" }}{{ end }}
{{- if hasKey . "selector" }}{{ fail "field 'selector' is not supported in Neo4j Helm Chart service.*.spec" }}{{ end }}
{{- if hasKey . "ports" }}{{ fail "field 'ports' is not supported in Neo4j Helm Chart service.*.spec" }}{{ end }}
{{ toYaml . }}
{{- end }}
