import { server } from '../server/api';

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
  return server.post(`/forecasts/${id}`, {
    data,
  });
};

export const chronosApi = {
    getAllForecastProjects,
    getForecastProjectById,
    createForecastProject,
    getAllInputFiles,
    createForecastRun
};
