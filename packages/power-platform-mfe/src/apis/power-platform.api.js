import { server, hostServer, reportsServer, storageServer } from '../server/api';

const getPowerPlatformEnvironments = (offset, limit) => {
  return server.get(`/powerapps?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createPowerPlatformEnvironment = (data) => {
  return server.post(`/powerapps`, {
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

const getSubDivisions = (id) => {
  return hostServer.get(`/subdivisions/${id}`, {
    data: {},
  });
}

export const powerPlatformApi = {
  getPowerPlatformEnvironments,
  createPowerPlatformEnvironment,
  getLovData,
  getSubDivisions,
};
