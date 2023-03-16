/* tslint:disable:no-console */
import * as React from 'react';
import { removeURLParameter } from '../../../frontend/src/services/utils';
import Progress from 'components/progress/Progress';
import { SESSION_STORAGE_KEYS } from 'globals/constants';
import { Envs } from 'globals/Envs';
import { history } from './../router/History';
import { ApiClient } from './../services/ApiClient';
import { Pkce } from './../services/Pkce';
import { getQueryParameterByName } from './../services/Query';

export interface IAuthState {
  fetchingCode: boolean;
  fetchingJwt: boolean;
  fetchingAccessToken: boolean;
  hasCode: boolean;
  hasJwt: boolean;
  hasAccessToken: boolean;
  isCodeInvalid: boolean;
  isAccessTokenInvalid: boolean;
}

export default class AuthRedirector extends React.Component<{}, IAuthState> {
  public constructor(props: {}, context: any) {
    super(props, context);
    this.state = {
      fetchingAccessToken: false,
      fetchingCode: false,
      fetchingJwt: false,
      hasAccessToken: Envs.OIDC_DISABLED ? true : !!sessionStorage.getItem(SESSION_STORAGE_KEYS.ACCESS_TOKEN),
      hasCode: Envs.OIDC_DISABLED ? true : !!sessionStorage.getItem(SESSION_STORAGE_KEYS.PKCE),
      hasJwt: !!sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT),
      isAccessTokenInvalid: false,
      isCodeInvalid: false,
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
      fetchingCode,
      fetchingAccessToken,
      fetchingJwt,
      hasCode,
      hasJwt,
      hasAccessToken,
      isAccessTokenInvalid,
      isCodeInvalid,
    } = this.state;

    if (!sessionStorage.getItem(SESSION_STORAGE_KEYS.REDIRECT_URL)) {
      sessionStorage.setItem(SESSION_STORAGE_KEYS.REDIRECT_URL, window.location.href);
    }
    /**
     * Redirect to CD login if
     * code is invalid
     * or
     * no code
     * no access token
     * no jwt
     * => returns back to this page with a code
     */
    if (isCodeInvalid || (!hasCode && !hasAccessToken && !hasJwt && !fetchingCode)) {
      console.log('Authorizing: Request authorization code');
      this.setState({
        fetchingCode: true,
      });
      this.redirectToCorporateLogin();
      return;
    }

    /**
     * Request access token if
     * has code
     * code is not invalid
     * no access token
     * no jwt
     * => returns access token if code is valid
     * => error if code is not valid
     */
    if (hasCode && !isCodeInvalid && !hasAccessToken && !hasJwt && !fetchingAccessToken) {
      console.log('Authorizing: Request access token');
      const code = getQueryParameterByName('code');
      if (code) {
        sessionStorage.setItem(SESSION_STORAGE_KEYS.CODE, code);
        this.setState({
          fetchingAccessToken: true,
        });
        Pkce.getToken(code)
          .then(() => {
            this.reloadPage();
          })
          .catch(() => {
            console.log('Authorizing: Request access token FAILED');
            this.setState({
              fetchingAccessToken: false,
              isCodeInvalid: true,
            });
          });
      } else {
        sessionStorage.removeItem(SESSION_STORAGE_KEYS.PKCE);
        this.reloadPage();
      }
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
    if (hasAccessToken && !isAccessTokenInvalid && !hasJwt && !fetchingJwt) {
      console.log('Authorizing: Request Jwt');
      this.setState({
        fetchingJwt: true,
      });
      ApiClient.getJwt()
        .then(() => {
          this.setState({
            fetchingJwt: false,
            hasJwt: true,
          });
        })
        .catch((error) => {
          this.setState({
            fetchingJwt: false,
            isAccessTokenInvalid: true,
          });
          console.log('Authorizing: Request Jwt FAILED');
          console.log(error);
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
   * Redirects the user to the CD login page and automatically creates a code verifier, which
   * gets stored to sessionStorage.
   * That means, that the variable hasCode of AuthRedirector.state is then set to true.
   */
  protected redirectToCorporateLogin = () => {
    ApiClient.destroyJwt();
    Pkce.destroyAccessToken();
    Pkce.createAuthUrl().then((redirectUrl) => {
      console.log(`Redirecting to CD login...`);
      window.location.href = redirectUrl;
    });
  };
}
