import classNames from 'classnames';
import React, { useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import Styles from './fabric-workspace-row.scss';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import Spinner from '../spinner/Spinner';

const FabricWorkspaceRow = ({user, workspace, onSelectWorkspace, onEditWorkspace, onDeleteWorkspace}) => {
  const history = useHistory();

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const handleOpenWorkspace = () => {
    history.push(`/workspace/${workspace?.id}`);
  }

  return (
    <div className={Styles.projectRow} onClick={handleOpenWorkspace}>
      <div className={Styles.col1}>
        <span>
          {workspace?.name || 'null'}
        </span>
        {workspace?.status?.state === 'IN_PROGRESS' &&
          <button className={Styles.stateBtn} tooltip-data={'Click for more information'} onClick={(e) => { e.stopPropagation(); onSelectWorkspace(workspace) }}>
            <Spinner /> <span>&nbsp;</span>
          </button>
        }
        {workspace?.status?.state === 'COMPLETED' && 
          <button className={Styles.completedStatus}>
            <i className={'icon mbc-icon check circle'}></i> <span>Provisioned</span>
          </button>
        }
      </div>
      <div className={Styles.col2}>
        <a href={`https://app.fabric.microsoft.com/groups/${workspace.id}`} target='_blank' rel='noopener noreferrer'>
          Access Workspace
          <i className={classNames('icon mbc-icon new-tab')} />
        </a>
      </div>
      <div className={Styles.col3}>
        {workspace?.createdBy?.firstName} {workspace?.createdBy?.lastName}
      </div>
      <div className={Styles.col4}>
        {workspace?.createdOn && regionalDateAndTimeConversionSolution(workspace?.createdOn)}
      </div>
      <div className={Styles.col5}>
        {workspace?.dataClassification}
      </div>
      <div className={Styles.col6}>
        {user?.id === workspace?.createdBy?.id &&
          <div className={Styles.btnTblGrp}>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onEditWorkspace(workspace); }}
            >
              <i className="icon mbc-icon edit"></i>
              <span>Edit</span>
            </button>
            <button
              className={classNames('btn btn-primary', Styles.projectLink)}
              onClick={(e) => { e.stopPropagation(); onDeleteWorkspace(workspace); }}
            >
              <i className="icon delete"></i>
              <span>Delete</span>
            </button>
          </div>
        }
      </div>
    </div>
  );
}

export default FabricWorkspaceRow;