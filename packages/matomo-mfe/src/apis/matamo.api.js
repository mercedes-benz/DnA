import { hostServer, server, reportsServer } from '../server/api';

const getMatomoProjectsList = () => {
  return server.get(`/matomo/projects`, {
    data: {},
  });
};

const getMatomoByName = (siteName) => {
  return server.get(`/matomo/${siteName}`, {
    data: {},
  });
};

const createMatomo = (data) => {
  return server.post('/matomo', {
    data,
  });
};

const updateMatomo = (data) => {
  return server.put('/matomo', {
    data,
  });
};

const deleteMatomo = (bucketName) => {
  return server.delete(`/matomo/${bucketName}`, {
    data: {},
  });
};

const getConnectionInfo = (siteName) => {
  return server.get(`/matomo/${siteName}/connect`, { data: {} });
};

const getDataConnectionTypes = () => {
  return server.get('/classifications', { data: {} });
};

const getDataikuProjects = (live) => {
  return hostServer.get(`/dataiku/projects?live=${live}`, { data: {} });
};

const connectToDataikuProjects = (data) => {
  return server.post('/matomo/dataiku/connect', data);
};

const connectToJupyterNotebook = (data) => {
  return server.post('/matomo/dataiku/connect', data);
};

const transferOwnership = (bucketName, userId) => {
  return server.patch(`/matomo/${bucketName}/reAssignOwner/${userId}`, {});
};


const getDepartments = () => {
  return reportsServer.get('/departments', {
    data: {},
  });
};

export const matomoApi = {
  getMatomoProjectsList,
  getMatomoByName,
  createMatomo,
  updateMatomo,
  deleteMatomo,
  getConnectionInfo,
  getDataConnectionTypes,
  getDataikuProjects,
  connectToDataikuProjects,
  connectToJupyterNotebook,
  transferOwnership,
  getDepartments
};
