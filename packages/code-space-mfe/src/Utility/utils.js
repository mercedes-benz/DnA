import { Envs } from './envs';
import { PRIVATE_RECIPES } from './constants';
import { matchPath } from 'react-router';
import { routes } from '../components/CodeSpaceRoutes';

export const getParams = () => {
  for (const route of routes) {
    const match = matchPath(window.location.hash, {
      path: `#/codespaces${route.path}`,
    });
    if (match && match.isExact) {
      return match.params;
    }
  }
};

export const getQueryParam = (paramName) => {
  const hash = window.location.hash.slice(1);
  const queryString = hash.replace(/\?/g, '&');
  const params = new URLSearchParams(queryString);
  return params.get(paramName);
}

export const getPath = () => {
  return window.location.hash;
};

export const trackEvent = (category, action, name, value = false) => { //value optional
    // For tracking event in matamo
    if (window._paq) {
      const eventArr = ['trackEvent', category, action, name];
      if (value) {
        eventArr.push(value);
      }
      window._paq.push(eventArr);
    }
};

export const buildGitJobLogViewURL = (gitJobRunId) => {
    try {
      return Envs.CODESPACE_OPENSEARCH_BUILD_LOGS_URL.replaceAll('$INSTANCE_ID$', gitJobRunId);
    } catch {
      return "Error in building git job log view Url. Please check the git job run id."
    }
};

export const buildGitJobLogViewAWSURL = (gitJobRunId) => {
  try {
    return Envs.CODESPACE_AWS_OPENSEARCH_BUILD_LOGS_URL.replaceAll('$INSTANCE_ID$', gitJobRunId);
  } catch {
    return "Error in building git job log view Url. Please check the git job run id."
  }
};

export const buildGitUrl = (gitRepoInfo) => {
    if (gitRepoInfo.includes('.git')) return gitRepoInfo.split(',')[0];
    return Envs.CODE_SPACE_GIT_PAT_APP_URL + Envs.CODE_SPACE_GIT_ORG_NAME + '/' + gitRepoInfo;
};

const isValidURL = (urlString) => {
  try {
    new URL(urlString);
    return true;
  } catch (err) {
    return false;
  }
}

export const buildLogViewURL = (deployedInstance, isStagging = false) => { //isstagingOptional
    try {
      let instanceId = deployedInstance;
      if(isValidURL(deployedInstance)) {
        instanceId = new URL(deployedInstance).pathname.split("/")[1];
      }
      return Envs.CODESPACE_OPENSEARCH_LOGS_URL.replaceAll('$INSTANCE_ID$', instanceId + (isStagging ? '-int' : '-prod'));
    } catch {
      return "Error in building log view Url. Please check the deployment Url."
    }
};
export const buildLogViewAWSURL = (deployedInstance, isStagging = false) => { //isstagingOptional
  try {
    let instanceId = deployedInstance;
    if(isValidURL(deployedInstance)) {
      instanceId = new URL(deployedInstance).pathname.split("/")[1];
    }
    return Envs.CODESPACE_AWS_OPENSEARCH_LOGS_URL.replaceAll('$INSTANCE_ID$', instanceId + (isStagging ? '-int' : '-prod'));
  } catch {
    return "Error in building log view Url. Please check the deployment Url."
  }
};
export const isValidGitUrl = (str) => {
  const privateHost = new URL(Envs.CODE_SPACE_GIT_PAT_APP_URL).host;
  const regex = new RegExp(`((http|https)?:\\/\\/)?(?:github.com|${privateHost})\\/([\\w.@:/\\-~]+)(\\.git)`);
  return (str == null) ? false : regex.test(str);
};

export const isValidGITRepoUrl = (str, isPublicRecipeChoosen) => {
    const privateHost = new URL(Envs.CODE_SPACE_GIT_PAT_APP_URL).host;
    const regex = new RegExp('((http|http(s)|\\/?))(:(\\/\\/' + (isPublicRecipeChoosen ? 'github.com'  : privateHost) + '\\/))([\\w.@:/\\-~]+)(\\.git)(\\/)?');
  
    return (str == null) ? false : regex.test(str);
};

export const regionalDateAndTimeConversionSolution = (dateString) => { 
    // const newDateString = dateString.split(/-| /);   
    // const dateUTC = newDateString[2]+'-'+newDateString[1]+'-'+newDateString[0]+'T'+newDateString[3]+'Z';
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat(navigator.language, {
        year: 'numeric', month: 'numeric', day: 'numeric',
        hour: 'numeric', minute: 'numeric', second: 'numeric',
        hour12: false,
      }).format(date);
    } catch {
      return 'Invalid Time Value'
    }
};

export const recipesMaster = [

    { id: 'default', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Plain or Empty (Debian 11 OS, 2GB RAM, 1CPU)', repodetails: '' },
  
    { id: 'private-user-defined', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: `Recipe from Private Github(${Envs.CODE_SPACE_GIT_PAT_APP_URL}) (Debian 11 OS, 6GB RAM, 2CPU)`, repodetails: '' },
  
    { id: 'public-user-defined', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'Recipe from Public Github(https://github.com/) (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: '' },
    
    { id: 'springboot', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using Spring Boot (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'quarkus', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using QUARKUS (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'micronaut', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using MICRONAUT (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'py-fastapi', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using Python FastAPI (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'dash', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Dash Python (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'streamlit', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Streamlit Python (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'expressjs', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using Express - Node.js (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'nestjs', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using NestJS - Node.js (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'react', resource: '4Gi,2000Mi,500m,4000Mi,1000m', name: 'React SPA (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'angular', resource: '4Gi,2000Mi,500m,4000Mi,1000m', name: 'Angular SPA (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'vuejs', resource: '4Gi,2000Mi,500m,4000Mi,1000m', name: 'Vue3 Webpack SPA (Debian 11 OS, 2GB RAM, 1CPU)' },
        
    { id: 'public-dna-frontend', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/frontend/*' },
    { id: 'public-dna-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/backend/*' },
    { id: 'public-dna-report-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Report Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/dashboard-backend/*' },
    { id: 'public-dna-codespace-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Code Space Backend (Debian 11 OS, 2GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/code-server/*' },
    { id: 'public-dna-code-space-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Code Space Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/code-space-mfe/*' },
    { id: 'public-dna-malware-scanner', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Malware Scanner (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/malware-scanner/*' },
    { id: 'public-dna-storage-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Storage Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/storage-mfe/*' },
    { id: 'public-dna-storage-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Storage Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/storage-backend/*' },
    { id: 'public-dna-chronos-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Chronos Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/chronos-mfe/*' },
    { id: 'public-dna-chronos-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Chronos Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/chronos/*' },
    { id: 'public-dna-data-product-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Data Product Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/data-product-mfe/*' },
    { id: 'public-dna-data-product-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Data Product Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/data-product-backend/*' },
    { id: 'public-dna-dss-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA DSS Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/dss-mfe/*' },
    { id: 'public-dna-dataiku-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA DSS Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/dataiku-backend/*' },
    { id: 'public-dna-airflow-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Airflow Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/airflow-backend/*' },
    { id: 'public-dna-modal-registry-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Modal Registry Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/model-registry/*' },
    { id: 'public-dna-trino-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Trino Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/trino-backend/*' },
    { id: 'public-dna-nass', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Notification Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/naas/*' },
    { id: 'public-dna-authenticator-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Authenticator Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/authenticator-service/*' },
    { id: 'public-dna-matomo-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Matomo Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/matomo-mfe/*' },
    { id: 'public-dna-matomo-backend', resource: '4Gi,3000Mi,1500m,5000Mi,2000m', name: 'DnA Matomo Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/matomo-backend/*' },
    { id: 'public-dna-datalake-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Data Lakehouse Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/datalake-mfe/*' },
    { id: 'public-dna-fabric-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Fabric Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/fabric-mfe/*' },
    { id: 'public-dna-fabric-backend', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Fabric Backend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/fabric-backend/*' },
    { id: 'public-dna-dataentry-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Data Entry as a Service Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/dataentry-mfe/*' },
    // { id: 'public-dna-dataentry-backend', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Data Entry as a Service Backend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/dataentry-backend/*' },
  
    ...PRIVATE_RECIPES,
  
    { id: 'chronos', name: 'CHRONOS Workspace (Coming Soon)' },
    { id: 'mean', name: 'MEAN Stack (Coming Soon)' },
    { id: 'mern', name: 'MERN Stack (Coming Soon)' },
];