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
// import { ApiClient } from '../../../services/ApiClient';
import Modal from '../../formElements/modal/Modal';
import Styles from './CodeSpace.scss';
import FullScreenModeIcon from '../../icons/fullScreenMode/FullScreenModeIcon';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { useState } from 'react';
import { useEffect } from 'react';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
import ProgressWithMessage from '../../../components/progressWithMessage/ProgressWithMessage';
import { CodeSpaceApiClient } from '../../../services/CodeSpaceApiClient';
import { getParams } from '../../../router/RouterUtils';
import classNames from 'classnames';
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
  deployedUrl?: string;
  createdDate?: string;
  lastDeployedDate?: string;
  url: string;
  running: boolean;
  status?: string;
}

const CodeSpace = (props: ICodeSpaceProps) => {
  // const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
  //   url: `https://code-spaces.dev.dna.app.corpintra.net/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
  //   running: false
  // });
  const { id } = getParams();
  const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
    url: undefined,
    running: false
  });
  const [fullScreenMode, setFullScreenMode] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState<boolean>(false);
  const [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false);
  const [showCodeDeployModal, setShowCodeDeployModal] = useState<boolean>(false);
  const [codeDeploying, setCodeDeploying] = useState<boolean>(false);
  const [codeDeployed, setCodeDeployed] = useState<boolean>(false);
  const [codeDeployedUrl, setCodeDeployedUrl] = useState<string>();
  const [acceptContinueCodingOnDeployment, setAcceptContinueCodingOnDeployment] = useState<boolean>();

  useEffect(() => {
    CodeSpaceApiClient.getCodeSpaceStatus(id).then((res: any) => {
      setLoading(false);
      const status = res.status;
      if (
        status !== 'CREATE_REQUESTED' &&
        status !== 'CREATE_FAILED' &&
        status !== 'DELETE_REQUESTED' &&
        status !== 'DELETED' &&
        status !== 'DELETE_FAILED'
      ) {
        const deployed = res.status === 'DEPLOYED';
        const deployedUrl = res.deploymentUrl;
        setCodeSpaceData({
          id: res.id,
          name: res.name,
          recipe:
            res.recipeId !== 'default'
              ? `Microservice using Spring Boot (${res.operatingSystem}, ${res.ramSize}${res.ramMetrics} RAM, ${res.cpuCapacity}CPU)`
              : 'Default',
          environment: res.cloudServiceProvider,
          deployed: deployed,
          deployedUrl: deployedUrl,
          createdDate: res.intiatedOn,
          lastDeployedDate: res.lastDeployedOn,
          url: res.workspaceUrl,
          running: !!res.intiatedOn,
          status: res.status,
        });
        setCodeDeployed(deployed);
        setCodeDeployedUrl(deployedUrl);
        Tooltip.defaultSetup();
        if (res.status === 'DEPLOY_REQUESTED') {
          setCodeDeploying(true);
          enableDeployLivelinessCheck(res.name);
        }
      } else {
        Notification.show(`Code space ${res.name} is getting created. Please try again later.`, 'warning');
      }
    }).catch((err: Error) => {
      Notification.show("Error in validating code space - " + err.message, 'alert');
    });
    // ApiClient.getCodeSpace().then((res: any) => {
    //   setLoading(false);
    //   const codeSpaceRunning = (res.success === 'true');
    //   setCodeSpaceData({
    //     ...codeSpaceData,
    //     running: codeSpaceRunning
    //   });
    //   setShowNewCodeSpaceModal(!codeSpaceRunning);
    // }).catch((err: Error) => {
    //   Notification.show("Error in validating code space - " + err.message, 'alert');
    // });
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
    clearInterval(livelinessInterval);
    Tooltip.clear();
    history.goBack();
  }

  const onShowCodeDeployModal = () => {
    setShowCodeDeployModal(true);
  }

  const onCodeDeployModalCancel = () => {
    setShowCodeDeployModal(false);
  }

  let livelinessInterval: any = undefined;
  const enableDeployLivelinessCheck = (name: string) => {
    clearInterval(livelinessInterval);
    livelinessInterval = setInterval(() => {
      CodeSpaceApiClient.getCodeSpaceStatus(name)
        .then((res:any) => {
          if (res.status === 'DEPLOYED') {
            setIsApiCallTakeTime(false);
            ProgressIndicator.hide();
            clearInterval(livelinessInterval);
            // setCodeSpaceData({
            //   ...codeSpaceData,
            //   deployed: true,
            //   deployedUrl: res.deployedUrl,
            //   lastDeployedDate: res.lastDeployedOn
            // });
            setCodeDeployed(true);
            setCodeDeploying(false);
            setCodeDeployedUrl(res.deploymentUrl);
            Tooltip.defaultSetup();
            setShowCodeDeployModal(false);
            Notification.show(`Code from code space ${res.name} succesfully deployed.`);
          }
        })
        .catch((err: Error) => {
          clearInterval(livelinessInterval);
          setIsApiCallTakeTime(false);
          ProgressIndicator.hide();
          Notification.show('Error in validating code space deployment - ' + err.message, 'alert');
        });
    }, 2000);
  };

  const onAcceptCodeDeploy = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deployCodeSpace(codeSpaceData.id).then((res: any) => {
      trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
      if(res.success === 'SUCCESS') {
        // setCreatedCodeSpaceName(res.data.name);
        setCodeDeploying(true);
        if (acceptContinueCodingOnDeployment) {
          ProgressIndicator.hide();
          Notification.show(`Code space '${codeSpaceData.name}' deployment successfully started. Please check the status later.`);
          setShowCodeDeployModal(false);
        } else {
          setIsApiCallTakeTime(true);
        }
        enableDeployLivelinessCheck(codeSpaceData.name);
      } else {
        setIsApiCallTakeTime(false);
        ProgressIndicator.hide();
        Notification.show('Error in deploying code space. Please try again later.\n' + res.errors[0].message, 'alert');
      }
    }).catch((err: Error) => {
      ProgressIndicator.hide();
      Notification.show('Error in deploying code space. Please try again later.\n' + err.message, 'alert');
    });
  };

  const goBack = () => {
    clearInterval(livelinessInterval);
    Tooltip.clear();
    history.goBack();
  };

  const onAcceptContinueCodingOnDeployment = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAcceptContinueCodingOnDeployment(e.target.checked);
  };

  return (
    <div className={fullScreenMode ? Styles.codeSpaceWrapperFSmode : '' + ' ' + Styles.codeSpaceWrapper}>
      {codeSpaceData.running && (
        <React.Fragment>
          <div className={Styles.nbheader}>
            <div className={Styles.headerdetails}>
              <img src={Envs.DNA_BRAND_LOGO_URL} className={Styles.Logo} />
              <div className={Styles.nbtitle}>
                <button tooltip-data="Go Back" className="btn btn-text back arrow" onClick={goBack}></button>
                <h2>
                  {props.user.firstName}&apos;s Code Space - {codeSpaceData.name}
                </h2>
              </div>
            </div>
            <div className={Styles.navigation}>
              {codeSpaceData.running && (
                <div className={Styles.headerright}>
                  {codeDeployed && (
                    <div className={Styles.urlLink} tooltip-data="API BASE URL">
                      <a href={codeDeployedUrl} target="_blank" rel="noreferrer">
                        <i className="icon mbc-icon link" />
                      </a>
                    </div>
                  )}
                  <div>
                    <button className={classNames('btn btn-secondary', codeDeploying ? 'disable' : '')} onClick={onShowCodeDeployModal}>
                      {codeDeployed && '(Re)'}Deploy{codeDeploying && 'ing...'}
                    </button>
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
          title={'Deploy Code'}
          showAcceptButton={true}
          acceptButtonTitle={'Deploy'}
          cancelButtonTitle={'Cancel'}
          onAccept={onAcceptCodeDeploy}
          showCancelButton={true}
          modalWidth="500px"
          buttonAlignment="center"
          show={showCodeDeployModal}
          content={
            <>
              <p>
                The code from your workspace will be deployed and is run in a container and you will get the access url
                after the deployment.
              </p>
              <div>
                <label className="checkbox">
                  <span className="wrapper">
                    <input
                      type="checkbox"
                      className="ff-only"
                      checked={acceptContinueCodingOnDeployment}
                      onChange={onAcceptContinueCodingOnDeployment}
                    />
                  </span>
                  <span className="label">Continue with your workspace while the deployment is in progress?</span>
                </label>
              </div>
            </>
          }
          scrollableContent={false}
          onCancel={onCodeDeployModalCancel}
        />
      )}
      {isApiCallTakeTime && <ProgressWithMessage message={'Please wait as this process can take up 2 to 5 minutes....'} />}
    </div>
  );
};

export default CodeSpace;
