import classNames from 'classnames';
import React, { useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './db-service-row.scss';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Spinner from '../spinner/Spinner';

const DBServiceRow = ({user, dbservice, onSelectDbService, onEditDbService, onDeleteDbService}) => {
  const history = useHistory();
  const isOwner = user?.id === dbservice?.createdBy?.id;

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const handleOpenDbService = () => {
    history.push(`/dbservice/${dbservice?.id}`);
  }

  const displayPermission = (item) => {
    return Object.entries(item || {})
      ?.map(([key, value]) => {
        if (value === true) {
          return key;
        }
      })
      ?.filter((x) => x) // remove falsy values
      ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1)) // update first character to Uppercase
      ?.join(' / ');
  };

  return (
    <div className={Styles.projectRow} onClick={handleOpenDbService}>
      <div className={Styles.col1}>
        <span>
          {dbservice?.bucketName}
        </span>
        {dbservice?.status?.state === 'IN_PROGRESS' &&
          <button className={Styles.stateBtn} tooltip-data={'Click for more information'} onClick={(e) => { e.stopPropagation(); onSelectDbService(dbservice) }}>
            <Spinner /> <span>&nbsp;</span>
          </button>
        }
        {dbservice?.status?.state === 'COMPLETED' && 
          <button className={Styles.completedStatus}>
            <i className={'icon mbc-icon check circle'}></i> <span>Provisioned</span>
          </button>
        }
      </div>
      <div className={Styles.col2}>
        {displayPermission(dbservice?.permission) || 'N/A'}
        {isOwner && ` (Owner)`}
      </div>
      <div className={Styles.col3}>
        {dbservice?.createdDate && regionalDateAndTimeConversionSolution(dbservice?.createdDate)}
      </div>
      <div className={Styles.col4}>
        {dbservice?.lastModifiedDate && regionalDateAndTimeConversionSolution(dbservice?.lastModifiedDate)}
      </div>
      <div className={Styles.col5}>
        {dbservice?.classificationType}
      </div>
      <div className={Styles.col6}>
        {user?.id === dbservice?.createdBy?.id &&
          <div className={Styles.btnTblGrp}>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onEditDbService(dbservice); }}
            >
              <i className="icon mbc-icon edit"></i>
              <span>Edit</span>
            </button>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onDeleteDbService(dbservice); }}
            >
              <i className="icon delete"></i>
              <span>Delete</span>
            </button>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onDeleteDbService(dbservice); }}
            >
              <i className="icon mbc-icon comparison"></i>
              <span>Connect</span>
            </button>
          </div>
        }
      </div>
    </div>
  );
}

export default DBServiceRow;