export enum SessionStorageKeys {
  PKCE = 'pkce_verifier_token',
  MAMS = 'mams_access_token',
  OIDC = 'oidc_access_token',
  LANGUAGE = 'projectsmo-current-user-language',
}

export enum LANGUAGES {
  EN = 'en',
  DE = 'de',
}

export enum TeamMemberType {
  INTERNAL = 'internal',
  EXTERNAL = 'external',
}

export enum ProvisionSource {
  DATAIKU = 'dataiku',
  NOTEBOOK = 'notebook',
  MALWARESCANSERVICE = 'malwarescanservice',
}

export enum ComputeFixedTag {
  DATAIKU = 'DnA Dataiku Project',
  DATAIKUEXTELLO = 'Instance-eXtollo',
  DATAIKUONPREMISE = 'Instance-onPremise',
  NOTEBOOK = 'DnA Internal Notebook',
  MALWARESCANSERVICE = 'DnA Malware Scan Service',
}

export enum ErrorMsg {
  CUSTOMER_TAB = '*Please add minimum one customer.',
  KPI_TAB = '*Please add minimum one KPI',
  DATAFUNCTION_TAB = '*Please add minimum one data source',
  MEMBERS_TAB_DEVELOPERS = '*Please add minimum one developer',
  MEMBERS_TAB_PRODUCT_OWNER = '*Please add minimum one report member',
  MEMBERS_TAB_ADMIN = '*Please add minimum one report administrator',
}
