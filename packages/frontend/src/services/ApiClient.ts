/* tslint:disable:no-console */
import { ICreateCodeSpaceData } from '../components/mbc/codeSpace/newCodeSpace/NewCodeSpace';
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
} from '../globals/types';
import { Pkce } from './Pkce';

export interface IResponse<T> {
  meta?: {
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  };
  data: T;
}

const baseUrl = Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

export class ApiClient {
  public static get(endpoint: string) {
    return this.fetch(getUrl(endpoint), HTTP_METHOD.GET);
  }

  public static post(endpoint: string, body?: any) {
    return this.fetch(getUrl(endpoint), HTTP_METHOD.POST, body);
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

  public static getLoginJWT(token: string) {
    return fetch(`${baseUrl}/login`, {
      headers: {
        Accept: 'application/json',
        Authorization: token,
        'Content-Type': 'application/json',
      },
    }).then((response) => {
      if (!response.ok) {
        return response.json().then((result) => {
          throw new Error(result.message);
        });
      }

      return response.json();
    });
  }

  public static logoutUser() {
    const tokenObject = Pkce.readAccessToken();
    const token = tokenObject.access_token;
    const jwt = this.readJwt();
    const method = HTTP_METHOD.POST;
    return fetch(`${baseUrl}/logout`, {
      headers: {
        Accept: 'application/json',
        AccessToken: token,
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

  public static fetch(url: string, method: HTTP_METHOD, body?: any) {
    const jwt = this.readJwt();
    body = JSON.stringify(body);
    return fetch(url, {
      body,
      headers: {
        Accept: 'application/json',
        Authorization: jwt,
        'Content-Type': 'application/json',
      },
      method,
    }).then((response) => {
      if (!response.ok) {
        return response.json().then((result) => {
          let message = '';
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
    const tokenObject = Pkce.readAccessToken();
    const token = tokenObject.access_token;
    return ApiClient.getLoginJWT(token)
      .then((result) => ApiClient.writeJwt(result.token))
      .catch((error) => {
        ApiClient.destroyJwt();
        throw error;
      });
  }

  public static verifyDigiLogin() {
    return this.post('verifyLogin');
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

  public static deleteVisualization(id: string): Promise<any> {
    return this.delete(`visualizations/${id}`);
  }
  public static relatedProductList(): Promise<IFitlerCategory[]> {
    return this.get(`relatedproducts`);
  }

  public static deleterelatedProductList(id: string): Promise<any> {
    return this.delete(`relatedProducts/${id}`);
  }

  public static getDescriptionLovData(): Promise<any[]> {
    return Promise.all([
      this.get(`lov/businessgoals`), 
      this.get('lov/strategydomains'),
      this.get('lov/additionalresources')
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

  // Create New Code Space
  public static getCodeSpace() {
    return this.get('users/code-server');
  }

  public static createCodeSpace(data: ICreateCodeSpaceData) {
    return this.post('users/code-server', data);
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

  public static getDataikuProjectDetails(projectId: any, isLive: boolean) {
    return this.get(`dataiku/projects/${projectId}?live=${isLive}`);
  }

  public static updateSolution(data: ICreateNewSolutionRequest): Promise<ICreateNewSolutionResult> {
    return this.put('solutions', data);
  }
  public static deleteSolution(id: string): Promise<any> {
    return this.delete(`solutions/${id}`);
  }

  public static getDRDUserInfo(adId: string): Promise<any> {
    return this.get(`userinfo/${adId}`);
  }

  public static getUsersBySearchTerm(searchTerm: string): Promise<any> {	
    return this.get(`users?searchTerm=${searchTerm}&offset=0&limit=0`);	
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
  public static getUsers(
    offset?: number,
    limit?: number,
    sortBy?: string,
    sortOrder?: string,
  ): Promise<IUserInfoResponse> {
    return this.get(`users?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`);
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
  ): Promise<any> {
    let reqQuery = `location:"${locations}",phase:"${phases}",division:"${divisions}",projectStatus:"${status}",useCaseType:"${useCaseType}",dataVolume:"${dataVolumes}",tags:"${tags}",offset:${offset},limit:${limit},sortBy:"${sortBy}",sortOrder:"${sortOrder}"`;
    if (published) {
      reqQuery += `,published:${published}`;
    }
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
          digitalValue,
          digitalEffort
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
  ): Promise<IWidgetsResponse[]> {
    let reqQuery = 'published=true&';
    if (locations != '') reqQuery += `location=${locations}&`;
    if (phases != '') reqQuery += `phase=${phases}&`;
    if (divisions != '') reqQuery += `division=${encodeURIComponent(divisions)}&`;
    if (status != '') reqQuery += `projectstatus=${status}&`;
    if (useCaseType != '') reqQuery += `useCaseType=${useCaseType}&`;
    if (tagSearch != '') reqQuery += `tags=${tagSearch}&`;

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

  public static downloadAttachment(attachment: IAttachment): Promise<any> {
    const id = attachment.id + '~' + attachment.fileName;
    const jwt = this.readJwt();
    return fetch(`${baseUrl}/attachments/${id}`, {
      headers: {
        Accept: 'application/json',
        Authorization: jwt,
        'Content-Type': 'application/octet-stream',
      },
    }).then((response) => {
      return response;
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
  ): Promise<any> {
    let reqQuery = `location:"${locations}",phase:"${phases}",division:"${divisions}",projectStatus:"${status}",useCaseType:"${useCaseType}",dataVolume:"${dataVolumes}",tags:"${tags}",offset:0,limit:0,sortBy:"${sortBy}",sortOrder:"${sortOrder}"`;
    if (published) {
      reqQuery += `,published:${published}`;
    }
    if (searchKey) {
      reqQuery = `searchTerm:"${searchKey}",offset:0,limit:0`;
    }
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
          teamMemberPosition
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
        dataCompliance {
          expertGuidelineNeeded,
          readyForImplementation,
          quickCheck,
          useCaseDescAndEval,
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
}
