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
  'Azure'
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

export const UPDATE_FREQUENCIES = [
  'Annually',
  'Quarterly',
  'Monthly',
  'Weekly',
  'Daily',
  'Half-daily',
  'Hourly',
  'Live',
  'Never',
  'Other',
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
  
export const SKU_OPTIONS = ['F2', 'F4', 'F8', 'F16', 'F32', 'F64', 'F128', 'F256', 'F512', 'F1024', 'F2048'];

export const REGION_OPTIONS = [ 'Australia Central', 'Australia Central 2', 'Australia East', 'Australia Southeast',
  'Austria East', 'Belgium Central', 'Brazil South', 'Brazil Southeast',
  'Canada Central', 'Canada East', 'Central India', 'Central US', 'Chile Central', 'Denmark East', 'East Asia',
  'East US', 'East US 2', 'France Central', 'France South', 'Germany North', 'Germany West Central', 'Indonesia Central',
  'Israel Central', 'Italy North', 'Japan East', 'Japan West', 'Korea Central', 'Korea South', 'Malaysia West', 'Mexico Central', 'New Zealand North',
  'North Central US', 'North Europe', 'Norway East', 'Norway West', 'Poland Central', 'Qatar Central',
  'South Africa North', 'South Africa West', 'South Central US', 'South India', 'Southeast Asia', 'Spain Central', 'Sweden Central',
  'Sweden South', 'Switzerland North', 'Switzerland West', 'UAE Central', 'UAE North', 'UK South', 'UK West', 'US Gov Arizona','US Gov Texas',
  'US Gov Virginia', 'West Central US', 'West Europe', 'West India', 'West US', 'West US 2', 'West US 3',
];
export const STATE_OPTIONS = ['Active', 'Deleting', 'Failed', 'Inactive', 'Paused', 'Provisioning'];