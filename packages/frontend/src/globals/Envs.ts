class EnvParser {
  public static parseBool(env: string | undefined, defaultValue: boolean): boolean {
    if (!env) {
      return defaultValue;
    }

    return env.toLowerCase() === 'true';
  }
}

declare global {
  interface Window {
    INJECTED_ENVIRONMENT?: any;
    NOTIFICATION_POLL_ID?: any;
    STORAGE_INJECTED_ENVIRONMENT?: any;
  }
}

const getInjectedEnv = (key: string) => {
  if (window.INJECTED_ENVIRONMENT) {
    return window.INJECTED_ENVIRONMENT[key];
  }
  return undefined;
};

const getStorageInjectedEnv = (key: string) => {
  if (window.STORAGE_INJECTED_ENVIRONMENT) {
    return window.STORAGE_INJECTED_ENVIRONMENT[key];
  }
  return undefined;
}

// You have to go via this or directly use the process.env
// BUT using in direct statements like === will result in direct expansion in builds this means the variable is lost
// we have to make sure that the string value of process.env is placed here.
export const Envs = {
  OIDC_DISABLED:
    getInjectedEnv('OIDC_DISABLED') !== undefined
      ? getInjectedEnv('OIDC_DISABLED')
      : EnvParser.parseBool(process.env.OIDC_DISABLED, true),

  API_BASEURL: getInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  APP_URL: getInjectedEnv('APP_URL') || process.env.APP_URL,
  LOGINREDIRECTURL: getInjectedEnv('LOGINREDIRECTURL') || process.env.LOGINREDIRECTURL,
  DATAIKU_API_BASEURL: getInjectedEnv('DATAIKU_API_BASEURL') || process.env.DATAIKU_API_BASEURL,
  DATA_PIPELINES_API_BASEURL: getInjectedEnv('DATA_PIPELINES_API_BASEURL') || process.env.DATA_PIPELINES_API_BASEURL,
  DATA_PIPELINES_APP_BASEURL: getInjectedEnv('DATA_PIPELINES_APP_BASEURL') || process.env.DATA_PIPELINES_APP_BASEURL,
  NOTIFICATIONS_API_BASEURL: getInjectedEnv('NOTIFICATIONS_API_BASEURL') || process.env.NOTIFICATIONS_API_BASEURL,
  DASHBOARD_API_BASEURL: getInjectedEnv('DASHBOARD_API_BASEURL') || process.env.DASHBOARD_API_BASEURL,
  NOTEBOOK_API_BASEURL: getInjectedEnv('NOTEBOOK_API_BASEURL') || process.env.NOTEBOOK_API_BASEURL,
  STORAGE_API_BASEURL: getStorageInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
  MALWARESCAN_API_BASEURL: getInjectedEnv('MALWARESCAN_API_BASEURL') || process.env.MALWARESCAN_API_BASEURL,
  MODEL_REGISTRY_API_BASEURL: getInjectedEnv('MODEL_REGISTRY_API_BASEURL') || process.env.MODEL_REGISTRY_API_BASEURL,
  NODE_ENV: getInjectedEnv('NODE_ENV') || process.env.NODE_ENV,
  CLIENT_IDS: getInjectedEnv('CLIENT_IDS') || process.env.CLIENT_IDS,
  OIDC_PROVIDER: getInjectedEnv('OIDC_PROVIDER') || process.env.OIDC_PROVIDER || 'INTERNAL',
  REDIRECT_URLS: getInjectedEnv('REDIRECT_URLS') || process.env.REDIRECT_URLS,
  OAUTH2_AUTH_URL: getInjectedEnv('OAUTH2_AUTH_URL') || process.env.OAUTH2_AUTH_URL,
  OAUTH2_LOGOUT_URL: getInjectedEnv('OAUTH2_LOGOUT_URL') || process.env.OAUTH2_LOGOUT_URL,
  OAUTH2_REVOKE_URL: getInjectedEnv('OAUTH2_REVOKE_URL') || process.env.OAUTH2_REVOKE_URL,
  OAUTH2_TOKEN_URL: getInjectedEnv('OAUTH2_TOKEN_URL') || process.env.OAUTH2_TOKEN_URL,
  DNA_COMPANY_NAME: getInjectedEnv('DNA_COMPANY_NAME') || process.env.DNA_COMPANY_NAME,
  DNA_APPNAME_HEADER: getInjectedEnv('DNA_APPNAME_HEADER') || process.env.DNA_APPNAME_HEADER,
  DNA_APPNAME_HOME: getInjectedEnv('DNA_APPNAME_HOME') || process.env.DNA_APPNAME_HOME,
  DNA_CONTACTUS_HTML: getInjectedEnv('DNA_CONTACTUS_HTML') || process.env.DNA_CONTACTUS_HTML,
  DNA_BRAND_LOGO_URL: getInjectedEnv('DNA_BRAND_LOGO_URL') || process.env.DNA_BRAND_LOGO_URL,
  DNA_APP_LOGO_URL: getInjectedEnv('DNA_APP_LOGO_URL') || process.env.DNA_APP_LOGO_URL,
    ENABLE_INTERNAL_USER_INFO:
    getInjectedEnv('ENABLE_INTERNAL_USER_INFO') !== undefined
      ? getInjectedEnv('ENABLE_INTERNAL_USER_INFO')
      : EnvParser.parseBool(process.env.ENABLE_INTERNAL_USER_INFO, true),
  ENABLE_DATA_COMPLIANCE:
    getInjectedEnv('ENABLE_DATA_COMPLIANCE') !== undefined
      ? getInjectedEnv('ENABLE_DATA_COMPLIANCE')
      : EnvParser.parseBool(process.env.ENABLE_DATA_COMPLIANCE, false),
  ENABLE_REPORTS:
    getInjectedEnv('ENABLE_REPORTS') !== undefined
      ? getInjectedEnv('ENABLE_REPORTS')
      : EnvParser.parseBool(process.env.ENABLE_REPORTS, false),
  ENABLE_TRAININGS:
    getInjectedEnv('ENABLE_TRAININGS') !== undefined
      ? getInjectedEnv('ENABLE_TRAININGS')
      : EnvParser.parseBool(process.env.ENABLE_TRAININGS, false),
  ENABLE_JUPYTER_WORKSPACE:
    getInjectedEnv('ENABLE_JUPYTER_WORKSPACE') !== undefined
      ? getInjectedEnv('ENABLE_JUPYTER_WORKSPACE')
      : EnvParser.parseBool(process.env.ENABLE_JUPYTER_WORKSPACE, false),
  JUPYTER_NOTEBOOK_URL: getInjectedEnv('JUPYTER_NOTEBOOK_URL') || process.env.JUPYTER_NOTEBOOK_URL,
  JUPYTER_NOTEBOOK_OIDC_POPUP_URL:
    getInjectedEnv('JUPYTER_NOTEBOOK_OIDC_POPUP_URL') || process.env.JUPYTER_NOTEBOOK_OIDC_POPUP_URL,
  JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME:
    getInjectedEnv('JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME') ||
    parseInt(process.env.JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME || '5000', 10),
  ENABLE_DATAIKU_WORKSPACE:
    getInjectedEnv('ENABLE_DATAIKU_WORKSPACE') !== undefined
      ? getInjectedEnv('ENABLE_DATAIKU_WORKSPACE')
      : EnvParser.parseBool(process.env.ENABLE_DATAIKU_WORKSPACE, false),
  DATAIKU_LIVE_APP_URL: getInjectedEnv('DATAIKU_LIVE_APP_URL') || process.env.DATAIKU_LIVE_APP_URL,
  DATAIKU_LIVE_ON_PREMISE_APP_URL: getInjectedEnv('DATAIKU_LIVE_ON_PREMISE_APP_URL') || process.env.DATAIKU_LIVE_ON_PREMISE_APP_URL,
  DATAIKU_TRAINING_APP_URL: getInjectedEnv('DATAIKU_TRAINING_APP_URL') || process.env.DATAIKU_TRAINING_APP_URL,
  DATAIKU_FERRET_URL: getInjectedEnv('DATAIKU_FERRET_URL') || process.env.DATAIKU_FERRET_URL,
  ENABLE_MALWARE_SCAN_SERVICE:
    getInjectedEnv('ENABLE_MALWARE_SCAN_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_MALWARE_SCAN_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_MALWARE_SCAN_SERVICE, false),
  MALWARE_SCAN_SWAGGER_UI_URL: getInjectedEnv('MALWARE_SCAN_SWAGGER_UI_URL') || process.env.MALWARE_SCAN_SWAGGER_UI_URL,
  ENABLE_DATA_PIPELINE_SERVICE:
    getInjectedEnv('ENABLE_DATA_PIPELINE_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_DATA_PIPELINE_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_DATA_PIPELINE_SERVICE, false),
  ENABLE_STORAGE_SERVICE:
    getInjectedEnv('ENABLE_STORAGE_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_STORAGE_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_STORAGE_SERVICE, false),
  ALICE_BASE_URL:
    getInjectedEnv('ALICE_BASE_URL') !== undefined
      ? getInjectedEnv('ALICE_BASE_URL')
      : EnvParser.parseBool(process.env.ALICE_BASE_URL, false),
  FABRIC_API_BASEURL: getInjectedEnv('FABRIC_API_BASEURL') || process.env.FABRIC_API_BASEURL,
  ML_PIPELINE_URL: getInjectedEnv('ML_PIPELINE_URL') || process.env.ML_PIPELINE_URL,
  ENABLE_MALWARE_SCAN_ONEAPI:
    getInjectedEnv('ENABLE_MALWARE_SCAN_ONEAPI') !== undefined
      ? getInjectedEnv('ENABLE_MALWARE_SCAN_ONEAPI')
      : EnvParser.parseBool(process.env.ENABLE_MALWARE_SCAN_ONEAPI, false),
  ENABLE_MALWARE_SCAN_ONEAPI_INFO:
    getInjectedEnv('ENABLE_MALWARE_SCAN_ONEAPI_INFO') !== undefined
      ? getInjectedEnv('ENABLE_MALWARE_SCAN_ONEAPI_INFO')
      : EnvParser.parseBool(process.env.ENABLE_MALWARE_SCAN_ONEAPI_INFO, false),
  ENABLE_NOTIFICATION:
    getInjectedEnv('ENABLE_NOTIFICATION') !== undefined
      ? getInjectedEnv('ENABLE_NOTIFICATION')
      : EnvParser.parseBool(process.env.ENABLE_NOTIFICATION, false),
  STORAGE_TOU_HTML: getStorageInjectedEnv('TOU_HTML') || process.env.TOU_HTML,
  INTERNAL_USER_TEAMS_INFO: getInjectedEnv('INTERNAL_USER_TEAMS_INFO') || process.env.INTERNAL_USER_TEAMS_INFO,
  ENABLE_DATA_PRODUCT:
    getInjectedEnv('ENABLE_DATA_PRODUCT') !== undefined
      ? getInjectedEnv('ENABLE_DATA_PRODUCT')
      : EnvParser.parseBool(process.env.ENABLE_DATA_PRODUCT, false),
  ENABLE_SAP_ANALYTICS_CLOUD:
    getInjectedEnv('ENABLE_SAP_ANALYTICS_CLOUD') !== undefined
      ? getInjectedEnv('ENABLE_SAP_ANALYTICS_CLOUD')
      : EnvParser.parseBool(process.env.ENABLE_SAP_ANALYTICS_CLOUD, false),
  SAP_ANALYTICS_CLOUD_URL: getInjectedEnv('SAP_ANALYTICS_CLOUD_URL') || process.env.SAP_ANALYTICS_CLOUD_URL,
  ENABLE_APP_FEEDBACK:
    getInjectedEnv('ENABLE_APP_FEEDBACK') !== undefined
      ? getInjectedEnv('ENABLE_APP_FEEDBACK')
      : EnvParser.parseBool(process.env.ENABLE_APP_FEEDBACK, false),
  APP_FEEDBACK_EXTERNAL_URL: getInjectedEnv('APP_FEEDBACK_EXTERNAL_URL') || process.env.APP_FEEDBACK_EXTERNAL_URL,
  ENABLE_CODE_SPACE:
    getInjectedEnv('ENABLE_CODE_SPACE') !== undefined
      ? getInjectedEnv('ENABLE_CODE_SPACE')
      : EnvParser.parseBool(process.env.ENABLE_CODE_SPACE, false),
  CODE_SPACE_API_BASEURL: getInjectedEnv('CODE_SPACE_API_BASEURL') || process.env.CODE_SPACE_API_BASEURL,
  CODE_SPACE_GIT_PAT_APP_URL: getInjectedEnv('CODE_SPACE_GIT_PAT_APP_URL') || process.env.CODE_SPACE_GIT_PAT_APP_URL,
  CODE_SPACE_GIT_ORG_NAME: getInjectedEnv('CODE_SPACE_GIT_ORG_NAME') || process.env.CODE_SPACE_GIT_ORG_NAME,
  CODESPACE_OPENSEARCH_LOGS_URL: getInjectedEnv('CODESPACE_OPENSEARCH_LOGS_URL') || process.env.CODESPACE_OPENSEARCH_LOGS_URL,
  CODESPACE_OPENSEARCH_BUILD_LOGS_URL: getInjectedEnv('CODESPACE_OPENSEARCH_BUILD_LOGS_URL') || process.env.CODESPACE_OPENSEARCH_BUILD_LOGS_URL,
  CODESPACE_OIDC_POPUP_URL: getInjectedEnv('CODESPACE_OIDC_POPUP_URL') || process.env.CODESPACE_OIDC_POPUP_URL,
  CODESPACE_OIDC_POPUP_WAIT_TIME:
    getInjectedEnv('CODESPACE_OIDC_POPUP_WAIT_TIME') ||
    parseInt(process.env.CODESPACE_OIDC_POPUP_WAIT_TIME || '5000', 10),
  CODESPACE_HARDWARE_REQUEST_TEMPLATE: getInjectedEnv('CODESPACE_HARDWARE_REQUEST_TEMPLATE') || process.env.CODESPACE_HARDWARE_REQUEST_TEMPLATE,
  CODESPACE_SOFTWARE_REQUEST_TEMPLATE:getInjectedEnv('CODESPACE_SOFTWARE_REQUEST_TEMPLATE') || process.env.CODESPACE_SOFTWARE_REQUEST_TEMPLATE,
  VAULT_API_BASEURL: getInjectedEnv('VAULT_API_BASEURL') || process.env.VAULT_API_BASEURL,
  SIMILARITY_SEARCH_API_BASEURL: getInjectedEnv('SIMILARITY_SEARCH_API_BASEURL') || process.env.SIMILARITY_SEARCH_API_BASEURL,
  ENABLE_CHRONOS_FORECASTING_SERVICE:
    getInjectedEnv('ENABLE_CHRONOS_FORECASTING_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_CHRONOS_FORECASTING_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_CHRONOS_FORECASTING_SERVICE, false),
  ENABLE_DATALAKE_SERVICE:
    getInjectedEnv('ENABLE_DATALAKE_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_DATALAKE_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_DATALAKE_SERVICE, false),
  ENABLE_FABRIC_SERVICE:
    getInjectedEnv('ENABLE_FABRIC_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_FABRIC_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_FABRIC_SERVICE, false),
  ENABLE_DATA_ENTRY_SERVICE:
    getInjectedEnv('ENABLE_DATA_ENTRY_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_DATA_ENTRY_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_DATA_ENTRY_SERVICE, false),
  ENABLE_POWER_PLATFORM_SERVICE:
    getInjectedEnv('ENABLE_POWER_PLATFORM_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_POWER_PLATFORM_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_POWER_PLATFORM_SERVICE, false),
  ENABLE_PROMPT_CRAFT_SERVICE:
    getInjectedEnv('ENABLE_PROMPT_CRAFT_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_PROMPT_CRAFT_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_PROMPT_CRAFT_SERVICE, false),
  ENABLE_MATOMO_SERVICE:
    getInjectedEnv('ENABLE_MATOMO_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_MATOMO_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_MATOMO_SERVICE, false),
  

  DATA_GOVERNANCE_INFO_LINK: getInjectedEnv('DATA_GOVERNANCE_INFO_LINK') || process.env.DATA_GOVERNANCE_INFO_LINK,
  BISO_CONTACTS_URL: getInjectedEnv('BISO_CONTACTS_URL') || process.env.BISO_CONTACTS_URL,
  UDEMY_URL: getInjectedEnv('UDEMY_URL') || process.env.UDEMY_URL,
  LINKEDIN_LEARNING_URL: getInjectedEnv('LINKEDIN_LEARNING_URL') || process.env.LINKEDIN_LEARNING_URL,
  DATAIKU_TRAINING_URL: getInjectedEnv('DATAIKU_TRAINING_URL') || process.env.DATAIKU_TRAINING_URL,
  POWERBI_TRAINING_URL: getInjectedEnv('POWERBI_TRAINING_URL') || process.env.POWERBI_TRAINING_URL,
  SAC_TRAINING_URL: getInjectedEnv('SAC_TRAINING_URL') || process.env.SAC_TRAINING_URL,
  DATABRICKS_TRAINING_URL: getInjectedEnv('DATABRICKS_TRAINING_URL') || process.env.DATABRICKS_TRAINING_URL,
  FABRIC_TRAINING_URL: getInjectedEnv('FABRIC_TRAINING_URL') || process.env.FABRIC_TRAINING_URL,
  FORMBRICKS_SURVEY_URL: getInjectedEnv('FORMBRICKS_SURVEY_URL') || process.env.FORMBRICKS_SURVEY_URL,
  PGADMIN_URL: getInjectedEnv('PGADMIN_URL') || process.env.PGADMIN_URL,
  DATASPHERE_TRAINING_URL: getInjectedEnv('DATASPHERE_TRAINING_URL') || process.env.DATASPHERE_TRAINING_URL,
  CHINA_TRAINING_URL: getInjectedEnv('CHINA_TRAINING_URL') || process.env.CHINA_TRAINING_URL,
  TRANSACTIONAL_DATA_URL: getInjectedEnv('TRANSACTIONAL_DATA_URL') || process.env.TRANSACTIONAL_DATA_URL,
  CARLA_ARCHITECTURE_URL: getInjectedEnv('CARLA_ARCHITECTURE_URL') || process.env.CARLA_ARCHITECTURE_URL,
  AFO_TOOL_URL: getInjectedEnv('AFO_TOOL_URL') || process.env.AFO_TOOL_URL,
  BPT_TOOL_URL: getInjectedEnv('BPT_TOOL_URL') || process.env.BPT_TOOL_URL,
  DATA_OASIS_TOOL_URL: getInjectedEnv('DATA_OASIS_TOOL_URL') || process.env.DATA_OASIS_TOOL_URL,
  DATAQ_TOOL_URL: getInjectedEnv('DATAQ_TOOL_URL') || process.env.DATAQ_TOOL_URL,
  DATASPHERE_TOOL_URL: getInjectedEnv('DATASPHERE_TOOL_URL') || process.env.DATASPHERE_TOOL_URL,
  EXTOLLO_TOOL_URL: getInjectedEnv('EXTOLLO_TOOL_URL') || process.env.EXTOLLO_TOOL_URL,
  POWER_BI_TOOL_URL: getInjectedEnv('POWER_BI_TOOL_URL') || process.env.POWER_BI_TOOL_URL,
  SAC_TOOL_URL: getInjectedEnv('SAC_TOOL_URL') || process.env.SAC_TOOL_URL,

  POWER_PLATFORM_DEFAULT_ENVIRONMENT_URL: getInjectedEnv('POWER_PLATFORM_DEFAULT_ENVIRONMENT_URL') || process.env.POWER_PLATFORM_DEFAULT_ENVIRONMENT_URL,
  POWER_PLATFORM_CITIZEN_DEVELOPER_ACCOUNT_URL: getInjectedEnv('POWER_PLATFORM_CITIZEN_DEVELOPER_ACCOUNT_URL') || process.env.POWER_PLATFORM_CITIZEN_DEVELOPER_ACCOUNT_URL,
  POWER_PLATFORM_FULL_DEVELOPER_ACCOUNT_URL: getInjectedEnv('POWER_PLATFORM_FULL_DEVELOPER_ACCOUNT_URL') || process.env.POWER_PLATFORM_FULL_DEVELOPER_ACCOUNT_URL,
  POWER_PLATFORM_SUPPORT_EMAIL: getInjectedEnv('POWER_PLATFORM_SUPPORT_EMAIL') || process.env.POWER_PLATFORM_SUPPORT_EMAIL,
  POWER_PLATFORM_SOCIAL_INTRANET_URL: getInjectedEnv('POWER_PLATFORM_SOCIAL_INTRANET_URL') || process.env.POWER_PLATFORM_SOCIAL_INTRANET_URL,
  POWER_PLATFORM_TRAINING_URL: getInjectedEnv('POWER_PLATFORM_TRAINING_URL') || process.env.POWER_PLATFORM_TRAINING_URL,

  ALICE_APP_ID:  getInjectedEnv('ALICE_APP_ID') || process.env.ALICE_APP_ID,
  
  TERMS_OF_USE_CONTENT: getInjectedEnv('TERMS_OF_USE_CONTENT') || process.env.TERMS_OF_USE_CONTENT,
  COMING_SOON_CONTENT: getInjectedEnv('COMING_SOON_CONTENT') || process.env.COMING_SOON_CONTENT,
  DATA_MODEL_URL: getInjectedEnv('DATA_MODEL_URL') || process.env.DATA_MODEL_URL,
  DATA_CATALOG_URL: getInjectedEnv('DATA_CATALOG_URL') || process.env.DATA_CATALOG_URL,
  CORPORATE_DATA_CATALOG_URL: getInjectedEnv('CORPORATE_DATA_CATALOG_URL') || process.env.CORPORATE_DATA_CATALOG_URL,
  SMART_DATA_GOVERNANCE_URL: getInjectedEnv('SMART_DATA_GOVERNANCE_URL') || process.env.SMART_DATA_GOVERNANCE_URL,
  DATA_PRODUCT_API_BASEURL: getInjectedEnv('DATA_PRODUCT_API_BASEURL') || process.env.DATA_PRODUCT_API_BASEURL,
  ROPA_PROCEDURE_ID_PREFIX: getInjectedEnv('ROPA_PROCEDURE_ID_PREFIX') || process.env.ROPA_PROCEDURE_ID_PREFIX,
  CHRONOS_API_BASEURL: getInjectedEnv('CHRONOS_API_BASEURL') || process.env.CHRONOS_API_BASEURL,
  MATOMO_API_BASEURL: getInjectedEnv('MATOMO_API_BASEURL') || process.env.MATOMO_API_BASEURL,
  MATOMO_APP_URL: getInjectedEnv('MATOMO_APP_URL') || process.env.MATOMO_APP_URL,
  SPIRE_URL: getInjectedEnv('SPIRE_URL') || process.env.SPIRE_URL,
  ODIN_URL: getInjectedEnv('ODIN_URL') || process.env.ODIN_URL,
  SASS_URL: getInjectedEnv('SASS_URL') || process.env.SASS_URL,
  GENAI_DIRECT_CHAT_URL: getInjectedEnv('GENAI_DIRECT_CHAT_URL') || process.env.GENAI_DIRECT_CHAT_URL,
  GENAI_LLM_PROMPT_ENG_URL:getInjectedEnv('GENAI_LLM_PROMPT_ENG_URL') || process.env.GENAI_LLM_PROMPT_ENG_URL,
  CLAMAV_IMAGE_URL:  getInjectedEnv('CLAMAV_IMAGE_URL') || process.env.CLAMAV_IMAGE_URL,
  CODESPACE_SECURITY_APP_ID: getInjectedEnv('CODESPACE_SECURITY_APP_ID') || process.env.CODESPACE_SECURITY_APP_ID,

  ONEAPI_SUBSCRIPTION_URL:
    getInjectedEnv('ONEAPI_SUBSCRIPTION_URL') !== undefined
      ? getInjectedEnv('ONEAPI_SUBSCRIPTION_URL')
      : EnvParser.parseBool(process.env.ONEAPI_SUBSCRIPTION_URL, false),
  ONEAPI_PROMPT_CRAFT_USAGE_LINK:
    getInjectedEnv('ONEAPI_PROMPT_CRAFT_USAGE_LINK') !== undefined
      ? getInjectedEnv('ONEAPI_PROMPT_CRAFT_USAGE_LINK')
      : EnvParser.parseBool(process.env.ONEAPI_PROMPT_CRAFT_USAGE_LINK, false),

  ENABLE_DB_SERVICE: getInjectedEnv('ENABLE_DB_SERVICE') || process.env.ENABLE_DB_SERVICE,
  DB_SERVICE_MFE_APP_URL: getInjectedEnv('DB_SERVICE_MFE_APP_URL') || process.env.DB_SERVICE_MFE_APP_URL,
};
