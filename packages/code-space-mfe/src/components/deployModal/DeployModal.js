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
import { trackEvent, regionalDateAndTimeConversionSolution, buildGitRepoUrl } from '../../Utility/utils';
import Tags from 'dna-container/Tags';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import IntMigrationModal, { needsIntMigration } from '../intMigrationModal/IntMigrationModal';

const DeployModal = (props) => {
  const [branches, setBranches] = useState([]);
  const [branchValue, setBranchValue] = useState(['main']);
  const [isBranchValueMissing, setIsBranchValueMissing] = useState(false);
  const [deployEnvironment, setDeployEnvironment] = useState('staging');
  const [acceptContinueCodingOnDeployment, setAcceptContinueCodingOnDeployment] = useState(true);
  const projectDetails = props.codeSpaceData?.projectDetails;
  const [retainBuildImage, setRetainBuildImage] = useState(false);
  const [showIntMigrationModal, setShowIntMigrationModal] = useState(false);
  const [autoDeployEnabled, setAutoDeployEnabled] = useState(props.codeSpaceData?.autoDeploy || false);
  const [isStagingBranchMissing, setIsStagingBranchMissing] = useState(false);
  const [isProdBranchMissing, setIsProdBranchMissing] = useState(false);
  const [stagingBranchValue, setStagingBranchValue] = useState(
    projectDetails?.intAutoDeployBranchName ? [projectDetails.intAutoDeployBranchName] : []
  );
  const [prodBranchValue, setProdBranchValue] = useState(
    projectDetails?.prodAutoDeployBranchName ? [projectDetails.prodAutoDeployBranchName] : []
  );

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
    const isWorkspaceMigratedToGHE = props.codeSpaceData?.isWorkspaceMigratedToGHE;
    const repoUrl = buildGitRepoUrl(projectDetails?.gitRepoName, isWorkspaceMigratedToGHE);
    if (!version?.length) {
      ProgressIndicator.show();
    }
    CodeSpaceApiClient.getCodeSpacesGitBranchList(repoUrl)
      .then((res) => {
        if (!version?.length) {
          ProgressIndicator.hide();
          props.setShowCodeDeployModal(true);
        }
        let branchList = res?.data;
        branchList.forEach((element) => {
          element.id = element.name;
        });
        setBranches(branchList);
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        if (!version?.length) {
          ProgressIndicator.hide();
          Notification.show('Error in getting code space branch list - ' + err.message, 'alert');
        }
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
    if (!version?.length) {
      const found = branches.some(branch => 
       Object.values(branch).includes(branchValue[0])
      );
      if (!found) {
        formValid = false;
        Notification.show('Branch doesnot exist.','alert',);
      }
    }
    if (formValid) {
      const targetEnv = version?.length ? buildEnvironment : deployEnvironment;
      
      if (targetEnv === 'staging' && needsIntMigration(props.codeSpaceData) && !props.skipIntMigrationCheck) {
        setShowIntMigrationModal(true);
        return;
      }
      proceedWithDeployment();
    }
  };

  const proceedWithDeployment = () => {
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
      keepBuildImage: retainBuildImage,
    };
    ProgressIndicator.show();
    CodeSpaceApiClient.deployCodeSpace(props.codeSpaceData.id, deployRequest)
      .then((res) => {
        trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
        if (res.data.success === 'SUCCESS') {
          props.setCodeDeploying(true);
          
          // Only start SSE for direct deploys (pre-built version).
          // For build-first flow (no version), the card auto-poll handles status updates
          // until DEPLOY_REQUESTED is reached.
          if (props.startDeploymentStatusListener && version?.length) {
            props.startDeploymentStatusListener(
              projectDetails.projectName,
              deployRequest.targetEnvironment,
              props.onDeploymentStatusUpdate,
              props.onDeploymentComplete,
              props.onDeploymentSSEError
            );
          }
          
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
  };

  const handleIntMigrationDismiss = () => {
    setShowIntMigrationModal(false);
    const projectName = props.codeSpaceData?.projectDetails?.projectName;
    if (projectName) {
      localStorage.setItem('intMigrationDismissed_' + projectName, 'true');
    }
    proceedWithDeployment();
  };

  const onUpdateAutoDeploySettings = () => {
    if (autoDeployEnabled) {
      const stagingMissing = stagingBranchValue.length === 0;
      const prodMissing = prodBranchValue.length === 0;
      setIsStagingBranchMissing(stagingMissing);
      setIsProdBranchMissing(prodMissing);
      if (stagingMissing || prodMissing) {
        Notification.show('Please select both Staging and Prod branches for auto deployment.', 'alert');
        return;
      }
      if (stagingBranchValue[0] === prodBranchValue[0]) {
        Notification.show('Staging and Production branches must be different. Please select distinct branches.', 'alert');
        return;
      }
    }
    const webhookData = {
      repoName: projectDetails?.gitRepoName,
      intRepoName: stagingBranchValue[0] || '',
      prodRepoName: prodBranchValue[0] || '',
      webHookEnabled: autoDeployEnabled,
    };
    ProgressIndicator.show();
    CodeSpaceApiClient.addWebhook(webhookData)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show('Auto deploy settings have been updated successfully.');
        props.onAutoDeploySettingsUpdated?.();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show(
          'Error in updating auto deploy settings. Please try again later.\n' +
            (err?.response?.data?.errors?.[0]?.message || err.message),
          'alert',
        );
      });
  };

  return (
    <>
      <Modal
        title={`Deploy Code - ${props?.codeSpaceData?.projectDetails?.projectName || ''}`}
        showAcceptButton={false}
        showCancelButton={false}
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
                    {!version && (
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
                    )}
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
            <div className={Styles.autoDeploySection}>
              <div className={Styles.sectionTitle}>
                Auto Deployment Settings
                <span className={Styles.autoDeployInfo}>
                  <i className="icon mbc-icon info"></i>
                  <span className={Styles.autoDeployTooltip}>
                    Auto deployment is triggered exclusively on <strong>Push</strong> and{' '}
                    <strong>Pull Request merge</strong> events for selected branch linked to 
                    staging or production environment.
                    <br /><br />
                    <strong>Note:</strong> It is strongly recommended to enable <strong>branch protection rules</strong>
                      on the selected branch to prevent unintentional deployments. 
                     Any commit or merge to the selected branch will trigger an automatic deployment.
                  </span>
                </span>
              </div>
              <label className="checkbox">
                <span className="wrapper">
                  <input
                    type="checkbox"
                    className="ff-only"
                    checked={autoDeployEnabled}
                    onChange={(e) => setAutoDeployEnabled(e.target.checked)}
                  />
                </span>
                <span className="label">Enable Auto Deployment</span>
              </label>
              {autoDeployEnabled && (
                <div className={Styles.branchSelectors}>
                  <div>
                    <Tags
                      title={'Staging Branch'}
                      max={1}
                      chips={stagingBranchValue}
                      placeholder={'Type here...'}
                      tags={branches}
                      setTags={(val) => { setStagingBranchValue(val); setIsStagingBranchMissing(false); }}
                      isMandatory={true}
                      showMissingEntryError={isStagingBranchMissing}
                      showAllTagsOnFocus={true}
                      disableSelfTagAdd={true}
                      suggestionPopupHeight={150}
                    />
                  </div>
                  <div>
                    <Tags
                      title={'Production Branch'}
                      max={1}
                      chips={prodBranchValue}
                      placeholder={'Type here...'}
                      tags={branches}
                      setTags={(val) => { setProdBranchValue(val); setIsProdBranchMissing(false); }}
                      isMandatory={true}
                      showMissingEntryError={isProdBranchMissing}
                      showAllTagsOnFocus={true}
                      disableSelfTagAdd={true}
                      suggestionPopupHeight={150}
                    />
                  </div>
                </div>
              )}
            </div>
            <div className={Styles.modalFooter}>
              <div className={Styles.footerLeft}>
                <button
                  className="btn btn-tertiary"
                  type="button"
                  onClick={onUpdateAutoDeploySettings}
                >
                  Update Auto Deploy Settings
                </button>
              </div>
              <div className={Styles.footerRight}>
                <button
                  className="btn btn-primary"
                  type="button"
                  onClick={() => props.setShowCodeDeployModal(false)}
                >
                  Cancel
                </button>
                <button
                  className="btn btn-tertiary"
                  type="button"
                  onClick={onAcceptCodeDeploy}
                >
                  Deploy
                </button>
              </div>
            </div>
          </div>
        }
        scrollableContent={false}
        scrollableBox={true}
        onCancel={() => props.setShowCodeDeployModal(false)}
      />
      {showIntMigrationModal && (
        <IntMigrationModal
          show={showIntMigrationModal}
          codeSpaceData={props.codeSpaceData}
          onDismiss={handleIntMigrationDismiss}
        />
      )}
    </>
  );
};

export default DeployModal;
