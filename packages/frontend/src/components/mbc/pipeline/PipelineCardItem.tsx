import React, { useRef, useState, useEffect } from 'react';
import Styles from './PipelineCardItem.scss';
import { IPipelineProjectDetail } from 'globals/types';
import { history } from '../../../router/History';

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
      if (
        popupRef.current &&
        !popupRef.current.contains(event.target as Node)
      ) {
        setIsDagPopupVisible(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  return (
    <div className={Styles.pipelineCard}>
      <div className={Styles.cardHead}>
        <span className={Styles.projectName} title={project.projectName}>
          {project.projectName}
        </span>
      </div>
      <hr />

      <div className={Styles.dagList}>
        <div className={Styles.cardBodySection}>

          <div className={Styles.metaItem}>
            <span className={Styles.metaLabel}>ProjectId:</span>
            <span className={Styles.metaValue}>{project.projectId}</span>
          </div>
          <div className={Styles.metaItem}>
            <span className={Styles.metaLabel}>ProjectName:</span>
            <span className={Styles.metaValue} title={project.projectName}>
              {project.projectName}
            </span>
          </div>
          <div className={Styles.metaItem}>
            <span className={Styles.metaLabel}>Permission:</span>
            <span className={Styles.metaValue}>
              {project.permission ? 'Read/Edit' : 'Read'}
            </span>
          </div>

          <div className={Styles.cardDagSection} >
            <div>DAGs</div>
            <div className={Styles.dagSpacing} />
            {project.dags?.length > 0 ? (
              <div
                onMouseEnter={() => setIsDagPopupVisible(true)}
                onMouseLeave={() => setIsDagPopupVisible(false)}
              >
                <i className="icon mbc-icon profile" />
                <span
                  className={Styles.cardDagIcon}
                >
                  {project.dags.length}
                </span>
                <div
                  ref={popupRef}
                  className={`${Styles.dagListPopup} ${isDagPopupVisible ? 'show' : 'hide'
                    }`}
                >
                  <ul>
                    {project.dags.map((dag, idx) => (
                      <li key={idx} className={Styles.dagItem}>
                        <div className={Styles.dagLeft}>
                          <span className={Styles.dagName}>{dag.dagName}</span>
                          <span className={Styles.permission}>
                            [{dag.permissions?.includes('can_edit') ? 'Read/Edit' : 'Read'}]
                          </span>
                        </div>
                        <div className={Styles.cardDagActions}>
                          <button
                            type="button"
                            className={Styles.actionBtn}
                            onClick={() => goToDag(dag.dagName)}
                            title="Edit Code"
                          >
                            <i className="icon mbc-icon edit" />
                          </button>

                          <a
                            href={`/editcode/${dag.dagName}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className={`${Styles.actionBtn} ${Styles.externalBtn}`}
                            title="Open in new tab"
                          >
                            <i className={Styles.airflowNewTab + ' icon mbc-icon new-tab'} />
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
        <div className={Styles.cardFooter}>
          <button className={Styles.actionBtn} onClick={goToEditProject}>
            <i className="icon mbc-icon edit" />
            <span>Edit Project</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default PipelineCardItem;
