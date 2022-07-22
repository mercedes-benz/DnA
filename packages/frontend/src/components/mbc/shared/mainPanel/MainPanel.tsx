import React from 'react';
import Styles from './MainPanel.scss';

export interface IMainPanelProps {
  title: string;
  children: any;
}

const MainPanel = (props: IMainPanelProps) => {

  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <div className={Styles.caption}>
          <h3>{props.title}</h3>
        </div>
      </div>
      <div className={Styles.content}>
        {props.children}
      </div>
    </div>
  );
};

export default MainPanel;