import { Envs } from 'globals/Envs';

const enableMalwareScanService = Envs.ENABLE_MALWARE_SCAN_SERVICE;
const enableDataPipelineService = Envs.ENABLE_DATA_PIPELINE_SERVICE;
const enableMyModelRegistryService = Envs.ENABLE_MY_MODEL_REGISTRY_SERVICE;
const enableMLPipelineService = Envs.ENABLE_ML_PIPELINE_SERVICE;
const enableStorageService = Envs.ENABLE_STORAGE_SERVICE;
const mLPipelineUrl = enableMLPipelineService ? Envs.ML_PIPELINE_URL : '#/comingsoon';
const enableChronosForecastingService = Envs.ENABLE_CHRONOS_FORECASTING_SERVICE;
const enableMatomoService = Envs.ENABLE_MATOMO_SERVICE;
const enableSapAnalyticsCloud = Envs.ENABLE_SAP_ANALYTICS_CLOUD;
const sapAnalyticsUrl = Envs.SAP_ANALYTICS_CLOUD_URL;
const enableCodeSpace = Envs.ENABLE_CODE_SPACE;
const enableJupiyterNoteWorkspace = Envs.ENABLE_JUPYTER_WORKSPACE;
const enableDataikuWorkspace = Envs.ENABLE_DATAIKU_WORKSPACE;
const udemyUrl = Envs.UDEMY_URL;
const linkedinLearningUrl = Envs.LINKEDIN_LEARNING_URL;
const dataikuTrainingUrl = Envs.DATAIKU_TRAINING_URL;
const powerbiTrainingUrl = Envs.POWERBI_TRAINING_URL;
const sacTrainingUrl = Envs.SAC_TRAINING_URL;
const databricksTrainingUrl = Envs.DATABRICKS_TRAINING_URL;
const digitalCaseProgramUrl = Envs.DIGITAL_CASE_PROGRAM_URL;
const datasphereTrainingUrl = Envs.DATASPHERE_TRAINING_URL;
const afoUrl= Envs.AFO_TOOL_URL;
const btpUrl = Envs.BPT_TOOL_URL;
const dataOasisUrl = Envs.DATA_OASIS_TOOL_URL;
const dataQToolUrl = Envs.DATAQ_TOOL_URL;
const datasphereUrl = Envs.DATASPHERE_TOOL_URL;
const extolloUrl = Envs.EXTOLLO_TOOL_URL;
const powerBIUrl = Envs.POWER_BI_TOOL_URL;
const dataModelUrl = Envs.DATA_MODEL_URL;
const dataCatalogUrl = Envs.DATA_CATALOG_URL;
const corporateDataCatalogUrl = Envs.CORPORATE_DATA_CATALOG_URL;
const smartDataGovernanceUrl = Envs.SMART_DATA_GOVERNANCE_URL;
const spireUrl = Envs.SPIRE_URL;
const enableDatalakeService = Envs.ENABLE_DATALAKE_SERVICE;
const genAIDirectChatUrl = Envs.GENAI_DIRECT_CHAT_URL;
const bisoContactsLink = Envs.BISO_CONTACTS_URL;
const clamavImagwUrl = Envs.CLAMAV_IMAGE_URL;

export const DataLayerElements = [
  {
    name: 'Data Model',
    description:
      'The Data Model shows the most important Master data dimensions and how they relate to each other.',
    tags: ['Self Service', 'FOSS'],
    url: dataModelUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !dataModelUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'Corporate Data Catalogue',
    description:
      'Catalog of Mercedes-Benz data assets. If you never logged in the CDC before, a user will be generated for your this can take a while. CarLA systems can be found by searching for CarLA.',
    tags: ['Self Service', 'FOSS'],
    url: corporateDataCatalogUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !corporateDataCatalogUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'Smart Data Governance',
    description:
      'Information on the compliance with architecture guidelines and the data volume in the CarLA Core Datawarehouse.',
    tags: ['Self Service', 'FOSS'],
    url: smartDataGovernanceUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !smartDataGovernanceUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
];

export const DataGovernanceElements = [
  {
    name: 'DGO Social Intranet',
    description:
      'The Data Governance Office is your single point of contact around Data Compliance & Data Protection, Data Management, (Business) Information Security and Data@Cloud questions.',
    tags: ['Self Service', 'FOSS'],
    url: Envs.DATA_GOVERNANCE_INFO_LINK,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !Envs.DATA_GOVERNANCE_INFO_LINK?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'datagovernancesocialintranet',
  },
  {
    name: 'Usage Information',
    description:
      'Sharing Data? Fill out the required Usage Information (Policy A22) to let the receiving side know of any restriction when using the data.',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datasharing',
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
      'The Local Compliance Officers/Responsibles are your primary contacts on all compliance topics.',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datacompliancenetworklist',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
  },
  {
    name: 'BISO Contacts',
    description:
      'The Business Information Security Officer helps you in the topic area of information security.',
    tags: ['Self Service', 'FOSS'],
    url: bisoContactsLink,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !bisoContactsLink?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
  },
];

export const ToolsLandingPageElements = [
  {
    id:'chronosForecasting',
    name: 'Chronos Forecasting',
    description:
      'Chronos is a self-service tool that provides accurate and explainable forecasts based on Machine Learning for any KPI.',
    tags: ['Frontend Reporting', 'Data Engineering', 'Data Pipeline', 'Data Science', 'Machine Learning', 'No / Low Code', 'Coding', 'Cloud', 'Onprem'],
    url: '/chronos',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableChronosForecastingService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'chronos',
    isDnAInternalTool: true,
  },
  {
    id: 'matomo',
    name: 'Matomo',
    description:
      'Matomo is a free open source web-analytics platform which will help in analyzing the website traffic, visitors.',
    tags: ['Onprem'],
    url: '/matomo',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableMatomoService,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'tools-mini',
    isDnAInternalTool: true,
  },
  {
    id: 'dataLakehouse',
    name: 'Data Lakehouse',
    description:
      'A data lakehouse is an open source data management architecture that combines the flexibility and cost-efficiency of data lakes with the data management and structure features of data warehouses, all on one data platform.',
    tags: ['Data Engineering', 'Data Science', 'No / Low Code', 'Cloud', 'Onprem'],
    url: '/datalake',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableDatalakeService,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'tools-mini',
    isDnAInternalTool: true,
  },
  {
    id: 'kubeflow',
    name: 'Kubeflow',
    description:
      'Kubeflow is a platform for data scientists who want to build and experiment with Machine Learning [ML] pipelines. Kubeflow is also for ML engineers and operational teams who want to deploy ML systems to various environments for development, testing, and production-level serving.',
    tags: ['Data Pipeline', 'Data Science', 'Machine Learning', 'FOSS', 'No / Low Code', 'Coding', 'Cloud', 'Onprem'],
    url: mLPipelineUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableMLPipelineService,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'kubeflow',
    isDnAInternalTool: true,
  },
  {
    id: 'dataikuDSS',
    name: 'Dataiku DSS',
    description:
      'Dataiku Data Science Studio is a low-code/ no-code data wrangling and machine learning platform. Typical use cases are data preparation, analysis and the development of machine learning models',
    tags: ['Frontend Reporting', 'Data Engineering', 'Data Pipeline', 'Data Science', 'Data Storage', 'Machine Learning', 'No / Low Code', 'Coding', 'Cloud', 'Onprem'],
    url: '/mydataiku',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableDataikuWorkspace,
    isDetailedPage: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataiku-new',
    isDnAInternalTool: true,
  },
  {
    id:'SAC',
    name: 'SAC',
    description:
      'SAP Analytics Cloud is an end-to-end cloud solution that brings together business intelligence, augmented analytics, and enterprise planning in a single system. SAC is the strategic Frontend tool to build dashboards (stories), explore data and build planning applications.',
    tags: ['Frontend Reporting', 'SAP', 'No / Low Code', 'Cloud'],
    url: sapAnalyticsUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableSapAnalyticsCloud,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sac',
    isDnAInternalTool: false,
  },
  {
    id: 'powerBI',
    name: 'Power BI',
    description:
      'Power BI Desktop is an application you install on your local computer that lets you connect to, transform, and visualize your data. With Power BI Desktop, you can connect to multiple different sources on data, and combine them into a data model. This data model lets you build visuals, and collections of visuals you can share as reports, with other people inside your organization. You get Power BI in the ITShop. Most users who work on business intelligence projects use Power BI Desktop to create reports, and then use the Power BI service called PBOS to share their reports with others.',
    tags: ['Frontend Reporting', 'No / Low Code', 'Onprem'],
    url: powerBIUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !powerBIUrl?.startsWith('http'),
    isDetailedPage: true,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'powerbi',
    isDnAInternalTool: false,
  },
  {
    id: 'malwareScan',
    name: 'Malware Scan',
    description:
      'Malware Scan as a Service is an API for scanning files for malicious code.',
    tags: ['FOSS'],
    url: '/malwarescanservice',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableMalwareScanService,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'malwarescan',
    isDnAInternalTool: true,
  },
  {
    id: 'malwareAzureBlobScanner',
    name: 'Malware Azure Blob Scanner',
    description:
      'A docker instance that can be configured to connect to an Azure Blob instance and enables anti malware detection on any files added to the storage.',
    tags: ['FOSS'],
    url: '/azureBlobService',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !clamavImagwUrl?.startsWith('reg'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'malwarescan',
    isDnAInternalTool: true,
   },
  {
    id:'storageBucket',
    name: 'Storage Bucket',
    description:
      'Storage Buckets provide data file storage that can be shared and connected to many tools. the storage buckets are based on a FOSS solution called minIO and provide a standard API for accessing file stores: S3.',
    tags: ['Data Storage', 'FOSS', 'Onprem'],
    url: '/storage',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableStorageService,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'storage',
    isDnAInternalTool: true,
  },
  {
    id: 'codeApaces',
    name: 'Code Spaces',
    description:
      'Code spaces provides developers with one click developer workspace customizable based on the technology used. Developers can collaborate on the team developing same solution but also deploy solution to different staging environments with click of the mouse.',
    tags: ['FOSS', 'Coding', 'Cloud', 'Onprem'],
    url: '/codespaces',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableCodeSpace,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'codespace',
    isDnAInternalTool: true,
  },
    {
    id: 'dataOasis',
    name: 'Data Oasis',
    description:
      'Digital Oasis is an intergrated data platform, which is to drive and support the FG Digital Transformation with 6 modules (Mall, Factory, Workflow, Logistics, Data Warehouse, College) for 6 transformation scenarios.',
    tags: ['Frontend Reporting', 'Data Storage', 'Onprem'],
    url: dataOasisUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !dataOasisUrl?.startsWith('http'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'data-oasis',
    isDnAInternalTool: false,
  },
  {
    id:'dataQ',
    name: 'DataQ',
    description:
      'DataQ is the intelligent IT tool for the simplified application of the Compliance Framework for Data Analytics and the AI Risk Self Assessments.',
    tags: ['Data Compliance', 'Onprem'],
    url: dataQToolUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !dataQToolUrl?.startsWith('http'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'tools-mini',
    isDnAInternalTool: false,
  },
  {
    id: 'jupyter',
    name: 'Jupyter',
    description:
      'Jupyter is a web-based interactive computational environment for creating data analysis in notebook like documents. At FC it is mainly used for Python Data Analysis.',
    tags: ['Data Science', 'Data Storage', 'FOSS', 'Coding', 'Cloud', 'Onprem'],
    url: '/notebook',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableJupiyterNoteWorkspace,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'jupyter',
    isDnAInternalTool: true,
  },
  {
    id: 'AFO',
    name: 'AFO',
    description:
      'SAP Analysis for Office (AfO) is an office add-in that enables multidimensional ad-hoc analyzes on OLAP data sources in Excel. In addition, the product enables workbook-based application design and the creation of BI presentations in PowerPoint. Connectivity to our SBISS platform is fully supported.',
    tags: ['Frontend Reporting', 'FOSS', 'SAP', 'No / Low Code', 'Onprem'],
    url: afoUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !afoUrl?.startsWith('http'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'afo',
    isDnAInternalTool: false,
  },
  {
    id: 'airflow',
    name: 'Airflow',
    description:
      'Apache Airflow is an open-source platform for developing, scheduling, and monitoring batch-oriented workflows. Airflow’s extensible Python framework enables you to build workflows connecting with virtually any technology. A web interface helps manage the state of your workflows. Airflow is deployable in many ways, varying from a single process on your laptop to a distributed setup to support even the biggest workflows.',
    tags: ['Data Engineering', 'Data Pipeline', 'FOSS', 'Coding', 'Cloud', 'Onprem'],
    url: '/pipeline',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableDataPipelineService,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'airflow',
    isDnAInternalTool: true,
  },
  {
    id: 'eXtollo',
    name: 'eXtollo',
    description: 'eXtollo helps Mercedes business units to perform advanced analytics and AI use cases on very large amounts of data (big data) with high flexibility. eXtollo\'s main building blocks are the Data Lake, which serves data from the whole company, and the eXtollo Instances, which provide an Azure-based toolset for Big Data and Machine Learning use cases.',
    tags: ['Frontend Reporting', 'Data Engineering', 'Data Pipeline', 'Data Science', 'Data Storage', 'Machine Learning', 'No / Low Code', 'Coding', 'Cloud'],
    url: extolloUrl
    ,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !extolloUrl?.startsWith('http'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'extollo',
    isDnAInternalTool: false,
  },
  {
    id:'BTP',
    name: 'BTP',
    description: 'Business Technology Platform is a set of data integration, analytics, AI, application development and automation tools in the cloud. The Platform is managed by the SBISS team.',
    tags: ['Frontend Reporting', 'Data Storage', 'SAP', 'No / Low Code', 'Coding', 'Cloud'],
    url: btpUrl
    ,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !btpUrl?.startsWith('http'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'btp',
    isDnAInternalTool: false,
  },
  {
    id: 'datasphere',
    name: 'Datasphere',
    description: 'SAP Datasphere is a cloud based data warehouse designed for self service data integration, modelling and analysis.It provides access to FC Data products and is connected to our SAP Analytics Cloud Frontend.',
    tags: ['Data Engineering', 'Data Storage', 'SAP', 'No / Low Code', 'Cloud'],
    url: datasphereUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !datasphereUrl?.startsWith('http'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sac',
    isDnAInternalTool: false,
  },
  {
    id: 'modelRegistry',
    name: 'Model Registry',
    description:
      'Model registry provides access to published models resulting out of kubeflow model development.',
    tags: ['Data Science', 'Machine Learning', 'FOSS', 'Cloud', 'Onprem'],
    url: '/modelregistry',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableMyModelRegistryService,
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'modelregistry',
    isDnAInternalTool: true,
  },
  {
    id: 'spire',
    name: 'Spire',
    description: 'Spire is the standardized & compliant global cloud analytics platform for MBM – live and running, along with a team of experts leveraging cutting-edge technology and end2end services to create business value.',
    tags: ['Data Engineering','Cloud', 'Machine Learning'],
    url: spireUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !spireUrl?.startsWith('http'),
    isDetailedPage: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'spire',
    isDnAInternalTool: false,
  },
];

export const TranparencyLandingPageElements = [
  {
    name: 'Portfolio',
    description:
      'All your solutions at a glance and visualized for steering.',
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
      'Central place to search, find and create all MB Data & Analytics Solutions.',
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
      'Full transparency about reports from various sources. Process of collection to be defined.',
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
      'Leverage the value of Data by sharing them in a fully compliant way.',
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
      'More information and definitions of applications, key figures and data.',
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
      'Understanding & fulfilling ever more complex Legal & Compliance requirements can be challenging. Data Governance helps you to navigate through these requirements and supports on you data journey.',
    tags: ['Self Service', 'FOSS'],
    url: '/data/datagovernance',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIcon: 'datagovernance',
  },
  {
    name: 'Data Catalog (OpenMetadata)',
    description:
      'Unlock the value of data assets with an end-to-end metadata management solution that includes data discovery, governance, data quality, observability, and people collaboration.',
    tags: ['Self Service', 'FOSS'],
    url: dataCatalogUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !dataCatalogUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIcon: 'data-mini',
  },
];

export const TrainingsLandingPageElements = [
  {
    name: 'Udemy',
    description:
      'If you\'re an IT employee or want to build IT knowledge, there are more than 18,000 courses available in Udemy Business. You can also orient yourself on specially defined learning paths for special skills and roles.',
    tags: ['Self Service', 'FOSS'],
    url: udemyUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'tools-mini',
  },
  {
    name: 'LinkedIn Learning',
    description:
      'The learning platform LinkedIn Learning enables employees as well as leaders to individualize their learning experience and become lifelong learners. Following the Turn2Learn idea of voluntary participation and self-motivation of all learners.',
    tags: ['Self Service', 'FOSS'],
    url: linkedinLearningUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'tools-mini',
  },
  {
    name: 'Dataiku',
    description:
      'To support your continuous learning and development on DSS, Dataiku provides a free online Academy, a safe place to learn at your own pace with detailed concept videos and step-by-step tutorials.',
    tags: ['Self Service', 'FOSS'],
    url: dataikuTrainingUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !dataikuTrainingUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'dataiku-new',
  },
  {
    name: 'PowerBI',
    description:
      'Learn new skills and discover the power of Microsoft Power BI with step-by-step guidance. Start your journey today by exploring the Microsoft learning paths and modules for free.',
    tags: ['Self Service', 'FOSS'],
    url: powerbiTrainingUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !powerbiTrainingUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'powerbi',
  },
  {
    name: 'SAC',
    description:
      'To support your continuous learning and development in the SAP Analytics Cloud, we offer SAC Basic Training Modules as web trainings and a self-service track with selected videos and screencasts of the SAC Basic Training Module content.',
    tags: ['Self Service', 'FOSS'],
    url: sacTrainingUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !sacTrainingUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'sac',
  },
  {
    name: 'Databricks',
    description:
      'On the Databricks Lakehouse Platform you have the opportunity to carry out tailor-made learning paths for different roles and career paths. These pathways include self-paced courses, instructor-led training, and certifications or accreditations where appropriate.',
    tags: ['Self Service', 'FOSS'],
    url: databricksTrainingUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !databricksTrainingUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'tools-mini',
  },
  {
    name: 'Digital College',
    description:
      'The Digital College is available to all colleagues in Finance & Controlling worldwide and helps us to build the right skills for digitization.',
    tags: ['Self Service', 'FOSS'],
    url: digitalCaseProgramUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !digitalCaseProgramUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'tools-mini',
  },
  {
    name: 'SAP Datasphere',
    description:
      'Here you can find all relevant Information on available SAP Datasphere Trainings. So get started or advance your skills!',
    tags: ['Self Service', 'FOSS'],
    url: datasphereTrainingUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !datasphereTrainingUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'sac',
  },
];

export const GenAILandingPageElements = [
  {
    name: 'Create GenAI Solution',
    description:
      'Create new GenAI solutions which answer business questions and create their plannings or reports.',
    tags: ['Self Service', 'FOSS'],
    url: '/createnewgenaisolution',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'genai-create',
  },
  {
    name: 'GenAI Solutions',
    description:
      'GenAI Solutions are at the heart of our landscape and provide data and functions to business users to answer their questions and create their plannings or reports.',
    tags: ['Self Service', 'FOSS'],
    url: '/viewsolutions/tag/GenAI',
    isExternalLink: false,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'genai-solutions',
  },
  {
    name: 'Mercedes-Benz Direct Chat',
    description:
      'Direct Chat - Your new AI assistant for more efficiency at work.',
    tags: ['Self Service', 'FOSS'],
    url: genAIDirectChatUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !genAIDirectChatUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'genai-direct-chat',
  }
];
