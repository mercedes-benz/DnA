import cn from 'classnames';
import React from 'react';
import { Envs } from 'globals/Envs';
import { getDateFromTimestampForDifference, getDateDifferenceFromToday } from '../../../../services/utils';
import { IDataiku } from 'globals/types';
import { history } from '../../../../router/History';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import Styles from './ProjectListRowItem.scss';

const classNames = cn.bind(Styles);

export interface ISolutionListRowItemProps {
  project: IDataiku;
  projectId: string;
  isProduction: boolean;
  openProvisionModal?: (project: IDataiku) => void;
  openDetailsModal: (project: IDataiku, isProduction: boolean) => void;
}

const ProjectListRowItem = (props: ISolutionListRowItemProps) => {
  const onProvisionBtnClick = (e: React.FormEvent<HTMLElement>) => {
    e.stopPropagation();
    props.openProvisionModal(props.project);
  };

  const onInfoBtnClick = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    props.openDetailsModal(props.project, props.isProduction);
  };

  const openProject = (event: React.FormEvent<HTMLElement>, isProduction: boolean, projectId: string) => {
    event.stopPropagation();
    const baseUrl = isProduction ? Envs.DATAIKU_LIVE_APP_URL : Envs.DATAIKU_TRAINING_APP_URL;
    window.open(baseUrl + '/projects/' + projectId + '/');
  };

  const goToSolution = (solutionId: string) => {
    Tooltip.clear();
    history.push('/summary/' + solutionId);
  };

  return (
    <React.Fragment>
      <tr
        id={props.project.projectKey}
        key={props.project.projectKey}
        className={classNames('data-row')}
        onClick={onInfoBtnClick}
      >
        <td className="wrap-text projectName">{props.project.name}</td>
        <td className="wrap-text">
          <span className={Styles.descriptionColumn}>{props.project.shortDesc}</span>
        </td>
        {props.isProduction ? (
          <td className="wrap-text">{props.project.role ? props.project.role.toLowerCase() : ''}</td>
        ) : (
          ''
        )}
        <td className="wrap-text">
          {getDateDifferenceFromToday(getDateFromTimestampForDifference(props?.project?.versionTag?.lastModifiedOn))}{' '}
          days ago
        </td>
        <td className={Styles.iconAction}>
          {props.isProduction ? (
            <span id={'provision' + props.project.projectKey}>
              {props.project.solutionId ? (
                <i
                  tooltip-data={'Go to Solution'}
                  onClick={(event: React.FormEvent<HTMLSpanElement>) => goToSolution(props.project.solutionId)}
                  className={'icon mbc-icon solutions '}
                />
              ) : (
                <i
                  className="icon mbc-icon provision"
                  tooltip-data={'Provision to Solution'}
                  onClick={onProvisionBtnClick}
                />
              )}
            </span>
          ) : (
            ''
          )}
          <i
            className={classNames('icon mbc-icon new-tab', Styles.OpenNewTabIcon)}
            tooltip-data={'Open in New Tab'}
            onClick={(event: React.FormEvent<HTMLElement>) =>
              openProject(event, props.isProduction, props.project.projectKey)
            }
          />
        </td>
      </tr>
    </React.Fragment>
  );
};

export default ProjectListRowItem;
