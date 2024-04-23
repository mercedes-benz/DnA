import { reportsServer, server, hostServer } from '../server/api';

const getAllDataProducts = (sortBy, sortOrder, isProviderCreatorFilter=false) => {
  return server.get(`/datatransfers?limit=0&offset=0&sortBy=${sortBy}&sortOrder=${sortOrder}&isProviderCreator=${isProviderCreatorFilter}`, {
    data: {},
  });
};

const getDataTransferById = (id) => {
  return server.get(`/datatransfers/${id}`, {
    data: {},
  });
};

const getDepartments = () => {
  return reportsServer.get('/departments', {
    data: {},
  });
};

const createDataProduct = (data) => {
  return server.post('/datatransfers/provider', {
    data,
  });
};

const updateProvider = (data) => {
  return server.put('/datatransfers/provider', {
    data,
  });
};

const updateConsumer = (data) => {
  return server.put('/datatransfers/consume', {
    data,
  });
};

const deleteDataTransfer = (id) => {
  return server.delete(`/datatransfers/${id}`, {
    data: {},
  });
};

const getDataComplianceList = (offset, limit, sortBy, sortOrder) => {
  return server.get(`datacompliance?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
};

const getAllClassificationTypes = () => {
  return server.get('/classifications', { data: {} });
};

const getAllLegalBasis = () => {
  return server.get('/legalbasis', { data: {} });
};

const getPlanningIT = (searchTerm = '') => {
  return server.get(`/planningit?searchTerm=${searchTerm}`, { data: {} });
};

const getSubDivisionsData = (divisions) => {
  const divisionIds = divisions.map((division) => division.id);
  return new Promise ( (resolve) => {getAllSubDivisions(divisionIds).then((res) => {
    const tempSubDivision = [];
    res.data.forEach((division) => {
      division.subdivisions.forEach((subdiv) => {
        subdiv.division = division.id;
        subdiv.id = subdiv.id + '@-@' + division.id;
        tempSubDivision.push(subdiv);
      });
    });
    resolve (tempSubDivision);
  })});
}

const getFilterMasterData = () => {
  return Promise.all([
    hostServer.get('/divisions'),
    reportsServer.get('/departments', {
      data: {},
    }),
    server.get('/datatransfers/dataStwerdLov',{
      data: {},
    }),
    server.get('/datatransfers/IOLov',{
      data: {},
    }),
  ]);
};

const getAllSubDivisions = (divisionIds) => {
  return hostServer.get('divisions?ids=' + divisionIds);
}

export const dataTransferApi = {
  getAllDataProducts,
  createDataProduct,
  getDepartments,
  updateProvider,
  updateConsumer,
  deleteDataTransfer,
  getDataTransferById,
  getDataComplianceList,
  getAllClassificationTypes,
  getAllLegalBasis,
  getPlanningIT,
  getFilterMasterData,
  getSubDivisionsData,
};
