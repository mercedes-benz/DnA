import classNames from 'classnames';
import React, { useState } from 'react'; 
import Styles from '../DeployApprovalModal/DeployApprovalModal.scss';
import Modal from 'dna-container/Modal';
import TextBox from 'dna-container/TextBox';

const DeployApprovalModal = (props) => {
  const [showCredentialsModal, setShowCredentialsModal] = useState(false);

  const [clientSecret, setClientSecret] = useState('');
  const [clientSecretError, setClientSecretError] = useState('');

  const handleApproveClick = () => {
    console.log("Approve button clicked");
    setShowCredentialsModal(true); 
  };

  const handleCloseCredentialsModal = () => {
    setShowCredentialsModal(false); 
  };

  const handleFinalApproveClick = () => {
    console.log("Final Approve button clicked");
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
            <div className={classNames(Styles.allCodeSpace)}>
              <div className={classNames(Styles.allcodeSpaceListviewContent)}>
                {/* First row (4 items) */}
                <div className={classNames(Styles.flexLayout)}>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Branch</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>main</label>
                    </div>
                  </div>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Triggered By</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>N/A</label>
                    </div>
                  </div>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Triggered On</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>27/02/25</label>
                    </div>
                  </div>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Deployment Type</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>DEPLOYED</label>
                    </div>
                  </div>
                </div>

                {/* Second row (next 4 items) */}
                <div className={classNames(Styles.flexLayout)}>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Client ID</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>270223010203</label>
                    </div>
                  </div>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Redirect URL</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>NA</label>
                    </div>
                  </div>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Ignore Paths</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>NA</label>
                    </div>
                  </div>
                  <div className={classNames(Styles.itemWrapper)}>
                    <label className={classNames(Styles.label)}>Scope Options</label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>NA</label>
                    </div>
                  </div>
                </div>
              </div>
              <div className={Styles.approveBtnWrapper}>
                <button
                  className={'btn btn-tertiary'} 
                  onClick={handleApproveClick} 
                >
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
          title={'Please enter the client secret associated with the client ID'}
          hiddenTitle={false}
          modalWidth={'30%'} 
          modalStyle={{ minHeight: '30%' }}  
          show={showCredentialsModal}
          content={
            <div className={classNames(Styles.newCredentialsModalContent)}>
              <div className={classNames(Styles.flexLayout)}>

                  <div className={classNames(Styles.clientIdWrapper)}>
                    <label className={classNames(Styles.label)}>Client ID <sup>*</sup></label>
                    <div>
                      <label className={classNames('chips', Styles.Chips)}>
                        270223010203
                      </label>
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
                    errorText={clientSecretError}
                    required={true}
                    maxLength={100}
                    onChange={(e) => {
                      setClientSecret(e.currentTarget.value);
                      setClientSecretError('');
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
          scrollableContent={true}
          onCancel={handleCloseCredentialsModal}
        />
      )}
    </>
  );
};

export default DeployApprovalModal;
