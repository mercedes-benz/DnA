import { Envs, } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';

const baseUrl = Envs.DATA_PIPELINES_API_BASEURL
  ? Envs.DATA_PIPELINES_API_BASEURL
  : `http://${window.location.hostname}:7172/airflow/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};


export class PipelineApiClient {
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


  public static getPipelineProjectList() {
    return this.get('v1/projects');
  }
  public static addNewProject(data: any) {
    return this.post('v1/projects', data);
  }
  public static putExistingProject(id: string, data: any) {
    return this.put('v1/projects/' + id, data);
  }
  public static getExistingProject(id: string) {
    return this.get('v1/projects/' + id);
  }
  public static getUniqProjectId() {
    return this.get('v1/projects/projectid');
  }
  public static getSpecificDagDetails(dagNaem: string) {
    return this.get('v1/dags/' + dagNaem);
  }
  public static putDag(dagData: any) {
    return this.put('v1/dags/', dagData);
  }

  public static getUniqueDagName(dagName: string) {
    return this.put('v1/dags/', dagName);
  }
  public static getUniquePID() {
    return this.get('v1/projects/projectid');
  }
  public static deleteDag(dagName: string, projectName: string) {
    return this.delete('v1/dags/' + dagName + '/' + projectName);
  }
  public static getDagPermissions(dagName: string, projectId: string) {
    return this.get('v1/dags/' + dagName + '/' + projectId + '/permission');
  }
  public static updateDagPermissions(dagName: string, projectId: string, data: any ) {
    return this.put('v1/dags/' + dagName + '/' + projectId + '/permission', data);
  }
  
}
