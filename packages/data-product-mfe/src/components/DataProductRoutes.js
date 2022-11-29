import React, { Suspense } from 'react';
import { Route, Router, Switch } from 'react-router-dom';

// import component from container app
import Progress from 'dna-container/Progress';
import NotFoundPage from 'dna-container/NotFound';
import UnAuthorised from 'dna-container/UnAuthorised';
import DataWorkspaces from 'dna-container/DataWorkspace';
import DataLayer from 'dna-container/DataLayer';
import DataGovernance from 'dna-container/DataGovernance';

import ProviderForm from './dataTransfer/ProviderForm';
import ConsumerForm from './dataTransfer/ConsumerForm';

import { ProtectedRoute } from './ProtectedRoutes';
import { history } from '../store';

import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import SessionExpired from './SessionExpired';
import DataProducts from './dataTransfer/DataProducts';
import Summary from './dataTransfer/Summary';
import DataComplianceNetworkList from './DataComplianceNetworkList';

import DataList from './data/dataList';
import CreateData from './data/createData';
import DataSummary from './data/summary';

export const protectedRoutes = [
  {
    component: DataWorkspaces,
    exact: true,
    path: '/',
  },
  {
    component: DataLayer,
    exact: true,
    path: '/datalayer',
    title: 'Data Layer',
  },
  {
    component: DataGovernance,
    exact: true,
    path: '/datagovernance',
    title: 'Data Governance',
  },
  {
    component: DataProducts,
    exact: true,
    path: '/datasharing',
  },
  {
    component: ProviderForm,
    exact: true,
    path: '/create',
  },
  {
    component: ProviderForm,
    exact: true,
    path: '/edit/:id',
  },
  {
    component: ConsumerForm,
    exact: true,
    path: '/consume/:id',
  },
  {
    component: Summary,
    exact: true,
    path: '/summary/:id',
  },
  {
    component: DataComplianceNetworkList,
    exact: true,
    path: '/datacompliancenetworklist',
  },
  {
    component: DataList,
    exact: true,
    path: '/dataproductlist',
  },
  {
    component: CreateData,
    exact: true,
    path: '/createData',
  },
  {
    component: CreateData,
    exact: true,
    path: '/editData/:id',
  },
  {
    component: DataSummary,
    exact: true,
    path: '/dataSummary/:id',
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
