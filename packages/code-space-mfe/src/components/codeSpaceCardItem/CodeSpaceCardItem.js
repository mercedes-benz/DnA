import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import {
  // recipesMaster,
  regionalDateAndTimeConversionSolution,
  buildLogViewURL,
  buildGitJobLogViewURL,
  buildLogViewAWSURL,
  buildGitJobLogViewAWSURL,
  buildGitUrl,
} from '../../Utility/utils';
import ConfirmModal from 'dna-container/ConfirmModal';
import Modal from 'dna-container/Modal';
import { history } from '../../store';
// @ts-ignore
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// import { ICodeSpaceData } from '../CodeSpace';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { trackEvent } from '../../Utility/utils';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
// import { IUserInfo } from 'globals/types';
import { IconGear } from 'dna-container/IconGear';
// import { DEPLOYMENT_DISABLED_RECIPE_IDS } from '../../Utility/constants';
import DoraMetrics from '../doraMetrics/DoraMetrics';
import VaultManagement from '../vaultManagement/VaultManagement';
import DeployAuditLogsModal from '../deployAuditLogsModal/DeployAuditLogsModal';
import { setRippleAnimation } from '../../common/modules/uilab/js/src/util';
import { marked } from 'marked';
import { Envs } from '../../Utility/envs';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

// interface CodeSpaceCardItemProps {
//   userInfo: IUserInfo;
//   codeSpace: ICodeSpaceData;
//   onDeleteSuccess?: () => void;
//   toggleProgressMessage?: (show: boolean) => void;
//   onShowCodeSpaceOnBoard: (codeSpace: ICodeSpaceData, isRetryRequest?: boolean) => void;
//   onCodeSpaceEdit: (codeSpace: ICodeSpaceData) => void;
//   onShowDeployModal: (codeSpace: ICodeSpaceData) => void;
//   onStartStopCodeSpace: (codeSpace: ICodeSpaceData, startSuccessCB: () => void) => void;
// }

let isTouch = false;

const CodeSpaceCardItem = (props) => {
  let codeSpace = props.codeSpace;
  // const collaborationCodeSpace = codeSpace.projectDetails.projectCollaborators?.find((user: ICodeCollaborator) => user.id === props.userInfo.id);
  const enableOnboard = codeSpace ? codeSpace.status === 'COLLABORATION_REQUESTED' : false;
  // const codeDeploying = codeSpace.status === 'DEPLOY_REQUESTED';
  const deleteInProgress = codeSpace.status === 'DELETE_REQUESTED';
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const creationFailed = codeSpace.status === 'CREATE_FAILED';
  // const serverStarted = codeSpace.serverStatus === 'SERVER_STARTED';

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showVaultManagementModal, setShowVaultManagementModal] = useState(false);
  const [showAuditLogsModal, setShowAuditLogsModal] = useState(false);
  // const recipes = recipesMaster;
  const collaborator = codeSpace.projectDetails?.projectCollaborators?.find((collaborator) => {return collaborator?.id === props?.userInfo?.id });
  const isOwner = codeSpace.projectDetails?.projectOwner?.id === props.userInfo.id || collaborator?.isAdmin;
  const hasCollaborators = codeSpace.projectDetails?.projectCollaborators?.length > 0;
  // const disableDeployment =
  //   codeSpace?.projectDetails?.recipeDetails?.recipeId.startsWith('public') ||
  //   DEPLOYMENT_DISABLED_RECIPE_IDS.includes(codeSpace?.projectDetails?.recipeDetails?.recipeId);
  const disableDeployment = !codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled;
  const [showDoraMetricsModal, setShowDoraMetricsModal] = useState(false);
  const [isStaging, setIsStaging] = useState(false);
  const [logsList, setlogsList] = useState([]);

  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState(0);

  const [serverStarted, setServerStarted] = useState(false);
  const [serverFailed, setServerFailed] = useState(false);
  const [serverProgress, setServerProgress] = useState(0);

  const [showStagingActions, setShowStagingActions] = useState(false);
  const [showProdActions, setShowProdActions] = useState(false);
  const stagingWrapperRef = useRef(null);
  const prodWrapperRef = useRef(null);
  const [showRestartModal, setShowRestartModal] = useState(false);
  const [env, setEnv] = useState("");
  const [showReadMeModal, setShowReadMeModal] = useState(false);
  const [readMeContent, setReadMeContent] = useState('');
  const enableReadMe =  Envs.CODESPACE_RECIEPES_ENABLE_README?.split(',')?.includes(codeSpace?.projectDetails?.recipeDetails?.Id) || false;
  const resourceUsageUrl = Envs.MONITORING_DASHBOARD_BASE_URL + `codespace-cpu-and-memory-usage?orgId=1&from=now-1h&to=now&var-namespace=${Envs.CODESERVER_NAMESPACE}&var-pod=${codeSpace.workspaceId}&var-container=notebook`;
  const [showMigrateOrStartModal, setShowMigrateOrStartModal] = useState(false);
  const [showOnPremStartModal, setShowOnPremStartModal] = useState(false);

  useEffect(() => {

    handleServerStatusAndProgress();
    Tooltip.defaultSetup();
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);
    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
      Tooltip.clear();
    };
  }, []);// eslint-disable-line react-hooks/exhaustive-deps

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
    const cardDivElement = document?.querySelector('#card-' + codeSpace.id);
    const contextMenuWrapper = cardDivElement?.querySelector('.contextMenuWrapper');

    if (
      cardDivElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      showContextMenu
    ) {
      setShowContextMenu(false);
    } else if (cardDivElement?.contains(target) === false) {
      setShowContextMenu(false);
    }

    if (!contextMenuWrapper?.contains(target)) {
      setShowContextMenu(false);
    }

    if (
      showContextMenu &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };

  useEffect(() => {
    Tooltip.defaultSetup();
  }, [serverStarted]);// eslint-disable-line react-hooks/exhaustive-deps

  const toggleContextMenu = (e) => {
    e.stopPropagation();
    setRippleAnimation(prodWrapperRef.current);
    setRippleAnimation(stagingWrapperRef.current);
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 17);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 230);
    setShowContextMenu(!showContextMenu);
  };

  const deleteCodeSpaceContent = (
    <div>
      <h3>
        {/* Are you sure to delete {codeSpace.projectDetails.projectName} Code Space?
        <br /> */}
        {codeSpace?.projectDetails?.projectOwner?.id === props.userInfo.id ? (
          <>
            {hasCollaborators ? (
              <>
                You have collaborators in your project.
                <br />
                Please transfer your ownership to any one of the collaborator <br /> or remove the collaborator(s)
                before deleting this code space &apos;{codeSpace?.projectDetails?.projectName}&apos;.
              </>
            ) : (
              <>
                Deleting a CodeSpace would delete the code associated with it,
                <br /> Do you want to proceed?
              </>
            )}
          </>
        ) : (
          <>
            You were asked to collaborate on this CodeSpace by your colleague.
            <br />
            Deleting this CodeSpace will revoke your access to collaborate.
            <br />
            Do you wish to proceed?
          </>
        )}
      </h3>
    </div>
  );

  const deleteCodeSpaceAccept = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deleteCodeSpace(codeSpace.id)
      .then((res) => {
        trackEvent('DnA Code Space', 'Delete', 'Delete code space');
        if (res.data.success === 'SUCCESS') {
          props.onDeleteSuccess();
          setShowDeleteModal(false);
          ProgressIndicator.hide();
          Notification.show(`Code space '${codeSpace.projectDetails?.projectName}' has been deleted successfully.`);
        } else {
          ProgressIndicator.hide();
          Notification.show('Error in deleting code space. Please try again later.\n' + res.data.errors[0].message, 'alert');
        }
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('Error in deleting code space. Please try again later.\n' + err.message, 'alert');
      });
  };
  const deleteCodeSpaceClose = () => {
    setShowDeleteModal(false);
  };

  const onCardNameClick = () => {
    if (enableOnboard) {
      props.onShowCodeSpaceOnBoard(codeSpace);
    } else if (!serverStarted) {
      onStartStopCodeSpace(codeSpace);
    } else {
      history.push(`codespace/${codeSpace.workspaceId}`);
    }
  };

  const getReadMeFile = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getReadMeFile(codeSpace?.workspaceId)
      .then((res) => {
        ProgressIndicator.hide();
        let htmlContent = '';
        if(res.status === 200){
          const base64Data = atob(res.data.file);
          const decodedText = atob(base64Data);
          htmlContent = marked(decodedText);
          setReadMeContent(htmlContent);
          setShowReadMeModal(true);
        }else{
          Notification.show('No content found', 'alert');
        }
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('something went wrong' + err.message, 'alert');
      });
  };

  const onRetryCreateClick = () => {
    props.onShowCodeSpaceOnBoard(codeSpace, true);
  };

  const onCodeSpaceSecurityConfigClick = (codeSpace) => {
    if (codeSpace?.projectDetails?.publishedSecuirtyConfig) {
      history.push(
        `/codespace/publishedSecurityconfig/${codeSpace.id}?name=${codeSpace.projectDetails.projectName}?intIAM=${projectDetails?.intDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}?prodIAM=${projectDetails?.prodDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}`,
      );
      return;
    }
    history.push(`codespace/securityconfig/${codeSpace.id}?name=${codeSpace.projectDetails.projectName}?intIAM=${projectDetails?.intDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}?prodIAM=${projectDetails?.prodDeploymentDetails?.secureWithIAMRequired ? 'true' : 'false'}`);
  };

  const onCodeSpaceDelete = () => {
    if (creationFailed) {
      deleteCodeSpaceAccept();
    } else {
      setShowDeleteModal(true);
    }
  };

  const handleOpenDoraMetrics = () => {
    setShowDoraMetricsModal(true);
  };

  const onStartStopCodeSpace = (codespace) => {
    if(codespace?.projectDetails?.recipeDetails?.cloudServiceProvider ==='DHC-CaaS-AWS'){
      props.onStartStopCodeSpace(codespace, handleServerStatusAndProgress, 'DHC-CaaS-AWS');
    }
    else{
      codespace.serverStatus === 'SERVER_STARTED' ? props.onStartStopCodeSpace(codespace, handleServerStatusAndProgress, 'DHC-CaaS') : setShowMigrateOrStartModal(true);
    }
  };

  const onMigrateWorkplace = () => {
    setShowMigrateOrStartModal(false);
    ProgressIndicator.show();
    CodeSpaceApiClient.migrateWorkplace(codeSpace.id)
      .then((res) => {
        
        if (res.data.success === 'SUCCESS') {
          codeSpace.projectDetails.recipeDetails.cloudServiceProvider = 'DHC-CaaS-AWS';
          ProgressIndicator.hide();
          Notification.show(
            'Your Codespace for project ' + codeSpace.projectDetails?.projectName +' is requested to migrate.'
          );
          props.onStartStopCodeSpace(codeSpace, handleServerStatusAndProgress, 'DHC-CaaS-AWS');
          setTimeout(() => {
            window.location.reload();
          }, 100);
        } else {
          ProgressIndicator.hide();
          Notification.show(
            'Error in migrating your code space. Please try again later.',
            'alert',
          );
        }
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show(
          'Error in migrating your code space. Please try again later.'+ err.message,
          'alert',
        );
      });

  }

  const handleServerStatusAndProgress = () => {
    codeSpace.serverStatus = 'SERVER_STOPPED';
    const env = codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' ? 'DHC-CaaS-AWS' : 'DHC-CaaS';
    CodeSpaceApiClient.serverStatusFromHub(env,props.userInfo.id.toLowerCase(), codeSpace.workspaceId, (e) => {
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

  const projectDetails = codeSpace?.projectDetails;
  const intDeploymentDetails = projectDetails.intDeploymentDetails;
  const prodDeploymentDetails = projectDetails.prodDeploymentDetails;
  const intDeployedUrl = intDeploymentDetails?.deploymentUrl;
  // const intLastDeployedOn = intDeploymentDetails?.lastDeployedOn;
  const prodDeployedUrl = prodDeploymentDetails?.deploymentUrl;
  // const prodLastDeployedOn = prodDeploymentDetails?.lastDeployedOn;
  const deployingInProgress =
    intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED';
  const intDeployed =
    intDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ||
    (intDeployedUrl !== null && intDeployedUrl !== 'null') ||
    false;
  const intCodeDeployFailed = intDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
  const intLastDeployedTime = new Date(
    // regionalDateAndTimeConversionSolution(
      intDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED'
        ? intDeploymentDetails?.lastDeployedOn
        : intDeploymentDetails?.deploymentAuditLogs &&
            intDeploymentDetails?.deploymentAuditLogs[intDeploymentDetails?.deploymentAuditLogs?.length - 1]
              ?.triggeredOn,
    // ),
  ).getTime();
  const prodDeployed =
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ||
    (prodDeployedUrl !== null && prodDeployedUrl !== 'null') ||
    false;
  const prodCodeDeployFailed = prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
  const prodLastDeployedTime = new Date(
    // regionalDateAndTimeConversionSolution(
      prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED'
        ? prodDeploymentDetails?.lastDeployedOn
        : prodDeploymentDetails?.deploymentAuditLogs &&
            prodDeploymentDetails?.deploymentAuditLogs[prodDeploymentDetails?.deploymentAuditLogs?.length - 1]
              ?.triggeredOn,
    // ),
  ).getTime();
  const deployed = intDeployed || prodDeployed || prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED' || intDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
  const allowDelete = codeSpace?.projectDetails?.projectOwner?.id === props.userInfo.id ? !hasCollaborators : true;
  const isPublicRecipe = projectDetails.recipeDetails?.recipeId?.startsWith('public');
  // const isAPIRecipe =
  //   props.codeSpace.projectDetails.recipeDetails.recipeId === 'springboot' ||
  //   props.codeSpace.projectDetails.recipeDetails.recipeId === 'py-fastapi' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'dash' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'streamlit' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'nestjs' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven' ;

  const isIAMRecipe =
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven' ;

  const resources = projectDetails?.recipeDetails?.resource?.split(',');

  const deploymentMigrated = !(codeSpace?.projectDetails?.intDeploymentDetails?.deploymentUrl?.includes(Envs.CODESPACE_OIDC_POPUP_URL) || codeSpace?.projectDetails?.prodDeploymentDetails?.deploymentUrl?.includes(Envs.CODESPACE_OIDC_POPUP_URL));

  const securedWithIAMContent = (
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

  const RestartContent = (
    <div>
      <h3>Are you sure you want to restart your deployed application?</h3>
      <p>Note: Please refresh and check the application restart status under action audit logs.</p>
    </div>
  );

  const migrateOrStartContent = (
    <div className={Styles.modalContentWrapper}>
      <div className={Styles.modalTitle}>Do you want to migrate from DyP-CaaS(On-Prem) to DyP-CaaS(AWS) ? </div>
      <div className={Styles.modalContent}>
        <p>Note: Before migrating please commit or keep a backup of your changes and untracked files present in your current workspace. On migration your workspace will be requested to start but please note that the initial start may take some time.</p>
        <br/>
        <p>If you do not wish to migrate at the moment you can start your workspace by simply clicking on the DyP-CaaS(On-Prem) option.</p>
      </div>
    </div>
  );

  const onRestart = (env) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.restartDeployments(codeSpace?.id, env)
    .then((res) => {
      if (res.data.success === 'SUCCESS') {
        ProgressIndicator.hide();
        Notification.show("Restart requested successfully")
      } else {
          ProgressIndicator.hide();
          Notification.show('Error in Restarting deployed application. Please try again later.\n' + res.data.errors[0].message, 'alert');
        }
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('Error in Restarting deployed application. Please try again later.\n' + err.message, 'alert');
      });
    setShowRestartModal(false);
  }

  const onShowOnPremStartModal = (
    <div>
      <p>
        Click on the Start button to start your workspace incase the link is inaccessible. If you have already started before then access your workspace through the link provided. Please note that the link may take some time to be accessible after the start.
      </p>
      <div className={Styles.manualStart}>
        <div>
          <button
            className={classNames('btn btn-tertiary')}
            onClick={() => {props.onStartStopCodeSpace(codeSpace, handleServerStatusAndProgress, 'DHC-CaaS', true);}}
          >
            Start your old workspace
          </button>
        </div>
        <div><a target="_blank" href={Envs.CODESPACE_OIDC_POPUP_URL+"user/"+codeSpace?.workspaceOwner?.id.toLowerCase()+"/"+codeSpace?.workspaceId+"/?folder=/home/coder/app"} rel="noreferrer">Your old workspace URL</a></div>
      </div>
    </div>
  );

  return (
    <>
      <div
        id={'card-' + codeSpace.id}
        key={codeSpace.id}
        className={classNames(Styles.codeSpaceCard, deleteInProgress || createInProgress ? Styles.disable : null)}
      >
        <div className={Styles.cardHead}>
          <div
            className={classNames(
              Styles.cardHeadInfo,
              deleteInProgress || createInProgress || creationFailed ? Styles.disable : null,
            )}
          >
            <div className={classNames('btn btn-text', Styles.cardHeadTitle)}>
              <label onClick={onCardNameClick}>{projectDetails.projectName}</label>
              {!enableOnboard && !creationFailed && serverStarted && (
                <a
                  className={Styles.OpenNewTab}
                  tooltip-data="Open workspace in new tab"
                  onClick={(e) => {
                    e.stopPropagation();
                    window.open(codeSpace?.workspaceUrl, '_blank');
                    trackEvent('DnA Code Space', 'Code Space Open', 'Open in New Tab');
                  }}
                >
                  <i className="icon mbc-icon arrow small right" />
                  <span> &nbsp; </span>
                </a>
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
                <div
                  style={{
                    top: contextMenuOffsetTop + 'px',
                    left: contextMenuOffsetLeft + 'px',
                    zIndex: 5,
                  }}
                  className={classNames('contextMenuWrapper', Styles.contextMenu, showContextMenu ? '' : 'hide')}
                >
                  <ul>
                    <li className={classNames(deployingInProgress ? 'inactive' : '')}>
                      <span
                        onClick={() => {
                          props.onShowDeployModal(codeSpace);
                        }}
                      >
                        Deploy Code
                      </span>
                    </li>
                    {projectDetails?.gitRepoName && (
                      <li>
                        <a target="_blank" href={buildGitUrl(codeSpace.projectDetails?.gitRepoName)} rel="noreferrer">
                          Go to code repo
                          <i className="icon mbc-icon new-tab" />
                        </a>
                      </li>
                    )}
                     {serverStarted && (
                      <li>
                         <a target="_blank" href={resourceUsageUrl} rel="noreferrer">
                          Resource usage
                          <i className="icon mbc-icon new-tab" />
                        </a>
                      </li>
                    )}
                    {codeSpace.isWorkspaceMigrated && Envs.SHOW_ON_PREM_START && (
                      <li>
                        <span
                          onClick={() => {
                            setShowOnPremStartModal(true);
                          }}
                        >
                          Start on DyP-CaaS On-Prem (manual)
                        </span>
                      </li>
                    )}
                    <li>
                      <hr />
                    </li>
                    <li>
                      <button
                        className={classNames('btn btn-primary', Styles.btnOutline, !((codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) || intDeploymentDetails?.deploymentAuditLogs) && Styles.btnDisabled)}
                        onClick={() => {
                          setShowStagingActions(!showStagingActions);
                        }}
                      >
                        <div>
                          <strong>Staging:</strong>{' '}
                          {intDeploymentDetails?.lastDeployedBranch ? 'Deployed' : 'No Deployment'}
                          <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                            (DORA Metrics)
                          </span>
                        </div>
                        <div ref={stagingWrapperRef} className={classNames(Styles.collapseIcon, showStagingActions ? Styles.open : '')}>
                          {((codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) || intDeploymentDetails?.deploymentAuditLogs) && (
                            <>
                              <span className={classNames('animation-wrapper', Styles.animationWrapper)}></span>
                              <i className={classNames("icon down-up-flip")}></i>
                            </>
                          )}
                        </div>
                      </button>
                    </li>
                    {showStagingActions && (
                      <>
                        {intDeploymentDetails?.lastDeployedBranch && (
                          <li style={{ color: 'var(--color-orange)' }}>
                            [Branch - {intDeploymentDetails?.lastDeployedBranch}]
                          </li>
                        )}
                        {codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner && (
                          <li>
                            <span
                              onClick={() => {
                                setShowVaultManagementModal(true);
                                setIsStaging(true);
                              }}
                            >
                              Environment variables config
                            </span>
                          </li>
                        )}
                        {intDeploymentDetails?.gitjobRunID && (
                          <li>
                            <a
                              target="_blank"
                              href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildGitJobLogViewAWSURL(intDeploymentDetails?.gitjobRunID) : buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                              rel="noreferrer"
                            >
                              Last Build &amp; Deploy Logs{' '}
                              {intCodeDeployFailed && <span className={classNames(Styles.error)}>[Failed]</span>}{' '}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {intDeployed && (
                          <li>
                            <a href={intDeployedUrl} target="_blank" rel="noreferrer">
                              Deployed App URL {intDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {intDeploymentDetails?.lastDeploymentStatus && (
                          <li>
                            <a
                              target="_blank"
                              href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildLogViewAWSURL(intDeployedUrl || projectDetails?.projectName.toLowerCase(), true) :buildLogViewURL(intDeployedUrl || projectDetails?.projectName.toLowerCase(), true)}
                              rel="noreferrer"
                            >
                              Application Logs <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {intDeploymentDetails?.deploymentAuditLogs && (
                          <li>
                            <span
                              onClick={() => {
                                setShowAuditLogsModal(true);
                                setIsStaging(true);
                                setlogsList(intDeploymentDetails?.deploymentAuditLogs);
                              }}
                            >
                              Deploy & Action Audit Logs
                            </span>
                          </li>
                        )}
                        {intDeployed && (
                          <li>
                            <span
                              onClick={() => {setEnv("int"); setShowRestartModal(true);}}
                            >
                              Restart Deployed Application
                            </span>
                          </li>
                        )}
                      </>
                    )}
                    <li>
                      <hr />
                    </li>
                    <li>
                      <button
                        className={classNames('btn btn-primary', Styles.btnOutline, !((codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) || prodDeploymentDetails?.deploymentAuditLogs) && Styles.btnDisabled)}
                        onClick={() => {
                          setShowProdActions(!showProdActions);
                        }}
                      >
                        <div>
                          <strong>Production:</strong>{' '}
                          {prodDeploymentDetails?.lastDeployedBranch ? 'Deployed' : 'No Deployment'}
                          <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                            (DORA Metrics)
                          </span>
                        </div>
                        <div ref={prodWrapperRef} className={classNames(Styles.collapseIcon, showProdActions ? Styles.open : '')} >
                          {((codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) || prodDeploymentDetails?.deploymentAuditLogs) && (
                            <>
                              <span className={classNames('animation-wrapper', Styles.animationWrapper)}></span>
                              <i className={classNames("icon down-up-flip")}></i>
                            </>
                          )}
                        </div>
                      </button>
                    </li>
                    {showProdActions &&(
                      <>
                        {prodDeploymentDetails?.lastDeployedBranch && (
                          <li style={{ color: 'var(--color-orange)' }}>
                            [Branch - {prodDeploymentDetails?.lastDeployedBranch}]
                          </li>
                        )}
                        {codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner && (
                          <li>
                            <span
                              onClick={() => {
                                setShowVaultManagementModal(true);
                                setIsStaging(false);
                              }}
                            >
                              Environment variables config
                            </span>
                          </li>
                        )}
                        {prodDeploymentDetails?.gitjobRunID && (
                          <li>
                            <a
                              target="_blank"
                              href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildGitJobLogViewAWSURL(prodDeploymentDetails?.gitjobRunID) : buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                              rel="noreferrer"
                            >
                              Build &amp; Deploy Logs{' '}
                              {prodCodeDeployFailed && <span className={classNames(Styles.error)}>[Failed]</span>}{' '}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {prodDeployed && (
                          <li>
                            <a href={prodDeployedUrl} target="_blank" rel="noreferrer">
                              Deployed App URL {prodDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {prodDeploymentDetails?.lastDeploymentStatus && (
                          <li>
                            <a
                              target="_blank"
                              href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildLogViewAWSURL(prodDeployedUrl || projectDetails?.projectName.toLowerCase()) : buildLogViewURL(prodDeployedUrl || projectDetails?.projectName.toLowerCase())}
                              rel="noreferrer"
                            >
                              Application Logs <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {prodDeploymentDetails?.deploymentAuditLogs && (
                          <li>
                            <span
                              onClick={() => {
                                setShowAuditLogsModal(true);
                                setIsStaging(false);
                                setlogsList(prodDeploymentDetails?.deploymentAuditLogs);
                              }}
                            >
                              Deploy & Action Audit Logs
                            </span>
                          </li>
                        )}
                        {prodDeployed && (
                          <li>
                            <span
                              onClick={() => {
                                setEnv('prod');
                                setShowRestartModal(true);
                              }}
                            >
                              Restart Deployed Application
                            </span>
                          </li>
                        )}
                      </>
                    )}
                  </ul>
                </div>
              </div>
            )}
            {!enableOnboard && !creationFailed && !createInProgress && disableDeployment && serverStarted && (
                <div>
                <button
                  className={classNames('btn btn-primary', Styles.btnOutline)}
                  tooltip-data="Resource usage" 
                  onClick={() => window.open(resourceUsageUrl, "_blank")} 
                >
                  <i className="icon mbc-icon worksspace right" />
                </button>
              </div>
            )}
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Code Recipe</div>
              <div>{projectDetails?.recipeDetails?.recipeName ? projectDetails?.recipeDetails?.recipeName+'( '+projectDetails?.recipeDetails?.operatingSystem+', '+(resources[3]?.split('M')[0])/1000+'GB RAM, '+resources[4]+'CPU)' : 'N/A'}</div>
            </div>
            <div>
              <div>Environment</div>
              <div>{(projectDetails.recipeDetails.cloudServiceProvider === 'DHC-CaaS-AWS' || enableOnboard) ? 'DyP-CaaS AWS' : 'DyP-CaaS On-Prem'}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.projectDetails.projectCreatedOn)}</div>
            </div>
            <div>
              <div>Owner</div>
              <div>
                {codeSpace?.projectDetails?.projectOwner?.firstName +
                  ' ' +
                  codeSpace?.projectDetails?.projectOwner?.lastName +
                  ' (' +
                  codeSpace?.projectDetails?.projectOwner?.id +
                  ')'}
              </div>
            </div>
            {/* {!enableOnboard && !creationFailed && !createInProgress && !disableDeployment && (
              <>
                <div className={Styles.deploymentInfo}>
                  <div>
                    {intDeployed && (
                      <>
                        <strong>Staging:</strong>{' '}
                        <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                          (DORA Metrics)
                        </span>
                        {intCodeDeployFailed && (
                          <a
                            target="_blank"
                            className={classNames(Styles.error)}
                            tooltip-data="Last deployement failed on Staging - Click to view logs"
                            href={buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                            rel="noreferrer"
                          >
                            <i className="icon mbc-icon alert circle small right" />
                          </a>
                        )}
                        <br />
                        Branch '{intDeploymentDetails?.lastDeployedBranch}' deployed on
                        <br />
                        {intDeploymentDetails.gitjobRunID ? (
                          <a
                            target="_blank"
                            href={buildGitJobLogViewURL(intDeploymentDetails.gitjobRunID)}
                            tooltip-data="Show staging build & deploy logs in new tab"
                            rel="noreferrer"
                          >
                            {regionalDateAndTimeConversionSolution(intLastDeployedOn)}
                          </a>
                        ) : (
                          <>{regionalDateAndTimeConversionSolution(intLastDeployedOn)}&nbsp;</>
                        )}
                        <br />
                        by {intDeploymentDetails?.lastDeployedBy?.firstName}
                        <br />
                        <>
                          <span>
                            <a target="_blank" href={buildLogViewURL(intDeployedUrl, true)} rel="noreferrer">
                              <i
                                tooltip-data="Show Staging App logs in new tab"
                                className="icon mbc-icon workspace small right"
                              />
                            </a>
                          </span>
                          {isAPIRecipe && (
                            <span>
                              <i
                                onClick={() => {
                                  setShowVaultManagementModal(true);
                                  setIsStaging(true);
                                }}
                                tooltip-data="Staging Environment variables configuration"
                                className="icon mbc-icon document small right"
                              />
                            </span>
                          )}
                          {props?.codeSpace?.projectDetails?.intDeploymentDetails?.deploymentAuditLogs && (
                            <span>
                              <i
                                onClick={() => {
                                  setShowAuditLogsModal(true);
                                  setIsStaging(true);
                                  setlogsList(
                                    props?.codeSpace?.projectDetails?.intDeploymentDetails?.deploymentAuditLogs,
                                  );
                                }}
                                tooltip-data="Deployment Audit Logs - Staging"
                                className="icon mbc-icon reports small right"
                              />
                            </span>
                          )}
                        </>
                      </>
                    )}
                  </div>
                  <div>
                    {prodDeployed && (
                      <>
                        <strong>Production:</strong>{' '}
                        <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                          (DORA Metrics)
                        </span>
                        {prodCodeDeployFailed && (
                          <a
                            target="_blank"
                            tooltip-data="Last deployement failed on Production - Click to view logs"
                            className={classNames(Styles.error)}
                            href={buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                            rel="noreferrer"
                          >
                            <i className="icon mbc-icon alert circle small right" />
                          </a>
                        )}
                        <br />
                        Branch '{prodDeploymentDetails?.lastDeployedBranch}' deployed on
                        <br />
                        {prodDeploymentDetails.gitjobRunID ? (
                          <a
                            target="_blank"
                            href={buildGitJobLogViewURL(prodDeploymentDetails.gitjobRunID)}
                            tooltip-data="Show production build & deploy logs in new tab"
                            rel="noreferrer"
                          >
                            {regionalDateAndTimeConversionSolution(prodLastDeployedOn)}
                          </a>
                        ) : (
                          <>{regionalDateAndTimeConversionSolution(prodLastDeployedOn)}&nbsp;</>
                        )}
                        <br />
                        by {prodDeploymentDetails?.lastDeployedBy?.firstName}
                        <br />
                        <>
                          <span>
                            <a target="_blank" href={buildLogViewURL(prodDeployedUrl)} rel="noreferrer">
                              <i
                                tooltip-data="Show Production App logs in new tab"
                                className="icon mbc-icon workspace small right"
                              />
                            </a>
                          </span>
                          {isAPIRecipe && (
                            <span>
                              <i
                                onClick={() => {
                                  setShowVaultManagementModal(true);
                                  setIsStaging(false);
                                }}
                                tooltip-data="Production Environment variables configuration"
                                className="icon mbc-icon document small right"
                              />
                            </span>
                          )}
                          {props?.codeSpace?.projectDetails?.prodDeploymentDetails?.deploymentAuditLogs && (
                            <span>
                              <i
                                onClick={() => {
                                  setShowAuditLogsModal(true);
                                  setIsStaging(false);
                                  setlogsList(
                                    props?.codeSpace?.projectDetails?.prodDeploymentDetails?.deploymentAuditLogs,
                                  );
                                }}
                                tooltip-data="Deployment Audit Logs - Production"
                                className="icon mbc-icon reports small right"
                              />
                            </span>
                          )}
                        </>
                      </>
                    )}
                  </div>
                </div>
                <div>
                  <div>Last Deployed on</div>
                  <div>
                    {intDeployed && (
                      <>
                        Staging({intDeploymentDetails?.lastDeployedBranch}):
                        <br />
                        {!creationFailed && !enableOnboard && intDeploymentDetails.gitjobRunID ? (
                          <a
                            target="_blank"
                            href={buildGitJobLogViewURL(intDeploymentDetails.gitjobRunID)}
                            tooltip-data="Show staging build & deploy logs in new tab"
                            rel="noreferrer"
                          >
                            {regionalDateAndTimeConversionSolution(intLastDeployedOn)}
                          </a>
                        ) : (
                          <>{regionalDateAndTimeConversionSolution(intLastDeployedOn)}</>
                        )}
                        {!creationFailed && !enableOnboard && (
                          <a target="_blank" href={buildLogViewURL(intDeployedUrl, true)} rel="noreferrer">
                            <i
                              tooltip-data="Show Staging App logs in new tab"
                              className="icon mbc-icon workspace small right"
                            />
                          </a>
                        )}
                      </>
                    )}
                    <br />
                    {prodDeployed && (
                      <>
                        Production({prodDeploymentDetails?.lastDeployedBranch}):
                        <br />
                        {!creationFailed && !enableOnboard && prodDeploymentDetails.gitjobRunID ? (
                          <a
                            target="_blank"
                            href={buildGitJobLogViewURL(prodDeploymentDetails.gitjobRunID)}
                            tooltip-data="Show production build & deploy logs in new tab"
                            rel="noreferrer"
                          >
                            {regionalDateAndTimeConversionSolution(intLastDeployedOn)}
                          </a>
                        ) : (
                          <>{regionalDateAndTimeConversionSolution(prodLastDeployedOn)}</>
                        )}
                        {!creationFailed && !enableOnboard && (
                          <a target="_blank" href={buildLogViewURL(prodDeployedUrl)} rel="noreferrer">
                            <i
                              tooltip-data="Show Production App logs in new tab"
                              className="icon mbc-icon workspace small right"
                            />
                          </a>
                        )}
                      </>
                    )}
                  </div>
                </div> 
              </>
            )}*/}
          </div>
        </div>
        <div className={Styles.cardFooter}>
          {enableOnboard ? (
            <div>
              <span onClick={onCardNameClick} className={classNames(Styles.statusIndicator, Styles.colloboration)}>
                Collaboration Requested...
              </span>
            </div>
          ) : (
            <>
              <div>
                {!createInProgress && !creationFailed && !serverFailed && (
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
                )}
                {createInProgress ? (
                  <span className={classNames(Styles.statusIndicator, Styles.creating)}>Creating...</span>
                ) : (
                  <>
                    {!creationFailed && deployingInProgress && (
                      <a
                        href={ (codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? 
                          (intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED'
                            ? buildGitJobLogViewAWSURL(intDeploymentDetails?.gitjobRunID)
                            : buildGitJobLogViewAWSURL(prodDeploymentDetails?.gitjobRunID)) :
                          (intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED'
                            ? buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)
                            : buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID))
                        }
                        target="_blank"
                        rel="noreferrer"
                        className={Styles.deployingLink}
                        tooltip-data={
                          intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED'
                            ? 'Deploying to Staging'
                            : 'Deploying to Production'
                        }
                      >
                        <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Deploying...</span>
                      </a>
                    )}
                    {!creationFailed && deployed && (
                      <>
                        {!deployingInProgress &&
                          (intLastDeployedTime > prodLastDeployedTime ? (
                            intCodeDeployFailed ? (
                              <span className={classNames(Styles.statusIndicator, Styles.deployFailed)}>
                                <a
                                  href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildGitJobLogViewAWSURL(intDeploymentDetails?.gitjobRunID) : buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                                  target="_blank"
                                  rel="noreferrer"
                                  className={Styles.deployFailLink}
                                  tooltip-data={
                                    intDeploymentDetails?.deploymentAuditLogs
                                      ? 'Deployment to Staging failed on ' +
                                        regionalDateAndTimeConversionSolution(
                                          intDeploymentDetails?.deploymentAuditLogs[
                                            intDeploymentDetails?.deploymentAuditLogs?.length - 1
                                          ].triggeredOn,
                                        )
                                      : 'Deployment to staging failed'
                                  }
                                >
                                  Failed
                                </a>
                              </span>
                            ) : (
                              <span className={Styles.statusIndicator}>
                                <a
                                  href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildGitJobLogViewAWSURL(intDeploymentDetails?.gitjobRunID) : buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                                  target="_blank"
                                  rel="noreferrer"
                                  className={Styles.deployedLink}
                                  tooltip-data={
                                    'Deployed to Staging on ' +
                                    regionalDateAndTimeConversionSolution(intDeploymentDetails.lastDeployedOn)
                                  }
                                >
                                  Deployed
                                </a>
                              </span>
                            )
                          ) : prodCodeDeployFailed ? (
                            <span className={classNames(Styles.statusIndicator, Styles.deployFailed)}>
                              <a
                                href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildGitJobLogViewAWSURL(prodDeploymentDetails?.gitjobRunID) : buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                                target="_blank"
                                rel="noreferrer"
                                className={Styles.deployFailLink}
                                tooltip-data={
                                  prodDeploymentDetails?.deploymentAuditLogs
                                    ? 'Deployment to Production failed on ' +
                                      regionalDateAndTimeConversionSolution(
                                        prodDeploymentDetails?.deploymentAuditLogs[
                                          prodDeploymentDetails?.deploymentAuditLogs?.length - 1
                                        ].triggeredOn,
                                      )
                                    : 'Deployment to production failed'
                                }
                              >
                                Failed
                              </a>
                            </span>
                          ) : (
                            <span className={Styles.statusIndicator}>
                              <a
                                href={(codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' && deploymentMigrated) ? buildGitJobLogViewAWSURL(prodDeploymentDetails?.gitjobRunID) : buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                                target="_blank"
                                rel="noreferrer"
                                className={Styles.deployedLink}
                                tooltip-data={
                                  'Deployed to Production on ' +
                                  regionalDateAndTimeConversionSolution(prodDeploymentDetails.lastDeployedOn)
                                }
                              >
                                Deployed
                              </a>
                            </span>
                          ))}
                        {/* {intDeployed && (
                          <a
                            href={intDeployedUrl}
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployedLink}
                            tooltip-data="APP BASE URL - Staging"
                          >
                            <i className="icon mbc-icon link" /> Staging{' '}
                            {projectDetails?.intDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                          </a>
                        )}
                        {prodDeployed && (
                          <a
                            href={prodDeployedUrl}
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployedLink}
                            tooltip-data="APP BASE URL - Production"
                          >
                            <i className="icon mbc-icon link" /> Production{' '}
                            {projectDetails?.prodDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                          </a>
                        )} */}
                      </>
                    )}
                  </>
                )}
                {deleteInProgress && (
                  <span className={classNames(Styles.statusIndicator, Styles.deleting)}>Deleting...</span>
                )}
                {creationFailed && (
                  <span className={classNames(Styles.statusIndicator, Styles.deleting)}>Create Failed</span>
                )}
              </div>
              <div className={Styles.btnGrp}>
                {!disableDeployment &&
                  !isPublicRecipe &&
                  !createInProgress &&
                  !deployingInProgress &&
                  !creationFailed &&
                  isIAMRecipe &&
                  isOwner && (
                    <button className="btn btn-primary" onClick={() => onCodeSpaceSecurityConfigClick(codeSpace)}>
                      <IconGear size={'18'} />
                    </button>
                  )}
                {enableReadMe && (
                  <button className="btn btn-primary" onClick={() =>  getReadMeFile()}>
                    <i className={classNames("icon mbc-icon help", Styles.helpIcon)} tooltip-data="Steps to set up"></i>
                  </button>
                )}
                {!isPublicRecipe && !createInProgress && !deployingInProgress && !creationFailed && isOwner && (
                  <button className="btn btn-primary" onClick={() => props.onCodeSpaceEdit(codeSpace)}>
                    <i className="icon mbc-icon edit"></i>
                  </button>
                )}
                {!creationFailed && !deleteInProgress && !createInProgress && !deployingInProgress && (
                  <button className="btn btn-primary" onClick={onCodeSpaceDelete}>
                    <i className="icon delete"></i>
                  </button>
                )}
                {creationFailed && (
                  <button className="btn btn-primary" onClick={onRetryCreateClick}>
                    <i className="icon mbc-icon refresh"></i> Retry
                  </button>
                )}
              </div>
            </>
          )}
        </div>
      </div>
      {showVaultManagementModal && (
        <Modal
          title={isStaging ? 'Secret Management - Staging' : 'Secret Management - Production'}
          hiddenTitle={false}
          showAcceptButton={false}
          showCancelButton={false}
          acceptButtonTitle="Save"
          onAccept={() => console.log('save')}
          modalWidth={'70%'}
          modalStyle={{ minHeight: '86%' }}
          buttonAlignment="center"
          show={showVaultManagementModal}
          content={<VaultManagement projectName={projectDetails.projectName.toLowerCase()} isStaging={isStaging} />}
          scrollableContent={true}
          onCancel={() => setShowVaultManagementModal(false)}
        />
      )}
      {showAuditLogsModal && (
        <DeployAuditLogsModal
          deployedEnvInfo={isStaging ? 'Staging' : 'Production'}
          show={showAuditLogsModal}
          setShowAuditLogsModal={setShowAuditLogsModal}
          logsList={logsList}
          projectName={projectDetails.projectName.toLowerCase()}
        />
      )}
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle={allowDelete ? 'No' : 'OK'}
        showAcceptButton={allowDelete}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteCodeSpaceContent}
        onCancel={deleteCodeSpaceClose}
        onAccept={deleteCodeSpaceAccept}
      />

      {showReadMeModal && (
        <Modal
          showAcceptButton={false}
          showCancelButton={false}
          show={showReadMeModal}
          content={ <div dangerouslySetInnerHTML={{ __html: readMeContent }} />}
          scrollableContent={true}
          onCancel={() => setShowReadMeModal(false)}
          modalStyle={{
            width: '90%',
            maxHeight: '90%',
          }}
        />
      )}

      {showDoraMetricsModal && (
        <Modal
          title={`DORA Metrics for ` + projectDetails.projectName}
          showAcceptButton={true}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          acceptButtonTitle="Ok"
          show={showDoraMetricsModal}
          content={<DoraMetrics />}
          scrollableContent={false}
          onCancel={() => setShowDoraMetricsModal(false)}
          onAccept={() => setShowDoraMetricsModal(false)}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
          }}
        />
      )}
      { showRestartModal && (
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="Cancel"
        showAcceptButton={true}
        showCancelButton={true}
        show={showRestartModal}
        content={RestartContent}
        onCancel={() => {
          setEnv('');
          setShowRestartModal(false);
        }}
        onAccept={() => {
          onRestart(env);
          setShowRestartModal(false);
        }}
      />)}
      { showMigrateOrStartModal && (
        <ConfirmModal
          title={''}
          acceptButtonTitle="Migrate your workspace to DyP-CaaS(AWS)"
          cancelButtonTitle="Start your workspace on DyP-CaaS(On-Prem)"
          showAcceptButton={true}
          showCancelButton={true}
          show={showMigrateOrStartModal}
          content={migrateOrStartContent}
          onCancel={() => {
            props.onStartStopCodeSpace(codeSpace, handleServerStatusAndProgress, 'DHC-CaaS' );
            setShowMigrateOrStartModal(false);
          }}
          onAccept={onMigrateWorkplace}
        />
      )}
      {showOnPremStartModal && (
        <Modal
          title={'Manual Start'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
          }}
          buttonAlignment="center"
          show={showOnPremStartModal}
          content={onShowOnPremStartModal}
          onCancel={() => setShowOnPremStartModal(false)}
        />
      )}
    </>
  );
};
export default CodeSpaceCardItem;
