import { server } from '../server/api';

const getMatomoProjectsList = () => {
  return server.get(`/matomo/projects`, {
    data: {},
  });
};


export const matomoApi = {
  getMatomoProjectsList,
};
