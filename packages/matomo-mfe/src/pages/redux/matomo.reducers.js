import { SESSION_STORAGE_KEYS } from '../Utility/constants';

export const matomoInitialState = {
  isLoading: false,
  matomoList: [],
  pagination: {
    matomoListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  },
  error: '',
};

export const matomoReducer = (state = matomoInitialState, action) => {
  switch (action.type) {
    case 'MATOMO_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'MATOMO_DATA':
      return Object.assign({}, state, {
        matomoList: action.payload,
      });
    case 'MATOMO_ERROR':
      return Object.assign({}, state, {
        error: action.payload,
      });
    case 'RESET_MATOMO_LIST':
      return Object.assign({}, state, {
        matomoList: matomoInitialState.matomoList,
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
