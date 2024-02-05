import { bucketsApi } from '../../../apis/buckets.api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { history } from '../../../store/storeRoot';

const getBucketList = (offset, limit) => {
  return async (dispatch, getStore) => {
    const {
      bucket: { pagination },
    } = getStore();
    dispatch({
      type: 'BUCKET_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    bucketsApi
      .getAllBuckets(offset, limit)
      .then((res) => {
        const totalNumberOfPages = Math.ceil(res?.data?.totalCount / pagination.maxItemsPerPage);
        // const modifiedData = res?.data ? res.data.data.slice(0, pagination.maxItemsPerPage) : [];
        dispatch({
          type: 'SET_PAGINATION',
          payload: {
            totalNumberOfPages,
            bucketListResponse: res.data.data,
            currentPageNumber: pagination.currentPageNumber > totalNumberOfPages ? 1 : pagination.currentPageNumber,
          },
        });
        dispatch({
          type: 'BUCKET_DATA',
          payload: res.data.data,
        });
        dispatch({
          type: 'BUCKET_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
      })
      .catch((e) => {
        dispatch({
          type: 'BUCKET_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Fetching list of storage buckets failed!',
          'alert',
        );
      });
  };
};

const createBucket = (data) => {
  return async (dispatch) => {
    dispatch({
      type: 'BUCKET_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      const res = await bucketsApi.createBucket(data);
      dispatch({
        type: 'BUCKET_LOADING',
        payload: false,
      });
      dispatch({
        type: 'CONNECTION_INFO',
        payload: {
          bucketName: data.bucketName,
          modal: true,
          creator: data.creator,
          accessInfo: res.data.bucketAccessinfo,
        },
      });
      ProgressIndicator.hide();
      Notification.show(`Bucket ${data.bucketName} created successfully.`);
    } catch (error) {
      dispatch({
        type: 'BUCKET_ERROR',
        payload: error.response.data.errors?.length
          ? error.response.data.errors[0].message
          : 'Error while creating a bucket',
      });
      dispatch({
        type: 'BUCKET_LOADING',
        payload: false,
      });
      Notification.show(
        error.response.data.errors?.length ? error.response.data.errors[0].message : 'Error while creating a bucket',
        'alert',
      );
      ProgressIndicator.hide();
    }
  };
};

const updateBucket = (data) => {
  return async (dispatch) => {
    dispatch({
      type: 'BUCKET_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      await bucketsApi.updateBucket(data);
      dispatch({
        type: 'BUCKET_LOADING',
        payload: false,
      });
      ProgressIndicator.hide();
      Notification.show(`Bucket ${data.bucketName} updated successfully.`);
      history.push('/');
    } catch (error) {
      dispatch({
        type: 'BUCKET_ERROR',
        payload: error.response.data.errors?.length
          ? error.response.data.errors[0].message
          : 'Error while updating a bucket.',
      });
      dispatch({
        type: 'BUCKET_LOADING',
        payload: false,
      });
      Notification.show(
        error.response.data.errors?.length ? error.response.data.errors[0].message : 'Error while updating a bucket.',
        'alert',
      );
      ProgressIndicator.hide();
    }
  };
};

export const bucketActions = {
  getBucketList,
  createBucket,
  updateBucket,
};
