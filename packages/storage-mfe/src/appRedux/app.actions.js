export const getUserInfo = (data) => {
  return (dispatch) => {
    dispatch({
      type: 'USER_INFO',
      payload: data,
    });
  };
};
