import React, { useEffect } from 'react';
import Styles from './subscriptionsSettings.scss';


const SubscriptionsSettings = () => {

    useEffect(() => {
    }, []);


    return (
        <React.Fragment>
            <div className={Styles.wrapper}>  
                <div className={Styles.caption}>
                    <h3>Subscriptions</h3>
                </div>
            </div>
        </React.Fragment>
    )

}

export default SubscriptionsSettings;