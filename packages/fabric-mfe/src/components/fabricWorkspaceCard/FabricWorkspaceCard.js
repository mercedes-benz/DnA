import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './fabric-workspace-card.scss';
import { useHistory } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Spinner from '../spinner/Spinner';

const FabricWorkspaceCard = ({user, workspace, onSelectWorkspace, onEditWorkspace, onDeleteWorkspace}) => {
  const history = useHistory();
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [workspace]);

  const handleOpenWorkspace = () => {
    history.push(`/workspace/${workspace?.id}`);
  }

  return (
    <div className={classNames(Styles.projectCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div
            className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
            onClick={handleOpenWorkspace}
          >
            {workspace?.name || 'null'}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          <div>
            <div>Workspace Link</div>
            <div>
              <a href={`https://app.fabric.microsoft.com/groups/${workspace.id}`} target='_blank' rel='noopener noreferrer'>
                Access Workspace
                <i className={classNames('icon mbc-icon new-tab')} />
              </a>
            </div>
          </div>
          <div>
            <div>Created on</div>
            <div>{workspace?.createdOn && regionalDateAndTimeConversionSolution(workspace?.createdOn)}</div>
          </div>
          <div>
            <div>Created by</div>
            <div>{workspace?.createdBy?.firstName} {workspace?.createdBy?.lastName}</div>
          </div>
          {/* <div>
            <div>Role</div>
            <div>{workspace?.role || 'N/A'}</div>
          </div> */}
          <div>
            <div>Classification</div>
            <div>{workspace?.dataClassification || 'N/A'}</div>
          </div>
        </div>
      </div>
      <div className={Styles.cardFooter}>
        <>
          <div className={Styles.statusContainer}>
            <div className={Styles.statusItem}>
              <button tooltip-data={'Click for more information'} onClick={() => onSelectWorkspace(workspace)}>
                {workspace?.status?.state === 'IN_PROGRESS' && <><Spinner /> <span>In progress</span></>}
              </button>
              {workspace?.status?.state === 'COMPLETED' && 
                <button className={Styles.completedStatus} onClick={() => onSelectWorkspace(workspace)}>
                  <i className={'icon mbc-icon check circle'}></i> <span>Provisioned</span>
                </button>
              }
              {/* {isRequestedWorkspace && workspace?.status?.state === 'IN_PROGRESS' && <p className={Styles.requestStatus}>Workspace Accesss Requested</p>} */}
            </div>
          </div>
          {user?.id === workspace?.createdBy?.id &&
            <div className={Styles.btnGrp}>
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={() => onEditWorkspace(workspace)}
              >
                <i className="icon mbc-icon edit"></i>
                <span>Edit</span>
              </button>
              <button
                className={'btn btn-primary'}
                type="button"
                onClick={() => onDeleteWorkspace(workspace)}
              >
                <i className="icon delete"></i>
                <span>Delete</span>
              </button>
            </div>
          }
        </>
      </div>
    </div>
  );
};
export default FabricWorkspaceCard;