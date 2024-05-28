import { server, hostServer, reportsServer, storageServer } from '../server/api';

const getPowerPlatformWorkspaces = (offset, limit) => {
  return server.get(`/power-platform-workspaces?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createPowerPlatformWorkspace = (data) => {
  return server.post(`/power-platform-workspaces`, {
    data,
  });
};

const getPowerPlatformWorkspace = (id) => {
  return server.get(`/power-platform-workspaces/${id}`, {
    data: {},
  });
};

const updatePowerPlatformWorkspace = (id, data) => {
  return server.put(`/power-platform-workspaces/${id}`, data);
};

const deletePowerPlatformWorkspace = (id) => {
  return server.delete(`/power-platform-workspaces/${id}`, {
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
  getPowerPlatformWorkspaces,
  getPowerPlatformWorkspace,
  createPowerPlatformWorkspace,
  updatePowerPlatformWorkspace,
  deletePowerPlatformWorkspace,
  getLovData,
};
