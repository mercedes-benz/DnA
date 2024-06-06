import classNames from 'classnames';
import React, { useEffect } from 'react';
import Styles from './data-entry-project-card.scss';
import { useHistory } from 'react-router-dom';
import { formatDateToISO, regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const DataEntryProjectCard = ({user, project, onEditProject, onDeleteProject}) => {
  const history = useHistory();
  
  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  return (
    <div className={classNames(Styles.projectCard)}>
      <div className={Styles.cardHead}>
        <div className={classNames(Styles.cardHeadInfo)}>
          <div
            className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
            onClick={() => {history.push(`/project/${project.id}`)}}
          >
            {project.name}
          </div>
        </div>
      </div>
      <hr />
      <div className={Styles.cardBodySection}>
        <div>
          {user.id === project?.createdBy?.id &&
            <div>
              <div>Data Lakehouse Link</div>
              <div>
                <a href={`${project?.dataLakeDetails?.link}`} target='_blank' rel='noopener noreferrer'>
                  Access Lakehouse
                  <i className={classNames('icon mbc-icon new-tab')} />
                </a>
              </div>
            </div>
          }
          <div>
            <div>Created on</div>
            <div>{regionalDateAndTimeConversionSolution(formatDateToISO(new Date(project?.createdOn)))}</div>
          </div>
          <div>
            <div>Classification</div>
            <div>{project?.dataClassification || 'N/A'}</div>
          </div>
          <div>
            <div>State</div>
            <div>{project?.state}</div>
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
              onClick={() => onEditProject(project)}
            >
              <i className="icon mbc-icon edit"></i>
              <span>Edit</span>
            </button>
            
            <button
              className={'btn btn-primary'}
              type="button"
              onClick={() => onDeleteProject(project)}
            >
              <i className="icon delete"></i>
              <span>Delete</span>
            </button>
          </div>
        </>
      </div>
    </div>
  );
};
export default DataEntryProjectCard;