import React, { useState } from 'react';
import classNames from 'classnames';
import Styles from './ContextMenu.scss';
import { Envs } from '../../Utility/envs';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import DoraMetrics from '../doraMetrics/DoraMetrics';
import VaultManagement from '../vaultManagement/VaultManagement';
import DeployAuditLogsModal from '../deployAuditLogsModal/DeployAuditLogsModal';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import {
  buildLogViewURL,
  buildGitJobLogViewURL,
  buildLogViewAWSURL,
  buildGitJobLogViewAWSURL,
  buildGitUrl,
} from '../../Utility/utils';

const ContextMenu = (props) => {
  const codeSpace = props?.codeSpace;
  const projectDetails = codeSpace?.projectDetails;
  const intDeploymentDetails = projectDetails.intDeploymentDetails;
  const prodDeploymentDetails = projectDetails.prodDeploymentDetails;
  const intDeployedUrl = intDeploymentDetails?.deploymentUrl;
  const prodDeployedUrl = prodDeploymentDetails?.deploymentUrl;

  const [showOnPremStartModal, setShowOnPremStartModal] = useState(false);
  const [showStagingActions, setShowStagingActions] = useState(false);
  const [showProdActions, setShowProdActions] = useState(false);
  const [showDoraMetricsModal, setShowDoraMetricsModal] = useState(false);
  const [showVaultManagementModal, setShowVaultManagementModal] = useState(false);
  const [isStaging, setIsStaging] = useState(false);
  const [showAuditLogsModal, setShowAuditLogsModal] = useState(false);
  const [logsList, setlogsList] = useState([]);
  const [env, setEnv] = useState('');
  const [showRestartModal, setShowRestartModal] = useState(false);

  const deployingInProgress =
    intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED';

  const resourceUsageUrl =
    Envs.MONITORING_DASHBOARD_BASE_URL +
    `codespace-cpu-and-memory-usage?orgId=1&from=now-1h&to=now&var-namespace=${Envs.CODESERVER_NAMESPACE}&var-pod=${codeSpace.workspaceId}&var-container=notebook`;

  const intDeploymentMigrated = intDeployedUrl?.includes(Envs.CODESPACE_AWS_POPUP_URL);
  const prodDeploymentMigrated = prodDeployedUrl?.includes(Envs.CODESPACE_AWS_POPUP_URL);

  const prodCodeDeployFailed = prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
  const intCodeDeployFailed = intDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';

  const intDeployed =
    intDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ||
    (intDeployedUrl !== null && intDeployedUrl !== 'null') ||
    false;

  const prodDeployed =
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ||
    (prodDeployedUrl !== null && prodDeployedUrl !== 'null') ||
    false;

  const intAppResourceUsageUrl =
    Envs.MONITORING_DASHBOARD_APP_BASE_URL +
    `codespace-app-cpu-and-memory-usage?orgId=1&var-namespace=${Envs.CODESERVER_APP_NAMESPACE}&var-app=${projectDetails?.projectName}-int&var-container=`;

  const prodAppResourceUsageUrl =
    Envs.MONITORING_DASHBOARD_APP_BASE_URL +
    `codespace-app-cpu-and-memory-usage?orgId=1&var-namespace=${Envs.CODESERVER_APP_NAMESPACE}&var-app=${projectDetails?.projectName}-prod&var-container=`;

  const collaborator = codeSpace.projectDetails?.projectCollaborators?.find((collaborator) => {
    return collaborator?.id === props?.userInfo?.id;
  });
  const isOwner = codeSpace.projectDetails?.projectOwner?.id === props.userInfo.id || collaborator?.isAdmin;

  const onRestart = (env) => {
    ProgressIndicator.show();
    CodeSpaceApiClient.restartDeployments(codeSpace?.id, env)
      .then((res) => {
        if (res.data.success === 'SUCCESS') {
          ProgressIndicator.hide();
          Notification.show('Restart requested successfully');
        } else {
          ProgressIndicator.hide();
          Notification.show(
            'Error in Restarting deployed application. Please try again later.\n' + res?.data?.errors[0]?.message,
            'alert',
          );
        }
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show(
          'Error in Restarting deployed application. Please try again later.\n' +
            err?.response?.data?.errors[0]?.message,
          'alert',
        );
      });
    setShowRestartModal(false);
  };

  const RestartContent = (
    <div>
      <h3>Are you sure you want to restart your deployed application?</h3>
      <p>Note: Please refresh and check the application restart status under action audit logs.</p>
    </div>
  );

  const onShowOnPremStartModal = (
    <div>
      <p>
        Click on the Start button to start your workspace incase the link is inaccessible. If you have already started
        before then access your workspace through the link provided. Please note that the link may take some time to be
        accessible after the start.
      </p>
      <div className={Styles.manualStart}>
        <div>
          <button
            className={classNames('btn btn-tertiary')}
            onClick={() => {
              props.onStartStopCodeSpace(codeSpace, props.handleServerStatusAndProgress, 'DHC-CaaS', true);
            }}
          >
            Start your old workspace
          </button>
        </div>
        <div>
          <a
            target="_blank"
            href={
              Envs.CODESPACE_OIDC_POPUP_URL +
              'user/' +
              codeSpace?.workspaceOwner?.id.toLowerCase() +
              '/' +
              codeSpace?.workspaceId +
              '/?folder=/home/coder/app'
            }
            rel="noreferrer"
          >
            Your old workspace URL
          </a>
        </div>
      </div>
    </div>
  );

  const handleOpenDoraMetrics = () => {
    setShowDoraMetricsModal(true);
  };

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

  return (
    <>
      <div
        style={{
          top: props?.contextMenuOffsetTop + 'px',
          left: props?.contextMenuOffsetLeft + 'px',
          zIndex: 5,
        }}
        className={classNames('contextMenuWrapper', Styles.contextMenu, props?.showContextMenu ? '' : 'hide')}
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
          {props?.serverStarted && (
            <li>
              <a target="_blank" href={resourceUsageUrl} rel="noreferrer">
                Workspace Resource Usage
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
              className={classNames(
                'btn btn-primary',
                Styles.btnOutline,
                !(
                  (codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) ||
                  intDeploymentDetails?.deploymentAuditLogs
                ) && Styles.btnDisabled,
              )}
              onClick={() => {
                setShowStagingActions(!showStagingActions);
              }}
            >
              <div>
                <strong>Staging:</strong> {intDeploymentDetails?.lastDeployedBranch ? 'Deployed' : 'No Deployment'}
                <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                  (DORA Metrics)
                </span>
              </div>
              <div
                ref={props?.stagingWrapperRef}
                className={classNames(Styles.collapseIcon, showStagingActions ? Styles.open : '')}
              >
                {((codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) ||
                  intDeploymentDetails?.deploymentAuditLogs) && (
                  <>
                    <span className={classNames('animation-wrapper', Styles.animationWrapper)}></span>
                    <i className={classNames('icon down-up-flip')}></i>
                  </>
                )}
              </div>
            </button>
          </li>
          {showStagingActions && (
            <>
              {intDeploymentDetails?.lastDeployedBranch && (
                <li style={{ color: 'var(--color-orange)' }}>[Branch - {intDeploymentDetails?.lastDeployedBranch}]</li>
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
                    href={
                      codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' &&
                      intDeploymentMigrated
                        ? buildGitJobLogViewAWSURL(intDeploymentDetails?.gitjobRunID)
                        : buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)
                    }
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
                    href={
                      codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' &&
                      intDeploymentMigrated
                        ? buildLogViewAWSURL(intDeployedUrl || projectDetails?.projectName.toLowerCase(), true)
                        : buildLogViewURL(intDeployedUrl || projectDetails?.projectName.toLowerCase(), true)
                    }
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
                    onClick={() => {
                      setEnv('int');
                      setShowRestartModal(true);
                    }}
                  >
                    Restart Deployed Application
                  </span>
                </li>
              )}
              {intDeployed && intDeploymentMigrated && (
                <li>
                  <a target="_blank" href={intAppResourceUsageUrl} rel="noreferrer">
                    Deployed App Resource Usage
                    <i className="icon mbc-icon new-tab" />
                  </a>
                </li>
              )}
            </>
          )}
          <li>
            <hr />
          </li>
          <li>
            <button
              className={classNames(
                'btn btn-primary',
                Styles.btnOutline,
                !(
                  (codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) ||
                  prodDeploymentDetails?.deploymentAuditLogs
                ) && Styles.btnDisabled,
              )}
              onClick={() => {
                setShowProdActions(!showProdActions);
              }}
            >
              <div>
                <strong>Production:</strong> {prodDeploymentDetails?.lastDeployedBranch ? 'Deployed' : 'No Deployment'}
                <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                  (DORA Metrics)
                </span>
              </div>
              <div
                ref={props?.prodWrapperRef}
                className={classNames(Styles.collapseIcon, showProdActions ? Styles.open : '')}
              >
                {((codeSpace?.projectDetails?.recipeDetails?.isDeployEnabled && isOwner) ||
                  prodDeploymentDetails?.deploymentAuditLogs) && (
                  <>
                    <span className={classNames('animation-wrapper', Styles.animationWrapper)}></span>
                    <i className={classNames('icon down-up-flip')}></i>
                  </>
                )}
              </div>
            </button>
          </li>
          {showProdActions && (
            <>
              {prodDeploymentDetails?.lastDeployedBranch && (
                <li style={{ color: 'var(--color-orange)' }}>[Branch - {prodDeploymentDetails?.lastDeployedBranch}]</li>
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
                    href={
                      codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' &&
                      prodDeploymentMigrated
                        ? buildGitJobLogViewAWSURL(prodDeploymentDetails?.gitjobRunID)
                        : buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)
                    }
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
                    href={
                      codeSpace?.projectDetails?.recipeDetails?.cloudServiceProvider === 'DHC-CaaS-AWS' &&
                      prodDeploymentMigrated
                        ? buildLogViewAWSURL(prodDeployedUrl || projectDetails?.projectName.toLowerCase())
                        : buildLogViewURL(prodDeployedUrl || projectDetails?.projectName.toLowerCase())
                    }
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
              {prodDeployed && prodDeploymentMigrated && (
                <li>
                  <a target="_blank" href={prodAppResourceUsageUrl} rel="noreferrer">
                    Deployed App Resource Usage
                    <i className="icon mbc-icon new-tab" />
                  </a>
                </li>
              )}
            </>
          )}
        </ul>
      </div>
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
      {showRestartModal && (
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
        />
      )}
    </>
  );
};
export default ContextMenu;
