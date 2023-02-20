import { server } from '../server/api';

const getDataikuProjectsList = (live) => {
  return server.get(`/dataiku/projects?live=${live}`, {
    data: {},
  });
};

export const dataikuApi = {
  getDataikuProjectsList,
};
