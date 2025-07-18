import React, { useRef, useState, useEffect } from 'react';
import Styles from './PipelineCardItem.scss';
import { IPipelineProjectDetail } from 'globals/types';
import { history } from '../../../router/History';
import { Envs } from 'globals/Envs';
import Modal from 'components/formElements/modal/Modal';
import { IconGear } from '/home/coder/app/packages/frontend/src/components/icons/IconGear';
import VaultManagement from '/home/coder/app/packages/frontend/src/components/mbc/pipeline/pipelineSubList/VaultManagement';
import Tooltip from '/home/coder/app/packages/frontend/src/assets/modules/uilab/js/src/tooltip';
import ExpansionPanel from '/home/coder/app/packages/frontend/src/assets/modules/uilab/js/src/expansion-panel';

interface Props {
  project: IPipelineProjectDetail;
  getRefreshedDagPermission: (projectId: string, dagIndex: number) => void;
}

const PipelineCardItem = ({ project, getRefreshedDagPermission }: Props) => {
  const [isDagPopupVisible, setIsDagPopupVisible] = useState(false);
  const popupRef = useRef<HTMLDivElement>(null);
  const [showVaultManagementModal, setShowVaultManagementModal] = useState(false);
  const [selectedProjectName, setSelectedProjectName] = useState('');

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
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);
  useEffect(() => {
    Tooltip.defaultSetup();
    ExpansionPanel.defaultSetup();
  }, []);

  return (
    <div className={Styles.pipelineCard}>
      <div className={Styles.cardHead}>
        <div className={Styles.cardHeadInfo} onClick={goToEditProject}>
          <div className={`btn btn-text forward arrow ${Styles.cardHeadTitle}`} title={project.projectName}>
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
            <div>Project Name</div>
            <div>{project.projectName}</div>
          </div>
          <div>
            <div>Permission</div>
            <div>{project.permission ? 'Read/Edit' : 'Read'}</div>
          </div>
          <div className={Styles.cardCollabSection}>
            <div>DAGs</div>
            {project.dags?.length > 0 ? (
              <div onMouseEnter={() => setIsDagPopupVisible(true)} onMouseLeave={() => setIsDagPopupVisible(false)}>
                <i className="icon mbc-icon profile" />
                <span className={Styles.cardCollabIcon}>{project.dags.length}</span>
                <div ref={popupRef} className={`${Styles.collabsList} ${isDagPopupVisible ? 'show' : 'hide'}`}>
                  <ul>
                    {project.dags.map((dag, idx) => (
                      <li key={idx} className={Styles.dagItem}>
                        <span className={Styles.dagName}>{dag.dagName}</span>

                        <span className={Styles.permission}>
                          {dag.permissions?.includes('can_edit') ? 'Read/Edit' : 'Read'}
                        </span>

                        <div className={Styles.cardDagActions}>
                          {project.isOwner && (
                            <button
                              className={Styles.actionBtn + ' btn btn-primary'}
                              onClick={() => setShowVaultManagementModal(prev => !prev)}
                              type="button"
                              tooltip-data="Configure Environment Variables"
                            >
                              <IconGear size={'18'} />
                            </button>
                          )}
                          <button
                            className={Styles.actionBtn}
                            onClick={() => goToDag(dag.dagName)}
                            tooltip-data="Open in Editor"
                          >
                            <i className="icon mbc-icon edit" />
                          </button>

                          <a
                            href={`${Envs.DATA_PIPELINES_APP_BASEURL}/graph?dag_id=${dag.dagName}`}
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.actionBtn}
                            tooltip-data="Open in New Tab"
                          >
                            <i className="icon mbc-icon new-tab" />
                          </a>
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
          <button className="btn btn-primary" onClick={goToEditProject}>
            <i className="icon mbc-icon edit"></i>
            <span>Edit</span>
          </button>
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
              projectName={selectedProjectName}
            />
          }
          scrollableContent={true}
          onCancel={() => setShowVaultManagementModal(false)}
        />
      )}

    </div>
    
  );
};

export default PipelineCardItem;
