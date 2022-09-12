import classNames from 'classnames';
import React from 'react';
import { Link } from 'react-router-dom';
import Styles from './styles.scss';

import help from '../../assets/help.png';

const ChronosHelp = () => {
  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.breadcrumb)}>
          <ol>
            <li><a href='#/'>Start</a></li>
            <li><a href='#/services'>My Services</a></li>
            <li><Link to='/'>Chronos Forecasting</Link></li>
            <li>Help</li>
          </ol>
        </div>
        <h3 className={classNames(Styles.title)}>Chronos Help</h3>
        <div className={classNames(Styles.wrapper)}>
            <div className={Styles.container}>
              <h2>Learn more about how to set up your forecast<br />the right way to achieve better results.</h2>
              <img src={help} />
              <button className='btn btn-secondary'>Learn more</button>
            </div>
        </div>
      </div>
    </>
  );
};
export default ChronosHelp;
