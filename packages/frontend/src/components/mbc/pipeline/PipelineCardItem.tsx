import React, { useRef, useState, useEffect } from 'react';
import Styles from './PipelineCardItem.scss';
import { IPipelineProjectDetail } from 'globals/types';
import { history } from '../../../router/History';
import { Envs } from 'globals/Envs';

interface Props {
  project: IPipelineProjectDetail;
  getRefreshedDagPermission: (projectId: string, dagIndex: number) => void;
}

const PipelineCardItem = ({ project, getRefreshedDagPermission }: Props) => {
  const [isDagPopupVisible, setIsDagPopupVisible] = useState(false);
  const popupRef = useRef<HTMLDivElement>(null);

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

  const isInProgress =
    project.projectStatus === 'CREATE_REQUESTED' ||
    project.projectStatus === 'UPDATE_REQUESTED';


  return (
    <div className={Styles.pipelineCard}>
      <div className={Styles.cardHead}>
        <div className={Styles.cardHeadInfo}>
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
            <div>{project.isOwner ? 'Owner' : 'Collaborator'}</div>
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
                                  title="Edit Code"
                                >
                                  <i className="icon mbc-icon edit" />
                                </button>
                              ) : (

                                <button
                                  className={Styles.actionBtn}
                                  onClick={() => goToDag(dag.dagName)}
                                  title="View Code"
                                >
                                  <i className="icon mbc-icon document" />
                                </button>
                              )}


                              <a
                                href={`${Envs.DATA_PIPELINES_APP_BASEURL}/graph?dag_id=${dag.dagName}`}
                                target="_blank"
                                rel="noreferrer"
                                className={Styles.actionBtn}
                                title="Open in New Tab"
                              >
                                <i className="icon mbc-icon new-tab" />
                              </a>
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
            <span className={`${Styles.statusIndicator} ${Styles.colloboration}`}>
              Creation in progress...
            </span>
          ) : project.projectStatus === 'UPDATE_REQUESTED' ? (
            <span className={`${Styles.statusIndicator} ${Styles.colloboration}`}>
              Updation in progress...
            </span>
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


    </div>
  );
};

export default PipelineCardItem;
