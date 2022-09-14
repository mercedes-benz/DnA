import { hostServer, server } from '../server/api';

const getAllBuckets = () => {
  return server.get('/buckets', {
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

const connectToDataikuProjects = (data) => {
  return server.post('/buckets/dataiku/connect', data);
};

const connectToJupyterNotebook = (data) => {
  return server.post('/buckets/dataiku/connect', data);
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
  connectToDataikuProjects,
  connectToJupyterNotebook
};
