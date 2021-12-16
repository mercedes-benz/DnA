import cn from 'classnames';
import * as React from 'react';
import { ApiClient } from '../../../../src/services/ApiClient';
import { USER_ROLE } from '../../../globals/constants';
import { getTranslatedLabel } from '../../../globals/i18n/TranslationsProvider';
import { IRole, IUserInfo } from '../../../globals/types';
import { history } from './../../../router/History';
import { Pkce } from './../../../services/Pkce';
import Styles from './HeaderUserPanel.scss';
import { createQueryParams } from './../../../services/utils';
import { Envs } from '../../../globals/Envs';

const classNames = cn.bind(Styles);

export interface IHeaderUserPanelProps {
  show?: boolean;
  onClose?: () => void;
  user: IUserInfo;
}
export interface IHeaderUserPanelState {
  isAdmin: boolean;
}

export class HeaderUserPanel extends React.Component<IHeaderUserPanelProps, IHeaderUserPanelState> {
  protected isTouch = false;
  protected domContainer: HTMLDivElement;

  public constructor(props: IHeaderUserPanelProps, context?: any) {
    super(props, context);
    this.state = {
      isAdmin: this.props.user.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN) !== undefined,
    };
  }

  public componentWillMount() {
    document.addEventListener('touchend', this.handleUserPanelOutside, true);
    document.addEventListener('click', this.handleUserPanelOutside, true);
  }

  public componentWillUnmount() {
    document.removeEventListener('touchend', this.handleUserPanelOutside, true);
    document.removeEventListener('click', this.handleUserPanelOutside, true);
  }

  public render() {
    return (
      <div className={classNames(this.props.show ? Styles.userContexMenu : 'hide')} ref={this.connectContainer}>
        <div className={Styles.upArrow} />
        <ul className={classNames(Styles.innerContainer)}>
          {/* <li onClick={this.navigateToMyProfile}>{getTranslatedLabel('MyProfile')}</li> */}
          <li className={Styles.userName}>
            {' '}
            <span>{this.props.user.firstName + ' ' + this.props.user.lastName}</span>
            <label>{this.props.user.eMail}</label>{' '}
          </li>
          <li onClick={this.navigateToMySolutions}>{getTranslatedLabel('MySolutions')}</li>
          <li onClick={this.navigateToMyBookmarks}>{getTranslatedLabel('MyBookmarks')}</li>
          {this.state.isAdmin && (
            <li onClick={this.navigateToAdministration}>{getTranslatedLabel('Administration')}</li>
          )}
          <li onClick={this.onLogout}>{getTranslatedLabel('LogoutButton')}</li>
        </ul>
      </div>
    );
  }

  protected onLogout = () => {
    ApiClient.logoutUser()
      .then(() => {
        const access_token = Pkce.readAccessToken();
        Pkce.clearUserSession();
        const redirectUrl = Pkce.getLogoutUrl();
        console.log('Error in logout the user.' + redirectUrl);
        if (Envs.OIDC_PROVIDER === 'OKTA') {
          const log_out_constant = {
            id_token_hint: access_token.id_token,
            post_logout_redirect_uri: Envs.REDIRECT_URLS,
          };
          window.location.href = redirectUrl + '?' + createQueryParams(log_out_constant);
          console.log(`Redirecting to CD logout...` + redirectUrl);
        } else {
          console.log(`Redirecting to CD logout...` + redirectUrl);
          window.location.href = redirectUrl;
        }

        /* tslint:disable:no-console */
      })
      .catch((err: Error) => {
        /* tslint:disable:no-console */
        console.log('Error in logout the user.');
      });
  };

  protected navigateToMyBookmarks = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/bookmarks`);
    this.props.onClose();
  };
  protected navigateToAdministration = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/administration`);
    // this.props.onClose();
  };
  protected navigateToMySolutions = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/mysolutions`);
    this.props.onClose();
  };
  protected navigateToMyProfile = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/profile`);
    this.props.onClose();
  };

  protected connectContainer = (element: HTMLDivElement) => {
    this.domContainer = element;
  };

  protected handleUserPanelOutside = (event: MouseEvent | TouchEvent) => {
    if (!this.props.show) {
      return;
    }

    if (event.type === 'touchend') {
      this.isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && this.isTouch === true) {
      return;
    }

    const target = event.target as Element;

    if (
      this.domContainer &&
      target.className !== 'userAvatar' &&
      target.className.indexOf('iconContainer') === -1 &&
      this.domContainer.contains(target) === false
    ) {
      event.stopPropagation();
      this.props.onClose();
    }
  };
}
