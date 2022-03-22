import React from 'react';
import { Route } from 'react-router-dom';

import { LocalWrapper } from './LocalWrapper';

export const ProtectedRoute = ({ component: Component, ...rest }) => {
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
