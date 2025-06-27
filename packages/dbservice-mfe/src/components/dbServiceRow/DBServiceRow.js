import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './db-service-row.scss';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const DBServiceRow = ({user, dbservice, onEditDbService, onDeleteDbService, onShowDetailsModal}) => {
  // const history = useHistory();
  const isOwner = user?.id === dbservice?.createdBy?.id;

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const handleOpenDbService = () => {
    onShowDetailsModal(dbservice);
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
              tooltip-data={'Edit'}
            >
              <i className="icon mbc-icon edit"></i>
              {/* <span>Edit</span> */}
            </button>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onDeleteDbService(dbservice); }}
              tooltip-data={'Delete'}
            >
              <i className="icon delete"></i>
              {/* <span>Delete</span> */}
            </button>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onDeleteDbService(dbservice); }}
              tooltip-data={'Connect'}
            >
              <i className="icon mbc-icon comparison"></i>
              {/* <span>Connect</span> */}
            </button>
          </div>
        }
      </div>
    </div>
  );
}

export default DBServiceRow;