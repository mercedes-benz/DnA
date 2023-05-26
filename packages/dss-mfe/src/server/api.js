import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../Utility/constants';
import { Envs } from '../Utility/envs';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.DATAIKU_API_BASEURL
  ? Envs.DATAIKU_API_BASEURL
  : `http://${window.location.hostname}:7777/api`;

const headers = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
  Authorization: jwt,
};

export const server = axios.create({
  baseURL,
  headers,
});

export const storageServer = axios.create({
  baseURL: Envs.STORAGE_API_BASEURL ? Envs.STORAGE_API_BASEURL : `http://${window.location.hostname}:7175/api`,
  headers
});

export const reportsServer = axios.create({
  baseURL: Envs.REPORTS_API_BASEURL ? Envs.REPORTS_API_BASEURL : `http://${window.location.hostname}:7173/api`,
  headers,
});

export const hostServer = axios.create({
  baseURL: Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`,
  headers,
});

storageServer.interceptors.request.use((config) => {
  if (config.method === 'get') {
    config.data = {}; // Send an empty object as the request body
  }
  return config;
});

reportsServer.interceptors.request.use((config) => {
  if (config.method === 'get') {
    config.data = {}; // Send an empty object as the request body
  }
  return config;
});

server.interceptors.request.use((config) => {
  if (config.method === 'get') {
    config.data = {}; // Send an empty object as the request body
  }
  return config;
});
