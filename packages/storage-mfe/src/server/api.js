import axios from 'axios';
const jwt = sessionStorage.length ? sessionStorage.getItem('jwt') : null;

export const baseURL = process.env.API_BASEURL
  ? `persistence/${process.env.API_BASEURL}`
  : `http://${window.location.hostname}:7175/persistence/api`;

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
