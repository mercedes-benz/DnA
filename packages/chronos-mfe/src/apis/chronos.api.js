import { server, storageServer, storageServerX } from '../server/api';
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

const deleteSavedInputFile = (id, rid) => {
  return server.delete(`/forecasts/${id}/inputs/${rid}`, {
    data: {},
  });
};

const deleteForecastRuns = (rids, id) => {
  return server.delete(`/forecasts/${id}/runs`, { ids: rids });
};

const getConfigurationFiles = (id) => {
  return server.get(`/forecasts/default-config/files?id=${id}`, {
    data: {},
  });
};

const generateApiKeyById = (id) => {
  return server.post(`/forecasts/${id}/apikey`, {
    data: {},
  });
};

const getApiKeyById = (id) => {
  return server.get(`/forecasts/${id}/apikey`, {
    data: {},
  });
};

const getHTML = (projectName, resultFolderName, fileName) => {
  return storageServer.get(`/buckets/${projectName}/objects/metadata?prefix=results%2F${resultFolderName}%2Fvisuals%2F${fileName}`, {
    data: {},
  });
};

const getFile = (projectName, resultFolderName, fileName) => {
  return storageServer.get(`/buckets/${projectName}/objects/metadata?prefix=results%2F${resultFolderName}%2F${fileName}`, {
    data: {},
  });
};

const getExcelFile = (projectName, resultFolderName, fileName) => {
  return storageServerX.get(`/buckets/${projectName}/objects/metadata?prefix=results%2F${resultFolderName}%2F${fileName}`, {
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
    deleteSavedInputFile,
    getConfigurationFiles,
    generateApiKeyById,
    getApiKeyById,
    getHTML,
    getFile,
    getExcelFile,
};
