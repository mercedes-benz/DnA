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
  'Daimler Buses',
  'Daimler Trucks',
  'Mercedes-Benz Cars',
  'Mercedes-Benz Mobility',
  'Mercedes-Benz Vans',
];

export const DATA_ORIGINS = ['Internal', 'External', 'Mixed'];
export const YES_NO_OPTIONS = ['Yes', 'No'];
export const DATA_TIER = ['Tier1', 'Tier2'];
export const DATA_TIER_MAP = {
  Tier1: 1,
  Tier2: 2,
};
