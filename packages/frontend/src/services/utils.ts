import { Envs } from '../globals/Envs';
import { IUserInfo } from '../globals/types';

declare global {
  interface Window {
    _paq?: any;
  }
}

const baseUrl = Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:3000/api`;

export const createQueryParams = (obj: any): string => {
  const str = [];
  for (const p in obj) {
    if (obj.hasOwnProperty(p)) {
      str.push(p + '=' + obj[p]);
    }
  }
  return str.join('&');
};

export const getImageUrlById = (imageId: string): string => {
  if (!imageId) {
    return '';
  }

  return `${baseUrl}/images/${imageId}`;
};

export const getFullNameOfUser = (user: IUserInfo) => {
  if (!user) {
    return '';
  }

  if (user.firstName && user.lastName) {
    return `${user.lastName}, ${user.firstName}`;
  }

  if (user.firstName || user.lastName) {
    return `${user.firstName || user.lastName}`;
  } else {
    if (user.id) {
      return user.id;
    }
    return '';
  }
};
// Removing uery parameter from an current URL code and state params are hanled here //
export const removeURLParameter = (url: string, paramKey: string) => {
  const r = new URL(url);
  r.searchParams.delete(paramKey);
  return r.href;
};

// Format big number value to as K, M, B for display
export const DataFormater = (value: number) => {
  const sign = Math.sign(Number(value));
  // Nine Zeroes for Billions
  
  let formatedValue = (
    Math.abs(Number(value)) >= 1.0e9
      ? new Intl.NumberFormat(navigator.language).format(Number((sign * (Math.abs(Number(value)) / 1.0e9)).toFixed(2))) + 'B'
      : // Six Zeroes for Millions
      Math.abs(Number(value)) >= 1.0e6
      ? new Intl.NumberFormat(navigator.language).format(Number((sign * (Math.abs(Number(value)) / 1.0e6)).toFixed(2))) + 'M'
      : // Three Zeroes for Thousands
      Math.abs(Number(value)) >= 1.0e3
      ? new Intl.NumberFormat(navigator.language).format(Number((sign * (Math.abs(Number(value)) / 1.0e3)).toFixed(2))) + 'K'
      : Math.abs(Number(value)).toFixed(2)
  ).replace('.00', '');

  if (formatedValue.includes('.')) {
    formatedValue = formatedValue.replace('0B', 'B').replace('0M', 'M').replace('0K', 'K');
  }

  return formatedValue;
};

export const attachEllipsis = (value: string, length: number) => {
  return value?.length >= length ? value.substring(0, length) + '...' : value;
};

export const trackPageView = (url: string, title: string, userId: string) => {
  // For tracking page view in matamo
  if (window._paq) {
    window._paq.push(['setCustomUrl', '/hash' + url]);
    window._paq.push(['setDocumentTitle', title]);
    window._paq.push(['setUserId', userId]);
    window._paq.push(['trackPageView']);
    window._paq.push(['enableLinkTracking']);
  }
};

export const trackEvent = (category: string, action: string, name: string, value?: string) => {
  // For tracking event in matamo
  if (window._paq) {
    const eventArr = ['trackEvent', category, action, name];
    if (value) {
      eventArr.push(value);
    }
    window._paq.push(eventArr);
  }
};

export const getDateFromTimestamp = (givenDate: string, seperator?: string) => {
  const d = new Date(givenDate);
  return regionalDateAndTimeConversionSolution(d);
  // const td = new Date((d.getTime() + (-d.getTimezoneOffset() * 60000)));
  // const sep = seperator || '-';
  // return td.getUTCDate() + sep + (td.getUTCMonth() + 1) + sep + td.getUTCFullYear();
};

export const getDateFromTimestampForDifference = (givenDate: string, seperator?: string) => {
  const d = (new Date(givenDate)).toUTCString();
  return d;
};

export const getDateTimeFromTimestamp = (givenDate: string, seperator?: string) => {
  const d = new Date(givenDate);
  return regionalDateAndTimeConversionSolution(d);
  // const td = new Date((d.getTime() + (-d.getTimezoneOffset() * 60000)));
  // const time = td.getUTCHours();
  // const mins = td.getUTCMinutes();
  // return (
  //   getDateFromTimestamp(givenDate, seperator) +
  //   ' at ' +
  //   (time < 10 ? '0' : '') +
  //   time +
  //   ':' +
  //   (mins < 10 ? '0' : '') +
  //   mins
  // );
};

export const getDateDifferenceFromToday = (dateFrom: string) => {
  const date1 = new Date(dateFrom);
  const date2 = new Date();
  const diffTime = Math.abs(date2.getTime() - date1.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 3600 * 24));
  return diffDays;
};

export const getDateDifferenceFromTodayUsingGetDate = (dateFrom: string) => {
  const now = new Date().getTime();
  const dateF = new Date(dateFrom).getTime();
  const diff = Math.abs(now - dateF);
  const diffDays = Math.ceil(diff / (1000 * 60 * 60 * 24)) - 1;
  return diffDays;
};

export const getDateAfterSomeDays = (noOfDays: number) => {
  const someDate = new Date();
  const numberOfDaysToAdd = noOfDays;
  return regionalDateAndTimeConversionSolution(someDate.setDate(someDate.getDate() + numberOfDaysToAdd));
  // const dd = someDate.getDate();
  // const mm = someDate.getMonth() + 1;
  // const y = someDate.getFullYear();

  // const someFormattedDate = dd + '-' + mm + '-' + y;
  // return someFormattedDate;
};

export const convertTextToLink = (text: string, env: string) => {
  if (text.lastIndexOf('[') === -1 && text.lastIndexOf(']') === -1) {
    return text;
  }
  const tempArray = [{ data: '', link: '' }];
  const a = '](';
  let linkData = '';
  let getLinkFromString = '';
  text.split(a).forEach((item) => {
    if (item.includes('[')) {
      if (item.includes('[') && item.includes('/)')) {
        tempArray.push({ data: item.split('[')[1], link: item.split('/)')[0] });
      } else {
        linkData = item.split('[')[1];
      }
    } else if (item.includes('/)')) {
      getLinkFromString = item.split('/)')[0];
      tempArray.push({ data: linkData, link: getLinkFromString });
    }
  });
  tempArray.forEach((item) => {
    const searchString = '[' + item.data + '](' + item.link + '/)';
    const formattedURL =
      (env === 'production' ? Envs.DATAIKU_LIVE_APP_URL : Envs.DATAIKU_TRAINING_APP_URL) + '/' + item.link;
    const anchor = `<a href=${formattedURL} target="_blank">${item.data}</a>`;
    text = text.replace(searchString, anchor);
  });
  return text;
};

export const getDivisionsQueryValue = (divisions: string[], subDivisions: string[]) => {
  let divisionIds = divisions.join(',');
  if (divisions.length > 0) {
    const distinctSelectedDivisions = divisions;
    const tempArr: any[] = [];
    distinctSelectedDivisions.forEach((item) => {
      const tempString = '{' + item + ',[]}';
      tempArr.push(tempString);
    });
    divisionIds = JSON.stringify(tempArr).replace(/['"]+/g, '');
  }

  if (subDivisions.length > 0) {
    const distinctSelectedDivisions = divisions;
    const tempArr: any[] = [];
    let hasEmpty = false; // To find none selected in sub division since its not mandatory
    const emptySubDivId = 'EMPTY';
    distinctSelectedDivisions.forEach((item) => {
      const tempSubdiv = subDivisions.map((value) => {
        const tempArray = value.split('@-@');
        const subDivId = tempArray[0];
        if (subDivId === emptySubDivId) {
          hasEmpty = true;
        }
        if (item === tempArray[1]) {
          return subDivId;
        }
      });

      if (hasEmpty && !tempSubdiv.includes(emptySubDivId)) {
        tempSubdiv.unshift(emptySubDivId);
      }

      let tempString = '';

      if (tempSubdiv.length === 0) {
        tempString += '{' + item + ',[]}';
      } else {
        tempString += '{' + item + ',[' + tempSubdiv.filter((div) => div) + ']}';
      }

      tempArr.push(tempString);
    });
    divisionIds = JSON.stringify(tempArr).replace(/['"]+/g, '');
  }
  if (divisions.length === 0) {
    divisionIds = '';
  }  
  return divisionIds;
};

export const regionalDateAndTimeConversion = (dateString: any) => { 
  // const newDateString = dateString.split(/-| /);   
  // const dateUTC = newDateString[2]+'-'+newDateString[1]+'-'+newDateString[0]+'T'+newDateString[3]+'Z';
  
  const date = new Date(dateString);
  return new Intl.DateTimeFormat(navigator.language,{
    year: 'numeric', month: 'numeric', day: 'numeric',
    hour: 'numeric', minute: 'numeric', second: 'numeric',
    hour12: false,
  }).format(date);
};

export const regionalDateAndTimeConversionSolution = (dateString: any) => { 
  // const newDateString = dateString.split(/-| /);   
  // const dateUTC = newDateString[2]+'-'+newDateString[1]+'-'+newDateString[0]+'T'+newDateString[3]+'Z';
  
  const date = new Date(dateString);
  return new Intl.DateTimeFormat(navigator.language,{
    year: 'numeric', month: 'numeric', day: 'numeric',
    hour: 'numeric', minute: 'numeric', second: 'numeric',
    hour12: false,
  }).format(date);
};

export const regionalForMonthAndYear = (dateString: any) => { 
   const date = new Date(dateString);
  return new Intl.DateTimeFormat(navigator.language,{
    year: 'numeric', month: 'numeric'
  }).format(date);
};

export const thousandSeparator = (region: string) => {
  if(region.includes('de') || region.includes('it') || region.includes('es'))
    return  "."
  else if(region.includes('fr') || region.includes('fi') || region.includes('da') || region.includes('sv')) 
    return " " 
  else  
    return ","
}

export const decimalSeparator = (region: string) => {
  if(region.includes('de') || region.includes('it') || region.includes('es'))
    return  ","
  else if(region.includes('fr') || region.includes('fi') || region.includes('da') || region.includes('sv')) 
    return "," 
  else  
    return "."
}

export const csvSeparator = (region: string) => {
  if(region.includes('de'))
    return  ";" 
  
}

export const recipesMaster = [
  { id: 'default', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Plain or Empty (Debian 11 OS, 2GB RAM, 1CPU)', repodetails: '' },
  { id: 'springboot', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using Spring Boot (Debian 11 OS, 2GB RAM, 1CPU)' },
  { id: 'py-fastapi', resource: '2Gi,1000Mi,500m,2000Mi,1000m', name: 'Microservice using Python FastAPI (Debian 11 OS, 2GB RAM, 1CPU)' },
  { id: 'react', resource: '4Gi,2000Mi,500m,4000Mi,1000m', name: 'React SPA (Debian 11 OS, 2GB RAM, 1CPU)' },
  { id: 'angular', resource: '4Gi,2000Mi,500m,4000Mi,1000m', name: 'Angular SPA (Debian 11 OS, 2GB RAM, 1CPU)' },

  { id: 'public-dna-frontend', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/frontend/*' },
  { id: 'public-dna-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/backend/*' },
  { id: 'public-dna-report-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Report Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/dashboard-backend/*' },
  { id: 'public-dna-codespace-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Code Space Backend (Debian 11 OS, 2GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/code-server/*' },
  { id: 'public-dna-malware-scanner', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Malware Scanner (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/malware-scanner/*' },
  { id: 'public-dna-storage-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Storage Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/storage-mfe/*' },
  { id: 'public-dna-storage-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Storage Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/storage-backend/*' },
  { id: 'public-dna-chronos-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Chronos Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/chronos-mfe/*' },
  { id: 'public-dna-chronos-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Chronos Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/chronos/*' },
  { id: 'public-dna-data-product-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA Data Product Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/data-product-mfe/*' },
  { id: 'public-dna-data-product-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Data Product Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/data-product-backend/*' },
  { id: 'public-dna-dss-mfe', resource: '4Gi,4000Mi,1000m,6000Mi,2000m', name: 'DnA DSS Micro Frontend (Debian 11 OS, 6GB RAM, 2CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/data-dss-mfe/*' },
  { id: 'public-dna-dataiku-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA DSS Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/dataiku-backend/*' },
  { id: 'public-dna-airflow-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Airflow Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/airflow-backend/*' },
  { id: 'public-dna-modal-registry-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Modal Registry Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/model-registry/*' },
  { id: 'public-dna-trino-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Trino Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/trino-backend/*' },
  { id: 'public-dna-nass', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Notification Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/naas/*' },
  { id: 'public-dna-authenticator-backend', resource: '2Gi,2000Mi,500m,4000Mi,1000m', name: 'DnA Authenticator Backend (Debian 11 OS, 4GB RAM, 1CPU)', repodetails: 'github.com/mercedes-benz/DnA.git,packages/authenticator-service/*' },



  { id: 'chronos', name: 'CHRONOS Workspace (Coming Soon)' },
  { id: 'mean', name: 'MEAN Stack (Coming Soon)' },
  { id: 'mern', name: 'MERN Stack (Coming Soon)' },
];