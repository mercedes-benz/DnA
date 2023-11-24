import { server, hostServer, reportsServer, storageServer } from '../server/api';

const getDatalakeProjectsList = (limit, offset) => {
  return server.get(`/datalakes?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createDatalakeProject = (data) => {
  return server.post(`/datalakes`, {
    data,
  });
};

const getDatalakeProject = (id) => {
  return server.get(`/datalakes/${id}`, {
    data: {},
  });
};

const updateDatalakeProject = (id, data) => {
  return server.put(`/datalakes/${id}`, {
    data,
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

export const datalakeApi = {
  getDatalakeProjectsList,
  createDatalakeProject,
  getDatalakeProject,
  updateDatalakeProject,
  getLovData,
};
