import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../utilities/constants';
import { Envs } from '../utilities/envs';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.CHRONOS_API_BASEURL
  ? Envs.CHRONOS_API_BASEURL
  : `http://${window.location.hostname}:8989/api`;

const headers = {
  Accept: 'application/json',
  'Content-Type': 'multipart/form-data',
  Authorization: jwt,
};

export const formServer = axios.create({
  baseURL,
  headers,
});

export const hostServer = axios.create({
  baseURL: Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`,
  headers,
});
