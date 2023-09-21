import { hostServer, server, reportsServer, storageServer } from '../server/api';

const getMatomoProjectsList = (limit=10, offset=1) => {
  return server.get(`/matomo?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const getMatomoById = (id) => {
  return server.get(`/matomo/${id}/sites`, {
    data: {},
  });
};

const createMatomo = (data) => {
  return server.post('/matomo', {
    data,
  });
};

const updateMatomo = (data) => {
  return server.put(`/matomo/${data.id}/sites`, data);
};

const deleteMatomo = (id) => {
  return server.delete(`/matomo/${id}/sites`, {
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

export const matomoApi = {
  getMatomoProjectsList,
  getMatomoById,
  createMatomo,
  updateMatomo,
  deleteMatomo,
  getConnectionInfo,
  getDataConnectionTypes,
  getDataikuProjects,
  connectToDataikuProjects,
  connectToJupyterNotebook,
  transferOwnership,
  getDepartments,
  getLovData
};
