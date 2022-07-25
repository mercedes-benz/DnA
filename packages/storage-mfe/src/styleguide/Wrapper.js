import React, { Component } from 'react';
import { Provider } from 'react-redux';
import { applyMiddleware, compose, createStore } from 'redux';
import ReduxThunk from 'redux-thunk';
import { INITIAL_STATE, rootReducer } from '../store/storeRoot';
import { ConnectedRouter, routerMiddleware } from 'connected-react-router';
import { createHashHistory } from 'history';
const history = createHashHistory({
  hashType: 'noslash',
});

const enhancers = [];
const middleware = [routerMiddleware(history), ReduxThunk];

const composedEnhancers = compose(applyMiddleware(...middleware), ...enhancers);

const store = createStore(rootReducer, INITIAL_STATE, composedEnhancers);

export default class Wrapper extends Component {
  render() {
    return (
      <Provider store={store}>
        <ConnectedRouter history={history}>{this.props.children}</ConnectedRouter>
      </Provider>
    );
  }
}
