import { hostServer, server } from '../server/api';

const getAllDataProductList = (sortBy, sortOrder) => {
  return server.get(`/dataproducts?limit=0&offset=0&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
};

const getDataProductById = (id) => {
  return server.get(`/dataproducts/${id}`, {
    data: {},
  });
};

const createDataProduct = (data) => {
  return server.post('/dataproducts', {
    data,
  });
};

const updateDataProduct = (data) => {
  return server.put('/dataproducts', {
    data,
  });
};

const deleteDataProduct = (id) => {
  return server.delete(`/dataproducts/${id}`, {
    data: {},
  });
};

const createDataTransfer = (id, data) => {
  return server.post(`/dataproducts/${id}/datatransfer`, {
    data,
  });
};

const getAllDataTransfers = (dataTransferIds, sortBy, sortOrder) => {
  return server.get(
    `/datatransfers?limit=0&offset=0&datatransferIds=${dataTransferIds}&sortBy=${sortBy}&sortOrder=${sortOrder}`,
    {
      data: {},
    },
  );
};

const getMyDataTransfers = (dataTransferIds, sortBy, sortOrder) => {
  return server.get(
    `/datatransfers?limit=0&offset=0&isCreator=true&datatransferIds=${dataTransferIds}&sortBy=${sortBy}&sortOrder=${sortOrder}`,
    {
      data: {},
    },
  );
};

const getAllAgileReleaseTrains = () => {
  return server.get('/agileReleaseTrains', { data: {} });
};

const getAllCarlaFunctions = () => {
  return server.get('/carlafunctions', { data: {} });
};

const getAllCorporateDataCatalogs = () => {
  return hostServer.get('/datasources?source=CDC', { data: {} });
};

export const dataProductApi = {
  getAllDataProductList,
  getDataProductById,
  createDataProduct,
  updateDataProduct,
  deleteDataProduct,
  createDataTransfer,
  getAllDataTransfers,
  getMyDataTransfers,
  getAllAgileReleaseTrains,
  getAllCarlaFunctions,
  getAllCorporateDataCatalogs,
};
