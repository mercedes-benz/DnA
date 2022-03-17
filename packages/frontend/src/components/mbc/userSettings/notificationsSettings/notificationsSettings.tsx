import React, { useEffect, useState } from 'react';
// import { ApiClient } from '../../../../services/ApiClient';
import { IUserInfo } from '../../../../globals/types';
import { INoticationModuleWrapper } from '../../../../globals/types';
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
    
    const [notificationPreferences, setNotificationPreferences] = useState<INoticationModuleWrapper>();
    
    

    useEffect(() => {
        setNotificationPreferences({
            modules: {        
                solution: [        
                    {enableAppNotifications: true},        
                    {enableEmailNotifications: false}        
                ],        
                notebook: [        
                    {enableAppNotifications: true},        
                    {enableEmailNotifications: false}        
                ]        
            }        
        });
        // ApiClient.getUserPreference(props?.user?.id)
        // .then((res) => {
        //   if (res.length) {
        //     setNotificationPreferences(res[1])
        //   }
        // }) 
    },[])
    useEffect(() => {    
        if(notificationPreferences?.modules){
            const solutionModules = notificationPreferences?.modules?.solution;
            solutionModules.forEach(item => {
                for( const k in item){ 
                    if (k == 'enableAppNotifications') {                        
                        setEnableSystemNotificationForSolutions(item[k])
                    }
                    if (k == 'enableEmailNotifications') {
                        setEnableEmailNotificationForSolutions(item[k])
                    }
                }
            });
            const notebookModules = notificationPreferences?.modules?.notebook;
            notebookModules.forEach(item => {
                for( const k in item){ 
                    if (k == 'enableAppNotifications') {                        
                        setEnableSystemNotificationForNotebooks(item[k])
                    }
                    if (k == 'enableEmailNotifications') {
                        setEnableEmailNotificationForNotebooks(item[k])
                    }
                }
            })
            
        }     
    }, [notificationPreferences]);


    const onChangeEmailNotificationForSolution = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEnableEmailNotificationForSolutions(e.target.checked);
        
        const target = notificationPreferences?.modules?.solution;
        target.forEach(item => {
            for( const k in item){ 
                if (k == 'enableEmailNotifications') {
                    item[k] = e.target.checked;
               }
            }
        })
        
        setNotificationPreferences(notificationPreferences);
        callToUpdatePreference();
    }

    const onChangeEmailNotificationForNotebook = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEnableEmailNotificationForNotebooks(e.target.checked);
        
        const target = notificationPreferences?.modules?.notebook;
        target.forEach(item => {
            for( const k in item){ 
                if (k == 'enableEmailNotifications') {
                    item[k] = e.target.checked;
               }
            }
        })
        
        setNotificationPreferences(notificationPreferences);
        callToUpdatePreference();
    }

    const callToUpdatePreference = () => {
        // ApiClient.enableEmailNotificationsForSolutions(enableEmailNotificationForSolutions, props?.user?.id).then(
        //     res => {

        //     }
        // )
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
                                    disabled
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