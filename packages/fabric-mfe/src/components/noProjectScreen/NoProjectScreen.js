import classNames from 'classnames';
import React from 'react';
import Styles from './no-project-screen.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const NoProjectScreen = ({ user, ...props }) => {
  let currentdate = new Date().toISOString();

  const openCreateProjectModal = () => {
    props.openCreateProjectModal();
  }

  return (
    <div className={classNames(Styles.content)}>
      <div className={classNames(Styles.header)}>
        <p className={Styles.loginTime}>Login on {regionalDateAndTimeConversionSolution(currentdate)}</p>
        <p className={Styles.lead}>Hi <span>{user.firstName} {user.lastName}</span>, this is the first time you are using Data Lakehouse.</p>
      </div>
      <div className={Styles.forecastContainer}>
        <div className={Styles.messageContainer}>
          <div className={Styles.iconContainer}>
            <i className={classNames('icon mbc-icon data', Styles.createProj)}></i>
          </div>
          <h2>Create a new data lakehouse project</h2>
          <p>Database design tool based on entity relation diagram</p>
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
export default NoProjectScreen;
