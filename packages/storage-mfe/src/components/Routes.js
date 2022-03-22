import React, { lazy, Suspense, useEffect } from 'react';
import { Route, Switch } from 'react-router-dom';
import { ConnectedRouter } from 'connected-react-router';
import { useDispatch } from 'react-redux';

import { getUserInfo } from '../appRedux/app.actions';

// import component from container app
const Progress = lazy(() => import('dna-container/Progress'));
const NotFoundPage = lazy(() => import('dna-container/NotFound'));
const UnAuthorised = lazy(() => import('dna-container/UnAuthorised'));

import AllBuckets from './Bucket/Bucket';
import CreateBucket from './Bucket/CreateBucket';
import FileExplorer from './Explorer/FileExplorer';
import { ProtectedRoute } from './ProtectedRoute';
import { history } from '../store/storeRoot';

const protectedRoutes = [
  {
    component: AllBuckets,
    exact: true,
    path: '/',
  },
  {
    component: CreateBucket,
    exact: true,
    path: '/createBucket',
  },
  {
    component: CreateBucket,
    exact: true,
    path: '/editBucket/:id',
  },
  {
    component: FileExplorer,
    exact: false,
    path: '/explorer/:fileName?',
  },
];

const Routes = ({ user }) => {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getUserInfo(user));
  }, []);

  return (
    <Suspense fallback={user?.roles?.length ? <Progress show={true} /> : <div>Loading...</div>}>
      <ConnectedRouter history={history}>
        {process.env.NODE_ENV === 'development' && !user?.roles?.length ? (
          <Switch>
            {protectedRoutes.map((route, index) => (
              <ProtectedRoute
                key={index}
                path={route.path}
                exact={route.exact}
                component={route.component}
                user={user}
              />
            ))}
            <Route path="/unauthorized" component={UnAuthorised} />
            <Route component={NotFoundPage} />
          </Switch>
        ) : user?.roles?.length ? (
          <Switch>
            {protectedRoutes.map((route, index) => (
              <ProtectedRoute
                key={index}
                path={route.path}
                exact={route.exact}
                component={route.component}
                user={user}
              />
            ))}
            <Route exact path={'/unauthorized'} component={UnAuthorised} />
            <Route component={NotFoundPage} />
          </Switch>
        ) : (
          <Route to="/unauthorized" component={UnAuthorised} />
        )}
      </ConnectedRouter>
    </Suspense>
  );
};

export default Routes;
