import React, { useState } from 'react';
import classNames from 'classnames';
import Styles from './code-space-group-card.scss';
import { trackEvent } from '../../Utility/utils';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const CodeSpaceGCard = ({ codeSpace, userInfo, onStartStopCodeSpace }) => {
  
  const enableOnboard = codeSpace ? codeSpace.status === 'COLLABORATION_REQUESTED' : false;
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const creationFailed = codeSpace.status === 'CREATE_FAILED';

  const [serverStarted, setServerStarted] = useState(false);
  const [serverFailed, setServerFailed] = useState(false);
  const [serverProgress, setServerProgress] = useState(0);

  const onStartStopCodeSpaceLocal = (codespace) => {
    if(codespace?.cloudServiceProvider ==='DHC-CaaS-AWS'){
      onStartStopCodeSpace(codespace, handleServerStatusAndProgress, 'DHC-CaaS-AWS');
    }
    else{
      codespace.serverStatus === 'SERVER_STARTED' ? onStartStopCodeSpace(codespace, handleServerStatusAndProgress, 'DHC-CaaS') : 'Not started';
    }
  };

  const handleServerStatusAndProgress = () => {
    codeSpace.serverStatus = 'SERVER_STOPPED';
    const env = codeSpace?.cloudServiceProvider === 'DHC-CaaS-AWS' ? 'DHC-CaaS-AWS' : 'DHC-CaaS';
    CodeSpaceApiClient.serverStatusFromHub(env, userInfo.id.toLowerCase(), codeSpace.wsId, (e) => {
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

  return (
    <div key={codeSpace?.wsId} className={classNames(Styles.codeSpaceItem)}>
      <div>
          <h3>
            {codeSpace?.name}
            {!enableOnboard && !creationFailed && serverStarted && (
              <a
                className={Styles.csOpenNewTab}
                tooltip-data="Open workspace in new tab"
                onClick={(e) => {
                  e.stopPropagation();
                  window.open(codeSpace?.workspaceUrl, '_blank');
                  trackEvent('DnA Code Space', 'Code Space Open', 'Open in New Tab');
                }}
              >
                <i className="icon mbc-icon arrow small right" />
              </a>
            )}
          </h3>
          <p className={Styles.workspaceType}>{codeSpace?.projectOwner?.id === userInfo.id ? 'Own' : 'Shared'}</p>
      </div>
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
  )
}

export default CodeSpaceGCard;