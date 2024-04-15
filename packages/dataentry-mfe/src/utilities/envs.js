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
  DATA_ENTRY_API_BASEURL: getInjectedEnv('DATA_ENTRY_API_BASEURL') || process.env.DATA_ENTRY_API_BASEURL,
  API_BASEURL: getDNAInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  REPORTS_API_BASEURL: getDNAInjectedEnv('REPORTS_API_BASEURL') || process.env.REPORTS_API_BASEURL,
  STORAGE_API_BASEURL: getDNAInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
  DNA_BRAND_LOGO_URL: getDNAInjectedEnv('DNA_BRAND_LOGO_URL') || process.env.DNA_BRAND_LOGO_URL,
  DNA_APP_LOGO_URL: getDNAInjectedEnv('DNA_APP_LOGO_URL') || process.env.DNA_APP_LOGO_URL,
};
