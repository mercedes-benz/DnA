// const parseBool = (env, defaultValue) => {
//   if (!env) {
//     return defaultValue;
//   }

//   return env.toLowerCase() === 'true';
// };

const getInjectedEnv = (key) => {
  if (window.DATA_ENTRY_INJECTED_ENVIRONMENT) {
    return window.DATA_ENTRY_INJECTED_ENVIRONMENT[key];
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
  CONTAINER_APP_URL: getInjectedEnv('CONTAINER_APP_URL') || process.env.CONTAINER_APP_URL,
  DATA_ENTRY_API_BASEURL: getInjectedEnv('DATA_ENTRY_API_BASEURL') || process.env.DATA_ENTRY_API_BASEURL,
  API_BASEURL: getDNAInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  REPORTS_API_BASEURL: getInjectedEnv('REPORTS_API_BASEURL') || process.env.REPORTS_API_BASEURL,
  STORAGE_API_BASEURL: getInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
  TOU_HTML: getInjectedEnv('TOU_HTML') || process.env.TOU_HTML,
};
