import { Envs } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';
import {
  ICreateNewReport,
  ICreateNewReportRequest,
  IReportUserPreference,
  IReportUserPreferenceRequest,
  IReportListItems,
  IAddNewCategoriesItem,
} from '../globals/types';

const baseUrl = Envs.DASHBOARD_API_BASEURL
  ? Envs.DASHBOARD_API_BASEURL
  : `http://${window.location.hostname}:7173/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

export class ReportsApiClient {
  public static get(endpoint: string) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.GET);
  }
  public static post(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.POST, body);
  }
  public static put(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.PUT, body);
  }
  public static putWithFormData(endpoint: string, formData: FormData) {
    return ApiClient.fetchWithFormData(getUrl(endpoint), HTTP_METHOD.PUT, formData);
  }
  public static patch(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.PATCH, body);
  }
  public static delete(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrl(endpoint), HTTP_METHOD.DELETE, body);
  }

  public static getCreateNewReportData(): Promise<any[]> {
    return Promise.all([
      this.get('lov/datasources'),
      this.get('lov/customer/departments'),
      this.get('lov/frontendtechnologies'),
      this.get('lov/hierarchies'),
      this.get('lov/integratedportals'),
      this.get('lov/kpinames'),
      this.get('lov/productphases'),
      this.get('lov/reportingcauses'),
      this.get('lov/ressort'),
      this.get('lov/statuses'),
      this.get('lov/designguides'),
      this.get('lov/agilereleasetrains'),
      this.get('tags'),
      this.get('lov/connectiontypes'),
      this.get('datawarehouses'),
      this.get('lov/subsystems'),
      ApiClient.get('divisions'),
      this.get('departments'),
    ]);
  }

  public static createNewReport(data: ICreateNewReportRequest): Promise<ICreateNewReportRequest> {
    return this.post('reports', data);
  }

  public static getReportById(id: string): Promise<ICreateNewReport> {
    return this.get(`reports/${id}`);
  }

  public static updateReport(data: ICreateNewReportRequest): Promise<ICreateNewReportRequest> {
    return this.put('reports', data);
  }

  public static getAllReports(limit: number, offset: number, sortBy: string, sortOrder: string): Promise<any> {
    return this.get(`reports?limit=${limit}&offset=${offset}&sortBy=${sortBy}&sortOrder=${sortOrder}`);
  }

  public static getReportsByGraphQL(
    divisions: string,
    agileReleaseTrains: string,
    departments: string,
    processOwners: string,
    productOwners: string,
    limit: number,
    offset: number,
    sortBy: string,
    sortOrder: string,
    published?: boolean,
  ): Promise<any> {
    let reqQuery = `division:"${divisions}",art:"${agileReleaseTrains}", department:"${departments}",processOwner:"${processOwners}",productOwner:"${productOwners}",offset:${offset},limit:${limit},sortBy:"${sortBy}",sortOrder:"${sortOrder}"`;
    if (published) {
      reqQuery += `,published:${published}`;
    }
    const resQuery = `totalCount
      records {id,
        reportId,
        productName,
        description { division { id, name, subdivision { id, name } }, department, productDescription, agileReleaseTrains, status },
        members {
          productOwners { firstName, lastName, department, shortId },
          developers { firstName, lastName, department },
          admin { firstName, lastName, department, shortId }
        },
        publish
      }`;
    const apiQuery = {
      query: `query {
        reports(${reqQuery}){
          ${resQuery}
        }
      }`,
    };

    return this.post('minified', apiQuery);
  }

  public static exportDatatoCSV(
    divisions: string,
    agileReleaseTrains: string,
    departments: string,
    processOwners: string,
    productOwners: string,
    sortBy: string,
    sortOrder: string,
    published?: boolean,
  ): Promise<any> {
    let reqQuery = `division:"${divisions}",art:"${agileReleaseTrains}", department:"${departments}",processOwner:"${processOwners}",productOwner:"${productOwners}",sortBy:"${sortBy}",sortOrder:"${sortOrder}"`;
    if (published) {
      reqQuery += `,published:${published}`;
    }
    const resQuery = `totalCount
      records {id,
        productName,
        description { division { id, name, subdivision { id, name } }, department, status, productDescription, tags, agileReleaseTrains, integratedPortal, frontendTechnologies, reportLink, reportType  },
        customer {
          customerDetails { hierarchy, ressort, department, comment },
          processOwners { shortId }
        },
        kpis { name, reportingCause, comment, kpiLink },
        dataAndFunctions { 
          dataWarehouseInUse { dataWarehouse, commonFunctions, specificFunctions, queries, dataSources, connectionTypes } , 
          singleDataSources { dataSources, subsystems, connectionTypes } 
        }
        members {
          productOwners { firstName, lastName, department, shortId },
          developers { firstName, lastName, department, shortId },
          admin { firstName, lastName, department, shortId }
        },
        publish,
        createdDate,
        lastModifiedDate,
        closeDate,
        createdBy {
          id,
          email
        },
        reportId
      }`;
    const apiQuery = {
      query: `query {
        reports(${reqQuery}){
          ${resQuery}
        }
      }`,
    };

    return this.post('minified', apiQuery);
  }

  public static exportJSON(): Promise<any> {
    const resQuery = `totalCount
    records {id,
      productName,
      description { division { id, name, subdivision { id, name } }, department, status, productDescription, productPhase, tags, agileReleaseTrains, integratedPortal, frontendTechnologies, designGuideImplemented  },
      customer {
        customerDetails { hierarchy, ressort, department, comment },
        processOwners { shortId }
      },
      kpis { name, reportingCause, comment, kpiLink },
      dataAndFunctions { 
        dataWarehouseInUse { dataWarehouse, commonFunctions, specificFunctions, queries, dataSources, connectionTypes } , 
        singleDataSources { dataSources, subsystems, connectionTypes } 
      }
      members {
        productOwners { firstName, lastName, department, shortId },
        developers { firstName, lastName, department, shortId },
        admin { firstName, lastName, department, shortId }
      },
      publish,
      createdDate,
      lastModifiedDate
    }`;
    const apiQuery = {
      query: `query {
        reports {
          ${resQuery}
        }
      }`,
    };

    return this.post('minified', apiQuery);
  }

  public static getFilterMasterData() {
    return Promise.all([
      this.get('lov/agilereleasetrains'),
      ApiClient.get('divisions'),
      this.get('departments'),
      this.get('reports/processowners'),
      this.get('reports/productowners'),
    ]);
  }

  public static getUserPreference(userId: string): Promise<IReportUserPreference[]> {
    return this.get(`widget-preference?userId=${userId}`);
  }

  public static saveUserPreference(data: IReportUserPreferenceRequest): Promise<IReportUserPreference> {
    return this.post('widget-preference', data);
  }

  public static removeUserPreference(id: string): Promise<any> {
    return this.delete(`widget-preference/${id}`);
  }

  public static deleteReportById(id: string): Promise<any> {
    return this.delete(`reports/${id}`);
  }

  // Lov Get Calls //
  public static getProductPhases(): Promise<IReportListItems[]> {
    return this.get('lov/productphases');
  }
  public static getStatuses(): Promise<IReportListItems[]> {
    return this.get('lov/statuses');
  }
  public static getIntegratedPortal(): Promise<IReportListItems[]> {
    return this.get('lov/integratedportals');
  }
  public static getDesignGuideImplementation(): Promise<IReportListItems[]> {
    return this.get('lov/designguides');
  }
  public static getFronEndTechnologies(): Promise<IReportListItems[]> {
    return this.get('lov/frontendtechnologies');
  }
  public static getRessort(): Promise<IReportListItems[]> {
    return this.get('lov/ressort');
  }
  public static getDepartments(): Promise<IReportListItems[]> {
    return this.get('lov/customer/departments');
  }
  public static getSubsystem(): Promise<IReportListItems[]> {
    return this.get('lov/subsystems');
  }
  public static getConnectionType(): Promise<IReportListItems[]> {
    return this.get('lov/connectiontypes');
  }
  public static getAgileReleaseTrain(): Promise<IReportListItems[]> {
    return this.get('lov/agilereleasetrains');
  }
  public static getGetHierarchies(): Promise<IReportListItems[]> {
    return this.get('lov/hierarchies');
  }
  public static getHierarchies(): Promise<IReportListItems[]> {
    return this.get('lov/hierarchies');
  }
  public static getDatawareHouseInUse(): Promise<IReportListItems[]> {
    return this.get('datawarehouses');
  }
  public static getCommonFunctions(): Promise<IReportListItems[]> {
    return this.get('lov/commonfunctions');
  }
  public static getSpecificFunctions(): Promise<IReportListItems[]> {
    return this.get('lov/specificfunctions');
  }
  public static getQueries(): Promise<IReportListItems[]> {
    return this.get('lov/queries');
  }
  public static getDataSource(): Promise<IReportListItems[]> {
    return this.get('lov/datasources');
  }
  public static getReportingCause(): Promise<IReportListItems[]> {
    return this.get('lov/reportingcauses');
  }
  public static getDepartmentsComman(): Promise<IReportListItems[]> {
    return this.get('departments');
  }
  public static getTags(): Promise<IReportListItems[]> {
    return this.get('tags');
  }
  public static getKpiName(): Promise<IReportListItems[]> {
    return this.get('lov/kpinames');
  }

  // Lov Add Calls //
  public static addCategoryItem(categoryType: string, data: IAddNewCategoriesItem): Promise<IReportListItems[]> {
    if (categoryType === 'departments') {
      return this.post(`lov/customer/${categoryType}/`, data);
    }
    if (categoryType === 'descriptiondepartement') {
      return this.post(`departments`, data);
    }
    if (categoryType === 'tags') {
      return this.post(`tags`, data);
    }
    return this.post(`lov/${categoryType}/`, data);
  }
  public static addDataWareHouseInUse(data: IAddNewCategoriesItem): Promise<IReportListItems[]> {
    return this.post(`datawarehouses`, data);
  }
  // lov update calls
  public static updateCategoryItem(categoryType: string, data: IAddNewCategoriesItem): Promise<IReportListItems[]> {
    if (categoryType === 'departments') {
      return this.put(`lov/customer/${categoryType}/`, data);
    }
    if (categoryType === 'descriptiondepartement') {
      return this.put(`departments`, data);
    }
    if (categoryType === 'tags') {
      return this.put(`tags`, data);
    }
    return this.put(`lov/${categoryType}`, data);
  }
  public static updateCategoryItemDatawareHouseInUse(
    categoryType: string,
    data: IAddNewCategoriesItem,
  ): Promise<IReportListItems[]> {
    return this.put(`${categoryType}`, data);
  }
  // Lov Delete Calls //
  public static deleteCategoryItem(categoryType: string, id: string): Promise<IReportListItems[]> {
    if (categoryType === 'departments') {
      return this.delete(`lov/customer/${categoryType}/${id}`);
    }
    if (categoryType === 'descriptiondepartement') {
      return this.delete(`departments/${id}`);
    }
    if (categoryType === 'datawarehouses') {
      return this.delete(`${categoryType}/${id}`);
    }
    if (categoryType === 'tags') {
      return this.delete(`tags/${id}`);
    }
    return this.delete(`lov/${categoryType}/${id}`);
  }
}
