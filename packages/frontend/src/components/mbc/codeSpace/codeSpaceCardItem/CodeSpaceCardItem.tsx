import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import { recipesMaster, regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { history } from '../../../../router/History';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { ICodeSpaceData } from '../CodeSpace';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { trackEvent } from '../../../../services/utils';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { IUserInfo } from 'globals/types';

interface CodeSpaceCardItemProps {
  userInfo: IUserInfo;
  codeSpace: ICodeSpaceData;
  onDeleteSuccess?: () => void;
  toggleProgressMessage?: (show: boolean) => void;
  onShowCodeSpaceOnBoard: (codeSpace: ICodeSpaceData) => void;
}

const CodeSpaceCardItem = (props: CodeSpaceCardItemProps) => {
  const codeSpace = props.codeSpace;
  // const collaborationCodeSpace = codeSpace.projectDetails.projectCollaborators?.find((user: ICodeCollaborator) => user.id === props.userInfo.id);
  const enableOnboard = codeSpace ? codeSpace.status === 'COLLABORATION_REQUESTED' : false;
  // const codeDeploying = codeSpace.status === 'DEPLOY_REQUESTED';
  const deleteInProgress = codeSpace.status === 'DELETE_REQUESTED';
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const recipes = recipesMaster;
  const isOwner = codeSpace.workspaceOwner.id === props.userInfo.id;
  const hasCollaborators = codeSpace.projectDetails?.projectCollaborators?.length;

  const deleteCodeSpaceContent = (
    <div>
      <h3>
        {/* Are you sure to delete {codeSpace.projectDetails.projectName} Code Space?
        <br /> */}
        {isOwner ? (
          <>
            Deleting a CodeSpace would delete the code associated with it
            {hasCollaborators && <><br />and the CodeSpaces of the collaborators you have added</>},
            <br /> Do you want to proceed?
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
        trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
        if (res.success === 'SUCCESS') {
          props.onDeleteSuccess();
          setShowDeleteModal(false);
          ProgressIndicator.hide();
          // setCreatedCodeSpaceName(res.data.name);
          // setIsApiCallTakeTime(true);
          // enableDeployLivelinessCheck(codeSpaceData.name);
        } else {
          // setIsApiCallTakeTime(false);
          ProgressIndicator.hide();
          Notification.show(
            'Error in deleting code space. Please try again later.\n' + res.errors[0].message,
            'alert',
          );
        }
        //props.getCodeSpaces();
        //setShowDeleteModal(false);
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

  const projectDetails = codeSpace?.projectDetails;
  const intDeploymentDetails = projectDetails.intDeploymentDetails;
  const prodDeploymentDetails = projectDetails.prodDeploymentDetails;
  const intDeployedUrl = intDeploymentDetails?.deploymentUrl;
  const intLastDeployedOn = intDeploymentDetails?.lastDeployedOn;
  const prodDeployedUrl = prodDeploymentDetails?.deploymentUrl;
  const prodLastDeployedOn = prodDeploymentDetails?.lastDeployedOn;
  const deployingInProgress =
    intDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
    prodDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED';
  const intDeployed =
    intDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
    (intDeployedUrl !== null && intDeployedUrl !== 'null');
  const prodDeployed =
    prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
    (prodDeployedUrl !== null && prodDeployedUrl !== 'null');

  const deployed = intDeployed || prodDeployed;

  return (
    <>
      <div className={classNames(Styles.codeSpaceCard, deleteInProgress || createInProgress ? Styles.disable : null)}>
        <div className={Styles.cardHead}>
          <div
            className={classNames(Styles.cardHeadInfo, deleteInProgress || createInProgress ? Styles.disable : null)}
          >
            <div className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)} onClick={onCardNameClick}>
              {projectDetails.projectName}
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Code Recipe</div>
              <div>{recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId).name}</div>
            </div>
            <div>
              <div>Environment</div>
              <div>{projectDetails.recipeDetails.cloudServiceProvider}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.projectDetails.projectCreatedOn)}</div>
            </div>
            {deployed && (
              <div>
                <div>Last Deployed on</div>
                <div>
                  {intDeployed && <>Staging({intDeploymentDetails.lastDeployedBranch}):<br />{regionalDateAndTimeConversionSolution(intLastDeployedOn)}</>}
                  <br />
                  {prodDeployed && <>Production({prodDeploymentDetails.lastDeployedBranch}):<br />{regionalDateAndTimeConversionSolution(prodLastDeployedOn)}</>}
                </div>
              </div>
            )}
            {/* <div>
              <div>Code Space ID</div>
              <div>{codeSpace.name}</div>
            </div> */}
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
                {deployingInProgress && (
                  <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Deploying...</span>
                )}
                {deployed && (
                  <>
                    {!deployingInProgress && <span className={Styles.statusIndicator}>Deployed</span>}
                    {intDeployed && (
                      <a href={intDeployedUrl} target="_blank" rel="noreferrer" className={Styles.deployedLink}>
                        <i className="icon mbc-icon link" /> Staging
                      </a>
                    )}
                    {prodDeployed && (
                      <a href={prodDeployedUrl} target="_blank" rel="noreferrer" className={Styles.deployedLink}>
                        <i className="icon mbc-icon link" /> Production
                      </a>
                    )}
                  </>
                )}
                {deleteInProgress && (
                  <span className={classNames(Styles.statusIndicator, Styles.deleting)}>Deleting...</span>
                )}
                {createInProgress && (
                  <span className={classNames(Styles.statusIndicator, Styles.creating)}>Creating...</span>
                )}
              </div>
              <div className={Styles.btnGrp}>
                <button className="btn btn-primary hide" onClick={() => history.push(`/edit/${codeSpace.workspaceId}`)}>
                  <i className="icon mbc-icon edit"></i>
                </button>
                {!deleteInProgress && !createInProgress && !deployingInProgress && (
                  <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
                    <i className="icon delete"></i>
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
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteCodeSpaceContent}
        onCancel={deleteCodeSpaceClose}
        onAccept={deleteCodeSpaceAccept}
      />
    </>
  );
};
export default CodeSpaceCardItem;
