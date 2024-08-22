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
  APPREDIRECT_URL = 'appredirect_url',
  LISTVIEW_MODE_ENABLE = 'listViewModeEnable',
  SOLUTION_SORT_VALUES = 'sortingInfo',
  REPORT_SORT_VALUES = 'reportsSortingInfo',
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
  DATA_VALUE = 'Data Value (MBM)',
}

export enum SOLUTION_DATA_VALUE_CATEGORY_TYPES {
  SAVINGS = 'Savings',
  REVENUE = 'Revenue',
}

export enum DATA_COMPLIANCE_INFO_LINKS {
  GUIDELINES = 'https://legalbot.app.corpintra.net/?bot=6454c7f4e348b8f6af9247be95cbf70271a7727f4c56a116c679d8f99ad7f0a7',
  LOCAL_OFFICER = 'https://social.intra.corpintra.net/docs/DOC-188868',
  MORE_INFO = 'https://social.intra.corpintra.net/docs/DOC-170669',
}

export enum AI_RISK_ASSESSMENT_TYPES {
  NOT_APPLICABLE = 'AI Risk Self-Assessment does not apply',
  BASIC_RISK = 'Basic Risk AI',
  HIGH_RISK = 'High Risk AI'
}

export const MALWARE_SCAN_SERVICE_ONE_API_LINK = 'https://developer.corpinter.net/apis/malwarescanapi';

export const SUPPORT_EMAIL_ID = 'dna@mercedes-benz.com';

export const TEAMS_PROFILE_APP_NAME = 'MBInside';
export const TEAMS_PROFILE_LINK_URL_PREFIX = 'https://mbinside.app.corpintra.net/person/';
export const IAM_URL = 'https://iamat.iam.corpintra.net/iamat/';

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
    {
      id: 'cars',
      name: 'Cars',
    },
    {
      id: 'trucks',
      name: 'Trucks',
    },
    {
      id: 'buses',
      name: 'Buses',
    },
    {
      id: 'parts',
      name: 'Parts',
    },
    {
      id: 'research',
      name: 'Research',
    },
    {
      id: 'infrastructure',
      name: 'Infrastructure',
    },
    {
      id: 'emobility',
      name: 'E-Mobility',
    },
    {
      id: 'customer',
      name: 'Customer',
    },
    {
      id: 'concept',
      name: 'Concept',
    },
    {
      id: 'logistics',
      name: 'Logistics',
    },
    {
      id: 'service',
      name: 'Service',
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
};

export const AdditionalResourceTooltipContent = 'if required please detail your need via Members tab.';

export const DataStrategyDomainInfoList = [
  {
    title: 'Data driven Process Optimization & Self-Service',
    description:
      'Based on data and technology we automate our internal marketing, sales and services processes as well as external customer communication to decrease costs, generate leads, increase sales and improve the customer experience., By this we shift capacities from dull and repetitive tasks to higher value and creative tasks (e.g. Chatbots, Process Automation)',
  },
  {
    title: 'Data-driven Product Optimization',
    description:
      'All product related decisions in MS are driven and powered by data. Every data-driven decision is incremental better than a non data-driven one. By doing so we will make better and better decisions with clear positive impact on our business e.g. cars and Digital Services (e.g. Radical SA, Data-driven Product Improvement)',
  },
  {
    title: 'Data-as-a-Service Business',
    description:
      'With Data as a Service (DaaS) we shape our role in the platform-based and data-driven economy. By sharing data with 3rd parties we generate new profit pools for B2B data sales and B2G/B3M data-driven services. We comply with external legal regulations and put customers trust before profit.',
  },
  {
    title: 'Data driven Offers & Recommendations',
    description:
      'We offer a consistent customer journey, create traffic on our platforms and collect customer data to enhance our understanding of customer needs, improve our data-driven offers and recommendations and sell more Cars, Services and Parts. (e.g. Car Data Driven Marketing, Next Best Action)',
  },
  {
    title: 'Data-driven Marketing & Sales',
    description:
      'We put the customer and our understanding of them, their needs and their behavior at the center of everything we do. Increase sales, services and parts revenue and reduce marketing costs by reaching the right person, at the right time, with the right message, in the right context via the right channel.',
  },
  {
    title: 'Digital Service Business',
    description:
      'We will only be successful if our products embrace our customers, data are the basis to develop products that perfectly fit the needs of our customers. Digital is the only channel how to target our customers with Digital Services in order to deliver an excellent customer experience across all touchpoints. (Mme Ecosystem, Digital Services)',
  },
  {
    title: 'Forecasting, Planning & Reporting',
    description:
      'We use data and machine learning algorithms to optimize forecasts, planning and automate reporting. .By doing so, we e.g. generate transparency, efficiencies, speed-up processes, identify root-causes, calculate better predictions, allocate resources in an optimal manner (e.g. Sales Forecasting, Sales Planning)',
  },
];

export const RolesInfoList = [
  {
    title: 'Data Product Owner',
    description:
      'Manages the data product creation including strategic planning, prioritisation, communication, stakeholder involvement, metrics and reporting and conveying business value.',
  },
  {
    title: 'Data Engineer',
    description:
      'Builds systems that collect, manage, and convert raw data into usable information for data scientists and business analysts to interpret.',
  },
  {
    title: 'Data Analyst',
    description: 'Collects, cleans, and interprets data sets in order to answer a question or solve a problem.',
  },
  {
    title: 'Machine Learning Engineer',
    description:
      'Builds artificial intelligence systems that leverage huge data sets to generate and develop algorithms capable of learning and eventually making predictions.',
  },
  {
    title: 'Automation Engineer',
    description: 'Develops, tests and implements automation technology.',
  },
  {
    title: 'No/Low Code BI Developer',
    description: 'Interprets and displays data for an organization using business intelligence software.',
  },
  {
    title: 'BI Engineer',
    description: 'Designs, implements, and maintains systems used to collect and analyze business intelligence data.',
  },
  {
    title: 'Data Protection Expert',
    description: 'Ensures the requirements of the General Data Protection Regulation in the company are met.',
  },
  {
    title: 'Knowledge Engineer',
    description:
      'Acquires domain knowledge (by talking to subject matter expert and analyzing domain content) and then formally models this domain knowledge within a domain ontology (using proper modeling tools and standards). This domain ontology is essential for enterprise-wide data integration and the starting point for data engineers.',
  },
];

export const TOTAL_LOCATIONS_COUNT = 162;
export const OTHER_PLATFORMS = 'Other platforms (e.g. eXtollo, Azure, Spire, MIC,…)';
export const SOLUTION_FIXED_TAGS = [ '#GenAI', 'GenAI' ];

export const PRIVATE_RECIPES: any[] = [
  /* Use bellow code to keep your private recipes until we make proper recipe management */
  { id: 'bat-frontend', resource: '4Gi,2000Mi,2000m,4000Mi,2000m', name: `BAT Frontend`, repodetails: 'git.i.mercedes-benz.com/BAT/bat-ui.git,/*' },
  { id: 'bat-backend', resource: '4Gi,3000Mi,2000m,4000Mi,2000m', name: `BAT Backend`, repodetails: 'git.i.mercedes-benz.com/BAT/bat-api.git,/*' }
];

export const DEPLOYMENT_DISABLED_RECIPE_IDS: string[] = [ 'default', 'private-user-defined', 'bat-frontend', 'bat-backend' ];

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
export const CODE_SPACE_TITLE = 'Alice Securtity Configuration';

