import cn from 'classnames';
import React, { useEffect } from 'react';
import { USER_ROLE } from 'globals/constants';
import { getTranslatedLabel } from 'globals/i18n/TranslationsProvider';
import { IRole, IUserInfo } from 'globals/types';
import { history } from './../../../router/History';
import { Pkce } from './../../../services/Pkce';
import Styles from './HeaderUserPanel.scss';
import { createQueryParams } from './../../../services/utils';
import { Envs } from 'globals/Envs';

const classNames = cn.bind(Styles);

interface IHeaderUserPanelProps {
  show?: boolean;
  onClose?: () => void;
  user: IUserInfo;
}

let isTouch = false;

export default function HeaderUserPanel(props: IHeaderUserPanelProps) {
  const enableAdmin =
    props.user.roles.find((role: IRole) => role.id === USER_ROLE.ADMIN) !== undefined ||
    props.user.roles.find((role: IRole) => role.id === USER_ROLE.REPORTADMIN) !== undefined;

  useEffect(() => {
    eventClenUp();

    if (props.show) {
      document.addEventListener('touchend', handleUserPanelOutside, true);
      document.addEventListener('click', handleUserPanelOutside, true);
    }
  }, [props.show]);

  useEffect(() => {
    return () => {
      eventClenUp();
    };
  }, []);

  const eventClenUp = () => {
    document.removeEventListener('touchend', handleUserPanelOutside, true);
    document.removeEventListener('click', handleUserPanelOutside, true);
  };

  const handleUserPanelOutside = (event: MouseEvent | TouchEvent) => {
    const helpMenuWrapper = document?.querySelector('#profileMenuContentWrapper');

    if (event.type === 'touchend') {
      isTouch = true;
    }
    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }
    const target = event.target as Element;
    if (!helpMenuWrapper?.contains(target) && !target.classList.contains('profile')) {
      eventClenUp();
      props.onClose();
    }
  };

  const onLogout = () => {
    const access_token = Pkce.readAccessToken();
    Pkce.clearUserSession();
    const redirectUrl = Pkce.getLogoutUrl();
    console.log('Error in logout the user.' + redirectUrl);
    if (Envs.OIDC_PROVIDER === 'OKTA') {
      const log_out_constant = {
        id_token_hint: access_token?.id_token,
        post_logout_redirect_uri: Envs.REDIRECT_URLS,
      };
      window.location.href = redirectUrl + '?' + createQueryParams(log_out_constant);
      console.log(`Redirecting to CD logout...` + redirectUrl);
    } else {
      console.log(`Redirecting to CD logout...` + redirectUrl);
      window.location.href = Pkce.getRedirectUrl() + '/logout';
    }
  };

  const navigateToMyBookmarks = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/bookmarks`);
    props.onClose();
  };

  const navigateToAdministration = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/administration`);
    props.onClose();
  };

  const navigateToMySolutions = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/mysolutions`);
    props.onClose();
  };

  const navigateToSettings = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/usersettings`);
    props.onClose();
  };

  // const navigateToMyProfile = (event: React.MouseEvent<HTMLElement>) => {
  //   history.push(`/profile`);
  //   props.onClose();
  // };

  return (
    <div id="profileMenuContentWrapper" className={classNames(props.show ? Styles.userContexMenu : 'hide')}>
      <div className={Styles.upArrow} />
      <ul className={classNames(Styles.innerContainer)}>
        {/* <li onClick={navigateToMyProfile}>{getTranslatedLabel('MyProfile')}</li> */}
        <li className={Styles.userName}>
          {' '}
          <span>{props.user.firstName + ' ' + props.user.lastName}</span>
          <label>{props.user.eMail}</label>{' '}
        </li>
        <li onClick={navigateToMySolutions}>{getTranslatedLabel('MySolutions')}</li>
        <li onClick={navigateToMyBookmarks}>{getTranslatedLabel('MyBookmarks')}</li>
        {enableAdmin && <li onClick={navigateToAdministration}>{getTranslatedLabel('Administration')}</li>}
        <li onClick={navigateToSettings}>{getTranslatedLabel('Settings')}</li>
        <li onClick={onLogout}>{getTranslatedLabel('LogoutButton')}</li>
      </ul>
    </div>
  );
}
