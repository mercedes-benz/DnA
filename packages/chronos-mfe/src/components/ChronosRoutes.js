import React, { Suspense } from 'react';
import { Route, Router, Switch } from 'react-router-dom';

// import component from container app
const Progress = React.lazy(() => import('dna-container/Progress'));
const NotFoundPage = React.lazy(() => import('dna-container/NotFound'));
const UnAuthorised = React.lazy(() => import('dna-container/UnAuthorised'));

import { ProtectedRoute } from './ProtectedRoutes';
import { history } from '../store';

import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import SessionExpired from './SessionExpired';
import Projects from './Projects';
import ForecastForm from './form';
import ForecastingResults from './ForecastingResults';
import ChronosHelp from './ChronosHelp';

const protectedRoutes = [
  {
    component: Projects,
    exact: true,
    path: '/',
  },
  {
    component: ForecastForm,
    exact: true,
    path: '/project',
  },
  {
    component: ForecastingResults,
    exact: true,
    path: '/results'
  },
  {
    component: ChronosHelp,
    exact: true,
    path: '/help'
  }, 
];

const Routes = ({ user }) => {
  return (
    <Suspense fallback={user?.roles?.length ? <Progress show={true} /> : <>Loading</>}>
      <Router history={history}>
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
      </Router>
    </Suspense>
  );
};

export default Routes;
