import { bucketsApi } from '../../apis/buckets.api';

const setBucketList = (data) => {
  return async (dispatch) => {
    dispatch({
      type: 'BUCKET_LOADING',
      payload: true,
    });
    dispatch({
      type: 'RESET_BUCKET',
    });
    try {
      const res = await bucketsApi.createBucket(data);
      dispatch({
        type: 'CREATE_BUCKET',
        payload: {
          data: res.data.data,
          accessInfo: res.data.bucketAccessinfo,
        },
      });
      dispatch({
        type: 'BUCKET_LOADING',
        payload: false,
      });
      dispatch({
        type: 'CONNECTION_INFO',
        payload: {
          bucketName: data.bucketName,
          modal: true,
          accessInfo: res.data.bucketAccessinfo,
        },
      });
    } catch (error) {
      dispatch({
        type: 'BUCKET_ERROR',
        payload: error?.response?.data?.message,
      });
      dispatch({
        type: 'BUCKET_LOADING',
        payload: false,
      });
    }
  };
};

export const bucketActions = {
  setBucketList,
};
