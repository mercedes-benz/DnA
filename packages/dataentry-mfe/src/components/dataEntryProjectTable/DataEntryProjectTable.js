import classNames from 'classnames';
import React from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './data-entry-project-table.scss';
import { regionalDateAndTimeConversionSolution, formatDateToISO } from '../../utilities/utils';

const DataEntryProjectTable = ({user, project, onEditProject, onDeleteProject}) => {
  const history = useHistory();

  const handleOpenProject = () => {
    history.push(`/project/${project.id}`);
    window.location.reload();
  }

  return (
    <div className={Styles.projectRow} onClick={handleOpenProject}>
      <div className={Styles.col1}>
        <span>
          {project?.name}
        </span>
      </div>
      <div className={Styles.col2}>
      {user.id === project?.createdBy?.id ? project?.dataLakeDetails?.link !== 'null' &&
        <a href={`${project?.dataLakeDetails?.link}`} target='_blank' rel='noopener noreferrer'>
          Access Data Lakehouse
          <i className={classNames('icon mbc-icon new-tab')} />
        </a> : 'N/A'}
      </div>
      <div className={Styles.col3}>
        {regionalDateAndTimeConversionSolution(formatDateToISO(new Date(project?.createdOn)))}
      </div>
      <div className={Styles.col4}>
        {project?.dataClassification}
      </div>
      <div className={Styles.col5}>
        <div className={Styles.btnTblGrp}>
          <button
            className={classNames('btn btn-primary', Styles.projectLink)}
            onClick={(e) => { e.stopPropagation(); onEditProject(project); }}
          >
            <i className="icon mbc-icon edit"></i>
            <span>Edit</span>
          </button>
          
          <button
            className={classNames('btn btn-primary', Styles.projectLink)}
            onClick={(e) => { e.stopPropagation(); onDeleteProject(project); }}
          >
            <i className="icon delete"></i>
            <span>Delete</span>
          </button>
        </div>
      </div>
    </div>
  );
}

export default DataEntryProjectTable;