import classNames from 'classnames';
import React, { useState, useEffect, useRef, forwardRef } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import {
  regionalDateAndTimeConversionSolution,
  buildGitJobLogViewAWSURL,
} from '../../Utility/utils';
import ConfirmModal from 'dna-container/ConfirmModal';
import Modal from 'dna-container/Modal';
import { history } from '../../store';
// @ts-ignore
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { trackEvent } from '../../Utility/utils';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
import { IconGear } from 'dna-container/IconGear';
import { setRippleAnimation } from '../../common/modules/uilab/js/src/util';
import { marked } from 'marked';
import { Envs } from '../../Utility/envs';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ContextMenu from '../contextMenu/ContextMenu';

let isTouch = false;

const CodeSpaceCardItem = forwardRef((props, ref) => {
  let codeSpace = props.codeSpace;
  const enableOnboard = codeSpace ? codeSpace.status === 'COLLABORATION_REQUESTED' : false;
  const deleteInProgress = codeSpace.status === 'DELETE_REQUESTED';
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const creationFailed = codeSpace.status === 'CREATE_FAILED';

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const collaborator = codeSpace.projectDetails?.projectCollaborators?.find((collaborator) => {return collaborator?.id === props?.userInfo?.id });
  const isOwner = codeSpace.projectDetails?.projectOwner?.id === props.userInfo.id || collaborator?.isAdmin;
  const isApprover = codeSpace.projectDetails?.projectOwner?.id === props.userInfo.id || collaborator?.isApprover;
  const hasCollaborators = codeSpace.projectDetails?.projectCollaborators?.length > 0;
  const disableDeployment = !codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled;

  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState(0);

  const [serverStarted, setServerStarted] = useState(false);
  const [serverFailed, setServerFailed] = useState(false);
  const [serverProgress, setServerProgress] = useState(0);

  const stagingWrapperRef = useRef(null);
  const prodWrapperRef = useRef(null);
  const [showReadMeModal, setShowReadMeModal] = useState(false);
  const [readMeContent, setReadMeContent] = useState('');
  const enableReadMe =  Envs.CODESPACE_RECIEPES_ENABLE_README?.split(',')?.includes(codeSpace?.projectDetails?.recipeDetails?.Id) || false;
  const [showMigrateOrStartModal, setShowMigrateOrStartModal] = useState(false);

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

  const onCodeSpaceAuthorizationConfigClick = (codeSpace) => {
    if (codeSpace?.projectDetails?.publishedSecuirtyConfig) {
      history.push(
        `/codespace/publishedSecurityconfig/${codeSpace.id}?name=${codeSpace.projectDetails.projectName}?intIAM=${projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false}?intDna=${projectDetails?.intDeploymentDetails?.secureWithDnaRequired || false}?prodIAM=${projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || false}?prodDna=${projectDetails?.intDeploymentDetails?.secureWithDnaRequired || false}`,
      );
      return;
    }
    history.push(`codespace/securityconfig/${codeSpace.id}?name=${codeSpace.projectDetails.projectName}?intIAM=${projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false}?intDna=${projectDetails?.intDeploymentDetails?.secureWithDnaRequired || false}?prodIAM=${projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || false}?prodDna=${projectDetails?.intDeploymentDetails?.secureWithDnaRequired || false}`);
  };

  const onCodeSpaceDelete = () => {
    if (creationFailed) {
      deleteCodeSpaceAccept();
    } else {
      setShowDeleteModal(true);
    }
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
  const intDeploymentDetails = projectDetails?.intDeploymentDetails;
  const prodDeploymentDetails = projectDetails?.prodDeploymentDetails;
  const deployingInProgress =
    intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED' || 
    prodDeploymentDetails?.lastDeploymentStatus === 'APPROVAL_PENDING' ||
    projectDetails?.lastBuildOrDeployedStatus === 'APPROVAL_PENDING';
  const buildInProgress = projectDetails?.lastBuildOrDeployedStatus === 'BUILD_REQUESTED';
  const allowDelete = codeSpace?.projectDetails?.projectOwner?.id === props.userInfo.id ? !hasCollaborators : true;
  const isPublicRecipe = projectDetails?.recipeDetails?.recipeId?.startsWith('public');
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

  // const isIAMRecipe =
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
  //   props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven' ;

  const resources = projectDetails?.recipeDetails?.resource?.split(',');

  const resourceUsageUrl = Envs.MONITORING_DASHBOARD_BASE_URL + `codespace-cpu-and-memory-usage?orgId=1&from=now-1h&to=now&var-namespace=${Envs.CODESERVER_NAMESPACE}&var-pod=${codeSpace.workspaceId}&var-container=notebook`;

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

  const handleDragStart = (e) => {
    e.dataTransfer.setData("application/json", JSON.stringify(codeSpace));
  }

  return (
    <>
      <div
        id={'card-' + codeSpace.id}
        draggable={true}
        ref={ref}
        key={codeSpace.id}
        onDragStart={handleDragStart}
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
              <label onClick={onCardNameClick}>{projectDetails?.projectName}</label>
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
                <ContextMenu
                  codeSpace={props?.codeSpace}
                  userInfo={props?.userInfo}
                  showContextMenu={showContextMenu}
                  // toggleContextMenu={toggleContextMenu}
                  contextMenuOffsetTop={contextMenuOffsetTop}
                  contextMenuOffsetLeft={contextMenuOffsetLeft}
                  stagingWrapperRef={stagingWrapperRef}
                  prodWrapperRef={prodWrapperRef}
                  onShowDeployModal={props?.onShowDeployModal}
                  serverStarted={serverStarted}
                  onStartStopCodeSpace={props?.onStartStopCodeSpace}
                  handleServerStatusAndProgress={handleServerStatusAndProgress}
                  onShowBlueprintModal={props?.onShowBlueprintModal}
                  onShowBuildModal={props?.onShowBuildModal}
                />
              </div>
            )}
            {!enableOnboard && !creationFailed && !createInProgress && disableDeployment && serverStarted && (
                <div>
                <button
                  className={classNames('btn btn-primary', Styles.btnOutline)}
                  tooltip-data="Workspace Resource Usage" 
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
              <div>{(projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' || enableOnboard) ? 'DyP-CaaS AWS' : 'DyP-CaaS On-Prem'}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.projectDetails?.projectCreatedOn)}</div>
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
                  !creationFailed && projectDetails?.lastBuildOrDeployedStatus && (
                    <>
                      {buildInProgress && (
                        <a
                          href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                            ? buildGitJobLogViewAWSURL(projectDetails?.intBuildDetails?.gitjobRunID)
                            : buildGitJobLogViewAWSURL(projectDetails?.prodBuildDetails?.gitjobRunID)
                          }
                          target="_blank"
                          rel="noreferrer"
                          className={Styles.deployingLink}
                          tooltip-data={
                            projectDetails?.lastBuildOrDeployedEnv === 'int'
                              ? 'Building Staging environment'
                              : 'Building Production environment'
                          }
                        >
                          <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Building...</span>
                        </a>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'DEPLOY_REQUESTED' && (
                        <a
                          href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                            ? buildGitJobLogViewAWSURL(projectDetails?.intDeploymentDetails?.gitjobRunID)
                            : buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)
                          }
                          target="_blank"
                          rel="noreferrer"
                          className={Styles.deployingLink}
                          tooltip-data={
                            projectDetails?.lastBuildOrDeployedEnv === 'int'
                              ? 'Deploying to Staging'
                              : 'Deploying to Production'
                          }
                        >
                          <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Deploying...</span>
                        </a>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'BUILD_FAILED' && (
                        <span className={classNames(Styles.statusIndicator, Styles.deployFailed)}>
                          <a
                            href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                              ? buildGitJobLogViewAWSURL(projectDetails?.intBuildDetails?.gitjobRunID)
                              : buildGitJobLogViewAWSURL(projectDetails?.prodBuildDetails?.gitjobRunID)
                            }
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployFailLink}
                            tooltip-data={
                             `Build to ${projectDetails?.lastBuildOrDeployedEnv === 'int' ? 'staging' : 'production'} failed on ` +
                              regionalDateAndTimeConversionSolution(projectDetails?.lastBuildOrDeployedOn)
                            }
                          >
                           Failed
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'DEPLOYMENT_FAILED' && (
                        <span className={classNames(Styles.statusIndicator, Styles.deployFailed)}>
                          <a
                            href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                              ? buildGitJobLogViewAWSURL(projectDetails?.intDeploymentDetails?.gitjobRunID)
                              : buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)
                            }
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployFailLink}
                            tooltip-data={
                             `Deployment to ${projectDetails?.lastBuildOrDeployedEnv === 'int' ? 'staging' : 'production'} failed on ` +
                              regionalDateAndTimeConversionSolution(projectDetails?.lastBuildOrDeployedOn)
                            }
                          >
                           Failed
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'BUILD_SUCCESS' && (
                        <span className={Styles.statusIndicator}>
                          <a
                            href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                              ? buildGitJobLogViewAWSURL(projectDetails?.intBuildDetails?.gitjobRunID)
                              : buildGitJobLogViewAWSURL(projectDetails?.prodBuildDetails?.gitjobRunID)
                            }
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployedLink}
                            tooltip-data={
                              `Build to ${projectDetails?.lastBuildOrDeployedEnv === 'int' ? 'staging' : 'production'} on ` +
                              regionalDateAndTimeConversionSolution(projectDetails?.lastBuildOrDeployedOn)
                            }
                          >
                            Built
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'DEPLOYED' && (
                        <span className={Styles.statusIndicator}>
                          <a
                            href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                              ? buildGitJobLogViewAWSURL(projectDetails?.intDeploymentDetails?.gitjobRunID)
                              : buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)
                            }
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployedLink}
                            tooltip-data={
                              `Deployed to ${projectDetails?.lastBuildOrDeployedEnv === 'int' ? 'staging' : 'production'} on ` +
                              regionalDateAndTimeConversionSolution(projectDetails?.lastBuildOrDeployedOn)
                            }
                          >
                            Deployed
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'APPROVAL_PENDING' && (
                        
                        <span className={classNames(Styles.statusIndicator, Styles.deploying)}>

                          <a
                            href={buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)}
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployingLink}
                            tooltip-data={
                              `Deploment to production requires approval`
                            }
                          >
                            Pending...
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'APPROVAL_REJECTED' && (
                        <span className={classNames(Styles.statusIndicator, Styles.deployFailed)}>
                          <a
                            href={buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)}
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployFailLink}
                            tooltip-data={
                              `Production deployment rejected by approver`
                            }
                          >
                            Rejected
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'RESTART_REQUESTED' && (
                        <span className={classNames(Styles.statusIndicator, Styles.deploying)}>
                          <a
                            href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                              ? buildGitJobLogViewAWSURL(projectDetails?.intDeploymentDetails?.gitjobRunID)
                              : buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)
                            }
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployingLink}
                            tooltip-data={
                              projectDetails?.lastBuildOrDeployedEnv === 'int'
                                ? 'Restarting Staging deployment'
                                : 'Restarting Production deployment'
                            }
                          >
                            <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Restarting...</span>
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'RESTART_FAILED' && (
                        <span className={classNames(Styles.statusIndicator, Styles.deployFailed)}>
                          <a
                            href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                              ? buildGitJobLogViewAWSURL(projectDetails?.intDeploymentDetails?.gitjobRunID)
                              : buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)
                            }
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployFailLink}
                            tooltip-data={
                             `${projectDetails?.lastBuildOrDeployedEnv === 'int' ? 'Staging' : 'Production'} restart failed on ` +
                              regionalDateAndTimeConversionSolution(projectDetails?.lastBuildOrDeployedOn)
                            }
                          >
                           Failed
                          </a>
                        </span>
                      )}
                      {projectDetails?.lastBuildOrDeployedStatus === 'RESTARTED' && (
                        <span className={Styles.statusIndicator}>
                          <a
                            href={(projectDetails?.lastBuildOrDeployedEnv === 'int')
                              ? buildGitJobLogViewAWSURL(projectDetails?.intDeploymentDetails?.gitjobRunID)
                              : buildGitJobLogViewAWSURL(projectDetails?.prodDeploymentDetails?.gitjobRunID)
                            }
                            target="_blank"
                            rel="noreferrer"
                            className={Styles.deployedLink}
                            tooltip-data={
                              `${projectDetails?.lastBuildOrDeployedEnv === 'int' ? 'Staging' : 'Production'} deployment restarted on ` +
                              regionalDateAndTimeConversionSolution(projectDetails?.lastBuildOrDeployedOn)
                            }
                          >
                            Restarted
                          </a>
                        </span>
                      )}
                    </>
                  ) 
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
                  !buildInProgress &&
                  !creationFailed &&
                  isOwner && (
                    <button className="btn btn-primary" onClick={() => onCodeSpaceAuthorizationConfigClick(codeSpace)}>
                      <IconGear size={'18'} />
                    </button>
                )}
                {enableReadMe && (
                  <button className="btn btn-primary" onClick={() =>  getReadMeFile()}>
                    <i className={classNames("icon mbc-icon help", Styles.helpIcon)} tooltip-data="Steps to set up"></i>
                    </button>
                  )}
                {!isPublicRecipe && !createInProgress && !deployingInProgress && !buildInProgress && !creationFailed && isOwner && (
                  <button className="btn btn-primary" onClick={() => props.onCodeSpaceEdit(codeSpace)}>
                    <i className="icon mbc-icon edit"></i>
                  </button>
                )}
                 {isApprover && !disableDeployment && projectDetails?.lastBuildOrDeployedStatus === 'APPROVAL_PENDING' && (
                    <button
                      className={classNames('btn btn-primary')}
                      tooltip-data="Deployment Approval"
                      onClick={()=>{props.onShowDeployApprovalModal(codeSpace)}}
                    >
                      <i className={classNames('icon mbc-icon back_files', Styles.trainingIcon)} />
                    </button>
                  )}
                {!creationFailed && !deleteInProgress && !createInProgress && !deployingInProgress && !buildInProgress && (
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
    </>
  );
});

// Add a displayName for debugging
CodeSpaceCardItem.displayName = 'CodeSpaceCardItem';
export default CodeSpaceCardItem;
