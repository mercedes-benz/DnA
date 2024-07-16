import React, { useEffect, useState } from 'react';
import Styles from './workspaces.scss';
import classNames from 'classnames';
import { history } from '../../store';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import Spinner from '../spinner/Spinner';
import { fabricApi } from '../../apis/fabric.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import FabricWorkspaceForm from '../fabricWorkspaceForm/FabricWorkspaceForm';

const RoleCreationStatus = ({workspace, onClose}) => {
  return (
    <div className={Styles.modal}>
      <div className={Styles.header}>
        <h3>{workspace?.name}</h3>
        <p>Entitlements, Roles, Microsoft Groups</p>
      </div>
      <div className={Styles.content}>
        { /* Entitlements Table */}
        <div className={Styles.tableContainer}>
          <h4>Entitlements</h4>
          <div className={Styles.table}>
            <div className={classNames(Styles.tableRow, Styles.tableHeader)}>
                <div className={Styles.tableCell}>Entitlement Name</div>
                <div className={Styles.tableCell}>Status</div>
            </div>
            {workspace?.status?.entitlements?.map(entitlement => 
              <div key={entitlement?.id} className={Styles.tableRow}>
                <div className={Styles.tableCell}>{entitlement?.displayName}</div>
                <div className={Styles.tableCell}>
                  <div className={Styles.statusContainer}>
                    <div className={Styles.statusItem}>
                      <button tooltip-data={'Click for more information'}>
                        {entitlement?.state === 'PENDING' && <><Spinner /> <span>In progress</span></>}
                        {entitlement?.state === 'CREATED' && <><i className={classNames('icon mbc-icon check circle')} /> <span>Created</span></>}
                        {entitlement?.state === 'FAILED' && <><i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} /> <span>Failed</span></>}
                      </button>
                    </div>
                  </div>
                </div>
              </div>  
            )}
          </div>
        </div>
        { /* Roles Table */}
        <div className={Styles.tableContainer}>
          <h4>Roles</h4>
          <div className={Styles.table}>
            <div className={classNames(Styles.tableRow, Styles.tableHeader)}>
                <div className={classNames(Styles.tableCell, Styles.column1)}>Role</div>
                <div className={classNames(Styles.tableCell, Styles.column2)}>Entitlements</div>
                <div className={classNames(Styles.tableCell, Styles.column3)}>Link</div>
                <div className={classNames(Styles.tableCell, Styles.column4)}>Status</div>
            </div>
            {workspace?.status?.roles?.map(role => 
              <div key={role?.id} className={Styles.tableRow}>
                <div className={classNames(Styles.tableCell, Styles.column1)}>{role?.name}</div>
                <div className={classNames(Styles.tableCell, Styles.column2)}>
                  {role?.entitlements.map(entitlement => <p key={entitlement?.id}>{entitlement?.displayName}</p>)}
                </div>
                <div className={classNames(Styles.tableCell, Styles.column3)}>
                  <a href={role?.link} target='_blank' rel='noopener noreferrer'>Alice Link <i className={classNames('icon mbc-icon new-tab')} /></a>
                </div>
                <div className={classNames(Styles.tableCell, Styles.column4)}>
                  <div className={Styles.statusContainer}>
                    <div className={Styles.statusItem}>
                      <button tooltip-data={'Click for more information'}>
                        {role?.state === 'PENDING' && <><Spinner /> <span>In progress</span></>}
                        {role?.state === 'CREATED' && <><i className={classNames('icon mbc-icon check circle')} /> <span>Created</span></>}
                        {role?.state === 'FAILED' && <><i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} /> <span>Failed</span></>}
                      </button>
                    </div>
                  </div>
                </div>
              </div>  
            )}
          </div>
        </div>
        { /* Microsoft Groups Table */}
        <div className={Styles.tableContainer}>
          <h4>Microsoft Groups</h4>
          <div className={Styles.table}>
            <div className={classNames(Styles.tableRow, Styles.tableHeader)}>
                <div className={Styles.tableCell}>Group Name</div>
                <div className={Styles.tableCell}>Status</div>
            </div>
            {workspace?.status?.microsoftGroups?.map(group => 
              <div key={group?.id} className={Styles.tableRow}>
                <div className={Styles.tableCell}>{group?.groupName}</div>
                <div className={Styles.tableCell}>
                  <div className={Styles.statusContainer}>
                    <div className={Styles.statusItem}>
                      <button tooltip-data={'Click for more information'}>
                        {group?.state === 'PENDING' && <><Spinner /> <span>In progress</span></>}
                        {group?.state === 'CREATED' && <><i className={classNames('icon mbc-icon check circle')} /> <span>Created</span></>}
                        {group?.state === 'FAILED' && <><i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} /> <span>Failed</span></>}
                        {group?.state === 'ASSIGNED' && <><i className={classNames('icon mbc-icon check circle', Styles.assigned)} /> <span>Assigned</span></>}
                      </button>
                    </div>
                  </div>
                </div>
              </div>  
            )}
          </div>
        </div>
      </div>
      <div className={Styles.footer}>
        <button
          className="btn btn-tertiary"
          type="button"
          onClick={onClose}
        >
          Okay
        </button>
      </div>
    </div>
  );
}

const Workspaces = (props) => {
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});
  const [editWorkspace, setEditWorkspace]  = useState(false);
  const [showStatusModal, setShowStatusModal] = useState(false);

  const isCardView = props.isCardView;
  const workspaces = props.workspaces;

  useEffect(() => {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }, []);

  const deleteWorkspaceContent = (
    <div>
      <h3>Are you sure you want to delete {selectedItem.name}? </h3>
      <h5>It will delete the workspace.</h5>
    </div>
  );

  const deleteWorkspaceClose = () => {
    setDeleteModal(false);
  };

  const deleteWorkspaceAccept = () => {
    ProgressIndicator.show();
    fabricApi
      .deleteFabricWorkspace(selectedItem.id)
      .then(() => {
        props.callWorkspaces();
        Notification.show(`Fabric Workspace ${selectedItem.name} deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting fabric workspace. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  return (
    <>
      {isCardView ? (
        <>
          <div className={Styles.newStorageCard} onClick={() => props.onCreateWorkspace(true)}>
            <div className={Styles.addicon}> &nbsp; </div>
            <label className={Styles.addlabel}>Create new Fabric Workspace</label>
          </div>
          {workspaces?.map((workspace, index) => {
            return (
              <div key={'card-' + index} className={classNames(Styles.storageCard)}>
                <div className={Styles.cardHead}>
                  <div className={classNames(Styles.cardHeadInfo)}>
                    <div
                      className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
                      onClick={() => {history.push(`/workspace/${workspace.id}`)}}
                    >
                      {workspace.name}
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
                      <div>{regionalDateAndTimeConversionSolution(workspace.createdOn)}</div>
                    </div>
                    {/* <div>
                      <div>Last modified</div>
                      <div>{regionalDateAndTimeConversionSolution(workspace.lastModified)}</div>
                    </div> */}
                    <div>
                      <div>Classification</div>
                      <div>{workspace.dataClassification || 'N/A'}</div>
                    </div>
                  </div>
                </div>
                <div className={Styles.cardFooter}>
                  <>
                    <div className={Styles.statusContainer}>
                      <div className={Styles.statusItem}>
                        <button tooltip-data={'Click for more information'} onClick={() => { setSelectedItem(workspace); setShowStatusModal(true) }}>
                          {workspace?.status?.state === 'IN_PROGRESS' && <><Spinner /> <span>In progress</span></>}
                          {workspace?.status?.state === 'COMPLETED' && <><i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} /> <span>Completed</span></>}
                        </button>
                      </div>
                    </div>
                    <div className={Styles.btnGrp}>
                      <button
                        className={'btn btn-primary'}
                        type="button"
                        onClick={() => {
                          setSelectedItem(workspace);
                          setEditWorkspace(true);
                        }}
                      >
                        <i className="icon mbc-icon edit"></i>
                        <span>Edit</span>
                      </button>
                      
                      <button
                        className={'btn btn-primary'}
                        type="button"
                        onClick={() => {
                          setSelectedItem(workspace);
                          setDeleteModal(true);
                        }}
                      >
                        <i className="icon delete"></i>
                        <span>Delete</span>
                      </button>
                    </div>
                  </>
                </div>
              </div>
            );
          })}
        </>
      ) : (
        <div className={classNames('expanstion-table', Styles.bucketList)}>
          <div className={Styles.bucketGrp}>
            <div className={Styles.bucketGrpList}>
              <div className={Styles.bucketGrpListItem}>
                <div className={Styles.bucketCaption}>
                  <div className={Styles.bucketTile}>
                    <div className={classNames(Styles.bucketTitleCol, Styles.bucketName)}>
                      <label>
                        {/* <i className="icon sort" /> */}
                        Name
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>
                      <label>
                        {/* <i className="icon sort" /> */}
                        Workspace Link
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>
                      <label>
                        {/* <i className="icon sort" /> */}
                        Created On
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>
                      <label>
                        {/* <i className="icon sort" /> */}
                        Data Classification
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>Action</div>
                  </div>
                </div>
                {workspaces?.map((workspace, index) => {
                  return (
                    <div
                      key={index}
                      className={'expansion-panel-group airflowexpansionPanel ' + Styles.bucketGrpListItemPanel}
                      onClick={() => {history.push(`/workspace/${workspace.id}`)}}
                    >
                      <div className={classNames('expansion-panel ', index === 0 ? 'open' : '')}>
                        <span className="animation-wrapper"></span>
                        <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                        <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                          <div className={Styles.bucketTile}>
                            <div className={classNames(Styles.bucketTitleCol, Styles.bucketName)}>
                              <span>
                                {workspace.name}
                              </span>
                            </div>
                            <div className={classNames(Styles.bucketTitleCol, Styles.workspaceLink)}>
                              <a href={`https://app.fabric.microsoft.com/groups/${workspace.id}`} target='_blank' rel='noopener noreferrer'>
                                Access Workspace
                                <i className={classNames('icon mbc-icon new-tab')} />
                              </a>
                            </div>
                            <div className={Styles.bucketTitleCol}>
                              {regionalDateAndTimeConversionSolution(workspace.createdOn)}
                            </div>
                            <div className={Styles.bucketTitleCol}>{workspace.dataClassification}</div>
                            <div className={Styles.bucketTitleCol}>
                            <div className={Styles.cardFooter}>
                              <>
                                <div className={Styles.btnTblGrp}>
                                  <button
                                    className={classNames('btn btn-primary', Styles.projectLink)}
                                    type="button"
                                    onClick={(e) => { e.stopPropagation(); setSelectedItem(workspace); setEditWorkspace(true); }}
                                  >
                                    {/* <i className="icon mbc-icon edit"></i> */}
                                    <span>Edit</span>
                                  </button>
                                  
                                  <button
                                    className={classNames('btn btn-primary', Styles.projectLink)}
                                    type="button"
                                    onClick={(e) => { e.stopPropagation(); setSelectedItem(workspace); setDeleteModal(true); }}
                                  >
                                    {/* <i className="icon delete"></i> */}
                                    <span>Delete</span>
                                  </button>
                                </div>
                              </>
                            </div>
                            </div>
                          </div>
                        </label>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      )}
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteWorkspaceContent}
        onCancel={deleteWorkspaceClose}
        onAccept={deleteWorkspaceAccept}
      />
      { editWorkspace &&
        <Modal
          title={'Edit Fabric Workspace'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={editWorkspace}
          content={<FabricWorkspaceForm edit={true} workspace={selectedItem} onSave={() => {setEditWorkspace(false); props.callWorkspaces(); }} />}
          scrollableContent={true}
          onCancel={() => setEditWorkspace(false)}
        />
      }
      { showStatusModal &&
        <Modal
          title={'Role Creation Status'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={showStatusModal}
          content={<RoleCreationStatus workspace={selectedItem} onClose={() => setShowStatusModal(false)} />}
          scrollableContent={true}
          onCancel={() => setShowStatusModal(false)}
        />
      }
    </>
  );
};

export default Workspaces;
