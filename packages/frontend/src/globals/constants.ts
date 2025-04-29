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
  DIVISIONADMIN = '5',
  DATACOMPLIANCEADMIN = '6',
  CODESPACEADMIN = '7'
}

export enum SESSION_STORAGE_KEYS {
  PKCE = 'pkce_verifier',
  CODE = 'code',
  JWT = 'jwt',
  ACCESS_TOKEN = 'access_token',
  USER_ID = 'user_id',
  REDIRECT_URL = 'redirect_url',
  PORTFOLIO_FILTER_VALUES = 'portfolioFilterValues',
  DATAPRODUCT_FILTER_VALUE = 'dataproductFilterValues',
  REPORT_FILTER_VALUES = 'reportFilterValues',
  PAGINATION_MAX_ITEMS_PER_PAGE = 'paginationMaxItemsPerPage',
  AUDIT_LOGS_MAX_ITEMS_PER_PAGE = 'auditLogsMaxItemsPerPage',
  APPREDIRECT_URL = 'appredirect_url',
  LISTVIEW_MODE_ENABLE = 'listViewModeEnable',
  SOLUTION_SORT_VALUES = 'sortingInfo',
  REPORT_SORT_VALUES = 'reportsSortingInfo',
  ALICE_ROLES_CREATED = 'aliceRolesCreated',
  NAVIGATE_CODESPACE_RECIPE = 'navigateCodespaceRecipe'
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

export enum SOLUTION_VALUE_CALCULATION_TYPES {
  DIGITAL_VALUE = 'Digital Value',
  DATA_VALUE = 'Data Value',
}

export enum SOLUTION_DATA_VALUE_CATEGORY_TYPES {
  SAVINGS = 'Savings',
  REVENUE = 'Revenue',
}

export enum DATA_COMPLIANCE_INFO_LINKS {
  GUIDELINES = 'https://your-data-compliance-guidelines-url',
  LOCAL_OFFICER = 'https://your-local-data-compliance-officer-profile-url',
  MORE_INFO = 'https://your-data-compliance-more-info-url',
}

export enum AI_RISK_ASSESSMENT_TYPES {
  NOT_APPLICABLE = 'AI Risk Self-Assessment does not apply',
  BASIC_RISK = 'Basic Risk AI',
  HIGH_RISK = 'High Risk AI'
}

export const MALWARE_SCAN_SERVICE_ONE_API_LINK = 'https://your-malware-scan-one-api-link/';

export const SUPPORT_EMAIL_ID = 'support@your-domain.com';

export const TEAMS_PROFILE_APP_NAME = 'Teams';
export const TEAMS_PROFILE_LINK_URL_PREFIX = 'https://your-team-profile-url-prefix/';
export const IAM_URL = 'https://your-iam-url/';

export const ATTACH_FILES_TO_ACCEPT =
  '.doc,.docx,.odt,.pptx,.rtf,.pdf,.bmp,.gif,.png,.jpg,.jpeg,.csv,.xsl,.xlsx,.ppt,.txt,.zip';

export const OPEN_SOURCE_TOOLS = [
  {
    name: 'Jupyter Notebook',
    version: '2.3.2',
    github: 'https://github.com/jupyter/notebook',
    license: {
      name: '3-clause BSD License',
      link: 'https://github.com/jupyter/notebook/blob/main/LICENSE',
    },
  },
  {
    name: 'Apache Airflow',
    version: '1.10.15',
    github: 'https://github.com/apache/airflow',
    license: {
      name: 'Apache License 2.0',
      link: 'https://github.com/apache/airflow/blob/main/LICENSE',
    },
  },
  {
    name: 'Kubeflow',
    version: '1.4.0',
    github: 'https://github.com/kubeflow/kubeflow',
    license: {
      name: 'Apache License 2.0',
      link: 'https://github.com/kubeflow/kubeflow/blob/master/LICENSE',
    },
  },
];

export const PredefinedSolutionLogoImagesInfo = {
  folder: 'images/solutionLogoImages', // Path inside frontend/public folder
  images: [
    {
      id: 'default',
      name: 'Default',
    },
  ],
};

export const ToolsPageImagesInfo = {
  folder: 'images/toolDetailedPageImages',
  images:
    [
    {
      id: 'dataikuDSS',
      banner: 'banner.png',
      toolPipeline:'toolPipeline.png'
    },
    {
      id: 'powerBI',
      banner: 'banner.jpg',
      toolPipeline:'toolPipeline.png'
    },
    {
      id: 'fabric',
      banner: 'banner.png',
      toolPipeline:'toolPipeline.png'
    },
    {
      id: 'powerPlatform',
      banner: 'banner.png',
      toolPipeline:'toolPipeline.png'
    }
  ]
}


export const AdditionalResourceTooltipContent = 'if required please detail your need via Members tab.';

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

export const TOTAL_LOCATIONS_COUNT = 162;

export const OTHER_PLATFORMS = 'Other platforms (e.g. Azure,…)';

export const SOLUTION_FIXED_TAGS = ['#GenAI', 'GenAI'];

export const PRIVATE_RECIPES: any[] = [
  /* Uncomment and use bellow code to keep your private recipes until we make proper recipe management */
  // { id: 'private-frontend', resource: '4Gi,2000Mi,2000m,4000Mi,2000m', name: `Private Frontend (Debian 11 OS, 2GB RAM, 2CPU)`, repodetails: 'YOUR PRIVATE REPO URL' },
  // { id: 'private-backend', resource: '4Gi,3000Mi,2000m,4000Mi,2000m', name: `Private Backend (Debian 11 OS, 2GB RAM, 2CPU)`, repodetails: 'YOUR PRIVATE REPO URL' }
];

export const DEPLOYMENT_DISABLED_RECIPE_IDS: string[] = [ 'default', 'private-user-defined' ];

export const HTTP_OPTIONS = [{
  id: 1,
  name: 'POST'
}, {
  id: 2,
  name: 'GET'
}, {
  id: 3,
  name: 'PUT'
}, {
  id: 4,
  name: 'DELETE'
}, {
  id: 5,
  name: 'PATCH'
}, {
  id: 6,
  name: 'HEAD'
}, {
  id: 7,
  name: 'OPTIONS'
}, {
  id: 8,
  name: 'TRACE'
}, {
  id: 9,
  name: 'CONNECT'
}];

export const CODE_SPACE_STATUS = ['DRAFT', 'PUBLISHED'];
export const CODE_SPACE_DISABLE_DNA_PROTECT = ['PUBLISHED', 'ACCEPTED', 'REQUESTED'];
export const CODE_SPACE_TITLE = 'Securtity Configuration';
