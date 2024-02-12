import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import { recipesMaster, regionalDateAndTimeConversionSolution, buildLogViewURL, buildGitJobLogViewURL } from '../../../../services/utils';
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

interface CodeSpaceCardItemProps {
  userInfo: IUserInfo;
  codeSpace: ICodeSpaceData;
  onDeleteSuccess?: () => void;
  toggleProgressMessage?: (show: boolean) => void;
  onShowCodeSpaceOnBoard: (codeSpace: ICodeSpaceData, isRetryRequest?: boolean) => void;
  onCodeSpaceEdit: (codeSpace: ICodeSpaceData) => void;
}

const CodeSpaceCardItem = (props: CodeSpaceCardItemProps) => {
  const codeSpace = props.codeSpace;
  // const collaborationCodeSpace = codeSpace.projectDetails.projectCollaborators?.find((user: ICodeCollaborator) => user.id === props.userInfo.id);
  const enableOnboard = codeSpace ? codeSpace.status === 'COLLABORATION_REQUESTED' : false;
  // const codeDeploying = codeSpace.status === 'DEPLOY_REQUESTED';
  const deleteInProgress = codeSpace.status === 'DELETE_REQUESTED';
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const creationFailed = codeSpace.status === 'CREATE_FAILED';
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const recipes = recipesMaster;
  const isOwner = codeSpace.projectDetails?.projectOwner?.id === props.userInfo.id;
  const hasCollaborators = codeSpace.projectDetails?.projectCollaborators?.length > 0;
  const disableDeployment = codeSpace?.projectDetails?.recipeDetails?.recipeId.startsWith('public') || DEPLOYMENT_DISABLED_RECIPE_IDS.includes(codeSpace?.projectDetails?.recipeDetails?.recipeId);
  const [showDoraMetricsModal, setShowDoraMetricsModal] = useState(false);

  const deleteCodeSpaceContent = (
    <div>
      <h3>
        {/* Are you sure to delete {codeSpace.projectDetails.projectName} Code Space?
        <br /> */}
        {isOwner ? (
          <>
            {hasCollaborators ? (
              <>
                You have collaborators in your project.
                <br />
                Please transfer your ownership to any one of the collaborator <br /> or remove the collaborator(s) before
                deleting this code space '{codeSpace?.projectDetails?.projectName}'.
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
      .then((res: any) => {
        trackEvent('DnA Code Space', 'Delete', 'Delete code space');
        if (res.success === 'SUCCESS') {
          props.onDeleteSuccess();
          setShowDeleteModal(false);
          ProgressIndicator.hide();
          Notification.show(`Code space '${codeSpace.projectDetails?.projectName}' has been deleted successfully.`);
        } else {
          ProgressIndicator.hide();
          Notification.show(
            'Error in deleting code space. Please try again later.\n' + res.errors[0].message,
            'alert',
          );
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
    } else {
      history.push(`codespace/${codeSpace.workspaceId}`);
    }
  };

  const onRetryCreateClick = () => {
    props.onShowCodeSpaceOnBoard(codeSpace, true);
  };

  const onCodeSpaceSecurityConfigClick = (codeSpace: ICodeSpaceData) => {
    if (codeSpace?.projectDetails?.publishedSecuirtyConfig) {
      history.push(`/codespace/publishedSecurityconfig/${codeSpace.id}?pub=true&name=${codeSpace.projectDetails.projectName}`);
      return;
    }
    history.push(`codespace/securityconfig/${codeSpace.id}?pub=false&name=${codeSpace.projectDetails.projectName}`);
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
  }

  const projectDetails = codeSpace?.projectDetails;
  const intDeploymentDetails = projectDetails.intDeploymentDetails;
  const prodDeploymentDetails = projectDetails.prodDeploymentDetails;
  const intDeployedUrl = intDeploymentDetails?.deploymentUrl;
  const intLastDeployedOn = intDeploymentDetails?.lastDeployedOn;
  const prodDeployedUrl = prodDeploymentDetails?.deploymentUrl;
  const prodLastDeployedOn = prodDeploymentDetails?.lastDeployedOn;
  const deployingInProgress =
    intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED';
  const intDeployed =
    intDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ||
    (intDeployedUrl !== null && intDeployedUrl !== 'null') || false;
  const prodDeployed =
    prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOYED' ||
    (prodDeployedUrl !== null && prodDeployedUrl !== 'null') || false;

  const deployed = intDeployed || prodDeployed;
  const allowDelete = isOwner ? !hasCollaborators : true;
  const isPublicRecipe = projectDetails.recipeDetails?.recipeId.startsWith('public');

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
      <div className={classNames(Styles.codeSpaceCard, deleteInProgress || createInProgress ? Styles.disable : null)}>
        <div className={Styles.cardHead}>
          <div
            className={classNames(
              Styles.cardHeadInfo,
              deleteInProgress || createInProgress || creationFailed ? Styles.disable : null,
            )}
          >
            <div className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)} onClick={onCardNameClick}>
              {projectDetails.projectName}
              {!enableOnboard && !creationFailed && (
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
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Code Recipe</div>
              <div>{recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId)?.name}</div>
            </div>
            <div>
              <div>Environment</div>
              <div>{projectDetails.recipeDetails.cloudServiceProvider}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.projectDetails.projectCreatedOn)}</div>
            </div>
            {!enableOnboard && !creationFailed && deployed && (
              <>
                <div className={Styles.deploymentInfo}>
                    <div>
                      {intDeployed && (
                        <>
                          <strong>Staging:</strong> (<span className={Styles.metricsTrigger} onClick={handleOpenDoraMetrics}>DORA Metrics</span>)
                          <br />
                          Branch '{intDeploymentDetails?.lastDeployedBranch}' deployed on
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
                          <br />
                          by {intDeploymentDetails?.lastDeployedBy?.firstName}
                        </>
                      )}
                    </div>
                    <div>
                      {prodDeployed && (
                        <>
                          <strong>Production:</strong> (<span className={Styles.metricsTrigger} onClick={handleOpenDoraMetrics}>DORA Metrics</span>)
                          <br />
                          Branch '{prodDeploymentDetails?.lastDeployedBranch}' deployed on
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
                          <br />
                          by {intDeploymentDetails?.lastDeployedBy?.firstName}
                        </>
                      )}
                    </div>
                </div>
                {/* <div>
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
                </div> */}
              </>
            )}
          </div>
        </div>
        <div className={Styles.cardFooter}>
          {enableOnboard ? (
            <div>
              <span className={classNames(Styles.statusIndicator, Styles.colloboration)}>
                Collaboration Requested...
              </span>
            </div>
          ) : (
            <>
              <div>
                {createInProgress ? (
                  <span className={classNames(Styles.statusIndicator, Styles.creating)}>Creating...</span>
                ) : (
                  <>
                    {!creationFailed && deployingInProgress && (
                      <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Deploying...</span>
                    )}
                    {!creationFailed && deployed && (
                      <>
                        {!deployingInProgress && <span className={Styles.statusIndicator}>Deployed</span>}
                        {intDeployed && (
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
                        )}
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

      {
        showDoraMetricsModal && 
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
      }
    </>
  );
};
export default CodeSpaceCardItem;
