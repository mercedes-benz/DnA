import { Envs } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';
import {
  IDataProductListItem,
} from '../globals/types';

const baseUrl = Envs.DATA_PRODUCT_API_BASEURL
  ? Envs.DATA_PRODUCT_API_BASEURL
  : `http://${window.location.hostname}:7184/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

const baseUrlReport = Envs.DASHBOARD_API_BASEURL
  ? Envs.DASHBOARD_API_BASEURL
  : `http://${window.location.hostname}:7173/api`;
const getUrlReport = (endpoint: string) => {
  return `${baseUrlReport}/${endpoint}`;
};

export class DataProductFilterApiClient {
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

  public static getReport(endpoint: string) {
    return ApiClient.fetch(getUrlReport(endpoint), HTTP_METHOD.GET);
  }

  public static getAllDataProducts(limit: number, offset: number, sortBy: string, sortOrder: string): Promise<any> {
    return this.get(`reports?limit=${limit}&offset=${offset}&sortBy=${sortBy}&sortOrder=${sortOrder}`);
  }

  public static getFilterMasterData() {
    return Promise.all([
      this.getArts(),
      this.getPlatforms(),
      this.getFrontendTools(),
      // this.getProductOwners(),
      this.getCarlaFunctions(),
      // this.getTags(),
    ]);
  }

  // Lov Get Calls //
  public static getArts(): Promise<IDataProductListItem[]> {
    return this.getReport('lov/agilereleasetrains');
  }
  public static getPlatforms(): Promise<IDataProductListItem[]> {
    return this.get('platforms');
  }
  public static getFrontendTools(): Promise<IDataProductListItem[]> {
    return this.getReport('lov/frontendtechnologies');
  }
  // public static getProductOwners(): Promise<IDataProductListItem[]> {
  //   return this.getReport('lov/productowners');
  // }
  public static getCarlaFunctions(): Promise<IDataProductListItem[]> {
    return this.get('carlafunctions');
  }
  // public static getTags(): Promise<IDataProductListItem[]> {
  //   return this.get('tags');
  // }
}
