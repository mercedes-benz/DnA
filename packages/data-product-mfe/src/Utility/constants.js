import { Envs } from './envs';

export const SESSION_STORAGE_KEYS = {
  JWT: 'jwt',
  PAGINATION_MAX_ITEMS_PER_PAGE: 'paginationMaxItemsPerPage',
  MY_DATATRANSFER_FILTER: 'MyDataTransferFilter',
  DATATRANSFER_FILTER_VALUES: 'datatransferFilterValues',
};

export const LOCAL_STORAGE_KEYS = {
  TOUR_GUIDE_STATUS: 'dataProductTourGuide',
};

export const USER_ROLE = {
  GUEST: '0',
  USER: '1',
  EXTENDED: '2',
  ADMIN: '3',
  REPORTADMIN: '4',
  DIVISIONADMIN: '5',
  DATACOMPLIANCEADMIN: '6',
};

export const MAP_URLS = {
  SBISS4: Envs.SBISS_LAUNCHPAD_TOOL_URL,
  SBISSonHANA: Envs.SBISS_HANA_LAUNCHPAD_TOOL_URL,
  SAC: Envs.SAP_ANALYTICS_CLOUD_URL,
  AFO: Envs.AFO_TOOL_URL,
  'Power BI': Envs.POWER_BI_TOOL_URL,
};
