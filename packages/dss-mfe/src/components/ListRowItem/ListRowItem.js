import cn from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './ListRowItem.scss';

import { history } from '../../store';

import Tooltip from '../../common/modules/uilab/js/src/tooltip';

import { Envs } from '../../Utility/envs';
import { getDateFromTimestampForDifference, getDateDifferenceFromToday } from '../../Utility/utils';

const classNames = cn.bind(Styles);
let isTouch = false;

const ProjectListRowItem = (props) => {
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [showLocationsContextMenu, setShowLocationsContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState(0);

  const toggleContextMenu = (e) => {
    e.stopPropagation();
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 10);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 200);
    setShowLocationsContextMenu(false);
    setShowContextMenu(!showContextMenu);
  };

  const handleContextMenuOutside = (event) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }

    const target = event.target;
    const elemClasses = target.classList;
    const cardDivElement = document.querySelector('#card-' + props.project.projectKey);
    const contextMenuWrapper = cardDivElement?.querySelector('.contextMenuWrapper');
    const locationContextMenuWrapper = cardDivElement?.querySelector('.contextMenuWrapper');

    if (
      cardDivElement &&
      !target?.classList?.contains('trigger') &&
      !target?.classList?.contains('context') &&
      !target?.classList?.contains('contextList') &&
      !target?.classList?.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper?.contains(target) === false &&
      (showContextMenu || showLocationsContextMenu)
    ) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    } else if (cardDivElement?.contains(target) === false) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    }

    if (!contextMenuWrapper?.contains(target)) {
      setShowContextMenu(false);
    }

    if (!locationContextMenuWrapper?.contains(target)) {
      setShowLocationsContextMenu(false);
    }

    if (
      (showContextMenu || showLocationsContextMenu) &&
      (elemClasses?.contains('contextList') ||
        elemClasses?.contains('contextListItem') ||
        elemClasses?.contains('contextMenuWrapper') ||
        elemClasses?.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };

  useEffect(() => {
    Tooltip.defaultSetup();
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);

    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const onEditBtnClick = (e) => {
    e.stopPropagation();
    props.editDataikuProjectDetail(props.project);
  };

  const deleteDataikuProject = (projectId) => {
    return () => {
      setShowContextMenu(!showContextMenu);
      props.deleteDataikuProject(projectId);
    };
  };
  const onProvisionBtnClick = (e) => {
    e.stopPropagation();
    props.openProvisionModal(props.project);
  };

  const onInfoBtnClick = (e) => {
    e.stopPropagation();
    props.openDetailsModal(props.project, props.isProduction);
  };

  const openProject = (event, isProduction, projectId) => {
    event.stopPropagation();
    const baseUrl = isProduction ? Envs.DATAIKU_LIVE_APP_URL : Envs.DATAIKU_TRAINING_APP_URL;
    window.open(baseUrl + '/projects/' + projectId + '/');
  };

  const goToSolution = (solutionId) => {
    Tooltip.clear();
    history.push('/summary/' + solutionId);
  };

  if (props.isDnaProject) {
    return (
      <React.Fragment>
        <tr
          id={props.project.projectKey}
          key={props.project.projectKey}
          className={classNames('data-row')}
        >
          <td className="wrap-text projectName" >{props.project.projectName}</td>
          <td className="wrap-text projectName" >{props.project.cloudProfile}</td>
          {props?.user?.id === props?.project?.createdBy ? <td id={'card-' + props.project.projectKey} key={props.project.projectKey} className={Styles.actionMenus}>
            <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
              <span
                tooltip-data="More Action"
                onClick={toggleContextMenu}
                className={classNames('trigger', Styles.contextMenuTrigger)}
              >
                <i className="icon mbc-icon listItem context" />
              </span>
              <div
                style={{
                  top: contextMenuOffsetTop + 'px',
                  left: contextMenuOffsetLeft + 'px',
                }}
                className={classNames('contextMenuWrapper', showContextMenu ? Styles.showMenu : 'hide')}
              >
                <ul className={classNames('contextList', Styles.contextList)}>
                  <li className="contextListItem" onClick={onEditBtnClick}>
                    <span>Edit</span>
                  </li>
                  <li className="contextListItem" onClick={deleteDataikuProject(props.project.id)}>
                    <span>Delete</span>
                  </li>
                </ul>
              </div>
            </div>
          </td> : <td></td>}
        </tr>
      </React.Fragment>
    )
  }

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
                  onClick={() => goToSolution(props.project.solutionId)}
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
            onClick={(event) => openProject(event, props.isProduction, props.project.projectKey)}
          />
        </td>
      </tr>
    </React.Fragment>
  );
};

export default ProjectListRowItem;
