import { hostServer, server } from '../server/api';

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

const createNewDataikuProjects = (data) => {
  return server.post('/dataiku', data);
}

const updateDataikuProjects = (data, id) => {
  return server.put(`/dataiku/${id}`, data);
}

const validateUserPrivilage = (userID) => {
  return server.get(`/userprivilege/validate?userId=${userID}`);
}

export const dataikuApi = {
  getDataikuProjectsList,
  getDnaProjectList,
  createNewDataikuProjects,
  validateUserPrivilage,
  deleteDnaProjectList,
  updateDataikuProjects
};
