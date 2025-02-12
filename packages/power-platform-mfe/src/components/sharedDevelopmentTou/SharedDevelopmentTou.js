import classNames from 'classnames';
import React from 'react';
import { Envs } from '../../utilities/envs';
import Styles from './shared-development-tou.scss';

const SharedDevelopmentTou = ({ onAccept, hideAccept, isEn }) => {
  return (
    <div className={classNames(Styles.form)}>
      <div className={Styles.formHeader}>
        <h3>Terms of Use</h3>
        <p>Power Platform: Shared Development Account</p>
      </div>
      <div className={Styles.flex}>
        <div className={Styles.col}>
          <div className={Styles.touContent}>
            {isEn ? <div dangerouslySetInnerHTML={{__html: Envs.TOU_HTML}}></div> : <div dangerouslySetInnerHTML={{__html: Envs.TOU_GE_HTML}}></div>}
          </div>
        </div>
      </div>
      { !hideAccept && 
        <div className={Styles.formFooter}>
          <button
            className="btn btn-tertiary"
            type="button"
            onClick={onAccept}
          >
            Accept Terms of Use
          </button>
        </div>
      }
    </div>
  )
};

export default SharedDevelopmentTou;