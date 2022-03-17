import React, { useEffect } from 'react';
import { Route, Switch } from 'react-router-dom';
import { useDispatch } from 'react-redux';

import { history } from '../store/storeRoot';
import { ConnectedRouter } from 'connected-react-router';

import AllBuckets from './Bucket/Bucket';
import CreateBucket from './Bucket/CreateBucket';

const Progress = React.lazy(() => import('dna-container/Progress'));
const Header = React.lazy(() => import('dna-container/Header'));
const MainNavigation = React.lazy(() => import('dna-container/MainNavigation'));
const Footer = React.lazy(() => import('dna-container/Footer'));
const NotFoundPage = React.lazy(() => import('dna-container/NotFound'));
const UnAuthorised = React.lazy(() => import('dna-container/UnAuthorised'));

import { getUserInfo } from '../appRedux/app.actions';
import FileExplorer from './Explorer/FileExplorer';

const Wrapper = ({ children }) => {
  return (
    <div className="container">
      <Header
        user={{
          department: 'TE/ST',
          eMail: 'demouser@web.de',
          firstName: 'Demo',
          lastName: 'User',
          id: 'DEMOUSER',
          mobileNumber: '',
          roles: [
            {
              id: '3',
              name: 'Admin',
            },
          ],
        }}
      />
      <main id="mainContainer" className="mainContainer">
        <aside>
          <MainNavigation showExpandEffect={false} isMaximized={false} onNavOpen={() => {}} onNavClose={() => {}} />
        </aside>
        <section>{children}</section>
        <Footer />
      </main>
    </div>
  );
};

const ProtectedRoutes = ({ component: Component, ...rest }) => {
  return (
    <Route
      exact={rest.exact}
      path={rest.path}
      render={(props) => {
        return rest.user?.roles?.length ? (
          <Component {...props} user={rest.user} />
        ) : (
          <Wrapper user={rest.user}>
            <Component {...props} user={rest.user} />
          </Wrapper>
        );
      }}
    />
  );
};

const protectedRoutes = [
  {
    // allowedRoles: UserAndAdminRole,
    component: AllBuckets,
    exact: true,
    path: '/',
  },
  {
    // allowedRoles: UserAndAdminRole,
    component: CreateBucket,
    exact: true,
    path: '/createBucket',
  },
  {
    // allowedRoles: UserAndAdminRole,
    component: FileExplorer,
    exact: false,
    path: '/explorer/:fileName?',
  },
];

const ContainerComponents = ({ user }) => {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getUserInfo(user));
  }, []);

  return (
    <React.Suspense fallback={user?.roles?.length ? <Progress show={true} /> : <div>Loading...</div>}>
      <ConnectedRouter history={history}>
        {process.env.NODE_ENV === 'development' && !user?.roles?.length ? (
          <Switch>
            {protectedRoutes.map((route, index) => (
              <ProtectedRoutes
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
              <ProtectedRoutes
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
    </React.Suspense>
  );
};

export default ContainerComponents;
