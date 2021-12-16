/* tslint:disable */
import * as React from 'react';
import { SESSION_STORAGE_KEYS } from '../globals/constants';
import { Redirect, Route, RouteProps } from 'react-router-dom';
import { LOCAL_STORAGE_KEY, USER_ROLE } from './../globals/constants';
import { IUserInfo } from '../globals/types';
import { ApiClient } from './../services/ApiClient';
import { Layout } from './Layout';
import { Pkce } from '../../src/services/Pkce';
import Progress from '../components/progress/Progress';
import { trackPageView } from '../services/utils';

interface IProtectedRouteProps extends RouteProps {
  component: React.LazyExoticComponent<{ user: IUserInfo } | any>;
  allowedRoles: string[];
  title: string;
}

interface IProtectedRouteState {
  user: IUserInfo;
  loading: boolean;
  redirectPath: string;
}

const initialUserState: IUserInfo = {
  department: '',
  eMail: '',
  firstName: '',
  id: '',
  lastName: '',
  mobileNumber: '',
  roles: [{ id: USER_ROLE.GUEST, name: 'GUEST' }],
};

export class ProtectedRoute extends React.Component<IProtectedRouteProps, IProtectedRouteState> {
  public state = {
    loading: true,
    user: initialUserState,
    redirectPath: '',
  };

  public componentDidMount() {
    if (!sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT)) {
      sessionStorage.setItem(SESSION_STORAGE_KEYS.APPREDIRECT_URL, this.props.location.pathname);
      this.setState({ redirectPath: '/' });
    }
    this.verifyLogin();
  }

  public logout = () => {
    this.setState({ user: initialUserState });
    sessionStorage.removeItem(LOCAL_STORAGE_KEY.TOKEN);
  };

  public async verifyLogin() {
    // try {
    //   const result = await ApiClient.verifyDigiLogin();
    //   this.setState({ user: result.data, loading: false });
    // } catch (e) {
    //   this.setState({ loading: false });
    // }

    ApiClient.verifyDigiLogin()
      .then((result) => {
        this.setState({ redirectPath: '/' });
        this.setState({ user: result.data, loading: false }, () => {
          const title = this.props.title;
          document.title = title;
          trackPageView(window.location.hash.substr(1), title, result.data.id);
        });
      })
      .catch((error: Error) => {
        Pkce.clearUserSession();
        this.setState({ loading: false });
        this.setState({ redirectPath: '/SessionExpired' });
      });
  }

  /* tslint:disable:jsx-no-lambda */
  public render(): JSX.Element {
    const { component: Component, ...rest } = this.props;
    const roles = this.state.user.roles;
    return (
      <Route
        {...rest}
        render={(props) =>
          this.props.allowedRoles.find((allowedRole) => {
            const userHasAccess = roles.find((userRole) => userRole.id === allowedRole);
            return userHasAccess;
          }) ? (
            <>
              <Layout user={this.state.user}>
                <Component {...props} user={this.state.user} />
              </Layout>
            </>
          ) : this.state.loading ? (
            <Progress show={true} />
          ) : this.state.redirectPath !== '' ? (
            <Redirect
              to={{
                pathname: this.state.redirectPath,
              }}
            />
          ) : (
            ''
          )
        }
      />
    );
  }
}
