import { server } from '../server/api';

const getAllDataProducts = () => {
  return server.get('/dataproducts?limit=0&offset=0', {
    data: {},
  });
};

const getDataProductById = (id) => {
  return server.get(`/dataproducts/${id}`, {
    data: {},
  });
};

const getDepartments = () => {
  return server.get('/departments', {
    data: {},
  });
};

const createDataProduct = (data) => {
  return server.post('/dataproducts/provider', {
    data,
  });
};

const updateProvider = (data) => {
  return server.put('/dataproducts/provider', {
    data,
  });
};

const updateConsumer = (data) => {
  return server.put('/dataproducts/consume', {
    data,
  });
};

const deleteDataProduct = (id) => {
  return server.delete(`/dataproducts/${id}`, {
    data: {},
  });
};

const getDataComplianceList = (offset, limit, sortBy, sortOrder) => {
  return server.get(`datacompliance?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
};

export const dataProductsApi = {
  getAllDataProducts,
  createDataProduct,
  getDepartments,
  updateProvider,
  updateConsumer,
  deleteDataProduct,
  getDataProductById,
  getDataComplianceList,
};
