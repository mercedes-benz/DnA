import React from 'react';
import Styles from './Comingsoon.scss';
import { history } from '../../../router/History';

const Comingsoon = () => {
  const goback = () => {
    history.goBack();
  };
  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <div className={Styles.backButtonWapper}>
          <button className="btn btn-text back arrow" type="submit" onClick={goback}>
            Back
          </button>
        </div>
        {/* <div className={Styles.caption}>
          <h3>Coming Soon</h3>
        </div> */}
        <div className={Styles.content}>
          <div className={Styles.comingSoonPg}>
            <h4>Coming Soon </h4>
            <p>The feature is currently under development</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Comingsoon;
