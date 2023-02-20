import { hostServer } from '../server/api';

const getDataikuProjectsList = (live) => {
  return hostServer.get(`/dataiku/projects?live=${live}`, {
    data: {},
  });
};

export const dataikuApi = {
  getDataikuProjectsList,
};
