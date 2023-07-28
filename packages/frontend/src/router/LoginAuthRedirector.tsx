/* tslint:disable:no-console */
import * as React from 'react';
import { removeURLParameter } from '../services/utils';
import Progress from 'components/progress/Progress';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { Envs } from 'globals/Envs';
import { history } from './History';
import { ApiClient } from '../services/ApiClient';
import { Pkce } from '../services/Pkce';

export interface IAuthState {
  fetchingJwt: boolean;
  hasJwt: boolean;
  isAccessTokenInvalid: boolean;
  isredirected: boolean;
  count: number;
}

export default class LoginAuthRedirector extends React.Component<{}, IAuthState> {
  public constructor(props: {}, context: any) {
    super(props, context);
    this.state = {
      fetchingJwt: false,
      hasJwt: !!sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT),
      isAccessTokenInvalid: false,
      count: 2,
      isredirected: false
    };

    if (Envs.OIDC_DISABLED) {
      // provide any mock value for the access token in dev mode
      Pkce.writeAccessToken(JSON.parse('{"access_token": "blablabla"}'));
    }
  }

  public componentDidMount() {
    this.processAuth();
  }
  public componentDidUpdate() {
    this.processAuth();
  }

  public render() {
    return <Progress show={true} />;
  }

  protected processAuth() {
    const {
      hasJwt,
    } = this.state;

    if (!sessionStorage.getItem(SESSION_STORAGE_KEYS.REDIRECT_URL)) {
      sessionStorage.setItem(SESSION_STORAGE_KEYS.REDIRECT_URL, window.location.href);
    }

    /**
     * Redirect to login (Kong manages Authentication)
     * no jwt
     * => redirect to login
     */
    if (!hasJwt) {
      console.log('Redirecting to login page');
      this.redirectToLogin();
      return;
    }

    /**
     * Request jwt if
     * has access token
     * access token is not invalid
     * no jwt
     * => return jwt if access token is valid
     * => error if access token is not valid
     */
    if (!hasJwt && this.state.count > 0) {
      console.log('Authorizing: Request Jwt');
      this.setState({
        fetchingJwt: true,
      });
      ApiClient.getJwt()
        .then(() => {
          this.setState({
            fetchingJwt: false,
            hasJwt: true,
            isredirected: true
          });
          const newURL = window.location.origin + '/';
          window.location.assign(newURL);
        })
        .catch((error) => {
          this.setState({
            fetchingJwt: false,
            isAccessTokenInvalid: true,
          });
          console.log('Authorizing: Request Jwt FAILED');
          console.log(error);
        });
      this.setState({
        count: this.state.count - 1,
      });
    }

    if (hasJwt) {
      const reqUrl = sessionStorage.getItem(SESSION_STORAGE_KEYS.APPREDIRECT_URL);
      if (!reqUrl) {
        console.log('redirect to home');
        history.replace('home');
      } else {
        console.log('redirect to requested URL' + reqUrl);
        sessionStorage.removeItem(SESSION_STORAGE_KEYS.APPREDIRECT_URL);
        history.replace(reqUrl);
      }
    }
  }

  protected reloadPage = () => {
    const currentUrl = window.location.href;
    let reloadUrl = removeURLParameter(currentUrl, 'code');
    reloadUrl = removeURLParameter(reloadUrl, 'state');
    window.history.replaceState({}, '', reloadUrl);
    window.location.reload();
  };

  /**
   * Redirects the user to the /login/redirect page and automatically after successfully generating jwt which
   * gets stored to the sessionStorage.
   */
  protected redirectToLogin = () => {
    const newURL = Pkce.getRedirectUrl() + '/login/redirect';
    window.location.assign(newURL);
  };
}
