import React, { useEffect } from 'react';

import { Provider } from 'react-redux';
import store, { history } from './store';

import Routes from './routes/ChronosRoutes';

const App = ({ user, ...rest }) => {
  useEffect(() => {
    user?.roles?.length > 0 && rest.history?.location?.pathname === '/chronos' && history.replace('/');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return (
    <Provider store={store}>
      <Routes user={user} />
    </Provider>
  );
};

export default App;
