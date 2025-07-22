import { server, hostServer, storageServer, reportsServer } from '../server/api';

const getDBServices = (offset, limit) => {
  return server.get(`/dbService?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createDBService = (data) => {
  return server.post(`/dbService`, data);
};

const getDBService = (id) => {
  return server.get(`/dbService/${id}`, {
    data: {},
  });
};

const updateDBService = (id, data) => {
  return server.patch('/dbService', { id, ...data });
};

const deleteDBService = (id) => {
  return server.delete(`/dbService/${id}`, {
    data: {},
  });
};

const transferOwnership = (bucketName, userId) => {
  return server.patch(`/dbService/${bucketName}/reAssignOwner/${userId}`, {});
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
  transferOwnership,
  getLovData,
};
