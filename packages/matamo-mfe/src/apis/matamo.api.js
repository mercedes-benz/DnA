import { server } from '../server/api';

const getMatamoProjectsList = () => {
  return server.get(`/matamo/projects`, {
    data: {},
  });
};


export const matamoApi = {
  getMatamoProjectsList,
};
