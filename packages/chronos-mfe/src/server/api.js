import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../utilities/constants';
import { Envs } from '../utilities/envs';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.CHRONOS_API_BASEURL
  ? Envs.CHRONOS_API_BASEURL
  : `http://${window.location.hostname}:8989/api`;

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
