import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './fabric-workspace-card.scss';
import { useHistory } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Spinner from '../spinner/Spinner';
import { Envs } from '../../utilities/envs';
import ConfirmModal from 'dna-container/ConfirmModal';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { fabricApi } from '../../apis/fabric.api';

const FabricWorkspaceCard = ({user, workspace, onSelectWorkspace, onEditWorkspace, onDeleteWorkspace}) => {
  const history = useHistory();
  const [ownerId, setOwnerId] = useState(null);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [workspace]);

   const onAcceptTransferOwnership = () => {
    ProgressIndicator.show();
    fabricApi
      .transferOwnership(workspace.id, ownerId)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show('Ownership transferred successfully.');
        setShowConfirmModal(false);
        history.push('/');
      })
      .catch(() => {
        ProgressIndicator.hide();
        Notification.show('Error while transferring ownership.', 'alert');
      });
  };
  const handleOpenWorkspace = () => {
    history.push(`/workspace/${workspace?.id}`);
  }

  const userRoles = user?.entitlementGroup
    ?.filter(ent => ent.startsWith(`${Envs.FABRIC_ENTITLEMENT_PREFIX}${workspace?.id}`))
    ?.map(ent => ent.split('_').at(-1));

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
          {workspace?.initiatedBy && (
            <div>
              <div>Initiated by</div>
              <div>{workspace?.initiatedBy}</div>
            </div>
          )}
          <div>
            <div>Created by</div>
            <div>{workspace?.createdBy?.firstName} {workspace?.createdBy?.lastName}</div>
          </div>
          <div>
            <div>Role</div>
            <div>{userRoles?.length ? userRoles?.join(', ') : 'Owner'}
             <i
                className="icon mbc-icon comparison"
                tooltip-data="Transfer Ownership"
                onClick={() => setShowConfirmModal(true)}
                style={{ cursor: 'pointer', marginLeft: '6px' }}
              />
            </div>
          </div>
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

      {showConfirmModal && (
        <ConfirmModal
          title="Confirm Transfer Ownership"
          description={
            <>
              <p>Select the new owner for this workspace:</p>
              <div className={Styles.bucketColContent}>
                <div className={Styles.bucketColUsersList}>
                  {workspace.members?.length > 0 ? (
                    <>
                      <div className={Styles.collUserTitle}>
                        <div className={Styles.collUserTitleCol}>User ID</div>
                        <div className={Styles.collUserTitleCol}>Name</div>
                        <div className={Styles.collUserTitleCol}>Action</div>
                      </div>
                      <div className={classNames('mbc-scroll', Styles.collUserContent)}>
                        {workspace.members
                          ?.filter((m) => m.id !== user?.id && m.id !== workspace?.createdBy?.id)
                          ?.map((m) => (
                            <div key={m.id} className={Styles.collUserContentRow}>
                              <div className={Styles.collUserTitleCol}>{m.id}</div>
                              <div className={Styles.collUserTitleCol}>
                                {m.firstName} {m.lastName}
                              </div>
                              <div className={Styles.collUserTitleCol}>
                                <button
                                  className="btn btn-primary"
                                  onClick={() => setOwnerId(m.id)}
                                >
                                  {ownerId === m.id ? 'Selected' : 'Select'}
                                </button>
                              </div>
                            </div>
                          ))}
                      </div>
                    </>
                  ) : (
                    <div className={Styles.bucketColContentEmpty}>
                      <h6>No collaborators available!</h6>
                    </div>
                  )}
                </div>
              </div>
            </>
          }
          onConfirm={onAcceptTransferOwnership}
          onCancel={() => setShowConfirmModal(false)}
        />
      )}
    </div>
  );
};
export default FabricWorkspaceCard;