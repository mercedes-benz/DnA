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
  NOTEBOOK = 'DnA Internal Notebook',
  MALWARESCANSERVICE = 'DnA Malware Scan Service',
}
