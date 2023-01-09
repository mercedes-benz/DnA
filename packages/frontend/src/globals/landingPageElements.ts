import { Envs } from 'globals/Envs';

const enableMalwareScanService = Envs.ENABLE_MALWARE_SCAN_SERVICE;
const enableDataPipelineService = Envs.ENABLE_DATA_PIPELINE_SERVICE;
const enableMyModelRegistryService = Envs.ENABLE_MY_MODEL_REGISTRY_SERVICE;
const enableMLPipelineService = Envs.ENABLE_ML_PIPELINE_SERVICE;
const enableStorageService = Envs.ENABLE_STORAGE_SERVICE;
const mLPipelineUrl = enableMLPipelineService ? Envs.ML_PIPELINE_URL : '#/comingsoon';
const enableChronosForecastingService = Envs.ENABLE_CHRONOS_FORECASTING_SERVICE;
const enableSapAnalyticsCloud = Envs.ENABLE_SAP_ANALYTICS_CLOUD;
const sapAnalyticsUrl = Envs.SAP_ANALYTICS_CLOUD_URL;
const enableCodeSpace = Envs.ENABLE_CODE_SPACE;
const enableJupiyterNoteWorkspace = Envs.ENABLE_JUPYTER_WORKSPACE;
const enableDataikuWorkspace = Envs.ENABLE_DATAIKU_WORKSPACE;
const powerBIUrl = '', dataOasisUrl = '', afoUrl= '', extolloUrl = '', btpUrl = '', dcwUrl = '', sbissUrl = '';

export const DataLayerElements = [
  {
    name: 'Data Model',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'KPI Wiki',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '',
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'CarLA Economic Model',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'Corporate Data Catalogue',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'SAP Connection Book',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'Smart Data Governance',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
];

export const DataGovernanceElements = [
  {
    name: 'DGO Social Intranet',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: Envs.DATA_GOVERNANCE_INFO_LINK,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'datagovernancesocialintranet',
  },
  {
    name: 'Minimum Information',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datasharing/create',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'a22minimuminformation',
  },
  {
    name: 'LCO/LCR Contacts',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datacompliancenetworklist',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
  },
];

export const ToolsLandingPageElements = [
  {
    name: 'Chronos Forecasting',
    description:
      'Chronos is a self-service tool that provides accurate and explainable forecasts based on Machine Learning for any KPI.',
    tags: ['Self Service', 'FOSS'],
    url: '/chronos',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableChronosForecastingService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'chronos',
  },
  {
    name: 'Kubeflow',
    description:
      'Kubeflow is a platform for data scientists who want to build and experiment with Machine Learning [ML] pipelines. Kubeflow is also for ML engineers and operational teams who want to deploy ML systems to various environments for development, testing, and production-level serving.',
    tags: ['Self Service', 'FOSS'],
    url: mLPipelineUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !enableMLPipelineService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'kubeflow',
  },
  {
    name: 'Dataiku',
    description:
      'Dataiku Data Science Studio is a low-code/ no-code data wrangling and machine learning platform. Typical use cases are data preparation, analysis and the development of machine learning models',
    tags: ['Self Service', 'FOSS'],
    url: '/mydataiku',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableDataikuWorkspace,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataiku-new',
  },
  {
    name: 'SAC',
    description:
      'SAP Analytics Cloud is an end-to-end cloud solution that brings together business intelligence, augmented analytics, and enterprise planning in a single system. SAC is the strategic Frontend tool to build dashboards (stories), explore data and build planning applications.',
    tags: ['Self Service', 'FOSS'],
    url: sapAnalyticsUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !enableSapAnalyticsCloud,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sac',
  },
  {
    name: 'Power BI',
    description:
      'Power BI Desktop is an application you install on your local computer that lets you connect to, transform, and visualize your data. With Power BI Desktop, you can connect to multiple different sources on data, and combine them into a data model. This data model lets you build visuals, and collections of visuals you can share as reports, with other people inside your organization. You get Power BI in the ITShop. Most users who work on business intelligence projects use Power BI Desktop to create reports, and then use the Power BI service called PBOS to share their reports with others.',
    tags: ['Self Service', 'FOSS'],
    url: powerBIUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !powerBIUrl,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'powerbi',
  },
  {
    name: 'Malware Scan',
    description:
      'Malware Scan as a Service is an API for scanning files for malicious code.',
    tags: ['Self Service', 'FOSS'],
    url: '/malwarescanservice',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableMalwareScanService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'malwarescan',
  },
  {
    name: 'Data Storage',
    description:
      'Storage Buckets provide data file storage that can be shared and connected to many tools. the storage buckets are based on a FOSS solution called minIO and provide a standard API for accessing file stores: S3.',
    tags: ['Self Service', 'FOSS'],
    url: '/storage',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableStorageService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'storage',
  },
  {
    name: 'Code Spaces (Beta)',
    description:
      'Code spaces provides developers with one click developer workspace customizable based on the technology used. Developers can collaborate on the team developing same solution but also deploy solution to different staging environments with click of the mouse.',
    tags: ['Self Service', 'FOSS'],
    url: '/codespaces',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableCodeSpace,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'codespace',
  },
  {
    name: 'Data Oasis',
    description:
      'Digital Oasis is an intergrated data platform, which is to drive and support the FG Digital Transformation with 6 modules (Mall, Factory, Workflow, Logistics, Data Warehouse, College) for 6 transformation scenarios.',
    tags: ['Self Service', 'FOSS'],
    url: dataOasisUrl,
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !dataOasisUrl,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'data-oasis',
  },
  {
    name: 'Jupyter',
    description:
      'Jupyter is a web-based interactive computational environment for creating data analysis in notebook like documents. At FC it is mainly used for Python Data Analysis.',
    tags: ['Self Service', 'FOSS'],
    url: '/notebook',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableJupiyterNoteWorkspace,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'jupyter',
  },
  {
    name: 'AFO',
    description:
      'SAP Analysis for Office (AfO) is an office add-in that enables multidimensional ad-hoc analyzes on OLAP data sources in Excel. In addition, the product enables workbook-based application design and the creation of BI presentations in PowerPoint. Connectivity to our SBISS platform is fully supported.',
    tags: ['Self Service', 'FOSS'],
    url: afoUrl,
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !afoUrl,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'afo',
  },
  {
    name: 'Airflow',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/pipeline',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableDataPipelineService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'airflow',
  },
  {
    name: 'eXtollo',
    description: 'eXtollo helps Mercedes business units to perform advanced analytics and AI use cases on very large amounts of data (big data) with high flexibility. eXtollo\'s main building blocks are the Data Lake, which serves data from the whole company, and the eXtollo Instances, which provide an Azure-based toolset for Big Data and Machine Learning use cases.',
    tags: ['Self Service', 'FOSS'],
    url: extolloUrl
    ,
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !extolloUrl,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'extollo',
  },
  {
    name: 'BTP',
    description: 'Business Technology Platform is a set of data integration, analytics, AI, application development and automation tools in the cloud. The Platform is managed by the SBISS team.',
    tags: ['Self Service', 'FOSS'],
    url: btpUrl
    ,
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !btpUrl,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'btp',
  },
  {
    name: 'DCW',
    description: 'SAP Data Warehouse Cloud is a cloud based data warehouse designed for self service data integration, modelling and analysis.It provides access to FC Data products and is connected to our SAP Analytics Cloud Frontend.',
    tags: ['Self Service', 'FOSS'],
    url: dcwUrl
    ,
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !dcwUrl,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'tools-mini',
  },
  {
    name: 'SBISS4',
    description: 'The SBISS/4 Launchpad is the entry Point to access Finance Functions for MBC based on CarLA as well as functions on Corporate FC DWH.',
    tags: ['Self Service', 'FOSS'],
    url: sbissUrl
    ,
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !sbissUrl,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sbiss',
  },
  {
    name: 'My Model Registry',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/modelregistry',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: !enableMyModelRegistryService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'modelregistry',
  },
];

export const TranparencyLandingPageElements = [
  {
    name: 'Portfolio',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/portfolio',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'portfolio',
  },
  {
    name: 'Solutions',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/allsolutions',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'solutionoverview',
  },
  {
    name: 'Reports',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/allreports',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'reports',
  },
  {
    name: 'Data Sharing',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datasharing',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'datasharing',
  },
];

export const DataLandingPageElements = [
  {
    name: 'Data Products',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/data/dataproducts',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIcon: 'dataproductoverview',
  },
  {
    name: 'Data Layer',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datalayer',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIcon: 'datamodel',
  },
  {
    name: 'Data Governance',
    description:
      'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datagovernance',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIcon: 'datagovernance',
  },
];

export const TrainingsLandingPageElements = [
  {
    name: 'Udemy',
    description:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    tags: ['Self Service', 'FOSS'],
    url: Envs.UDEMY_URL,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'IconDataProducts',
  },
  {
    name: 'LinkedIn Learning',
    description:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    tags: ['Self Service', 'FOSS'],
    url: Envs.LINKEDIN_LEARNING_URL,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'IconDataLayer',
  },
  {
    name: 'Dataiku',
    description:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    tags: ['Self Service', 'FOSS'],
    url: Envs.DATAIKU_TRAINING_APP_URL,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'IconDataLayer',
  },
  {
    name: 'PowerBI',
    description:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    tags: ['Self Service', 'FOSS'],
    url: Envs.POWERBI_TRAINING_URL,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'IconDataLayer',
  },
  {
    name: 'SAC',
    description:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    tags: ['Self Service', 'FOSS'],
    url: Envs.SAC_TRAINING_URL,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
  },
  {
    name: 'Databricks',
    description:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    tags: ['Self Service', 'FOSS'],
    url: Envs.DATABRICKS_TRAINING_URL,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
  },
];

export const CarlaLandingPageElements = [
  {
    name: 'Architecture',
    description:
      'Amet consetetur lorem ipsum dolor sit amet.',
    tags: ['Self Service', 'FOSS'],
    url: '/',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'architecture',
  },
  {
    name: 'Solutions',
    description:
      'Amet consetetur lorem ipsum dolor sit amet.',
    tags: ['Self Service', 'FOSS'],
    url: '/allsolutions',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'solutionoverview',
  },
  {
    name: 'Data',
    description:
      'Amet consetetur lorem ipsum dolor sit amet.',
    tags: ['Self Service', 'FOSS'],
    url: '/data',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'data',
  },
  {
    name: 'Transactional Data',
    description:
      'Amet consetetur lorem ipsum dolor sit amet.',
    tags: ['Self Service', 'FOSS'],
    url: '/',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'transactionaldata',
  },
];