import server from '../server/api';

const getAllBuckets = () => {
  return server.get('/buckets', {
    data: {},
  });
};

const createBucket = (data) => {
  return server.post('/buckets', {
    data,
  });
};

export const bucketsApi = {
  getAllBuckets,
  createBucket,
};
