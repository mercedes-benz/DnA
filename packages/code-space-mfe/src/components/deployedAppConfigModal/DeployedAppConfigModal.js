import React, { useEffect, useState } from 'react';
import classNames from 'classnames';

import TextBox from 'dna-container/TextBox';
import Tags from 'dna-container/Tags';

import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { Envs } from '../../Utility/envs';

import Styles from './DeployedAppConfigModal.scss';

const DeployedAppConfigModal = (props) => {
  const [deploymentType, setDeploymentType] = useState(props?.deploymentDetails?.deploymentType || 'API');
  const [secureWithIAMSelected, setSecureWithIAMSelected] = useState(props?.deploymentDetails?.secureWithIAMRequired || false);
  const [oneApiSelected, setOneApiSelected] = useState(props?.deploymentDetails?.oneApiVersionShortName?.length || false);
  const [oneApiVersionShortName, setOneApiVersionShortName] = useState(props?.deploymentDetails?.oneApiVersionShortName || '');
  const [cookieSelected, setCookieSelected] = useState(props?.deploymentDetails?.isSecuredWithCookie || false);
  const [isUiRecipe, setIsUiRecipe] = useState(props?.deploymentDetails?.deploymentType === 'UI' ? true : false);
  const [clientId, setClientId] = useState(props?.deploymentDetails?.clientId || '');

  const [oneApiVersionShortNameError, setOneApiVersionShortNameError] = useState('');
  const [clientIdError, setClientIdError] = useState('');
  const [clientSecretError, setClientSecretError] = useState('');
  const [redirectUriError, setRedirectUriError] = useState('');

  const [disableIAM, setDisableIAM] = useState(true);
  const [resetRequired, setResetRequired] = useState(false);
  const [changeSelected, setChangeSelected] = useState(false);
  const [clientSecret, setClientSecret] = useState('');
  const [ignorePath, setIgnorePath] = useState([]);
  const [redirectUri, setRedirectUri] = useState('');
  const [scope, setScope] = useState(['openid', 'offline_access']);
  const [ssoType, setSsoType] = useState(props?.deploymentDetails?.ssoType === 'prod' ? 'prod' : 'int');

  const ignorePaths = [
    { id: '1', name: '/favicon.ico' },
    { id: '2', name: '/manifest.json' },
    { id: '3', name: '/obfuskator-api/int/api/docs' },
    { id: '4', name: '/docs' },
    { id: '5', name: '/obfuskator-api/int/api/openapi.json' },
    { id: '6', name: '/openapi.json' },
  ];

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

  const fixedScope = ['openid', 'offline_access'];

  const deploymentMigrated = props?.deploymentDetails?.deploymentUrl?.includes(Envs.CODESPACE_OIDC_POPUP_URL);
  const envUrl = deploymentMigrated ? Envs.CODESPACE_AWS_DEPLOYMENT_URL : Envs.CODESPACE_DEPLOYMENT_URL;

  useEffect(() => {
    Tooltip.defaultSetup();
    let appId;
    let entitlements;
    // ProgressIndicator.show();
    CodeSpaceApiClient.getPublishedConfig(props?.workspaceId, props?.isStaging ? 'int' : 'prod').then((res) => {
      appId = res.data.appID || '';
      entitlements = res.data.entitlements || [];
      appId.length !== 0 && entitlements.length !== 0 ? setDisableIAM(false) : setDisableIAM(true);
    });
    const env = props?.isStaging ? 'int' : 'prod';
    setRedirectUri(
      props?.deploymentDetails?.redirectUri
        ? `${envUrl}/${props?.deploymentDetails?.redirectUri}`
        : props?.deploymentDetails?.deploymentType === 'UI'
          ? `${envUrl}/${props?.projectName}/${env}/cb`
          : '',
    );
    props?.deploymentDetails?.ignorePaths?.length && setIgnorePath(props?.deploymentDetails?.ignorePaths?.split(','));
    props?.deploymentDetails?.scope?.length && setScope(props?.deploymentDetails?.scope?.split(' '));
    return Tooltip.clear();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    setRedirectUriError('');
    if (deploymentType === 'API') {
      setIsUiRecipe(false);
      setRedirectUri('');
    } else {
      setIsUiRecipe(true);
      setRedirectUri(`${envUrl}/${props?.projectName}/${props?.isStaging ? 'int' : 'prod'}/cb`);
      setOneApiSelected(false);
    }
  }, [deploymentType]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    Tooltip.defaultSetup();
    const shouldReset =
      (deploymentType !== 'UI' &&
        cookieSelected &&
        props?.deploymentDetails?.secureWithIAMRequired &&
        !props?.deploymentDetails?.isSecuredWithCookie) ||
      (secureWithIAMSelected &&
        !cookieSelected &&
        props?.deploymentDetails?.secureWithIAMRequired &&
        props?.deploymentDetails?.isSecuredWithCookie) ||
      (props?.deploymentDetails?.deploymentType?.length
        ? deploymentType !== props?.deploymentDetails?.deploymentType
        : deploymentType === 'UI');
    setResetRequired(shouldReset);
  }, [secureWithIAMSelected, cookieSelected, deploymentType]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const redirectUri =
      deploymentType === 'UI' ? `${envUrl}/${props?.projectName}/${props?.isStaging ? 'int' : 'prod'}/cb` : '';
    if (resetRequired) {
      setClientId('');
      setClientSecret('');
      setRedirectUri(redirectUri);
      setIgnorePath([]);
      setScope(['openid', 'offline_access']);
    } else {
      setClientId(props?.deploymentDetails?.clientId || '');
      setRedirectUri(
        props?.deploymentDetails?.redirectUri ? `${envUrl}/${props?.deploymentDetails?.redirectUri}` : redirectUri,
      );
      props?.deploymentDetails?.ignorePaths?.length && setIgnorePath(props?.deploymentDetails?.ignorePaths?.split(','));
      props?.deploymentDetails?.scope?.length && setScope(props?.deploymentDetails?.scope?.split(' '));
    }
  }, [resetRequired]); // eslint-disable-line react-hooks/exhaustive-deps

  const onChangeSecureWithIAM = (e) => {
    setSecureWithIAMSelected(e.target.checked);
    e.target.checked ? setOneApiSelected(false) : '';
  };

  const onChangeOpenApi = (e) => {
    setOneApiSelected(e.target.checked);
    e.target.checked ? setSecureWithIAMSelected(false) : '';
  };

  const onIgnorePathChange = (selectedTags) => {
    setIgnorePath(selectedTags);
  };

  const onScopeChnage = (selectedTags) => {
    setScope(selectedTags);
  };

  const onSaveConfig = () => {
    let formValid = true;
    const secureWithIAMValidation = secureWithIAMSelected &&
      ((!props?.deploymentDetails?.secureWithIAMRequired ||
        changeSelected || resetRequired));
    if (secureWithIAMValidation && clientId.length === 0) {
      formValid = false;
      setClientIdError('*Missing Entry');
    }
    if (secureWithIAMValidation && clientSecret.length === 0) {
      formValid = false;
      setClientSecretError('*Missing Entry');
    }
    if (secureWithIAMValidation && isUiRecipe && redirectUri.length === 0) {
      formValid = false;
      setRedirectUriError('*Missing Entry');
    }
    if (ignorePath.length !== 0 && ignorePath.some(item => item.endsWith('/') || item.includes(' ') || !item.startsWith('/'))) {
      formValid = false;
    }
    if (oneApiSelected && oneApiVersionShortName?.length === 0) {
      formValid = false;
      setOneApiVersionShortNameError('*Missing Entry');
    }
    if (formValid) {
      const configRequest = {
        secureWithIAMRequired: secureWithIAMSelected,
        clientID: secureWithIAMSelected ? clientId : '',
        clientSecret: clientSecret,
        redirectUri: (secureWithIAMSelected && deploymentType === 'UI' && redirectUri?.length) ? redirectUri?.split(envUrl)[1] : '',
        ignorePaths: secureWithIAMSelected && ignorePath?.length ? ignorePath?.join(',') : '',
        scope: secureWithIAMSelected ? scope?.join(' ') : '',
        isApiRecipe: deploymentType === 'API',
        oneApiVersionShortName: oneApiSelected ? oneApiVersionShortName : '',
        isSecuredWithCookie: (secureWithIAMSelected && deploymentType === 'API' && cookieSelected) || false,
        ssoType: secureWithIAMSelected ? ssoType : '',
      };
      console.log("save ", configRequest);
    }
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.securityConfigMainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <span className="label">
            <p>Deployment Type</p>
          </span>
          <div>
            <label className={classNames('radio')}>
              <span className="wrapper">
                <input
                  type="radio"
                  className="ff-only"
                  value="API"
                  name="deploymentType"
                  onChange={(e) => {
                    setDeploymentType(e.currentTarget.value.trim());
                  }}
                  checked={deploymentType === 'API'}
                />
              </span>
              <span className="label">
                API recipe deployment <span className={classNames(Styles.warning)}>(Backend Application)</span>
              </span>
            </label>
            <label className={classNames('radio')}>
              <span className="wrapper">
                <input
                  type="radio"
                  className="ff-only"
                  value="UI"
                  name="deploymentType"
                  onChange={(e) => {
                    setDeploymentType(e.currentTarget.value.trim());
                  }}
                  checked={deploymentType === 'UI'}
                />
              </span>
              <span className="label">
                UI recipe deployment <span className={classNames(Styles.warning)}>(Frontend Application)</span>
              </span>
            </label>
          </div>
        </div>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.credentialsFlexLayout)}>
            <div>
              <span className="label">
                <p>Authentication Type</p>
              </span>
            </div>
            {props?.deploymentDetails?.secureWithIAMRequired && !changeSelected && !resetRequired && secureWithIAMSelected ? (
              <div className={classNames(Styles.credentialsLink)}>
                <span
                  className={classNames(Styles.linkButton)}
                  onClick={() => setChangeSelected(true)}
                  tooltip-data="Click to update authentication credentials."
                >
                  Update Credentials ?
                </span>
              </div>
            ) : (
              <div></div>
            )}
          </div>
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
                  />
                </span>
                <span className={classNames('label', oneApiSelected ? Styles.disableText : '')}>
                  Secure with your own IAM Credentials{' '}
                  {/* {!isUiRecipe && (
                    <span className={classNames(Styles.configLink)} onClick={props?.navigateSecurityConfig}>
                      <a target="_blank" rel="noreferrer">
                        {CODE_SPACE_TITLE} (
                        {props.publishedSecuirtyConfig?.status || props?.securityConfig?.status || 'New'})
                      </a>
                    </span>
                  )} */}
                </span>
              </label>
            </div>
            {deploymentType === 'API' && (
              <>
                <div>OR</div>
                <div className={classNames(Styles.oneApi)}>
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
                    <span className={classNames('label', secureWithIAMSelected ? Styles.disableText : '')}>
                      Provision your api through oneAPI
                    </span>
                  </label>
                </div>
              </>
            )}
          </div>
          {!isUiRecipe && (
            // <span>
            //   <p
            //     style={{ color: 'var(--color-orange)' }}
            //     className={classNames(Styles.align, disableIAM && secureWithIAMSelected ? '' : 'hide')}
            //   >
            //     <i className="icon mbc-icon alert circle"></i> You can configure your authorization config <a target="_blank" rel="noreferrer" onClick={props?.navigateSecurityConfig}>here</a>.
            //   </p>
            // </span>
            <span className={classNames(Styles.configLink, Styles.align, disableIAM && secureWithIAMSelected ? '' : 'hide')} onClick={props?.navigateSecurityConfig}>
              <a target="_blank" rel="noreferrer">
                Configure your authorization config
              </a>
            </span>
          )}
          {secureWithIAMSelected && !isUiRecipe && (
            <div className={classNames(Styles.align, Styles.flexLayout)}>
              <div className={Styles.infoIcon}>
                <label className={classNames('switch', cookieSelected ? 'on' : '')}>
                  <span className="label" style={{ marginRight: '5px' }}>
                    Switch to cookie based authentication
                  </span>
                  <span className="wrapper">
                    <input
                      value={cookieSelected}
                      type="checkbox"
                      className="ff-only"
                      onChange={() => {
                        setCookieSelected(!cookieSelected);
                      }}
                      checked={cookieSelected}
                      maxLength={63}
                    />
                  </span>
                </label>
              </div>
              <div className={Styles.oneAPILink}>
                <label className={classNames('chips', Styles.chip)}>
                  {cookieSelected
                    ? 'Cookie based authentication enabled'
                    : 'OIDC based authentication enabled (default)'}
                </label>
              </div>
            </div>
          )}
        </div>
        {(!props?.deploymentDetails?.secureWithIAMRequired || changeSelected || resetRequired) &&
          secureWithIAMSelected && (
            <>
              <div className={classNames(Styles.wrapper)}>
                <span className="label">
                  <p>Single Sign On Type</p>
                </span>
                <div>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value="int"
                        name="ssoType"
                        onChange={(e) => {
                          setSsoType(e.currentTarget.value.trim());
                        }}
                        checked={ssoType === 'int'}
                      />
                    </span>
                    <span className="label">
                      MB SSO int
                    </span>
                  </label>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value="prod"
                        name="ssoType"
                        onChange={(e) => {
                          setSsoType(e.currentTarget.value.trim());
                        }}
                        checked={ssoType === 'prod'}
                      />
                    </span>
                    <span className="label">
                      MB SSO prod
                    </span>
                  </label>
                </div>
              </div>
              <div className={classNames(Styles.wrapper)}>
                <span className="label">
                  <p>{isUiRecipe ? 'Authorization Code Flow' : 'Client Credentials Grant / Authorization Code Flow'}</p>
                </span>
                <div className={classNames(Styles.align, Styles.flexLayout)}>
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
              <div className={classNames(Styles.wrapper)}>
                <span className="label">
                  <p>Additional Configuration</p>
                </span>
                <div className={classNames(Styles.align, isUiRecipe ? Styles.flexLayout : '')}>
                  {isUiRecipe && (
                    <div className={classNames(Styles.redirectFlexLayout)}>
                      <TextBox
                        type="text"
                        label={'Redirect Uri'}
                        placeholder={`eg:${envUrl}/${props?.projectName}/${props?.isStaging ? 'int' : 'prod'}/cb`}
                        value={redirectUri}
                        required={isUiRecipe}
                        errorText={redirectUriError}
                        maxLength={200}
                        onChange={(e) => {
                          setRedirectUri(e.currentTarget.value);
                          setRedirectUriError('');
                        }}
                      />
                      <div>
                        <i
                          className="icon mbc-icon info"
                          tooltip-data="Note: Make sure the Redirect Url is part of the Client Id OIDC Service Config Redirect URI(s)"
                        />{' '}
                      </div>
                    </div>
                  )}

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
                    isDeployedAppConfig={true}
                  />
                </div>
                <div className={classNames(Styles.align)}>
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
                    isDeployedAppConfig={true}
                  />
                </div>
              </div>
            </>
          )}
        {oneApiSelected && (
          <div className={classNames(Styles.wrapper)}>
            <span className="label">
              <p>oneAPI Credentials</p>
            </span>
            <div className={classNames(Styles.align, Styles.flexLayout)}>
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
                  Where to provision your api ?
                </a>
              </div>
            </div>
            <span>
              <p style={{ color: 'var(--color-orange)' }} className={Styles.align}>
                <i className="icon mbc-icon alert circle"></i> We are currently supporting only GAS/OIDC. Please ensure
                that GAS/OIDC is selected as the identity provider under API management in the oneAPI portal.
              </p>
            </span>
          </div>
        )}
        <div className={Styles.saveButton}>
          <button
            className={'btn btn-tertiary'}
            type="button"
            onClick={onSaveConfig}
          >
            Save
          </button>
        </div>
      </div>
    </React.Fragment>
  );
};
export default DeployedAppConfigModal;
