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
      setTimeout(() => {
        dispatch({
          type: 'BUCKET_LOADING',
          payload: false,
        });
        dispatch({
          type: 'SUBMISSION_MODAL',
          payload: {
            bucketId: data.id,
            modal: true,
          },
        });
      }, 1000);
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
