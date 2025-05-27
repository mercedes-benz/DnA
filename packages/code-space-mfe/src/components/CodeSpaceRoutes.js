import React, { Suspense } from 'react';
import { Route, Router, Switch } from 'react-router-dom';

import Progress from 'dna-container/Progress';
import NotFoundPage from 'dna-container/NotFound';
import UnAuthorised from 'dna-container/UnAuthorised';

import { ProtectedRoute } from './ProtectedRoutes';
import { history } from '../store';
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import SessionExpired from './SessionExpired';

import AllCodeSpaces from './AllCodeSpaces';
import CodeSpace from './CodeSpace';
import CodeSpaceSecurityConfig from './securityConfig/SecurityConfig';
import ManageRecipes from './manageRecipes/ManageRecipes';
import CodeSpaceRecipe from './codeSpaceRecipe/CodeSpaceRecipe';
import Tutorials from './codeSpaceTutorials/Tutorials';
import CodeSpaceAdministration from './codeSpaceAdministration/CodeSpaceAdministration';

export const protectedRoutes = [
    {
        component: AllCodeSpaces,
        exact: true,
        path: '/',
    },
    {
        component: CodeSpace,
        exact: true,
        path: '/codespace/:id?',
    },
    {
        component: ManageRecipes,
        exact: false,
        path: '/manageRecipes',
    },
    {
        component: CodeSpaceSecurityConfig,
        exact: false,
        path: '/codespace/securityconfig/:id?',
    },
    {
        component: CodeSpaceSecurityConfig,
        exact: false,
        path: '/codespace/publishedSecurityconfig/:id?',
    },
    {
        component: CodeSpaceSecurityConfig,
        exact: false,
        path: '/codespace/adminSecurityconfig/:id?',
    },
    {
        component: CodeSpaceRecipe,
        exact: false,
        path: '/codespaceRecipes/:id?',
    },
    {
        component: Tutorials,
        extact: false,
        path: '/tutorials/:id?'
    },
    {
        component: CodeSpaceAdministration,
        exact: false,
        path: '/administration',
    },
];

export const routes = [...protectedRoutes];

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