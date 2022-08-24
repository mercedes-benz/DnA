import { SESSION_STORAGE_KEYS } from '../../Utility/constants';

export const bucketInitialState = {
  isLoading: false,
  bucketList: [],
  pagination: {
    bucketListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
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
