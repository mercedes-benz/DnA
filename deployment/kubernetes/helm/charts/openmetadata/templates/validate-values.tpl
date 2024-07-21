{{- if not (has .Values.openmetadata.config.authentication.provider (list "basic" "azure" "auth0" "custom-oidc" "google" "okta" "aws-cognito" "ldap" "saml")) }}
  {{ required "The authentication provider must be basic, azure, auth0, custom-oidc, google, okta, aws-cognito, ldap, saml" nil }}
{{- end }}

{{- if not .Values.openmetadata.config.openmetadata }}
{{- include "error-message" "Global key has been replaced by openmetadata.config. Please refer docs for the further explaination." }}
{{- end }}