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

export const DATA_ORIGINS = ['Internal', 'External', 'Mixed'];
export const YES_NO_OPTIONS = ['Yes', 'No'];
export const DATA_TIER = ['Tier1 [DATA ASSET]', 'Tier2 [NON DATA ASSET]'];
export const DATA_TIER_MAP = {  
  'Tier1 [DATA ASSET]': 1,
  'Tier2 [NON DATA ASSET]': 2,
};

export const BUSINESS_DOMAINS = [
  'FC',
  'HR',
  'IL',
  'IT',
  'MO',
  'MP',
  'MS', 
  'RD',
  'GSP',
  'MBM',
  'VAN',
];

export const CLOUD_PROVIDERS = [
  'Azure',
  'AWS',
];

export const DATA_HUBS = [
  'eXtollo',
  'MO360 Data platform',
  'MO360 Ingest Quality',
];

export const TECHNOLOGIES = [
  'UnityCatalog',
  'Fabric',
];

export const SECURITY_LEVELS = [
  'Confidential',
  'Internal',
  'Public',
];

export const PURPOSES = [
  'Develop a product',
  'Improve a product',
  'Improve risk / damage prevention',
  'Improve predictive car maintenance',
  'Improve internal processes',
  'Evaluate new business model',
  'Improve customer marketing and relationship',
  'Direct marketing and promotion',
  'Identify new trends and market developments',
  'Optimize product portfolio and pricing and revenue stream',
];

export const CRITERIA_TRANSFER_PRICING = [
  'Raw',
  'Cleaned up',
  'Restructured',
  'Enriched using AI methodology',
  'Enriched using non-AI methodology',
];

export const QUALIFICATION_TRANSFER_PRICING = [
  'Other',
  'HR data',
  'IT data',
  'R&D data',
  'Robot data',
  'Sales data',
  'Vehicle data',
  'Customer data',
  'Financial data',
  'Logistics data',
  'Marketing data',
  'Engineering data',
  'Procurement data',
];