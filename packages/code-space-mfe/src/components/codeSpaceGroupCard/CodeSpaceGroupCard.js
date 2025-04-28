import React, { useState } from 'react';
import classNames from 'classnames';
import Styles from './code-space-group-card.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import CodeSpaceGCard from './CodeSpaceGCard';

const CodeSpaceGroupCard = ({ group, userInfo, onStartStopCodeSpace, onShowDeployModal, onShowCodeSpaceOnBoard, onShowCodeSpacesModal, onShowCodeSpaceGroupModal, onCodeSpaceGroupDeleteModal, onCodeSpaceDropped }) => {
  const [highlight, setHighlight] = useState(false);
  
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
      className={classNames(Styles.group, highlight && Styles.highlight)}
      id={`group-${group?.id}`}
      onDrop={(e) => {
          e.preventDefault();
          handleEditGroup(JSON.parse(e.dataTransfer.getData("application/json")));
          setHighlight(false);
      }}
      onDragOver={(e) => {
          e.preventDefault();
          setHighlight(true);
      }}
      onDragLeave={(e) => {
          e.preventDefault();
          setHighlight(false);
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
          <CodeSpaceGCard key={workspace?.workspaceId} codeSpace={workspace} userInfo={userInfo} onStartStopCodeSpace={onStartStopCodeSpace} onShowDeployModal={onShowDeployModal} onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard} />
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