import { server, hostServer, reportsServer, storageServer ,dataProductServer} from '../server/api';

const getDatalakeProjectsList = (offset, limit) => {
  return server.get(`/datalakes?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const createDatalakeProject = (data) => {
  return server.post(`/datalakes`, {
    data,
  });
};

const getDatalakeProject = (id) => {
  return server.get(`/datalakes/${id}`, {
    data: {},
  });
};

const updateDatalakeProject = (id, data) => {
  return server.put(`/datalakes/${id}`, data);
};

const updateTechnicalUser = (id, data) => {
  return server.patch(`/datalakes/${id}/techuser`, {
    data,
  });
};

const getLovData = () => {
  return Promise.all([
    storageServer.get(`/classifications`, {
      data: {},
    }),
    hostServer.get('/divisions'),
    reportsServer.get('/departments', {
      data: {},
    }),
  ]);
}

const getConnectors = () => {
  return server.get(`/connectors`, {
    data: {},
  });
};

const getConnectionInfo = (id) => {
  return server.get(`/datalakes/${id}/connect`, { data: {} });
};

const createDataProduct = (data) => {
  return dataProductServer.post('/dataproducts', {
    data,
  });
};

const getExistingDataProduts = () => {
  return dataProductServer.get(`/dataproducts/bookmarked`);
}

const getLatestDetails = (id,data) => {
  return server.get(`/datalakes/${id}`,{
    data,
  });
}

const linkDataProduct = (id, data) => {
  return server.patch(`datalakes/${id}/dataproduct`,
    data,
  )
}

export const datalakeApi = {
  getDatalakeProjectsList,
  createDatalakeProject,
  getDatalakeProject,
  updateDatalakeProject,
  getLatestDetails,
  updateTechnicalUser,
  getLovData,
  getConnectors,
  getConnectionInfo,
  createDataProduct,
  linkDataProduct,
  getExistingDataProduts
};
