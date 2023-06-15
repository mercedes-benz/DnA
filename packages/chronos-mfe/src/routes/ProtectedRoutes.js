import React, { useEffect, useState } from 'react';
import { Route } from 'react-router-dom';

import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import { hostServer } from '../server/api';
import { history } from '../store';

import { LocalWrapper } from './LocalWrapper';
import { SESSION_STORAGE_KEYS } from '../utilities/constants';

export const ProtectedRoute = ({ component: Component, ...rest }) => {
  const [hasJwt, setJwt] = useState(!!sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT));

  useEffect(() => {
    if (process.env.NODE_ENV === 'development') {
      if (!rest.user?.roles?.length) {
        if (!hasJwt) {
          hostServer.get('/login', { data: {} }).then((res) => {
            sessionStorage.setItem(SESSION_STORAGE_KEYS.JWT, res.data.token);
            window.location.reload();
            setJwt(true);
          });
        }
      }
    }
  }, [hasJwt, rest.user?.roles?.length]);

  useEffect(() => {
    if (hasJwt) {
      hostServer
        .post('/verifyLogin', {
          data: {},
        })
        .then((res) => {
          sessionStorage.setItem(SESSION_STORAGE_KEYS.JWT, res.data.token);
        })
        .catch(() => {
          sessionStorage.removeItem(SESSION_STORAGE_KEYS.JWT);
          ProgressIndicator.hide();
          // reset history to base page before accessing container app's public routes;
          history.replace('/');
          window.location.replace('#/SessionExpired');
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
