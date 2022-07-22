import React, { useEffect } from 'react';
import Routes from './components/Routes';
import { createHashHistory } from 'history';

import { Provider } from 'react-redux';
import store from './store';

export const history = createHashHistory({
  basename: '/dataproduct',
});

const App = ({ user, ...rest }) => {
  useEffect(() => {
    user?.roles?.length > 0 && rest.history?.location?.pathname === '/dataproduct' && history.replace('/');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <Provider store={store}>
      <Routes user={user} />
    </Provider>
  );
};

export default App;
