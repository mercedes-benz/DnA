import React, { Suspense } from 'react';
import { Route, Router, Switch } from 'react-router-dom';

// import component from container app
import Progress from 'dna-container/Progress';
import NotFoundPage from 'dna-container/NotFound';
import UnAuthorised from 'dna-container/UnAuthorised';
// import Dataiku from 'dna-container/Dataiku';

import ListProjects from './ListProjects';
import { ProtectedRoute } from './ProtectedRoutes';
import { history } from '../store';

import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import SessionExpired from './SessionExpired';

export const protectedRoutes = [
  {
    component: ListProjects,
    exact: true,
    path: '/',
  },
];

const Routes = ({ user, hostHistory }) => {
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
                hostHistory={hostHistory}
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
                hostHistory={hostHistory}
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
