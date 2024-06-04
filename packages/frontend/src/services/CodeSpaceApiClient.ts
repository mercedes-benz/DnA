import { EventSourcePolyfill } from 'event-source-polyfill';
import { Envs } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';
import { ReportsApiClient } from './ReportsApiClient';

const baseUrl = Envs.CODE_SPACE_API_BASEURL
  ? Envs.CODE_SPACE_API_BASEURL
  : `http://${window.location.hostname}:7979/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
};

const baseUrlVault = Envs.VAULT_API_BASEURL
  ? Envs.VAULT_API_BASEURL
  : `http://${window.location.hostname}:8000`;
const getUrlVault = (endpoint: string) => {
  return `${baseUrlVault}/${endpoint}`;
};

const baseUrlStorage = Envs.DASHBOARD_API_BASEURL
  ? Envs.DASHBOARD_API_BASEURL
  : `http://${window.location.hostname}:7175/api`;
const getUrlStorage = (endpoint: string) => {
  return `${baseUrlStorage}/${endpoint}`;
};

const getUrlHub = (endpoint: string) => {
  return `${new URL('../hub/api/', baseUrl).href}${endpoint}`;
};

export class CodeSpaceApiClient {
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

  public static getStorage(endpoint: string) {
    return ApiClient.fetch(getUrlStorage(endpoint), HTTP_METHOD.GET);
  }

  public static getVault(endpoint: string) {
    return ApiClient.fetch(getUrlVault(endpoint), HTTP_METHOD.GET);
  }

  public static putVault(endpoint: string, body?: any) {
    return ApiClient.fetch(getUrlVault(endpoint), HTTP_METHOD.PUT, body);
  }

  public static getCodeSpacesList() {
    return this.get('workspaces');
  }
  public static createCodeSpace(data: any) {
    return this.post('workspaces', data);
  }
  public static editCodeSpace(id: string, data: any){
    return this.patch(`workspaces/${id}/datagovernance`, data);
  }
  public static getCodeSpaceStatus(id: string) {
    return this.get(`workspaces/status/${id}`);
  }
  public static deleteCodeSpace(id: string) {
    return this.delete(`workspaces/${id}`);
  }
  public static getCodeSpacesGitBranchList(repoName: string) {
    return this.get(`workspaces/${repoName}/branches`);
  }
  public static deployCodeSpace(id: string, data: any) {
    return this.post(`workspaces/${id}/deploy`, data);
  }
  public static unDeployCodeSpace(id: string, data: any) {
    return this.delete(`workspaces/${id}/deploy`, data);
  }
  public static onBoardCollaborator(id: string, data: any) {
    return this.put(`workspaces/${id}`, data);
  }

  public static addCollaborator(id: string, data: any) {
    return this.post(`workspaces/${id}/collaborator`, data);
  }

  public static deleteCollaborator(id: string, userId: string) {
    return this.delete(`workspaces/${id}/collaborator/${userId}`);
  }

  public static transferOwnership(id: string, data: any) {
    return this.patch(`workspaces/${id}/projectowner`, data);
  }

  public static assignAdminRole(id: string, userId: string, data: any){
    return this.post(`workspaces/${id}/collaborator/${userId}/admin?isAdmin=${data}`);
  }
  
  // Usage statistics
  public static getWorkSpacesTransparency(): Promise<any> {
    return this.get('workspaces/transparency');
  }

  public static createOrUpdateCodeSpaceConfig(id: string, data: any, env: string) {
    return this.patch(`workspaces/${id}/config?env=${env}`, data);
  }

  public static getCodeSpaceConfig(id: string, env: string): Promise<any[]> {
    return this.get(`/workspaces/${id}/config?env=${env}`)
  };

  public static getPublishedConfig(id: string, env: string): Promise<any[]> {
    return this.get(`/workspaces/${id}/config/publish?env=${env}`)
  };

  public static getEntitlements(id: string): Promise<any[]> {
    return this.get(`/workspaces/${id}/config/entitlements`)
  };

  public static getRoles(id: string): Promise<any[]> {
    return this.get(`/workspaces/${id}/config/roles`)
  };

  public static getRolesMappings(id: string): Promise<any[]> {
    return this.get(`/workspaces/${id}/config/mappings`)
  };

  public static addCodeSpaceRequest(id: string, env: string): Promise<any[]> {
    return this.post(`/workspaces/${id}/config/publish?env=${env}`)
  };

  public static createCodeSpaceRecipe(data: any) {
    return this.post('recipeDetails', data);
  }

  public static verifyGitUser(data: any) {
    return this.post('recipeDetails/validate', data);
  }

  public static getCodeSpaceRecipeRequests() {
    return this.get('recipeDetails')
  }

  public static getCodeSpaceRecipe(id: string){
    return this.get(`recipeDetails/${id}`);
  }

  public static getCodeSpaceRecipesStatus() {
    return this.get(`recipeDetails/recipesByStatus`);
  }

  public static acceptCodeSpaceRecipeRequest (name:string): Promise<any[]>{
    return this.post(`recipeDetails/${name}/accept`);
  };

  public static publishCodeSpaceRecipeRequest (name:string): Promise<any[]>{
    return this.post(`recipeDetails/${name}/publish`);
  };

  // public static getRecipeLov(){
  //  return this.get('recipeDetails/recipeLov');
  // }

  public static getSoftwareLov(){
    return this.get('recipeDetails/softwareLov');
  }

  public static getLovData(): Promise<any[]>{
    return Promise.all([
      this.getStorage(`lov/dataclassifications`),
      ApiClient.get('divisions'),
      ReportsApiClient.get('departments'),
    ]);
  };
  
  public static getWorkspaceConfigs(): Promise<any[]>{
    return this.get(`/workspaces/configs`)
  };

  public static acceptSecurityConfigRequest (id:string): Promise<any[]>{
    return this.post(`/workspaces/${id}/config/accept`)
  };

  public static publishSecurityConfigRequest (id:string): Promise<any[]>{
    return this.post(`/workspaces/${id}/config/publish`)
  };

  public static read_secret(codeSpaceName: string, env: string){
    return this.getVault(`/secret/${codeSpaceName}/${env}`)
  };

  public static update_secret(path: string, secret_value: any, env: string): Promise<any[]> {
    return this.putVault(`/secret/${path}/${env}`, secret_value);
  }

  public static startStopWorkSpace(id: string, serverStarted: boolean): Promise<any> {
    if (serverStarted) return this.delete(`/workspaces/server/${id}`);
    return this.post(`/workspaces/startserver/${id}`);
  }

  public static workSpaceStatus(): Promise<any> {
    return this.get(`/workspaces/serverstatus`);
  }

  public static serverStatusFromHub(userId: string, workspaceId: string, onMessageCB: (e: any) => void, onCloseCB?: (e: any) => void) {
    const sse = new EventSourcePolyfill(getUrlHub(`users/${userId}/servers/${workspaceId}/progress`), {
      withCredentials: true,
      headers: { Authorization: ApiClient.readJwt() },
    });

    sse.onmessage = onMessageCB;

    sse.onerror = (e: any) => {
      onCloseCB && onCloseCB(e);
      console.log(e);
      console.error('Event stream closed');
      sse.close();
    };
  }
}
