import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';

// @ts-ignore
import { Envs } from '../../../globals/Envs';
import { IUserInfo } from '../../../globals/types';
import { history } from '../../../router/History';
import { trackEvent } from '../../../services/utils';
import { ApiClient } from '../../../services/ApiClient';
import Modal from '../../formElements/modal/Modal';
import Styles from './CodeSpace.scss';
import FullScreenModeIcon from '../../icons/fullScreenMode/FullScreenModeIcon';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { useState } from 'react';
import { useEffect } from 'react';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
import ProgressWithMessage from '../../../components/progressWithMessage/ProgressWithMessage';
// import { HTTP_METHOD } from '../../../globals/constants';


export interface ICodeSpaceProps {
  user: IUserInfo;
}

export interface ICodeSpaceData {
  id?: string;
  name?: string;
  recipe?: string;
  environment?: string;
  deployed?: boolean;
  createdDate?: string;
  lastDeployedDate?: string;
  url: string;
  running: boolean;
}

const CodeSpace = (props: ICodeSpaceProps) => {
  const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
    url: `https://code-spaces.dev.dna.app.corpintra.net/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
    running: false
  });
  const [fullScreenMode, setFullScreenMode] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState<boolean>(false);
  const [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false);
  const [showCodeDeployModal, setShowCodeDeployModal] = useState<boolean>(false);

  useEffect(() => {
    ApiClient.getCodeSpace().then((res: any) => {
      setLoading(false);
      const codeSpaceRunning = (res.success === 'true');
      setCodeSpaceData({
        ...codeSpaceData,
        running: codeSpaceRunning
      });
      setShowNewCodeSpaceModal(!codeSpaceRunning);
    }).catch((err: Error) => {
      Notification.show("Error in validating code space - " + err.message, 'alert');
    });
  }, [])

  const toggleFullScreenMode = () => {
    setFullScreenMode(!fullScreenMode);
    trackEvent(
      'DnA Code Space',
      'View Mode',
      'Code Space iframe view changed to ' + (fullScreenMode ? 'Full Screen Mode' : 'Normal Mode'),
    );
  };

  const openInNewtab = () => {
    window.open(codeSpaceData.url, '_blank');
    trackEvent('DnA Code Space', 'Code Space Open', 'Open in New Tab');
  };

  const isCodeSpaceCreationSuccess = (status: boolean, codeSpaceData: ICodeSpaceData) => {
    setShowNewCodeSpaceModal(!status);
    setCodeSpaceData(codeSpaceData);
    Tooltip.defaultSetup();
  }

  const toggleProgressMessage = (show: boolean) => {
    setIsApiCallTakeTime(show);
  }

  const onNewCodeSpaceModalCancel = () => {
    setShowNewCodeSpaceModal(false);
    history.goBack();
  }

  const onShowCodeDeployModal = () => {
    setShowCodeDeployModal(true);
  }

  const onCodeDeployModalCancel = () => {
    setShowCodeDeployModal(false);
  }

  const onAcceptCodeDeploy =() => {
    Notification.show('This feature will be enabled soon.', 'warning');
  }

  return (
    <div className={fullScreenMode ? Styles.codeSpaceWrapperFSmode : '' + ' ' + Styles.codeSpaceWrapper}>
      {codeSpaceData.running && (
        <React.Fragment>
          <div className={Styles.nbheader}>
            <div className={Styles.headerdetails}>
              <img src={Envs.DNA_BRAND_LOGO_URL} className={Styles.Logo} />
              <div className={Styles.nbtitle}>
                <h2>{props.user.firstName}&apos;s Code Space</h2>
              </div>
            </div>
            <div className={Styles.navigation}>
              {codeSpaceData.running && (
                <div className={Styles.headerright}>
                  <div>
                    <button className="btn btn-secondary" onClick={onShowCodeDeployModal}>(Re)Deploy</button>
                  </div>
                  <div tooltip-data="Open New Tab" className={Styles.OpenNewTab} onClick={openInNewtab}>
                    <i className="icon mbc-icon arrow small right" />
                    <span> &nbsp; </span>
                  </div>
                  <div onClick={toggleFullScreenMode}>
                    <FullScreenModeIcon fsNeed={fullScreenMode} />
                  </div>
                </div>
              )}
            </div>
          </div>
          <div className={Styles.codeSpaceContent}>
            {
              <div className={Styles.codeSpace}>
                {loading ? (
                  <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
                    <div className="progress infinite" />
                  </div>
                ) : (
                  codeSpaceData.running && (
                    <div className={Styles.codespaceframe}>
                      <iframe
                        className={fullScreenMode ? Styles.fullscreen : ''}
                        src={codeSpaceData.url}
                        title="Code Space"
                      />
                    </div>
                  )
                )}
              </div>
            }
          </div>
        </React.Fragment>
      )}
      {!codeSpaceData.running && showNewCodeSpaceModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="800px"
          buttonAlignment="right"
          show={showNewCodeSpaceModal}
          content={
            <NewCodeSpace
              user={props.user}
              isCodeSpaceCreationSuccess={isCodeSpaceCreationSuccess}
              toggleProgressMessage={toggleProgressMessage}
            />
          }
          scrollableContent={true}
          onCancel={onNewCodeSpaceModalCancel}
        />
      )}
      {showCodeDeployModal && (
        <Modal
          title={'Deploy Code (Coming Soon)'}
          showAcceptButton={true}
          acceptButtonTitle={'Deploy'}
          cancelButtonTitle={'Cancel'}
          onAccept={onAcceptCodeDeploy}
          showCancelButton={true}
          modalWidth="500px"
          buttonAlignment="center"
          show={showCodeDeployModal}
          content={
            <p>The code from your workspace will be deployed and is run in a container and you will get the access url after the deployment.</p>
          }
          scrollableContent={false}
          onCancel={onCodeDeployModalCancel}
        />
      )}
      {isApiCallTakeTime && <ProgressWithMessage message={'Please wait as this process can take up to a minute....'} />}
    </div>
  );
};

export default CodeSpace;
