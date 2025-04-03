import { dbServices } from '../data/mock';
import { server, hostServer, storageServer, reportsServer } from '../server/api';

const getDBServices = (offset, limit) => {
  // return server.get(`/dbservices?limit=${limit}&offset=${offset}`, {
  //   data: {},
  // });
  console.log(offset, limit);
  return Promise.resolve(dbServices);
};

const createDBService = (data) => {
  return server.post(`/dbservices`, {
    data,
  });
};

const getDBService = (id) => {
  return server.get(`/dbservices/${id}`, {
    data: {},
  });
};

const updateDBService = (id, data) => {
  return server.put(`/dbservices/${id}`, data);
};

const deleteDBService = (id) => {
  return server.delete(`/dbservices/${id}`, {
    data: {},
  });
};

const getLovData = () => {
  return Promise.all([
    storageServer.get(`/classifications`, {
      data: {},
    }),
    hostServer.get('/divisions'),
    reportsServer.get('/departments', {
      data: {},
    }),
  ]);
}

export const dbServiceApi = {
  getDBServices,
  getDBService,
  createDBService,
  updateDBService,
  deleteDBService,
  getLovData,
};
