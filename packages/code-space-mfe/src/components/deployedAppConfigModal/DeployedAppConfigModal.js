import React, { useEffect, useState } from 'react';
import classNames from 'classnames';

import TextBox from 'dna-container/TextBox';
import Tags from 'dna-container/Tags';
import Modal from 'dna-container/Modal';

import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { Envs } from '../../Utility/envs';

import Styles from './DeployedAppConfigModal.scss';
import { ProgressIndicator } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import Notification from '../../common/modules/uilab/js/src/notification';

const DeployedAppConfigModal = (props) => {
  const [deploymentType, setDeploymentType] = useState(props?.deploymentDetails?.deploymentType || 'API');
  const [secureWithIAMSelected, setSecureWithIAMSelected] = useState(
    props?.deploymentDetails?.secureWithIAMRequired || false,
  );
  const [oneApiSelected, setOneApiSelected] = useState(
    props?.deploymentDetails?.oneApiVersionShortName?.length || false,
  );
  const [oneApiVersionShortName, setOneApiVersionShortName] = useState(
    props?.deploymentDetails?.oneApiVersionShortName || '',
  );
  const [cookieSelected, setCookieSelected] = useState(props?.deploymentDetails?.isSecuredWithCookie || false);
  const [isUiRecipe, setIsUiRecipe] = useState(props?.deploymentDetails?.deploymentType === 'UI' ? true : false);
  const [clientId, setClientId] = useState(props?.deploymentDetails?.clientId || '');
  const [secureWithDnaSelected, setSecureWithDnaSelected] = useState(
    props?.deploymentDetails?.secureWithDnaRequired || false,
  );
  const [ssoType, setSsoType] = useState(props?.deploymentDetails?.ssoType === 'SSO_PROD' ? 'SSO_PROD' : 'SSO_INT');

  const [oneApiVersionShortNameError, setOneApiVersionShortNameError] = useState('');
  const [clientIdError, setClientIdError] = useState('');
  const [clientSecretError, setClientSecretError] = useState('');
  const [redirectUriError, setRedirectUriError] = useState('');

  // const [disableIAM, setDisableIAM] = useState(true);
  const [resetRequired, setResetRequired] = useState(false);
  const [changeSelected, setChangeSelected] = useState(false);
  const [clientSecret, setClientSecret] = useState('');
  const [ignorePath, setIgnorePath] = useState([]);
  const [ignorePathError, setIgnorePathError] = useState('');
  const [redirectUri, setRedirectUri] = useState('');
  const [scope, setScope] = useState(['openid', 'offline_access']);
  const [pluginEnabled, setPluginEnabled] = useState(false);
  const [showEnablePluginWarning, setShowEnablePluginWarning] = useState(false);
  const [securedWithIAMWarning, setSecuredWithIAMWarning] = useState(false);
  const [enableAliceRole, setEnableAliceRole] = useState(props?.deploymentDetails?.aliceRoleEnabled || false);
  const [enableEntitlementPrefix, setEnableEntitlementPrefix] = useState(false);
  const [existingRoles, setExistingRoles] = useState([]);
  const [selectedRoles, setSelectedRoles] = useState(props?.deploymentDetails?.selectedAliceRoles || []);
  const [aliceRolesError, setAliceRolesError] = useState('');

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
    { id: '10', name: 'organizational_data' },
  ];

  const entitlementScope = scopes
    .filter((scope) =>
      [
        'openid',
        'autorization_group',
        'entitlement_group',
        'scoped_entitlement',
        'email',
        'profile',
        'organizational_data',
      ].includes(scope.name),
    )
    .map((scope) => {
      return (
        <label key={scope.name} className={classNames('chips',Styles.deployConfigChips)}>
          {scope.name}
        </label>
      );
    });

  const userInfoScope = scopes
    .filter((scope) =>
      ['openid', 'autorization_group', 'scoped_entitlement', 'email', 'profile', 'organizational_data'].includes(
        scope.name,
      ),
    )
    .map((scope) => {
      return (
        <label key={scope.name} className={classNames('chips',Styles.deployConfigChips)}>
          {scope.name}
        </label>
      );
    });

  const fixedScope = ['openid', 'offline_access', 'entitlement_group'];

  const deploymentMigrated = props?.deploymentDetails?.deploymentUrl?.includes(Envs.CODESPACE_OIDC_POPUP_URL);
  const envUrl = deploymentMigrated ? Envs.CODESPACE_AWS_DEPLOYMENT_URL : Envs.CODESPACE_DEPLOYMENT_URL;

  useEffect(() => {
    Tooltip.defaultSetup();
    setCookieSelected(false); //remove once cookie enabled
    // let appId;
    // let entitlements;
    // ProgressIndicator.show();
    // CodeSpaceApiClient.getPublishedConfig(props?.workspaceId, props?.isStaging ? 'int' : 'prod')
    //   .then((res) => {
    //     appId = res.data.appID || '';
    //     entitlements = res.data.entitlements || [];
    //     appId.length !== 0 && entitlements.length !== 0 ? setDisableIAM(false) : setDisableIAM(true);
    //   })
    //   .catch((err) => {
    //     ProgressIndicator.hide();
    //     Notification.show(
    //       'Error in fetching published config. Please try again later.\n' +
    //         err?.response?.data?.errors[0]?.message,
    //       'alert',
    //     );
    //   });
    const env = props?.isStaging ? 'int' : 'prod';
    setRedirectUri(
      props?.deploymentDetails?.redirectUri
        ? `${envUrl}${props?.deploymentDetails?.redirectUri.toLowerCase()}`
        : props?.deploymentDetails?.deploymentType === 'UI'
        ? `${envUrl}/${props?.projectName.toLowerCase()}/${env}/cb`
        : '',
    );
    props?.deploymentDetails?.ignorePaths?.length && setIgnorePath(props?.deploymentDetails?.ignorePaths?.split(','));
    props?.deploymentDetails?.scope?.length && setScope(props?.deploymentDetails?.scope?.split(' '));
    if (props?.deploymentDetails?.secureWithIAMRequired || props?.deploymentDetails?.secureWithDnaRequired) {
      ProgressIndicator.show();
      CodeSpaceApiClient.getPluginStatus(props?.workspaceId, props?.isStaging ? 'int' : 'prod', 'oidc')
        .then((res) => {
          ProgressIndicator.hide();
          setPluginEnabled(res?.data?.enabled || false);
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(
            'Error in fetching OIDC plugin status. Please try again later.\n' + err?.response?.data?.errors[0]?.message,
            'alert',
          );
          // Notification.show('Error in fetching plugin status. Please try again later.', 'alert');
        });
    }
    return Tooltip.clear();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    setRedirectUriError('');
    if (deploymentType === 'API') {
      setIsUiRecipe(false);
      setRedirectUri('');
      setEnableAliceRole(false);
    } else {
      setIsUiRecipe(true);
      setRedirectUri(`${envUrl}/${props?.projectName.toLowerCase()}/${props?.isStaging ? 'int' : 'prod'}/cb`);
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
        : deploymentType === 'UI') ||
      (secureWithIAMSelected && props?.deploymentDetails?.secureWithDnaRequired) ||
      (secureWithDnaSelected && props?.deploymentDetails?.secureWithIAMRequired);
    setResetRequired(shouldReset);
  }, [secureWithIAMSelected, cookieSelected, deploymentType, secureWithDnaSelected]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const redirectUri =
      deploymentType === 'UI'
        ? `${envUrl}/${props?.projectName.toLowerCase()}/${props?.isStaging ? 'int' : 'prod'}/cb`
        : '';
    if (resetRequired) {
      setClientId('');
      setClientSecret('');
      setRedirectUri(redirectUri);
      setIgnorePath([]);
      setScope(['openid', 'offline_access']);
      setEnableAliceRole(false);
      setEnableEntitlementPrefix(false);
      setSelectedRoles([]);
    } else {
      setClientId(props?.deploymentDetails?.clientId || '');
      setRedirectUri(
        props?.deploymentDetails?.redirectUri
          ? `${envUrl}${props?.deploymentDetails?.redirectUri.toLowerCase()}`
          : redirectUri,
      );
      props?.deploymentDetails?.ignorePaths?.length && setIgnorePath(props?.deploymentDetails?.ignorePaths?.split(','));
      props?.deploymentDetails?.scope?.length && setScope(props?.deploymentDetails?.scope?.split(' '));
      setEnableAliceRole(props?.deploymentDetails?.aliceRoleEnabled || false);
      if (props?.deploymentDetails?.selectedRoles?.length) {
        const updatedRoles = props?.deploymentDetails?.selectedRoles.map((role) =>
          role.startsWith('DNA.') ? role.replace('DNA.', '') : role,
        );
        setSelectedRoles(updatedRoles);
      }
      setEnableEntitlementPrefix(props?.deploymentType?.entitlementPrefixEnabled || false);
    }
  }, [resetRequired]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (enableAliceRole) {
      setScope(['openid', 'offline_access', 'entitlement_group']);
      CodeSpaceApiClient.getExistingRoles(Envs.CODESPACE_SECURITY_APP_ID)
        .then((res) => {
          if (res?.data?.roles) {
            const existingRoles = res?.data?.roles.map((role) => ({
              id: role.roleID,
              name: role.roleID,
            }));
            setExistingRoles(existingRoles);
          } else {
            if (res?.data?.errors[0]?.message?.length > 0) {
              Notification.show(res?.errors[0]?.message, 'alert');
            }
            if (res?.data?.warnings[0]?.message?.length > 0) {
              Notification.show(res?.warnings[0]?.message, 'warning');
            }
          }
        })
        .catch((err) => {
          Notification.show(err?.message || 'Something went wrong', 'alert');
        });
    } else {
      setScope(['openid', 'offline_access']);
    }
  }, [enableAliceRole]); // eslint-disable-line react-hooks/exhaustive-deps

  const onChangeSecureWithIAM = (e) => {
    if (!e.target.checked && props?.deploymentDetails?.secureWithIAMRequired) {
      setSecuredWithIAMWarning(true);
    } else {
      setSecureWithIAMSelected(e.target.checked);
    }
    if (e.target.checked) {
      setOneApiSelected(false);
      setSecureWithDnaSelected(false);
      setEnableAliceRole(false);
    }
  };

  const onChangeSecureWithDna = (e) => {
    if (!e.target.checked && props?.deploymentDetails?.secureWithDnaRequired) {
      setSecuredWithIAMWarning(true);
    } else {
      setSecureWithDnaSelected(e.target.checked);
    }
    if (e.target.checked) {
      setOneApiSelected(false);
      setSecureWithIAMSelected(false);
      setSsoType(Envs.DNA_SSO_TYPE);
    }
  };

  const onChangeOpenApi = (e) => {
    setOneApiSelected(e.target.checked);
    if (e.target.checked) {
      setSecureWithIAMSelected(false);
      setSecureWithDnaSelected(false);
      setEnableAliceRole(false);
    }
  };

  const onIgnorePathChange = (selectedTags) => {
    setIgnorePath(selectedTags);
    const ignorePathError = selectedTags.some(
      (item) => item.endsWith('/') || item.includes(' ') || !item.startsWith('/'),
    );
    ignorePathError
      ? setIgnorePathError(`*path should start with '/' and path should not end with '/' or include white spaces.`)
      : setIgnorePathError('');
  };

  const onScopeChange = (selectedTags) => {
    setScope(selectedTags);
  };

  const onRolesChange = (selectedTags) => {
    const upperCaseTags = selectedTags.map((tag) => tag.toUpperCase());
    setSelectedRoles(upperCaseTags);
    if (enableEntitlementPrefix) {
      const entitlementPrefixError = upperCaseTags.some((tag) => !tag.startsWith(`${Envs.DNA_ENTITLEMENT_PREFIX}.`));
      entitlementPrefixError
        ? setAliceRolesError(`*Entitlement prefix should start with ${Envs.DNA_ENTITLEMENT_PREFIX}. and then your prefix name`)
        : setAliceRolesError('');
    } else {
      const aliceRoleError = upperCaseTags.some((tag) => !tag.startsWith(`${Envs.CODESPACE_SECURITY_APP_ID}_`));
      aliceRoleError
        ? setAliceRolesError(`*Role names should start with ${Envs.CODESPACE_SECURITY_APP_ID}_`)
        : setAliceRolesError('');
    }
  };

  const onSaveConfig = () => {
    let formValid = true;
    const secureWithIAMValidation =
      secureWithIAMSelected && (!props?.deploymentDetails?.secureWithIAMRequired || changeSelected || resetRequired);
    const secureWithDnaValidation =
      secureWithDnaSelected && (!props?.deploymentDetails?.secureWithDnaRequired || changeSelected || resetRequired);
    if (secureWithIAMValidation && clientId.length === 0) {
      formValid = false;
      setClientIdError('*Missing Entry');
    }
    if (secureWithIAMValidation && clientSecret.length === 0) {
      formValid = false;
      setClientSecretError('*Missing Entry');
    }
    if ((secureWithIAMValidation || secureWithDnaValidation) && isUiRecipe && redirectUri.length === 0) {
      formValid = false;
      setRedirectUriError('*Missing Entry');
    }
    if (ignorePathError?.length > 0 || aliceRolesError?.length > 0) {
      formValid = false;
    }
    if (oneApiSelected && oneApiVersionShortName?.length === 0) {
      formValid = false;
      setOneApiVersionShortNameError('*Missing Entry');
    }
    if (enableAliceRole && enableEntitlementPrefix && selectedRoles.length === 0) {
      formValid = false;
      setAliceRolesError('*Missing Entry');
    } else{
      setAliceRolesError('');
    }
    if (formValid) {
      const entitlementScope = 'openid autorization_group entitlement_group scoped_entitlement email profile organizational_data';
      const userInfoScope = 'openid autorization_group scoped_entitlement email profile organizational_data';
      const configRequest = {
        targetEnvironment: props?.isStaging ? 'int' : 'prod',
        secureWithIAMRequired: secureWithIAMSelected,
        secureWithDnaRequired: secureWithDnaSelected,
        clientID: secureWithIAMSelected ? clientId : secureWithDnaSelected ? Envs.DNA_CLIENT_ID : '',
        clientSecret: clientSecret,
        redirectUri:
          (secureWithIAMSelected || secureWithDnaSelected) && deploymentType === 'UI' && redirectUri?.length
            ? redirectUri?.split(envUrl)[1]
            : '',
        ignorePaths:
          (secureWithIAMSelected || secureWithDnaSelected) && ignorePath?.length ? ignorePath?.join(',') : '',
        scope: secureWithIAMSelected ? scope?.join(' ') : (secureWithDnaSelected ? (enableAliceRole ? entitlementScope : userInfoScope) : ''),
        isApiRecipe: deploymentType === 'API',
        oneApiVersionShortName: oneApiSelected ? oneApiVersionShortName : '',
        // isSecuredWithCookie: (secureWithIAMSelected && deploymentType === 'API' && cookieSelected) || false,
        isSecuredWithCookie: false,
        ssoType: secureWithIAMSelected ? ssoType : secureWithDnaSelected ? Envs.DNA_SSO_TYPE : 'SSO_INT',
        aliceRoleEnabled: enableAliceRole,
        selectedAliceRoles: enableAliceRole ? selectedRoles : [],
        entitlementPrefixEnabled: enableAliceRole && enableEntitlementPrefix,
      };
      ProgressIndicator.show();
      CodeSpaceApiClient.updateDeployedAppConfig(props?.workspaceId, configRequest)
        .then((res) => {
          ProgressIndicator.hide();
          if (res?.data?.success === 'SUCCESS') {
            Notification.show(`Code space '${props?.projectName}' updated successfully.`);
            props.setShowDeployedAppConfigModal(false);
            props.onGetCodespaceData();
          } else {
            Notification.show(
              'Error in updating deployed app config. Please try again later.\n' + res?.data?.errors[0]?.message,
              'alert',
            );
          }
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(
            'Error in updating deployed app config. Please try again later.\n' +
              err?.response?.data?.errors[0]?.message,
            'alert',
          );
        });
    }
  };

  const togglePlugin = () => {
    const publishedSecurityConfig = props?.isStaging
      ? props?.securityConfig?.staging?.published
      : props?.securityConfig?.production?.published;
    if (!pluginEnabled && publishedSecurityConfig?.appID?.length) {
      setShowEnablePluginWarning(true);
    } else {
      onPluginStatusChange();
    }
  };

  const onPluginStatusChange = (enableAuthorizer = false) => {
    setShowEnablePluginWarning(false);
    ProgressIndicator.show();
    CodeSpaceApiClient.updatePluginStatus(props?.workspaceId, props?.isStaging ? 'int' : 'prod', 'oidc', !pluginEnabled)
      .then((res) => {
        if (res?.data?.success === 'SUCCESS') {
          Notification.show(`Oidc plugin updated successfully`);
          setPluginEnabled(!pluginEnabled);
          if (enableAuthorizer) {
            CodeSpaceApiClient.updatePluginStatus(
              props?.workspaceId,
              props?.isStaging ? 'int' : 'prod',
              'apiauthoriser',
              true,
            )
              .then((res) => {
                if (res?.data?.success === 'SUCCESS') {
                  Notification.show(` Api authoriser updated successfully`);
                } else {
                  Notification.show(
                    'Error in updating api authoriser plugin. Please try again later.\n' +
                      res?.data?.errors[0]?.message,
                    'alert',
                  );
                  // Notification.show('Error in updating api authoriser plugin', 'alert');
                }
              })
              .catch((err) => {
                Notification.show(
                  'Error in updating api authoriser plugin. Please try again later.\n' +
                    err?.response?.data?.errors[0]?.message,
                  'alert',
                );
                // Notification.show('Error in updating api authoriser Plugin. Please try again later.', 'alert');
              });
          }
        } else {
          Notification.show(
            'Error in updating oidc plugin. Please try again later.\n' + res?.data?.errors[0]?.message,
            'alert',
          );
          // Notification.show('Error in updating OIDC plugin', 'alert');
        }
      })
      .catch((err) => {
        Notification.show(
          'Error in updating oidc plugin. Please try again later.\n' + err?.response?.data?.errors[0]?.message,
          'alert',
        );
        // Notification.show('Error in updating OIDC Plugin. Please try again later.', 'alert');
      })
      .finally(() => {
        ProgressIndicator.hide();
        if(pluginEnabled){
          props.setShowDeployedAppConfigModal(false);
        }
      });
  };

  const navigateAliceRoleCreate = () => {
    window.open(`${window.location.pathname}#/aliceRoleRequest`);
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
        {((props?.deploymentDetails?.secureWithIAMRequired && secureWithIAMSelected) ||
          (props?.deploymentDetails?.secureWithDnaRequired && secureWithDnaSelected)) &&
          !changeSelected &&
          !resetRequired && (
            <div className={classNames(Styles.pluginStatus)}>
              <div className={Styles.infoIcon}>
                <label className={classNames('switch', pluginEnabled ? 'on' : '')}>
                  <span className="label" style={{ marginRight: '5px' }}>
                    SSO Authentication enabled
                  </span>
                  <span className="wrapper">
                    <input
                      value={pluginEnabled}
                      type="checkbox"
                      className="ff-only"
                      onChange={() => togglePlugin()}
                      checked={pluginEnabled}
                      maxLength={63}
                    />
                  </span>
                </label>
              </div>
            </div>
          )}
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.credentialsFlexLayout)}>
            <div>
              <span className="label">
                <p>Authentication Type</p>
              </span>
            </div>
            {((props?.deploymentDetails?.secureWithIAMRequired && secureWithIAMSelected) ||
              (props?.deploymentDetails?.secureWithDnaRequired && secureWithDnaSelected)) &&
            !changeSelected &&
            !resetRequired ? (
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
                    checked={secureWithDnaSelected}
                    onChange={onChangeSecureWithDna}
                    disabled={oneApiSelected || secureWithIAMSelected}
                  />
                </span>
                <span
                  className={classNames('label', oneApiSelected || secureWithIAMSelected ? Styles.disableText : '')}
                >
                  Secure with DnA IAM Credentials{' '}
                </span>
              </label>
            </div>
            <div>
              <label className="checkbox">
                <span className="wrapper">
                  <input
                    type="checkbox"
                    className="ff-only"
                    checked={secureWithIAMSelected}
                    onChange={onChangeSecureWithIAM}
                    disabled={oneApiSelected || secureWithDnaSelected}
                  />
                </span>
                <span
                  className={classNames('label', oneApiSelected || secureWithDnaSelected ? Styles.disableText : '')}
                >
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
            {deploymentType === 'API' ? (
              // <>
              //   <div>OR</div>
              <div className={classNames(Styles.oneApi)}>
                <label className="checkbox">
                  <span className="wrapper">
                    <input
                      type="checkbox"
                      className="ff-only"
                      checked={oneApiSelected}
                      onChange={onChangeOpenApi}
                      disabled={secureWithIAMSelected || secureWithDnaSelected}
                      // disabled={projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                      // disabled={disableIntIAM && !projectDetails?.intDeploymentDetails?.secureWithIAMRequired}
                    />
                  </span>
                  <span
                    className={classNames(
                      'label',
                      secureWithIAMSelected || secureWithDnaSelected ? Styles.disableText : '',
                    )}
                  >
                    Provision your api through oneAPI
                  </span>
                </label>
              </div>
            ) : (
              // </>
              <div></div>
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
            <span
              className={classNames(
                Styles.configLink,
                Styles.align,
                (props?.deploymentDetails?.secureWithIAMRequired || props?.deploymentDetails?.secureWithDnaRequired) &&
                  !resetRequired &&
                  (secureWithIAMSelected || secureWithDnaSelected)
                  ? ''
                  : 'hide',
              )}
              // className={classNames(Styles.configLink, Styles.align, disableIAM && secureWithIAMSelected ? '' : 'hide')}
              onClick={props?.navigateSecurityConfig}
            >
              <a target="_blank" rel="noreferrer">
                Configure your authorization config
              </a>
            </span>
          )}
          {(!props?.deploymentDetails?.secureWithDnaRequired || changeSelected || resetRequired) &&
            secureWithDnaSelected &&
            isUiRecipe && (
              // <div className={classNames(Styles.align, Styles.flexLayout)}>
              <div className={classNames(Styles.align, Styles.infoIcon)}>
                <label className={classNames('switch', enableAliceRole ? 'on' : '')}>
                  <span className="label" style={{ marginRight: '5px' }}>
                    Use entitlements in cookie authentication
                  </span>
                  <span className="wrapper">
                    <input
                      value={enableAliceRole}
                      type="checkbox"
                      className="ff-only"
                      onChange={() => {
                        setEnableAliceRole(!enableAliceRole);
                      }}
                      checked={enableAliceRole}
                      // maxLength={63}
                    />
                  </span>
                </label>
              </div>
              // </div>
            )}
          {/* {secureWithIAMSelected && !isUiRecipe && (
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
          )} */}
        </div>
        {((!props?.deploymentDetails?.secureWithIAMRequired && !props?.deploymentDetails?.secureWithDnaRequired) ||
          changeSelected ||
          resetRequired) &&
          (secureWithIAMSelected || secureWithDnaSelected) && (
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
                        value="SSO_INT"
                        name="ssoType"
                        disabled={secureWithDnaSelected}
                        onChange={(e) => {
                          setSsoType(e.currentTarget.value.trim());
                        }}
                        checked={ssoType === 'SSO_INT'}
                      />
                    </span>
                    <span className="label">MB SSO int</span>
                  </label>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value="SSO_PROD"
                        name="ssoType"
                        disabled={secureWithDnaSelected}
                        onChange={(e) => {
                          setSsoType(e.currentTarget.value.trim());
                        }}
                        checked={ssoType === 'SSO_PROD'}
                      />
                    </span>
                    <span className="label">MB SSO prod</span>
                  </label>
                </div>
              </div>
              <div className={classNames(Styles.wrapper)}>
                <span className="label">
                  <p>
                    {isUiRecipe
                      ? 'SSO Authentication with Authorization Code Flow'
                      : 'Client Credentials Grant / Authorization Code Flow'}
                  </p>
                </span>
                {secureWithIAMSelected ? (
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
                ) : (
                  <div className={classNames(Styles.align, Styles.flexLayout)}>
                    <div>
                      <label className={classNames(Styles.clientIdLabel)}>Client ID</label>
                      <div>
                        <label className={classNames('chips', Styles.deployConfigChips)}>{Envs.DNA_CLIENT_ID}</label>
                      </div>
                    </div>
                    {isUiRecipe && (
                      <div className={classNames(Styles.oneAPILink, Styles.clientIdLabel)}>
                        Since you are securing your application with our credentials, please contact us on our{' '}
                        <a href={Envs.CODESPACE_TEAMS_LINK} target="_blank" rel="noopener noreferrer">
                          Teams channel
                        </a>{' '}
                        or{' '}
                        <a href={Envs.CODESPACE_MATTERMOST_LINK} target="_blank" rel="noopener noreferrer">
                          Mattermost channel{' '}
                        </a>{' '}
                        to configure your <span className={classNames(Styles.warning)}>redirect url</span>.
                      </div>
                    )}
                  </div>
                )}
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
                        placeholder={`eg:${envUrl}/${props?.projectName.toLowerCase()}/${props?.isStaging ? 'int' : 'prod'}/cb`}
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
                    errorText={ignorePathError}
                    showAllTagsOnFocus={true}
                    isDeployedAppConfig={true}
                  />
                </div>
                <div className={classNames(Styles.align)}>
                  {secureWithDnaSelected && isUiRecipe ? (
                    <div>
                      <label className={classNames(Styles.clientIdLabel)}>Scope</label>
                      {enableAliceRole ? (<div>{entitlementScope}</div>) : (<div>{userInfoScope}</div>)}
                    </div>
                  ) : (
                    <Tags
                      title={'Scope'}
                      max={100}
                      chips={scope}
                      fixedChips={fixedScope}
                      tags={scopes}
                      setTags={onScopeChange}
                      isMandatory={false}
                      disableSelfTagAdd={true}
                      suggestionPopupHeight={150}
                      showAllTagsOnFocus={true}
                      isDeployedAppConfig={true}
                    />
                  )}
                </div>
              </div>
              {secureWithDnaSelected && enableAliceRole && (
                <div className={classNames(Styles.wrapper)}>
                  <span className="label">
                    <p>{`Add Roles from Alice ${Envs.CODESPACE_SECURITY_APP_ID}`}</p>
                  </span>
                  <div className={classNames(Styles.clientIdLabel, Styles.align)}>
                    {`You can create Alice roles within the DNA platform (Application ID: ${Envs.CODESPACE_SECURITY_APP_ID}
                    ) `}
                    <span className={Styles.configLink} onClick={navigateAliceRoleCreate}>
                      <a target="_blank" rel="noreferrer">
                        here
                      </a>
                    </span>
                    .
                  </div>
                  <div className={classNames(Styles.align, Styles.infoIcon)}>
                    <label className={classNames('switch', enableEntitlementPrefix ? 'on' : '')}>
                      <span className="label" style={{ marginRight: '5px' }}>
                        Enable filtering based on entitlement prefix
                      </span>
                      <span className="wrapper">
                        <input
                          value={enableEntitlementPrefix}
                          type="checkbox"
                          className="ff-only"
                          onChange={() => {
                            setEnableEntitlementPrefix(!enableEntitlementPrefix);
                          }}
                          checked={enableEntitlementPrefix}
                        />
                      </span>
                    </label>
                  </div>
                  <div className={classNames(Styles.align)}>
                    <Tags
                      title={enableEntitlementPrefix ? 'Entitlement Prefix' : 'Alice Roles'}
                      max={20}
                      chips={selectedRoles}
                      tags={enableEntitlementPrefix ? [] : existingRoles}
                      setTags={onRolesChange}
                      errorText={aliceRolesError}
                      isMandatory={enableEntitlementPrefix ? true : false}
                      disableSelfTagAdd={false}
                      suggestionPopupHeight={150}
                      showAllTagsOnFocus={true}
                      isDeployedAppConfig={true}
                    />
                  </div>
                </div>
              )}
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
          <button className={'btn btn-tertiary'} type="button" onClick={onSaveConfig}>
            Save
          </button>
        </div>
      </div>
      {securedWithIAMWarning && (
        <Modal
          title={''}
          showAcceptButton={true}
          acceptButtonTitle={'Yes'}
          cancelButtonTitle={'Cancel'}
          onAccept={() => {
            setSecuredWithIAMWarning(false);
            setSecureWithIAMSelected(false);
            setSecureWithDnaSelected(false);
          }}
          showCancelButton={true}
          modalWidth={'40%'}
          content={
            <div>
              <h3>
                Please note that once you uncheck this your application will not be secured with SSO Authentication
                anymore. Do you wish to continue?
              </h3>
              <p>
                If your application was secured by us please contact us on our{' '}
                <a href={Envs.CODESPACE_TEAMS_LINK} target="_blank" rel="noopener noreferrer">
                  Teams channel
                </a>{' '}
                or{' '}
                <a href={Envs.CODESPACE_MATTERMOST_LINK} target="_blank" rel="noopener noreferrer">
                  Mattermost channel
                </a>{' '}
                before performing this action.
              </p>
            </div>
          }
          buttonAlignment="center"
          modalStyle={{
            maxWidth: '40%',
          }}
          show={securedWithIAMWarning}
          onCancel={() => setSecuredWithIAMWarning(false)}
        />
      )}
      {showEnablePluginWarning && (
        <Modal
          title={''}
          showAcceptButton={true}
          acceptButtonTitle={'Yes'}
          cancelButtonTitle={'No'}
          onAccept={() => onPluginStatusChange(true)}
          showCancelButton={true}
          modalWidth={'40%'}
          content={
            <div>
              <h3>Do you wish to enable both SSO Authentication and Authorization?</h3>
              <p>
                Click No to only enable Authentication. You can enable the Authorization plugin using the Authorization
                config page as well.
              </p>
            </div>
          }
          buttonAlignment="center"
          modalStyle={{
            maxWidth: '40%',
          }}
          show={showEnablePluginWarning}
          onCancel={() => onPluginStatusChange()}
        />
      )}
    </React.Fragment>
  );
};
export default DeployedAppConfigModal;
