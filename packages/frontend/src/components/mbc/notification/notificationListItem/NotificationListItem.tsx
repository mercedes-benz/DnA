import cn from 'classnames';
import React, { useEffect, useState } from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { ISubsriptionAdminList } from '../../../../globals/types';
// import { history } from '../../../../router/History';
import Styles from '../Notifications.scss';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
// import { getDateFromTimestamp, getDateDifferenceFromTodayUsingGetDate } from '../../../../services/utils';
const classNames = cn.bind(Styles);

export interface INotificationDetailsProps {
  item: ISubsriptionAdminList;
  checkedAll?: boolean;
  checkAllWithException?: boolean;
  currentItemNotToBeDeleted?: boolean;
  checkedAllCount: number;
  notificationIdsNotTobeDeleted?: any[];
  selectedNotifications?: any[];
  openDetails?: (notificationDetails: any) => void;
  selectNotification?: (selectedNotification: any) => void;
  deselectNotification?: (selectedNotification: any) => void;
  unCheckAll?: () => void;
}

const NotificationListItem = (props: INotificationDetailsProps) => {
  Tooltip.defaultSetup();
  const item = props.item;

  const [showCheckBox, setShowCheckBox] = useState<boolean>(false);
  const [isChecked, setIsChecked] = useState<boolean>(false);
  const [isAvaialableInWithExceptionArray, setIsAvaialableInWithExceptionArray] = useState<boolean>(false);

  useEffect(() => {
    Tooltip.defaultSetup();
    setIsAvaialableInWithExceptionArray(props.checkAllWithException);
    if (props.checkedAllCount > 0 && !props.checkedAll) {
      setIsChecked(false);
    }
  }, [props, isChecked]);

  const onChangeCheck = (event: React.FormEvent<HTMLInputElement>) => {
    if (!event.currentTarget.checked) {
      props.unCheckAll();
      props.deselectNotification(event.currentTarget.id);
    } else {
      props.selectNotification(event.currentTarget.id);
    }
    setIsChecked(event.currentTarget.checked);
    event.nativeEvent.stopImmediatePropagation();
  };

  const onRowClick = (event: React.FormEvent<HTMLSpanElement>) => {
    props.openDetails({});
  };

  const stopPropagation = (event: React.FormEvent<HTMLElement>) => {
    event.stopPropagation();
  };

  const markAsRead = (event: React.FormEvent<HTMLElement>) => {
    event.stopPropagation();
  };

  return (
    <React.Fragment>
      <tr
        className="data-row"
        key={item.id}
        onClick={onRowClick}
        onMouseEnter={() => setShowCheckBox(true)}
        onMouseLeave={() => setShowCheckBox(false)}
      >
        <td>
          {showCheckBox ||
          props.checkedAll ||
          isChecked ||
          (isAvaialableInWithExceptionArray && !props.currentItemNotToBeDeleted) ? (
            <label
              className={classNames('checkbox', Styles.checkboxItem)}
              onClick={(event: React.FormEvent<HTMLElement>) => stopPropagation(event)}
            >
              <span className="wrapper">
                <input
                  type="checkbox"
                  id={'checkbox-' + item.id}
                  checked={
                    props.checkedAll ||
                    isChecked ||
                    (isAvaialableInWithExceptionArray && !props.currentItemNotToBeDeleted)
                  }
                  onChange={(event: React.FormEvent<HTMLInputElement>) => {
                    onChangeCheck(event);
                  }}
                />
              </span>
              <span className={classNames('label', Styles.checkboxItemLabel)} onClick={onRowClick}>
                3 days left to provision your dummy solution{' '}
              </span>
            </label>
          ) : (
            <p>3 days left to provision your dummy solution </p>
          )}
        </td>
        <td className={classNames(Styles.notificationCategory, showCheckBox ? Styles.showMarkAsRead : '')}>
          <div className={Styles.elementToMove}>System Notification</div>
        </td>
        <td className={classNames(Styles.notificationDate, showCheckBox ? Styles.showMarkAsRead : '')}>
          <div className={Styles.elementToMove}>20-9-2021 / 15:21</div>
          {showCheckBox ? (
            <div className={Styles.markAsRead} onClick={(event: React.FormEvent<HTMLElement>) => markAsRead(event)}>
              <i className={'icon mbc-icon visibility-show'} />
              &nbsp; Mark as read
            </div>
          ) : (
            ''
          )}
        </td>
      </tr>
    </React.Fragment>
  );
};

export default NotificationListItem;
