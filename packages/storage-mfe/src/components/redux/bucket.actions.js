import { history } from '../../store/storeRoot';

const setBucketList = (data) => {
  return async (dispatch) => {
    dispatch({
      type: 'BUCKET_LOADING',
      payload: true,
    });
    try {
      dispatch({
        type: 'BUCKET_DATA',
        payload: data,
      });
      history.push('/');
    } catch (error) {
      dispatch({
        type: 'BUCKET_ERROR',
        payload: error?.response?.data?.message,
      });
    } finally {
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
