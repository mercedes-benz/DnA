import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './fabric-workspace-card.scss';
import { useHistory } from 'react-router-dom';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Spinner from '../spinner/Spinner';
import { Envs } from '../../utilities/envs';
import InfoModal from 'dna-container/InfoModal';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { fabricApi } from '../../apis/fabric.api';
import AddUser from 'dna-container/AddUser';
import { USER_ROLE } from '../../utilities/constants';

const FabricWorkspaceCard = ({user, workspace, onSelectWorkspace, onEditWorkspace, onDeleteWorkspace}) => {
  const history = useHistory();
  const [newOwnerDetails, setNewOwnerDetails] = useState(null);
  const [showTransferOwnershipModal, setShowTransferOwnershipModal] = useState(false);
  const [showTakeOwnershipModal, setShowTakeOwnershipModal] = useState(false);
  const isFabricAdmin = user.roles.find(role => role.id === USER_ROLE.FABRICADMIN);
  useEffect(() => {
    Tooltip.defaultSetup();
  }, [workspace]);

  const onTransferOwnership = () => {
    ProgressIndicator.show();
    fabricApi
      .transferOwnership(workspace?.id, newOwnerDetails)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show('Ownership transferred successfully.');
        setShowTransferOwnershipModal(false);
        history.push('/');
      })
      .catch(() => {
        ProgressIndicator.hide();
        Notification.show('Error while transferring ownership.', 'alert');
      });
  };

  const takeOwnershipModalContent = (
    <div className={classNames('input-field-group include-error')}>
      <label className="input-labels">
        Are you sure you want to take ownership of this workspace from {' '} 
        <a
        href={`${Envs.MB_INSIDE_URL}${workspace?.createdBy?.id}`}
        target="_blank"
        rel="noopener noreferrer"
      >
        {workspace?.createdBy?.firstName} {workspace?.createdBy?.lastName}
      </a>
        ? Once you take ownership,
        the current owner will lose their ownership rights.
      </label>
      <div className={Styles.transferButton}>
        <button
          className="btn btn-tertiary"
          onClick={() => {
            ProgressIndicator.show();
            fabricApi
              .takeOwnership(workspace?.id)
              .then(() => {
                ProgressIndicator.hide();
                Notification.show('You are now the owner of this workspace.');
                setShowTakeOwnershipModal(false);
                history.push('/');
              })
              .catch(() => {
                ProgressIndicator.hide();
                Notification.show('Error while taking ownership.', 'alert');
              });
          }}
        >
          Yes
        </button>
        <button
          className="btn btn-tertiary"
          style={{ marginLeft: '12px' }}
          onClick={() => setShowTakeOwnershipModal(false)}
        >
          No
        </button>
      </div>
    </div>
  );

  const getCollaborators = (collaborator) => {
    const collaborationData = {
      firstName: collaborator.firstName,
      lastName: collaborator.lastName,
      id: collaborator.shortId,
      department: collaborator.department,
      email: collaborator.email,
      mobileNumber: collaborator.mobileNumber, 
    };
    setNewOwnerDetails(collaborationData);
  };

  const securedWithIAMContent = (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      stroke="#00adef"
      fill="#00adef"
      strokeWidth="0"
      viewBox="0 0 30 30"
      width="15px"
      height="15px"
    >
      {' '}
      <path d="M 15 2 C 11.145666 2 8 5.1456661 8 9 L 8 11 L 6 11 C 4.895 11 4 11.895 4 13 L 4 25 C 4 26.105 4.895 27 6 27 L 24 27 C 25.105 27 26 26.105 26 25 L 26 13 C 26 11.895 25.105 11 24 11 L 22 11 L 22 9 C 22 5.2715823 19.036581 2.2685653 15.355469 2.0722656 A 1.0001 1.0001 0 0 0 15 2 z M 15 4 C 17.773666 4 20 6.2263339 20 9 L 20 11 L 10 11 L 10 9 C 10 6.2263339 12.226334 4 15 4 z" />
    </svg>
  );

  const transferOwnershipModalContent = (
    <div className={classNames('input-field-group include-error')}>
      <label htmlFor="userId" className="input-label">
        Please note that once you transfer ownership your access to the workspace will be removed. You can request access to the workspace through the DnA request fabric workspace access features or Alice.
      </label>
      <div className={Styles.collaboratorSection}>
        <div className={Styles.collaboratorSectionList}>
          {!newOwnerDetails ? (
            <div className={classNames(Styles.collaboratorSectionListAdd, Styles.addUserOverlay)}>
              <AddUser
                getCollabarators={getCollaborators}
                dagId={''}
                isRequired={false}
                isUserprivilegeSearch={false}
              />
            </div>
          ) : (
            <div className={Styles.ownerCard}>
              <button className="modal-close-button" onClick={() => setNewOwnerDetails(null)}>
                <i className="icon mbc-icon close thin" />
              </button>
              <div className={Styles.ownerInfo}>
                <div className={Styles.flexLayout}><div>Short ID:</div><div><a href={`${Envs.MB_INSIDE_URL}${newOwnerDetails.id}`} target='_blank' rel='noopener noreferrer'>{newOwnerDetails.id}</a></div></div>
                <div className={Styles.flexLayout}><div>First Name:</div><div>{newOwnerDetails.firstName || 'N/A'}</div></div>
                <div className={Styles.flexLayout}><div>Last Name:</div><div>{newOwnerDetails.lastName || 'N/A'}</div></div>
                <div className={Styles.flexLayout}><div>Department:</div><div>{newOwnerDetails.department || 'N/A'}</div></div>
                <div className={Styles.flexLayout}><div>Email:</div><div>{newOwnerDetails.email || 'N/A'}</div></div>
                <div className={Styles.flexLayout}><div>Mobile No:</div><div>{newOwnerDetails.mobileNumber || 'N/A'}</div></div>
              </div>
            </div>
          )}
        </div>  
      </div>
      {newOwnerDetails && (
        <div className={Styles.transferButton}>
          <button className="btn btn-tertiary" onClick={onTransferOwnership}>Transfer</button>     
        </div>
      )}
    </div>
  );

  const handleOpenWorkspace = () => {
    history.push(`/workspace/${workspace?.id}`);
  }

  const userRole = workspace?.userRole;
  const isOwner = user?.id === workspace?.createdBy?.id;
  const isAdmin = userRole === 'Admin';
  return (
    <>
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
              {isFabricAdmin && workspace?.createdBy?.id !== user?.id ? (
                <span className={Styles.disabledLink} title="Admins can only access their own workspaces">
                  Access Workspace
                  <i className={classNames('icon mbc-icon new-tab')} />
                </span>
              ) : (
                <a
                  href={`https://app.fabric.microsoft.com/groups/${workspace.id}`}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {workspace?.cmkDetails?.cmkKeyAssign && <span style={{ display: 'inline-block', verticalAlign: 'text-bottom', marginRight: '4px' }}>{securedWithIAMContent}</span>}Access Workspace
                  <i className={classNames('icon mbc-icon new-tab')} />
                </a>
              )}
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
              <div>{userRole}
                {isOwner && (
             <i
                className="icon mbc-icon comparison"
                tooltip-data="Transfer Ownership"
                onClick={() => setShowTransferOwnershipModal(true)}
                style={{ cursor: 'pointer', marginLeft: '6px' }}
              />
                )}
                 {isAdmin && (
                  <i
                    className="icon mbc-icon comparison"
                    tooltip-data="Take Ownership"
                    onClick={() => setShowTakeOwnershipModal(true)}
                    style={{ cursor: 'pointer', marginLeft: '6px' }}
                  />
                )}
            </div>
          </div>
          <div>
            <div>Classification</div>
            <div>{workspace?.dataClassification || 'N/A'}</div>
          </div>
          <div>
            <div className="report-links">
              <a
                href={`${Envs.FABRIC_REPORT_URL}%27${encodeURIComponent(workspace?.name)}%27`}
                target="_blank"
                rel="noopener noreferrer"
              >
                Billing
                <i className={classNames('icon mbc-icon new-tab')} />|&nbsp;&nbsp;
              </a>
              {(isAdmin || isOwner) && (
                <a
                  href={`${Envs.FABRIC_ACTIVITY_REPORT_URL}'${encodeURIComponent(workspace?.name)}'`}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Activity
                  <i className={classNames('icon mbc-icon new-tab')} />|&nbsp;&nbsp;
                </a> 
              )}
                <a
                  href={`${Envs.TICKET_SUPPORT_URL}`}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Support
                  <i className={classNames('icon mbc-icon new-tab')} />
                </a>          
            </div>
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
          {(user?.id === workspace?.createdBy?.id || isFabricAdmin) &&
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
    {showTransferOwnershipModal && (
      <div className={Styles.confirmModal}>
        <InfoModal
          title={`Select user to transfer ownership - ${workspace?.name}`}
          modalWidth={'60%'}
          modalStyle={{
            maxWidth: '80%',
            minHeight: '70%',
          }}
          show={showTransferOwnershipModal}
          content={transferOwnershipModalContent}
          onCancel={() => setShowTransferOwnershipModal(false)}
        />
      </div>
    )}
    {showTakeOwnershipModal && (
        <div className={Styles.confirmModal}>
          <InfoModal
            title={`Take ownership of ${workspace?.name}`}
            modalWidth={'40%'}
            modalStyle={{
              maxWidth: '50%',
              minHeight: '30%',
            }}
            show={showTakeOwnershipModal}
            content={takeOwnershipModalContent}
            onCancel={() => setShowTakeOwnershipModal(false)}
          />
        </div>
      )}
    </>
  );
};
export default FabricWorkspaceCard;
