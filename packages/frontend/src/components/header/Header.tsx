import classNames from 'classnames';
import * as React from 'react';
import { IUserInfo } from '../../globals/types';
import { history } from './../../router/History';
import { getPath } from './../../router/RouterUtils';
import Styles from './Header.scss';
import { HeaderSearchBox } from './headerSearchBox/HeaderSearchBox';
import { HeaderUserPanel } from './headerUserPanel/HeaderUserPanel';
import { NotificationPanel } from './notificationpanel/NotificationPanel';
import { Envs } from '../../globals/Envs';

export interface IHeaderProps {
  user: IUserInfo;
  onHamburgerIconMouseHover: () => void;
  onHamburgerIconClick: () => void;
  showMenuCloseIcon: boolean;
  isHome: boolean;
}

export interface IHeaderState {
  showNavigation: boolean;
  showUserPanel: boolean;
  notificationPanel: boolean;
}

export class Header extends React.Component<IHeaderProps, IHeaderState> {
  protected showPanel: () => {};

  public constructor(props: IHeaderProps, context?: any) {
    super(props, context);
    this.state = {
      showNavigation: false,
      showUserPanel: false,
      notificationPanel: false,
    };
  }

  public render() {
    return (
      <header
        id="header"
        className={classNames(Styles.mainHeader, ' nav-header', this.props.isHome && 'nav-header-noshadow')}
      >
        <div className={Styles.logoWrapper}>
          <span
            onMouseEnter={this.props.onHamburgerIconMouseHover}
            onMouseLeave={this.props.onHamburgerIconMouseHover}
            onClick={this.props.onHamburgerIconClick}
            className={Styles.menuTrigger}
          >
            {this.props.showMenuCloseIcon ? (
              <i className={classNames('icon mbc-icon close thin', Styles.closeIcon)} />
            ) : (
              <i className={classNames('icon mbc-icon hamburger', Styles.hamburgerIcon)} />
            )}
          </span>
          <a href="#" className={classNames(Styles.logoCompany, 'brand')}>
            <img src={Envs.DNA_BRAND_LOGO_URL} />
          </a>
        </div>
        <div className={Styles.appWrapper}>
          <HeaderSearchBox />
          <div className={classNames(Styles.notificationPanel, 'hide')}>
            <div onClick={this.toggleNotificationPanel}>
              <div className={Styles.userIcon}>
                <i className="icon mbc-icon notification" />
              </div>
              <span className={classNames(Styles.status, 'hide')} />
            </div>
            <div className={Styles.notificationPanel}>
              <NotificationPanel show={this.state.notificationPanel} onClose={this.closeNotificationPanel} />
            </div>
          </div>
          <div className={classNames(Styles.appLogo, 'app-info')}>
            <div className={Styles.userInfoPanel}>
              <div
                className={classNames(Styles.avatar, 'userAvatar')}
                title={this.props.user.firstName + ', ' + this.props.user.lastName}
                onClick={this.toggleUserPanel}
              >
                <div className={Styles.userIcon}>
                  <i className="icon mbc-icon profile" />
                </div>
                <span className={classNames(Styles.status, 'hide')} />
              </div>
              <HeaderUserPanel show={this.state.showUserPanel} onClose={this.closeUserPanel} user={this.props.user} />
            </div>
            {/* <h6 className="app-name">{getTranslatedLabel('HeaderName')}</h6> */}
            <h6 className="app-name">{Envs.DNA_APPNAME_HEADER}</h6>
            <img className="app-logo" src={Envs.DNA_APP_LOGO_URL} />
          </div>
        </div>
      </header>
    );
  }

  protected closeUserPanel = () => {
    this.setState({ showUserPanel: false });
  };

  protected toggleUserPanel = () => {
    this.setState({ showUserPanel: !this.state.showUserPanel });
  };
  protected toggleNotificationPanel = () => {
    // this.setState({ notificationPanel: !this.state.notificationPanel });
  };
  protected closeNotificationPanel = () => {
    this.setState({ notificationPanel: false });
  };

  protected toggleNavigation = () => {
    this.setState({ showNavigation: !this.state.showNavigation });
  };

  protected toggleOnLogoClick = () => {
    history.push(`/portfolio`, { forceRefresh: true });
    if (getPath().includes('/portfolio')) {
      history.go(0);
    }
  };
}
