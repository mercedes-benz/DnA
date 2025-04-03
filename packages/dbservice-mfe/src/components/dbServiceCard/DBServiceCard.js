import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './db-service-card.scss';
import { useHistory } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Spinner from '../spinner/Spinner';
import Popper from 'popper.js';

const DBServiceCard = ({user, dbservice, onSelectDbService, onEditDbService, onDeleteDbService}) => {
  const history = useHistory();
  let popperObj, tooltipElem = null;
  const isOwner = user?.id === dbservice?.createdBy?.id;
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [dbservice]);

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

  const onCollabsIconMouseOver = (e) => {
    const targetElem = e.target;
    tooltipElem = targetElem.nextElementSibling;
    if (tooltipElem) {
      tooltipElem.classList.add('tooltip', 'show');
      tooltipElem.classList.remove('hide');
      popperObj = new Popper(targetElem, tooltipElem, {
        placement: 'top',
      });
    }
  };

  const onCollabsIconMouseOut = () => {
    if (tooltipElem) {
      tooltipElem.classList.add('hide');
      tooltipElem.classList.remove('tooltip', 'show');
    }
    popperObj?.destroy();
  };

  const handleConnect = () => {
    onSelectDbService(dbservice);  
  }

  return (
    <div className={classNames(Styles.projectCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div
            className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
            onClick={handleOpenDbService}
          >
            {dbservice?.bucketName}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          <div>
            <div>Created on</div>
            <div>{dbservice?.createdDate && regionalDateAndTimeConversionSolution(dbservice?.createdDate)}</div>
          </div>
          <div>
            <div>Last modified</div>
            <div>{dbservice?.lastModifiedDate && regionalDateAndTimeConversionSolution(dbservice?.lastModifiedDate)}</div>
          </div>
          <div>
            <div>Classification</div>
            <div>{dbservice?.classificationType || 'N/A'}</div>
          </div>
          <div>
            <div>Permission</div>
            <div>
              {displayPermission(dbservice?.permission) || 'N/A'}
              {isOwner && ` (Owner)`}
            </div>
          </div>
          <div>
            <div>DB Service Link</div>
            <div>
              <a href={`https://link.com/${dbservice.id}`} target='_blank' rel='noopener noreferrer'>
                Go to DB Service
                <i className={classNames('icon mbc-icon new-tab')} />
              </a>
            </div>
          </div>
          <div>
            <div>Created by</div>
            <div>{dbservice?.createdBy?.firstName} {dbservice?.createdBy?.lastName}</div>
          </div>
          <div className={Styles.cardCollabSection}>
            <div>Collaborators</div>
            {dbservice?.collaborators?.length > 0 ? (
              <div>
                <i className="icon mbc-icon profile"/>
                <span className={Styles.cardCollabIcon} onMouseOver={onCollabsIconMouseOver} onMouseOut={onCollabsIconMouseOut}>
                  {dbservice?.collaborators?.length}
                </span>
                <div className={classNames(Styles.collabsList, 'hide')}>
                  <ul>
                    {dbservice?.collaborators?.map((bucketItem, bucketIndex) => {
                        // Check if lastName is more than 12 characters
                        let lastName = bucketItem.lastName;
                        if (lastName?.length > 12) {
                          lastName = lastName.substring(0, 12) + " ...";
                        }
                      return (
                        <li key={'collab' + bucketIndex}>
                          <span>
                            {`${bucketItem.firstName} ${lastName}`}
                            {dbservice.createdBy?.id === bucketItem.accesskey ? ' (Owner)' : ''}
                          </span>
                          <span className={Styles.permission}>
                            &nbsp;[{displayPermission(bucketItem?.permission)}]
                          </span>
                        </li>
                      );
                    })}
                  </ul>
                </div>
              </div>
            ) : <div>None</div>}
          </div>
        </div>
      </div>
      <div className={Styles.cardFooter}>
        <>
          <div className={Styles.statusContainer}>
            <div className={Styles.statusItem}>
              <button tooltip-data={'Click for more information'} onClick={() => onSelectDbService(dbservice)}>
                {dbservice?.status?.state === 'IN_PROGRESS' && <><Spinner /> <span>In progress</span></>}
              </button>
              {dbservice?.status?.state === 'COMPLETED' && 
                <button className={Styles.completedStatus} onClick={() => onSelectDbService(dbservice)}>
                  <i className={'icon mbc-icon check circle'}></i> <span>Provisioned</span>
                </button>
              }
              {/* {isRequestedWorkspace && dbservice?.status?.state === 'IN_PROGRESS' && <p className={Styles.requestStatus}>Workspace Accesss Requested</p>} */}
            </div>
          </div>
          {user?.id === dbservice?.createdBy?.id &&
            <div className={Styles.btnGrp}>
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={() => onEditDbService(dbservice)}
              >
                <i className="icon mbc-icon edit"></i>
                <span>Edit</span>
              </button>
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={() => onDeleteDbService(dbservice)}
              >
                <i className="icon delete"></i>
                <span>Delete</span>
              </button>
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={handleConnect}
              >
                <i className="icon mbc-icon comparison"></i>
                <span>Connect</span>
              </button>
            </div>
          }
        </>
      </div>
    </div>
  );
};
export default DBServiceCard;