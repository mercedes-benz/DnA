import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../components/Utility/constants';
import { Envs } from '../components/Utility/envs';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.STORAGE_API_BASEURL
  ? Envs.STORAGE_API_BASEURL
  : `http://${window.location.hostname}:7175/api`;

const server = axios.create({
  baseURL,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: jwt,
  },
});

server.interceptors.request.use((config) => {
  if (['/login', '/verifyLogin'].includes(config.url)) {
    const appUrl = Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`;
    config.baseURL = appUrl;
    config.data = {};
    config.headers = {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: jwt,
    };
  }
  return config;
});

export default server;
