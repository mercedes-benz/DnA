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
// import { ICodeSpaceData } from '../CodeSpace';
import { CODE_SPACE_TITLE } from '../../Utility/constants';
// import { Envs } from '../../Utility/envs';
import { trackEvent } from '../../Utility/utils';
import TextBox from 'dna-container/TextBox';
import Tags from 'dna-container/Tags';

// import TextBox from '../../shared/textBox/TextBox';

// export interface IBranch {
//   name: string;
// }

// export interface IDeployRequest {
//   targetEnvironment: string; // int or prod
//   branch: string;
//   secureWithIAMRequired?: boolean;
//   technicalUserDetailsForIAMLogin?: string;
//   valutInjectorEnable?: boolean;
//   clientID?: string;
//   clientSecret?: string;
// }

// interface DeployModalProps {
//   codeSpaceData: ICodeSpaceData;
//   enableSecureWithIAM: boolean;
//   setShowCodeDeployModal: (show: boolean) => void;
//   startDeployLivelinessCheck?: (workspaceId: string, deployEnvironment: string) => void;
//   setCodeDeploying: (codeDeploying: boolean) => void;
//   setIsApiCallTakeTime: (apiCallTakeTime: boolean) => void;
//   navigateSecurityConfig: () => void;
// }

const DeployModal = (props) => {
  const [secureWithIAMSelected, setSecureWithIAMSelected] = useState(true);
  const [branches, setBranches] = useState([]);
  const [branchValue, setBranchValue] = useState(['main']);
  const [isBranchValueMissing, setIsBranchValueMissing] = useState(false);
  const [deployEnvironment, setDeployEnvironment] = useState('staging');
  // const [vaultEnabled, setVaultEnabled] = useState(false);
  const [acceptContinueCodingOnDeployment, setAcceptContinueCodingOnDeployment] = useState(true);
  // const [iamTechnicalUserID, setIAMTechnicalUserID] = useState<string>('');
  // const [iamTechnicalUserIDError, setIAMTechnicalUserIDError] = useState<string>('');
  const [clientId, setClientId] = useState('');
  const [clientIdError, setClientIdError] = useState('');
  const [clientSecret, setClientSecret] = useState('');
  const [clientSecretError, setClientSecretError] = useState('');
  const [changeSelected, setChangeSelected] = useState(false);
  const [disableIntIAM, setDisableIntIAM] = useState(true);
  const [disableProdIAM, setDisableProdIAM] = useState(true);

  const projectDetails = props.codeSpaceData?.projectDetails;
  const collaborator = projectDetails?.projectCollaborators?.find((collaborator) => {return collaborator?.id === props?.userInfo?.id });
  const isOwner = projectDetails?.projectOwner?.id === props.userInfo.id || collaborator?.isAdmin;
  const intDeployLogs = (projectDetails?.intDeploymentDetails?.deploymentAuditLogs)?.filter((item) => item?.branch) || [] ;
  const prodDeployLogs = (projectDetails?.prodDeploymentDetails?.deploymentAuditLogs)?.filter((item) => item?.branch) || [];

  useEffect(() => {
    intDeployLogs.length && setBranchValue([intDeployLogs[(intDeployLogs.length)-1]?.branch]);
    setClientId('');
    setClientIdError('');
    setClientSecret('');
    setClientSecretError('');
    setChangeSelected(false);
    // setIAMTechnicalUserID('');
    getPublishedConfig(props?.codeSpaceData?.id, 'int');
    ProgressIndicator.show();
    CodeSpaceApiClient.getCodeSpacesGitBranchList(projectDetails?.recipeDetails?.recipeId === "private-user-defined" ? projectDetails?.recipeDetails?.repodetails : projectDetails?.gitRepoName)
      .then((res) => {
        ProgressIndicator.hide();
        props.setShowCodeDeployModal(true);
        let branches = res?.data;
        branches.forEach(element => {
          element.id = element.name;
        });
        setBranches(branches);
        // setIAMTechnicalUserID(projectDetails?.intDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
        setSecureWithIAMSelected(projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false);
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('Error in getting code space branch list - ' + err.message, 'alert');
      });
    // setVault();
  }, []);// eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if(deployEnvironment === 'staging'){
      intDeployLogs.length && setBranchValue([intDeployLogs[(intDeployLogs.length)-1]?.branch]);
    }
    else{
      prodDeployLogs.length && setBranchValue([prodDeployLogs[(prodDeployLogs.length)-1]?.branch]);
    }
  }, [deployEnvironment]);// eslint-disable-line react-hooks/exhaustive-deps

  const getPublishedConfig = (id, env) => {
    let appId;
    let entitlements;
    // ProgressIndicator.show();
    CodeSpaceApiClient.getPublishedConfig(id, env).then((res) => {
      appId = res.data.appID || '';
      entitlements = res.data.entitlements || [];
      if (env === 'int') {
        appId.length !== 0 && entitlements.length !== 0 ? setDisableIntIAM(false) : setDisableIntIAM(true);
      } else if (env === 'prod') {
        appId.length !== 0 && entitlements.length !== 0 ? setDisableProdIAM(false) : setDisableProdIAM(true);
      }
    });
  };

  const onBranchChange = (selectedTags) => {
    setBranchValue(selectedTags);
    setIsBranchValueMissing(false);
  };

  const onChangeSecureWithIAM = (e) => {
    setSecureWithIAMSelected(e.target.checked);
  };

  const onAcceptContinueCodingOnDeployment = (e) => {
    setAcceptContinueCodingOnDeployment(e.target.checked);
  };

  const onDeployEnvironmentChange = (evnt) => {
    setClientId('');
    setClientIdError('');
    setClientSecret('');
    setClientSecretError('');
    setChangeSelected(false);
    const deployEnv = evnt.currentTarget.value.trim();
    setDeployEnvironment(deployEnv);
    if (deployEnv === 'staging') {
      setSecureWithIAMSelected(projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false);
      getPublishedConfig(props?.codeSpaceData?.id, 'int');
      // setIAMTechnicalUserID(projectDetails?.intDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
    } else {
      setSecureWithIAMSelected(projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || false);
      getPublishedConfig(props?.codeSpaceData?.id, 'prod');
      // setIAMTechnicalUserID(projectDetails?.prodDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
    }
  };

  // const onIAMTechnicalUserIDOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
  //   const iamUserID = evnt.currentTarget.value.trim();
  //   setIAMTechnicalUserID(iamUserID);
  //   setIAMTechnicalUserIDError(iamUserID.length ? '' : requiredError);
  // };

  // const setVault = () => {
  //   ProgressIndicator.show();
  //   CodeSpaceApiClient.read_secret(
  //     projectDetails?.projectName.toLowerCase(),
  //     deployEnvironment === 'staging' ? 'int' : 'prod',
  //   )
  //     .then((response) => {
  //       ProgressIndicator.hide();
  //       Object.keys(response.data).length !== 0 ? setVaultEnabled(true) : setVaultEnabled(false);
  //     })
  //     .catch(() => {
  //       ProgressIndicator.hide();
  //       // if (err?.response?.data?.errors?.length > 0) {
  //       //   err?.response?.data?.errors.forEach((err: any) => {
  //       //     Notification.show(err?.message || 'Something went wrong.', 'alert');
  //       //   });
  //       // } else {
  //       //   Notification.show(err?.message || 'Something went wrong.', 'alert');
  //       // }
  //     });
  // };

  const onAcceptCodeDeploy = () => {
    // if (secureWithIAMSelected && iamTechnicalUserID.trim() === '') {
    //   setIAMTechnicalUserIDError(requiredError);
    //   return;
    // } else {
    //   setIAMTechnicalUserIDError('');
    // }
    let formValid = true;
    if (
      secureWithIAMSelected &&
      ((deployEnvironment === 'staging'
        ? !projectDetails.intDeploymentDetails.secureWithIAMRequired
        : !projectDetails.prodDeploymentDetails.secureWithIAMRequired) ||
        changeSelected) &&
      clientSecret.length === 0
    ) {
      formValid = false;
      setClientIdError('*Missing Entry');
    }
    if (
      secureWithIAMSelected &&
      ((deployEnvironment === 'staging'
        ? !projectDetails.intDeploymentDetails.secureWithIAMRequired
        : !projectDetails.prodDeploymentDetails.secureWithIAMRequired) ||
        changeSelected) &&
      clientSecret.length === 0
    ) {
      formValid = false;
      setClientSecretError('*Missing Entry');
    }
    if(branchValue.length === 0){
      formValid = false;
      setIsBranchValueMissing(true);
    }
    if (formValid) {
      const deployRequest = {
        secureWithIAMRequired: secureWithIAMSelected,
        // technicalUserDetailsForIAMLogin: secureWithIAMSelected ? iamTechnicalUserID : null,
        targetEnvironment: deployEnvironment === 'staging' ? 'int' : 'prod', // int or prod
        branch: branchValue[0],
        // valutInjectorEnable: vaultEnabled,
        clientID: clientId,
        clientSecret: clientSecret,
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
          Notification.show('Error in deploying code space. Please try again later.\n' + err?.response?.data?.errors[0]?.message, 'alert');
        });
    }
  };

  return (
    <Modal
      title={'Deploy Code'}
      showAcceptButton={true}
      acceptButtonTitle={'Deploy'}
      cancelButtonTitle={'Cancel'}
      onAccept={onAcceptCodeDeploy}
      showCancelButton={true}
      modalWidth="600px"
      buttonAlignment="center"
      show={true}
      content={
        <div className={Styles.deployModal}>
          <p>
            The code from your workspace will be deployed and is run in a container and you will get the access url
            after the deployment.
          </p>
          <div className={Styles.flexLayout}>
            <div>
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
            </div>
            <div>
                <Tags
                  title={'Code Branch to Deploy'}
                  max={1}
                  chips={branchValue}
                  placeholder={'Only the top 100 branches will be fetched'}
                  tags={branches}
                  setTags={onBranchChange}
                  isMandatory={true}
                  showMissingEntryError={isBranchValueMissing}
                  showAllTagsOnFocus={true}
                  disableSelfTagAdd={true}
                  suggestionPopupHeight={150}
                />
            </div>
          </div>
          {props.enableSecureWithIAM && (
            <>
              {deployEnvironment === 'staging' && (
                <>
                  <div>
                    <label className="checkbox">
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          checked={secureWithIAMSelected}
                          onChange={onChangeSecureWithIAM}
                          // disabled={projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                          // disabled={disableIntIAM && !projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                        />
                      </span>
                      <span className="label">
                        Secure with your own IAM Credentials{' '}
                        {isOwner && (<span className={classNames(Styles.configLink)} onClick={props.navigateSecurityConfig}>
                          <a target="_blank" rel="noreferrer">
                            {CODE_SPACE_TITLE} (
                            {projectDetails?.publishedSecuirtyConfig?.status ||
                              projectDetails?.securityConfig?.status ||
                              'New'}
                            )
                          </a>
                        </span>)}
                      </span>
                    </label>
                    <span>
                      <p
                        style={{ color: 'var(--color-orange)' }}
                        className={classNames(disableIntIAM && secureWithIAMSelected ? '' : 'hide')}
                      >
                        <i className="icon mbc-icon alert circle"></i> You do not have any published Authorization Configuration and therefore no authorization checks would happen.
                      </p>
                    </span>
                  </div>
                  {secureWithIAMSelected && (
                    <div>
                      {!projectDetails?.intDeploymentDetails?.secureWithIAMRequired || changeSelected ? (
                        <div className={classNames(Styles.flexLayout)}>
                          <TextBox
                            type="text"
                            controlId={'Client ID'}
                            labelId={'clientIdLabel'}
                            label={'Client ID'}
                            placeholder={'Client ID as per IAM used with Alice'}
                            value={clientId}
                            errorText={clientIdError}
                            required={true}
                            maxLength={200}
                            onChange={(e) => {
                              setClientId(e.currentTarget.value);
                              setClientIdError('');
                            }}
                          />
                          <TextBox
                            type="text"
                            controlId={'Client Secret'}
                            labelId={'clientSecretLabel'}
                            label={'Client Secret'}
                            placeholder={'Client Secret as per IAM used with Alice'}
                            value={clientSecret}
                            errorText={clientSecretError}
                            required={true}
                            maxLength={200}
                            onChange={(e) => {
                              setClientSecret(e.currentTarget.value);
                              setClientSecretError('');
                            }}
                          />
                        </div>
                      ) : (
                        <div className={classNames(Styles.actionWrapper)}>
                          <button
                            className={classNames('btn btn-primary', Styles.saveSettingsBtn)}
                            onClick={() => setChangeSelected(true)}
                          >
                            Change Credentials
                          </button>
                        </div>
                      )}
                    </div>
                  )}
                  {/* {secureWithIAMSelected && (
                <div
                  className={classNames(
                    Styles.flexLayout,
                    projectDetails?.intDeploymentDetails?.secureWithIAMRequired && Styles.disabledDiv,
                  )}
                >
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
              )} */}
                </>
              )}
              {deployEnvironment === 'production' && (
                <>
                  <div>
                    <label className="checkbox">
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          checked={secureWithIAMSelected}
                          onChange={onChangeSecureWithIAM}
                          // disabled={projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                          // disabled={disableProdIAM && !projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                        />
                      </span>
                      <span className="label">
                        Secure with your own IAM Credentials{' '}
                        {isOwner && (<span className={classNames(Styles.configLink)} onClick={props.navigateSecurityConfig}>
                          <a target="_blank" rel="noreferrer">
                            {CODE_SPACE_TITLE} (
                            {projectDetails?.publishedSecuirtyConfig?.status ||
                              projectDetails?.securityConfig?.status ||
                              'New'}
                            )
                          </a>
                        </span>)}
                      </span>
                    </label>
                    <span>
                      <p
                        style={{ color: 'var(--color-orange)' }}
                        className={classNames(disableProdIAM && secureWithIAMSelected ? '' : 'hide')}
                      >
                        <i className="icon mbc-icon alert circle"></i> You do not have any published Authorization Configuration and therefore no authorization checks would happen.
                      </p>
                    </span>
                  </div>
                  {secureWithIAMSelected && (
                    <div>
                      {!projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || changeSelected ? (
                        <div className={classNames(Styles.flexLayout)}>
                          <TextBox
                            type="text"
                            controlId={'Client ID'}
                            labelId={'clientIdLabel'}
                            label={'Client ID'}
                            placeholder={'Client ID as per IAM used with Alice'}
                            value={clientId}
                            errorText={clientIdError}
                            required={true}
                            maxLength={200}
                            onChange={(e) => {
                              setClientId(e.currentTarget.value);
                              setClientIdError('');
                            }}
                          />
                          <TextBox
                            type="text"
                            controlId={'Client Secret'}
                            labelId={'clientSecretLabel'}
                            label={'Client Secret'}
                            placeholder={'Client Secret as per IAM used with Alice'}
                            value={clientSecret}
                            errorText={clientSecretError}
                            required={true}
                            maxLength={200}
                            onChange={(e) => {
                              setClientSecret(e.currentTarget.value);
                              setClientSecretError('');
                            }}
                          />
                        </div>
                      ) : (
                        <div className={classNames(Styles.actionWrapper)}>
                          <button
                            className={classNames('btn btn-primary', Styles.saveSettingsBtn)}
                            onClick={() => setChangeSelected(true)}
                          >
                            Change Credentials
                          </button>
                        </div>
                      )}
                    </div>
                  )}
                  {/* {secureWithIAMSelected && (
                <div
                  className={classNames(
                    Styles.flexLayout,
                    projectDetails?.prodDeploymentDetails?.secureWithIAMRequired && Styles.disabledDiv,
                  )}
                >
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
              )} */}
                </>
              )}
            </>
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
      onCancel={() => props.setShowCodeDeployModal(false)}
    />
  );
};

export default DeployModal;
