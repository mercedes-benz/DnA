import React, { useEffect, useState } from 'react';
import Styles from './workspaces.scss';
import classNames from 'classnames';
import { history } from '../../store';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import { fabricApi } from '../../apis/fabric.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import FabricWorkspaceForm from '../fabricWorkspaceForm/FabricWorkspaceForm';

const Workspaces = (props) => {
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});
  const [editWorkspace, setEditWorkspace]  = useState(false);

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
                    <div></div>
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
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editWorkspace}
          content={<FabricWorkspaceForm edit={true} workspace={selectedItem} onSave={() => {setEditWorkspace(false); props.callWorkspaces(); }} />}
          scrollableContent={true}
          onCancel={() => setEditWorkspace(false)}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
    </>
  );
};

export default Workspaces;
