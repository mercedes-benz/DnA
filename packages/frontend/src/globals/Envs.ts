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
  }
}

const getInjectedEnv = (key: string) => {
  if (window.INJECTED_ENVIRONMENT) {
    return window.INJECTED_ENVIRONMENT[key];
  }
  return undefined;
};

// You have to go via this or directly use the process.env
// BUT using in direct statements like === will result in direct expansion in builds this means the variable is lost
// we have to make sure that the string value of process.env is placed here.
export const Envs = {
  OIDC_DISABLED:
    getInjectedEnv('OIDC_DISABLED') !== undefined
      ? getInjectedEnv('OIDC_DISABLED')
      : EnvParser.parseBool(process.env.OIDC_DISABLED, false),

  API_BASEURL: getInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
  NODE_ENV: getInjectedEnv('NODE_ENV') || process.env.NODE_ENV,
  CLIENT_IDS: getInjectedEnv('CLIENT_IDS') || process.env.CLIENT_IDS || '0oa2vd2h9wdMppJ0D5d7',
  REDIRECT_URLS: getInjectedEnv('REDIRECT_URLS') || process.env.REDIRECT_URLS || 'http://localhost:9090/home',
  OAUTH2_AUTH_URL:
    getInjectedEnv('OAUTH2_AUTH_URL') ||
    process.env.OAUTH2_AUTH_URL ||
    'https://xxxxx',
  OAUTH2_LOGOUT_URL:
    getInjectedEnv('OAUTH2_LOGOUT_URL') ||
    process.env.OAUTH2_LOGOUT_URL ||
    'https://xxxxx',
  OAUTH2_REVOKE_URL:
    getInjectedEnv('OAUTH2_REVOKE_URL') ||
    process.env.OAUTH2_REVOKE_URL ||
    'https://xxxxx',
  OAUTH2_TOKEN_URL:
    getInjectedEnv('OAUTH2_TOKEN_URL') ||
    process.env.OAUTH2_TOKEN_URL ||
    'https://xxxxx',
  OIDC_PROVIDER: getInjectedEnv('OIDC_PROVIDER') || process.env.OIDC_PROVIDER || 'INTERNAL',
  JUPYTER_NOTEBOOK_URL:
    getInjectedEnv('JUPYTER_NOTEBOOK_URL') ||
    process.env.JUPYTER_NOTEBOOK_URL ||
    'https://xxxxx',
  JUPYTER_NOTEBOOK_OIDC_POPUP_URL:
    getInjectedEnv('JUPYTER_NOTEBOOK_OIDC_POPUP_URL') ||
    process.env.JUPYTER_NOTEBOOK_OIDC_POPUP_URL ||
    'https://xxxxx',
  JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME:
    getInjectedEnv('JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME') ||
    parseInt(process.env.JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME || '5000', 10),
  DATAIKU_LIVE_APP_URL:
    getInjectedEnv('DATAIKU_LIVE_APP_URL') || process.env.DATAIKU_LIVE_APP_URL || 'https://xxxxx',
  DATAIKU_TRAINING_APP_URL:
    getInjectedEnv('DATAIKU_TRAINING_APP_URL') ||
    process.env.DATAIKU_TRAINING_APP_URL ||
    'https://xxxxx',
  DNA_SWAGGER_UI_URL:
    getInjectedEnv('DNA_SWAGGER_UI_URL') ||
    process.env.DNA_SWAGGER_UI_URL ||
    'https://xxxxx',
  DATAIKU_FERRET_URL:
    getInjectedEnv('DATAIKU_FERRET_URL') ||
    process.env.DATAIKU_FERRET_URL ||
    'https://xxxxx',
  DNA_APPNAME_HEADER: getInjectedEnv('DNA_APPNAME_HEADER') || process.env.DNA_APPNAME_HEADER || 'DnA App',
  DNA_APPNAME_HOME: getInjectedEnv('DNA_APPNAME_HOME') || process.env.DNA_APPNAME_HOME || 'Data and Analytics',
  DNA_CONTACTUS_HTML:
    getInjectedEnv('DNA_CONTACTUS_HTML') ||
    process.env.DNA_CONTACTUS_HTML ||
    `
    <div>
      <p>
        There could be many places where you may need our help, and we are happy to support you. <br />
        Please post your question(s) in our communication channels mentioned below.
      </p>
      <p>
        Mattermost:&nbsp;
        <a href="https://xxxxx" target="_blank" rel="noreferrer">
          https://xxxxx
        </a>
      </p>
      <p>
        Email:&nbsp;<a href="mailto:xxxxx">xxxxx</a>
      </p>
    </div>
  `,
  DNA_BRAND_LOGO_URL:
    getInjectedEnv('DNA_BRAND_LOGO_URL') || process.env.DNA_BRAND_LOGO_URL || '/images/branding/logo-brand.png',
  DNA_APP_LOGO_URL:
    getInjectedEnv('DNA_APP_LOGO_URL') || process.env.DNA_APP_LOGO_URL || '/images/branding/logo-app.png',
  ENABLEINTERNALUSERINFO:
    getInjectedEnv('ENABLEINTERNALUSERINFO') !== undefined
      ? getInjectedEnv('ENABLEINTERNALUSERINFO')
      : EnvParser.parseBool(process.env.ENABLEINTERNALUSERINFO, false),
  ENABLEDATACOMPLIANCE:
    getInjectedEnv('ENABLEDATACOMPLIANCE') !== undefined
      ? getInjectedEnv('ENABLEDATACOMPLIANCE')
      : EnvParser.parseBool(process.env.ENABLEDATACOMPLIANCE, false),
  ENABLEJUPYTERWORKSPACE:
    getInjectedEnv('ENABLEJUPYTERWORKSPACE') !== undefined
      ? getInjectedEnv('ENABLEJUPYTERWORKSPACE')
      : EnvParser.parseBool(process.env.ENABLEJUPYTERWORKSPACE, false),
  ENABLEDATAIKUWORKSPACE:
    getInjectedEnv('ENABLEDATAIKUWORKSPACE') !== undefined
      ? getInjectedEnv('ENABLEDATAIKUWORKSPACE')
      : EnvParser.parseBool(process.env.ENABLEDATAIKUWORKSPACE, false),
  ENABLEMALWARESERVICE:
    getInjectedEnv('ENABLEMALWARESERVICE') !== undefined
      ? getInjectedEnv('ENABLEMALWARESERVICE')
      : EnvParser.parseBool(process.env.ENABLEMALWARESERVICE, false),
  ENABLEPIPELINSERVICE:
    getInjectedEnv('ENABLEPIPELINSERVICE') !== undefined
      ? getInjectedEnv('ENABLEPIPELINSERVICE')
      : EnvParser.parseBool(process.env.ENABLEPIPELINSERVICE, false),
  ENABLEMALWARESCANONEAPI:
    getInjectedEnv('ENABLEMALWARESCANONEAPI') !== undefined
      ? getInjectedEnv('ENABLEMALWARESCANONEAPI')
      : EnvParser.parseBool(process.env.ENABLEMALWARESCANONEAPI, false),
  ENABLEMALWAREAPIINFO:
    getInjectedEnv('ENABLEMALWAREAPIINFO') !== undefined
      ? getInjectedEnv('ENABLEMALWAREAPIINFO')
      : EnvParser.parseBool(process.env.ENABLEMALWAREAPIINFO, false),
  DNA_COMPANYNAME: getInjectedEnv('DNA_COMPANYNAME') || process.env.DNA_COMPANYNAME || 'xxxxx',
};
