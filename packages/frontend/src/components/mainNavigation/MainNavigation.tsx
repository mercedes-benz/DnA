import classNames from 'classnames';
import React, {useEffect} from 'react';
import { Link } from 'react-router-dom';
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

const MainNavigation:React.FC<IMainNavigationProps> = (props) => {
  let isTouch = false;
  let mainNavContainer: HTMLDivElement;

  // const [showNavigation, setShowNavigation] = useState<boolean>(false);
  // const [showUserPanel, setShowUserPanel] = useState<boolean>(false);

  useEffect(() => {
    document.addEventListener('touchend', handleMainMenuClickOutside, true);
    document.addEventListener('click', handleMainMenuClickOutside, true);
  
    return () => {
      document.removeEventListener('touchend', handleMainMenuClickOutside, true);
      document.removeEventListener('click', handleMainMenuClickOutside, true);
    }
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

  useEffect(() => {
    // set height of the active side nav item
    const activeNavItem = document?.querySelectorAll('.nav-item.has-sub-nav.active.opened')?.[0];
    props.isMaximized && activeNavItem?.setAttribute('style', `height: auto !important`);
  }, [props.isMaximized]);

  // const reportNav = {
  //   id: 3,
  //   title: 'Reports',
  //   icon: 'reports',
  //   subNavItems: [
  //     {
  //       allowedRoles: UserAndAdminRole,
  //       id: 1,
  //       route: `/createnewreport`,
  //       title: 'CreateNewReport',
  //     },
  //     {
  //       allowedRoles: UserAndAdminRole,
  //       id: 2,
  //       route: `/allreports`,
  //       title: 'AllReports',
  //     },
  //   ],
  // };
  const navItems = [
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
      title: 'Transparency',
      icon: 'reports',
      enabled: true,
      subNavItems: [
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
          title: 'ExploreSolutions',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 3,
          route: `/createnewsolution`,
          title: 'CreateSolution',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 4,
          route: `/allreports`,
          title: 'ExploreReports',
          enabled: Envs.ENABLE_REPORTS,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 5,
          route: `/createnewreport`,
          title: 'CreateReport',
          enabled: Envs.ENABLE_REPORTS,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 6,
          route: `/createnewreport`,
          title: 'DataSharingMI',
          enabled: true,
        },
      ],
    },
    {
      id: 2,
      title: 'Data',
      icon: 'solutions',
      enabled: true,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/data-products`,
          title: 'DataProducts',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/data-layer`,
          title: 'DataLayer',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 3,
          route: `/data-governance`,
          title: 'DataGovernance',
          enabled: true,
        },
      ],
    },
    {
      id: 3,
      title: 'Tools',
      icon: 'dashboard',
      enabled: true,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/explore-tools`,
          title: 'ExploreTools',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/low-no-code-tools`,
          title: 'LowNoCodeTools',
          enabled: true,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 3,
          route: `/tools-for-pro-developer`,
          title: 'ToolsforProDeveloper',
          enabled: true,
        },
      ],
    },
    {
      id: 4,
      title: 'Trainings',
      icon: 'training',
      enabled: Envs.ENABLE_TRAININGS,
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/explore-trainings`,
          title: 'ExploreTrainings',
          enabled: Envs.ENABLE_TRAININGS,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/knowledge-base`,
          title: 'KnowledgeBase',
          enabled: Envs.ENABLE_TRAININGS,
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 3,
          route: `/udemy`,
          title: 'Udemy',
          enabled: Envs.ENABLE_TRAININGS,
        },
      ],
    },
    // {
    //   id: 2,
    //   title: 'Solutions',
    //   icon: 'solutions',
    //   subNavItems: [
    //     {
    //       allowedRoles: UserAndAdminRole,
    //       id: 1,
    //       route: `/createnewsolution`,
    //       title: 'CreateNewSolution',
    //     },
    //     {
    //       allowedRoles: UserAndAdminRole,
    //       id: 2,
    //       route: `/allsolutions`,
    //       title: 'AllSolutions',
    //     },
    //   ],
    // },
    // {
    //   id: 4,
    //   title: 'MyWorkspace',
    //   icon: 'workspace',
    //   subNavItems: [
    //     {
    //       allowedRoles: UserAndAdminRole,
    //       id: 1,
    //       route: `/workspaces`,
    //       title: 'Workspaces',
    //     },
    //     {
    //       allowedRoles: UserAndAdminRole,
    //       id: 2,
    //       route: `/services`,
    //       title: 'Services',
    //     },
    //   ],
    // },
  ];

  // if (Envs.ENABLE_REPORTS) {
  //   navItems.splice(3, 0, reportNav);
  // }

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

  // const closeUserPanel = () => {
  //   setShowUserPanel(false);
  // };

  // const toggleUserPanel = () => {
  //   setShowUserPanel(!showUserPanel);
  // };

  // const toggleNavigation = () => {
  //   setShowNavigation(!showNavigation);
  // };

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
        {navItems.map((navItem, index) => {
          return navItem.subNavItems ? (
            <li
              key={index}
              className={classNames('nav-item', 'has-sub-nav', {
                active: subNavItemSelected(navItem.subNavItems),
                opened: subNavItemSelected(navItem.subNavItems),
              })}
              title={navItem.title}
            >
              <a className={classNames('nav-link', navItem.enabled ? '' : Styles.disableLink)}>
                <i className={classNames('icon', 'mbc-icon', navItem.icon)} />
                {getTranslatedLabel(navItem.title)}
              </a>
              <ul className="sub-nav-list">
                {navItem.subNavItems.map((subNavItem, subIndex) => {
                  return (
                    <li
                      key={`${index}${subIndex}`}
                      className={classNames('nav-item sub-nav-item', {
                        active: getPath().includes(subNavItem.route),
                      })}
                    >
                      <Link className={classNames('nav-link', subNavItem.enabled ? '' : Styles.disableSubLink)} to={subNavItem.route}>
                        {getTranslatedLabel(subNavItem.title)}
                      </Link>
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
            >
              <Link
                className={classNames('nav-link', navItem.enabled ? '' : Styles.disableLink)}
                to={{
                  pathname: navItem.route,
                }}
              >
                <i className={classNames('icon', 'mbc-icon', navItem.icon)} />
                {getTranslatedLabel(navItem.title)}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}

export default MainNavigation;
