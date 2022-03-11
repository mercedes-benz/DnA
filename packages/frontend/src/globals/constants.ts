export enum HTTP_METHOD {
  POST = 'POST',
  GET = 'GET',
  PUT = 'PUT',
  PATCH = 'PATCH',
  DELETE = 'DELETE',
  MKCOL = 'MKCOL',
}

export enum LOCAL_STORAGE_KEY {
  TOKEN = 'TOKEN',
}
export enum USER_ROLE {
  GUEST = '0',
  USER = '1',
  EXTENDED = '2',
  ADMIN = '3',
  REPORTADMIN = '4',
}

export enum SESSION_STORAGE_KEYS {
  PKCE = 'pkce_verifier',
  CODE = 'code',
  JWT = 'jwt',
  ACCESS_TOKEN = 'access_token',
  USER_ID = 'user_id',
  REDIRECT_URL = 'redirect_url',
  PORTFOLIO_FILTER_VALUES = 'portfolioFilterValues',
  REPORT_FILTER_VALUES = 'reportFilterValues',
  PAGINATION_MAX_ITEMS_PER_PAGE = 'paginationMaxItemsPerPage',
  APPREDIRECT_URL = 'appredirect_url',
  LISTVIEW_MODE_ENABLE = 'listViewModeEnable',
}

export enum LOCAL_STORAGE_KEYS {
  SHOW_DISCLAIMER_STATUS = 'showDisclaimerStatus',
}

export enum ENV {
  LOCAL = 'local',
  DEV = 'development',
  INT = 'integration',
  PROD = 'production',
}

export enum SOLUTION_LOGO_IMAGE_TYPES {
  THUMBNAIL = 'thumbnails',
  TILE = 'tiles',
  BANNER = 'banners',
}

export enum DATA_COMPLIANCE_INFO_LINKS {
  GUIDELINES = 'https://your-data-compliance-guidelines-url',
  LOCAL_OFFICER = 'https://your-local-data-compliance-officer-profile-url',
  MORE_INFO = 'https://your-data-compliance-more-info-url',
}

export const MALWARE_SCAN_SERVICE_ONE_API_LINK = 'https://your-malware-scan-one-api-link/';

export const SUPPORT_EMAIL_ID = 'support@your-domain.com';

export const TEAMS_PROFILE_LINK_URL_PREFIX = 'https://your-team-profile-url-prefix/';

export const ATTACH_FILES_TO_ACCEPT =
  '.doc,.docx,.odt,.pptx,.rtf,.pdf,.bmp,.gif,.png,.jpg,.jpeg,.csv,.xsl,.xlsx,.ppt,.txt,.zip';

export const PredefinedSolutionLogoImagesInfo = {
  folder: 'images/solutionLogoImages', // Path inside frontend/public folder
  images: [
    {
      id: 'default',
      name: 'Default',
    },
  ],
};

export const ExistingSolutionInfoList = [
  {
    title: 'Yes',
    description: 'if required please detail your need via Members tab.',
  },
  {
    title: 'No',
    description:
      '',
  },
];

export const DataStrategyDomainInfoList = [
  {
    title: 'DUMMY 1',
    description:
      'DUMMY 1 Description',
  },
  {
    title: 'DUMMY 2',
    description:
      'DUMMY 2 Description',
  },
];

export const RolesInfoList = [
  {
    title: 'DUMMY 1',
    description:
      'DUMMY 1 Description',
  },
  {
    title: 'DUMMY 2',
    description:
      'DUMMY 2 Description',
  },
];
