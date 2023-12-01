import { Envs } from '../globals/Envs';
import { HTTP_METHOD } from '../globals/constants';
import { ApiClient } from './ApiClient';

const baseUrl = Envs.CODE_SPACE_API_BASEURL
  ? Envs.CODE_SPACE_API_BASEURL
  : `http://${window.location.hostname}:7979/api`;
const getUrl = (endpoint: string) => {
  return `${baseUrl}/${endpoint}`;
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

  public static getCodeSpacesList() {
    return this.get('workspaces');
  }
  public static createCodeSpace(data: any) {
    return this.post('workspaces', data);
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

  // Usage statistics
  public static getWorkSpacesTransparency(): Promise<any> {
    return this.get('workspaces/transparency');
  }

  public static createOrUpdateCodeSpaceConfig(id: string, data: any) {
    return this.patch(`workspaces/${id}/config`, data);
  }

  public static getCodeSpaceConfig(id: string): Promise<any[]> {
    return this.get(`/workspaces/${id}/config`)
  };

  public static getPublishedConfig(id: string): Promise<any[]> {
    return this.get(`/workspaces/${id}/config/publish`)
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

  public static addCodeSpaceRequest(id: string): Promise<any[]> {
    return this.post(`/workspaces/${id}/config/request`)
  };

}
