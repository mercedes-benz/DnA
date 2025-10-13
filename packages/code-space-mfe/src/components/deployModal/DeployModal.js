import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './DeployModal.scss';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

import { CodeSpaceApiClient } from '../../apis/codespace.api';
import SelectBox from 'dna-container/SelectBox';
import Modal from 'dna-container/Modal';
import { trackEvent, regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import Tags from 'dna-container/Tags';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const DeployModal = (props) => {
  const [branches, setBranches] = useState([]);
  const [branchValue, setBranchValue] = useState(['main']);
  const [isBranchValueMissing, setIsBranchValueMissing] = useState(false);
  const [deployEnvironment, setDeployEnvironment] = useState('staging');
  const [acceptContinueCodingOnDeployment, setAcceptContinueCodingOnDeployment] = useState(true);
  const projectDetails = props.codeSpaceData?.projectDetails;
  const [retainBuildImage, setRetainBuildImage] = useState(false);

  //details from build
  const version = props?.buildDetails?.version || '';
  const buildOn = regionalDateAndTimeConversionSolution(props?.buildDetails?.buildOn) || '';
  const buildBranch = props?.buildDetails?.branch || '';
  const triggeredBy = props?.buildDetails?.triggeredBy || '';
  const buildEnvironment = props?.buildDetails?.environment || '';
  const comment = props?.buildDetails?.comments || '';

  useEffect(() => {
    Tooltip.defaultSetup();
    projectDetails?.intDeploymentDetails?.lastDeployedBranch?.length &&
      setBranchValue([projectDetails?.intDeploymentDetails?.lastDeployedBranch]);
    version?.length && setDeployEnvironment(buildEnvironment);
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpacesGitBranchList(
      projectDetails?.recipeDetails?.recipeId === 'private-user-defined'
        ? projectDetails?.recipeDetails?.repodetails
        : projectDetails?.gitRepoName,
    )
      .then((res) => {
        ProgressIndicator.hide();
        props.setShowCodeDeployModal(true);
        let branches = res?.data;
        branches.forEach((element) => {
          element.id = element.name;
        });
        setBranches(branches);
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('Error in getting code space branch list - ' + err.message, 'alert');
      });
    // setVault();
    return Tooltip.clear();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (deployEnvironment === 'staging') {
      projectDetails?.intDeploymentDetails?.lastDeployedBranch?.length &&
        setBranchValue([projectDetails?.intDeploymentDetails?.lastDeployedBranch]);
    } else {
      projectDetails?.prodDeploymentDetails?.lastDeployedBranch?.length &&
        setBranchValue([projectDetails?.prodDeploymentDetails?.lastDeployedBranch]);
    }
  }, [deployEnvironment]); // eslint-disable-line react-hooks/exhaustive-deps

  const onBranchChange = (selectedTags) => {
    setBranchValue(selectedTags);
    setIsBranchValueMissing(false);
  };

  const onAcceptContinueCodingOnDeployment = (e) => {
    setAcceptContinueCodingOnDeployment(e.target.checked);
  };

  const onDeployEnvironmentChange = (evnt) => {
    const deployEnv = evnt.currentTarget.value.trim();
    setDeployEnvironment(deployEnv);
  };

  const onAcceptCodeDeploy = () => {
    let formValid = true;
    if (!version?.length && branchValue?.length === 0) {
      formValid = false;
      setIsBranchValueMissing(true);
    }
    const found = branches.some(branch => 
     Object.values(branch).includes(branchValue[0])
    );
    if (!found) {
      formValid = false;
      Notification.show('Branch doesnot exist.','alert',);
    }
    if (formValid) {
      const deployRequest = {
        targetEnvironment: version?.length
          ? buildEnvironment === 'staging'
            ? 'int'
            : 'prod'
          : deployEnvironment === 'staging'
          ? 'int'
          : 'prod', // int or prod
        branch: version?.length ? buildBranch : branchValue[0],
        version: version || '',
        retainBuildImage: retainBuildImage,
      };
      ProgressIndicator.show();
      CodeSpaceApiClient.deployCodeSpace(props.codeSpaceData.id, deployRequest)
        .then((res) => {
          trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
          if (res.data.success === 'SUCCESS') {
            // setCreatedCodeSpaceName(res.data.name);
            props.setCodeDeploying(true);
            if (acceptContinueCodingOnDeployment) {
              ProgressIndicator.hide();
              Notification.show(
                `Code space '${projectDetails.projectName}' deployment successfully started. Please check the status later.`,
              );
              props.setShowCodeDeployModal(false);
            } else {
              props.setIsApiCallTakeTime(true);
            }
            props.startDeployLivelinessCheck &&
              props.startDeployLivelinessCheck(props.codeSpaceData.workspaceId, deployEnvironment);
          } else {
            props.setIsApiCallTakeTime(false);
            ProgressIndicator.hide();
            Notification.show(
              'Error in deploying code space. Please try again later.\n' + res.data.errors[0].message,
              'alert',
            );
          }
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(
            'Error in deploying code space. Please try again later.\n' + err?.response?.data?.errors[0]?.message,
            'alert',
          );
        });
    }
  };

  return (
    <>
      <Modal
        title={'Deploy Code'}
        showAcceptButton={true}
        acceptButtonTitle={'Deploy'}
        cancelButtonTitle={'Cancel'}
        onAccept={onAcceptCodeDeploy}
        showCancelButton={true}
        modalWidth="900px"
        buttonAlignment="center"
        show={true}
        content={
          <div className={Styles.deployModal}>
            <span>
              <p>
                <i className="icon mbc-icon alert circle"></i> The code from your workspace will be deployed and is run
                in a container. You will get the access url after the deployment.
              </p>
            </span>
            {version?.length ? (
              <div id="deployVersionContainer" className="input-field-group">
                <label className="input-label">Based on previous build</label>
                <div>
                  <label className="chips">
                    <b>Environment: </b> {buildEnvironment} &nbsp;|&nbsp;<b> Branch: </b> {buildBranch} &nbsp;|&nbsp;<b> Triggered By: </b>{' '}
                    {triggeredBy} &nbsp;|&nbsp;<b> Build On: </b> {buildOn} &nbsp;|&nbsp;<b> Version: </b> {version} &nbsp;|&nbsp;<b> Comment: </b>{' '}
                    {comment}
                  </label>
                </div>
              </div>
            ) : (
              <div className={Styles.flexLayout}>
                <div id="deployEnvironmentContainer" className="input-field-group">
                  <label className="input-label">Deploy Environment</label>
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
                <div>
                  <Tags
                    title={'Code Branch to Deploy'}
                    max={1}
                    chips={branchValue}
                    placeholder={'Type here...'}
                    tags={branches}
                    setTags={onBranchChange}
                    isMandatory={true}
                    showMissingEntryError={isBranchValueMissing}
                    showAllTagsOnFocus={true}
                    disableSelfTagAdd={true}
                    suggestionPopupHeight={150}
                  />
                  <div className={Styles.checkboxRow}>
                    <label className="checkbox">
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          checked={retainBuildImage}
                          onChange={(e) => setRetainBuildImage(e.target.checked)}
                        />
                      </span>
                      <span className="label">Do you want to retain the build image?</span>
                    </label>
                  </div>
                </div>
              </div>
            )}
            {props.startDeployLivelinessCheck && (
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
            )}
          </div>
        }
        scrollableContent={false}
        scrollableBox={true}
        onCancel={() => props.setShowCodeDeployModal(false)}
      />
    </>
  );
};

export default DeployModal;