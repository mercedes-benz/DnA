import React, { useEffect } from 'react';
import { Provider } from 'react-redux';
import { store } from './store/configureStore';
import { history } from './store/storeRoot';

import Routes from './components/StorageRoutes';

function App({ user, ...rest }) {
  useEffect(() => {
    user?.roles?.length > 0 && rest.history.location.pathname === '/storage' && history.replace('/');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Provider store={store}>
      <Routes user={user} />
    </Provider>
  );
}

export default App;
