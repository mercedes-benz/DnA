import React, { Suspense, useEffect } from 'react';
import { Route, Switch } from 'react-router-dom';
import { ConnectedRouter } from 'connected-react-router';
import { useDispatch } from 'react-redux';

import { getUserInfo } from '../appRedux/app.actions';

// import component from container app
import Progress from 'dna-container/Progress';
import NotFoundPage from 'dna-container/NotFound';
import UnAuthorised from 'dna-container/UnAuthorised';

import AllBuckets from './Bucket/Bucket';
import CreateBucket from './Bucket/CreateBucket';
import FileExplorer from './Explorer/FileExplorer';
import { ProtectedRoute } from './StorageProtectedRoute';
import { history } from '../store/storeRoot';
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import SessionExpired from './SessionExpired';

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
    path: '/explorer/:bucketName/:resultFolder?',
  },
];

const Routes = ({ user }) => {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getUserInfo(user));
  }, [dispatch, user]);

  return (
    <Suspense fallback={user?.roles?.length ? <Progress show={true} /> : <>Loading</>}>
      <ConnectedRouter history={history}>
        {process.env.NODE_ENV === 'development' && !user?.roles?.length ? (
          <Switch>
            {protectedRoutes?.map((route, index) => (
              <ProtectedRoute
                key={index}
                path={route.path}
                exact={route.exact}
                component={route.component}
                user={user}
              />
            ))}
            <Route path="/unauthorized" component={UnAuthorised} />
            <Route
              path="/SessionExpired"
              render={(props) => {
                ProgressIndicator.hide();
                return <SessionExpired {...props} />;
              }}
            />
            <Route component={NotFoundPage} />
          </Switch>
        ) : user?.roles?.length ? (
          <Switch>
            {protectedRoutes?.map((route, index) => (
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
          <Route
            to="/unauthorized"
            render={(props) => {
              ProgressIndicator.hide();
              return <UnAuthorised {...props} />;
            }}
          />
        )}
      </ConnectedRouter>
    </Suspense>
  );
};

export default Routes;
