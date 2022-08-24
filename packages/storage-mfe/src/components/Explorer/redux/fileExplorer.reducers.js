export const fileExplorerInitialState = {
  isLoading: false,
  files: {
    rootFolderId: '',
    fileMap: {},
  },
  bucketPermission: {
    write: true,
    read: true,
  },
  fileActions: [],
  bucketObjects: [],
  error: '',
};

export const fileExplorerReducer = (state = fileExplorerInitialState, action) => {
  switch (action.type) {
    case 'SET_FILES':
      return {
        ...state,
        files: {
          ...state.files,
          fileMap: action.payload,
        },
      };
    case 'CREATE_FOLDER':
      return Object.assign({}, state, {
        files: {
          ...state.files,
          fileMap: action.payload,
        },
      });
    case 'DELETE_FILES':
      return Object.assign({}, state, {
        files: {
          ...state.files,
          fileMap: action.payload,
        },
      });

    case 'FILE_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'UPDATE_ROOT_FOLDER':
      return {
        ...state,
        files: {
          ...state.files,
          rootFolderId: action.payload,
        },
      };

    case 'SET_BUCKET_PERMISSION': {
      return {
        ...state,
        bucketPermission: action.payload,
      };
    }
    case 'SET_ACTION_BUTTONS': {
      return {
        ...state,
        fileActions: action.payload,
      };
    }
    case 'SET_BUCKET_OBJECTS': {
      return {
        ...state,
        bucketObjects: action.payload,
      };
    }
    default:
      return state;
  }
};
