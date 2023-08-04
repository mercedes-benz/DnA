import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../Utility/constants';
import { Envs } from '../Utility/envs';
import { refreshToken } from 'dna-container/RefreshToken';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.DATA_PRODUCT_API_BASEURL
  ? Envs.DATA_PRODUCT_API_BASEURL
  : `http://${window.location.hostname}:7184/api`;

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

export const reportsServer = axios.create({
  baseURL: Envs.REPORTS_API_BASEURL ? Envs.REPORTS_API_BASEURL : `http://${window.location.hostname}:7173/api`,
  headers,
});

function createRefreshInterceptor(instance) {
  // Interceptor to handle token refresh on 403 error
  instance.interceptors.response.use(
    (response) => response,
    async (error) => {
      console.log('error', error);
      if (error?.response && error?.response.status === 403 && error?.response?.data?.error_description?.includes("JWT is expired")) {
        // Refresh the token and retry the failed request.
        const jwt = sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
        const newJwt = await refreshToken(jwt);

        // Update the Authorization header in Axios instances.
        server.defaults.headers.Authorization = newJwt;
        reportsServer.defaults.headers.Authorization = newJwt;
        hostServer.defaults.headers.Authorization = newJwt;

        // Retry the original request with the new token.
        error.config.headers.Authorization = sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
        return await axios.request(error.config);
      }
      return Promise.reject(error);
    }
  );
}

// Apply interceptor to server
createRefreshInterceptor(server);

// Apply interceptor to hostServer
createRefreshInterceptor(hostServer);

// Apply interceptor to reportsServer
createRefreshInterceptor(reportsServer);
