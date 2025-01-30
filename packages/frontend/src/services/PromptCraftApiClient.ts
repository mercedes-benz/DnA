import { subscriptions } from 'components/mbc/promptCraft/data/mock';
import { Envs } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';

const baseUrl = Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

export class PromptCraftApiClient {
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

  // Usage statistics
  public static getPromptCraftSubscriptions(offset: number, limit: number): Promise<any> {
    // return this.get(`promptcraftsubs?offset=${offset}&limit=${limit}`);
    console.log(offset, limit);
    return Promise.resolve(subscriptions);
  }
  public static createPromptCraftSubscription(data: any) {
    return this.post('promptcraftsubs', data);
  }
  public static editPromptCraftSubscription(id: string, data: any){
    return this.patch(`promptcraftsubs/${id}`, data);
  }
  public static deletePromptCraftSubscription(id: string) {
    return this.delete(`promptcraftsubs/${id}`);
  }
}
