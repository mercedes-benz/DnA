import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../components/Utility/constants';
import { Envs } from '../components/Utility/envs';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.STORAGE_API_BASEURL
  ? Envs.STORAGE_API_BASEURL
  : `http://${window.location.hostname}:7175/api`;

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

export const trinoServer = axios.create({
  baseURL: Envs.TRINO_API_BASEURL ? Envs.TRINO_API_BASEURL : `http://${window.location.hostname}:7575/api`,
  headers,
});
