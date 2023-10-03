import { hostServer } from '../server/api';

const getDatalakeProjectsList = (live) => {
  return hostServer.get(`/datalake/projects?live=${live}`, {
    data: {},
  });
};

export const datalakeApi = {
  getDatalakeProjectsList,
};
