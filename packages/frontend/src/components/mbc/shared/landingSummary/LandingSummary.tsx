import React from 'react';
import Styles from './LandingSummary.scss';

export interface ILandingSummaryProps {
  title: string;
  subTitle?: string;
  children: any;
}

const LandingSummary = (props: ILandingSummaryProps) => {

    return (
        <div className={Styles.landingSummary}>
            <div className={Styles.wrapper}>
                <div className={Styles.caption}>
                    <h2>{props.title}</h2>
                </div>
                <div className={Styles.subTitle}>
                    <p>{props.subTitle}</p>
                </div>
            </div>
            <div className={Styles.content}>
                {props.children}
            </div>
        </div>
    );
};

export default LandingSummary;