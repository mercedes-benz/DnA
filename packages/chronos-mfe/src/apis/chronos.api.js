import { server } from '../server/api';
import { formServer } from '../server/formApi';

const getAllForecastProjects = () => {
  return server.get('/forecasts?limit=0&offset=0', {
    data: {},
  });
};

const getForecastProjectById = (id) => {
  return server.get(`/forecasts/${id}`, {
    data: {},
  });
};

const createForecastProject = (data) => {
  return server.post('/forecasts', {
    data,
  });
};

const deleteForecastProject = (id) => {
  return server.delete(`/forecasts/${id}`, {
    data: {},
  });
};

const getAllInputFiles = (id) => {
  return server.get(`/forecasts/${id}/inputs`, {
    data: {},
  });
};

const updateForecastProjectCollaborators = (data, id) => {
  return server.put(`/forecasts/${id}`, data);
};

const createForecastRun = (data, id) => {
  return formServer.post(`/forecasts/${id}/runs`, data);
};

const getForecastRuns = (id) => {
  return server.get(`/forecasts/${id}/runs`, {
    data: {},
  });
};

const getForecastRun = (id, rid) => {
  return server.get(`/forecasts/${id}/runs/${rid}`, {
    data: {},
  });
};

const deleteForecastRun = (id, rid) => {
  return server.delete(`/forecasts/${id}/runs/${rid}`, {
    data: {},
  });
};

const deleteForecastRuns = (rids, id) => {
  return server.delete(`/forecasts/${id}/runs`, { ids: rids });
};

const getConfigurationFiles = () => {
  return server.get(`/forecasts/default-config/files`, {
    data: {},
  });
};

export const chronosApi = {
    getAllForecastProjects,
    getForecastProjectById,
    createForecastProject,
    deleteForecastProject,
    updateForecastProjectCollaborators,
    getAllInputFiles,
    createForecastRun,
    getForecastRun,
    deleteForecastRun,
    getForecastRuns,
    deleteForecastRuns,
    getConfigurationFiles
};
