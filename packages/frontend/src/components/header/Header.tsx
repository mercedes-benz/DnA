import classNames from 'classnames';
import React, {useState, useEffect, useContext} from 'react';
import { IUserInfo } from 'globals/types';
import { history } from './../../router/History';
// import { getPath } from './../../router/RouterUtils';
import Styles from './Header.scss';
import HeaderSearchBox from './headerSearchBox/HeaderSearchBox';
import HeaderUserPanel from './headerUserPanel/HeaderUserPanel';
import HeaderContactPanel from './headerContactPanel/HeaderContactPanel';
import { NotificationPanel } from './notificationpanel/NotificationPanel';
import { NotificationApiClient } from '../../services/NotificationApiClient';
import { Envs } from 'globals/Envs';
import AppContext from '../context/ApplicationContext';
import { Link } from 'react-router-dom';
import { getPath } from './../../router/RouterUtils';
import Modal from 'components/formElements/modal/Modal';
import { ApiClient } from '../../services/ApiClient';
import Notification from '../../assets/modules/uilab/js/src/notification';
import IconToU from 'components/icons/IconToU';
import SantaHat from '../../assets/images/santa-hat.png';

export interface IHeaderProps {
  user: IUserInfo;
  onHamburgerIconMouseHover: () => void;
  onHamburgerIconClick: () => void;
  showMenuCloseIcon: boolean;
  isHome?: boolean;
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

const Header:React.FC<IHeaderProps> = (props) => {

  // const showPanel: () => {};

  const context = useContext(AppContext);

  // const [showNavigation, setShowNavigation] = useState<boolean>(false);
  const [showUserPanel, setShowUserPanel] = useState<boolean>(false);
  const [showContactPanel, setShowContactPanel] = useState<boolean>(false);
  const [notificationPanel, setNotificationPanel] = useState<boolean>(false);
  const [notifications, setNotifications] = useState<any>([]);
  // const [showInfoModal, setShowInfoModal] = useState<boolean>(false);
  const [totalRecordCount, setTotalRecordCount] = useState<number>(0);
  const [isTouChecked, setIsTouChecked] = useState<boolean>(false);
  const [userPreferences, setUserPreferences] = useState<any>();

  useEffect(() => {
    history.listen((location, action) => {
      // console.log(`The current URL is ${location.pathname}${location.search}${location.hash}`)
      // console.log(`The last navigation action was ${action}`)
    });
    clearInterval(window.NOTIFICATION_POLL_ID);
    window.NOTIFICATION_POLL_ID = setInterval(() => {
      fetchNotification();
    }, 10000);
    fetchNotification();
  }, []);

  const fetchNotification = () => {
    if (Envs.ENABLE_NOTIFICATION) {
      NotificationApiClient.getNotifications(props.user.id, 5, 0, 'unread')
        .then((response: any) => {
          setNotifications(response.records);
          setTotalRecordCount(response.totalRecordCount)
        })
        .catch((error) => {
          if(error?.message) {
            Notification.show(error.message, 'alert');
          }
          clearInterval(window.NOTIFICATION_POLL_ID);
        });
    }
  }

  const getUserPreferences = () => {
    ApiClient.getNotificationPreferences(props.user.id)
      .then((response: any) => {
        setUserPreferences(response);
        setIsTouChecked(response.termsOfUse);
        setShowTermsModal(!response.termsOfUse);
      })
      .catch(() => {
        
      });
  }

  useEffect(() => {
    getUserPreferences();
  }, []);

  const closeUserPanel = () => {
    setShowUserPanel(false);
  };

  const closeContactPanel = () => {
    setShowContactPanel(false);
  };

  const toggleUserPanel = () => {
    setShowUserPanel(!showUserPanel);
    setNotificationPanel(false);
  };

  const toggleNotificationPanel = () => {
    setNotificationPanel(!notificationPanel);
  };

  const closeNotificationPanel = () => {
    setNotificationPanel(false);
  };

  const toggleHelpPanel = () => {
    setShowContactPanel(!showContactPanel);
    setNotificationPanel(false);
  };

  // const toggleNavigation = () => {
  //   setShowNavigation(!setShowNavigation);
  // };

  // const toggleOnLogoClick = () => {
  //   history.push(`/portfolio`, { forceRefresh: true });
  //   if (getPath().includes('/portfolio')) {
  //     history.go(0);
  //   }
  // };

  const { setMessage, setShowTermsModal } = context;

  /******** Following line is using context API to check for change and then setting new value ********/
  if (context.message === 'UPDATE_NOTIFICATIONS') {
    setMessage('COMPLETE_UPDATE_NOTIFICATIONS');
    fetchNotification();
  }

  // const [isDisabled, setIsDisabled] = useState(true);

  const handleTouAccept = () => {
    setShowTermsModal(false);
    ApiClient.enableEmailNotifications({...userPreferences, termsOfUse: isTouChecked})
      .then(() => {
        Notification.show('Terms of Use accepted successfully');
        getUserPreferences();
      })
      .catch(() => {
        Notification.show('Error while accepting Terms of Use', 'alert');
      });
  }

  const infoModalContent = (
    <div className={Styles.touContainer}>
      <div className={Styles.touTitle}>
        <div className={Styles.touIcon}>
          <IconToU size='80'/>
        </div>
        <h2>
          Terms of Use
          <span>{userPreferences?.termsOfUse ? "Agreed to terms of use" : "Please agree to our terms of use before you start."}</span>
        </h2>
      </div>
      <div className={Styles.touContent}>
        <div
          dangerouslySetInnerHTML={{
            __html: Envs.TERMS_OF_USE_CONTENT,
          }}
        ></div>
        {
          userPreferences?.termsOfUse ?
            '' :
            <div> 
              <label className={classNames('checkbox', Styles.checkbox)}>
                <span className="wrapper">
                  <input
                    name="write"
                    type="checkbox"
                    className="ff-only"
                    checked={isTouChecked}
                    onChange={(e) => {
                      setIsTouChecked(!isTouChecked);
                    }}
                  />
                </span>
                I have read and agree to the Terms of Use
              </label>
            </div>
        }
      </div>
      <div className={Styles.touFooter}>
        {
          userPreferences?.termsOfUse ?
            '' :
            <button className={classNames('btn', isTouChecked && Styles.btnAgree)} onClick={handleTouAccept} disabled={!isTouChecked}>Accept & Enter</button>
        }
      </div>
    </div>
  );

  return (
    <>
    <header
      id="header"
      className={classNames(Styles.mainHeader, 'nav-header')}
    >
      <div className={Styles.logoWrapper}>
        <span
          onMouseEnter={props.onHamburgerIconMouseHover}
          onMouseLeave={props.onHamburgerIconMouseHover}
          onClick={props.onHamburgerIconClick}
          className={Styles.menuTrigger}
        >
          {props.showMenuCloseIcon ? (
            <i className={classNames('icon mbc-icon close thin', Styles.closeIcon)} />
          ) : (
            <i className={classNames('icon mbc-icon hamburger', Styles.hamburgerIcon)} />
          )}
        </span>

        <a href="#" className={classNames(Styles.logoCompany, 'brand')}>
          <img src={Envs.DNA_BRAND_LOGO_URL} />
        </a>

        <div className={classNames(Styles.appLogo, 'app-info')}>
          {new Date().getMonth() === 11 && <img className={Styles.santaHat} src={SantaHat} />} {/* Santa hat comes on to DnA logo on December */}
          <img className="app-logo" src={Envs.DNA_APP_LOGO_URL} />
          <h6 className="app-name">{Envs.DNA_APPNAME_HEADER}</h6>
        </div>
      </div>
      <div className={Styles.search}>
        <HeaderSearchBox />
      </div>
      <div className={Styles.appWrapper}>
        <div className={classNames(Styles.headerIcons, 'app-info')}>
          <div
            id="notificationPanel"
            className={classNames(Styles.userInfoPanel, Envs.ENABLE_NOTIFICATION ? '' : 'hide')}
          >
            <div onClick={toggleNotificationPanel} className={classNames(Styles.avatar, 'userAvatar')}>
              <div className={Styles.userIcon}>
                <i
                  className={classNames(
                    'icon mbc-icon notification', Styles.rotate,
                    totalRecordCount > 0 ? Styles.notificationIcon : '',
                  )}
                />
              </div>
              <span className={classNames(Styles.status, totalRecordCount > 0 ? '' : 'hide')}>
                {totalRecordCount > 99 ? '99+' : totalRecordCount}
              </span>
            </div>
            <div className={Styles.notificationPanel}>
              <NotificationPanel
                show={notificationPanel}
                userId={props.user.id}
                notifications={notifications}
                onClose={closeNotificationPanel}
              />
            </div>
          </div>
          <div id="userInfoPanel" className={Styles.userInfoPanel}>
            <div
              className={classNames(Styles.avatar, 'userAvatar')}
              title={props.user.firstName + ', ' + props.user.lastName}
              onClick={toggleUserPanel}
            >
              <div className={classNames(Styles.userIcon, 'profile')}>
                <i className="icon mbc-icon profile" />
              </div>
              <span className={classNames(Styles.status, 'hide')} />
            </div>
            <HeaderUserPanel show={showUserPanel} onClose={closeUserPanel} user={props.user} />
          </div>
          <div id="contactPanel" className={Styles.userInfoPanel}>
            <div className={classNames(Styles.avatar, 'userAvatar')} title={'Help'} onClick={toggleHelpPanel}>
              <div className={classNames(Styles.userIcon, 'help')}>
                <i className="icon mbc-icon help" />
              </div>
              <span className={classNames(Styles.status, 'hide')} />
            </div>
            <HeaderContactPanel show={showContactPanel} onClose={closeContactPanel} />
          </div>
        </div>
      </div>
    </header>
    {
      // !props.isHome &&
      false &&
        <div className={Styles.levelTwoNav}>
          <div className={Styles.navItemContainer}>
            <div className={Styles.navItem}>
              <Link to="/home" className={getPath().includes('/home') ? Styles.active : ''}>
                <i className={classNames('icon mbc-icon home')} />
                <span>Home</span>
              </Link>
              <p>Amet consetetur lorem ipsum dolor sit amet. 
              34 updates</p>
            </div>
          </div>
          <div className={Styles.navItemContainer}>
            <div className={Styles.navItem}>
              <Link to="/transparency" className={getPath().includes('/transparency') ? Styles.active : ''}>
                <i className={classNames('icon mbc-icon reports')} />
                <span>Transparency</span>
              </Link>
              <p>Explore all Data & AI Solutions in MB and view your Portfolio.</p>
            </div>
          </div>
          <div className={Styles.navItemContainer}>
            <div className={Styles.navItem}>
              <Link to="/data" className={getPath().includes('/data') ? Styles.active : ''}>
                <i className={classNames('icon mbc-icon solutions')} />
                <span>Data</span>
              </Link>
              <p>From Data Products to Data Governance - all you need to work is here</p>
            </div>
          </div>
          <div className={Styles.navItemContainer}>
            <div className={Styles.navItem}>
              <Link to="/tools" className={getPath().includes('/tools') ? Styles.active : ''}>
                <i className={classNames('icon mbc-icon dashboard')} />
                <span>Tools</span>
              </Link>
              <p>Our standard Data & Analytics for both FC Users and Pro Developers</p>
            </div>
          </div>
          <div className={Styles.navItemContainer}>
            <div className={classNames(Styles.navItem, Styles.disabled)}>
              <Link to="/trainings" className={getPath().includes('/trainings') ? Styles.active : ''}>
                <i className={classNames('icon mbc-icon training')} />
                <span>Trainings <em>coming soon</em></span>
              </Link>
              <p>Data and Tools are not enough - here we enable you to become even more productive</p>
            </div>
          </div>
        </div>
    }
    {
        context.showTermsModal &&
          <Modal
            title=""
            show={context.showTermsModal}
            showAcceptButton={false}
            showCancelButton={false}
            content={infoModalContent}
            buttonAlignment="center"
            onCancel={() => setShowTermsModal(false)}
            modalStyle={{
              padding: '50px 90px 35px',
              minWidth: 'unset',
              width: '65%',
              maxWidth: '65%'
            }}
            hideCloseButton={!userPreferences?.termsOfUse}
          />
      }
      { props.showMenuCloseIcon && <div className={Styles.backdrop} onClick={props.onHamburgerIconClick}>&nbsp;</div> }
    </>
  );
}
export default Header;
