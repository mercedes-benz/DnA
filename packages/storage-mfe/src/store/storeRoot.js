import { createHashHistory } from 'history';
import { combineReducers } from 'redux';
import { connectRouter } from 'connected-react-router';

import { bucketInitialState, bucketReducer } from '../components/redux/bucket.reducers';
import appReducers, { appInitialState } from '../appRedux/app.reducers';
import { fileExplorerInitialState, fileExplorerReducer } from '../components/Explorer/redux/fileExplorer.reducers';

export const history = createHashHistory({
  basename: '/storage',
});

export const INITIAL_STATE = Object.assign(
  {},
  {
    app: appInitialState,
    bucket: bucketInitialState,
    fileExplorer: fileExplorerInitialState,
  },
);

const combinedReducers = combineReducers({
  router: connectRouter(history),
  app: appReducers,
  bucket: bucketReducer,
  fileExplorer: fileExplorerReducer,
});

const ACTION_NAME = 'ROOT_ACTIONS__';
export const ROOT_ACTIONS = {
  RESET: `${ACTION_NAME}RESET`,
};

export const rootReducer = (state, action) => {
  if (action.type === ROOT_ACTIONS.RESET) {
    return combinedReducers(undefined, action);
  }
  return combinedReducers(state, action);
};
