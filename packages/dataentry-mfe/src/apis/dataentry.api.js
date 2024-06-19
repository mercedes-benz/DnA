import { server, hostServer, reportsServer, storageServer, datalakeServer } from '../server/api';

const getDataEntryProjects = (offset, limit) => {
  return server.get(`/dataentries?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createDataEntryProject = (data) => {
  return server.post(`/dataentries`, data);
};

const getDataEntryProject = (id) => {
  return server.get(`/dataentries/${id}`, {
    data: {},
  });
};

const updateDataEntryProject = (id, data) => {
  return server.put(`/dataentries/${id}`, data);
};

const deleteDataEntryProject = (id) => {
  return server.delete(`/dataentries/${id}`, {
    data: {},
  });
};

const getDatalakeProjects = (offset, limit) => {
  return datalakeServer.get(`/datalakes?limit=${limit}&offset=${offset}`, {
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

export const dataEntryApi = {
  getDataEntryProjects,
  getDataEntryProject,
  createDataEntryProject,
  updateDataEntryProject,
  deleteDataEntryProject,
  getDatalakeProjects,
  getLovData,
};
