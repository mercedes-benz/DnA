import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';

import InfoModal from 'dna-container/InfoModal';
import ConfirmModal from 'dna-container/ConfirmModal';

const OtherRelevantInfo = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);
  const [showPublishModal, setPublishModal] = useState(false);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Specifying other relevant information</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div
              id="personalRelatedDataDescription"
              className={classNames('input-field-group include-error area', errors.otherRelevantInfo ? 'error' : '')}
            >
              <label className="input-label" htmlFor="otherRelevantInfo">
                Please provide any other relevant & app specific restrictions that might apply to the corresponding
                data, examples being individual deletion requirements, antitrust regulations, contractual restrictions
                etc. <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('otherRelevantInfo', {
                  required: '*Missing entry',
                })}
                rows={50}
                id="otherRelevantInfo"
              />
              <span className={classNames('error-message')}>{errors?.otherRelevantInfo?.message}</span>
            </div>
          </div>
          <div className="btnContainer">
            <div className="btn-set">
              <button
                className="btn btn-primary"
                type="button"
                onClick={handleSubmit((data) => {
                  onSave();
                  reset(data, {
                    keepDirty: false,
                  });
                })}
              >
                Save
              </button>
              <button
                className={'btn btn-tertiary'}
                type="button"
                onClick={handleSubmit(() => {
                  setPublishModal(true);
                })}
              >
                Publish
              </button>
            </div>
          </div>
        </div>
      </div>
      {showInfoModal && (
        <InfoModal
          title="Info Modal"
          show={showInfoModal}
          hiddenTitle={true}
          content={<div>Sample Info Modal</div>}
          onCancel={() => setShowInfoModal(false)}
        />
      )}
      <ConfirmModal
        title={''}
        acceptButtonTitle="Confirm Publish"
        cancelButtonTitle="Cancel"
        showAcceptButton={true}
        showCancelButton={true}
        show={showPublishModal}
        content={
          <div>
            <h3>Are you sure , you want to publish?</h3>
          </div>
        }
        buttonAlignment="right"
        onCancel={() => setPublishModal(false)}
        onAccept={() => {
          setPublishModal(false);
          console.log('Published data', watch());
        }}
      />
    </>
  );
};

export default OtherRelevantInfo;
