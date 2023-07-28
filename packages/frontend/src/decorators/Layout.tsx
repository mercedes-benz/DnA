import * as React from 'react';
// import Home from 'components/mbc/home/Home';
import MainNavigation from 'components/mainNavigation/MainNavigation';
import Footer from 'components/mbc/footer/Footer';
import { LOCAL_STORAGE_KEYS } from 'globals/constants';
import { IUserInfo } from 'globals/types';
import Header from 'components/header/Header';
// import SessionAlert from 'components/sessionAlert/SessionAlert';

export const AppContext = React.createContext({
  navMaximized: false,
});

export interface ILayoutProps {
  user: IUserInfo;
  children?: any;
}

export interface ILayoutState {
  appNavMaximized: boolean;
  enableNavExpandEfect: boolean;
  showDisclaimer: boolean;
  isHome: boolean;
  isNotebook: boolean;
}

export class Layout extends React.Component<ILayoutProps, ILayoutState> {
  public constructor(props: ILayoutProps, context?: any) {
    super(props, context);
    this.state = {
      appNavMaximized: false,
      enableNavExpandEfect: false,
      showDisclaimer: true,
      isHome: false,
      isNotebook: false,
    };
  }

  public componentDidMount() {
    const valueInLocalStore = localStorage.getItem(LOCAL_STORAGE_KEYS.SHOW_DISCLAIMER_STATUS);
    const showDisclaimer = valueInLocalStore != null ? (JSON.parse(valueInLocalStore) as boolean) : true;
    this.setState({
      showDisclaimer,
    });
    if (window.location.hash === '#/home') {
      this.setState({
        isHome: true,
      });
    }
    if (window.location.hash === '#/notebook') {
      this.setState({
        isNotebook: true,
      });
    }
  }

  public render() {
    const { showDisclaimer } = this.state;
    return (
      <React.Fragment>
        {showDisclaimer ? (
          <div className="disclaimer">
            <p className="message">For the best experience try the latest version of Edge, Chrome or Firefox.</p>
            <div className="close-btn">
              <button className="icon-btn" onClick={this.closeDisclaimer}>
                <i className="icon mbc-icon close thin" />
              </button>
            </div>
          </div>
        ) : (
          ''
        )}
        <div className={`container ${showDisclaimer ? 'withDisclaimer' : ''}`}>
          <Header
            user={this.props.user}
            onHamburgerIconMouseHover={this.toggleNavExpandEfect}
            onHamburgerIconClick={this.onNavMaximized}
            showMenuCloseIcon={this.state.appNavMaximized}
            isHome={this.state.isHome}
          />
          {/* <SessionAlert /> */}
          {/* {this.state.isHome ? (
            <Home user={this.props.user} />
          ) : ( */}
            <main id="mainContainer" className="mainContainer">
              <aside>
                <MainNavigation
                  showExpandEffect={this.state.enableNavExpandEfect}
                  isMaximized={this.state.appNavMaximized}
                  onNavOpen={this.onNavOpen}
                  onNavClose={this.onNavClose}
                />
              </aside>
              <section>{this.props.children}</section>
              <Footer isHome={this.state.isHome} isNotebook={this.state.isNotebook} />
            </main>
          {/* )} */}
        </div>
      </React.Fragment>
    );
  }

  protected closeDisclaimer = () => {
    localStorage.setItem(LOCAL_STORAGE_KEYS.SHOW_DISCLAIMER_STATUS, 'false');
    this.setState({
      showDisclaimer: false,
    });
  };

  protected onNavMaximized = () => {
    if (this.state.appNavMaximized) {
      this.setState({ appNavMaximized: false });
    } else {
      this.setState({ appNavMaximized: true });
    }
  };

  protected toggleNavExpandEfect = () => {
    if (this.state.enableNavExpandEfect) {
      this.setState({ enableNavExpandEfect: false });
    } else {
      this.setState({ enableNavExpandEfect: true });
    }
  };

  protected onNavOpen = () => {
    this.setState({ appNavMaximized: true, enableNavExpandEfect: true });
  };

  protected onNavClose = () => {
    this.setState({ appNavMaximized: false, enableNavExpandEfect: false });
  };
}
