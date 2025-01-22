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
import { trackEvent, regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import TextBox from 'dna-container/TextBox';
import Tags from 'dna-container/Tags';
import { Envs } from '../../Utility/envs';

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
  const ignorePaths = [
    { id: '1', name: '/favicon.ico' },
    { id: '2', name: '/manifest.json' },
    { id: '3', name: '/obfuskator-api/int/api/docs' },
    { id: '4', name: '/docs' },
    { id: '5', name: '/obfuskator-api/int/api/openapi.json' },
    { id: '6', name: '/openapi.json' },
  ];
  const [ignorePath, setIgnorePath] = useState([]);
  // const [ignorePathError, setIgnorePathError] = useState(false);
  const [redirectUri, setRedirectUri] = useState('');
  const scopes = [
    { id: '1', name: 'openid' },
    { id: '2', name: 'autorization_group' },
    { id: '3', name: 'entitlement_group' },
    { id: '4', name: 'scoped_entitlement' },
    { id: '5', name: 'email' },
    { id: '6', name: 'profile' },
    { id: '7', name: 'phone' },
    { id: '8', name: 'offline_access' },
    { id: '9', name: 'group_type' },
  ];
  const [scope, setScope] = useState(['openid', 'offline_access']);
  const fixedScope = ['openid', 'offline_access'];
  const [oneApiSelected, setOneApiSelected] = useState(false);
  const [oneApiVersionShortName, setOneApiVersionShortName] = useState('');
  const [oneApiVersionShortNameError, setOneApiVersionShortNameError] = useState('');
  const [cookieSelected, setCookieSelected] = useState(false);
  const [isSecuredWithCookie, setIsSecuredWithCookie] = useState(false);

  const projectDetails = props.codeSpaceData?.projectDetails;
  const collaborator = projectDetails?.projectCollaborators?.find((collaborator) => {return collaborator?.id === props?.userInfo?.id });
  const isOwner = projectDetails?.projectOwner?.id === props.userInfo.id || collaborator?.isAdmin;
  const intDeployLogs = (projectDetails?.intDeploymentDetails?.deploymentAuditLogs)?.filter((item) => item?.branch) || [] ;
  const prodDeployLogs = (projectDetails?.prodDeploymentDetails?.deploymentAuditLogs)?.filter((item) => item?.branch) || [];
  
  //details from build
  const version = props?.buildDetails?.version || '';
  const triggeredOn = regionalDateAndTimeConversionSolution(props?.buildDetails?.triggeredOn) || '';
  const buildBranch = props?.buildDetails?.branch || '';
  const artifactId = props?.buildDetails?.artifactId || '';
  const triggeredBy = props?.buildDetails?.triggeredBy || '';

  useEffect(() => {
    intDeployLogs.length && setBranchValue([intDeployLogs[(intDeployLogs.length)-1]?.branch]);
    setClientId('');
    setClientIdError('');
    setClientSecret('');
    setClientSecretError('');
    setOneApiVersionShortNameError('');
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
        setOneApiSelected(projectDetails?.intDeploymentDetails?.oneApiVersionShortName?.length || false);
        setOneApiVersionShortName(projectDetails?.intDeploymentDetails?.oneApiVersionShortName || '');
        setCookieSelected(projectDetails?.intDeploymentDetails?.isSecuredWithCookie || false);
        setIsSecuredWithCookie(projectDetails?.intDeploymentDetails?.isSecuredWithCookie || false);
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('Error in getting code space branch list - ' + err.message, 'alert');
      });
    // setVault();
    SelectBox.defaultSetup();
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
    e.target.checked ? setOneApiSelected(false) : '';
  };

  const onChangeOpenApi = (e) => {
    setOneApiSelected(e.target.checked);
    e.target.checked ? setSecureWithIAMSelected(false) : '';
  };

  const onAcceptContinueCodingOnDeployment = (e) => {
    setAcceptContinueCodingOnDeployment(e.target.checked);
  };

  const onIgnorePathChange = (selectedTags) => {
    setIgnorePath(selectedTags);
  };

  const onScopeChnage = (selectedTags) => {
    setScope(selectedTags);
  };

  const onDeployEnvironmentChange = (evnt) => {
    setClientId('');
    setClientIdError('');
    setClientSecret('');
    setClientSecretError('');
    setOneApiVersionShortNameError('');
    setChangeSelected(false);
    const deployEnv = evnt.currentTarget.value.trim();
    setDeployEnvironment(deployEnv);
    if (deployEnv === 'staging') {
      setSecureWithIAMSelected(projectDetails?.intDeploymentDetails?.secureWithIAMRequired || false);
      getPublishedConfig(props?.codeSpaceData?.id, 'int');
      setOneApiSelected(projectDetails?.intDeploymentDetails?.oneApiVersionShortName?.length || false);
      setOneApiVersionShortName(projectDetails?.intDeploymentDetails?.oneApiVersionShortName || '');
      // setIAMTechnicalUserID(projectDetails?.intDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
      setCookieSelected(projectDetails?.intDeploymentDetails?.isSecuredWithCookie || false);
      setIsSecuredWithCookie(projectDetails?.intDeploymentDetails?.isSecuredWithCookie || false);
    } else {
      setSecureWithIAMSelected(projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || false);
      getPublishedConfig(props?.codeSpaceData?.id, 'prod');
      setOneApiSelected(projectDetails?.prodDeploymentDetails?.oneApiVersionShortName?.length || false);
      setOneApiVersionShortName(projectDetails?.prodDeploymentDetails?.oneApiVersionShortName || '');
      // setIAMTechnicalUserID(projectDetails?.prodDeploymentDetails?.technicalUserDetailsForIAMLogin || '');
      setCookieSelected(projectDetails?.prodDeploymentDetails?.isSecuredWithCookie || false);
      setIsSecuredWithCookie(projectDetails?.prodDeploymentDetails?.isSecuredWithCookie || false);
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
      (((deployEnvironment === 'staging'
        ? (!projectDetails.intDeploymentDetails.secureWithIAMRequired && !cookieSelected)
        : (!projectDetails.prodDeploymentDetails.secureWithIAMRequired && !cookieSelected)) ||
        changeSelected) || (!cookieSelected && isSecuredWithCookie)) &&
      clientSecret.length === 0
    ) {
      formValid = false;
      setClientIdError('*Missing Entry');
    }
    if (
      secureWithIAMSelected && 
      (((deployEnvironment === 'staging'
        ? (!projectDetails.intDeploymentDetails.secureWithIAMRequired && !cookieSelected)
        : (!projectDetails.prodDeploymentDetails.secureWithIAMRequired && !cookieSelected)) ||
        changeSelected) || (!cookieSelected && isSecuredWithCookie)) &&
      clientSecret.length === 0
    ) {
      formValid = false;
      setClientSecretError('*Missing Entry');
    }
    if(!version?.length && branchValue?.length === 0){
      formValid = false;
      setIsBranchValueMissing(true);
    }
    if (ignorePath.length !== 0 && ignorePath.some(item => item.endsWith('/') || item.includes(' ') || !item.startsWith('/'))) {
      formValid = false;
    }
    if (oneApiSelected && oneApiVersionShortName?.length === 0) {
      formValid = false;
      setOneApiVersionShortNameError('*Missing Entry');
    }
    if (formValid) {
      const deployRequest = {
        secureWithIAMRequired: secureWithIAMSelected,
        // technicalUserDetailsForIAMLogin: secureWithIAMSelected ? iamTechnicalUserID : null,
        targetEnvironment: deployEnvironment === 'staging' ? 'int' : 'prod', // int or prod
        branch: version?.length ? '' : branchValue[0],
        version: version || '',
        // valutInjectorEnable: vaultEnabled,
        clientID: clientId,
        clientSecret: clientSecret,
        redirectUri: redirectUri || '',
        ignorePaths: ignorePath.join(',') || '',
        scope: secureWithIAMSelected ? scope.join(' ') : '',
        isApiRecipe: props.enableSecureWithIAM,
        oneApiVersionShortName: oneApiSelected ? oneApiVersionShortName : '',
        isSecuredWithCookie : cookieSelected || false,
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
      modalWidth="900px"
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
              {version?.length ? (
                <div id="deployVersionContainer" className="input-field-group">
                  <label className="input-label">Based on previous build</label>
                  <div>
                    <label className="chips">
                      <b>Branch: </b>
                      {buildBranch} | <b> Triggered By: </b>
                      {triggeredBy} | <b> Triggered On: </b>
                      {triggeredOn} | <b> Artifact id: </b>
                      {artifactId} | <b> Version: </b>
                      {version}
                    </label>
                  </div>
                </div>
              ) : (
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
              )}
            </div>
          </div>
          {(props.enableSecureWithIAM || props.isUIRecipe) && (
            <>
              {deployEnvironment === 'staging' && (
                <>
                  <div className={classNames(Styles.threeColumnFlexLayout)}>
                    <div>
                      <label className="checkbox">
                        <span className="wrapper">
                          <input
                            type="checkbox"
                            className="ff-only"
                            checked={secureWithIAMSelected}
                            onChange={onChangeSecureWithIAM}
                            disabled={oneApiSelected}
                            // disabled={projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                            // disabled={disableIntIAM && !projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                          />
                        </span>
                        <span className={classNames('label', oneApiSelected ? Styles.disableText : '')}>
                          Secure with your own IAM Credentials{' '}
                          {isOwner && !props.isUIRecipe && (
                            <span className={classNames(Styles.configLink)} onClick={props.navigateSecurityConfig}>
                              <a target="_blank" rel="noreferrer">
                                {CODE_SPACE_TITLE} (
                                {projectDetails?.publishedSecuirtyConfig?.status ||
                                  projectDetails?.securityConfig?.status ||
                                  'New'}
                                )
                              </a>
                            </span>
                          )}
                        </span>
                      </label>
                    </div>
                    {props.enableSecureWithIAM && (
                      <>
                        <div>OR</div>
                        <div>
                          <label className="checkbox">
                            <span className="wrapper">
                              <input
                                type="checkbox"
                                className="ff-only"
                                checked={oneApiSelected}
                                onChange={onChangeOpenApi}
                                disabled={secureWithIAMSelected}
                                // disabled={projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                                // disabled={disableIntIAM && !projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                              />
                            </span>
                            <span className={classNames('label', secureWithIAMSelected ? Styles.disableText : '')}>Provision your api through oneAPI</span>
                          </label>
                        </div>
                      </>
                    )}
                  </div>
                  {!props.isUIRecipe && (
                    <span>
                      <p
                        style={{ color: 'var(--color-orange)' }}
                        className={classNames(disableIntIAM && secureWithIAMSelected ? '' : 'hide')}
                      >
                        <i className="icon mbc-icon alert circle"></i> You do not have any published Authorization
                        Configuration and therefore no authorization checks would happen.
                      </p>
                    </span>
                  )}
                  {secureWithIAMSelected && (
                    <div>
                      {!props.isUIRecipe && (<div className={Styles.flexLayout}>
                        <div className={Styles.infoIcon}>
                          <label className={classNames("switch", cookieSelected ? 'on' : '')}>
                            <span className="label" style={{ marginRight: '5px' }}>
                              Switch to cookie based authentication
                            </span>
                            <span className="wrapper">
                              <input
                                value={cookieSelected}
                                type="checkbox"
                                className="ff-only"
                                onChange={() => {setCookieSelected(!cookieSelected);}}
                                checked={cookieSelected}
                                maxLength={63}
                              />
                            </span>
                          </label>
                        </div>
                        <div className={Styles.oneAPILink}><label className="chips">{cookieSelected ? 'Cookie based authentication enabled' : 'JWT based authentication enabled (default)'}</label></div>
                      </div>)}
                      {!cookieSelected ? (!projectDetails?.intDeploymentDetails?.secureWithIAMRequired || changeSelected || (!cookieSelected && isSecuredWithCookie) ? (
                        <>
                          <div className={classNames(Styles.wrapper)}>
                            <span className="label">
                              <p>Authorization Code Flow</p>
                            </span>
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
                          </div>
                          <div>
                            <div className={classNames(Styles.flexLayout)}>
                              <TextBox
                                type="text"
                                label={'Redirect Uri'}
                                placeholder={`eg: /${props.codeSpaceData.workspaceId}/cd`}
                                value={redirectUri}
                                required={false}
                                maxLength={500}
                                onChange={(e) => {
                                  setRedirectUri(e.currentTarget.value);
                                }}
                              />
                              <Tags
                                title={'Ignore Paths'}
                                max={100}
                                chips={ignorePath}
                                placeholder={'Type root path here....'}
                                tags={ignorePaths}
                                setTags={onIgnorePathChange}
                                isMandatory={false}
                                isIgnorePath={true}
                                showAllTagsOnFocus={true}
                              />
                            </div>
                            <Tags
                              title={'Scope'}
                              max={100}
                              chips={scope}
                              fixedChips={fixedScope}
                              tags={scopes}
                              setTags={onScopeChnage}
                              isMandatory={false}
                              disableSelfTagAdd={true}
                              suggestionPopupHeight={150}
                              showAllTagsOnFocus={true}
                            />
                          </div>
                        </>
                      ) : (
                        <div className={classNames(Styles.actionWrapper)}>
                          <button
                            className={classNames('btn btn-primary', Styles.saveSettingsBtn)}
                            onClick={() => setChangeSelected(true)}
                          >
                            Change Credentials
                          </button>
                        </div>
                      )) : ''}
                    </div>
                  )}
                  {oneApiSelected && (
                    <>
                      <div className={classNames(Styles.flexLayout)}>
                        <TextBox
                          type="text"
                          label={'Api version shortname'}
                          placeholder={'Api version shortname in oneAPI'}
                          value={oneApiVersionShortName}
                          errorText={oneApiVersionShortNameError}
                          required={true}
                          maxLength={200}
                          onChange={(e) => {
                            setOneApiVersionShortName(e.currentTarget.value);
                            setOneApiVersionShortNameError('');
                          }}
                        />
                        <div className={Styles.oneAPILink}>
                          <a href={Envs.ONE_API_URL} target="_blank" rel="noreferrer">
                            where to provision your api ? 
                          </a>
                        </div>
                      </div>
                      <span>
                        <p
                          style={{ color: 'var(--color-orange)' }}>
                          <i className="icon mbc-icon alert circle"></i> We are supporting only GAS/OIDC. Please ensure that GAS/OIDC is selected as the identity provider under API management in the oneAPI portal.
                        </p>
                      </span>
                    </>
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
                  <div className={classNames(Styles.threeColumnFlexLayout)}>
                    <div>
                      <label className="checkbox">
                        <span className="wrapper">
                          <input
                            type="checkbox"
                            className="ff-only"
                            checked={secureWithIAMSelected}
                            onChange={onChangeSecureWithIAM}
                            disabled={oneApiSelected}
                            // disabled={projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                            // disabled={disableProdIAM && !projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                          />
                        </span>
                        <span className={classNames('label', oneApiSelected ? Styles.disableText : '')}>
                          Secure with your own IAM Credentials{' '}
                          {isOwner && !props.isUIRecipe && (
                            <span className={classNames(Styles.configLink)} onClick={props.navigateSecurityConfig}>
                              <a target="_blank" rel="noreferrer">
                                {CODE_SPACE_TITLE} (
                                {projectDetails?.publishedSecuirtyConfig?.status ||
                                  projectDetails?.securityConfig?.status ||
                                  'New'}
                                )
                              </a>
                            </span>
                          )}
                        </span>
                      </label>
                    </div>
                    {props.enableSecureWithIAM && (
                      <>
                        <div>OR</div>
                        <div>
                          <label className="checkbox">
                            <span className="wrapper">
                              <input
                                type="checkbox"
                                className="ff-only"
                                checked={oneApiSelected}
                                onChange={onChangeOpenApi}
                                disabled={secureWithIAMSelected}
                                // disabled={projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                                // disabled={disableIntIAM && !projectDetails?.prodDeploymentDetails?.secureWithIAMRequired}
                              />
                            </span>
                            <span className={classNames('label', secureWithIAMSelected ? Styles.disableText : '')}>Provision your api through oneAPI</span>
                          </label>
                        </div>
                      </>
                    )}
                  </div>
                  {!props.isUIRecipe && (
                    <span>
                      <p
                        style={{ color: 'var(--color-orange)' }}
                        className={classNames(disableProdIAM && secureWithIAMSelected ? '' : 'hide')}
                      >
                        <i className="icon mbc-icon alert circle"></i> You do not have any published Authorization
                        Configuration and therefore no authorization checks would happen.
                      </p>
                    </span>
                  )}
                  {secureWithIAMSelected && (
                    <div>
                      {!props.isUIRecipe && (<div className={Styles.flexLayout}>
                        <div className={Styles.infoIcon}>
                          <label className={classNames("switch", cookieSelected ? 'on' : '')}>
                            <span className="label" style={{ marginRight: '5px' }}>
                              Switch to cookie based authentication
                            </span>
                            <span className="wrapper">
                              <input
                                value={cookieSelected}
                                type="checkbox"
                                className="ff-only"
                                onChange={() => {setCookieSelected(!cookieSelected);}}
                                checked={cookieSelected}
                                maxLength={63}
                              />
                            </span>
                          </label>
                        </div>
                        <div className={Styles.oneAPILink}><label className="chips">{cookieSelected ? 'Cookie based authentication enabled' : 'JWT based authentication enabled (default)'}</label></div>
                      </div>)}
                      {!cookieSelected ? (!projectDetails?.prodDeploymentDetails?.secureWithIAMRequired || changeSelected || (!cookieSelected && isSecuredWithCookie) ? (
                        <>
                          <div className={classNames(Styles.wrapper)}>
                            <span className="label">
                              <p>Authorization Code Flow</p>
                            </span>
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
                          </div>
                          <div>
                            <div className={classNames(Styles.flexLayout)}>
                              <TextBox
                                type="text"
                                label={'Redirect Uri'}
                                placeholder={`eg: /${props.codeSpaceData.workspaceId}/cd`}
                                value={redirectUri}
                                required={false}
                                maxLength={200}
                                onChange={(e) => {
                                  setRedirectUri(e.currentTarget.value);
                                }}
                              />
                              <Tags
                                title={'Ignore Paths'}
                                max={100}
                                chips={ignorePath}
                                placeholder={'Type root path here....'}
                                tags={ignorePaths}
                                setTags={onIgnorePathChange}
                                isMandatory={false}
                                isIgnorePath={true}
                                showAllTagsOnFocus={true}
                              />
                            </div>
                            <Tags
                              title={'Scope'}
                              max={100}
                              chips={scope}
                              fixedChips={fixedScope}
                              tags={scopes}
                              setTags={onScopeChnage}
                              isMandatory={false}
                              disableSelfTagAdd={true}
                              suggestionPopupHeight={150}
                              showAllTagsOnFocus={true}
                            />
                          </div>
                        </>
                      ) : (
                        <div className={classNames(Styles.actionWrapper)}>
                          <button
                            className={classNames('btn btn-primary', Styles.saveSettingsBtn)}
                            onClick={() => setChangeSelected(true)}
                          >
                            Change Credentials
                          </button>
                        </div>
                      )) : ''}
                    </div>
                  )}
                  {oneApiSelected && (
                    <>
                      <div className={classNames(Styles.flexLayout)}>
                        <TextBox
                          type="text"
                          label={'Api version shortname'}
                          placeholder={'Api version shortname in oneAPI'}
                          value={oneApiVersionShortName}
                          errorText={oneApiVersionShortNameError}
                          required={true}
                          maxLength={200}
                          onChange={(e) => {
                            setOneApiVersionShortName(e.currentTarget.value);
                            setOneApiVersionShortNameError('');
                          }}
                        />
                        <div className={Styles.oneAPILink}>
                          <a href={Envs.ONE_API_URL} target="_blank" rel="noreferrer">
                            where to provision your api ? 
                          </a>
                        </div>
                      </div>
                      <span>
                        <p
                          style={{ color: 'var(--color-orange)' }}>
                          <i className="icon mbc-icon alert circle"></i> We are supporting only GAS/OIDC. Please ensure that GAS/OIDC is selected as the identity provider under API management in the oneAPI portal.
                        </p>
                      </span>
                    </>
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
      scrollableBox={true}
      onCancel={() => props.setShowCodeDeployModal(false)}
    />
  );
};

export default DeployModal;
