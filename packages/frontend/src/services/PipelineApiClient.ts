import { Envs, } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';
import { projects } from '/home/coder/app/packages/frontend/public/mock';

const baseUrl = Envs.DATA_PIPELINES_API_BASEURL
  ? Envs.DATA_PIPELINES_API_BASEURL
  : `http://${window.location.hostname}:7172/airflow/api`;

const vaultBaseUrl = Envs.DNA_VAULT_API_BASEURL
  ? Envs.DNA_VAULT_API_BASEURL
  : `http://${window.location.hostname}:8080`;

const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};
const getVaultUrl = (endpoint: string) => `${vaultBaseUrl}/${endpoint}`;

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
  public static getVault(endpoint: string) {
    return ApiClient.fetch(getVaultUrl(endpoint), HTTP_METHOD.GET);
  }
  public static putVault(endpoint: string, body?: any) {
    return ApiClient.fetch(getVaultUrl(endpoint), HTTP_METHOD.PUT, body);
  }


  public static getPipelineProjectList() {
    // return this.get('v1/projects');
     return Promise.resolve(projects.records);
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
  public static getPiplineStatus(projectId: string) {
    return this.get(`v1/projects/status/${projectId}`);
  }
  public static getVaultSecret(dagName: string) {
    return this.getVault(`airflow/secret/${dagName}`);
  }
  public static putVaultSecret(dagName: string, data: any) {
    return this.putVault(`airflow/secret/${dagName}`, data);
  }
}
