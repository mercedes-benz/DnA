import { server, hostServer, reportsServer, storageServer } from '../server/api';

const getDataEntryProjects = (offset, limit) => {
  return server.get(`/data-entry-projects?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createDataEntryProject = (data) => {
  return server.post(`/data-entry-projects`, {
    data,
  });
};

const getDataEntryProject = (id) => {
  return server.get(`/data-entry-projects/${id}`, {
    data: {},
  });
};

const updateDataEntryProject = (id, data) => {
  return server.put(`/data-entry-projects/${id}`, data);
};

const deleteDataEntryProject = (id) => {
  return server.delete(`/data-entry-projects/${id}`, {
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
  getLovData,
};
