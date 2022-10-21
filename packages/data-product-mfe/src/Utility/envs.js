// const parseBool = (env, defaultValue) => {
//   if (!env) {
//     return defaultValue;
//   }

//   return env.toLowerCase() === 'true';
// };

const getInjectedEnv = (key) => {
  if (window.DATA_PRODUCT_INJECTED_ENVIRONMENT) {
    return window.DATA_PRODUCT_INJECTED_ENVIRONMENT[key];
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
  DATA_PRODUCT_API_BASEURL: getInjectedEnv('DATA_PRODUCT_API_BASEURL') || process.env.DATA_PRODUCT_API_BASEURL,
  API_BASEURL: getDNAInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  DATA_GOVERNANCE_HTML_FOR_CHINA_DATA:
    getInjectedEnv('DATA_GOVERNANCE_HTML_FOR_CHINA_DATA') || process.env.DATA_GOVERNANCE_HTML_FOR_CHINA_DATA,
  DATA_PRODUCT_TOU_HTML: getInjectedEnv('DATA_PRODUCT_TOU_HTML') || process.env.DATA_PRODUCT_TOU_HTML,
  INFORMATION_POLICY_LINK: getInjectedEnv('INFORMATION_POLICY_LINK') || process.env.INFORMATION_POLICY_LINK,
};
