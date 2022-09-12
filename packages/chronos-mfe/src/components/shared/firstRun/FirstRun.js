import classNames from 'classnames';
import React from 'react';
import Styles from './FirstRun.scss';
import computerIcon from '../../../assets/computer_icon.png';

const FirstRun = (props) => {
  const openCreateProjectModal = () => {
    props.openCreateProjectModal();
  }
  return (
    <div className={classNames(Styles.content)}>
      <div className={classNames(Styles.header)}>
        <p>Login on 23.02.2020, 14:12:15</p>
        <p className={Styles.lead}>Hi <span>Lukas Jan</span>, this is the first time you are using the Forecasting Cockpit.</p>
      </div>
      <div className={Styles.forecastContainer}>
        <div className={Styles.messageContainer}>
          <img src={computerIcon} />
          <h2>Create a new forecast</h2>
          <p>Start your prediction using an Excel data file</p>
        </div>
        <div className={Styles.btnContainer}>
          <button className={'btn btn-tertiary'} type="button" onClick={openCreateProjectModal}>
            <span>Create now</span>
          </button>
        </div>
      </div>
    </div>
  );
};
export default FirstRun;
