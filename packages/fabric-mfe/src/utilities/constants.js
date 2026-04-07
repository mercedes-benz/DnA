export const SESSION_STORAGE_KEYS = {
  JWT: 'jwt',
  PAGINATION_MAX_ITEMS_PER_PAGE: 'paginationMaxItemsPerPage',
};

export const ProvisionSource = {
  FABRIC: 'fabric',
};

export const FLOW_DIAGRAM_TYPES = {
  ROLE: 'ROLE',
  ENTITLEMENT: 'ENTITLEMENT',
  INTERMEDIATE: 'INTERMEDIATE',
  ENTITLEMENT_ROLE: 'ENTITLEMENT_ROLE',
  PRIVILEGE: 'PRIVILEDGE',
  GROUP: 'GROUP',
}

export const FLOW_DIAGRAM_STATES = {
  PENDING: 'PENDING',
  CREATED: 'CREATED',
  ASSIGNED: 'ASSIGNED',
  NULL: null,
}

export const FLOW_DIAGRAM_MEASUREMENTS = {
  NODE_WIDTH: 300,
  NODE_HEIGHT: 62,
  INTERMEDIATE_NODE_WIDTH: 10,
}

export const DIVISIONS = [
  'Mercedes-Benz Cars',
  'Mercedes-Benz Mobility',
  'Mercedes-Benz Vans',
];

export const DATA_TIER = [
  'Tier1 [DATA ASSET]', 
  'Tier2 [NON DATA ASSET]'
];

export const DATA_TIER_MAP = {
  'Tier1 [DATA ASSET]': 1,
  'Tier2 [NON DATA ASSET]': 2,
};


export const TEAMS_PROFILE_LINK_URL_PREFIX = 'https://your-team-profile-url-prefix/';
  export const USER_ROLE = { 
    GUEST : '0',
    USER : '1',
    EXTENDED : '2',
    ADMIN : '3',
    REPORTADMIN : '4',
    DIVISIONADMIN : '5',
    DATACOMPLIANCEADMIN : '6',
    CODESPACEADMIN : '7',
    FABRICADMIN : '8',
  };