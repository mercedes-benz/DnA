import { bucketsApi } from '../../../apis/buckets.api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';

export const getConnectionInfo = (bucketName) => {
  return async (dispatch) => {
    dispatch({
      type: 'CONNECTION_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      const response = await bucketsApi.getConnectionInfo(bucketName);
      if (response?.data?.data) {
        dispatch({
          type: 'CONNECTION_INFO',
          payload: {
            bucketName,
            modal: true,
            accessInfo: response.data.data.userVO,
            dataikuProjects: response.data.data.dataikuProjects || [],
          },
        });
        dispatch({
          type: 'CONNECTION_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
      }
    } catch (error) {
      dispatch({
        type: 'CONNECTION_LOADING',
        payload: false,
      });
      ProgressIndicator.hide();
      Notification.show(
        error.response.data.errors?.length ? error.response.data.errors[0].message : 'Error fetching connection info.',
        'alert',
      );
    }
  };
};

export const hideConnectionInfo = () => {
  return async (dispatch) => {
    dispatch({
      type: 'RESET_CONNECTION_INFO',
    });
  };
};
