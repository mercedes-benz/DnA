import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../utilities/constants';
import { Envs } from '../utilities/envs';
import { refreshToken } from 'dna-container/RefreshToken';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.POWER_PLATFORM_API_BASEURL
  ? Envs.POWER_PLATFORM_API_BASEURL
  : `http://${window.location.hostname}:9191/api`;

const headers = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
  Authorization: jwt,
};

export const server = axios.create({
  baseURL,
  headers,
});

export const hostServer = axios.create({
  baseURL: Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`,
  headers,
});

export const storageServer = axios.create({
  baseURL: Envs.STORAGE_API_BASEURL ? Envs.STORAGE_API_BASEURL : `http://${window.location.hostname}:7175/api`,
  headers,
});

export const storageServerX = axios.create({
  baseURL: Envs.STORAGE_API_BASEURL ? Envs.STORAGE_API_BASEURL : `http://${window.location.hostname}:7175/api`,
  headers,
  responseType: "arraybuffer",
});

export const reportsServer = axios.create({
  baseURL: Envs.REPORTS_API_BASEURL ? Envs.REPORTS_API_BASEURL : `http://${window.location.hostname}:7173/api`,
  headers,
});

function arrayBufferToJson(arrayBuffer) {
  const text = new TextDecoder().decode(arrayBuffer);
  try {
    return JSON.parse(text);
  } catch (e) {
    return text;
  }
}

function createRefreshInterceptor(instance) {
  // Interceptor to handle token refresh on 403 error
  instance.interceptors.response.use(
    (response) => response,
    async (error) => {
      // check if the response exists and it is a ArrayBuffer
      if (error.response && error.response.data instanceof ArrayBuffer) {
        // convert the ArrayBuffer to JSON
        error.response.data = await arrayBufferToJson(error.response.data);
      }
      console.log('error', error);
      if (error?.response && error?.response.status === 403 && error?.response?.data?.error_description?.includes("JWT is expired")) {
        // Refresh the token and retry the failed request.
        const jwt = sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
        const newJwt = await refreshToken(jwt);

        // Update the Authorization header in Axios instances.
        server.defaults.headers.Authorization = newJwt;
        hostServer.defaults.headers.Authorization = newJwt;
        storageServer.defaults.headers.Authorization = newJwt;
        storageServerX.defaults.headers.Authorization = newJwt;
        reportsServer.defaults.headers.Authorization = newJwt;

        // Retry the original request with the new token.
        error.config.headers.Authorization = newJwt;
        return await instance.request(error.config);
      }
      if (error?.message && (error.message.includes("Network Error") || error.message.includes("ERR_NAME_NOT_RESOLVED"))) {
        // Customize the error message
        const customErrorMessage = {
          error: true,
          message: "Failed to connect to server. Please check your VPN/Internet.",
        };
        // Modify the error response to use your custom error message
        error.response = {
          ...error.response,
          data: {
            errors: [customErrorMessage]
          }
        };
        return Promise.reject(error);
      } else {
        return Promise.reject(error);
      }
    }
  );
}

// Apply interceptor to server
createRefreshInterceptor(server);

// Apply interceptor to hostServer
createRefreshInterceptor(hostServer);

// Apply interceptor to storageServer
createRefreshInterceptor(storageServer);

// Apply interceptor to storageServerX
createRefreshInterceptor(storageServerX);

// Apply interceptor to reportsServer
createRefreshInterceptor(reportsServer);