import React from 'react';
import Styles from './MainPanel.scss';

export interface IMainPanelProps {
  title: string;
  subTitle?: string;
  children: any;
}

const MainPanel = (props: IMainPanelProps) => {

  return (
    <div className={Styles.mainPanel}>
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

export default MainPanel;