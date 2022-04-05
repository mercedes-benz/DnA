export const bucketInitialState = {
  isLoading: false,
  bucketList: [],
  submission: {
    bucketId: '',
    modal: false,
  },
  bucket: {
    data: {},
    accessInfo: [],
  },
  pagination: {
    bucketListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem('paginationMaxItemsPerPage'), 10) || 15,
  },
  error: '',
};

export const bucketReducer = (state = bucketInitialState, action) => {
  switch (action.type) {
    case 'BUCKET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'SUBMISSION_MODAL':
      return {
        ...state,
        submission: action.payload,
      };
    case 'BUCKET_DATA':
      return Object.assign({}, state, {
        bucketList: action.payload,
      });
    case 'BUCKET_ERROR':
      return Object.assign({}, state, {
        error: action.payload,
      });
    case 'RESET_BUCKET_LIST':
      return Object.assign({}, state, {
        bucketList: bucketInitialState.bucketList,
      });
    case 'CREATE_BUCKET':
      return {
        ...state,
        bucket: {
          ...state.bucket,
          data: action.payload.data,
          accessInfo: action.payload.accessInfo,
        },
      };
    case 'RESET_BUCKET':
      return {
        ...state,
        bucket: bucketInitialState.bucket,
      };
    case 'UPDATE_BUCKET':
      return Object.assign({}, state, {
        bucketList: state.bucketList.map((item) => {
          if (item.id === action.payload.id) {
            item = action.payload;
          }
          return item;
        }),
      });
    case 'DELETE_BUCKET':
      return Object.assign({}, state, {
        bucketList: state.bucketList,
      });
    case 'SET_PAGINATION':
      return {
        ...state,
        pagination: {
          ...state.pagination,
          ...action.payload,
        },
      };

    default:
      return state;
  }
};
