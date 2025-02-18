import React from 'react';
import classNames from 'classnames';
import Styles from './code-space-group-card.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const CodeSpaceGroupCard = ({ group, onShowCodeSpacesModal, onShowCodeSpaceGroupModal, onCodeSpaceGroupDeleteModal, onCodeSpaceDropped }) => {
  const handleEditGroup = (codespace) => {
    const data = {
      groupId: group?.groupId,
      name: group?.name,
      order: 0,
      wsAdded: [{ name: codespace?.projectDetails?.projectName, order: 0, wsId: codespace?.workspaceId }],
      wsRemoved: []
    }
    ProgressIndicator.show();
    CodeSpaceApiClient.editCodeSpaceGroup(data)
      .then(() => {
        Notification.show(`Code Space added successfully`);
        onCodeSpaceDropped();
        ProgressIndicator.hide();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Adding code space to group failed!',
          'alert',
        );
      });
  }

  return (
    <div
      className={classNames(Styles.group)}
      id={`group-${group?.id}`}
      onDrop={(e) => {
          e.preventDefault();
          handleEditGroup(JSON.parse(e.dataTransfer.getData("application/json")));
      }}
      onDragOver={(e) => {
          e.preventDefault();
          e.dataTransfer.dropEffect = "move";
      }}
    >
      <div className={classNames(Styles.groupHeader)}>
          <h2 onClick={() => onShowCodeSpacesModal(true, group)}>{group?.name}</h2>
      </div>
      {group?.warning &&
        <div className={classNames(Styles.groupWarning)}>
            <button className={classNames('btn btn-primary')} onClick={() => onShowCodeSpacesModal(true, group)}>
                <i className="icon mbc-icon alert circle"></i>
                Start failed for some code spaces, click to view
            </button>
        </div>
      }
      <div className={classNames(Styles.groupBody)}>
        {group?.workspaces?.slice(0, 3).map((workspace) => 
          <div key={workspace?.wsId} className={classNames(Styles.codeSpaceItem)} onClick={() => onShowCodeSpacesModal(true, group)}>
              <div>
                  <h3>{workspace?.name}</h3>
                  {/* ToDo
                      1. Add elements to show different codespace states
                  */}
                  {/* <a href="#" target="_blank" rel="noreferrer" tooltip-data="Deployed to Staging on 11/25/2024, 09:53:10">Deployed <i className="icon mbc-icon new-tab"></i></a> */}
              </div>
              {/* {!createInProgress && !creationFailed && !serverFailed && (
                <span
                  onClick={() => onStartStopCodeSpace(codeSpace)}
                  tooltip-data={(serverStarted ? 'Stop' : 'Start') + ' the Codespace'}
                  className={classNames(
                    Styles.statusIndicator,
                    Styles.wsStartStop,
                    serverStarted ? Styles.wsStarted : '',
                  )}
                >
                  {serverStarted ? 'Stop' : 'Start'}
                  {!serverStarted && serverProgress > 0 ? `ing... ${serverProgress}%` : ''}
                </span>
              )}
              {serverFailed && (
                <span
                  title={'Server Start Failed: Please contact Codespace Admin'}
                  className={classNames(Styles.statusIndicator, Styles.wsStartStop, Styles.wsStarted)}
                  onClick={() => onStartStopCodeSpace(codeSpace)}
                >
                  Start Failed
                </span>
              )} */}
          </div>
          )}
          <div className={Styles.btnContainer}>
            <button className={classNames('btn btn-primary')} onClick={() => onShowCodeSpaceGroupModal(true)}>
              <i className="icon mbc-icon plus"></i> Add Code Space
            </button>
            <button className={classNames('btn btn-primary')} onClick={() => onShowCodeSpacesModal(true, group)}>
              <i className="icon mbc-icon visibility-show"></i> View all
            </button>
            <button className={classNames('btn btn-primary')} onClick={() => onCodeSpaceGroupDeleteModal(true, group)}>
              <i className="icon delete"></i> Delete
            </button>
          </div>
      </div>
  </div>
  )
}

export default CodeSpaceGroupCard;