import classNames from 'classnames';
import React from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './power-platform-workspace-table.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const PowerPlatformWorkspaceTable = ({project, onEditProject, onDeleteProject}) => {
  const history = useHistory();

  return (
    <div className={Styles.projectRow} onClick={() => {history.push(`/workspace/${project.id}`)}}>
      <div className={Styles.col1}>
        <span>
          {project?.name}
        </span>
      </div>
      <div className={Styles.col3}>
        {regionalDateAndTimeConversionSolution(project?.createdOn)}
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

export default PowerPlatformWorkspaceTable;