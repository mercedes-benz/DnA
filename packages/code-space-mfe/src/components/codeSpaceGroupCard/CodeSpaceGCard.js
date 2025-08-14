import React, { useState, useEffect, useRef } from 'react';
import { useHistory } from 'react-router-dom';
import classNames from 'classnames';
import Styles from './code-space-group-card.scss';
import {
  trackEvent
} from '../../Utility/utils';
import { setRippleAnimation } from '../../common/modules/uilab/js/src/util';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import ContextMenu from '../contextMenu/ContextMenu';

const CodeSpaceGCard = ({ codeSpace, userInfo, onStartStopCodeSpace, onShowDeployModal, onShowCodeSpaceOnBoard, onShowBlueprintModal, onShowBuildModal, onGetCodespaceData }) => {
  const history = useHistory();
  const enableOnboard = codeSpace ? codeSpace.status === 'COLLABORATION_REQUESTED' : false;
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const creationFailed = codeSpace.status === 'CREATE_FAILED';

  const [serverStarted, setServerStarted] = useState(false);
  const [serverFailed, setServerFailed] = useState(false);
  const [serverProgress, setServerProgress] = useState(0);

  useEffect(() => {
      handleServerStatusAndProgress();
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const onStartStopCodeSpaceLocal = (codespace) => {
    if(codespace?.projectDetails?.recipeDetails?.cloudServiceProvider ==='DHC-CaaS-AWS'){
      onStartStopCodeSpace(codespace, handleServerStatusAndProgress, 'DHC-CaaS-AWS');
    }
    else{
      codespace.serverStatus === 'SERVER_STARTED' ? onStartStopCodeSpace(codespace, handleServerStatusAndProgress, 'DHC-CaaS') : 'Not started';
    }
  };

  const handleServerStatusAndProgress = () => {
    codeSpace.serverStatus = 'SERVER_STOPPED';
    const env = codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' ? 'DHC-CaaS-AWS' : 'DHC-CaaS';
    CodeSpaceApiClient.serverStatusFromHub(env, userInfo.id.toLowerCase(), codeSpace.workspaceId, (e) => {
      const data = JSON.parse(e.data);
      if (data.progress === 100 && data.ready) {
        setServerProgress(100);
        setTimeout(() => {
          setServerStarted(true);
          codeSpace.serverStatus = 'SERVER_STARTED';
        }, 300);
      } else if(!data.failed) {
        setServerProgress(data.progress);
      } else if(data.progress === 100 && data.failed) {
        setServerFailed(true);
      }
      console.log(JSON.parse(e.data));
    });
  };

  const disableDeployment = !codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled;

  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState(0);
  const stagingWrapperRef = useRef(null);
  const prodWrapperRef = useRef(null);

  const toggleContextMenu = (e) => {
    e.stopPropagation();
    setRippleAnimation(prodWrapperRef.current);
    setRippleAnimation(stagingWrapperRef.current);
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 17);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 230);
    setShowContextMenu(!showContextMenu);
  };

  const onCardNameClick = () => {
    if (enableOnboard) {
      onShowCodeSpaceOnBoard(codeSpace);
    } else if (!serverStarted) {
      onStartStopCodeSpaceLocal(codeSpace);
    } else {
      history.push(`codespace/${codeSpace.workspaceId}`);
    }
  };

  return (
    <>
      <div key={codeSpace?.workspaceId} className={classNames(Styles.codeSpaceItem)}>
        <div>
            <div className={Styles.flexDisplay}>
              <h3 onClick={onCardNameClick}>
                {codeSpace?.projectDetails?.projectName}
              </h3>
              {!enableOnboard && !creationFailed && serverStarted && (
                <a
                  className={Styles.csOpenNewTab}
                  tooltip-data="Open workspace in new tab"
                  onClick={() => {
                    window.open(codeSpace?.workspaceUrl, '_blank');
                    trackEvent('DnA Code Space', 'Code Space Open', 'Open in New Tab');
                  }}
                >
                  <i className="icon mbc-icon new-tab" />
                </a>
              )}
            </div>
            <p className={Styles.workspaceType}>{codeSpace?.projectOwner?.id === userInfo.id ? 'Own' : 'Shared'}</p>
        </div>
        <div className={Styles.flexDisplay}>
          <div>
            {!createInProgress && !creationFailed && !serverFailed && (
              <span
                onClick={() => onStartStopCodeSpaceLocal(codeSpace)}
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
                onClick={() => onStartStopCodeSpaceLocal(codeSpace)}
              >
                Start Failed
              </span>
            )}
          </div>
          {!enableOnboard && !creationFailed && !createInProgress && !disableDeployment && (
            <div>
              <span
                onClick={toggleContextMenu}
                className={classNames(Styles.trigger, showContextMenu ? Styles.open : '')}
              >
                <i className="icon mbc-icon listItem context" />
              </span>
              <ContextMenu
                  codeSpace={codeSpace}
                  userInfo={userInfo}
                  showContextMenu={showContextMenu}
                  setShowContextMenu = {(val) => {setShowContextMenu(val);}}
                  // toggleContextMenu={toggleContextMenu}
                  contextMenuOffsetTop={contextMenuOffsetTop}
                  contextMenuOffsetLeft={contextMenuOffsetLeft}
                  stagingWrapperRef={stagingWrapperRef}
                  prodWrapperRef={prodWrapperRef}
                  onShowDeployModal={onShowDeployModal}
                  serverStarted={serverStarted}
                  onStartStopCodeSpace={onStartStopCodeSpace}
                  handleServerStatusAndProgress={handleServerStatusAndProgress}
                  onShowBlueprintModal={onShowBlueprintModal}
                  onShowBuildModal={onShowBuildModal}
                  onGetCodespaceData={onGetCodespaceData}
              />
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default CodeSpaceGCard;