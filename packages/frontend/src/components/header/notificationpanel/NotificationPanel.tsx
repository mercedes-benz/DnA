import cn from 'classnames';
import * as React from 'react';
import { markdownParser } from '../../../utils/MarkdownParser';
import { history } from '../../../router/History';
import Styles from './NotificationPanel.scss';
import { regionalDateAndTimeConversionSolution } from '../../../services/utils';

const classNames = cn.bind(Styles);

export interface IHeaderNotificationPanelProps {
  show?: boolean;
  userId: string;
  notifications?: any;
  onClose?: () => void;
}

export interface IHeaderNotificationPanelState {
  notifications: [];
}

export class NotificationPanel extends React.Component<IHeaderNotificationPanelProps, IHeaderNotificationPanelState> {
  protected isTouch = false;
  protected domContainer: HTMLDivElement;

  public constructor(props: IHeaderNotificationPanelProps) {
    super(props);
    this.state = {
      notifications: [],
    };
  }

  public static getDerivedStateFromProps(props: IHeaderNotificationPanelProps, state: IHeaderNotificationPanelState) {
    if (props.notifications !== state.notifications) {
      return { notifications: props.notifications };
    }
    return null;
  }

  // public componentWillMount() {
  //   document.addEventListener('touchend', this.handleUserPanelOutside, true);
  //   document.addEventListener('click', this.handleUserPanelOutside, true);
  // }

  public componentWillUnmount() {
    document.removeEventListener('touchend', this.handleUserPanelOutside, true);
    document.removeEventListener('click', this.handleUserPanelOutside, true);
  }

  public componentDidMount() {
    // NotificationApiClient.getNotifications(15, 0, this.props.userId).then((response) => {
    //   console.log(response, '====================');
    //   this.setState({notifications: response.records});
    // });
  }

  public render() {
    const { notifications } = this.state;
    return (
      <div
        className={classNames(this.props.show ? Styles.NotificationPanelContentesection : 'hide')}
        ref={this.connectContainer}
      >
        <div className={Styles.uparrow} />
        {notifications ? (
          notifications?.length > 0 ? (
            <div>
              <div className={classNames(Styles.NotificationContenWrraper, 'mbc-scroll')}>
                {notifications
                  ? notifications.map((item: any) => {
                      return (
                        <div key={item.id} className={Styles.NotificationPanelContent}>
                          {/* <i className="icon mbc-icon home" /> */}
                          <div className={Styles.notificationDetails}>
                            {/* <span>16.08.2021 / 16:33 </span> */}
                            <span>{regionalDateAndTimeConversionSolution(item.dateTime)} </span>
                            <label
                              dangerouslySetInnerHTML={{
                                __html: markdownParser(item.message),
                              }}
                            />
                          </div>
                          <div className={classNames(Styles.closeIcon, 'hide')}>
                            <i className="icon mbc-icon close thin" />{' '}
                          </div>
                        </div>
                      );
                    })
                  : ''}
              </div>
              <div className={Styles.showAllNotificationLink}>
                <span onClick={this.showMoreNotifications}>
                  {' '}
                  Show More <i className="icon mbc-icon arrow right small " />{' '}
                </span>
              </div>
            </div>
          ) : (
            <div>
              <div className={Styles.noData}>There are no unread notifications</div>
              <div className={Styles.showAllNotificationLink}>
                <span onClick={this.showMoreNotifications}>
                  {' '}
                  Show More <i className="icon mbc-icon arrow right small " />{' '}
                </span>
              </div>
            </div>
          )
        ) : (
          <div>
            <div className={Styles.noData}>There are no unread notifications</div>
            <div className={Styles.showAllNotificationLink}>
              <span onClick={this.showMoreNotifications}>
                {' '}
                Show More <i className="icon mbc-icon arrow right small " />{' '}
              </span>
            </div>
          </div>
        )}
      </div>
    );
  }

  protected showMoreNotifications = () => {
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
