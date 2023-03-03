import classNames from 'classnames';
import React from 'react';
import styles from './first-run.scss';
import computerIcon from '../../assets/computer_icon.png';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const FirstRun = ({ user, ...props }) => {
  let currentdate = new Date().toISOString();

  const openCreateProjectModal = () => {
    props.openCreateProjectModal();
  }

  return (
    <div className={classNames(styles.content)}>
      <div className={classNames(styles.header)}>
        <p className={styles.loginTime}>Login on {regionalDateAndTimeConversionSolution(currentdate)}</p>
        <p className={styles.lead}>Hi <span>{user.firstName} {user.lastName}</span>, this is the first time you are using the Forecasting Cockpit.</p>
      </div>
      <div className={styles.forecastContainer}>
        <div className={styles.messageContainer}>
          <img src={computerIcon} />
          <h2>Create a new forecast</h2>
          <p>Start your prediction using an Excel data file</p>
        </div>
        <div className={styles.btnContainer}>
          <button className={'btn btn-tertiary'} type="button" onClick={openCreateProjectModal}>
            <span>Create now</span>
          </button>
        </div>
      </div>
    </div>
  );
};
export default FirstRun;
