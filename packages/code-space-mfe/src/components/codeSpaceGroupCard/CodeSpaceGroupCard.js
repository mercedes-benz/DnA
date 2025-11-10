import React, { useState, useEffect } from 'react';
import classNames from 'classnames';
import Styles from './code-space-group-card.scss';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import CodeSpaceGCard from './CodeSpaceGCard';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const CodeSpaceGroupCard = ({ group, userInfo, onStartStopCodeSpace, onShowDeployModal, onShowCodeSpaceOnBoard, onShowCodeSpacesModal, onShowCodeSpaceGroupModal, onCodeSpaceGroupDeleteModal, onCodeSpaceDropped, onShowBlueprintModal, onShowBuildModal, isAiGroupModal=false }) => {
  const [highlight, setHighlight] = useState(false);

  const hostWorkspace = group?.workspaces?.find(
  ws => ws?.projectDetails?.projectName?.endsWith('-host')
  );
  const hostProdDeployedUrl = hostWorkspace?.projectDetails?.prodDeploymentDetails?.deploymentUrl;

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);// eslint-disable-line react-hooks/exhaustive-deps

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
        <h2 onClick={() => {onShowCodeSpacesModal(true, group); isAiGroupModal ? sessionStorage.setItem(SESSION_STORAGE_KEYS.AI_CODE_SPACE_SELECTED_GROUPS,JSON.stringify(group)) : sessionStorage.setItem(SESSION_STORAGE_KEYS.CODE_SPACE_SELECTED_GROUPS,JSON.stringify(group));}} style={{ margin: 0 }}>
          {group?.name} {!isAiGroupModal && `(${group?.workspaces?.length || 0})`}
        </h2>
        {isAiGroupModal && hostProdDeployedUrl && (
          <a
            href={hostProdDeployedUrl}
            target="_blank"
            rel="noreferrer"
            tooltip-data="Open deployed host app"
            className={Styles.aiGroup}
          >
            <i className="icon mbc-icon new-tab" />
          </a>
        )}
      </div>
      {group?.warning &&
        <div className={classNames(Styles.groupWarning)}>
          <button className={classNames('btn btn-primary')} onClick={() => {onShowCodeSpacesModal(true, group); isAiGroupModal ? sessionStorage.setItem(SESSION_STORAGE_KEYS.AI_CODE_SPACE_SELECTED_GROUPS,JSON.stringify(group)) : sessionStorage.setItem(SESSION_STORAGE_KEYS.CODE_SPACE_SELECTED_GROUPS,JSON.stringify(group));}}>
            <i className="icon mbc-icon alert circle"></i>
            Start failed for some code spaces, click to view
          </button>
        </div>
      }
      <div className={classNames(Styles.groupBody)}>
        <div className={Styles.cardListContainer}>
          {group?.workspaces?.map((workspace) =>
            <CodeSpaceGCard key={workspace?.workspaceId} codeSpace={workspace} userInfo={userInfo} onStartStopCodeSpace={onStartStopCodeSpace} onShowDeployModal={onShowDeployModal} onShowCodeSpaceOnBoard={onShowCodeSpaceOnBoard} onShowBlueprintModal={onShowBlueprintModal} onShowBuildModal={onShowBuildModal} />
          )}
        </div>
        <div className={Styles.btnContainer}>
          {!isAiGroupModal && (<button className={classNames('btn btn-primary')} onClick={() => onShowCodeSpaceGroupModal(true)}>
            <i className="icon mbc-icon plus"></i> Add Code Space
          </button>)}
          <button className={classNames('btn btn-primary')} onClick={() => {onShowCodeSpacesModal(true, group); isAiGroupModal ? sessionStorage.setItem(SESSION_STORAGE_KEYS.AI_CODE_SPACE_SELECTED_GROUPS,JSON.stringify(group)) : sessionStorage.setItem(SESSION_STORAGE_KEYS.CODE_SPACE_SELECTED_GROUPS,JSON.stringify(group));}}>
            <i className="icon mbc-icon visibility-show"></i> View all
          </button>
          {!isAiGroupModal && <button className={classNames('btn btn-primary')} onClick={() => onCodeSpaceGroupDeleteModal(true, group)}>
            <i className="icon delete"></i> Delete
          </button>}
        </div>
      </div>
    </div>
  )
}

export default CodeSpaceGroupCard;