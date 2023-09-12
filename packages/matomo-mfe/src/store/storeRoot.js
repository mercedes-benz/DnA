import { createHashHistory } from 'history';
import { combineReducers } from 'redux';
import { connectRouter } from 'connected-react-router';
import { matomoInitialState, matomoReducer } from '../pages/redux/matomo.reducers';
import appReducers, { appInitialState } from '../appRedux/app.reducers';
// import { fileExplorerInitialState, fileExplorerReducer } from '../components/Explorer/redux/fileExplorer.reducers';
// import { connectionInfoInitialState, connectionReducer } from '../components/ConnectionInfo/redux/connection.reducers';

export const history = createHashHistory({
  basename: '/matomo',
});

export const INITIAL_STATE = Object.assign(
  {},
  {
    app: appInitialState,
    matomo: matomoInitialState,
    // fileExplorer: fileExplorerInitialState,
    // connectionInfo: connectionInfoInitialState,
  },
);

const combinedReducers = combineReducers({
  router: connectRouter(history),
  app: appReducers,
  matomo: matomoReducer,
  // fileExplorer: fileExplorerReducer,
  // connectionInfo: connectionReducer,
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
