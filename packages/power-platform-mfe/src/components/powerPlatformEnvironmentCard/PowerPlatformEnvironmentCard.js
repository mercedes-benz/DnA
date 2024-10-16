import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './power-platform-environment-card.scss';
// import { useHistory } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const PowerPlatformEnvironmentCard = ({ environment, onMoreInfoClick }) => {
  // const history = useHistory();
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [environment]);

  return (
    <div className={classNames(Styles.projectCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)} onClick={() => onMoreInfoClick(environment)}>
            {environment.name}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          {(environment?.state === 'APPROVED' && environment?.url?.length > 0) &&
            <div>
              <div>Environment URL</div>
              <div>
                <button className={classNames('btn btn-primary', Styles.accessLink)} onClick={() => window.open(environment?.url)}>
                  Access Link <i className={classNames('icon mbc-icon new-tab')}></i>
                </button>
              </div>
            </div>
          }
          <div>
            <div>Requested on</div>
            <div>{environment?.requestedOn ? regionalDateAndTimeConversionSolution(environment?.requestedOn) : 'N/A'}</div>
          </div>
          <div>
            <div>Requested by</div>
            <div>{environment?.requestedBy?.firstName} {environment?.requestedBy?.lastName}</div>
          </div>
          <div>
            <div>Environment Owner</div>
            <div>{environment?.envOwnerId} {environment?.envOwnerName.length > 0 ? ' - ' + environment?.envOwnerName : ''}</div>
          </div>
        </div>
      </div>
      <div className={Styles.cardFooter}>
        <>
          <div className={Styles.statusContainer}>
            <div className={Styles.statusItem}>
              <button tooltip-data={(environment?.state === 'APPROVED' || environment?.state === 'REJECTED') ? environment?.comments : undefined}>
                {environment?.state === 'REQUESTED' && <div className={Styles.requested}><i className={classNames('icon mbc-icon check circle')}></i> <span>Requested</span></div>}
                {environment?.state === 'APPROVED' && <div className={Styles.approved}><i className={classNames('icon mbc-icon check circle')}></i> <span>Approved</span></div>}
                {environment?.state === 'REJECTED' && <div className={Styles.rejected}><i className={classNames('icon mbc-icon close circle')}></i> <span>Rejected</span></div>}
              </button>
            </div>
          </div>
          <div className={Styles.btnGrp}>
            <button
              className={'btn btn-primary'}
              type="button"
              onClick={() => onMoreInfoClick(environment)}
            >
              <i className="icon mbc-icon info"></i>
              <span>More Info</span>
            </button>
          </div>
        </>
      </div>
    </div>
  );
};
export default PowerPlatformEnvironmentCard;