import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './power-platform-environment-table.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const PowerPlatformEnvironmentTable = ({ environment, onMoreInfoClick }) => {
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [environment]);

  const handleOnLinkClick = () => {
    (environment?.state === 'APPROVED' && environment?.url?.length > 0) ? window.open(environment?.url) : undefined
  }

  return (
    <div className={Styles.projectRow} onClick={() => onMoreInfoClick(environment)}>
      <div className={Styles.col1}>
        <span className={(environment?.state === 'APPROVED' && environment?.url?.length > 0) && Styles.link} onClick={handleOnLinkClick}>
          {environment?.name} {(environment?.state === 'APPROVED' && environment?.url?.length > 0) && <i className={classNames('icon mbc-icon new-tab')}></i>}
        </span>
      </div>
      <div className={Styles.col2}>
        {environment?.state === 'REQUESTED' && <div className={Styles.requested}><i className={classNames('icon mbc-icon check circle')}></i> <span>Requested</span></div>}
        {environment?.state === 'APPROVED' && <div className={Styles.approved} tooltip-data={environment?.comments}><i className={classNames('icon mbc-icon check circle')}></i> <span>Approved</span></div>}
        {environment?.state === 'REJECTED' && <div className={Styles.rejected} tooltip-data={environment?.comments}><i className={classNames('icon mbc-icon close circle')}></i> <span>Rejected</span></div>}
      </div>
      <div className={Styles.col3}>
        {environment?.requestedOn ? regionalDateAndTimeConversionSolution(environment?.requestedOn) : 'N/A'}
      </div>
      <div className={Styles.col4}>
        {environment?.envOwnerId} {environment?.envOwnerName?.length > 0 ? ' - ' + environment?.envOwnerName : ''}
      </div>
      {/* <div className={Styles.col5}>
        <div className={Styles.btnTblGrp}>
          <button
            className={classNames('btn btn-primary', Styles.projectLink)}
            onClick={(e) => { e.stopPropagation(); onEditProject(environment); }}
          >
            <i className="icon mbc-icon edit"></i>
            <span>Edit</span>
          </button>
          
          <button
            className={classNames('btn btn-primary', Styles.projectLink)}
            onClick={(e) => { e.stopPropagation(); onDeleteProject(environment); }}
          >
            <i className="icon delete"></i>
            <span>Delete</span>
          </button>
        </div>
      </div> */}
    </div>
  );
}

export default PowerPlatformEnvironmentTable;