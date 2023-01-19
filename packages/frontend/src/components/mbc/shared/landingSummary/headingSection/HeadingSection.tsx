import cn from 'classnames';
import React from 'react';
import Styles from './HeadingSection.scss';
import { history } from '../../../../../router/History';

const classNames = cn.bind(Styles);

export interface IHeadingSectionProps {
  title: string;
  subTitle?: string;
  headerImage?: string;
  isBackButton?: boolean;
}

const goback = () => {
  history.goBack();
};

const HeadingSection = (props: IHeadingSectionProps) => {
  return (
    <div className={Styles.wrapper}>
      <div className={classNames(props.headerImage && Styles.headingSection)}>
        {props.isBackButton ? (
          <button className={classNames('btn btn-text back arrow', Styles.backBtn)} type="submit" onClick={goback}>
            Back
          </button>
        ) : (
          ''
        )}
        {props.title && (
          <div className={Styles.caption}>
            <h2>{props.title}</h2>
          </div>
        )}
        {props.subTitle && (
          <div className={Styles.subTitle}>
            <p>{props.subTitle}</p>
          </div>
        )}
      </div>
      {props.headerImage && (
        <div className={Styles.imageSection}>
          <img src={props.headerImage}></img>
        </div>
      )}
    </div>
  );
};

export default HeadingSection;
