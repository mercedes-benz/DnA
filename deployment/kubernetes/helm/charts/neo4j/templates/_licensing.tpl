{{- define "neo4j.checkLicenseAgreement" }}
{{- $isEnterprise := required "neo4j.edition must be specified" .Values.neo4j.edition | regexMatch "(?i)enterprise" -}}
{{- if $isEnterprise }}
  {{- if not (kindIs "string" .Values.neo4j.acceptLicenseAgreement) | or (not .Values.neo4j.acceptLicenseAgreement) }}
  {{- include "neo4j.licenseAgreementMessage" .Values.neo4j.acceptLicenseAgreement | fail }}
  {{- else }}
  {{- if ne .Values.neo4j.acceptLicenseAgreement "yes" }}
    {{- include "neo4j.licenseAgreementMessage" .Values.neo4j.acceptLicenseAgreement | fail }}
  {{- end }}
  {{- end }}
{{- end }}
{{- end }}

{{- define "neo4j.licenseAgreementMessage" }}

In order to use Neo4j Enterprise Edition you must have a Neo4j license agreement.
More information is available at: https://neo4j.com/licensing/
Email inquiries can be directed to: licensing@neo4j.com
Set neo4j.acceptLicenseAgreement: "yes" to confirm that you have a Neo4j license agreement.

{{ end -}}
