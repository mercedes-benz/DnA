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

const FabricWorkspaceCard = ({user, workspace, onSelectWorkspace, onEditWorkspace, onDeleteWorkspace}) => {
  const history = useHistory();
  const [newOwnerDetails, setNewOwnerDetails] = useState(null);
  const [showTransferOwnershipModal, setShowTransferOwnershipModal] = useState(false);
  
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
              <div>{userRole}
                {isOwner && (
             <i
                className="icon mbc-icon comparison"
                tooltip-data="Transfer Ownership"
                onClick={() => setShowTransferOwnershipModal(true)}
                style={{ cursor: 'pointer', marginLeft: '6px' }}
              />
                )}
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
    </>
  );
};
export default FabricWorkspaceCard;
