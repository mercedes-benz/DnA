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
import { recipesMaster, trackEvent } from '../../../services/utils';
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
import { CODE_SPACE_TITLE, IAM_URL } from 'globals/constants';
import TextBox from '../shared/textBox/TextBox';
import { DEPLOYMENT_DISABLED_RECIPE_IDS } from 'globals/constants';
import { IconGear } from 'components/icons/IconGear';
// import { HTTP_METHOD } from '../../../globals/constants';

export interface ICodeSpaceProps {
  user: IUserInfo;
}

export interface IRecipeDetails {
  recipeId?: string;
  environment?: string;
  cloudServiceProvider?: string;
  ramSize?: string;
  cpuCapacity?: string;
  operatingSystem?: string;
}

export interface IProjectDetails {
  projectName?: string;
  projectOwner?: ICodeCollaborator;
  gitRepoName?: string,
  projectCreatedOn?: null,
  recipeDetails?: IRecipeDetails;
  projectCollaborators?: ICodeCollaborator[];
  intDeploymentDetails?: IDeploymentDetails;
  prodDeploymentDetails?: IDeploymentDetails;
  securityConfig?: any;
  publishSecurityConfig?: any;
}

export interface IDeploymentDetails {
  secureWithIAMRequired?: boolean,
  technicalUserDetailsForIAMLogin?: string,
  lastDeployedOn?: string;
  deploymentUrl?: string;
  lastDeployedBranch?: string;
  lastDeploymentStatus?: string;
  lastDeployedBy?: ICodeCollaborator;
}

export interface ICodeSpaceData {
  id?: string;
  workspaceId?: string;
  description?: string,
  gitUserName?: string,
  intiatedOn?: string,
  workspaceUrl?: string,
  status?: string;
  configStatus?: string;
  workspaceOwner?: ICodeCollaborator,
  projectDetails?: IProjectDetails;

  // name?: string;
  // recipe?: string;
  // environment?: string;
  // deployed?: boolean;
  // deployedUrl?: string;
  // deployedBranch?: string;
  // prodDeployed?: boolean;
  // prodDeployedUrl?: string;
  // prodDeployedBranch?: string;
  // createdDate?: string;
  // lastDeployedDate?: string;
  // url: string;
  running?: boolean;
  // status?: string;
  // collaborators?: ICodeCollaborator[];
}

export interface IBranch {
  name: string;
}

export interface IDeployRequest {
  targetEnvironment: string; // int or prod
  branch: string;
  secureWithIAMRequired?: boolean,
  technicalUserDetailsForIAMLogin?: string,
}

const CodeSpace = (props: ICodeSpaceProps) => {
  // const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
  //   url: `https://code-spaces.dev.dna.app.corpintra.net/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
  //   running: false
  // });
  const { id } = getParams();
  const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
    workspaceUrl: undefined,
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
  const [secureWithIAMSelected, setSecureWithIAMSelected] = useState<boolean>(true);
  const [iamTechnicalUserID, setIAMTechnicalUserID] = useState<string>('');
  const [iamTechnicalUserIDError, setIAMTechnicalUserIDError] = useState<string>('');
  const [acceptContinueCodingOnDeployment, setAcceptContinueCodingOnDeployment] = useState<boolean>(true);
  const [livelinessInterval, setLivelinessInterval] = useState<NodeJS.Timer>();
  const [branches, setBranches] = useState<IBranch[]>([]);

  const livelinessIntervalRef = React.useRef<NodeJS.Timer>();

  const [branchValue, setBranchValue] = useState('main');
  const [deployEnvironment, setDeployEnvironment] = useState('staging');
  const recipes = recipesMaster;
  const requiredError = '*Missing entry';

  useEffect(() => {
    SelectBox.defaultSetup();
    setIAMTechnicalUserID('');
  }, [showCodeDeployModal]);

  useEffect(() => {
    if (id) {
      CodeSpaceApiClient.getCodeSpaceStatus(id)
        .then((res: ICodeSpaceData) => {

          const loginWindow = window.open(
            Envs.CODESPACE_OIDC_POPUP_URL + res.workspaceId + '/',
            'codeSpaceSessionWindow',
            'width=100,height=100,location=no,menubar=no,status=no,titlebar=no,toolbar=no',
          );

          setTimeout(() => {
            loginWindow?.close();

            setLoading(false);
            const status = res.status;
            if (
              status !== 'CREATE_REQUESTED' &&
              status !== 'CREATE_FAILED' &&
              status !== 'DELETE_REQUESTED' &&
              status !== 'DELETED' &&
              status !== 'DELETE_FAILED'
            ) {
              const intDeploymentDetails = res.projectDetails.intDeploymentDetails;
              const prodDeploymentDetails = res.projectDetails.prodDeploymentDetails;
              const intDeployedUrl = intDeploymentDetails?.deploymentUrl;
              const prodDeployedUrl = prodDeploymentDetails?.deploymentUrl;
              const intDeployed =
                intDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
                (intDeployedUrl !== null && intDeployedUrl !== 'null');
              const prodDeployed =
                prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
                (prodDeployedUrl !== null && prodDeployedUrl !== 'null');
              const deployingInProgress =
                (intDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
                  prodDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED');
              // const deployed =
              //   intDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
              //   prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
              //   (intDeployedUrl !== null && intDeployedUrl !== 'null') ||
              //   (prodDeployedUrl !== null && prodDeployedUrl !== 'null');

              setCodeSpaceData({
                ...res,
                running: !!res.intiatedOn,
              });
              setCodeDeployed(intDeployed);
              setCodeDeployedUrl(intDeployedUrl);
              setCodeDeployedBranch(intDeploymentDetails.lastDeployedBranch);

              setProdCodeDeployed(prodDeployed);
              setProdCodeDeployedUrl(prodDeployedUrl);
              setProdCodeDeployedBranch(prodDeploymentDetails.lastDeployedBranch);

              Tooltip.defaultSetup();
              if (deployingInProgress) {
                const deployingEnv = intDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED' ? 'staging' : 'production';
                setDeployEnvironment(deployingEnv);
                setCodeDeploying(true);
                enableDeployLivelinessCheck(res.workspaceId, deployingEnv);
              }
            } else {
              Notification.show(`Code space ${res.projectDetails.projectName} is getting created. Please try again later.`, 'warning');
            }
          }, Envs.CODESPACE_OIDC_POPUP_WAIT_TIME);
        })
        .catch((err: Error) => {
          Notification.show('Error in loading codespace - Please contact support.' + err.message, 'alert');
          history.replace('/codespaces');
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
    } else {
      Notification.show('Codespace id is missing. Please choose your codespace to open.', 'warning');
      history.replace('/codespaces');
    }
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
    window.open(codeSpaceData.workspaceUrl, '_blank');
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
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpacesGitBranchList(codeSpaceData.projectDetails?.gitRepoName)
      .then((res: any) => {
        ProgressIndicator.hide();
        setShowCodeDeployModal(true);
        setBranches(res);
        setIAMTechnicalUserID(codeSpaceData.projectDetails?.intDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
        setSecureWithIAMSelected(codeSpaceData.projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false);
        SelectBox.defaultSetup();
        Tooltip.defaultSetup();
      })
      .catch((err: Error) => {
        ProgressIndicator.hide();
        Notification.show('Error in getting code space branch list - ' + err.message, 'alert');
      });
  };

  const onCodeDeployModalCancel = () => {
    setShowCodeDeployModal(false);
  };

  const enableDeployLivelinessCheck = (id: string, deployEnvironmentValue: string) => {
    clearInterval(livelinessInterval);
    const intervalId = setInterval(() => {
      CodeSpaceApiClient.getCodeSpaceStatus(id)
        .then((res: ICodeSpaceData) => {
          try {
            const intDeploymentDetails = res.projectDetails?.intDeploymentDetails;
            const prodDeploymentDetails = res.projectDetails?.prodDeploymentDetails;

            const deployStatus = deployEnvironmentValue === 'staging' ? intDeploymentDetails?.lastDeploymentStatus : prodDeploymentDetails?.lastDeploymentStatus;
            if (deployStatus === 'DEPLOYED') {
              setIsApiCallTakeTime(false);
              ProgressIndicator.hide();
              clearInterval(livelinessIntervalRef.current);
              setCodeDeploying(false);
              if (deployEnvironmentValue === 'staging') {
                setCodeDeployed(true);
                setCodeDeployedUrl(intDeploymentDetails?.deploymentUrl);
                setCodeDeployedBranch(branchValue);
              } else if (deployEnvironmentValue === 'production') {
                setProdCodeDeployed(true);
                setProdCodeDeployedUrl(prodDeploymentDetails?.deploymentUrl);
                setProdCodeDeployedBranch(branchValue);
              }

              Tooltip.defaultSetup();
              setShowCodeDeployModal(false);
              Notification.show(`Code from code space ${res.projectDetails?.projectName} succesfully deployed.`);
            }
            if (deployStatus === 'DEPLOYMENT_FAILED') {
              ProgressIndicator.hide();
              clearInterval(livelinessIntervalRef.current);
              setCodeDeploying(false);
              setShowCodeDeployModal(false);
              setIsApiCallTakeTime(false);
              Notification.show(`Deployment failed for code space ${res.projectDetails?.projectName}. Please try again.`, 'alert');
            }

            setCodeSpaceData({
              ...res,
              running: !!res.intiatedOn,
            });

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
    const deployEnv = evnt.currentTarget.value.trim();
    setDeployEnvironment(deployEnv);
    if (deployEnv === 'staging') {
      setSecureWithIAMSelected(codeSpaceData.projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false);
      setIAMTechnicalUserID(codeSpaceData.projectDetails?.intDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
    } else {
      setSecureWithIAMSelected(codeSpaceData.projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || false);
      setIAMTechnicalUserID(codeSpaceData.projectDetails?.prodDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
    }
  };

  const onAcceptCodeDeploy = () => {
    if (secureWithIAMSelected && iamTechnicalUserID.trim() === '') {
      setIAMTechnicalUserIDError(requiredError);
      return;
    } else {
      setIAMTechnicalUserIDError('');
    }
    const deployRequest: IDeployRequest = {
      secureWithIAMRequired: secureWithIAMSelected,
      technicalUserDetailsForIAMLogin: secureWithIAMSelected ? iamTechnicalUserID : null,
      targetEnvironment: deployEnvironment === 'staging' ? 'int' : 'prod', // int or prod
      branch: branchValue
    };
    ProgressIndicator.show();
    CodeSpaceApiClient.deployCodeSpace(codeSpaceData.id, deployRequest)
      .then((res: any) => {
        trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
        if (res.success === 'SUCCESS') {
          // setCreatedCodeSpaceName(res.data.name);
          setCodeDeploying(true);
          if (acceptContinueCodingOnDeployment) {
            ProgressIndicator.hide();
            Notification.show(
              `Code space '${codeSpaceData.projectDetails.projectName}' deployment successfully started. Please check the status later.`,
            );
            setShowCodeDeployModal(false);
          } else {
            setIsApiCallTakeTime(true);
          }
          enableDeployLivelinessCheck(codeSpaceData.workspaceId, deployEnvironment);
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

  const onChangeSecureWithIAM = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSecureWithIAMSelected(e.target.checked);
  };

  const onIAMTechnicalUserIDOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const iamUserID = evnt.currentTarget.value.trim();
    setIAMTechnicalUserID(iamUserID);
    setIAMTechnicalUserIDError(iamUserID.length ? '' : requiredError);
  };

  const onAcceptContinueCodingOnDeployment = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAcceptContinueCodingOnDeployment(e.target.checked);
  };

  const disableDeployment = codeSpaceData?.projectDetails?.recipeDetails?.recipeId.startsWith('public') || DEPLOYMENT_DISABLED_RECIPE_IDS.includes(codeSpaceData?.projectDetails?.recipeDetails?.recipeId);
  const securedWithIAMContent: React.ReactNode = (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      stroke="#00adef"
      fill="#00adef"
      strokeWidth="0"
      viewBox="0 0 30 30"
      width="15px"
      height="15px"
    >
      {' '}
      <path d="M 15 2 C 11.145666 2 8 5.1456661 8 9 L 8 11 L 6 11 C 4.895 11 4 11.895 4 13 L 4 25 C 4 26.105 4.895 27 6 27 L 24 27 C 25.105 27 26 26.105 26 25 L 26 13 C 26 11.895 25.105 11 24 11 L 22 11 L 22 9 C 22 5.2715823 19.036581 2.2685653 15.355469 2.0722656 A 1.0001 1.0001 0 0 0 15 2 z M 15 4 C 17.773666 4 20 6.2263339 20 9 L 20 11 L 10 11 L 10 9 C 10 6.2263339 12.226334 4 15 4 z" />
    </svg>
  );

  const isOwner = codeSpaceData.projectDetails?.projectOwner?.id === props.user.id;
  const navigateSecurityConfig = () => {
    if (codeSpaceData?.projectDetails?.publishSecurityConfig) {
      history.push(`/codespace/publishedSecurityconfig/${codeSpaceData.id}?pub=true`);
      return;
    }
    history.push(`/codespace/securityconfig/${codeSpaceData.id}?pub=false`);
  }

  return (
    <div className={fullScreenMode ? Styles.codeSpaceWrapperFSmode : '' + ' ' + Styles.codeSpaceWrapper}>
      {codeSpaceData.running && (
        <React.Fragment>
          <div className={Styles.nbheader}>
            <div className={Styles.headerdetails}>
              <img src={Envs.DNA_BRAND_LOGO_URL} className={Styles.Logo} />
              <div className={Styles.nbtitle}>
                <button tooltip-data="Go Back" className="btn btn-text back arrow" onClick={goBack}></button>
                <h2
                  tooltip-data={
                    recipes.find((item: any) => item.id === codeSpaceData.projectDetails.recipeDetails.recipeId).name
                  }
                >
                  {codeSpaceData.projectDetails.projectName}
                </h2>
              </div>
            </div>
            <div className={Styles.navigation}>
              {codeSpaceData.running && (
                <div className={Styles.headerright}>
                  {!disableDeployment && (
                    <>
                      {isOwner && (
                        <div className={Styles.configLink}
                          onClick={() => navigateSecurityConfig()}>
                          <a target="_blank" rel="noreferrer">
                            <IconGear size={'16'} /> {CODE_SPACE_TITLE} (
                            {codeSpaceData?.projectDetails?.securityConfig?.status})
                          </a>
                          &nbsp;
                        </div>
                      )}
                      {codeDeployed && (
                        <div className={Styles.urlLink} tooltip-data="API BASE URL - Staging">
                          <a href={codeDeployedUrl} target="_blank" rel="noreferrer">
                            <i className="icon mbc-icon link" /> Staging (
                            <i className="icon mbc-icon transactionaldata" /> {codeDeployedBranch})
                            {codeSpaceData.projectDetails.intDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                          </a>
                          &nbsp;
                        </div>
                      )}
                      {prodCodeDeployed && (
                        <div className={Styles.urlLink} tooltip-data="API BASE URL - Production">
                          <a href={prodCodeDeployedUrl} target="_blank" rel="noreferrer">
                            <i className="icon mbc-icon link" /> Production (
                            <i className="icon mbc-icon transactionaldata" /> {prodCodeDeployedBranch})
                            {codeSpaceData.projectDetails.prodDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
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
                    </>
                  )}
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
                        src={codeSpaceData.workspaceUrl}
                        title="Code Space"
                        allow="clipboard-read; clipboard-write"
                      />
                      <div className={Styles.textRight}>
                        <small>
                          Made with{' '}
                          <svg
                            stroke="#e84d47"
                            fill="#e84d47"
                            strokeWidth="0"
                            viewBox="0 0 512 512"
                            height="1em"
                            width="1em"
                            xmlns="http://www.w3.org/2000/svg"
                          >
                            <path d="M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9 49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3 0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z"></path>
                          </svg>{' '}
                          by Developers for Developers
                        </small>
                      </div>
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
          modalWidth="600px"
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
                          <option key={obj.name} id={obj.name + '-branch'} value={obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
                <div>
                  <div id="deployEnvironmentContainer" className="input-field-group">
                    <label className={classNames(Styles.inputLabel, 'input-label')}>Deploy Environment</label>
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
              {(!codeSpaceData.projectDetails.recipeDetails?.recipeId.match(/^(react|angular)$/)) &&
                <>
                  {deployEnvironment === 'staging' &&
                    <>
                      <div>
                        <label className="checkbox">
                          <span className="wrapper">
                            <input
                              type="checkbox"
                              className="ff-only"
                              checked={secureWithIAMSelected}
                              onChange={onChangeSecureWithIAM}
                              disabled={codeSpaceData.projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                            />
                          </span>
                          <span className="label">Secure with IAM</span>
                        </label>
                      </div>
                      {secureWithIAMSelected && (
                        <div className={classNames(Styles.flexLayout, codeSpaceData.projectDetails?.intDeploymentDetails?.secureWithIAMRequired && Styles.disabledDiv)}>
                          <div>
                            <TextBox
                              type="text"
                              controlId={'iamTechnicalUserID'}
                              labelId={'iamTechnicalUserIDLabel'}
                              label={'Technical User ID'}
                              placeholder={'IAM Technical User Id'}
                              value={iamTechnicalUserID}
                              errorText={iamTechnicalUserIDError}
                              required={true}
                              maxLength={7}
                              onChange={onIAMTechnicalUserIDOnChange}
                            />
                          </div>
                          <div className={Styles.createTechUserWrapper}>
                            <a href={IAM_URL} target="_blank" rel="noreferrer">
                              Create a new technical user in IAM (Enabled only with Production IAM)
                            </a>
                          </div>
                        </div>
                      )}
                    </>
                  }
                  {deployEnvironment === 'production' &&
                    <>
                      <div>
                        <label className="checkbox">
                          <span className="wrapper">
                            <input
                              type="checkbox"
                              className="ff-only"
                              checked={secureWithIAMSelected}
                              onChange={onChangeSecureWithIAM}
                              disabled={codeSpaceData.projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                            />
                          </span>
                          <span className="label">Secure with IAM</span>
                        </label>
                      </div>
                      {secureWithIAMSelected && (
                        <div className={classNames(Styles.flexLayout, codeSpaceData.projectDetails?.prodDeploymentDetails?.secureWithIAMRequired && Styles.disabledDiv)}>
                          <div>
                            <TextBox
                              type="text"
                              controlId={'iamTechnicalUserID'}
                              labelId={'iamTechnicalUserIDLabel'}
                              label={'Technical User ID'}
                              placeholder={'IAM Technical User Id'}
                              value={iamTechnicalUserID}
                              errorText={iamTechnicalUserIDError}
                              required={true}
                              maxLength={7}
                              onChange={onIAMTechnicalUserIDOnChange}
                            />
                          </div>
                          <div className={Styles.createTechUserWrapper}>
                            <a href={IAM_URL} target="_blank" rel="noreferrer">
                              Create a new technical user in IAM (Enabled only with Production IAM)
                            </a>
                          </div>
                        </div>
                      )}
                    </>
                  }
                </>
              }
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
