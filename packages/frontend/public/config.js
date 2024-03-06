window['INJECTED_ENVIRONMENT'] = {
  OIDC_DISABLED: true,
  API_BASEURL: 'http://localhost:7171/api',
  APP_URL:'https://localhost:9091/',
  LOGINREDIRECTURL: 'http://localhost:7171/login/redirect',
  DATA_PIPELINES_API_BASEURL: 'http://localhost:7172/airflow/api',
  MALWARESCAN_API_BASEURL: 'http://localhost:8181/api',
  DASHBOARD_API_BASEURL: 'http://localhost:7173/api',
  NOTIFICATIONS_API_BASEURL: 'http://localhost:7272/api',
  DNA_COMPANY_NAME: 'Company_Name',
  DNA_APPNAME_HEADER: 'DnA App',
  DNA_APPNAME_HOME: 'Data and Analytics',
  DNA_CONTACTUS_HTML:
    '<div><p>There could be many places where you may need our help, and we are happy to support you. <br /> Please add your communication channels links here</p></div>',
  DNA_BRAND_LOGO_URL: '/images/branding/logo-brand.png',
  DNA_APP_LOGO_URL: '/images/branding/logo-app.png',
  OIDC_PROVIDER: 'OKTA',
  CLIENT_IDS: 'YOUR_CLIENT_ID',
  REDIRECT_URLS: 'YOUR_OKTA_REDIRECT_URL',
  OAUTH2_AUTH_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/authorize',
  OAUTH2_LOGOUT_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/logout',
  OAUTH2_REVOKE_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/revoke',
  OAUTH2_TOKEN_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/token',
  ENABLE_INTERNAL_USER_INFO: false,
  ENABLE_DATA_COMPLIANCE: false,
  ENABLE_JUPYTER_WORKSPACE: false,
  JUPYTER_NOTEBOOK_URL: 'YOUR_NOTEBOOK_URL',
  JUPYTER_NOTEBOOK_OIDC_POPUP_URL: 'YOUR_NOTEBOOK_AUTH_REDIRECT_URL',
  JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME: 5000,
  ENABLE_DATAIKU_WORKSPACE: false,
  DSS_MFE_APP_URL: 'http://localhost:8086',
  DATAIKU_LIVE_APP_URL: 'YOUR_DATAIKU_LIVE_URL',
  DATAIKU_LIVE_ON_PREMISE_APP_URL: 'YOUR_DATAIKU_LIVE_ON_PREMISE_APP_URL',
  DATAIKU_TRAINING_APP_URL: 'YOUR_DATAIKU_TRAINING_URL',
  DATAIKU_FERRET_URL: 'YOUR_DATAIKU_FERRET_URL',
  MATOMO_MFE_APP_URL: 'http://localhost:8089',
  DATALAKE_MFE_APP_URL: 'http://localhost:8090',
  ENABLE_MALWARE_SCAN_SERVICE: true,
  MALWARE_SCAN_SWAGGER_UI_URL: 'http://localhost:8181/swagger-ui.html',
  ENABLE_MALWARE_SCAN_ONEAPI_INFO: false,
  ENABLE_DATA_PIPELINE_SERVICE: false,
  DATA_PIPELINES_APP_BASEURL: 'DATA_PIPELINE_AIRFLOW_APP_URL',
  ENABLE_MY_MODEL_REGISTRY_SERVICE: false,
  ENABLE_STORAGE_SERVICE: true,
  STORAGE_MFE_APP_URL: 'http://localhost:8083',
  ENABLE_REPORTS: true,
  ENABLE_TRAININGS: false,
  ENABLE_ML_PIPELINE_SERVICE: false,
  ENABLE_NOTIFICATION: true,
  ML_PIPELINE_URL: 'YOUR_ML_PIPELINE_URL',
  MODEL_REGISTRY_API_BASEURL: 'YOUR_MODEL_REGISTRY_API_BASEURL',
  INTERNAL_USER_TEAMS_INFO:
    '(Recommended to use Short ID. To find Short ID use <a href="YOUR_TEAMS_INFO_URL" target="_blank" rel="noreferrer noopener">Teams</a>)',
  ENABLE_DATA_PRODUCT: true,
  DATA_PRODUCT_MFE_APP_URL: 'http://localhost:8084',
  ENABLE_SAP_ANALYTICS_CLOUD: false,
  SAP_ANALYTICS_CLOUD_URL: 'YOUR_SAP_ANALYTICS_CLOUD_URL',
  ENABLE_APP_FEEDBACK: false,
  APP_FEEDBACK_EXTERNAL_URL: 'YOUR_APP_FEEDBACK_EXTERNAL_URL',
  ENABLE_CODE_SPACE: false,
  CODE_SPACE_API_BASEURL: 'http://localhost:7979/api',
  CODE_SPACE_GIT_PAT_APP_URL: 'YOUR_PRIVATE_GITHUB_URL',
  CODESPACE_OPENSEARCH_LOGS_URL: 'YOUR_CODESPACE_LOGS_VIEW_URL?query=$INSTANCE_ID$',
  CODESPACE_OPENSEARCH_BUILD_LOGS_URL: 'YOUR_CODESPACE_BUILD_LOGS_VIEW_URL?query=$INSTANCE_ID$',
  CODESPACE_OIDC_POPUP_URL: 'YOUR_CODESPACE_OIDC_LOGIN_URL',
  CODESPACE_OIDC_POPUP_WAIT_TIME: 5000,
  VAULT_API_BASEURL: 'YOUR_VAULT_API_BASEURL',
  ENABLE_CHRONOS_FORECASTING_SERVICE: true,
  ENABLE_DATALAKE_SERVICE: true,
  CHRONOS_MFE_APP_URL: 'http://localhost:8085',
  ENABLE_MATOMO_SERVICE: true,
  MATOMO_APP_URL: 'YOUR_MATOMO_APP_URL',
  DATA_GOVERNANCE_INFO_LINK: 'YOUR_DATA_GOVERNANCE_INFO_LINK',
  UDEMY_URL: 'YOUR_UDEMY_URL',
  LINKEDIN_LEARNING_URL: 'YOUR_LINKEDIN_LEARNING_URL',
  DATAIKU_TRAINING_URL: 'YOUR_DATAIKU_TRAINING_URL',
  POWERBI_TRAINING_URL: 'YOUR_POWERBI_TRAINING_URL',
  SAC_TRAINING_URL: 'YOUR_SAC_TRAINING_URL',
  DATABRICKS_TRAINING_URL: 'YOUR_DATABRICKS_TRAINING_URL',
  DIGITAL_CASE_PROGRAM_URL: 'YOUR_DIGITAL_CASE_PROGRAM_URL',
  DATASPHERE_TRAINING_URL: 'YOUR_DATASPHERE_TRAINING_URL',
  CHINA_TRAINING_URL: 'YOUR_CHINA_TRAINING_URL',
  TRANSACTIONAL_DATA_URL: 'YOUR_TRANSACTIONAL_DATA_URL',
  CARLA_ARCHITECTURE_URL: 'YOUR_CARLA_ARCHITECTURE_URL',
  AFO_TOOL_URL: 'YOUR_AFO_TOOL_URL',
  BPT_TOOL_URL: 'YOUR_BPT_TOOL_URL',
  DATA_OASIS_TOOL_URL: 'YOUR_DATA_OASIS_TOOL_URL',
  DATAQ_TOOL_URL: 'YOUR_DATAQ_TOOL_URL',
  DATASPHERE_TOOL_URL: 'YOUR_DATASPHERE_TOOL_URL',
  EXTOLLO_TOOL_URL: 'YOUR_EXTOLLO_TOOL_URL',
  POWER_BI_TOOL_URL: 'YOUR_POWER_BI_TOOL_URL',
  SAC_TOOL_URL: 'YOUR_SAC_TOOL_URL',
  TERMS_OF_USE_CONTENT: 'YOUR_TOU_CONTENT',
  COMING_SOON_CONTENT: '<p>YOUR_COMING_SOON_CONTENT</p>',
  DATA_MODEL_URL: 'YOUR_DATA_MODEL_URL',
  DATA_CATALOG_URL: 'YOUR_DATA_CATALOG_URL',
  CORPORATE_DATA_CATALOG_URL: 'YOUR_CORPORATE_DATA_CATALOG_URL',
  SMART_DATA_GOVERNANCE_URL: 'YOUR_SMART_DATA_GOVERNANCE_URL',
  DATA_PRODUCT_API_BASEURL: 'http://localhost:7184/api',
  ROPA_PROCEDURE_ID_PREFIX: 'YOUR_ROPA_PROCEDURE_ID_PREFIX',
  SPIRE_URL: 'YOUR_SPIRE_URL',
  GENAI_DIRECT_CHAT_URL: 'YOUR_GENAI_DIRECT_CHAT_URL',
  BISO_CONTACTS_URL: 'YOUR_BISO_CONTACTS_URL',
  CLAMAV_IMAGE_URL : 'YOUR_CLAMAV_IMAGE_URL',
  CODESPACE_SECURITY_APP_ID : 'YOUR_CODESPACE_SECURITY_APP_ID '
};

window['STORAGE_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  API_BASEURL: 'http://localhost:7171/api',
  STORAGE_API_BASEURL: 'http://localhost:7175/api',
  TOU_HTML: '<div>I agree to <a href="#" target="_blank" rel="noopener noreferrer">terms of use</a></div>',
  ENABLE_DATA_CLASSIFICATION_SECRET: false,
  TRINO_API_BASEURL: 'http://localhost:7575/api',
  DATAIKU_API_BASEURL: 'http://localhost:7777/api',
  ENABLE_TRINO_PUBLISH: false,
};

window['DATA_PRODUCT_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  DATA_PRODUCT_API_BASEURL: '',
  DATA_GOVERNANCE_HTML_FOR_CHINA_DATA: '<p>Your sample content</p>',
  DATA_PRODUCT_TOU_HTML:
    '<div>On behalf of my aforementioned Information Owner/Business Owner of Application, I confirm that the usage information for data transfer provided is correct and complete.</div>',
  INFORMATION_POLICY_LINK: 'YOUR_INFORMATION_POLICY_LINK',
  DDX_URL: 'YOUR_DDX_URL'
};

window['CHRONOS_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  CHRONOS_API_BASEURL: '',
  ENABLE_CHRONOS_ONEAPI: false,
  CHRONOS_ONEAPI_URL: 'YOUR_CHRONOS_ONEAPI_URL',
  STORAGE_API_BASEURL: 'http://localhost:7175/api',
  REPORTS_API_BASEURL: 'http://localhost:7173/api',
  CHRONOS_RELEASES_INFO_URL: 'YOUR_CHRONOS_RELEASES_INFO_URL',
  ADS_EMAIL: 'YOUR_ADS_EMAIL',
  CHRONOS_DOCUMENTATION_URL: 'YOUR_CHRONOS_DOCUMENTATION_URL',
  CHRONOS_SWAGGER_URL:'YOUR_CHRONOS_SWAGGER_DOCUMENTATION_URL',
  TOU_HTML: '<div>I agree to <a href="#" target="_blank" rel="noopener noreferrer">terms of use</a></div>',
};

window['DSS_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  DATAIKU_API_BASEURL: '',
  DATAIKU_LIVE_APP_URL: 'YOUR_DATAIKU_LIVE_URL',
  DATAIKU_LIVE_ON_PREMISE_APP_URL: 'YOUR_DATAIKU_LIVE_ON_PREMISE_APP_URL',
  DATAIKU_TRAINING_APP_URL: 'YOUR_DATAIKU_TRAINING_URL',
  DATAIKU_FERRET_URL: 'YOUR_DATAIKU_FERRET_URL',
  STORAGE_API_BASEURL: '',
  REPORTS_API_BASEURL: '',
  DATAIKU_LICENSE_CREATION_CONTENT: 'YOUR_DATAIKU_LICENSE_CREATION_CONTENT'
};

window['MATOMO_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'YOUR_CONTAINER_APP_URL',
  API_BASEURL: 'YOUR_API_BASEURL',
  MATOMO_API_BASEURL: 'YOUR_MATOMO_API_BASEURL',
  STORAGE_API_BASEURL: 'YOUR_STORAGE_API_BASEURL',
  REPORTS_API_BASEURL: 'YOUR_REPORTS_API_BASEURL',
  MATOMO_APP_URL: 'YOUR_MATOMO_APP_URL'
};

window["DATALAKE_INJECTED_ENVIRONMENT"]={
  CONTAINER_APP_URL:'YOUR_CONTAINER_APP_URL',
  DATALAKE_API_BASEURL: 'YOUR_DATALAKE_API_BASEURL',
  REPORTS_API_BASEURL: 'YOUR_REPORTS_API_BASEURL',
  STORAGE_API_BASEURL: 'YOUR_STORAGE_API_BASEURL',
  ENABLE_PROVISION_AND_UPLOAD : false,
}; 