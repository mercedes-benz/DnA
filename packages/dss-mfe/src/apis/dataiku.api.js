import { hostServer } from '../server/api';

const getDataikuProjectsList = (live) => {
  return hostServer.get(`/dataiku/projects?live=${live}`, {
    data: {},
  });
};

const getDnaProjectList = () => {
  return hostServer.get(`/dataiku?limit=0&offset=0`, {
    data: {},
  });
};

const deleteDnaProjectList = (id) => {
  return hostServer.delete(`/dataiku/${id}`);
};

const createNewDataikuProjects = (data) => {
  return hostServer.post('/dataiku', data);
}

const updateDataikuProjects = (data, id) => {
  return hostServer.put(`/dataiku/${id}`, data);
}

const validateUserPrivilage = (userID) => {
  return hostServer.get(`/userprivilege/validate?userId=${userID}`);
}

export const dataikuApi = {
  getDataikuProjectsList,
  getDnaProjectList,
  createNewDataikuProjects,
  validateUserPrivilage,
  deleteDnaProjectList,
  updateDataikuProjects
};
