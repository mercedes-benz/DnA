window['INJECTED_ENVIRONMENT'] = {
  OIDC_DISABLED: true,
  API_BASEURL: 'http://localhost:7171/api',
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
  DATAIKU_LIVE_APP_URL: 'YOUR_DATAIKU_LIVE_URL',
  DATAIKU_TRAINING_APP_URL: 'YOUR_DATAIKU_TRAINING_URL',
  DATAIKU_FERRET_URL: 'YOUR_DATAIKU_FERRET_URL',
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
  CODE_SPACE_GIT_PAT_APP_URL: 'YOUR_GIT_APP_URL_WERE_REPO_CREATED',
  ENABLE_CHRONOS_FORECASTING_SERVICE: true,
  CHRONOS_MFE_APP_URL: 'http://localhost:8085',
};

window['STORAGE_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  API_BASEURL: 'http://localhost:7171/api',
  STORAGE_API_BASEURL: 'http://localhost:7175/api',
  TOU_HTML: '<div>I agree to <a href="#" target="_blank" rel="noopener noreferrer">terms of use</a></div>',
  ENABLE_DATA_CLASSIFICATION_SECRET: false,
  TRINO_API_BASEURL: 'http://localhost:7575/api',
  ENABLE_TRINO_PUBLISH: false,
};

window['DATA_PRODUCT_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  DATA_PRODUCT_API_BASEURL: '',
  DATA_GOVERNANCE_HTML_FOR_CHINA_DATA:
    '<p>If yes, and due to the complexity of corresponding Data Governance requirements, we recommend you reach out to us at the <a href="#" target="_blank" rel="noopener noreferrer"> Data Governance Office FM </a> directly.</p>',
  DATA_PRODUCT_TOU_HTML:
    '<div>On behalf of my above mentioned Business Owner, I confirm the corresponding minimum information to be correct and complete to the best of my knowledge. I accept the conditions.</div>',
  INFORMATION_POLICY_LINK: 'YOUR_INFORMATION_POLICY_LINK',
};

window['CHRONOS_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  CHRONOS_API_BASEURL: '',
  ENABLE_CHRONOS_ONEAPI: false,
  CHRONOS_ONEAPI_URL: 'YOUR_CHRONOS_ONEAPI_URL',
};
