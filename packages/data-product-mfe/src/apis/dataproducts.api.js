import { server } from '../server/api';

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

const getAllAgileReleaseTrains = () => {
  return server.get('/agileReleaseTrains', { data: {} });
};

const getAllCarlaFunctions = () => {
  return server.get('/carlafunctions', { data: {} });
};

const getAllCorporateDataCatalogs = () => {
  return server.get('/corporateDataCatalogs', { data: {} });
};

export const dataProductApi = {
  getAllDataProductList,
  getDataProductById,
  createDataProduct,
  updateDataProduct,
  deleteDataProduct,
  getAllAgileReleaseTrains,
  getAllCarlaFunctions,
  getAllCorporateDataCatalogs,
};
