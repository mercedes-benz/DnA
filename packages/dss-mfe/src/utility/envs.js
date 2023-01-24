// const parseBool = (env, defaultValue) => {
//   if (!env) {
//     return defaultValue;
//   }

//   return env.toLowerCase() === 'true';
// };

const getInjectedEnv = (key) => {
  if (window.DSS_INJECTED_ENVIRONMENT) {
    return window.DSS_INJECTED_ENVIRONMENT[key];
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
  DATAIKU_API_BASEURL: getInjectedEnv('DATAIKU_API_BASEURL') || process.env.DATAIKU_API_BASEURL,
  API_BASEURL: getDNAInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  DATAIKU_LIVE_APP_URL: getInjectedEnv('DATAIKU_LIVE_APP_URL') || process.env.DATAIKU_LIVE_APP_URL,
  DATAIKU_TRAINING_APP_URL: getInjectedEnv('DATAIKU_TRAINING_APP_URL') || process.env.DATAIKU_TRAINING_APP_URL,
  DATAIKU_FERRET_URL: getInjectedEnv('DATAIKU_FERRET_URL') || process.env.DATAIKU_FERRET_URL,
  DNA_BRAND_LOGO_URL: getDNAInjectedEnv('DNA_BRAND_LOGO_URL') || process.env.DNA_BRAND_LOGO_URL,
  DNA_APP_LOGO_URL: getDNAInjectedEnv('DNA_APP_LOGO_URL') || process.env.DNA_APP_LOGO_URL,
};
