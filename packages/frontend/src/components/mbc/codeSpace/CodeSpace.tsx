import * as React from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import SelectBox from 'components/formElements/SelectBox/SelectBox';

// @ts-ignore
import { Envs } from 'globals/Envs';
import { ICodeCollaborator, IUserInfo } from 'globals/types';
import { history } from '../../../router/History';
import { trackEvent } from '../../../services/utils';
// import { ApiClient } from '../../../services/ApiClient';
import Modal from 'components/formElements/modal/Modal';
import Styles from './CodeSpace.scss';
import FullScreenModeIcon from 'components/icons/fullScreenMode/FullScreenModeIcon';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import { useState } from 'react';
import { useEffect } from 'react';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
import ProgressWithMessage from 'components/progressWithMessage/ProgressWithMessage';
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
  deployedBranch?: string;
  prodDeployed?: boolean;
  prodDeployedUrl?: string;
  prodDeployedBranch?: string;
  createdDate?: string;
  lastDeployedDate?: string;
  url: string;
  running: boolean;
  status?: string;
  collaborators?: ICodeCollaborator[];
}

const CodeSpace = (props: ICodeSpaceProps) => {
  // const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
  //   url: `https://code-spaces.***REMOVED***/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
  //   running: false
  // });
  const { id } = getParams();
  const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
    url: undefined,
    running: false,
  });
  const [fullScreenMode, setFullScreenMode] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState<boolean>(false);
  const [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false);
  const [showCodeDeployModal, setShowCodeDeployModal] = useState<boolean>(false);
  const [codeDeploying, setCodeDeploying] = useState<boolean>(false);
  const [codeDeployed, setCodeDeployed] = useState<boolean>(false);
  const [codeDeployedUrl, setCodeDeployedUrl] = useState<string>();
  const [codeDeployedBranch, setCodeDeployedBranch] = useState<string>('main');
  const [prodCodeDeployed, setProdCodeDeployed] = useState<boolean>(false);
  const [prodCodeDeployedUrl, setProdCodeDeployedUrl] = useState<string>();
  const [prodCodeDeployedBranch, setProdCodeDeployedBranch] = useState<string>('main');
  const [acceptContinueCodingOnDeployment, setAcceptContinueCodingOnDeployment] = useState<boolean>();
  const [livelinessInterval, setLivelinessInterval] = useState<NodeJS.Timer>();

  const livelinessIntervalRef = React.useRef<NodeJS.Timer>();

  const [branchValue, setBranchValue] = useState('main');
  const [deployEnvironment, setDeployEnvironment] = useState('staging');
  const branches = [
    { id: 'main', name: 'main' },
    { id: 'dev', name: 'dev' },
    { id: 'test', name: 'test' },
    { id: 'feature/code-space', name: 'feature/code-space' },
  ];

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [showCodeDeployModal]);

  useEffect(() => {
    CodeSpaceApiClient.getCodeSpaceStatus(id)
      .then((res: any) => {
        setLoading(false);
        const status = res.status;
        if (
          status !== 'CREATE_REQUESTED' &&
          status !== 'CREATE_FAILED' &&
          status !== 'DELETE_REQUESTED' &&
          status !== 'DELETED' &&
          status !== 'DELETE_FAILED'
        ) {
          const deployedUrl = res.deploymentUrl;
          const deployed = res.status === 'DEPLOYED' || deployedUrl;
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
          setCodeDeployedBranch(branchValue);
          Tooltip.defaultSetup();
          if (res.status === 'DEPLOY_REQUESTED') {
            setCodeDeploying(true);
            enableDeployLivelinessCheck(res.name);
          }
        } else {
          Notification.show(`Code space ${res.name} is getting created. Please try again later.`, 'warning');
        }
      })
      .catch((err: Error) => {
        Notification.show('Error in validating code space - ' + err.message, 'alert');
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
  }, []);

  useEffect(() => {
    livelinessIntervalRef.current = livelinessInterval;
    return () => {
      livelinessIntervalRef.current && clearInterval(livelinessIntervalRef.current);
    };
  }, [livelinessInterval]);

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
  };

  const toggleProgressMessage = (show: boolean) => {
    setIsApiCallTakeTime(show);
  };

  const onNewCodeSpaceModalCancel = () => {
    setShowNewCodeSpaceModal(false);
    clearInterval(livelinessInterval);
    Tooltip.clear();
    history.goBack();
  };

  const onShowCodeDeployModal = () => {
    setShowCodeDeployModal(true);
  };

  const onCodeDeployModalCancel = () => {
    setShowCodeDeployModal(false);
  };

  const enableDeployLivelinessCheck = (name: string) => {
    clearInterval(livelinessInterval);
    const intervalId = setInterval(() => {
      CodeSpaceApiClient.getCodeSpaceStatus(name)
        .then((res: any) => {
          try {
            if (res.status === 'DEPLOYED') {
              setIsApiCallTakeTime(false);
              ProgressIndicator.hide();
              clearInterval(livelinessIntervalRef.current);
              // setCodeSpaceData({
              //   ...codeSpaceData,
              //   deployed: true,
              //   deployedUrl: res.deployedUrl,
              //   lastDeployedDate: res.lastDeployedOn
              // });
              setCodeDeploying(false);
              if (deployEnvironment === 'staging') {
                setCodeDeployed(true);
                setCodeDeployedUrl(res.deploymentUrl);
                setCodeDeployedBranch(branchValue);
              } else if (deployEnvironment === 'production') {
                setProdCodeDeployed(true);
                setProdCodeDeployedUrl(res.deploymentUrl);
                setProdCodeDeployedBranch(branchValue);
              }
              
              Tooltip.defaultSetup();
              setShowCodeDeployModal(false);
              Notification.show(`Code from code space ${res.name} succesfully deployed.`);
            }
            if (res.status === 'DEPLOYMENT_FAILED') {
              clearInterval(livelinessIntervalRef.current);
              setCodeDeploying(false);
              setShowCodeDeployModal(false);
              Notification.show(`Deployment faild for code space ${res.name}. Please try again.`, 'alert');
            }
          } catch (err: any) {
            console.log(err);
          }
        })
        .catch((err: Error) => {
          clearInterval(livelinessInterval);
          setIsApiCallTakeTime(false);
          ProgressIndicator.hide();
          Notification.show('Error in validating code space deployment - ' + err.message, 'alert');
        });
    }, 2000);
    setLivelinessInterval(intervalId);
  };

  const onBranchChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setBranchValue(e.currentTarget.value);
  };

  const onDeployEnvironmentChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    setDeployEnvironment(evnt.currentTarget.value.trim());
  };

  const onAcceptCodeDeploy = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deployCodeSpace(codeSpaceData.id)
      .then((res: any) => {
        trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
        if (res.success === 'SUCCESS') {
          // setCreatedCodeSpaceName(res.data.name);
          setCodeDeploying(true);
          if (acceptContinueCodingOnDeployment) {
            ProgressIndicator.hide();
            Notification.show(
              `Code space '${codeSpaceData.name}' deployment successfully started. Please check the status later.`,
            );
            setShowCodeDeployModal(false);
          } else {
            setIsApiCallTakeTime(true);
          }
          enableDeployLivelinessCheck(codeSpaceData.name);
        } else {
          setIsApiCallTakeTime(false);
          ProgressIndicator.hide();
          Notification.show(
            'Error in deploying code space. Please try again later.\n' + res.errors[0].message,
            'alert',
          );
        }
      })
      .catch((err: Error) => {
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
                    <div className={Styles.urlLink} tooltip-data="API BASE URL - Staging">
                      <a href={codeDeployedUrl} target="_blank" rel="noreferrer">
                        <i className="icon mbc-icon link" /> Staging ({codeDeployedBranch})
                      </a>
                      &nbsp;
                    </div>
                  )}
                  {prodCodeDeployed && (
                    <div className={Styles.urlLink} tooltip-data="API BASE URL - Production">
                      <a href={prodCodeDeployedUrl} target="_blank" rel="noreferrer">
                        <i className="icon mbc-icon link" /> Production ({prodCodeDeployedBranch})
                      </a>
                      &nbsp;
                    </div>
                  )}
                  <div>
                    <button
                      className={classNames('btn btn-secondary', codeDeploying ? 'disable' : '')}
                      onClick={onShowCodeDeployModal}
                    >
                      {(codeDeployed || prodCodeDeployed) && '(Re)'}Deploy{codeDeploying && 'ing...'}
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
              <div className={Styles.flexLayout}>
                <div>
                  <div id="branchContainer" className="input-field-group">
                    <label id="branchLabel" className="input-label" htmlFor="branchSelect">
                      Code Branch to Deploy
                    </label>
                    <div id="branch" className="custom-select">
                      <select id="branchSelect" onChange={onBranchChange} value={branchValue}>
                        {branches.map((obj: any) => (
                          <option key={obj.id} id={obj.name + obj.id} value={obj.id}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
                <div>
                  <div id="deployEnvironmentContainer" className="input-field-group">
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Deploy Environment
                    </label>
                    <div>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value="staging"
                            name="deployEnvironment"
                            onChange={onDeployEnvironmentChange}
                            checked={deployEnvironment === 'staging'}
                          />
                        </span>
                        <span className="label">Staging</span>
                      </label>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value="production"
                            name="deployEnvironment"
                            onChange={onDeployEnvironmentChange}
                            checked={deployEnvironment === 'production'}
                          />
                        </span>
                        <span className="label">Production</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
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
      {isApiCallTakeTime && (
        <ProgressWithMessage message={'Please wait as this process can take up 2 to 5 minutes....'} />
      )}
    </div>
  );
};

export default CodeSpace;
