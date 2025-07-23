// const parseBool = (env, defaultValue) => {
//   if (!env) {
//     return defaultValue;
//   }

//   return env.toLowerCase() === 'true';
// };

const getInjectedEnv = (key) => {
  if (window.DB_SERVICE_INJECTED_ENVIRONMENT) {
    return window.DB_SERVICE_INJECTED_ENVIRONMENT[key];
  }
  return undefined;
};

// const getDNAInjectedEnv = (key) => {
//   if (window.INJECTED_ENVIRONMENT) {
//     return window.INJECTED_ENVIRONMENT[key];
//   }
//   return undefined;
// };

// You have to go via this or directly use the process.env
// BUT using in direct statements like === will result in direct expansion in builds this means the variable is lost
// we have to make sure that the string value of process.env is placed here.
export const Envs = {
  DB_SERVICE_API_BASEURL: getInjectedEnv('DB_SERVICE_API_BASEURL') || process.env.DB_SERVICE_API_BASEURL,
  API_BASEURL: getInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  REPORTS_API_BASEURL: getInjectedEnv('REPORTS_API_BASEURL') || process.env.REPORTS_API_BASEURL,
  STORAGE_API_BASEURL: getInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
  DNA_BRAND_LOGO_URL: getInjectedEnv('DNA_BRAND_LOGO_URL') || process.env.DNA_BRAND_LOGO_URL,
  DNA_APP_LOGO_URL: getInjectedEnv('DNA_APP_LOGO_URL') || process.env.DNA_APP_LOGO_URL,
  TOU_HTML: getInjectedEnv('TOU_HTML') || process.env.TOU_HTML,
  PGADMIN_URL: getInjectedEnv('PGADMIN_URL') || process.env.PGADMIN_URL,
};
