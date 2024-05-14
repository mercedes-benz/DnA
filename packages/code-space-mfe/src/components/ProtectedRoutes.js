import React, { useEffect, useState } from 'react';
import { Route } from 'react-router-dom';
import { hostServer } from '../server/api';
import { LocalWrapper } from './LocalWrapper';
import { SESSION_STORAGE_KEYS } from '../Utility/constants';

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

  return (
    <Route
      exact={rest.exact}
      path={rest.path}
      render={(props) => {
        return rest.user?.roles?.length ? (
          <Component {...props} user={rest.user} hostHistory={rest.hostHistory} />
        ) : (
          <LocalWrapper>
            <Component {...props} user={rest.user} />
          </LocalWrapper>
        );
      }}
    />
  );
};
