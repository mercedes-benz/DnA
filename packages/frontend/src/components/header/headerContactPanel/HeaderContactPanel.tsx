import cn from 'classnames';
import * as React from 'react';
import { USER_ROLE } from '../../../globals/constants';
// import { getTranslatedLabel } from '../../../globals/i18n/TranslationsProvider';
import { IRole, IUserInfo } from '../../../globals/types';
import { history } from '../../../router/History';
import Styles from './HeaderContactPanel.scss';

const classNames = cn.bind(Styles);

export interface IHeaderContactPanelProps {
  show?: boolean;
  onClose?: () => void;
  user: IUserInfo;
  toggleContactPanelCallBack: () => void;
}
export interface IHeaderContactPanelState {
  isAdmin: boolean;
}

export class HeaderContactPanel extends React.Component<IHeaderContactPanelProps, IHeaderContactPanelState> {
  protected isTouch = false;
  protected domContainer: HTMLDivElement;

  public constructor(props: IHeaderContactPanelProps, context?: any) {
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
          <li onClick={this.navigateToMyContactUs}>
            {/* {getTranslatedLabel('Contact Us')} */}
            Contact Us
          </li>
          <li onClick={this.navigateToMyLicences}>
            {/* {getTranslatedLabel('Licences')} */}
            Licences
          </li>
        </ul>
      </div>
    );
  }

  protected navigateToMyContactUs = (event: React.MouseEvent<HTMLElement>) => {
    this.props.toggleContactPanelCallBack();
  };
  protected navigateToMyLicences = (event: React.MouseEvent<HTMLElement>) => {
    history.push(`/license`);
    // this.props.onClose();
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
