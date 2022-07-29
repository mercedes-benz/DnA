import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../common.styles.scss';
import { withRouter } from 'react-router-dom';

import { useFormContext } from 'react-hook-form';

import InfoModal from 'dna-container/InfoModal';
import ConfirmModal from 'dna-container/ConfirmModal';

import { Envs } from '../../../Utility/envs';

const PersonalRelatedData = ({ onSave, history }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
    setValue,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);
  const [showPublishModal, setPublishModal] = useState(false);
  const [touChecked, setTOUChecked] = useState(false);

  const isValid = (value) =>
    !watch('personalRelatedDataTransferred') ||
    watch('personalRelatedDataTransferred') === 'No' ||
    value?.length > 0 ||
    '*Missing entry';

  const publishContent = (
    <div>
      <h3>Publish Data Product</h3>
      <div className={Styles.termsOfUseContainer}>
        <div className={Styles.termsOfUseContent}>
          <label className="checkbox">
            <span className="wrapper">
              <input
                value={touChecked}
                type="checkbox"
                className="ff-only"
                onChange={() => setTOUChecked(!touChecked)}
                defaultChecked={false}
              />
            </span>
            <div
              className={classNames(Styles.termsOfUseText, 'mbc-scroll')}
              dangerouslySetInnerHTML={{
                __html: Envs.DATA_PRODUCT_TOU_HTML,
              }}
            ></div>
          </label>
        </div>
      </div>
    </div>
  );

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
              className={classNames(
                `input-field-group include-error ${errors?.personalRelatedDataTransferred ? 'error' : ''}`,
              )}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Is personal related data transferred and actually processed at application level? <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataTransferred', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataTransferred"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataTransferred', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataTransferred"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.personalRelatedDataTransferred?.message}</span>
            </div>
            <div
              id="personalRelatedDataPurpose"
              className={classNames(
                'input-field-group include-error area',
                errors.personalRelatedDataPurpose ? 'error' : '',
              )}
            >
              <label id="personalRelatedDataPurposeLabel" className="input-label" htmlFor="personalRelatedDataPurpose">
                (Business) purpose of processing this personal related data <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('personalRelatedDataPurpose', {
                  validate: isValid,
                })}
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
                Legal basis for processing this personal related data? <sup>*</sup>
              </label>
              <div className={Styles.radioButtons}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataLegalBasis', {
                        validate: isValid,
                      })}
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
                      {...register('personalRelatedDataLegalBasis', {
                        validate: isValid,
                      })}
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
                      {...register('personalRelatedDataLegalBasis', {
                        validate: isValid,
                      })}
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
                      {...register('personalRelatedDataLegalBasis', {
                        validate: isValid,
                      })}
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
                      {...register('personalRelatedDataLegalBasis', {
                        validate: isValid,
                      })}
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
                      {...register('personalRelatedDataLegalBasis', {
                        validate: isValid,
                      })}
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
            <div
              className={classNames(`input-field-group include-error ${errors?.LCOCheckedLegalBasis ? 'error' : ''}`)}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                LCO/LCR checked legal basis of usage of personal data <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('LCOCheckedLegalBasis', {
                        validate: isValid,
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOCheckedLegalBasis"
                      value="N.A"
                    />
                  </span>
                  <span className="label">N.A</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('LCOCheckedLegalBasis', {
                        validate: isValid,
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOCheckedLegalBasis"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('LCOCheckedLegalBasis', {
                        validate: isValid,
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOCheckedLegalBasis"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.LCOCheckedLegalBasis?.message}</span>
            </div>
            <div
              id="LCOComments"
              className={classNames('input-field-group include-error area', errors.LCOComments ? 'error' : '')}
            >
              <label id="LCOCommentsLabel" className="input-label" htmlFor="LCOComments">
                LCO/LCR comments to data usage of personal data <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('LCOComments', {
                  validate: isValid,
                })}
                rows={50}
                id="LCOComments"
              />
              <span className={classNames('error-message')}>{errors?.LCOComments?.message}</span>
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
                className="btn btn-tertiary"
                type="button"
                onClick={handleSubmit(() => {
                  setPublishModal(true);
                })}
              >
                Consume Data Product
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
        acceptButtonTitle="Publish"
        showAcceptButton={true}
        showCancelButton={true}
        show={showPublishModal}
        content={publishContent}
        onAccept={() => {
          setPublishModal(false);
          setValue('tou', touChecked);
          console.log('Published data', watch());
          // dispatch(SetDataProducts(watch()));
          history.push('/');
        }}
        onCancel={() => setPublishModal(false)}
        acceptButtonDisabled={!touChecked}
      />
    </>
  );
};

export default withRouter(PersonalRelatedData);
