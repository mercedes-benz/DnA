import classNames from 'classnames';
import React, { useEffect } from 'react';
import { Link, useHistory } from 'react-router-dom';
// @ts-ignore
import Navigation from './../../assets/modules/uilab/js/src/navigation';
import { getTranslatedLabel } from 'globals/i18n/TranslationsProvider';
import { USER_ROLE } from 'globals/constants';
import { getPath } from './../../router/RouterUtils';
import Styles from './MainNavigation.scss';
import { Envs } from 'globals/Envs';

export interface IMainNavigationProps {
  showExpandEffect: boolean;
  isMaximized: boolean;
  onNavOpen: () => void;
  onNavClose: () => void;
}

const UserAndAdminRole = [USER_ROLE.USER, USER_ROLE.EXTENDED, USER_ROLE.ADMIN];

const MainNavigation: React.FC<IMainNavigationProps> = (props) => {
  let isTouch = false;
  let mainNavContainer: HTMLDivElement;

  const history = useHistory();

  useEffect(() => {
    document.addEventListener('touchend', handleMainMenuClickOutside, true);
    document.addEventListener('click', handleMainMenuClickOutside, true);

    return () => {
      document.removeEventListener('touchend', handleMainMenuClickOutside, true);
      document.removeEventListener('click', handleMainMenuClickOutside, true);
    };
  }, []);

  useEffect(() => {
    const navElement = document.getElementById('main-nav');
    if (navElement) {
      // tslint:disable-next-line: no-unused-expression
      new Navigation(navElement);

      // Code for making uilab navigation work as expected in Data@MBC
      navElement.addEventListener('click', (e: Event) => {
        const elem = e.target as HTMLElement;
        const isIconElem = elem.classList.contains('icon');
        const mainSubNavElem = navElement.querySelector('.has-sub-nav');
        if (elem.classList.contains('nav-link') || isIconElem) {
          const linkElem = elem.classList.contains('icon') ? elem.parentElement : elem;
          const liElem = linkElem.parentElement;
          if (liElem.classList.contains('has-sub-nav')) {
            if (isIconElem) {
              if (props.isMaximized) {
                if (linkElem.classList.contains('opened')) {
                  liElem.classList.add('opened');
                } else {
                  liElem.classList.remove('opened');
                }
              } else {
                liElem.classList.add('opened');
              }
            }
            props.onNavOpen();
          } else {
            liElem.classList.add('active');
          }

          if (mainSubNavElem.querySelector('.sub-nav-list').contains(liElem)) {
            mainSubNavElem.classList.add('active');
          }
        }
      });
    }
  }, []);

  // useEffect(() => {
  //   // set height of the active side nav item
  //   const activeNavItem = document?.querySelectorAll('.nav-item.has-sub-nav.active.opened')?.[0];
  //   props.isMaximized && activeNavItem?.setAttribute('style', `height: auto !important`);
  // }, [props.isMaximized]);

  const navItems: any = [
    {
      allowedRoles: UserAndAdminRole,
      id: 0,
      route: `/home`,
      title: 'Home',
      icon: 'home',
      enabled: true,
    },
    {
      id: 1,
      route: `/transparency`,
      title: 'Transparency',
      icon: 'transparency',
      enabled: true,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 0,
          route: `/transparency`,
          title: 'Overview',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/portfolio`,
          title: 'Portfolio',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/allsolutions`,
          title: 'Solutions',
          enabled: true,
        },
        // {
        //   allowedRoles: UserAndAdminRole,
        //   id: 3,
        //   route: `/createnewsolution`,
        //   title: 'CreateSolution',
        //   enabled: true,
        // },
        {
          allowedRoles: UserAndAdminRole,
          id: 4,
          route: `/allreports`,
          title: 'Reports',
          enabled: Envs.ENABLE_REPORTS,
        },
        // {
        //   allowedRoles: UserAndAdminRole,
        //   id: 5,
        //   route: `/createnewreport`,
        //   title: 'CreateReport',
        //   enabled: Envs.ENABLE_REPORTS,
        // },
        {
          allowedRoles: UserAndAdminRole,
          id: 6,
          route: `/data/datasharing`,
          title: 'DataSharingMI',
          enabled: true,
        },
      ],
    },
    {
      id: 2,
      route: `/carla`,
      title: 'CarLA',
      icon: 'carla-mini',
      enabled: true,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 0,
          route: `/carla`,
          title: 'Overview',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: Envs.CARLA_ARCHITECTURE_URL,
          title: 'Architecture',
          enabled: true,
          isExternalLink: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/viewsolutions/tag/CarLA`,
          title: 'CarLASolutions',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 3,
          route: `/data/dataproducts`,
          title: 'CarLAData',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 4,
          route: Envs.TRANSACTIONAL_DATA_URL,
          title: 'TransactionalData',
          enabled: true,
          isExternalLink: true,
        },
      ],
    },
    {
      id: 3,
      route: `/data`,
      title: 'Data',
      icon: 'data',
      enabled: true,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 0,
          route: `/data`,
          title: 'Overview',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/data/dataproducts`,
          title: 'DataProducts',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/data/datalayer`,
          title: 'DataLayer',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 3,
          route: `/data/datagovernance`,
          title: 'DataGovernance',
          enabled: true,
        },
      ],
    },
    {
      id: 4,
      route: `/tools`,
      title: 'Tools',
      icon: 'tools',
      enabled: true,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/tools`,
          title: 'ExploreTools',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/tools/lowcode`,
          title: 'LowNoCodeTools',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 3,
          route: `/tools/prodev`,
          title: 'ToolsforProDeveloper',
          enabled: true,
        },
      ],
    },
    {
      id: 5,
      route: `/trainings`,
      title: 'Trainings',
      icon: 'trainings',
      enabled: Envs.ENABLE_TRAININGS,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/trainings`,
          title: 'ExploreTrainings',
          enabled: Envs.ENABLE_TRAININGS,
          isExternalLink: false,
        },
        // {
        //   allowedRoles: UserAndAdminRole,
        //   id: 2,
        //   route: `/knowledge-base`,
        //   title: 'KnowledgeBase',
        //   enabled: Envs.ENABLE_TRAININGS,
        //   isExternalLink: false,
        // },
        // {
        //   allowedRoles: UserAndAdminRole,
        //   id: 3,
        //   route: Envs.UDEMY_URL,
        //   title: 'Udemy',
        //   enabled: Envs.ENABLE_TRAININGS,
        //   isExternalLink: true,
        // },
      ],
    },
  ];

  const setReferenceMainNav = (element: HTMLDivElement) => {
    mainNavContainer = element;
  };

  const subNavItemSelected = (subNavItems: any) => {
    let oneOfChildNavSelected = false;
    subNavItems.forEach((subNavItem: any) => {
      if (!oneOfChildNavSelected) {
        oneOfChildNavSelected = getPath().includes(subNavItem.route);
      }
    });
    return oneOfChildNavSelected;
  };

  const handleMainMenuClickOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch) {
      return;
    }

    const target = event.target as Element;

    if (mainNavContainer && mainNavContainer.contains(target) === false) {
      props.onNavClose();
    }
  };

  return (
    <nav
      id="main-nav"
      className={classNames(
        Styles.mainNavigation,
        'navigation',
        'side-nav',
        { maximized: props.isMaximized },
        { expandEffect: props.showExpandEffect },
      )}
      ref={setReferenceMainNav}
    >
      {/* <ul className="nav-list">
        {navItems.map((navItem, index) => {
          return (
            <li
              key={index}
              className={classNames('nav-item', { active: getPath().includes(navItem.route) })}
              title={navItem.title}
            >
              <Link className="nav-link" to={navItem.route}>
                <i className={classNames('icon', 'mbc-icon', navItem.icon)} />
                {getTranslatedLabel(navItem.title)}
              </Link>
            </li>
          );
        })}
      </ul> */}
      <ul className="nav-list mbc-scroll sub">
        {navItems.map((navItem: any, index: number) => {
          return navItem.subNavItems ? (
            <li
              key={index}
              className={classNames('nav-item', 'has-sub-nav', {
                active: subNavItemSelected(navItem.subNavItems),
                opened: subNavItemSelected(navItem.subNavItems),
              })}
              title={navItem.title}
            >
              <a className={classNames('nav-link', navItem.enabled ? '' : Styles.disableLink, Styles.navLink)}>
                <i
                  className={classNames(
                    'icon',
                    'mbc-icon',
                    navItem.icon,
                    Styles.navIcon,
                    getPath().includes(navItem.route) ? Styles.navActive : '',
                  )}
                  onClick={() => {
                    props.onNavClose();
                    history.push(navItem.route);
                  }}
                />
                {getTranslatedLabel(navItem.title)}
              </a>
              <ul className="sub-nav-list">
                {navItem.subNavItems.map((subNavItem: any, subIndex: number) => {
                  return (
                    <li
                      key={`${index}${subIndex}`}
                      className={classNames('nav-item sub-nav-item', {
                        active: window.location.hash.split('?')[0] === ('#' + subNavItem.route),
                      })}
                      onClick={() => {
                        props.onNavClose();
                      }}
                    >
                      { subNavItem.isExternalLink ?
                      
                        <a href={subNavItem.route} target="_blank" rel="noopener noreferrer" className={classNames('nav-link', subNavItem.enabled ? '' : Styles.disableSubLink)} >
                          {getTranslatedLabel(subNavItem.title)}
                        </a>
                        :
                        <Link
                          className={classNames('nav-link', subNavItem.enabled ? '' : Styles.disableSubLink)}
                          to={subNavItem.route}
                        >
                          {getTranslatedLabel(subNavItem.title)}
                        </Link>
                      }
                    </li>
                  );
                })}
              </ul>
            </li>
          ) : (
            <li
              key={index}
              className={classNames('nav-item', { active: getPath().includes(navItem.route) })}
              title={navItem.title}
              onClick={() => {
                props.onNavClose();
              }}
            >
              <Link
                className={classNames('nav-link', navItem.enabled ? '' : Styles.disableLink, Styles.navLink)}
                to={{
                  pathname: navItem.route,
                }}
              >
                <i
                  className={classNames(
                    'icon',
                    'mbc-icon',
                    navItem.icon,
                    Styles.navIcon,
                    getPath().includes(navItem.route) ? Styles.navActive : '',
                  )}
                  onClick={() => {
                    props.onNavClose();
                    history.push(navItem.route);
                  }}
                />
                {getTranslatedLabel(navItem.title)}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
};

export default MainNavigation;
