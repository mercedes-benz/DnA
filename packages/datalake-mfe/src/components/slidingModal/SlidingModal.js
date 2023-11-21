import classNames from 'classnames';
import React from 'react';
import Styles from './sliding-modal.scss';

const SlidingModal = ({title, content, toggle, setToggle}) => {
  return (
    <div className={classNames('drawer-wrapper', toggle ? '' : 'modal-open')}>
      <div className="drawer-mask" onClick={setToggle}></div>
      <div className="drawer">
        <div className="drawer-inner">
          <div className="drawer-scroll">
            <div className={Styles.modalHeader}>
                <h4>{title}</h4>
                  <button className="modal-close-button" onClick={setToggle}>
                      <i className="icon mbc-icon close thin"></i>
                  </button>
            </div>
            <div className="drawer-content">
                {content}
            </div>
            {/* <div className="drawer-footer">
              <button className="btn btn-primary" onClick={onCancel}>Cancel</button>
              <button className="btn btn-tertiary" onClick={onSave}>Commit</button>
            </div> */}
          </div>
      </div>
      </div>
    </div>
  )
}

export default SlidingModal;