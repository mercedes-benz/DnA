import { server } from '../server/api';

const getDataComplianceNetworkList = (offset, limit, sortBy, sortOrder) => {
  return server.get(`/datacompliance?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
}

const saveDataComplianceNetworkList = (data) => {
  return server.post('/datacompliance', {
    data,
  });
}
const updateDataComplianceNetworkList = (data) => {
  return server.put('/datacompliance', {
    data,
  });
}
const deleteDataComplianceNetworkList = (id) => {
  return server.delete(`/datacompliance/${id}`, {
    data: {},
  });
}

const getEntityList = (offset, limit, sortBy, sortOrder) => {
  return server.get(`/entityids?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
}

export const dataComplianceNetworkListApi = {
  getDataComplianceNetworkList,
  saveDataComplianceNetworkList,
  updateDataComplianceNetworkList,
  deleteDataComplianceNetworkList,
  getEntityList
};