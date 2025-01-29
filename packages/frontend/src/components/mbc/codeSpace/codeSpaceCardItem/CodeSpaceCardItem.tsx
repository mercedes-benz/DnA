import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import {
  // recipesMaster,
  regionalDateAndTimeConversionSolution,
  buildLogViewURL,
  buildGitJobLogViewURL,
  buildGitUrl,
} from '../../../../services/utils';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import Modal from 'components/formElements/modal/Modal';
import { history } from '../../../../router/History';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { ICodeSpaceData } from '../CodeSpace';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { trackEvent } from '../../../../services/utils';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { IUserInfo } from 'globals/types';
import { IconGear } from 'components/icons/IconGear';
import { DEPLOYMENT_DISABLED_RECIPE_IDS } from 'globals/constants';
import DoraMetrics from '../doraMetrics/DoraMetrics';
import VaultManagement from '../vaultManagement/VaultManagement';
import DeployAuditLogsModal from '../deployAuditLogsModal/DeployAuditLogsModal';

interface CodeSpaceCardItemProps {
  userInfo: IUserInfo;
  codeSpace: ICodeSpaceData;
  onDeleteSuccess?: () => void;
  toggleProgressMessage?: (show: boolean) => void;
  onShowCodeSpaceOnBoard: (codeSpace: ICodeSpaceData, isRetryRequest?: boolean) => void;
  onCodeSpaceEdit: (codeSpace: ICodeSpaceData) => void;
  onShowDeployModal: (codeSpace: ICodeSpaceData) => void;
  onStartStopCodeSpace: (codeSpace: ICodeSpaceData, startSuccessCB: () => void) => void;
}

let isTouch = false;

const CodeSpaceCardItem = (props: CodeSpaceCardItemProps) => {
  const codeSpace = props.codeSpace;
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
  const disableDeployment =
    codeSpace?.projectDetails?.recipeDetails?.recipeId.startsWith('public') ||
    DEPLOYMENT_DISABLED_RECIPE_IDS.includes(codeSpace?.projectDetails?.recipeDetails?.recipeId);
  const [showDoraMetricsModal, setShowDoraMetricsModal] = useState(false);
  const [isStaging, setIsStaging] = useState(false);
  const [logsList, setlogsList] = useState([]);

  const [showContextMenu, setShowContextMenu] = useState<boolean>(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState<number>(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState<number>(0);

  const [serverStarted, setServerStarted] = useState(false);
  const [serverFailed, setServerFailed] = useState(false);
  const [serverProgress, setServerProgress] = useState(0);


  useEffect(() => {

    handleServerStatusAndProgress();

    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);
    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
    };
  }, []);

  const handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }

    const target = event.target as Element;
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

  const toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
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
                before deleting this code space '{codeSpace?.projectDetails?.projectName}'.
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
    CodeSpaceApiClient.deleteCodeSpace(codeSpace.workspaceId)
      .then((res: any) => {
        trackEvent('DnA Code Space', 'Delete', 'Delete code space');
        if (res.success === 'SUCCESS') {
          props.onDeleteSuccess();
          setShowDeleteModal(false);
          ProgressIndicator.hide();
          Notification.show(`Code space '${codeSpace.projectDetails?.projectName}' has been deleted successfully.`);
        } else {
          ProgressIndicator.hide();
          Notification.show('Error in deleting code space. Please try again later.\n' + res.errors[0].message, 'alert');
        }
      })
      .catch((err: Error) => {
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

  const onRetryCreateClick = () => {
    props.onShowCodeSpaceOnBoard(codeSpace, true);
  };

  const onCodeSpaceSecurityConfigClick = (codeSpace: ICodeSpaceData) => {
    if (codeSpace?.projectDetails?.publishedSecuirtyConfig) {
      history.push(
        `/codespace/publishedSecurityconfig/${codeSpace.id}?name=${codeSpace.projectDetails.projectName}`,
      );
      return;
    }
    history.push(`codespace/securityconfig/${codeSpace.id}?name=${codeSpace.projectDetails.projectName}`);
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

  const onStartStopCodeSpace = (codespace: ICodeSpaceData) => {
    props.onStartStopCodeSpace(codespace, handleServerStatusAndProgress);
  };

  const handleServerStatusAndProgress = () => {
    codeSpace.serverStatus = 'SERVER_STOPPED';
    CodeSpaceApiClient.serverStatusFromHub(props.userInfo.id.toLowerCase(), codeSpace.workspaceId, (e: any) => {
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
  const intLastDeployedTime = new Date(regionalDateAndTimeConversionSolution(intDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ? intDeploymentDetails?.lastDeployedOn : (intDeploymentDetails?.deploymentAuditLogs && intDeploymentDetails?.deploymentAuditLogs[intDeploymentDetails?.deploymentAuditLogs?.length - 1]?.triggeredOn ))).getTime();
  const prodDeployed =
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ||
    (prodDeployedUrl !== null && prodDeployedUrl !== 'null') ||
    false;
  const prodCodeDeployFailed = prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
  const prodLastDeployedTime = new Date(regionalDateAndTimeConversionSolution(prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ? prodDeploymentDetails?.lastDeployedOn : (prodDeploymentDetails?.deploymentAuditLogs && prodDeploymentDetails?.deploymentAuditLogs[prodDeploymentDetails?.deploymentAuditLogs?.length - 1]?.triggeredOn ))).getTime();

  const deployed = intDeployed || prodDeployed || prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED' || intDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
  const allowDelete = codeSpace?.projectDetails?.projectOwner?.id === props.userInfo.id ? !hasCollaborators : true;
  const isPublicRecipe = projectDetails.recipeDetails?.recipeId.startsWith('public');
  const isAPIRecipe =
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'dash' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'streamlit' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'nestjs' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven' ;


  const isIAMRecipe =
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
    props.codeSpace.projectDetails?.recipeDetails?.recipeId === 'springbootwithmaven' ;

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
                    <li>
                      <hr />
                    </li>
                    <li>
                      <strong>Staging:</strong>{' '}
                      {intDeploymentDetails?.lastDeployedBranch
                        ? `[Branch - ${intDeploymentDetails?.lastDeployedBranch}]`
                        : 'No Deployment'}
                      <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                        (DORA Metrics)
                      </span>
                    </li>
                    {isAPIRecipe && (
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
                          href={buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
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
                          href={buildLogViewURL(intDeployedUrl || projectDetails?.projectName.toLowerCase(), true)}
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
                          Deployment Audit Logs
                        </span>
                      </li>
                    )}
                    <li>
                      <hr />
                    </li>
                    <li>
                      <strong>Production:</strong>{' '}
                      {prodDeploymentDetails?.lastDeployedBranch
                        ? `[Branch - ${prodDeploymentDetails?.lastDeployedBranch}]`
                        : 'No Deployment'}
                      <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                        (DORA Metrics)
                      </span>
                    </li>
                    {isAPIRecipe && (
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
                          href={buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
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
                          href={buildLogViewURL(prodDeployedUrl || projectDetails?.projectName.toLowerCase())}
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
                          Deployment Audit Logs
                        </span>
                      </li>
                    )}
                  </ul>
                </div>
              </div>
            )}
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Code Recipe</div>
              {/* <div>{recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId)?.name}</div> */}
              <div>{projectDetails?.recipeDetails?.recipeName ? projectDetails?.recipeDetails?.recipeName+'( '+projectDetails?.recipeDetails?.operatingSystem+', '+projectDetails?.recipeDetails?.ramSize+'GB RAM, '+projectDetails?.recipeDetails?.cpuCapacity+'CPU)' : 'N/A'}</div>
            </div>
            <div>
              <div>Environment</div>
              <div>{projectDetails.recipeDetails.cloudServiceProvider}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.projectDetails.projectCreatedOn)}</div>
            </div>
            <div>
              <div>Owner</div>
              <div>{codeSpace?.projectDetails?.projectOwner?.firstName +' '+codeSpace?.projectDetails?.projectOwner?.lastName+' ('+codeSpace?.projectDetails?.projectOwner?.id +')'}</div>
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
                    title={'Please contact Codespace Admin'}
                    className={classNames(Styles.statusIndicator, Styles.wsStartStop, Styles.wsStarted)}
                  >
                    Server Start Failed
                  </span>
                )}
                {createInProgress ? (
                  <span className={classNames(Styles.statusIndicator, Styles.creating)}>Creating...</span>
                ) : (
                  <>
                    {!creationFailed && deployingInProgress && (
                      <a
                      href={intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED' ? buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID) : buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                      target="_blank"
                      rel="noreferrer"
                      className={Styles.deployingLink}
                      tooltip-data={intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED'? 'Deploying to Staging' : 'Deploying to Production'}
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
                                  href={buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                                  target="_blank"
                                  rel="noreferrer"
                                  className={Styles.deployFailLink}
                                  tooltip-data={intDeploymentDetails?.deploymentAuditLogs ?
                                    'Deployment to Staging failed on ' +
                                    regionalDateAndTimeConversionSolution(intDeploymentDetails?.deploymentAuditLogs[intDeploymentDetails?.deploymentAuditLogs?.length - 1].triggeredOn) : 'Deployment to staging failed'
                                  }
                                >
                                  Failed
                                </a>
                              </span>
                            ) : (
                              <span className={Styles.statusIndicator}>
                                <a
                                  href={buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
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
                                href={buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                                target="_blank"
                                rel="noreferrer"
                                className={Styles.deployFailLink}
                                tooltip-data={prodDeploymentDetails?.deploymentAuditLogs ? 
                                  'Deployment to Production failed on ' +
                                  regionalDateAndTimeConversionSolution(prodDeploymentDetails?.deploymentAuditLogs[prodDeploymentDetails?.deploymentAuditLogs?.length - 1].triggeredOn) : 'Deployment for production failed'
                                }
                              >
                                Failed
                              </a>
                            </span>
                          ) : (
                            <span className={Styles.statusIndicator}>
                              <a
                                href={buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
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
    </>
  );
};
export default CodeSpaceCardItem;
