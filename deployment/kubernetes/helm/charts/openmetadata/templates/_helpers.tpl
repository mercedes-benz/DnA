{{/*
Expand the name of the chart.
*/}}
{{- define "OpenMetadata.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "OpenMetadata.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "OpenMetadata.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "OpenMetadata.labels" -}}
{{- with .Values.commonLabels }}
{{ toYaml .}}
{{- end }}
helm.sh/chart: {{ include "OpenMetadata.chart" . }}
{{ include "OpenMetadata.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "OpenMetadata.selectorLabels" -}}
app.kubernetes.io/name: {{ include "OpenMetadata.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "OpenMetadata.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "OpenMetadata.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Quoted Array of strings
*/}}
{{- define "OpenMetadata.commaJoinedQuotedList" }}
{{- $list := list }}
{{- range .value }}
{{- $list = append $list (. | quote) }}
{{- end }}
{{- join "," $list }}
{{- end -}}

{{/*
Build the OpenMetadata Migration Command */}}
{{- define "OpenMetadata.buildUpgradeCommand" }}
command:
- "/bin/bash"
- "-c"
{{- if .Values.openmetadata.config.upgradeMigrationConfigs.force }}
- "/opt/openmetadata/bootstrap/bootstrap_storage.sh migrate-all debug force"
{{- else }}
- "/opt/openmetadata/bootstrap/bootstrap_storage.sh migrate-all"
{{- end }}
{{- end }}

{{/* 
Warning to update openmetadata global keyword to openmetadata.config */}}
{{- define "error-message" }}
{{- printf "Error: %s" . | fail }}
{{- end }}

{{/*
OpenMetadata Configurations Environment Variables*/}}
{{- define "OpenMetadata.configs" -}}
{{- if .Values.openmetadata.config.fernetkey.secretRef -}}
- name: FERNET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Values.openmetadata.config.fernetkey.secretRef }}
      key: {{ .Values.openmetadata.config.fernetkey.secretKey }}
{{ else }}
- name: FERNET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ include "OpenMetadata.fullname" . }}-secret 
      key: FERNET_KEY
{{- end }}
- name: EVENT_MONITOR
  value: "{{ .Values.openmetadata.config.eventMonitor.type }}"
- name: EVENT_MONITOR_BATCH_SIZE
  value: "{{ .Values.openmetadata.config.eventMonitor.batchSize }}"
- name: EVENT_MONITOR_PATH_PATTERN
  value: '[{{ include "OpenMetadata.commaJoinedQuotedList" (dict "value" .Values.openmetadata.config.eventMonitor.pathPattern) }}]'
- name: EVENT_MONITOR_LATENCY
  value: '[{{ include "OpenMetadata.commaJoinedQuotedList" (dict "value" .Values.openmetadata.config.eventMonitor.latency) }}]'
- name: MASK_PASSWORDS_API
  value: '{{ .Values.openmetadata.config.maskPasswordsApi }}'
- name: OPENMETADATA_CLUSTER_NAME
  value: "{{ .Values.openmetadata.config.clusterName }}"
- name: OM_URI
  value: "{{ .Values.openmetadata.config.openmetadata.uri }}"
- name: LOG_LEVEL
  value: "{{ .Values.openmetadata.config.logLevel }}"
- name: SERVER_HOST
  value: "{{ .Values.openmetadata.config.openmetadata.host }}"
- name: SERVER_PORT
  value: "{{ .Values.openmetadata.config.openmetadata.port }}"
- name: SERVER_ADMIN_PORT
  value: "{{ .Values.openmetadata.config.openmetadata.adminPort }}"
- name: AUTHENTICATION_PROVIDER
  value: "{{ .Values.openmetadata.config.authentication.provider }}"
- name: AUTHENTICATION_PUBLIC_KEYS
  value: '[{{ include "OpenMetadata.commaJoinedQuotedList" (dict "value" .Values.openmetadata.config.authentication.publicKeys) }}]'
- name: AUTHENTICATION_AUTHORITY
  value: "{{ .Values.openmetadata.config.authentication.authority }}"
- name: AUTHENTICATION_CLIENT_ID
  value: "{{ .Values.openmetadata.config.authentication.clientId }}"
- name: AUTHENTICATION_CALLBACK_URL
  value: "{{ .Values.openmetadata.config.authentication.callbackUrl }}"
- name: AUTHENTICATION_JWT_PRINCIPAL_CLAIMS
  value: '[{{ include "OpenMetadata.commaJoinedQuotedList" (dict "value" .Values.openmetadata.config.authentication.jwtPrincipalClaims) }}]'
- name: AUTHENTICATION_ENABLE_SELF_SIGNUP
  value: "{{ .Values.openmetadata.config.authentication.enableSelfSignup }}"
- name: AUTHORIZER_CLASS_NAME
  value: "{{ .Values.openmetadata.config.authorizer.className }}"
- name: AUTHORIZER_REQUEST_FILTER
  value: "{{ .Values.openmetadata.config.authorizer.containerRequestFilter }}"
- name: AUTHORIZER_ADMIN_PRINCIPALS
  value: '[{{ include "OpenMetadata.commaJoinedQuotedList" (dict "value" .Values.openmetadata.config.authorizer.initialAdmins) }}]'
- name: AUTHORIZER_PRINCIPAL_DOMAIN
  value: "{{ .Values.openmetadata.config.authorizer.principalDomain }}"
- name: AUTHORIZER_ENFORCE_PRINCIPAL_DOMAIN
  value: "{{ .Values.openmetadata.config.authorizer.enforcePrincipalDomain }}"
- name: AUTHORIZER_ENABLE_SECURE_SOCKET
  value: "{{ .Values.openmetadata.config.authorizer.enableSecureSocketConnection }}"
- name: AUTHORIZER_ALLOWED_REGISTRATION_DOMAIN
  value: '[{{ include "OpenMetadata.commaJoinedQuotedList" (dict "value" .Values.openmetadata.config.authorizer.allowedEmailRegistrationDomains) }}]'
{{- if .Values.openmetadata.config.jwtTokenConfiguration.enabled }}
- name: RSA_PUBLIC_KEY_FILE_PATH
  value: "{{ .Values.openmetadata.config.jwtTokenConfiguration.rsapublicKeyFilePath }}"
- name: RSA_PRIVATE_KEY_FILE_PATH
  value: "{{ .Values.openmetadata.config.jwtTokenConfiguration.rsaprivateKeyFilePath }}"
- name: JWT_ISSUER
  value: "{{ .Values.openmetadata.config.jwtTokenConfiguration.jwtissuer }}"
- name: JWT_KEY_ID
  value: "{{ .Values.openmetadata.config.jwtTokenConfiguration.keyId }}"
{{- end }}
{{- if eq .Values.openmetadata.config.authentication.provider "ldap" }}
- name: AUTHENTICATION_LDAP_HOST
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.host }}"
- name: AUTHENTICATION_LDAP_PORT
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.port }}"
- name: AUTHENTICATION_LOOKUP_ADMIN_DN
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.dnAdminPrincipal }}"
{{- with .Values.openmetadata.config.authentication.ldapConfiguration.dnAdminPassword }}
- name: AUTHENTICATION_LOOKUP_ADMIN_PWD
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
- name: AUTHENTICATION_USER_LOOKUP_BASEDN
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.userBaseDN }}"
- name: AUTHENTICATION_USER_MAIL_ATTR
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.mailAttributeName }}"
- name: AUTHENTICATION_LDAP_POOL_SIZE
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.maxPoolSize }}"
- name: AUTHENTICATION_LDAP_SSL_ENABLED
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.sslEnabled }}"
- name: AUTHENTICATION_LDAP_TRUSTSTORE_TYPE
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.truststoreConfigType }}"
{{- if eq .Values.openmetadata.config.authentication.ldapConfiguration.truststoreConfigType "CustomTrustStore" }}
- name: AUTHENTICATION_LDAP_TRUSTSTORE_PATH
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.trustStoreFilePath }}"
{{- with .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.trustStoreFilePassword }}
- name: AUTHENTICATION_LDAP_KEYSTORE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
- name: AUTHENTICATION_LDAP_SSL_KEY_FORMAT
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.trustStoreFileFormat }}"
- name: AUTHENTICATION_LDAP_SSL_VERIFY_CERT_HOST
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.verifyHostname }}"
- name: AUTHENTICATION_LDAP_EXAMINE_VALIDITY_DATES
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.examineValidityDates }}"
{{- end }}
{{- if eq .Values.openmetadata.config.authentication.ldapConfiguration.truststoreConfigType "HostName" }}
- name: AUTHENTICATION_LDAP_ALLOW_WILDCARDS
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.hostNameConfig.allowWildCards }}"
- name: AUTHENTICATION_LDAP_ALLOWED_HOSTNAMES
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.hostNameConfig.acceptableHostNames }}"
{{- end }}
{{- if eq .Values.openmetadata.config.authentication.ldapConfiguration.truststoreConfigType "JVMDefault" }}          
- name: AUTHENTICATION_LDAP_SSL_VERIFY_CERT_HOST
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.jvmDefaultConfig.verifyHostname }}"
{{- end }}
{{- if eq .Values.openmetadata.config.authentication.ldapConfiguration.truststoreConfigType "TrustAll" }}  
- name: AUTHENTICATION_LDAP_EXAMINE_VALIDITY_DATES
  value: "{{ .Values.openmetadata.config.authentication.ldapConfiguration.trustStoreConfig.trustAllConfig.examineValidityDates }}"
{{- end }}
{{- end }}
{{- if eq .Values.openmetadata.config.authentication.provider "saml" }}
- name: SAML_DEBUG_MODE
  value: "{{ .Values.openmetadata.config.authentication.saml.debugMode }}"
- name: SAML_IDP_ENTITY_ID
  value: "{{ .Values.openmetadata.config.authentication.saml.idp.entityId }}"
- name: SAML_IDP_SSO_LOGIN_URL          
  value: "{{ .Values.openmetadata.config.authentication.saml.idp.ssoLoginUrl }}"
{{- with .Values.openmetadata.config.authentication.saml.idp.idpX509Certificate }}
- name: SAML_IDP_CERTIFICATE
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
- name: SAML_AUTHORITY_URL
  value: "{{ .Values.openmetadata.config.authentication.saml.idp.authorityUrl }}"
- name: SAML_IDP_NAME_ID
  value: "{{ .Values.openmetadata.config.authentication.saml.idp.nameId }}"
- name: SAML_SP_ENTITY_ID
  value: "{{ .Values.openmetadata.config.authentication.saml.sp.entityId }}"
- name: SAML_SP_ACS
  value: "{{ .Values.openmetadata.config.authentication.saml.sp.acs }}"
{{- with .Values.openmetadata.config.authentication.saml.sp.spX509Certificate }}
- name: SAML_SP_CERTIFICATE
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}            
- name: SAML_SP_CALLBACK
  value: "{{ .Values.openmetadata.config.authentication.saml.sp.callback }}"
- name: SAML_STRICT_MODE
  value: "{{ .Values.openmetadata.config.authentication.saml.security.strictMode }}"
- name: SAML_SP_TOKEN_VALIDITY
  value: "{{ .Values.openmetadata.config.authentication.saml.security.tokenValidity }}"
- name: SAML_SEND_ENCRYPTED_NAME_ID
  value: "{{ .Values.openmetadata.config.authentication.saml.security.sendEncryptedNameId }}"
- name: SAML_SEND_SIGNED_AUTH_REQUEST
  value: "{{ .Values.openmetadata.config.authentication.saml.security.sendSignedAuthRequest }}"
- name: SAML_SIGNED_SP_METADATA
  value: "{{ .Values.openmetadata.config.authentication.saml.security.signSpMetadata }}"
- name: SAML_WANT_MESSAGE_SIGNED
  value: "{{ .Values.openmetadata.config.authentication.saml.security.wantMessagesSigned }}"
- name: SAML_WANT_ASSERTION_SIGNED
  value: "{{ .Values.openmetadata.config.authentication.saml.security.wantAssertionsSigned }}"
- name: SAML_WANT_ASSERTION_ENCRYPTED
  value: "{{ .Values.openmetadata.config.authentication.saml.security.wantAssertionEncrypted }}"
- name: SAML_WANT_NAME_ID_ENCRYPTED
  value: "{{ .Values.openmetadata.config.authentication.saml.security.wantNameIdEncrypted }}"
{{- if or .Values.openmetadata.config.authentication.saml.security.wantAssertionEncrypted .Values.openmetadata.config.authentication.saml.security.wantNameIdEncrypted }}
# Key Store should only be considered if either wantAssertionEncrypted or wantNameIdEncrypted will be true
- name: SAML_KEYSTORE_FILE_PATH
  value: "{{ .Values.openmetadata.config.authentication.saml.security.keyStoreFilePath }}"
{{- with .Values.openmetadata.config.authentication.saml.security.keyStoreAlias }}
- name: SAML_KEYSTORE_ALIAS
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
{{- with .Values.openmetadata.config.authentication.saml.security.keyStorePassword }}
- name: SAML_KEYSTORE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
{{- end }}
{{- end }}
- name: ELASTICSEARCH_HOST
  value: "{{ .Values.openmetadata.config.elasticsearch.host }}"
- name: SEARCH_TYPE
  value: "{{ .Values.openmetadata.config.elasticsearch.searchType }}"
- name: ELASTICSEARCH_PORT
  value: "{{ .Values.openmetadata.config.elasticsearch.port }}"
- name: ELASTICSEARCH_SCHEME
  value: "{{ .Values.openmetadata.config.elasticsearch.scheme }}"
- name: ELASTICSEARCH_INDEX_MAPPING_LANG
  value: "{{ .Values.openmetadata.config.elasticsearch.searchIndexMappingLanguage }}"
- name: ELASTICSEARCH_KEEP_ALIVE_TIMEOUT_SECS
  value: "{{ .Values.openmetadata.config.elasticsearch.keepAliveTimeoutSecs }}"
{{- if .Values.openmetadata.config.elasticsearch.auth.enabled -}}
{{- with .Values.openmetadata.config.elasticsearch.auth }}
- name: ELASTICSEARCH_USER
  value: "{{ .username }}"
- name: ELASTICSEARCH_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .password.secretRef }}
      key: {{ .password.secretKey }}
{{- end }}
{{- end }}
{{- if .Values.openmetadata.config.elasticsearch.trustStore.enabled }}
- name: ELASTICSEARCH_TRUST_STORE_PATH
  value: {{.Values.openmetadata.config.elasticsearch.trustStore.path }}
{{- with .Values.openmetadata.config.elasticsearch.trustStore }}
- name: ELASTICSEARCH_TRUST_STORE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .password.secretRef }}
      key: {{ .password.secretKey }}
{{- end }}
{{- end }}
- name: DB_HOST
  value: "{{ .Values.openmetadata.config.database.host }}"
- name: DB_PORT
  value: "{{ .Values.openmetadata.config.database.port }}"
{{- with .Values.openmetadata.config.database.auth }}
- name: DB_USER
  value: "{{ .username }}"
- name: DB_USER_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .password.secretRef }}
      key: {{ .password.secretKey }}
{{- end }}
- name: OM_DATABASE
  value: "{{ .Values.openmetadata.config.database.databaseName }}"
- name: DB_DRIVER_CLASS
  value: "{{ .Values.openmetadata.config.database.driverClass }}"
- name: DB_SCHEME
  value: "{{ .Values.openmetadata.config.database.dbScheme }}"
- name: DB_PARAMS
  value: "{{ .Values.openmetadata.config.database.dbParams }}"
{{- if .Values.openmetadata.config.pipelineServiceClientConfig.enabled }}
- name: PIPELINE_SERVICE_CLIENT_ENABLED
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.enabled }}"
- name: PIPELINE_SERVICE_CLIENT_CLASS_NAME
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.className }}"
- name: PIPELINE_SERVICE_IP_INFO_ENABLED
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.ingestionIpInfoEnabled }}"
- name: PIPELINE_SERVICE_CLIENT_ENDPOINT
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.apiEndpoint }}"
{{ if .Values.openmetadata.config.pipelineServiceClientConfig.auth.enabled }}
{{- with .Values.openmetadata.config.pipelineServiceClientConfig.auth }}
- name: AIRFLOW_USERNAME
  value: "{{ .username }}"
- name: AIRFLOW_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .password.secretRef }}
      key: {{ .password.secretKey }}
{{- end }}
{{- end }}
- name: PIPELINE_SERVICE_CLIENT_VERIFY_SSL
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.verifySsl }}"
- name: PIPELINE_SERVICE_CLIENT_HOST_IP
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.hostIp }}"
- name: PIPELINE_SERVICE_CLIENT_HEALTH_CHECK_INTERVAL
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.healthCheckInterval }}"      
- name: PIPELINE_SERVICE_CLIENT_SSL_CERT_PATH
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.sslCertificatePath }}"
- name: SERVER_HOST_API_URL
  value: "{{ .Values.openmetadata.config.pipelineServiceClientConfig.metadataApiEndpoint }}"
{{- end }}
- name: SECRET_MANAGER
  value: "{{ .Values.openmetadata.config.secretsManager.provider }}"
{{- if .Values.openmetadata.config.secretsManager.additionalParameters.enabled }}
- name: OM_SM_REGION
  value: "{{ .Values.openmetadata.config.secretsManager.additionalParameters.region }}"
{{- with .Values.openmetadata.config.secretsManager.additionalParameters.accessKeyId }}
- name: OM_SM_ACCESS_KEY_ID
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
{{- with .Values.openmetadata.config.secretsManager.additionalParameters.secretAccessKey }}
- name: OM_SM_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
{{- end }}
{{- if .Values.openmetadata.config.smtpConfig.enableSmtpServer }}
- name: OM_EMAIL_ENTITY
  value: "{{ .Values.openmetadata.config.smtpConfig.emailingEntity }}"
- name: OM_SUPPORT_URL
  value: "{{ .Values.openmetadata.config.smtpConfig.supportUrl }}"
- name: AUTHORIZER_ENABLE_SMTP
  value: "{{ .Values.openmetadata.config.smtpConfig.enableSmtpServer }}"
- name: OPENMETADATA_SERVER_URL
  value: "{{ .Values.openmetadata.config.smtpConfig.openMetadataUrl }}"
- name: SMTP_SERVER_ENDPOINT
  value: "{{ .Values.openmetadata.config.smtpConfig.serverEndpoint }}"
- name: SMTP_SERVER_PORT
  value: "{{ .Values.openmetadata.config.smtpConfig.serverPort }}"
- name: SMTP_SERVER_USERNAME
  value: "{{ .Values.openmetadata.config.smtpConfig.username }}"
{{- with .Values.openmetadata.config.smtpConfig.password }}
- name: SMTP_SERVER_PWD
  valueFrom:
    secretKeyRef:
      name: {{ .secretRef }}
      key: {{ .secretKey }}
{{- end }}
- name: SMTP_SERVER_STRATEGY
  value: "{{ .Values.openmetadata.config.smtpConfig.transportationStrategy }}"
- name: OPENMETADATA_SMTP_SENDER_MAIL
  value: "{{ .Values.openmetadata.config.smtpConfig.senderMail }}"
{{- end }}
- name: WEB_CONF_URI_PATH
  value: "{{ .Values.openmetadata.config.web.uriPath }}"
- name: WEB_CONF_HSTS_ENABLED
  value: "{{ .Values.openmetadata.config.web.hsts.enabled }}"
- name: WEB_CONF_HSTS_MAX_AGE
  value: "{{ .Values.openmetadata.config.web.hsts.maxAge }}"
- name: WEB_CONF_HSTS_INCLUDE_SUBDOMAINS
  value: "{{ .Values.openmetadata.config.web.hsts.includeSubDomains }}"
- name: WEB_CONF_HSTS_PRELOAD
  value: "{{ .Values.openmetadata.config.web.hsts.preload }}"
- name: WEB_CONF_FRAME_OPTION_ENABLED
  value: "{{ .Values.openmetadata.config.web.frameOptions.enabled }}"
- name: WEB_CONF_FRAME_OPTION
  value: "{{ .Values.openmetadata.config.web.frameOptions.option }}"
- name: WEB_CONF_FRAME_ORIGIN
  value: "{{ .Values.openmetadata.config.web.frameOptions.origin }}"
- name: WEB_CONF_CONTENT_TYPE_OPTIONS_ENABLED
  value: "{{ .Values.openmetadata.config.web.contentTypeOptions.enabled }}"
- name: WEB_CONF_XSS_PROTECTION_ENABLED
  value: "{{ .Values.openmetadata.config.web.xssProtection.enabled }}"
- name: WEB_CONF_XSS_PROTECTION_ON
  value: "{{ .Values.openmetadata.config.web.xssProtection.onXss }}"
- name: WEB_CONF_XSS_PROTECTION_BLOCK
  value: "{{ .Values.openmetadata.config.web.xssProtection.block }}"
- name: WEB_CONF_XSS_CSP_ENABLED
  value: "{{ .Values.openmetadata.config.web.csp.enabled }}"
- name: WEB_CONF_XSS_CSP_POLICY
  value: "{{ .Values.openmetadata.config.web.csp.policy }}"
- name: WEB_CONF_XSS_CSP_REPORT_ONLY_POLICY
  value: "{{ .Values.openmetadata.config.web.csp.reportOnlyPolicy }}"
- name: WEB_CONF_REFERRER_POLICY_ENABLED
  value: "{{ .Values.openmetadata.config.web.referrerPolicy.enabled }}"
- name: WEB_CONF_REFERRER_POLICY_OPTION
  value: "{{ .Values.openmetadata.config.web.referrerPolicy.option }}"
- name: WEB_CONF_PERMISSION_POLICY_ENABLED
  value: "{{ .Values.openmetadata.config.web.permissionPolicy.enabled }}"
- name: WEB_CONF_PERMISSION_POLICY_OPTION
  value: "{{ .Values.openmetadata.config.web.permissionPolicy.option }}"

{{- end }}
