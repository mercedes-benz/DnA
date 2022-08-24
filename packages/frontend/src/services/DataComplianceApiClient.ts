import { Envs, } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';

const baseUrl = Envs.DATA_COMPLIANCE_API_BASEURL
  ? Envs.DATA_COMPLIANCE_API_BASEURL
  : `http://${window.location.hostname}:7184/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

export class DataComplianceApiClient {
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

  public static getDataComplianceNetworkList(
    offset?: number,
    limit?: number,
    sortBy?: string,
    sortOrder?: string,
  ) {
      return this.get(`datacompliance?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`);
  }
  public static saveDataComplianceNetworkList(data: any) {
    return this.post('datacompliance', data);
  }
  public static updateDataComplianceNetworkList(data: any) {
    return this.put('datacompliance', data);
  }
  public static deleteDataComplianceNetworkList(id: any) {
    return this.delete('datacompliance/' + id);
  }

  public static getEntityList(
    offset?: number,
    limit?: number,
    sortBy?: string,
    sortOrder?: string,
  ) {
    return this.get(`entityids?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`);
  }
}
