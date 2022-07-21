import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';

import InfoModal from 'dna-container/InfoModal';

const PersonalRelatedData = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Identifying personal related data</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div
              className={classNames(`input-field-group include-error ${errors?.personalRelatedData ? 'error' : ''}`)}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Is data personal related? <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedData', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedData"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedData', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedData"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.personalRelatedData?.message}</span>
            </div>
            <div
              id="personalRelatedDataDescription"
              className={classNames(
                'input-field-group include-error area',
                errors.personalRelatedDataDescription ? 'error' : '',
              )}
            >
              <label
                id="personalRelatedDataDescriptionLabel"
                className="input-label"
                htmlFor="personalRelatedDataDescription"
              >
                Description of personal related data <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('personalRelatedDataDescription', { required: '*Missing entry' })}
                rows={50}
                id="personalRelatedDataDescription"
              />
              <span className={classNames('error-message')}>{errors?.personalRelatedDataDescription?.message}</span>
            </div>
            <div
              id="personalRelatedDataPurpose"
              className={classNames(
                'input-field-group include-error area',
                errors.personalRelatedDataPurpose ? 'error' : '',
              )}
            >
              <label id="personalRelatedDataPurposeLabel" className="input-label" htmlFor="personalRelatedDataPurpose">
                Original (business) purpose of processing this personal related data <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('personalRelatedDataPurpose', { required: '*Missing entry' })}
                rows={50}
                id="personalRelatedDataPurpose"
              />
              <span className={classNames('error-message')}>{errors?.personalRelatedDataPurpose?.message}</span>
            </div>
            <div
              className={classNames(
                `input-field-group include-error ${errors?.personalRelatedDataLegalBasis ? 'error' : ''}`,
              )}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Original legal basis for processing this personal related data? <sup>*</sup>
              </label>
              <div className={Styles.radioButtons}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataLegalBasis', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataLegalBasis"
                      value="Consent"
                    />
                  </span>
                  <span className="label">Consent</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataLegalBasis', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataLegalBasis"
                      value="Vital interest"
                    />
                  </span>
                  <span className="label">Vital interest</span>
                </label>
              </div>
              <div className={Styles.radioButtons}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataLegalBasis', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataLegalBasis"
                      value="Contract fulfillment"
                    />
                  </span>
                  <span className="label">Contract fulfillment</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataLegalBasis', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataLegalBasis"
                      value="Public interests"
                    />
                  </span>
                  <span className="label">Public interests</span>
                </label>
              </div>
              <div className={Styles.radioButtons}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataLegalBasis', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataLegalBasis"
                      value="Legal obligation"
                    />
                  </span>
                  <span className="label">Legal obligation</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataLegalBasis', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataLegalBasis"
                      value="Legitimate interests"
                    />
                  </span>
                  <span className="label">Legitimate interests</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.personalRelatedDataLegalBasis?.message}</span>
            </div>
          </div>
          <div className="btnContainer">
            <button
              className="btn btn-primary"
              type="submit"
              onClick={handleSubmit((data) => {
                onSave();
                reset(data, {
                  keepDirty: false,
                });
              })}
            >
              Save & Next
            </button>
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
    </>
  );
};

export default PersonalRelatedData;
