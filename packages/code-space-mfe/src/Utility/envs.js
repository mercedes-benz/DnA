const getInjectedEnv = (key) => {
    if (window.CODESPACE_INJECTED_ENVIRONMENT) {
      return window.CODESPACE_INJECTED_ENVIRONMENT[key];
    }
    return undefined;
  };
  
  const getDNAInjectedEnv = (key) => {
    if (window.INJECTED_ENVIRONMENT) {
      return window.INJECTED_ENVIRONMENT[key];
    }
    return undefined;
  };
  
  export const Envs = {
    CODE_SPACE_API_BASEURL: getInjectedEnv('CODE_SPACE_API_BASEURL') || process.env.CODE_SPACE_API_BASEURL,
    API_BASEURL: getInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
    CODE_SPACE_GIT_PAT_APP_URL: getInjectedEnv('CODE_SPACE_GIT_PAT_APP_URL') || process.env.CODE_SPACE_GIT_PAT_APP_URL,
    CODE_SPACE_GIT_ORG_NAME: getDNAInjectedEnv('CODE_SPACE_GIT_ORG_NAME') || process.env.CODE_SPACE_GIT_ORG_NAME,
    CODESPACE_OPENSEARCH_LOGS_URL: getDNAInjectedEnv('CODESPACE_OPENSEARCH_LOGS_URL') || process.env.CODESPACE_OPENSEARCH_LOGS_URL,
    CODESPACE_OPENSEARCH_BUILD_LOGS_URL: getDNAInjectedEnv('CODESPACE_OPENSEARCH_BUILD_LOGS_URL') || process.env.CODESPACE_OPENSEARCH_BUILD_LOGS_URL,
    CODESPACE_OIDC_POPUP_URL: getInjectedEnv('CODESPACE_OIDC_POPUP_URL') || process.env.CODESPACE_OIDC_POPUP_URL,
    CODESPACE_OIDC_POPUP_WAIT_TIME: getInjectedEnv('CODESPACE_OIDC_POPUP_WAIT_TIME') || process.env.CODESPACE_OIDC_POPUP_WAIT_TIME,
    REPORTS_API_BASEURL: getDNAInjectedEnv('REPORTS_API_BASEURL') || process.env.REPORTS_API_BASEURL,
    VAULT_API_BASEURL: getDNAInjectedEnv('VAULT_API_BASEURL') || process.env.VAULT_API_BASEURL,
    
  };