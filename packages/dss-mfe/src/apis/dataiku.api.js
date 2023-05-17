import { hostServer, server, storageServer, reportsServer } from '../server/api';

const getDataikuProjectsList = (live) => {
  return hostServer.get(`/dataiku/projects?live=${live}`, {
    data: {},
  });
};

const getDnaProjectList = () => {
  return server.get(`/dataiku?limit=0&offset=0&sortBy=&sortOrder=&projectName=`, {
    data: {},
  });
};

const deleteDnaProjectList = (id) => {
  return server.delete(`/dataiku/${id}`);
};

const deleteDnaProjectByProjectByName = (projectKey, cloudProfile) => {
  return server.delete(`/dataiku/${cloudProfile}/${projectKey}`);
};

const getDnaDataikuProjectByName = (projectKey, cloudProfile) => {
  return server.get(`/dataiku/${cloudProfile}/${projectKey}`);
};

const createNewDataikuProjects = (data) => {
  return server.post('/dataiku', data);
}

const updateDataikuProjects = (data, id) => {
  return server.put(`/dataiku/${id}`, data);
}

const validateUserPrivilage = (userID) => {
  return server.get(`/userprivilege/validate?userId=${userID}`);
}

const getSubDivisions = (divisionId) => {
  return hostServer.get('/subdivisions/' + divisionId);
}

const getLovData = () => {
  return Promise.all([
    storageServer.get(`/classifications`),
    hostServer.get('/divisions'),
    reportsServer.get('/departments'),
  ]);
}

export const dataikuApi = {
  getDataikuProjectsList,
  getDnaProjectList,
  createNewDataikuProjects,
  validateUserPrivilage,
  deleteDnaProjectList,
  deleteDnaProjectByProjectByName,
  updateDataikuProjects,
  getSubDivisions,
  getLovData,
  getDnaDataikuProjectByName
};
