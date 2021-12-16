import cn from 'classnames';
import * as React from 'react';
import { history } from '../../../router/History';
import Styles from './NotificationPanel.scss';

const classNames = cn.bind(Styles);

export interface IHeaderNotificationPanelProps {
  show?: boolean;
  onClose?: () => void;
}

export class NotificationPanel extends React.Component<IHeaderNotificationPanelProps> {
  protected isTouch = false;
  protected domContainer: HTMLDivElement;

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
      <div
        className={classNames(this.props.show ? Styles.NotificationPanelContentesection : 'hide')}
        ref={this.connectContainer}
      >
        <div className={Styles.uparrow}> &nbsp; </div>
        <div className={Styles.NotificationContenWrraper}>
          <div className={Styles.NotificationPanelContent}>
            {/* <i className="icon mbc-icon home" /> */}
            <div className={Styles.notificationDetails}>
              <span>16.08.2021 / 16:33 </span>
              <label>
                2 days left to <b>provision</b> your dummy solution{' '}
              </label>
            </div>
            <div className={Styles.closeIcon}>
              <i className="icon mbc-icon close thin" />{' '}
            </div>
          </div>

          <div className={Styles.NotificationPanelContent}>
            {/* <i className="icon mbc-icon home" /> */}
            <div className={Styles.notificationDetails}>
              <span>16.08.2021 / 16:33 </span>
              <label>
                2 days left to <b>provision</b> your dummy solution{' '}
              </label>
            </div>
            <div className={Styles.closeIcon}>
              <i className="icon mbc-icon close thin" />{' '}
            </div>
          </div>

          <div className={Styles.NotificationPanelContent}>
            {/* <i className="icon mbc-icon home" /> */}
            <div className={Styles.notificationDetails}>
              <span>16.08.2021 / 16:33 </span>
              <label>
                2 days left to <b>provision</b> your dummy solution{' '}
              </label>
            </div>
            <div className={Styles.closeIcon}>
              <i className="icon mbc-icon close thin" />{' '}
            </div>
          </div>

          <div className={Styles.NotificationPanelContent}>
            {/* <i className="icon mbc-icon home" /> */}
            <div className={Styles.notificationDetails}>
              <span>16.08.2021 / 16:33 </span>
              <label>
                2 days left to <b>provision</b> your dummy solution{' '}
              </label>
            </div>
            <div className={Styles.closeIcon}>
              <i className="icon mbc-icon close thin" />{' '}
            </div>
          </div>
        </div>
        <div className={Styles.showAllNotificationLink}>
          <span onClick={this.goToSolution}>
            {' '}
            Show More <i className="icon mbc-icon arrow right small " />{' '}
          </span>
        </div>
      </div>
    );
  }

  protected goToSolution = () => {
    history.push('/notifications/');
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
      target.className.indexOf('iconContainer') === -1 &&
      this.domContainer.contains(target) === false
    ) {
      event.stopPropagation();
      this.props.onClose();
    }
  };
}
