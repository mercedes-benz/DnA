import React, { useRef, useState, useEffect } from 'react';
import Styles from './PipelineCardItem.scss';
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import Notification from '../../../assets/modules/uilab/js/src/notification';
import { IPipelineProjectDetail } from 'globals/types';
import { history } from '../../../router/History';
import { Envs } from 'globals/Envs';
import Modal from 'components/formElements/modal/Modal';
import { IconGear } from 'components/icons/IconGear';
import VaultManagement from 'components/mbc/pipeline/pipelineSubList/VaultManagement';
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import ExpansionPanel from '../../../assets/modules/uilab/js/src/expansion-panel';
import { PipelineApiClient } from '../../../services/PipelineApiClient';


interface Props {
  project: IPipelineProjectDetail;
  getRefreshedDagPermission: (projectId: string, dagIndex: number) => void;
}

const PipelineCardItem = ({ project, getRefreshedDagPermission }: Props) => {
  const [isDagPopupVisible, setIsDagPopupVisible] = useState(false);
  const popupRef = useRef<HTMLDivElement>(null);
  const [showVaultManagementModal, setShowVaultManagementModal] = useState(false);
  const [selectedDagName, setSelectedDagName] = useState<string | null>(null);
  const goToDag = (dagId: string) => {
    history.push('/editcode/' + dagId);
  };

  const goToEditProject = () => {
    history.push(`/createnewpipeline/${project.projectId}/true`);
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (popupRef.current && !popupRef.current.contains(event.target as Node)) {
        setIsDagPopupVisible(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    Tooltip.defaultSetup();
    ExpansionPanel.defaultSetup();
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const isInProgress =
    project.projectStatus === 'CREATE_REQUESTED' ||
    project.projectStatus === 'UPDATE_REQUESTED';

  const handleTriggerDag = (dagName: string) => {
    ProgressIndicator.show();
  PipelineApiClient.triggerDag(dagName)

    .then((res) =>{
      Notification.show(res.success);
      ProgressIndicator.hide();
    })
    .catch(() => {
      Notification.show("error in Triggering the Dag. Please try again later.", 'alert');
      ProgressIndicator.hide();
    })
};



  return (
    <div className={Styles.pipelineCard}>
      <div className={Styles.cardHead}>
        <div className={Styles.cardHeadInfo}>
          <div className={`${Styles.cardHeadTitle}`} title={project.projectName}>
            {project.projectName}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          <div>
            <div>Project ID</div>
            <div>{project.projectId}</div>
          </div>
          <div>
            <div>Description</div>
            <div>{project.projectDescription}</div>
          </div>
          <div>
            <div>Permission</div>
            <div>{project.isOwner ? 'Owner' : 'Collaborator'}</div>
          </div>
          <div>
            <div>CreatedBy</div>
            <div>{project.createdBy}</div>
          </div>

          <div className={Styles.cardCollabSection}>
            <div>DAGs</div>
            {project.dags?.length > 0 ? (
              <div onMouseEnter={() => setIsDagPopupVisible(true)} onMouseLeave={() => setIsDagPopupVisible(false)}>
                <i className="icon mbc-icon listview big" />
                <span className={Styles.cardCollabIcon}>{project.dags.length}</span>
                <div ref={popupRef} className={`${Styles.collabsList} ${isDagPopupVisible ? 'show' : 'hide'}`}>
                  <ul>
                    <div className={`${Styles.dagHeader} ${Styles.dagRow}`}>
                      <span>DAG Name</span>
                      <span>Permission</span>
                      <span>Actions</span>
                    </div>

                    {project.dags.map((dag, idx) => (
                      <li key={idx} className={Styles.dagItem}>
                        <span className={Styles.dagName}>{dag.dagName}</span>

                        <span className={Styles.permission}>
                          {dag.permissions?.includes('can_read') && dag.permissions?.includes('can_edit')
                            ? 'Read/Edit'
                            : dag.permissions?.includes('can_read')
                            ? 'Read'
                            : ''}
                        </span>

                        <div className={Styles.cardDagActions}>
                          {isInProgress ? (
                            <>
                              <button
                                className={`${Styles.actionBtn} ${Styles.disabled}`}
                                disabled
                                title="Disabled during progress"
                              >
                                <i className="icon mbc-icon edit" />
                              </button>
                              <button
                                className={`${Styles.actionBtn} ${Styles.disabled}`}
                                disabled
                                title="Disabled during progress"
                              >
                                <i className="icon mbc-icon new-tab" />
                              </button>
                            </>
                          ) : (
                            <>
                              {dag.permissions?.includes('can_edit') ? (
                                <button
                                  className={Styles.actionBtn}
                                  onClick={() => goToDag(dag.dagName)}
                                  tooltip-data="Edit Code"
                                >
                                  <i className="icon mbc-icon edit" />
                                </button>
                              ) : dag.permissions?.includes('can_read') ? (
                                <button
                                  className={Styles.actionBtn}
                                  onClick={() => goToDag(dag.dagName)}
                                  title="View Code"
                                >
                                  <i className="icon mbc-icon document" />
                                </button>
                              ) : null}
                              {dag.permissions?.includes('can_edit') && (
                                <button
                                  className={Styles.actionBtn}
                                  title="Trigger DAG"
                                  onClick={() => handleTriggerDag(dag.dagName)}
                                >
                                  <i className="icon mbc-icon trainings" />
                                </button>
                              )}

                              <a
                                href={`${Envs.DATA_PIPELINES_APP_BASEURL}/graph?dag_id=${dag.dagName}`}
                                target="_blank"
                                rel="noreferrer"
                                className={Styles.actionBtn}
                                tooltip-data="Open in New Tab"
                              >
                                <i className="icon mbc-icon new-tab" />
                              </a>
                                {project.isOwner && (
                                  <button
                                    className={Styles.actionBtn + ' btn btn-primary'}
                                    onClick={() => {
                                      setSelectedDagName(dag.dagName);
                                      setShowVaultManagementModal(prev => !prev)
                                    }}
                                    type="button"
                                    tooltip-data="Configure Environment Variables"
                                  >
                                    <IconGear size={'18'} />
                                  </button>
                                )}
                              </>
                          )}
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            ) : (
              <div>None</div>
            )}
          </div>
        </div>
      </div>
      <div className={Styles.cardFooter}>
        <div></div>
        <div className={Styles.btnGrp}>
          {project.projectStatus === 'CREATE_REQUESTED' ? (
            <span className={`${Styles.statusIndicator} ${Styles.colloboration}`}>Creation in progress...</span>
          ) : project.projectStatus === 'UPDATE_REQUESTED' ? (
            <span className={`${Styles.statusIndicator} ${Styles.colloboration}`}>Updation in progress...</span>
          ) : (
            project.isOwner && (
              <button className="btn btn-primary" onClick={goToEditProject}>
                <i className="icon mbc-icon edit"></i>
                <span>Edit Project</span>
              </button>
            )
          )}
        </div>
      </div>
      {showVaultManagementModal && (
        <Modal
          title="Configure Environment Variables"
          showAcceptButton={false}
          showCancelButton={true}
          buttonAlignment="right"
          modalWidth="80vw"
          modalStyle={{ height: '80vh', maxWidth: 'none' }}
          show={true}
          content={
            <VaultManagement
              projectName={project.projectName}
              dagName={selectedDagName}
            />
          }
          scrollableContent={true}
          onCancel={() => {
            setShowVaultManagementModal(false);
            setSelectedDagName(null);
          }}
        />
      )}

    </div>
  );
};

export default PipelineCardItem;
