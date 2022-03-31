import Notification from '../../../../common/modules/uilab/js/src/notification';
import server from '../../../../server/api';

export const getConnectionInfo = (bucketName) => {
  return async (dispatch) => {
    dispatch({
      type: 'CONNECTION_LOADING',
      payload: true,
    });
    try {
      const response = await server.get(`/buckets/${bucketName}/connect`, { data: {} });
      if (response?.data?.data) {
        dispatch({
          type: 'CONNECTION_INFO',
          payload: {
            bucketName,
            modal: true,
            accessInfo: response.data.data,
          },
        });
        dispatch({
          type: 'CONNECTION_LOADING',
          payload: false,
        });
      }
    } catch (error) {
      dispatch({
        type: 'CONNECTION_LOADING',
        payload: false,
      });
      Notification.show(error.response.data.message ? error.response.data.message : 'Something went wrong.', 'alert');
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
