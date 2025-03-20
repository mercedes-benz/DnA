import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../DeployApprovalModal/DeployApprovalModal.scss';
import Modal from 'dna-container/Modal';
import TextBox from 'dna-container/TextBox';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { trackEvent } from '../../Utility/utils';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const DeployApprovalModal = (props) => {
  const codeSpace = props.codeSpaceData;
  const deploymentDetails = codeSpace?.projectDetails?.prodDeploymentDetails;
  const auditLogs = [...(deploymentDetails?.deploymentAuditLogs || [])].reverse();
  const scope = deploymentDetails?.scope?.split(' ') || ['openid', 'offline_access'];
  const ignorePaths = deploymentDetails?.ignorePaths?.length ? deploymentDetails?.ignorePaths?.split(',') : ['N/A'];
  const [showCredentialsModal, setShowCredentialsModal] = useState(false);
  const [clientSecret, setClientSecret] = useState('');

  const deployWorkspace = () => {
    const deployRequest = {
      secureWithIAMRequired: deploymentDetails?.secureWithIAMRequired,
      // technicalUserDetailsForIAMLogin: secureWithIAMSelected ? iamTechnicalUserID : null,
      targetEnvironment: 'prod', // int or prod
      branch: auditLogs?.[0]?.branch,
      // valutInjectorEnable: vaultEnabled,
      clientID: deploymentDetails?.secureWithIAMRequired ? deploymentDetails?.clientId : '',
      clientSecret: clientSecret,
      redirectUri:
        deploymentDetails?.secureWithIAMRequired && deploymentDetails?.redirectUri?.length
          ? deploymentDetails?.redirectUri
          : '',
      ignorePaths:
        deploymentDetails?.secureWithIAMRequired && deploymentDetails?.ignorePaths?.length
          ? deploymentDetails?.ignorePaths
          : '',
      scope: deploymentDetails?.secureWithIAMRequired ? deploymentDetails?.scope : '',
      isApiRecipe: deploymentDetails?.deploymentType === 'API',
      oneApiVersionShortName: deploymentDetails?.oneApiVersionShortName?.length
        ? deploymentDetails?.oneApiVersionShortName
        : '',
      isSecuredWithCookie: deploymentDetails?.cookieSelected || false,
    };
    ProgressIndicator.show();
    CodeSpaceApiClient.deployCodeSpace(props.codeSpaceData.id, deployRequest)
      .then((res) => {
        trackEvent('DnA Code Space', 'Deploy', 'Deploy code space');
        if (res.data.success === 'SUCCESS') {
          // setCreatedCodeSpaceName(res.data.name);
          props.setCodeDeploying(true);
          props.setIsApiCallTakeTime(true);
          props.startDeployLivelinessCheck &&
            props.startDeployLivelinessCheck(props.codeSpaceData.workspaceId, 'production');
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
    props.setShowDeployApprovalModal(false);
  };

  const handleApproveClick = () => {
    (deploymentDetails?.secureWithIAMRequired && deploymentDetails?.clientId?.length) ? setShowCredentialsModal(true) : deployWorkspace();
  };

  const handleFinalApproveClick = () => {
    deployWorkspace();
    setShowCredentialsModal(false);
  };

  return (
    <>
      <Modal
        title={'Deployment Approval'}
        hiddenTitle={false}
        modalWidth={'65%'}
        modalStyle={{ minHeight: '50%' }}
        show={props.show}
        content={
          <>
            <div className={classNames(Styles.deployApprovalModal)}>
              <div className={classNames(Styles.allcodeSpaceListviewContent)}>
                <div className={classNames(Styles.threeColumnFlexLayout)}>
                  <div>
                    <label className={classNames(Styles.label)}>Branch</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>{auditLogs?.[0]?.branch || 'N/A'}</label>
                    </div>
                  </div>
                  <div>
                    <label className={classNames(Styles.label)}>Triggered By</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>
                        {auditLogs?.[0]?.triggeredBy || 'N/A'}
                      </label>
                    </div>
                  </div>
                  <div>
                    <label className={classNames(Styles.label)}>Triggered On</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>
                        {regionalDateAndTimeConversionSolution(auditLogs?.[0]?.triggeredOn) || 'N/A'}
                      </label>
                    </div>
                  </div>
                </div>
                <div className={classNames(Styles.threeColumnFlexLayout)}>
                  <div>
                    <label className={classNames(Styles.label)}>Deployment Type</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>
                        {deploymentDetails?.deploymentType || 'API'}
                      </label>
                    </div>
                  </div>
                  <div>
                    <label className={classNames(Styles.label)}>secured with IAM</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>
                        {deploymentDetails?.secureWithIAMRequired
                          ? deploymentDetails?.isSecuredWithCookie
                            ? 'Cookie based authentication'
                            : 'OIDC based authentication(default)'
                          : 'No'}
                      </label>
                    </div>
                  </div>
                  <div>
                    <label className={classNames(Styles.label)}>Provisioned through oneAPI</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>
                        {deploymentDetails?.oneApiVersionShortName?.length ? 'Yes' : 'No'}
                      </label>
                    </div>
                  </div>
                </div>

                {deploymentDetails?.secureWithIAMRequired && (
                  <div>
                    {' '}
                    <div className={classNames(Styles.threeColumnFlexLayout)}>
                      <div>
                        <label className={classNames(Styles.label)}>Client ID</label>
                        <div>
                          <label className={classNames('chips')}>{deploymentDetails?.clientId || 'N/A'}</label>
                        </div>
                      </div>
                      <div>
                        <label className={classNames(Styles.label)}>Redirect URI</label>
                        <div>
                          <label className={classNames('chips')}>{deploymentDetails?.redirectUri || 'N/A'}</label>
                        </div>
                      </div>
                      <div>
                        <label className={classNames(Styles.label)}>Ignore Paths</label>
                        <div>
                          {ignorePaths.map((ignorePath) => (
                            <label className={classNames('chips')} key={ignorePath}>
                              {ignorePath}&nbsp;
                            </label>
                          ))}
                        </div>
                      </div>
                    </div>
                    <div>
                      <label className={classNames(Styles.label)}>Scope</label>
                      <div>
                        {scope.map((scopeItem) => (
                          <label className={classNames('chips', Styles.Chips)} key={scopeItem}>
                            {scopeItem}{' '}
                          </label>
                        ))}
                      </div>
                    </div>
                  </div>
                )}
                {deploymentDetails?.oneApiVersionShortName?.length !== 0 && (
                  <div className={classNames(Styles.flexLayout)}>
                    <div>
                      <label className={classNames(Styles.label)}>Api version shortname</label>
                      <div>
                        <label className={classNames('chips')}>
                          {deploymentDetails?.oneApiVersionShortName || 'N/A'}
                        </label>
                      </div>
                    </div>
                    <div></div>
                  </div>
                )}
              </div>
              <div className={Styles.approveBtnWrapper}>
                <button className={'btn btn-tertiary'} onClick={handleApproveClick}>
                  Approve
                </button>
              </div>
            </div>
          </>
        }
        scrollableContent={true}
        onCancel={() => props.setShowDeployApprovalModal(false)}
      />

      {showCredentialsModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          modalWidth={'60%'}
          modalStyle={{
            maxWidth: '60%',
          }}
          buttonAlignment="right"
          show={showCredentialsModal}
          content={
            <div className={classNames(Styles.newCredentialsModalContent)}>
              <label className={classNames(Styles.label)}>
                Please enter the client secret associated with the following client id if your your IAM details have
                changed or this is the first time you are securing with IAM. If not you can proceed with the approval
                without entering your client secret.
              </label>
              <div className={classNames(Styles.flexLayout)}>
                <div className={classNames(Styles.clientIdWrapper)}>
                  <label className={classNames(Styles.label)}>Client ID</label>
                  <div>
                    <label className={classNames('chips', Styles.Chips)}>{deploymentDetails?.clientId || 'N/A'}</label>
                  </div>
                </div>
                <div className={classNames(Styles.textboxWrapper)}>
                  <TextBox
                    type="text"
                    controlId={'Client Secret'}
                    labelId={'clientSecretLabel'}
                    label={'Client Secret'}
                    value={clientSecret}
                    placeholder={'Type here'}
                    required={false}
                    maxLength={100}
                    onChange={(e) => {
                      setClientSecret(e.currentTarget.value);
                    }}
                  />
                </div>
              </div>

              <div className={classNames(Styles.approveBtnWrapper)}>
                <button className="btn btn-tertiary" onClick={handleFinalApproveClick}>
                  Approve
                </button>
              </div>
            </div>
          }
          scrollableContent={false}
          onCancel={() => {
            setShowCredentialsModal(false);
          }}
        />
      )}
    </>
  );
};

export default DeployApprovalModal;
