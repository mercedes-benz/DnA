import classNames from 'classnames';
import React from 'react';
import { Link } from 'react-router-dom';
import Styles from './chronos-help.scss';
import Breadcrumb from '../../components/breadcrumb/Breadcrumb';
import help from '../../assets/help.png';

const ChronosHelp = () => {
  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Breadcrumb>
          <li><Link to='/'>Chronos Forecasting</Link></li>
          <li>Help</li>
        </Breadcrumb>
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