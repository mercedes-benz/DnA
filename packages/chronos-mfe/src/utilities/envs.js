// const parseBool = (env, defaultValue) => {
//   if (!env) {
//     return defaultValue;
//   }

//   return env.toLowerCase() === 'true';
// };

const getInjectedEnv = (key) => {
  if (window.CHRONOS_INJECTED_ENVIRONMENT) {
    return window.CHRONOS_INJECTED_ENVIRONMENT[key];
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
  CHRONOS_API_BASEURL: getInjectedEnv('CHRONOS_API_BASEURL') || process.env.CHRONOS_API_BASEURL,
  API_BASEURL: getDNAInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  ENABLE_CHRONOS_ONEAPI: getInjectedEnv('ENABLE_CHRONOS_ONEAPI') || process.env.ENABLE_CHRONOS_ONEAPI, 
  CHRONOS_ONEAPI_URL: getInjectedEnv('CHRONOS_ONEAPI_URL') || process.env.CHRONOS_ONEAPI_URL,
  STORAGE_MFE_APP_URL: getInjectedEnv('STORAGE_MFE_APP_URL') || process.env.STORAGE_MFE_APP_URL,
  STORAGE_API_BASEURL: getInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
  CHRONOS_RELEASES_GIT_URL: getInjectedEnv('CHRONOS_RELEASES_GIT_URL') || process.env.CHRONOS_RELEASES_GIT_URL,
};
