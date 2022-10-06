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

const getAllInputFiles = (id) => {
  return server.get(`/forecasts/${id}/inputs`, {
    data: {},
  });
};

const createForecastRun = (data, id) => {
  return formServer.post(`/forecasts/${id}/runs`, {
    data,
  });
};

const getForecastRuns = (id) => {
  return server.get(`/forecasts/${id}/runs`, {
    data: {},
  });
};

const deleteForecastRun = (id, rid) => {
  return server.delete(`/forecasts/${id}/runs/${rid}`, {
    data: {},
  });
};

export const chronosApi = {
    getAllForecastProjects,
    getForecastProjectById,
    createForecastProject,
    getAllInputFiles,
    createForecastRun,
    deleteForecastRun,
    getForecastRuns
};
