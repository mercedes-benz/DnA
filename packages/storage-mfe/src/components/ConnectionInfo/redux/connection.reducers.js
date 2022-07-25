export const connectionInfoInitialState = {
  isLoading: false,
  connect: {
    modal: false,
    accessInfo: {},
    dataikuProjects: [],
  },
};

export const connectionReducer = (state = connectionInfoInitialState, action) => {
  switch (action.type) {
    case 'CONNECTION_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'CONNECTION_INFO':
      return {
        ...state,
        connect: {
          ...state.connect,
          ...action.payload,
        },
      };
    case 'SELECTED_DATAIKU_PROJECTS':
      return {
        ...state,
        connect: {
          ...state.connect,
          dataikuProjects: action.payload,
        },
      };
    case 'RESET_CONNECTION_INFO':
      return {
        ...state,
        connect: connectionInfoInitialState.connect,
      };
    default:
      return state;
  }
};
