import { server } from '../server/api';

const getDBServices = (offset, limit) => {
  return server.get(`/fabric-workspaces?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createDBService = (data) => {
  return server.post(`/fabric-workspaces`, {
    data,
  });
};

const getDBService = (id) => {
  return server.get(`/fabric-workspaces/${id}`, {
    data: {},
  });
};

const updateDBService = (id, data) => {
  return server.put(`/fabric-workspaces/${id}`, data);
};

const deleteDBService = (id) => {
  return server.delete(`/fabric-workspaces/${id}`, {
    data: {},
  });
};

export const dbServiceApi = {
  getDBServices,
  getDBService,
  createDBService,
  updateDBService,
  deleteDBService,
};
