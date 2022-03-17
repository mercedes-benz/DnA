import { applyMiddleware, compose, createStore } from 'redux';
import ReduxThunk from 'redux-thunk';
import { history, INITIAL_STATE, rootReducer } from './storeRoot';
import { routerMiddleware } from 'connected-react-router';

const enhancers = [];
const middleware = [routerMiddleware(history), ReduxThunk];

if (process.env.NODE_ENV === 'development') {
  const devToolsExtension = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__;
  if (typeof devToolsExtension === 'function') {
    enhancers.push(devToolsExtension());
  }
}

const composedEnhancers = compose(applyMiddleware(...middleware), ...enhancers);

const configureStore = () => {
  const store = createStore(rootReducer, INITIAL_STATE, composedEnhancers);
  return { store };
};

export const { store, persistor } = configureStore();
