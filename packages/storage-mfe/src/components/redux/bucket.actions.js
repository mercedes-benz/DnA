import { bucketsApi } from '../../apis/buckets.api';

const getBucketList = () => {
  return async (dispatch, getStore) => {
    const {
      bucket: { pagination },
    } = getStore();
    dispatch({
      type: 'BUCKET_LOADING',
      payload: true,
    });
    bucketsApi
      .getAllBuckets()
      .then((res) => {
        const totalNumberOfPages = Math.ceil(res?.data?.data.length / pagination.maxItemsPerPage);
        const modifiedData = res?.data?.data.slice(0, pagination.maxItemsPerPage);
        dispatch({
          type: 'SET_PAGINATION',
          payload: {
            totalNumberOfPages,
            bucketListResponse: res.data.data,
            currentPageNumber: 1,
          },
        });
        dispatch({
          type: 'BUCKET_DATA',
          payload: modifiedData,
        });
        dispatch({
          type: 'BUCKET_LOADING',
          payload: false,
        });
      })
      .catch((e) => {
        dispatch({
          type: 'BUCKET_LOADING',
          payload: false,
        });
        Notification.show(
          e.response.data.message ? e.reponse.data.message : 'Fetching list of storage buckets failed!',
          'alert',
        );
      });
  };
};

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
  getBucketList,
};
