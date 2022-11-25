import cn from 'classnames';
import React from 'react';
import Styles from './HeadingSection.scss';
import { history } from '../../../../../router/History';

const classNames = cn.bind(Styles);

export interface IHeadingSectionProps {
  title: string;
  subTitle?: string;
}

const goback = () => {
    history.goBack();
};

const HeadingSection = (props: IHeadingSectionProps) => {

    return (
        <div className={Styles.wrapper}>
            <div className={Styles.headingSection}>
                <button className={classNames('btn btn-text back arrow', Styles.backBtn)} type="submit" onClick={goback}>
                    Back
                </button>
                <div className={Styles.caption}>
                    <h2>{props.title}</h2>
                </div>
                <div className={Styles.subTitle}>
                    <p>{props.subTitle}</p>
                </div>
            </div>
            <div className={Styles.imageSection}>
                <img src={'https://img.freepik.com/free-photo/abstract-luxury-gradient-blue-background-smooth-dark-blue-with-black-vignette-studio-banner_1258-56228.jpg?w=826&t=st=1669363801~exp=1669364401~hmac=0010305baaab20c4c1bedf9019e999057e38fc0e35d607e8eb05bfe8a409148a'}></img>
            </div>
        </div>
    );
};

export default HeadingSection;