import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../components/Utility/constants';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = process.env.API_BASEURL
  ? `storage/${process.env.API_BASEURL}`
  : `http://${window.location.hostname}:7175/storage/api`;

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
    const appUrl = process.env.API_BASEURL ? process.env.API_BASEURL : `http://${window.location.hostname}:7171/api`;
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
