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
    return Promise.resolve({
      totalCount: 1,
      records: [
        {
          id: '1110e328-1339-41e0-941c-1ba9e3ae73b2',
          name: 'wsx1',
          description: null,
          owner: 'BEALURI',
          recipeId: 'microservice',
          intiatedOn: '2022-08-25T10:18:39.460+00:00',
          lastDeployedOn: '2022-08-25T10:29:18.389+00:00',
          deploymentUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/demo',
          workspaceUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/?folder=/home/coder/projects/demo',
          environment: 'Development',
          cloudServiceProvider: 'DHC-CaaS',
          ramSize: '1',
          ramMetrics: 'GB',
          cpuCapacity: '1',
          operatingSystem: 'Debian-OS-11',
          status: 'DEPLOYED',
        },
      ],
    });
    // return this.get('workspaces');
  }
  public static createCodeSpace(data: any) {
    return Promise.resolve({
      data: {
        id: '1110e328-1339-41e0-941c-1ba9e3ae73b2',
        name: 'wsx1',
        description: null,
        owner: 'BEALURI',
        recipeId: 'microservice',
        intiatedOn: '2022-08-25T10:18:39.460+00:00',
        lastDeployedOn: '2022-08-25T10:29:18.389+00:00',
        deploymentUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/demo',
        workspaceUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/?folder=/home/coder/projects/demo',
        environment: 'Development',
        cloudServiceProvider: 'DHC-CaaS',
        ramSize: '1',
        ramMetrics: 'GB',
        cpuCapacity: '1',
        operatingSystem: 'Debian-OS-11',
        status: 'CREATE_REQUESTED',
      },
      errors: []
    });
    // return this.post('workspaces', data);
  }
  public static getCodeSpaceById(id: string) {
    return Promise.resolve({
      id: '1110e328-1339-41e0-941c-1ba9e3ae73b2',
      name: 'wsx1',
      description: null,
      owner: 'BEALURI',
      recipeId: 'microservice',
      intiatedOn: '2022-08-25T10:18:39.460+00:00',
      lastDeployedOn: '2022-08-25T10:29:18.389+00:00',
      deploymentUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/demo',
      workspaceUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/?folder=/home/coder/projects/demo',
      environment: 'Development',
      cloudServiceProvider: 'DHC-CaaS',
      ramSize: '1',
      ramMetrics: 'GB',
      cpuCapacity: '1',
      operatingSystem: 'Debian-OS-11',
      status: 'CREATED',
    });
    // return this.get(`workspaces/${id}`);
  }
  public static getCodeSpaceStatus(name: string) {
    return Promise.resolve({
      id: '1110e328-1339-41e0-941c-1ba9e3ae73b2',
      name: 'wsx1',
      description: null,
      owner: 'BEALURI',
      recipeId: 'microservice',
      intiatedOn: '2022-08-25T10:18:39.460+00:00',
      lastDeployedOn: '2022-08-25T10:29:18.389+00:00',
      deploymentUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/demo',
      workspaceUrl: 'https://code-spaces.dev.dna.app.corpintra.net/bealuri/wsx1/?folder=/home/coder/projects/demo',
      environment: 'Development',
      cloudServiceProvider: 'DHC-CaaS',
      ramSize: '1',
      ramMetrics: 'GB',
      cpuCapacity: '1',
      operatingSystem: 'Debian-OS-11',
      status: 'DEPLOYED',
    });
    // return this.get(`workspaces/status/${name}`);
  }
  public static deleteCodeSpace(id: string) {
    return this.delete(`workspaces/${id}`);
  }
  public static deployCodeSpace(id: string) {
    return Promise.resolve({ errors: [], success: 'SUCCESS', warnings: [] });
    // return this.post(`workspaces/deploy/${id}`);
  }
  public static unDeployCodeSpace(id: string) {
    return this.delete(`workspaces/deploy/${id}`);
  }
}
