import server from '../server/api';

const getAllBucketObjects = (bucketName) => {
  return server.get(`/buckets/${bucketName}/objects`, { data: {} });
};

const getObjects = (bucketName, prefix) => {
  return server.get(`/buckets/${bucketName}/objects`, {
    data: {},
    params: {
      prefix,
    },
  });
};

const downloadObjects = (bucketName, prefix) => {
  return server.get(`/buckets/${bucketName}/objects/metadata`, {
    data: {},
    params: {
      prefix,
    },
    responseType: 'blob',
  });
};

const deleteObjects = (bucketName, filesPath) => {
  return server.delete(`/buckets/${bucketName}/objects`, {
    params: {
      prefix: `${filesPath}`,
    },
    data: {},
  });
};

const previewFiles = (bucketName, prefix, isImage) => {
  return server.get(`/buckets/${bucketName}/objects/metadata`, {
    data: {},
    params: {
      prefix,
    },
    ...(isImage && { responseType: 'blob' }),
  });
};

export const bucketsObjectApi = {
  getAllBucketObjects,
  getObjects,
  downloadObjects,
  deleteObjects,
  previewFiles,
};
