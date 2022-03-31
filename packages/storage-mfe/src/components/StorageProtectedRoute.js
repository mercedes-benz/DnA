import React, { useEffect, useState } from 'react';
import { Route } from 'react-router-dom';

import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import server from '../server/api';
import { history } from '../store/storeRoot';

import { LocalWrapper } from './LocalWrapper';

export const ProtectedRoute = ({ component: Component, ...rest }) => {
  const [hasJwt, setJwt] = useState(!!sessionStorage.getItem('jwt'));

  useEffect(() => {
    if (process.env.NODE_ENV === 'development') {
      if (!rest.user?.roles?.length) {
        if (!hasJwt) {
          server.get('/login').then((res) => {
            sessionStorage.setItem('jwt', res.data.token);
            window.location.reload();
            setJwt(true);
          });
        }
      }
    }
  }, [hasJwt, rest.user?.roles?.length]);

  useEffect(() => {
    if (hasJwt) {
      server
        .post('/verifyLogin')
        .then((res) => {
          sessionStorage.setItem('jwt', res.data.token);
        })
        .catch(() => {
          sessionStorage.removeItem('jwt');
          window.location.replace('#/SessionExpired');
          ProgressIndicator.hide();
        });
    }

    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hasJwt, history.location]);

  return (
    <Route
      exact={rest.exact}
      path={rest.path}
      render={(props) => {
        return rest.user?.roles?.length ? (
          <Component {...props} user={rest.user} />
        ) : (
          <LocalWrapper>
            <Component {...props} user={rest.user} />
          </LocalWrapper>
        );
      }}
    />
  );
};
