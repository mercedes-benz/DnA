const parseBool = (env, defaultValue) => {
  if (!env) {
    return defaultValue;
  }

  return env.toLowerCase() === 'true';
};

const getInjectedEnv = (key) => {
  if (window.STORAGE_INJECTED_ENVIRONMENT) {
    return window.STORAGE_INJECTED_ENVIRONMENT[key];
  }
  return undefined;
};

const getDNAInjectedEnv = (key) => {
  if (window.INJECTED_ENVIRONMENT) {
    return window.INJECTED_ENVIRONMENT[key];
  }
  return undefined;
};

// You have to go via this or directly use the process.env
// BUT using in direct statements like === will result in direct expansion in builds this means the variable is lost
// we have to make sure that the string value of process.env is placed here.
export const Envs = {
  STORAGE_API_BASEURL: getInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
  API_BASEURL: getInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  REPORTS_API_BASEURL: getInjectedEnv('REPORTS_API_BASEURL') || process.env.REPORTS_API_BASEURL,
  TOU_HTML: getInjectedEnv('TOU_HTML') || process.env.TOU_HTML,
  ENABLE_DATA_CLASSIFICATION_SECRET:
    getInjectedEnv('ENABLE_DATA_CLASSIFICATION_SECRET') !== undefined
      ? getInjectedEnv('ENABLE_DATA_CLASSIFICATION_SECRET')
      : parseBool(process.env.ENABLE_DATA_CLASSIFICATION_SECRET, false),
  TRINO_API_BASEURL: getInjectedEnv('TRINO_API_BASEURL') || process.env.TRINO_API_BASEURL,
  DATAIKU_API_BASEURL: getInjectedEnv('DATAIKU_API_BASEURL') || process.env.DATAIKU_API_BASEURL,
  ENABLE_TRINO_PUBLISH: getInjectedEnv('ENABLE_TRINO_PUBLISH') || parseBool(process.env.ENABLE_TRINO_PUBLISH, false),
  ENABLE_DATAIKU:
    getDNAInjectedEnv('ENABLE_DATAIKU_WORKSPACE') || parseBool(process.env.ENABLE_DATAIKU_WORKSPACE, false),
  STORAGE_TERMS_OF_USE_CONTENT: getInjectedEnv('STORAGE_TERMS_OF_USE_CONTENT') || process.env.STORAGE_TERMS_OF_USE_CONTENT,
  BUCKET_PUBLIC_ACCESS_BASEURL: getInjectedEnv('BUCKET_PUBLIC_ACCESS_BASEURL') || process.env.BUCKET_PUBLIC_ACCESS_BASEURL,
};
