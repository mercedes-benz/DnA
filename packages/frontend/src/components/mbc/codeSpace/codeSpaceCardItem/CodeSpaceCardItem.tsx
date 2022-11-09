import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import { regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { history } from '../../../../router/History';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { ICodeSpaceData } from '../CodeSpace';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { trackEvent } from '../../../../services/utils';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { ICodeCollaborator, IUserInfo } from 'globals/types';

interface CodeSpaceCardItemProps {
  userInfo: IUserInfo;
  codeSpace: ICodeSpaceData;
  onDeleteSuccess?: () => void;
  toggleProgressMessage?: (show: boolean) => void;
  onShowCodeSpaceOnBoard: (codeSpace: ICodeSpaceData) => void;
}

const CodeSpaceCardItem = (props: CodeSpaceCardItemProps) => {
  const codeSpace = props.codeSpace;
  const collaborationCodeSpace = codeSpace.projectDetails.projectCollaborators?.find((user: ICodeCollaborator) => user.id === props.userInfo.id);
  const enableOnboard = collaborationCodeSpace ? collaborationCodeSpace.status === 'REQUESTED' : false;
  const codeDeploying = codeSpace.status === 'DEPLOY_REQUESTED';
  const deleteInProgress = codeSpace.status === 'DELETE_REQUESTED';
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const deleteCodeSpaceContent = (
    <div>
      <h3>
        Are you sure you want to delete {codeSpace.projectDetails.projectName} Code Space?
        <br />
        You will be loosing your code as well as the deployed code instance.
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
            'Error in deploying code space. Please try again later.\n' + res.errors[0].message,
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
      history.push(`codespace/${codeSpace.id}`);
    }
  };

  const projectDetails = codeSpace?.projectDetails;
  const intDeployedUrl = projectDetails.intDeploymentDetails?.deploymentUrl;
  const intLastDeployedOn = projectDetails.intDeploymentDetails?.lastDeployedOn;
  const prodDeployedUrl = projectDetails.prodDeploymentDetails?.deploymentUrl;
  const deployed = codeSpace?.status === 'DEPLOYED' || (intDeployedUrl !== null  || prodDeployedUrl !== null);

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
              <div>{projectDetails.recipeDetails.recipeId}</div>
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
                <div>{regionalDateAndTimeConversionSolution(intLastDeployedOn)}</div>
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
              <span className={classNames(Styles.statusIndicator, Styles.colloboration)}>Collaboration Requested...</span>
            </div>
          ) : (
            <>
              <div>
                {codeDeploying && (
                  <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Deploying...</span>
                )}
                {deployed && (
                  <>
                    <span className={Styles.statusIndicator}>Deployed</span>
                    <a href={intDeployedUrl} target="_blank" rel="noreferrer" className={Styles.deployedLink}>
                      <i className="icon mbc-icon link" />
                    </a>
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
                <button className="btn btn-primary hide" onClick={() => history.push(`/edit/${codeSpace.id}`)}>
                  <i className="icon mbc-icon edit"></i>
                </button>
                {!deleteInProgress && !createInProgress && (
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
