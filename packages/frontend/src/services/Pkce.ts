/* tslint:disable:no-console */
import { SESSION_STORAGE_KEYS } from '../globals/constants';
import { createQueryParams } from './utils';
import { Envs } from '../globals/Envs';
require('fast-text-encoding');

export class Pkce {
  public static readAccessToken() {
    return JSON.parse(sessionStorage.getItem(SESSION_STORAGE_KEYS.ACCESS_TOKEN));
  }

  public static writeAccessToken(obj: any) {
    return sessionStorage.setItem(SESSION_STORAGE_KEYS.ACCESS_TOKEN, JSON.stringify(obj));
  }

  public static destroyAccessToken() {
    return sessionStorage.removeItem(SESSION_STORAGE_KEYS.ACCESS_TOKEN);
  }

  public static clearUserSession() {
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.PKCE);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.JWT);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.USER_ID);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.ACCESS_TOKEN);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.PORTFOLIO_FILTER_VALUES);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.REPORT_FILTER_VALUES);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE);
  }

  public static getToken(code: string) {
    return fetch(`${this.getLoginTokenUrl()}`, {
      body: createQueryParams({
        client_id: this.getClientId(),
        code,
        code_verifier: sessionStorage.getItem(SESSION_STORAGE_KEYS.PKCE),
        grant_type: 'authorization_code',
        redirect_uri: this.getRedirectUrl(),
      }),
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      method: 'POST',
    })
      .then((response) => {
        if (!response.ok) {
          return response.json().then((result: any) => {
            throw new Error(result.Message);
          });
        }
        sessionStorage.removeItem(SESSION_STORAGE_KEYS.PKCE); // why remove code here ?
        return response.json();
      })
      .then((result) => {
        this.writeAccessToken(result);
        return result;
      });
  }

  public static createAuthUrl() {
    return this.hash('SHA-256', this.createVerifier()).then((result) => {
      const params = {
        client_id: this.getClientId(),
        code_challenge: this.encode64(result),
        code_challenge_method: 'S256',
        redirect_uri: this.getRedirectUrl(),
        response_type: 'code',
        scope: 'openid profile email',
        state: 'abc',
        nonce: 'UBGW',
      };
      return this.getLoginAuthUrl() + '?' + createQueryParams(params);
    });
  }

  public static revokeToken() {
    const token = this.readAccessToken();
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.PKCE);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.JWT);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.USER_ID);
    sessionStorage.removeItem(SESSION_STORAGE_KEYS.ACCESS_TOKEN);
    if (token) {
      return fetch(`${this.getLoginRevokeUrl()}`, {
        body: createQueryParams({
          client_id: this.getClientId(),
          token: token.access_token,
        }),
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        method: 'POST',
      }).then(() => {
        if (Envs.OIDC_PROVIDER === 'OKTA') {
          const log_out_constant = {
            id_token_hint: this.readAccessToken().id_token,
            post_logout_redirect_uri: Envs.REDIRECT_URLS,
          };

          return this.getLogoutUrl() + '?' + createQueryParams(log_out_constant);
        } else {
          return this.getLogoutUrl();
        }
      });
    } else {
      if (Envs.OIDC_PROVIDER === 'OKTA') {
        const log_out_constant = {
          id_token_hint: this.readAccessToken().id_token,
          post_logout_redirect_uri: Envs.REDIRECT_URLS,
        };

        return Promise.resolve(this.getLogoutUrl() + '?' + createQueryParams(log_out_constant));
      } else {
        return Promise.resolve(this.getLogoutUrl());
      }
    }
  }

  public static getLogoutUrl = () => {
    if (Envs.OAUTH2_LOGOUT_URL) {
      return Envs.OAUTH2_LOGOUT_URL;
    }

    console.warn('No logout url for' + window.location.hostname);
    return '';
  };

  protected static encode64(buff: ArrayBuffer) {
    return btoa(String.fromCharCode.apply(null, new Uint8Array(buff)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '');
  }

  protected static base64URLEncode(str: Buffer) {
    return str.toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
  }

  protected static guid() {
    const buf = new Uint16Array(16);
    window.crypto.getRandomValues(buf);
    const s4 = (num: number) => {
      let ret = num.toString(16);
      while (ret.length < 4) {
        ret = '0' + ret;
      }
      return ret;
    };
    return new Buffer(
      s4(buf[0]) +
        s4(buf[1]) +
        s4(buf[2]) +
        s4(buf[3]) +
        s4(buf[4]) +
        s4(buf[5]) +
        s4(buf[6]) +
        s4(buf[7]) +
        s4(buf[8]) +
        s4(buf[9]) +
        s4(buf[10]) +
        s4(buf[11]) +
        s4(buf[12]) +
        s4(buf[13]) +
        s4(buf[14]) +
        s4(buf[15]),
    );
  }

  /**
   * Creates a new code verifier and stores it to SESSION_STORAGE_KEYS.PKCE.
   * @returns {string} code verifier
   */
  protected static createVerifier(): string {
    const verifier = this.base64URLEncode(this.guid());
    sessionStorage.setItem(SESSION_STORAGE_KEYS.PKCE, verifier);
    return verifier;
  }

  protected static hash(algorithm: string, str: string) {
    if (window.crypto.subtle) {
      return window.crypto.subtle.digest(algorithm, new TextEncoder().encode(str));
    }
    return Promise.resolve(new ArrayBuffer(0));
  }

  protected static getClientId = () => {
    if (Envs.CLIENT_IDS) {
      return Envs.CLIENT_IDS;
    }

    console.warn('No clientId for ' + window.location.hostname);
    return '';
  };

  public static getRedirectUrl = () => {
    if (Envs.REDIRECT_URLS) {
      return Envs.REDIRECT_URLS;
    }

    console.warn('No redirect url for' + window.location.hostname);
    return '';
  };

  protected static getLoginAuthUrl = () => {
    if (Envs.OAUTH2_AUTH_URL) {
      return Envs.OAUTH2_AUTH_URL;
    }

    console.warn('No auth url for' + window.location.hostname);
    return '';
  };

  protected static getLoginRevokeUrl = () => {
    if (Envs.OAUTH2_REVOKE_URL) {
      return Envs.OAUTH2_REVOKE_URL;
    }

    console.warn('No revoke url for' + window.location.hostname);
    return '';
  };

  protected static getLoginTokenUrl = () => {
    if (Envs.OAUTH2_TOKEN_URL) {
      return Envs.OAUTH2_TOKEN_URL;
    }

    console.warn('No token url for' + window.location.hostname);
    return '';
  };
}
