export const bucketInitialState = {
  isLoading: false,
  bucketList: [],
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
        bucketList: [...state.bucketList, action.payload],
      });
    case 'BUCKET_ERROR':
      return Object.assign({}, state, {
        error: action.payload,
      });
    case 'RESET_BUCKET':
      return Object.assign({}, state, {
        bucketList: bucketInitialState.bucketList,
      });
    default:
      return state;
  }
};
