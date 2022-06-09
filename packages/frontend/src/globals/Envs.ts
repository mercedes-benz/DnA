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
  DATA_PIPELINES_API_BASEURL: getInjectedEnv('DATA_PIPELINES_API_BASEURL') || process.env.DATA_PIPELINES_API_BASEURL,
  DATA_PIPELINES_APP_BASEURL: getInjectedEnv('DATA_PIPELINES_APP_BASEURL') || process.env.DATA_PIPELINES_APP_BASEURL,
  NOTIFICATIONS_API_BASEURL: getInjectedEnv('NOTIFICATIONS_API_BASEURL') || process.env.NOTIFICATIONS_API_BASEURL,
  DASHBOARD_API_BASEURL: getInjectedEnv('DASHBOARD_API_BASEURL') || process.env.DASHBOARD_API_BASEURL,
  NOTEBOOK_API_BASEURL: getInjectedEnv('NOTEBOOK_API_BASEURL') || process.env.NOTEBOOK_API_BASEURL,
  STORAGE_API_BASEURL: getStorageInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
  MALWARESCAN_API_BASEURL: getInjectedEnv('MALWARESCAN_API_BASEURL') || process.env.MALWARESCAN_API_BASEURL,
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
      : EnvParser.parseBool(process.env.ENABLE_INTERNAL_USER_INFO, false),
  ENABLE_DATA_COMPLIANCE:
    getInjectedEnv('ENABLE_DATA_COMPLIANCE') !== undefined
      ? getInjectedEnv('ENABLE_DATA_COMPLIANCE')
      : EnvParser.parseBool(process.env.ENABLE_DATA_COMPLIANCE, false),
  ENABLE_REPORTS:
    getInjectedEnv('ENABLE_REPORTS') !== undefined
      ? getInjectedEnv('ENABLE_REPORTS')
      : EnvParser.parseBool(process.env.ENABLE_REPORTS, false),
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
  ENABLE_ML_PIPELINE_SERVICE:
    getInjectedEnv('ENABLE_ML_PIPELINE_SERVICE') !== undefined
      ? getInjectedEnv('ENABLE_ML_PIPELINE_SERVICE')
      : EnvParser.parseBool(process.env.ENABLE_ML_PIPELINE_SERVICE, false),
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
  INTERNAL_USER_TEAMS_INFO:
    getInjectedEnv('INTERNAL_USER_TEAMS_INFO') !== undefined
      ? getInjectedEnv('INTERNAL_USER_TEAMS_INFO') 
      : EnvParser.parseBool(process.env.INTERNAL_USER_TEAMS_INFO, false),
};
