import { hostServer, server, dataikuServer } from '../server/api';

const getAllBuckets = (offset, limit) => {
  return server.get(`/buckets?offset=${offset}&limit=${limit}`, {
    data: {},
  });
};

const getBucketByName = (bucketName) => {
  return server.get(`/buckets/${bucketName}`, {
    data: {},
  });
};

const createBucket = (data) => {
  return server.post('/buckets', {
    data,
  });
};

const updateBucket = (data) => {
  return server.put('/buckets', {
    data,
  });
};

const deleteBucket = (bucketName) => {
  return server.delete(`/buckets/${bucketName}`, {
    data: {},
  });
};

const getConnectionInfo = (bucketName) => {
  return server.get(`/buckets/${bucketName}/connect`, { data: {} });
};

const getDataConnectionTypes = () => {
  return server.get('/classifications', { data: {} });
};

const getDataikuProjects = (live) => {
  return hostServer.get(`/dataiku/projects?live=${live}`, { data: {} });
};

const validateUserPrivilage = (userID) => {
  return dataikuServer.get(`/userprivilege/validate?userId=${userID}`);
}

const getDnaProjectList = () => {
  return dataikuServer.get(`/dataiku?limit=0&offset=0&sortBy=&sortOrder=&projectName=`, {
    data: {},
  });
};

const connectToDataikuProjects = (data) => {
  return server.post('/buckets/dataiku/connect', data);
};

const connectToJupyterNotebook = (data) => {
  return server.post('/buckets/dataiku/connect', data);
};

const transferOwnership = (bucketName, userId) => {
  return server.patch(`/buckets/${bucketName}/reAssignOwner/${userId}`, {});
};

export const bucketsApi = {
  getAllBuckets,
  getBucketByName,
  createBucket,
  updateBucket,
  deleteBucket,
  getConnectionInfo,
  getDataConnectionTypes,
  getDataikuProjects,
  getDnaProjectList,
  connectToDataikuProjects,
  connectToJupyterNotebook,
  transferOwnership,
  validateUserPrivilage
};
