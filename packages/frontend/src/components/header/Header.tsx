import classNames from 'classnames';
import * as React from 'react';
import { IUserInfo } from 'globals/types';
import { history } from './../../router/History';
import { getPath } from './../../router/RouterUtils';
import Styles from './Header.scss';
import { HeaderSearchBox } from './headerSearchBox/HeaderSearchBox';
import HeaderUserPanel from './headerUserPanel/HeaderUserPanel';
import HeaderContactPanel from './headerContactPanel/HeaderContactPanel';
import { NotificationPanel } from './notificationpanel/NotificationPanel';
import { NotificationApiClient } from '../../services/NotificationApiClient';
import { Envs } from 'globals/Envs';
import AppContext from '../context/ApplicationContext';

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
  showContactPanel: boolean;
  notificationPanel: boolean;
  notifications: any;
  showInfoModal: boolean;
  totalRecordCount: number;
}

export default class Header extends React.Component<IHeaderProps, IHeaderState> {
  protected showPanel: () => {};

  public constructor(props: IHeaderProps, context?: any) {
    super(props, context);
    this.state = {
      showNavigation: false,
      showUserPanel: false,
      showContactPanel: false,
      notificationPanel: false,
      notifications: [],
      showInfoModal: false,
      totalRecordCount: 0,
    };
  }

  public render() {
    const { setMessage } = this.context;

    /******** Following line is using context API to check for change and then setting new value ********/
    if (this.context.message === 'UPDATE_NOTIFICATIONS') {
      setMessage('COMPLETE_UPDATE_NOTIFICATIONS');
      this.fetchNotification();
    }
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
          <div className={classNames(Styles.headerIcons, 'app-info')}>
            <div
              id="notificationPanel"
              className={classNames(Styles.userInfoPanel, Envs.ENABLE_NOTIFICATION ? '' : 'hide')}
            >
              <div onClick={this.toggleNotificationPanel} className={classNames(Styles.avatar, 'userAvatar')}>
                <div className={Styles.userIcon}>
                  <i
                    className={classNames(
                      'icon mbc-icon notification',
                      this.state.totalRecordCount > 0 ? Styles.notificationIcon : '',
                    )}
                  />
                </div>
                <span className={classNames(Styles.status, this.state.totalRecordCount > 0 ? '' : 'hide')}>
                  {this.state.totalRecordCount > 99 ? '99+' : this.state.totalRecordCount}
                </span>
              </div>
              <div className={Styles.notificationPanel}>
                <NotificationPanel
                  show={this.state.notificationPanel}
                  userId={this.props.user.id}
                  notifications={this.state.notifications}
                  onClose={this.closeNotificationPanel}
                />
              </div>
            </div>
            <div id="userInfoPanel" className={Styles.userInfoPanel}>
              <div
                className={classNames(Styles.avatar, 'userAvatar')}
                title={this.props.user.firstName + ', ' + this.props.user.lastName}
                onClick={this.toggleUserPanel}
              >
                <div className={classNames(Styles.userIcon, 'profile')}>
                  <i className="icon mbc-icon profile" />
                </div>
                <span className={classNames(Styles.status, 'hide')} />
              </div>
              <HeaderUserPanel show={this.state.showUserPanel} onClose={this.closeUserPanel} user={this.props.user} />
            </div>
            <div id="contactPanel" className={Styles.userInfoPanel}>
              <div className={classNames(Styles.avatar, 'userAvatar')} title={'Help'} onClick={this.toggleHelpPanel}>
                <div className={classNames(Styles.userIcon, 'help')}>
                  <i className="icon mbc-icon help" />
                </div>
                <span className={classNames(Styles.status, 'hide')} />
              </div>
              <HeaderContactPanel show={this.state.showContactPanel} onClose={this.closeContactPanel} />
            </div>
          </div>
          <div className={classNames(Styles.appLogo, 'app-info')}>
            {/* <h6 className="app-name">{getTranslatedLabel('HeaderName')}</h6> */}
            <h6 className="app-name">{Envs.DNA_APPNAME_HEADER}</h6>
            <img className="app-logo" src={Envs.DNA_APP_LOGO_URL} />
          </div>
        </div>
      </header>
    );
  }

  public componentDidMount() {
    history.listen((location, action) => {
      // console.log(`The current URL is ${location.pathname}${location.search}${location.hash}`)
      // console.log(`The last navigation action was ${action}`)
    });
    clearInterval(window.NOTIFICATION_POLL_ID);
    window.NOTIFICATION_POLL_ID = setInterval(() => {
      this.fetchNotification();
    }, 10000);
    this.fetchNotification();
  }

  protected fetchNotification() {
    if (Envs.ENABLE_NOTIFICATION) {
      NotificationApiClient.getNotifications(this.props.user.id, 5, 0, 'unread')
        .then((response: any) => {
          this.setState({ notifications: response.records, totalRecordCount: response.totalRecordCount });
        })
        .catch(() => {
          clearInterval(window.NOTIFICATION_POLL_ID);
        });
    }
  }

  protected closeUserPanel = () => {
    this.setState({ showUserPanel: false });
  };

  protected closeContactPanel = () => {
    this.setState({ showContactPanel: false });
  };

  protected toggleUserPanel = () => {
    this.setState({ showUserPanel: !this.state.showUserPanel, notificationPanel: false });
  };

  protected toggleNotificationPanel = () => {
    this.setState({ notificationPanel: !this.state.notificationPanel });
  };

  protected closeNotificationPanel = () => {
    this.setState({ notificationPanel: false });
  };

  protected toggleHelpPanel = () => {
    this.setState({ showContactPanel: !this.state.showContactPanel, notificationPanel: false });
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
Header.contextType = AppContext;
