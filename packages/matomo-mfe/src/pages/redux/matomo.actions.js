// import { bucketsApi } from '../../apis/buckets.api';
import { matomoApi } from '../../apis/matamo.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { history } from '../../store/storeRoot';

const getMatomoList = () => {
  return async (dispatch, getStore) => {
    const {
      matomo: { pagination },
    } = getStore();
    dispatch({
      type: 'MATOMO_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    matomoApi
      .getMatomoProjectsList()
      .then((res) => {
        const totalNumberOfPages = Math.ceil(res?.data?.records?.length / pagination.maxItemsPerPage);
        const modifiedData = res?.data ? res?.data?.records.slice(0, pagination.maxItemsPerPage) : [];
        dispatch({
          type: 'SET_PAGINATION',
          payload: {
            totalNumberOfPages,
            bucketListResponse: res?.data?.records,
            currentPageNumber: 1,
          },
        });
        dispatch({
          type: 'MATOMO_DATA',
          payload: modifiedData,
        });
        dispatch({
          type: 'MATOMO_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
      })
      .catch((e) => {
        dispatch({
          type: 'MATOMO_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Fetching list of matomo failed!',
          'alert',
        );
      });
  };
};

const createMatomo = (data) => {
  return async (dispatch) => {
    dispatch({
      type: 'MATOMO_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      const res = await matomoApi.createMatomo(data);
      dispatch({
        type: 'MATOMO_LOADING',
        payload: false,
      });
      dispatch({
        type: 'CONNECTION_INFO',
        payload: {
          siteName: res?.data?.siteName,
          modal: true,
          creator: data?.creator,
          // accessInfo: res.data?.bucketAccessinfo,
        },
      });
      ProgressIndicator.hide();
      Notification.show(`Site ${data.siteName} created successfully.`);
      history.replace('/');
    } catch (error) {
      dispatch({
        type: 'MATOMO_ERROR',
        payload: error.response.data.errors?.length
          ? error.response.data.errors[0].message
          : 'Error while creating a site',
      });
      dispatch({
        type: 'MATOMO_LOADING',
        payload: false,
      });
      Notification.show(
        error.response.data.errors?.length ? error.response.data.errors[0].message : 'Error while creating a site',
        'alert',
      );
      ProgressIndicator.hide();
    }
  };
};

// const updateBucket = (data) => {
//   return async (dispatch) => {
//     dispatch({
//       type: 'BUCKET_LOADING',
//       payload: true,
//     });
//     ProgressIndicator.show();
//     try {
//       await bucketsApi.updateBucket(data);
//       dispatch({
//         type: 'BUCKET_LOADING',
//         payload: false,
//       });
//       ProgressIndicator.hide();
//       Notification.show(`Bucket ${data.bucketName} updated successfully.`);
//       history.push('/');
//     } catch (error) {
//       dispatch({
//         type: 'BUCKET_ERROR',
//         payload: error.response.data.errors?.length
//           ? error.response.data.errors[0].message
//           : 'Error while updating a bucket.',
//       });
//       dispatch({
//         type: 'BUCKET_LOADING',
//         payload: false,
//       });
//       Notification.show(
//         error.response.data.errors?.length ? error.response.data.errors[0].message : 'Error while updating a bucket.',
//         'alert',
//       );
//       ProgressIndicator.hide();
//     }
//   };
// };

export const matomoActions = {
  getMatomoList,
  createMatomo,
  // updateBucket,
};
