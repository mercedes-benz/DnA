/* tslint:disable:no-console */
import 'whatwg-fetch';
import { HTTP_METHOD, SESSION_STORAGE_KEYS } from '../globals/constants';
import { Envs } from '../globals/Envs';
import {
  IAlgorithm,
  IAppVersion,
  IAttachment,
  IBookMarks,
  IBookMarksResponse,
  ICreateNewSolution,
  ICreateNewReport,
  ICreateNewSolutionRequest,
  ICreateNewReportRequest,
  ICreateNewSolutionResult,
  ICreateNewReportResult,
  IDataSource,
  IDataSourceMaster,
  IError,
  IFitlerCategory,
  ILanguage,
  IPlatform,
  IRole,
  ISubDivision,
  ISubDivisionSolution,
  ITag,
  IUserInfo,
  IUserInfoResponse,
  IUserPreference,
  IUserPreferenceRequest,
  IUserRequestVO,
  IVisualization,
  IWidgetsResponse,
  INotebookInfo,
  INoticationModules,
  IManageDivision,
  IManageDivisionRequest,
  IManageMarketingTabRequest,
  IMarketingCommunicationChannel,
  IMarketingCustomerJourney,
  IAnalyticsSolution,
} from '../globals/types';
import { Pkce } from './Pkce';
import { ReportsApiClient } from './ReportsApiClient';
import { refreshToken } from '../../src/utils/RefreshToken';

export interface IResponse<T> {
  meta?: {
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  };
  data: T;
}

const baseUrl = Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`;
const dataikUrl = Envs.DATAIKU_API_BASEURL ? Envs.DATAIKU_API_BASEURL : `http://${window.location.hostname}:7777/api`;
const baseUrlSimilaritySearch = Envs.SIMILARITY_SEARCH_API_BASEURL ? Envs.SIMILARITY_SEARCH_API_BASEURL : `http://${window.location.hostname}:8000`;
const fabricUrl = Envs.FABRIC_API_BASEURL ? Envs.FABRIC_API_BASEURL :  `http://${window.location.hostname}:9292/api`;
const dataProductUrl = Envs.DATA_PRODUCT_API_BASEURL ? Envs.DATA_PRODUCT_API_BASEURL :  `http://${window.location.hostname}:7184/api`;

const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

const getDataikuUrl = (endpoint: string) => {
  return `${dataikUrl}/${endpoint}`;
};

const getFabricUrl = (endpoint: string) => {
  return `${fabricUrl}/${endpoint}`;
}

const getDataProductUrl = (endpoint: string) => {
  return `${dataProductUrl}/${endpoint}`;
}

const getSimilaritySearchUrl = (endpoint: string) => {
  return `${baseUrlSimilaritySearch}/${endpoint}`;
};

export class ApiClient {
  public static get(endpoint: string) {
    return this.fetch(getUrl(endpoint), HTTP_METHOD.GET);
  }

  public static post(endpoint: string, body?: any) {
    return this.fetch(getUrl(endpoint), HTTP_METHOD.POST, body);
  }

  public static fabricPost(endpoint: string, body?: any) {
    return this.fetch(getFabricUrl(endpoint), HTTP_METHOD.POST, body);
  }
  
  public static fabricGet(endpoint: string, body?: any) {
    return this.fetch(getFabricUrl(endpoint), HTTP_METHOD.GET, body);
  }

  public static dataProductGet(endpoint: string, body?: any) {
    return this.fetch(getDataProductUrl(endpoint), HTTP_METHOD.GET, body);
  }
  
  public static postWithFormData(endpoint: string, formData: FormData) {
    return this.fetchWithFormData(getUrl(endpoint), HTTP_METHOD.POST, formData);
  }

  public static put(endpoint: string, body?: any) {
    return this.fetch(getUrl(endpoint), HTTP_METHOD.PUT, body);
  }

  public static putWithFormData(endpoint: string, formData: FormData) {
    return this.fetchWithFormData(getUrl(endpoint), HTTP_METHOD.PUT, formData);
  }

  public static patch(endpoint: string, body?: any) {
    return this.fetch(getUrl(endpoint), HTTP_METHOD.PATCH, body);
  }

  public static delete(endpoint: string, body?: any) {
    return this.fetch(getUrl(endpoint), HTTP_METHOD.DELETE, body);
  }

  public static getLoginJWT() {
    return fetch(`login?timestamp=${Date.now()}`, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    }).then((response) => {
      if (!response?.ok) {
        return response?.json()?.then((result) => {
          throw new Error(result.message);
        });
      }

      if (response?.headers?.get('authorization')) {
        return response?.headers?.get('authorization');
      }
      return response?.json();
    });
  }

  public static logoutUser() {
    const jwt = this.readJwt();
    const method = HTTP_METHOD.POST;
    return fetch(`${'logout'}`, {
      headers: {
        Accept: 'application/json',
        Authorization: jwt,
        'Content-Type': 'application/json',
      },
      method,
    }).then((response) => {
      if (!response.ok) {
        return response.json().then((result) => {
          throw new Error(result.message);
        });
      }

      return response.json();
    });
  }

  public static fetch(url: string, method: HTTP_METHOD, body?: any, jwtVal?: any): Promise<any> {
    const jwt = jwtVal ? jwtVal : this.readJwt();
    if (typeof body !== 'string') {
      body = JSON.stringify(body);
    }
    return fetch(url, {
      body,
      headers: {
        Accept: 'application/json',
        Authorization: jwt,
        'Content-Type': 'application/json',
      },
      method,
    }).then((response: any) => {
      let message = '';
      if (!response.ok) {
        return response.json().then((result: any) => {

          if (response?.status === 403 && result?.error_description?.includes("JWT is expired")) {
            return refreshToken(jwt).then((newJwt: any) => {
              return ApiClient.fetch(url, method, body, newJwt);
            });
          }

          if (response?.status === 401 && result?.error_description?.includes('Invalid JWT token')) {
            // Handle error during refresh
            message = 'Authtoken has expired';
            // Get the current URL
            const currentURL = window.location.href;
            const baseUrl = currentURL.split('#')[0];
            const sessionExpiredUrl = `${baseUrl}#/SessionExpired`;
            Pkce.clearUserSession();
            window.location.href = sessionExpiredUrl;
          }

          if (result && result.errors) {
            result.errors.forEach((error: IError) => {
              message += error.message + ' ';
            });
          } else if (response.status === 409) {
            message = 'Value or Item already exist!';
          } else {
            message = 'Some Error Occurred';
          }

          throw new Error(message);
        });
      }

      if (response.status === 204) {
        return [];
      }

      return response.json();
    }).catch((error) => {
      console.error('API call failed with error:', error);
      if (error instanceof TypeError && error.message === "Failed to fetch") {
        return Promise.reject({
          error: true,
          message: "Failed to connect to server. Please check your VPN/Internet.",
        });
      } else {
        // Return the error in a similar structure to response.json()
        return Promise.reject({
          error: true,
          message: error?.message || 'Some Error Occured',
          code: error?.status || "FETCH_ERROR" // use FETCH_ERROR if status code is not available
        });
      }
    });
  }

  public static fetchWithFormData(endpoint: string, method: HTTP_METHOD, formData: FormData) {
    const accessToken = this.readJwt();
    return fetch(`${baseUrl}/${endpoint}`, {
      body: formData,
      headers: {
        Accept: 'application/json',
        Authorization: accessToken,
      },
      method,
    }).then((response) => {
      if (!response.ok) {
        return response.json().then((result) => {
          throw new Error(result.message);
        });
      }
      return response.json();
    });
  }

  public static getAttachmentBaseURL(): any {
    return `${baseUrl}/attachments`;
  }

  public static readJwt(): any {
    return sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
  }

  public static writeJwt(obj: any) {
    sessionStorage.setItem(SESSION_STORAGE_KEYS.JWT, obj);
  }

  public static destroyJwt() {
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.JWT);
  }

  public static getJwt(): Promise<any> {
    return ApiClient.getLoginJWT()
      .then((result) => {
        if (result) {
          ApiClient.writeJwt(result);
        }
      })
      .catch((error) => {
        ApiClient.destroyJwt();
        throw error;
      });
  }

  public static verifyDigiLogin() {
    return this.post('verifyLogin');
  }

  public static parseJwt(token: any) {
    return JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString());
  }

  public static getTags(): Promise<ITag[]> {
    return this.get('tags');
  }

  public static deleteTag(id: string): Promise<any> {
    return this.delete(`tags/${id}`);
  }

  public static getAppVersion(): Promise<IAppVersion> {
    return this.get(`app-version`);
  }

  public static getDivisions(): Promise<IManageDivision[]> {
    return this.get('divisions');
  }

  public static postDivision(data: IManageDivisionRequest): Promise<IManageDivision[]> {
    return this.post('divisions', data);
  }

  public static putDivision(data: IManageDivisionRequest): Promise<IManageDivision[]> {
    return this.put('divisions', data);
  }

  public static deleteDivision(id: string): Promise<any> {
    return this.delete(`divisions/${id}`);
  }

  public static getSubDivisions(divisionId: string): Promise<ISubDivision[]> {
    return this.get('subdivisions/' + divisionId);
  }

  public static getAllSubDivisions(divisionIds: string[]): Promise<ISubDivision[]> {
    return this.get('divisions?ids=' + divisionIds);
  }

  public static getDropdownList(dropdownName: string): Promise<any[]> {
    return this.get('lov/' + dropdownName);
  }

  public static getDataSources(): Promise<IDataSource[]> {
    return this.get('datasources');
  }

  public static getMasterDataSources(): Promise<IDataSourceMaster[]> {
    return this.get(`datasources`);
  }

  public static deleteDataSource(id: string): Promise<any> {
    return this.delete(`datasources/${id}`);
  }

  public static getPlatforms(): Promise<IPlatform[]> {
    return this.get('platforms');
  }
  public static deletePlatform(id: string): Promise<any> {
    return this.delete(`platforms/${id}`);
  }

  public static getLanguages(): Promise<ILanguage[]> {
    return this.get('languages');
  }

  public static deleteLanguage(id: string): Promise<any> {
    return this.delete(`languages/${id}`);
  }

  public static getAlgorithms(): Promise<IAlgorithm[]> {
    return this.get('algorithms');
  }

  public static deleteAlgorithm(id: string): Promise<any> {
    return this.delete(`algorithms/${id}`);
  }

  public static getVisualizations(): Promise<IVisualization[]> {
    return this.get('visualizations');
  }

  public static getSolutions(): Promise<IAnalyticsSolution[]> {
    return this.get('analyticsSolutions');
  }

  public static deleteVisualization(id: string): Promise<any> {
    return this.delete(`visualizations/${id}`);
  }
  public static relatedProductList(): Promise<IFitlerCategory[]> {
    return this.get(`relatedproducts`);
  }

  public static deleterelatedProductList(id: string): Promise<any> {
    return this.delete(`relatedProducts/${id}`);
  }

  public static getMarketingCommunicationChannels(): Promise<IMarketingCommunicationChannel[]> {
    return this.get('marketingCommunicationChannels');
  }

  public static createMarketingCommunicationChannels(data: IManageMarketingTabRequest): Promise<[]> {
    return this.post('marketingCommunicationChannels', data);
  }

  public static putMarketingCommunicationChannels(data: IManageMarketingTabRequest): Promise<[]> {
    return this.put('marketingCommunicationChannels', data);
  }

  public static deleteMarketingCommunicationChannels(id: string): Promise<any> {
    return this.delete(`marketingCommunicationChannels/${id}`);
  }

  public static getCustomerJourneyPhases(): Promise<IMarketingCustomerJourney[]> {
    return this.get('customerJourneyPhases');
  }

  public static createCustomerJourneyPhases(data: IManageMarketingTabRequest): Promise<[]> {
    return this.post('customerJourneyPhases', data);
  }

  public static putCustomerJourneyPhases(data: IManageMarketingTabRequest): Promise<[]> {
    return this.put('customerJourneyPhases', data);
  }

  public static deleteCustomerJourneyPhases(id: string): Promise<any> {
    return this.delete(`customerJourneyPhases/${id}`);
  }


  public static getDescriptionLovData(): Promise<any[]> {
    return Promise.all([
      this.get(`lov/businessgoals`),
      this.get('lov/strategydomains'),
      this.get('lov/additionalresources'),
    ]);
  }

  public static getSkills(): Promise<any[]> {
    return this.get('skills');
  }

  public static getCreateNewSolutionData(): Promise<any[]> {
    return Promise.all([
      this.get('locations'),
      this.get('divisions'),
      this.get('project-statuses'),
      this.get('phases'),
      this.get('tags'),
      this.get('languages'),
      this.get('results'),
      this.get('algorithms'),
      this.get('visualizations'),
      this.get('datasources'),
      this.get('datavolumes'),
      this.get('platforms'),
      this.get('relatedproducts'),
      this.get('project-statuses'),
      this.get('lov/businessgoals'),
      this.get('lov/maturitylevels'),
      this.get('lov/benefitrelevances'),
      this.get('lov/strategicrelevances'),
      this.get('customerJourneyPhases'),
      this.get('marketingCommunicationChannels'),
      ReportsApiClient.get('departments'),
      this.get('marketingRoles'),
      this.get('analyticsSolutions')
    ]);
  }

  public static getFiltersMasterData(): Promise<any[]> {
    return Promise.all([
      this.get('locations'),
      this.get('divisions'),
      this.get('project-statuses'),
      this.get('phases'),
      this.get('datavolumes'),
      this.get('tags'),
      this.get('dashboard/datavalue/minmaxyear')
    ]);
  }

  public static getSubDivisionsData(divisions: any): Promise<any[]> {
    const divisionIds = divisions.map((division: any) => division.id);
    return this.getAllSubDivisions(divisionIds).then((res) => {
      const tempSubDivision: any = [];
      res.forEach((division: any) => {
        division.subdivisions.forEach((subdiv: ISubDivisionSolution) => {
          subdiv.division = division.id;
          subdiv.id = subdiv.id + '@-@' + division.id;
          tempSubDivision.push(subdiv);
        });
      });
      return tempSubDivision;
    });
  }

  public static getSubDivisionsDataWithDivision(divisions: any): Promise<any[]> {
    return this.getAllSubDivisions(divisions).then((res) => {
      const tempSubDivisions: any = [];
      res.forEach((division: any) => {
        division.subdivisions.forEach((subdiv: ISubDivisionSolution) => {
          subdiv.division = division.id;
          subdiv.id = subdiv.id + '@-@' + division.id;
          tempSubDivisions.push(subdiv);
        });
      });
      return Promise.all(tempSubDivisions);
    });
  }

  // Create New Sandbox
  public static createNewSandbox(data: INotebookInfo) {
    return this.post('notebooks/server?newUser=true', data);
  }
  public static getNotebooksDetails(id?: string) {
    return this.get(`notebooks${id ? '/' + id : ''}`);
  }
  public static stopNotebook() {
    return this.delete('notebooks/server?newUser=true');
  }
  public static updateSandbox(data: INotebookInfo) {
    return this.put('notebooks', data);
  }
  public static createNewSolution(data: ICreateNewSolutionRequest): Promise<ICreateNewSolutionResult> {
    return this.post('solutions', data);
  }
  public static createNewReport(data: ICreateNewReportRequest): Promise<ICreateNewReportResult> {
    return this.post('solutions', data);
  }

  public static getDataikuProjectsList(live: boolean): Promise<any> {
    return this.get(`dataiku/projects?live=${live}`);
  }

  public static getDnaDataikuProjectList(): Promise<any> {
    return this.fetch(getDataikuUrl(`dataiku?limit=0&offset=0&sortBy=&sortOrder=&projectName=`), HTTP_METHOD.GET);
  }

  public static getDataikuProjectDetails(projectId: any, isLive: boolean) {
    return this.get(`dataiku/projects/${projectId}?live=${isLive}`);
  }

  public static getDataikuProjectDetailsByProjectkey(projectKey: any, cloudProfile: any) {
    return this.fetch(getDataikuUrl(`dataiku/${cloudProfile}/${projectKey}`), HTTP_METHOD.GET);
  }

  public static createAliceRole(data: any) {
    return this.fabricPost('fabric-workspaces/createrole', data);
  }
  
  public static getExistingRoles(appId: string) {
    return this.fabricGet(`fabric-workspaces/${appId}/dnaroles`);
  }

  public static updateSolution(data: ICreateNewSolutionRequest): Promise<ICreateNewSolutionResult> {
    return this.put('solutions', data);
  }
  public static deleteSolution(id: string): Promise<any> {
    return this.delete(`solutions/${id}`);
  }

  public static transferSolutionOwner(userObj: any, solutionId: string): Promise<any> {
    return this.patch(`solutions/${solutionId}/reassignOwner`,userObj);
  }

  public static getDRDUserInfo(adId: string): Promise<any> {
    return this.get(`userinfo/${adId}`);
  }

  public static getUsersBySearchTerm(searchTerm: string): Promise<any> {
    return this.get(`users?searchTerm=${searchTerm}&offset=0&limit=0`);
  }

  public static getUserprivilegeSearchTerm(searchTerm: string): Promise<any> {
    return this.fetch(getDataikuUrl(`userprivilege?limit=0&offset=0&sortBy=&sortOrder=&searchTerm=${searchTerm}`), HTTP_METHOD.GET);
  }

  public static getAllSolutions(queryUrl?: string): Promise<ICreateNewSolution[]> {
    return queryUrl ? this.get(`solutions?${queryUrl}`) : this.get('solutions');
  }
  public static getSolutionById(id: string): Promise<ICreateNewSolution> {
    return this.get(`solutions/${id}`);
  }
  public static getReportById(id: string): Promise<ICreateNewReport> {
    return this.get(`solutions/${id}`);
  }
  public static getChangeLogs(id: string): Promise<any> {
    return this.get(`changelogs/${id}`);
  }
  public static getDivisionChangeLogs(): Promise<any> {
    return this.get('division/audit');
  }
  public static getUsers(
    offset?: number,
    limit?: number,
    sortBy?: string,
    sortOrder?: string,
  ): Promise<IUserInfoResponse> {
    return this.get(`users?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`);
  }

  public static onboardTechnicalUser(data: IUserRequestVO): Promise<IUserInfo> {
    return this.post('users', data);
  }

  public static updateUser(data: IUserRequestVO): Promise<IUserInfo> {
    return this.put('users', data);
  }

  public static getUserRoles(): Promise<IRole[]> {
    return this.get(`userRoles`);
  }
  public static getBookmarkedSolutions(): Promise<any> {
    const reqQuery = `useCaseType:"1"`;
    const resQuery = `totalCount records {id,productName}`;
    const apiQuery = {
      query: `query {
        solutions(${reqQuery}){
          ${resQuery}
        }
      }`,
    };
    return this.post('minified', apiQuery);
  }
  public static getSolutionsByGraphQL(
    locations: string,
    phases: string,
    divisions: string,
    status: string,
    useCaseType: string,
    dataVolumes: string,
    tags: string,
    limit: number,
    offset: number,
    sortBy: string,
    sortOrder: string,
    published?: boolean,
    digitalvaluecontribution?: boolean,
    notebookavailable?: boolean,
  ): Promise<any> {
    let reqQuery = `location:"${locations}",phase:"${phases}",division:"${divisions}",projectStatus:"${status}",useCaseType:"${useCaseType}",dataVolume:"${dataVolumes}",tags:"${tags}",offset:${offset},limit:${limit},sortBy:"${sortBy}",sortOrder:"${sortOrder}"`;
    if (published) {
      reqQuery += `,published:${published}`;
    }
    reqQuery += `,hasDigitalValue:${digitalvaluecontribution }`;
    reqQuery += `,hasNotebook:${notebookavailable ? notebookavailable : false}`;
    const resQuery = `totalCount
      records {id,
        productName,
        description,
        logoDetails {
          id,
          fileName,
          isPredefined,
        },
        division {
          id,
          name,
          subdivision {
            id,
            name
          }
        },
        team { 
          shortId
        },
        portfolio {
          dnaNotebookId,
          dnaDataikuProjectId
        },
        projectStatus {
          id,
          name
        },
        locations {
          id,
          name
        },
        currentPhase {
          id,
          name
        },
        digitalValue {
          typeOfCalculation,
          digitalValue,
          digitalEffort,
          dataValueCalculator {
            savingsValueFactorSummaryVO{
              year,
              value
            },
            revenueValueFactorSummaryVO{
              year,
              value
            }
          }
        },
        publish,
        bookmarked,
        createdBy {
          id,
          email
        }
      }`;
    const apiQuery = {
      query: `query {
        solutions(${reqQuery}){
          ${resQuery}
        }
      }`,
    };

    return this.post('minified', apiQuery);
  }

  public static getSolutionsBySearchTerm(
    searchTerm: string,
    limit: number,
    offset: number,
    withDetails?: boolean,
  ): Promise<any> {
    const reqQuery = `searchTerm:"${searchTerm}",limit:${limit},offset:${offset}`;
    let resQuery = `totalCount
      records {
        id,
        productName
      }`;

    if (withDetails) {
      resQuery = `totalCount
      records {
        id,
        productName,
        logoDetails {
          id,
          fileName,
          isPredefined,
        },
        division {
          id,
          name,
          subdivision {
            id,
            name
          }
        },
        projectStatus {
          id,
          name
        },
        locations {
          id,
          name
        },
        currentPhase {
          id,
          name
        },
        publish,
        bookmarked,
        createdBy {
          id,
          email
        },
      }`;
    }

    const apiQuery = {
      query: `query {
        solutions(${reqQuery}){
          ${resQuery}
        }
      }`,
    };

    return this.post('minified', apiQuery);
  }

  public static getSolutionsByGraphQLForPortfolio(
    locations: string,
    phases: string,
    divisions: string,
    status: string,
    useCaseType: string,
    tagSearch: string,
  ): Promise<any> {
    const reqQuery = `location:"${locations}",phase:"${phases}",division:"${divisions}",projectStatus:"${status}",useCaseType:"${useCaseType}",tags:"${tagSearch}",limit:0,published:true`;
    const resQuery = `totalCount
      records {id,
        productName,
        description,
        locations {
          id,
          name
        },
        currentPhase {
          id,
          name
        },
        dataSources {
          dataSources,
          dataVolume {
            id,
            name
          }
        },
        digitalValue {
          digitalValue,
          digitalEffort,
          valueCalculator {
            breakEvenPoint,
            calculatedDigitalValue {
              year
            }
          }
        },
        team {
          userType
        },
        portfolio {
          dnaNotebookId
        },
      }`;
    const apiQuery = {
      query: `query {
        solutions(${reqQuery}){
          ${resQuery}
        }
      }`,
    };

    return this.post('minified', apiQuery);
  }

  public static bookMarkSolutions(data: IBookMarks): Promise<IBookMarksResponse> {
    return this.post('/users/bookmarks', data);
  }

  public static getUserbookMarkSolutions(userId: string): Promise<IBookMarksResponse> {
    return this.get('/users/bookmarks?userId=' + userId);
  }

  public static getDashboardDataOld(
    calledResource: string,
    locations: string,
    phases: string,
    divisions: string,
    status: string,
    useCaseType: string,
    tagSearch: string,
  ): Promise<IWidgetsResponse[]> {
    const reqQuery = `location="${locations}"&phase="${phases}"&division="${divisions}"&projectStatus="${status}"&useCaseType="${useCaseType}"&tags="${tagSearch}"&published=true`;
    return this.get(`dashboard/${calledResource}?` + reqQuery);
  }

  public static getDashboardData(
    calledResource: string,
    locations: string,
    phases: string,
    divisions: string,
    status: string,
    useCaseType: string,
    tagSearch: string,
    dataValueRange?: string,
  ): Promise<IWidgetsResponse[]> {
    let reqQuery = 'published=true&';
    if (locations != '') reqQuery += `location=${locations}&`;
    if (phases != '') reqQuery += `phase=${phases}&`;
    if (divisions != '') reqQuery += `division=${encodeURIComponent(divisions)}&`;
    if (status != '') reqQuery += `projectstatus=${status}&`;
    if (useCaseType != '') reqQuery += `useCaseType=${useCaseType}&`;
    if (tagSearch != '') reqQuery += `tags=${tagSearch}&`;
    if (dataValueRange !='' && dataValueRange != undefined) reqQuery +=`enddate=${dataValueRange.split(',')[1]}&startdate=${dataValueRange.split(',')[0]}&`;

    return this.get(`dashboard/${calledResource}?` + reqQuery);
  }

  public static getAllWidgets(): Promise<IWidgetsResponse[]> {
    return this.get('widgets');
  }

  public static getUserPreference(userId: string): Promise<IUserPreference[]> {
    return this.get(`widget-preference?userId=${userId}`);
  }

  public static saveUserPreference(data: IUserPreferenceRequest): Promise<IUserPreference> {
    return this.post('widget-preference', data);
  }

  public static removeUserPreference(id: string): Promise<any> {
    return this.delete(`widget-preference/${id}`);
  }

  public static getNotificationPreferences(userId: string) {
    return this.get(`notification-preferences?userId=${userId}`);
  }

  public static enableEmailNotifications(notificationPreferences: INoticationModules) {
    return this.post(`notification-preferences`, { data: notificationPreferences });
  }

  public static downloadAttachment(attachment: IAttachment, newJwt?: any): Promise<any> {
    const id = attachment.id + '~' + attachment.fileName;
    const jwt = newJwt ? newJwt : this.readJwt();
    return fetch(`${baseUrl}/attachments/${id}`, {
      headers: {
        Accept: 'application/json',
        Authorization: jwt,
        'Content-Type': 'application/octet-stream',
      },
    }).then((response) => {
      if (!response.ok) {
        return response.json().then((result) => {
          if (response?.status === 403 && result?.error_description?.includes("JWT is expired")) {
            return refreshToken(jwt).then((newJwt: any) => {
              return ApiClient.downloadAttachment(attachment, newJwt);
            });
          }
        });
      }
      return response;
    }).catch((error) => {
      console.error('API call failed with error:', error);
      if (error instanceof TypeError && error.message === "Failed to fetch") {
        return Promise.reject({
          error: true,
          message: "Failed to connect to server. Please check your VPN/Internet.",
        });
      } else {
        // Return the error in a similar structure to response.json()
        return Promise.reject({
          error: true,
          message: error?.message || 'Some Error Occured',
          code: error?.status || "FETCH_ERROR" // use FETCH_ERROR if status code is not available
        });
      }
    });
  }

  public static deleteAttachment(id: string): Promise<any> {
    return this.delete('attachments/' + id);
  }

  public static exportDatatoCSV(
    locations: string,
    phases: string,
    divisions: string,
    // subDivisions: string,
    status: string,
    useCaseType: string,
    dataVolumes: string,
    tags: string,
    sortBy: string,
    sortOrder: string,
    published: boolean,
    searchKey: string,
    digitalvaluecontribution?: boolean,
    notebookavailable = false,
  ): Promise<any> {
    let reqQuery = `location:"${locations}",phase:"${phases}",division:"${divisions}",projectStatus:"${status}",useCaseType:"${useCaseType}",dataVolume:"${dataVolumes}",tags:"${tags}",offset:0,limit:0,sortBy:"${sortBy}",sortOrder:"${sortOrder}"`;
    if (published) {
      reqQuery += `,published:${published}`;
    }
    if (searchKey) {
      reqQuery = `searchTerm:"${searchKey}",offset:0,limit:0`;
    }
    reqQuery += `,hasDigitalValue:${digitalvaluecontribution}`;
    reqQuery += `,hasNotebook:${notebookavailable}`;
    const resQuery = `totalCount
      records {id,
        productName,
        description,
        businessNeed,
        businessGoals,
        additionalResource,
        dataStrategyDomain,
        relatedProducts,
        attachments {
          id,
          fileName,
          fileSize
        },
        expectedBenefits,
        reasonForHoldOrClose,
        createdDate,
        lastModifiedDate,
        closeDate,
        tags,
        milestones {
          phases {
            description,
            phase {
              id,
              name
            },
            month,
            year
          },
          rollouts {
            details {
              location {
                id,
                name
              },
              month,
              year
            },
            description
          }
        },
        team {
          shortId,
          userType,
          firstName,
          lastName,
          department,
          email,
          mobileNumber,
          company,
          teamMemberPosition,
          isUseCaseOwner
        },
        dataSources {
          dataSources {
            dataSource,
            weightage
          },
          dataVolume {
            id,
            name
          }
        },
        analytics {
          languages {
            id,
            name
          },
          algorithms {
            id,
            name
          },
          visualizations {
            id,
            name
          }
        },
        sharing {
          gitUrl,
          resultUrl,
          result {
            id,
            name
          }
        },
        department,
        marketing {
          customerJourneyPhases{id, name},
          marketingCommunicationChannels{id, name},
          personalization {
            isChecked,
            description
          },
          personas,
          marketingRoles {
            fromDate,
            role,
            requestedFTECount,
            toDate,
          }
        },
        dataCompliance {
          expertGuidelineNeeded,
          readyForImplementation,
          quickCheck,
          useCaseDescAndEval,
          aiRiskAssessmentType,
          workersCouncilApproval,
          attachments {
            id,
            fileName,
            fileSize
          },
          links {
            description,
            link,
            label
          },
          complianceOfficers {
            shortId,
            userType,
            firstName,
            lastName,
            department,
            email,
            mobileNumber,
            company,
            teamMemberPosition
          }
        },
        portfolio {
          solutionOnCloud,
          usesExistingInternalPlatforms,
          platforms {
            id,
            name
          }
        },
        openSegments,
        division {
          id,
          name,
          subdivision {
            id,
            name
          }
        },
        projectStatus {
          id,
          name
        },
        locations {
          id,
          name
        },
        currentPhase {
          id,
          name
        },
        digitalValue {
          maturityLevel,
          costDrivers {
            description,
            category,
            value,
            source,
            rampUp {
              year,
              value
            }
          },
          valueDrivers {
            description,
            category,
            value,
            source,
            rampUp {
              year,
              percent,
              value
            }
          },
          projectControllers {
            shortId,
            userType,
            firstName,
            lastName,
            department,
            email,
            mobileNumber,
            company,
            teamMemberPosition
          },
          permissions {
            shortId,
            userType,
            firstName,
            lastName,
            department,
            email,
            mobileNumber,
            company,
            teamMemberPosition
          },
          attachments {
            id,
            fileName,
            fileSize
          },
          assessment {
            strategicRelevance,
            commentOnStrategicRelevance,
            benefitRealizationRisk,
            commentOnBenefitRealizationRisk
          },
          valueCalculator {
            costFactorSummary{
              year,
              value
            },
            valueFactorSummary{
              year,
              value
            },
            calculatedDigitalValue{
              valueAt,
              year,
              value
            },
            breakEvenPoint
          }
        },
        publish,
        bookmarked,
        createdBy {
          id,
          email
        },
        skills {
          fromDate,
          neededSkill,
          requestedFTECount,
          toDate
        }
      }`;
    const apiQuery = {
      query: `query {
        solutions(${reqQuery}){
          ${resQuery}
        }
      }`,
    };

    return this.post('minified', apiQuery);
  }

  public static getLeanIX = (searchTerm = '') => {
    return this.dataProductGet(`/planningit?searchTerm=${searchTerm}`);
  };

  // Usage statistics
  public static getNotebooksTransparency(): Promise<any> {
    return this.get('notebooks/transparency');
  }
  public static getSolutionsTransparency(): Promise<any> {
    return this.get('solutions/transparency');
  }
  public static getUsersTransparency(): Promise<any> {
    return this.get('users/transparency');
  }

  public static getSimilarSearchResult(endpoint: string):Promise<any> {
    return this.fetch(getSimilaritySearchUrl(endpoint), HTTP_METHOD.GET);
  } 

  public static notifyUseCaseOwners(data: any):Promise<any>{
    return this.post('/solutions/broadcast/usecaseowners',data.data);
  }
}
