import React, { useEffect, useState } from 'react';
import { ApiClient } from '../../../../services/ApiClient';
import { IUserInfo } from '../../../../globals/types';
import { INoticationModules } from '../../../../globals/types';
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

    const [enableSystemNotificationForSolutions, setEnableSystemNotificationForSolutions] = useState(true);
    const [enableEmailNotificationForSolutions, setEnableEmailNotificationForSolutions] = useState(false);

    const [enableSystemNotificationForNotebooks, setEnableSystemNotificationForNotebooks] = useState(true);
    const [enableEmailNotificationForNotebooks, setEnableEmailNotificationForNotebooks] = useState(false);
    
    const [notificationPreferences, setNotificationPreferences] = useState<INoticationModules>();
    
    

    useEffect(() => {
        ProgressIndicator.show();
        ApiClient.getNotificationPreferences(props?.user?.id)
        .then((res) => {
          if (res) {
            setNotificationPreferences(res);
          }
            ProgressIndicator.hide();
        }) 
    },[])
    useEffect(() => {    
        if(notificationPreferences){
            const solutionModules = notificationPreferences?.solutionNotificationPref;                          
            setEnableSystemNotificationForSolutions(solutionModules['enableAppNotifications']);                    
            setEnableEmailNotificationForSolutions(solutionModules['enableEmailNotifications']);
            
            const notebookModules = notificationPreferences?.notebookNotificationPref;            
            setEnableSystemNotificationForNotebooks(notebookModules['enableAppNotifications']);                    
            setEnableEmailNotificationForNotebooks(notebookModules['enableEmailNotifications']);
                    
        }     
    }, [notificationPreferences]);


    const onChangeEmailNotificationForSolution = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEnableEmailNotificationForSolutions(e.target.checked);
        const target = notificationPreferences?.solutionNotificationPref;
        target['enableEmailNotifications'] = e.target.checked;
        setNotificationPreferences(notificationPreferences);
        const messageForNotification = e.target.checked ? 'Enabled Email Notification Successfully' : 'Disabled Email Notification Successfully';
        callToUpdatePreference(messageForNotification);
    }

    const onChangeEmailNotificationForNotebook = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEnableEmailNotificationForNotebooks(e.target.checked);
        const target = notificationPreferences?.notebookNotificationPref;
        target['enableEmailNotifications'] = e.target.checked;
        setNotificationPreferences(notificationPreferences);
        const messageForNotification = e.target.checked ? 'Enabled Email Notification Successfully' : 'Disabled Email Notification Successfully';
        callToUpdatePreference(messageForNotification);
    }

    const callToUpdatePreference = (message: string) => {
        ProgressIndicator.show();
        ApiClient.enableEmailNotifications(notificationPreferences).then(
            res => {
                Notification.show(message);
                ProgressIndicator.hide();
            }
        ).catch(
            err => {
                Notification.show('Something went wrong', 'alert');
                ProgressIndicator.hide();
            }
        )
    }


    return (
        <React.Fragment>
            <div className={Styles.wrapper}>  
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                    <div id="optionList-1">
                        <label className="input-label summary">Configure Notifications for Solutions</label>
                        <br />
                        <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    checked={enableSystemNotificationForSolutions}
                                    disabled
                                    />
                                </span>
                                <span className="label">
                                    App Notification
                                </span>
                            </label>
                        </div>
                        <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    checked={enableEmailNotificationForSolutions}
                                    onChange={onChangeEmailNotificationForSolution}
                                    />
                                </span>
                                <span className="label">
                                    Email
                                </span>
                            </label>
                        </div>
                        {/* <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    // checked={this.state.datacompliance.quickCheck}
                                    // onChange={this.onChangeOfQuickCheck}
                                    />
                                </span>
                                <span className="label">
                                    Option 3
                                </span>
                            </label>
                        </div> */}
                    </div>
                    <div id="optionList-2">
                        <label className="input-label summary">Configure Notifications for Notebooks</label>
                        <br />
                        <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    checked={enableSystemNotificationForNotebooks}
                                    disabled
                                    />
                                </span>
                                <span className="label">
                                    App Notification
                                </span>
                            </label>
                        </div>
                        <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    checked={enableEmailNotificationForNotebooks}
                                    onChange={onChangeEmailNotificationForNotebook}
                                    />
                                </span>
                                <span className="label">
                                    Email
                                </span>
                            </label>
                        </div>
                        {/* <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    // checked={this.state.datacompliance.quickCheck}
                                    // onChange={this.onChangeOfQuickCheck}
                                    />
                                </span>
                                <span className="label">
                                    Option 3
                                </span>
                            </label>
                        </div> */}
                    </div>
                    <div id="optionList-3">
                        {/* <label className="input-label summary">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi scelerisque vulputate dictum</label>
                        <br />
                        <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    // checked={this.state.datacompliance.quickCheck}
                                    // onChange={this.onChangeOfQuickCheck}
                                    />
                                </span>
                                <span className="label">
                                    Option 1
                                </span>
                            </label>
                        </div>
                        <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    // checked={this.state.datacompliance.quickCheck}
                                    // onChange={this.onChangeOfQuickCheck}
                                    />
                                </span>
                                <span className="label">
                                    Option 2
                                </span>
                            </label>
                        </div>
                        <div className={Styles.optionsList}>
                            <label className="checkbox">
                                <span className="wrapper">
                                    <input
                                    type="checkbox"
                                    className="ff-only"
                                    // checked={this.state.datacompliance.quickCheck}
                                    // onChange={this.onChangeOfQuickCheck}
                                    />
                                </span>
                                <span className="label">
                                    Option 3
                                </span>
                            </label>
                        </div> */}
                    </div>
                </div>
            </div>
        </React.Fragment>
    )

}

export default NotificationsSettings;