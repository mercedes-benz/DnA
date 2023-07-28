import React, { useEffect, useState } from 'react';
import { ApiClient } from '../../../../services/ApiClient';
import { IUserInfo } from 'globals/types';
import { INoticationModules } from 'globals/types';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import Styles from './notificationsSettings.scss';
import cn from 'classnames';
const classNames = cn.bind(Styles);

export interface INotificationSettings {
  user: IUserInfo;
}

const NotificationsSettings = (props: INotificationSettings) => {
  const [notificationPreferences, setNotificationPreferences] = useState<INoticationModules>();

  const [tempNotificationPreferences, setTempNotificationPreferences] = useState<any>();

  useEffect(() => {
    ProgressIndicator.show();
    ApiClient.getNotificationPreferences(props?.user?.id).then((res) => {
      if (res) {
        setNotificationPreferences(res);
        migrateResponseToLocalObject(res);
      }
      ProgressIndicator.hide();
    });
  }, []);
  useEffect(() => {}, [tempNotificationPreferences]);

  /*********************************************************************************************************
   *********    Following function is converting response object in array object for     *******************
   *********    displaying module automatically in screen                                *******************
   *********************************************************************************************************/

  const migrateResponseToLocalObject = (res: any) => {
    const tempArr = [];
    for (const x in res) {
      if (typeof res[x] === 'object' && res[x] !== null) {
        const temp = { module: '', title: '', enableAppNotifications: true, enableEmailNotifications: false };
        temp.module = x;
        temp.enableAppNotifications = res[x].enableAppNotifications ? res[x].enableAppNotifications : true;
        temp.enableEmailNotifications = res[x].enableEmailNotifications ? res[x].enableEmailNotifications : false;
        switch (x) {
          case 'solutionNotificationPref':
            temp.title = 'Configure Notifications for Solutions';
            break;
          case 'notebookNotificationPref':
            temp.title = 'Configure Notifications for Notebooks';
            break;
          case 'persistenceNotificationPref':
            temp.title = 'Configure Notifications for Storage';
            break;
          case 'dashboardNotificationPref':
            temp.title = 'Configure Notifications for Reports';
            break;
          case 'dataProductNotificationPref':
            temp.title = 'Configure Notification for Data Transfer';
            break;
          case 'dataComplianceNotificationPref':
            temp.title = 'Configure Notification for Data Compliance';
            break;
          case 'chronosNotificationPref':
            temp.title = 'Configure Notification for Chronos Forecasting';
            break;
          case 'codespaceNotificationPref':
            temp.title = 'Configure Notification for Code Spaces';
            break;
        }
        tempArr.push(temp);
      }
    }
    setTempNotificationPreferences(tempArr);
  };

  const onChangeEmailNotificationForSolution = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.solutionNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const onChangeEmailNotificationForNotebook = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.notebookNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const onChangeEmailNotificationForDashboard = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.dashboardNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const onChangeEmailNotificationForPersistence = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.persistenceNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const onChangeEmailNotificationForDataProduct = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.dataProductNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const onChangeEmailNotificationForDataCompliance = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.dataComplianceNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const onChangeEmailNotificationForChronos = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.chronosNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const onChangeEmailNotificationForCodeSpace = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = notificationPreferences?.codespaceNotificationPref;
    target['enableEmailNotifications'] = e.target.checked;
    setNotificationPreferences(notificationPreferences);
    const messageForNotification = e.target.checked
      ? 'Enabled Email Notification Successfully'
      : 'Disabled Email Notification Successfully';
    callToUpdatePreference(messageForNotification);
  };

  const callToUpdatePreference = (message: string) => {
    ProgressIndicator.show();
    ApiClient.enableEmailNotifications(notificationPreferences)
      .then((res) => {
        migrateResponseToLocalObject(res);
        Notification.show(message);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        Notification.show('Something went wrong', 'alert');
        ProgressIndicator.hide();
      });
  };

  /*********************************************************************************************************
   *********    Following function is deciding which function to call for update     ***********************
   *********************************************************************************************************/

  const callToCommonFunctionOnChange = (moduleName: string, e: React.ChangeEvent<HTMLInputElement>) => {
    switch (moduleName) {
      case 'solutionNotificationPref':
        onChangeEmailNotificationForSolution(e);
        break;
      case 'notebookNotificationPref':
        onChangeEmailNotificationForNotebook(e);
        break;
      case 'persistenceNotificationPref':
        onChangeEmailNotificationForPersistence(e);
        break;
      case 'dashboardNotificationPref':
        onChangeEmailNotificationForDashboard(e);
        break;
      case 'dataProductNotificationPref':
        onChangeEmailNotificationForDataProduct(e);
        break;
      case 'dataComplianceNotificationPref':
        onChangeEmailNotificationForDataCompliance(e);
        break;
      case 'chronosNotificationPref':
        onChangeEmailNotificationForChronos(e);
        break;   
      case 'codespaceNotificationPref':
        onChangeEmailNotificationForCodeSpace(e);
        break;   
    }
  };

  return (
    <React.Fragment>
      <div className={Styles.wrapper}>
        <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
          {tempNotificationPreferences?.map((item: any, index: number) => {
            return (
              <div id={'optionList-' + index} key={index} className={Styles.moduleWrapper}>
                <label className="input-label summary">{item.title}</label>
                <br />
                <div className={Styles.optionsList}>
                  <label className="checkbox">
                    <span className="wrapper">
                      <input type="checkbox" className="ff-only" checked={item.enableAppNotifications} disabled />
                    </span>
                    <span className="label">App Notification</span>
                  </label>
                </div>
                <div className={Styles.optionsList}>
                  <label className="checkbox">
                    <span className="wrapper">
                      <input
                        type="checkbox"
                        className="ff-only"
                        checked={item.enableEmailNotifications}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                          callToCommonFunctionOnChange(item.module, e)
                        }
                      />
                    </span>
                    <span className="label">Email</span>
                  </label>
                </div>
              </div>
            );
          })}
          <div></div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default NotificationsSettings;
