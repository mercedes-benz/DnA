import { server, hostServer, reportsServer, storageServer } from '../server/api';

const getFabricWorkspaces = (offset, limit) => {
  return server.get(`/fabric-workspaces?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createFabricWorkspace = (data) => {
  return server.post(`/fabric-workspaces`, {
    data,
  });
};

const getFabricWorkspace = (id) => {
  return server.get(`/fabric-workspaces/${id}`, {
    data: {},
  });
};

const updateFabricWorkspace = (id, data) => {
  return server.put(`/fabric-workspaces/${id}`, data);
};

const deleteFabricWorkspace = (id) => {
  return server.delete(`/fabric-workspaces/${id}`, {
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

export const fabricApi = {
  getFabricWorkspaces,
  getFabricWorkspace,
  createFabricWorkspace,
  updateFabricWorkspace,
  deleteFabricWorkspace,
  getLovData,
};
