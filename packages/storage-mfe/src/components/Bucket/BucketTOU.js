import classNames from 'classnames';
import React, { useState } from 'react';
import IconToU from 'dna-container/IconToU';
import { Envs } from '../Utility/envs';
import Styles from './BucketTOU.scss';
import { history } from '../../store/storeRoot';

const BucketTOU = (props) => {
    const [isTouChecked, setIsTouChecked] = useState(false);

    const handleTouAccept = () => {
        props.setShowTermsModal(false);
        history.push('createBucket');
    }

    return (
        <>
            <div className={Styles.touContainer}>
                <div className={Styles.touTitle}>
                    <div className={Styles.touIcon}>
                        <IconToU size='80' />
                    </div>
                    <h2>
                        Terms of Use
                        <span>Please agree to our terms of use before you start.</span>
                    </h2>
                </div>
                <div className={Styles.touContent}>
                    <div
                        dangerouslySetInnerHTML={{
                            __html: Envs.STORAGE_TERMS_OF_USE_CONTENT,
                        }}
                    ></div>
                    <div>
                        <label className={classNames('checkbox', Styles.checkbox)}>
                            <span className="wrapper">
                                <input
                                    name="write"
                                    type="checkbox"
                                    className="ff-only"
                                    checked={isTouChecked}
                                    onChange={() => {
                                        setIsTouChecked(!isTouChecked);
                                    }}
                                />
                            </span>
                            I have read and agree to the Terms of Use
                        </label>
                    </div>
                </div>
                <div className={Styles.touFooter}>
                    <button className={classNames('btn', isTouChecked && Styles.btnAgree)} onClick={handleTouAccept} disabled={!isTouChecked}>Accept & Enter</button>
                </div>
            </div>
        </>
    )
};

export default BucketTOU;