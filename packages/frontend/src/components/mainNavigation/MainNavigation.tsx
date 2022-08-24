import classNames from 'classnames';
import * as React from 'react';
import { Link } from 'react-router-dom';
// @ts-ignore
import Navigation from './../../assets/modules/uilab/js/src/navigation';
import { getTranslatedLabel } from '../../globals/i18n/TranslationsProvider';
import { USER_ROLE } from './../../globals/constants';
import { getPath } from './../../router/RouterUtils';
import Styles from './MainNavigation.scss';
import { Envs } from './../../globals/Envs';

export interface IMainNavigationProps {
  showExpandEffect: boolean;
  isMaximized: boolean;
  onNavOpen: () => void;
  onNavClose: () => void;
}

export interface IMainNavigationState {
  showNavigation: boolean;
  showUserPanel: boolean;
}

const UserAndAdminRole = [USER_ROLE.USER, USER_ROLE.EXTENDED, USER_ROLE.ADMIN];

export default class MainNavigation extends React.Component<IMainNavigationProps, IMainNavigationState> {
  protected isTouch = false;
  protected mainNavContainer: HTMLDivElement;

  public constructor(props: IMainNavigationProps, context?: any) {
    super(props, context);
    this.state = {
      showNavigation: false,
      showUserPanel: false,
    };
  }

  public componentWillMount() {
    document.addEventListener('touchend', this.handleMainMenuClickOutside, true);
    document.addEventListener('click', this.handleMainMenuClickOutside, true);
  }

  public componentWillUnmount() {
    document.removeEventListener('touchend', this.handleMainMenuClickOutside, true);
    document.removeEventListener('click', this.handleMainMenuClickOutside, true);
  }

  public componentDidMount() {
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
              if (this.props.isMaximized) {
                if (linkElem.classList.contains('opened')) {
                  liElem.classList.add('opened');
                } else {
                  liElem.classList.remove('opened');
                }
              } else {
                liElem.classList.add('opened');
              }
            }
            this.props.onNavOpen();
          } else {
            liElem.classList.add('active');
          }

          if (mainSubNavElem.querySelector('.sub-nav-list').contains(liElem)) {
            mainSubNavElem.classList.add('active');
          }
        }
      });
    }
  }

  public componentDidUpdate(
    prevProps: Readonly<IMainNavigationProps>,
    prevState: Readonly<IMainNavigationState>,
    snapshot?: any,
  ): void {
    // set height of the active side nav item
    const activeNavItem = document?.querySelectorAll('.nav-item.has-sub-nav.active.opened')?.[0];
    this.props.isMaximized && activeNavItem?.setAttribute('style', 'height: 134px !important');
  }

  public render() {
    const reportNav = {
      id: 3,
      title: 'Reports',
      icon: 'reports',
      subNavItems: [
        {
          allowedRoles: UserAndAdminRole,
          id: 1,
          route: `/createnewreport`,
          title: 'CreateNewReport',
        },
        {
          allowedRoles: UserAndAdminRole,
          id: 2,
          route: `/allreports`,
          title: 'AllReports',
        },
      ],
    };
    const navItems = [
      {
        allowedRoles: UserAndAdminRole,
        id: 0,
        route: `/home`,
        title: 'Home',
        icon: 'home',
      },
      {
        allowedRoles: UserAndAdminRole,
        id: 1,
        route: `/portfolio`,
        title: 'Portfolio',
        icon: 'dashboard',
      },
      {
        id: 2,
        title: 'Solutions',
        icon: 'solutions',
        subNavItems: [
          {
            allowedRoles: UserAndAdminRole,
            id: 1,
            route: `/createnewsolution`,
            title: 'CreateNewSolution',
          },
          {
            allowedRoles: UserAndAdminRole,
            id: 2,
            route: `/allsolutions`,
            title: 'AllSolutions',
          },
        ],
      },
      {
        id: 4,
        title: 'MyWorkspace',
        icon: 'workspace',
        subNavItems: [
          {
            allowedRoles: UserAndAdminRole,
            id: 1,
            route: `/workspaces`,
            title: 'Workspaces',
          },
          {
            allowedRoles: UserAndAdminRole,
            id: 2,
            route: `/services`,
            title: 'Services',
          },
        ],
      },
    ];

    if (Envs.ENABLE_REPORTS) {
      navItems.splice(3, 0, reportNav);
    }

    return (
      <nav
        id="main-nav"
        className={classNames(
          Styles.mainNavigation,
          'navigation',
          'side-nav',
          { maximized: this.props.isMaximized },
          { expandEffect: this.props.showExpandEffect },
        )}
        ref={this.setReferenceMainNav}
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
                  active: this.subNavItemSelected(navItem.subNavItems),
                  opened: this.subNavItemSelected(navItem.subNavItems),
                })}
                title={navItem.title}
              >
                <a className="nav-link">
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
                        <Link className="nav-link" to={subNavItem.route}>
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
                  className="nav-link"
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

  protected setReferenceMainNav = (element: HTMLDivElement) => {
    this.mainNavContainer = element;
  };

  protected subNavItemSelected = (subNavItems: any) => {
    let oneOfChildNavSelected = false;
    subNavItems.forEach((subNavItem: any) => {
      if (!oneOfChildNavSelected) {
        oneOfChildNavSelected = getPath().includes(subNavItem.route);
      }
    });
    return oneOfChildNavSelected;
  };

  protected closeUserPanel = () => {
    this.setState({ showUserPanel: false });
  };

  protected toggleUserPanel = () => {
    this.setState({ showUserPanel: !this.state.showUserPanel });
  };

  protected toggleNavigation = () => {
    this.setState({ showNavigation: !this.state.showNavigation });
  };

  protected handleMainMenuClickOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      this.isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && this.isTouch === true) {
      return;
    }

    const target = event.target as Element;

    if (this.mainNavContainer && this.mainNavContainer.contains(target) === false) {
      this.props.onNavClose();
    }
  };
}
