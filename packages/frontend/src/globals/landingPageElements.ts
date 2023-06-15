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
const udemyUrl = Envs.UDEMY_URL;
const linkedinLearningUrl = Envs.LINKEDIN_LEARNING_URL;
const dataikuTrainingUrl = Envs.DATAIKU_TRAINING_URL;
const powerbiTrainingUrl = Envs.POWERBI_TRAINING_URL;
const sacTrainingUrl = Envs.SAC_TRAINING_URL;
const databricksTrainingUrl = Envs.DATABRICKS_TRAINING_URL;
const digitalCaseProgramUrl = Envs.DIGITAL_CASE_PROGRAM_URL;
const afoUrl= Envs.AFO_TOOL_URL;
const btpUrl = Envs.BPT_TOOL_URL;
const dataOasisUrl = Envs.DATA_OASIS_TOOL_URL;
const dataQToolUrl = Envs.DATAQ_TOOL_URL;
const datasphereUrl = Envs.DATASPHERE_TOOL_URL;
const extolloUrl = Envs.EXTOLLO_TOOL_URL;
const powerBIUrl = Envs.POWER_BI_TOOL_URL;
const sbissUrl = Envs.SBISS_LAUNCHPAD_TOOL_URL;
const sbissPortalUrl = Envs.SBISS_PORTAL_TOOL_URL;
const sbissHanaUrl = Envs.SBISS_HANA_LAUNCHPAD_TOOL_URL;
const dataModelUrl = Envs.DATA_MODEL_URL;
const kpiWikiUrl = Envs.KPI_WIKI_URL;
// const carlaEconomicModelUrl = Envs.CARLA_ECONOMIC_MODEL_URL;
const corporateDataCatalogUrl = Envs.CORPORATE_DATA_CATALOG_URL;
const sapConnectionBookUrl = Envs.SAP_CONNECTION_BOOK_URL;
const smartDataGovernanceUrl = Envs.SMART_DATA_GOVERNANCE_URL;
const transactionalDataUrl = Envs.TRANSACTIONAL_DATA_URL;
const carlaArchitectureUrl = Envs.CARLA_ARCHITECTURE_URL;
const chatbotUrl = Envs.GPT4ALL_CHATBOT_TOOL_URL;

export const DataLayerElements = [
  {
    name: 'Data Model',
    description:
      'The Data Model shows the most important Master data dimensions and how they relate to each other.',
    tags: ['Self Service', 'FOSS'],
    url: dataModelUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'KPI Wiki',
    description:
      'Find approved definitions of the most commonly used KPIs.',
    tags: ['Self Service', 'FOSS'],
    url: kpiWikiUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  // {
  //   name: 'CarLA Economic Model',
  //   description:
  //     'The CarLA Economic Model is the orientation guide for all functions of the CarLA. This CarLA-City-Map gives you a first overview about the functions of the CarLA, which are clustered in 5 focus areas.',
  //   tags: ['Self Service', 'FOSS'],
  //   url: carlaEconomicModelUrl,
  //   isExternalLink: true,
  //   isTextAlignLeft: false,
  //   isDisabled: false,
  //   isSmallCard: false,
  //   isMediumCard: true,
  //   svgIcon: 'dataproduct',
  // },
  {
    name: 'Corporate Data Catalogue',
    description:
      'Catalog of Mercedes-Benz data assets. If you never logged in the CDC before, a user will be generated for your this can take a while. CarLA systems can be found by searching for CarLA.',
    tags: ['Self Service', 'FOSS'],
    url: corporateDataCatalogUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataproduct',
  },
  {
    name: 'SAP Connection Book',
    description:
      'List of all source systems connected to the CarLA Core Datawarehouse and the names of the targets where the data from the source system is written to.',
    tags: ['Self Service', 'FOSS'],
    url: sapConnectionBookUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: true,
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
    isDisabled: false,
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
    isDisabled: false,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'datagovernancesocialintranet',
  },
  {
    name: 'Minimum Information',
    description:
      'Sharing Data? Fill out the required Minimum Information (Policy A22) to let the receiving side know of any restriction when using the data.',
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
      'The Local Compliance Officers/Responsibles are your primary contacts on all compliance topics.',
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
    tags: ['Frontend Reporting', 'Data Engineering', 'Data Pipeline', 'Data Science', 'Machine Learning', 'No / Low Code', 'Coding', 'Cloud', 'Onprem'],
    url: '/chronos',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableChronosForecastingService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'chronos',
  },
  {
    name: 'Kubeflow',
    description:
      'Kubeflow is a platform for data scientists who want to build and experiment with Machine Learning [ML] pipelines. Kubeflow is also for ML engineers and operational teams who want to deploy ML systems to various environments for development, testing, and production-level serving.',
    tags: ['Data Pipeline', 'Data Science', 'Machine Learning', 'FOSS', 'No / Low Code', 'Coding', 'Cloud', 'Onprem'],
    url: mLPipelineUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableMLPipelineService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'kubeflow',
  },
  {
    name: 'Dataiku DSS',
    description:
      'Dataiku Data Science Studio is a low-code/ no-code data wrangling and machine learning platform. Typical use cases are data preparation, analysis and the development of machine learning models',
    tags: ['Frontend Reporting', 'Data Engineering', 'Data Pipeline', 'Data Science', 'Data Storage', 'Machine Learning', 'No / Low Code', 'Coding', 'Cloud', 'Onprem'],
    url: '/mydataiku',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableDataikuWorkspace,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'dataiku-new',
  },
  {
    name: 'SAC',
    description:
      'SAP Analytics Cloud is an end-to-end cloud solution that brings together business intelligence, augmented analytics, and enterprise planning in a single system. SAC is the strategic Frontend tool to build dashboards (stories), explore data and build planning applications.',
    tags: ['Frontend Reporting', 'SAP', 'No / Low Code', 'Cloud'],
    url: sapAnalyticsUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableSapAnalyticsCloud,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sac',
  },
  {
    name: 'Power BI',
    description:
      'Power BI Desktop is an application you install on your local computer that lets you connect to, transform, and visualize your data. With Power BI Desktop, you can connect to multiple different sources on data, and combine them into a data model. This data model lets you build visuals, and collections of visuals you can share as reports, with other people inside your organization. You get Power BI in the ITShop. Most users who work on business intelligence projects use Power BI Desktop to create reports, and then use the Power BI service called PBOS to share their reports with others.',
    tags: ['Frontend Reporting', 'No / Low Code', 'Onprem'],
    url: powerBIUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !powerBIUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'powerbi',
  },
  {
    name: 'Malware Scan',
    description:
      'Malware Scan as a Service is an API for scanning files for malicious code.',
    tags: ['FOSS'],
    url: '/malwarescanservice',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableMalwareScanService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'malwarescan',
  },
  {
    name: 'Storage Bucket',
    description:
      'Storage Buckets provide data file storage that can be shared and connected to many tools. the storage buckets are based on a FOSS solution called minIO and provide a standard API for accessing file stores: S3.',
    tags: ['Data Storage', 'FOSS', 'Onprem'],
    url: '/storage',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableStorageService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'storage',
  },
  {
    name: 'Code Spaces',
    description:
      'Code spaces provides developers with one click developer workspace customizable based on the technology used. Developers can collaborate on the team developing same solution but also deploy solution to different staging environments with click of the mouse.',
    tags: ['FOSS', 'Coding', 'Cloud', 'Onprem'],
    url: '/codespaces',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableCodeSpace,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'codespace',
  },
  {
    name: 'Data Oasis',
    description:
      'Digital Oasis is an intergrated data platform, which is to drive and support the FG Digital Transformation with 6 modules (Mall, Factory, Workflow, Logistics, Data Warehouse, College) for 6 transformation scenarios.',
    tags: ['Frontend Reporting', 'Data Storage', 'Onprem'],
    url: dataOasisUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !dataOasisUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'data-oasis',
  },
  {
    name: 'DataQ',
    description:
      'DataQ is the intelligent IT tool for the simplified application of the Compliance Framework for Data Analytics and the AI Risk Self Assessments.',
    tags: ['Data Compliance', 'Onprem'],
    url: dataQToolUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !dataQToolUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'tools-mini',
  },
  {
    name: 'Jupyter',
    description:
      'Jupyter is a web-based interactive computational environment for creating data analysis in notebook like documents. At FC it is mainly used for Python Data Analysis.',
    tags: ['Data Science', 'Data Storage', 'FOSS', 'Coding', 'Cloud', 'Onprem'],
    url: '/notebook',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableJupiyterNoteWorkspace,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'jupyter',
  },
  {
    name: 'AFO',
    description:
      'SAP Analysis for Office (AfO) is an office add-in that enables multidimensional ad-hoc analyzes on OLAP data sources in Excel. In addition, the product enables workbook-based application design and the creation of BI presentations in PowerPoint. Connectivity to our SBISS platform is fully supported.',
    tags: ['Frontend Reporting', 'FOSS', 'SAP', 'No / Low Code', 'Onprem'],
    url: afoUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !afoUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'afo',
  },
  {
    name: 'Airflow',
    description:
      'Apache Airflow is an open-source platform for developing, scheduling, and monitoring batch-oriented workflows. Airflow’s extensible Python framework enables you to build workflows connecting with virtually any technology. A web interface helps manage the state of your workflows. Airflow is deployable in many ways, varying from a single process on your laptop to a distributed setup to support even the biggest workflows.',
    tags: ['Data Engineering', 'Data Pipeline', 'FOSS', 'Coding', 'Cloud', 'Onprem'],
    url: '/pipeline',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableDataPipelineService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'airflow',
  },
  {
    name: 'eXtollo',
    description: 'eXtollo helps Mercedes business units to perform advanced analytics and AI use cases on very large amounts of data (big data) with high flexibility. eXtollo\'s main building blocks are the Data Lake, which serves data from the whole company, and the eXtollo Instances, which provide an Azure-based toolset for Big Data and Machine Learning use cases.',
    tags: ['Frontend Reporting', 'Data Engineering', 'Data Pipeline', 'Data Science', 'Data Storage', 'Machine Learning', 'No / Low Code', 'Coding', 'Cloud'],
    url: extolloUrl
    ,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !extolloUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'extollo',
  },
  {
    name: 'BTP',
    description: 'Business Technology Platform is a set of data integration, analytics, AI, application development and automation tools in the cloud. The Platform is managed by the SBISS team.',
    tags: ['Frontend Reporting', 'Data Storage', 'SAP', 'No / Low Code', 'Coding', 'Cloud'],
    url: btpUrl
    ,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !btpUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'btp',
  },
  {
    name: 'Datasphere',
    description: 'SAP Datasphere is a cloud based data warehouse designed for self service data integration, modelling and analysis.It provides access to FC Data products and is connected to our SAP Analytics Cloud Frontend.',
    tags: ['Data Engineering', 'Data Storage', 'SAP', 'No / Low Code', 'Cloud'],
    url: datasphereUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !datasphereUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'tools-mini',
  },
  {
    name: 'SBISS4',
    description: 'The SBISS/4 Launchpad is the entry Point to access Finance Functions for MBC based on CarLA as well as functions on Corporate FC DWH.',
    tags: ['Frontend Reporting', 'SAP', 'Onprem'],
    url: sbissUrl
    ,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !sbissUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sbiss',
  },
  {
    name: 'SBISS Portal',
    description: 'The SBISS Portal is the singe point of entry for endusers accessing applications based on SBISSonHANA',
    tags: ['Frontend Reporting', 'SAP', 'Onprem'],
    url: sbissPortalUrl
    ,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !sbissPortalUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sbiss',
  },
  {
    name: 'SBISSonHANA Launchpad',
    description: 'SBISSonHANA Launchpad is the central entry point to all applications based on the BW-Platform.',
    tags: ['SAP', 'Onprem'],
    url: sbissHanaUrl
    ,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !sbissHanaUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'sbiss',
  },
  {
    name: 'Model Registry',
    description:
      'Model registry provides access to published models resulting out of kubeflow model development.',
    tags: ['Data Science', 'Machine Learning', 'FOSS', 'Cloud', 'Onprem'],
    url: '/modelregistry',
    isExternalLink: false,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !enableMyModelRegistryService,
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'modelregistry',
  },
  {
    name: 'Chat GPT4All',
    description: 'This is an earlybird release of a chatbot based on GPT4 LLM.',
    tags: ['Data Engineering', 'No / Low Code', 'Cloud', 'Machine Learning', 'FOSS', 'Onprem'],
    url: chatbotUrl,
    isExternalLink: true,
    isTextAlignLeft: true,
    animation: true,
    isDisabled: !chatbotUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: true,
    svgIcon: 'tools-mini',
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
      'Official information and definitions of CarLA applications, key figures and FC data.',
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
    isMediumCard: true,
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
    isMediumCard: true,
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
    isMediumCard: true,
    svgIconId: 'tools-mini',
  },
];

export const CarLALandingPageElements = [
  {
    name: 'Architecture',
    description:
      'Within our CarLA we use several governance tools to ensure that we achieve our goals of a redundancy free and efficient functions and data architecture for Finance and Controlling. An overview of the content and data flows can be found in the Economic Model CarLA.',
    tags: ['Self Service', 'FOSS'],
    url: carlaArchitectureUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !carlaArchitectureUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'architecture',
  },
  {
    name: 'Solutions',
    description:
      'CarLA Solutions are at the heart of our landscape and provide data and functions to business users to answer their questions and create their plannings or reports.',
    tags: ['Self Service', 'FOSS'],
    url: '/viewsolutions/tag/CarLA',
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
      'Data is the foundation for the CarLA solutions. Ready to share data products and data governance can be found here.',
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
      'A large part of the relevant data in Finance Controlling will be generated by OneERP. OneERP will be the single source of truth for actuals data for major plants. The methods and process for OneERP can be found in the Economic Model OneERP.',
    tags: ['Self Service', 'FOSS'],
    url: transactionalDataUrl,
    isExternalLink: true,
    isTextAlignLeft: false,
    isDisabled: !transactionalDataUrl?.startsWith('http'),
    isSmallCard: false,
    isMediumCard: false,
    svgIconId: 'transactionaldata',
  },
];