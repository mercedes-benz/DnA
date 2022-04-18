import { IMAGE_EXTNS } from '../components/Utility/constants';
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

const previewFiles = (bucketName, prefix, fileExtension) => {
  const isImage = IMAGE_EXTNS.includes(fileExtension);
  const isPDF = fileExtension === 'pdf';
  const responseType = 'blob';
  const setResponseType = isImage || isPDF;

  return server.get(`/buckets/${bucketName}/objects/metadata`, {
    data: {},
    params: {
      prefix,
    },
    ...(setResponseType && { responseType }),
  });
};

export const bucketsObjectApi = {
  getAllBucketObjects,
  getObjects,
  downloadObjects,
  deleteObjects,
  previewFiles,
};
