export const appInitialState = {
  userInfo: {},
};

const appReducers = (state = appInitialState, actions) => {
  switch (actions.type) {
    case 'USER_INFO':
      return {
        ...state,
        userInfo: actions.payload,
      };
    default:
      return state;
  }
};

export default appReducers;
