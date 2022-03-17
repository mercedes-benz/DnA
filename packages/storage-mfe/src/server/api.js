import axios from 'axios';
const jwt = sessionStorage.length ? sessionStorage.getItem('jwt') : null;

const server = axios.create({
  baseURL: process.env.API_BASEURL ? process.env.API_BASEURL : `http://${window.location.hostname}:7171/api/`,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: jwt,
  },
});

export default server;
