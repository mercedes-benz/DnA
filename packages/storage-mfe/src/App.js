import React, { useEffect } from 'react';
import { Provider } from 'react-redux';
import { store } from './store/configureStore';
import { history } from './store/storeRoot';
import server from './server/api';

import Routes from './components/Routes';

function App({ user, ...rest }) {
  useEffect(() => {
    user?.roles?.length > 0 && rest.history.location.pathname === '/storage' && history.replace('/');
  }, []);

  useEffect(() => {
    if (process.env.NODE_ENV === 'development') {
      if (!user?.roles?.length) {
        server
          .get('login', {
            headers: {
              Authorization: 'blablabla',
            },
            data: {},
          })
          .then((res) => sessionStorage.setItem('jwt', res.data.token));
      }
    }
  }, []);

  return (
    <Provider store={store}>
      <Routes user={user} />
    </Provider>
  );
}

export default App;
