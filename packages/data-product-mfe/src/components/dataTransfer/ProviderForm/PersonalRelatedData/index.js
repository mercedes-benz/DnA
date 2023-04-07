import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';

import InfoModal from 'dna-container/InfoModal';
import { useDispatch, useSelector } from 'react-redux';
import { getLegalBasis } from '../../../redux/getDropdowns.services';

const PersonalRelatedData = (
  // { onSave }
  ) => {
  const {
    register,
    // handleSubmit,
    formState: { errors },
    // reset,
    watch,
    clearErrors,
    setValue,
    getValues,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const dispatch = useDispatch();
  const { legalBasisList } = useSelector((state) => state.dropdowns);

  const isValid = (value) =>
    !watch('personalRelatedData') || watch('personalRelatedData') === 'No' || value?.length > 0 || '*Missing entry';
  const isDisabled = !watch('personalRelatedData') || watch('personalRelatedData') === 'No';
  const isDisabledContactAwareTransfer = !watch('personalRelatedDataContactAwareTransfer') || watch('personalRelatedDataContactAwareTransfer') === 'No';
  const isDisabledTransferingComments =  !watch('personalRelatedDataObjectionsTransfer') || watch('personalRelatedDataObjectionsTransfer') === 'No' ||  watch('personalRelatedDataContactAwareTransfer') === 'No';
  useEffect(() => {
    dispatch(getLegalBasis());
  }, [dispatch]);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Identifying personal related data</h3>
            {showInfoModal && (
              <div className={Styles.infoIcon}>
                <i className={'icon mbc-icon info'} onClick={() => {}} />
              </div>
            )}
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
                      {...register('personalRelatedData', {
                        required: '*Missing entry',
                        onChange: () => {
                          clearErrors([
                            'personalRelatedDataDescription',
                            'personalRelatedDataPurpose',
                            'personalRelatedDataLegalBasis',
                            'personalRelatedDataContactAwareTransfer',
                            'personalRelatedDataObjectionsTransfer',
                            'personalRelatedDataTransferingNonetheless',
                            'personalRelatedDataTransferingObjections',
                          ]);
                          setValue('personalRelatedDataDescription', '');
                          setValue('personalRelatedDataPurpose', '');
                          setValue('personalRelatedDataLegalBasis', '');
                          setValue('personalRelatedDataContactAwareTransfer', '');
                          setValue('personalRelatedDataObjectionsTransfer', '');
                          setValue('personalRelatedDataTransferingNonetheless', '');
                          setValue('personalRelatedDataTransferingObjections', '');
                        },
                      })}
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
              <p>In case of doubt reach out to your corresponding LCO/R</p>
            </div>
            <div
              id="personalRelatedDataDescription"
              className={classNames(
                'input-field-group include-error area',
                errors.personalRelatedDataDescription ? 'error' : '',
                isDisabled ? 'disabled' : '',
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
                {...register('personalRelatedDataDescription', { required: '*Missing entry', disabled: isDisabled })}
                rows={50}
                id="personalRelatedDataDescription"
                placeholder="Please detail (on data ﬁeld level e.g. user ID, FIN etc.) which personal related data is to be transferred."
              />
              <span className={classNames('error-message')}>{errors?.personalRelatedDataDescription?.message}</span>
            </div>
            <div
              id="personalRelatedDataPurpose"
              className={classNames(
                'input-field-group include-error area',
                errors.personalRelatedDataPurpose ? 'error' : '',
                isDisabled ? 'disabled' : '',
              )}
            >
              <label id="personalRelatedDataPurposeLabel" className="input-label" htmlFor="personalRelatedDataPurpose">
                Original (business) purpose of processing this personal related data <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('personalRelatedDataPurpose', { required: '*Missing entry', disabled: isDisabled })}
                rows={50}
                id="personalRelatedDataPurpose"
              />
              <span className={classNames('error-message')}>{errors?.personalRelatedDataPurpose?.message}</span>
              <div>
                <p>Possible purpose of processing personal related data would be:</p>
                <ul>
                  <li>FC services such as Controlling, Accounting, Treasury, Taxes & Fiscal Matters, GeneralLedger, Planning & Forecast</li>
                  <li>Product development, improvement, monitoring, Analysis of product usage/behavior,Product lifecycle analysis</li>
                  <li>Risk/damage prevention, Predictive maintenance, Process optimization</li>
                  <li>Enhancement of Customer experience, 360 Customer View, Advertising/Promotions,Direct Marketing</li>
                  <li>Identiﬁcation of trends an market developments, Market analysis, Revenue streamanalysis, Portfolio evaluation, Price analysis and price optimization</li>
                </ul>
              </div>
            </div>
            <div
              className={classNames(
                'input-field-group include-error',
                errors?.personalRelatedDataLegalBasis ? 'error' : '',
                isDisabled ? 'disabled' : '',
              )}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Original legal basis for processing this personal related data? <sup>*</sup>
              </label>
              <div className={Styles.radioBtnsGrid}>
                {legalBasisList?.map((item) => {
                  return (
                    <div key={item.id}>
                      <label className={classNames('radio', isDisabled ? 'disabled' : '')}>
                        <span className="wrapper">
                          <input
                            {...register('personalRelatedDataLegalBasis', {
                              validate: isValid,
                              disabled: isDisabled,
                            })}
                            type="radio"
                            className="ff-only"
                            name="personalRelatedDataLegalBasis"
                            value={item.name}
                          />
                        </span>
                        <span className="label">{item.name}</span>
                      </label>
                    </div>
                  );
                })}
              </div>
              <span className={classNames('error-message')}>{errors?.personalRelatedDataLegalBasis?.message}</span>
              <p>Legal basis is already determined in RoPA, if unsure please check with your LCO/R</p>
            </div>
            <div
              id="personalRelatedDataContactAwareTransfer"
              className={classNames(
                'input-field-group include-error',
                errors.personalRelatedDataContactAwareTransfer ? 'error' : '',
                isDisabled ? 'disabled' : '',
              )}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Is corresponding Compliance contact aware of this transfer? <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataContactAwareTransfer', {
                        required: '*Missing entry',
                        disabled: isDisabled,
                        onChange: () => {
                          clearErrors([
                            'personalRelatedDataObjectionsTransfer',
                            'personalRelatedDataTransferingNonetheless',
                            'personalRelatedDataTransferingObjections',
                          ]);
                          setValue('personalRelatedDataObjectionsTransfer', '');
                          setValue('personalRelatedDataTransferingNonetheless', '');
                          setValue('personalRelatedDataTransferingObjections', '');
                        },
                      })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataContactAwareTransfer"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataContactAwareTransfer', {
                        required: '*Missing entry',
                        disabled: isDisabled,
                      })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataContactAwareTransfer"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.personalRelatedDataContactAwareTransfer?.message}</span>
            </div>
            <div
              id="personalRelatedDataObjectionsTransfer"
              className={classNames(
                'input-field-group include-error',
                errors.personalRelatedDataObjectionsTransfer ? 'error' : '',
                isDisabledContactAwareTransfer ? 'disabled' : '',
              )}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Has s/he any objections to this transfer?{' '}
                {getValues('personalRelatedDataContactAwareTransfer') === 'Yes' ? <sup>*</sup> : null}
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataObjectionsTransfer', {
                        required: '*Missing entry',
                        disabled: isDisabledContactAwareTransfer,
                        onChange: () => {
                          clearErrors([
                            'personalRelatedDataTransferingNonetheless',
                            'personalRelatedDataTransferingObjections',
                          ]);
                          setValue('personalRelatedDataTransferingNonetheless', '');
                          setValue('personalRelatedDataTransferingObjections', '');
                        },
                      })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataObjectionsTransfer"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('personalRelatedDataObjectionsTransfer', {
                        required: '*Missing entry',
                        disabled: isDisabledContactAwareTransfer,
                      })}
                      type="radio"
                      className="ff-only"
                      name="personalRelatedDataObjectionsTransfer"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.personalRelatedDataObjectionsTransfer?.message}</span>
            </div>
            <div
              id="personalRelatedDataTransferingNonetheless"
              className={classNames(
                'input-field-group include-error area',
                errors.personalRelatedDataTransferingNonetheless ? 'error' : '',
                isDisabledTransferingComments ? 'disabled' : '',
              )}
            >
              <label
                id="personalRelatedDataTransferingNonethelessLabel"
                className="input-label"
                htmlFor="personalRelatedDataTransferingNonetheless"
              >
                Please state your reasoning for transfering nonetheless{' '}
                {getValues('personalRelatedDataObjectionsTransfer') === 'Yes' ? <sup>*</sup> : null}
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('personalRelatedDataTransferingNonetheless', { required: '*Missing entry', disabled: isDisabledTransferingComments })}
                rows={50}
                id="personalRelatedDataTransferingNonetheless"
              />
              <span className={classNames('error-message')}>{errors?.personalRelatedDataTransferingNonetheless?.message}</span>
            </div>
            <div
              id="personalRelatedDataTransferingObjections"
              className={classNames(
                'input-field-group include-error area',
                errors.personalRelatedDataTransferingObjections ? 'error' : '',
                isDisabledTransferingComments ? 'disabled' : '',
              )}
            >
              <label
                id="personalRelatedDataTransferingObjectionsLabel"
                className="input-label"
                htmlFor="personalRelatedDataTransferingObjections"
              >
                Please state your objections{' '}
                {getValues('personalRelatedDataObjectionsTransfer') === 'Yes' ? <sup>*</sup> : null}
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('personalRelatedDataTransferingObjections', { required: '*Missing entry', disabled: isDisabledTransferingComments })}
                rows={50}
                id="personalRelatedDataTransferingObjections"
              />
              <span className={classNames('error-message')}>{errors?.personalRelatedDataTransferingObjections?.message}</span>
            </div>
          </div>
        </div>
      </div>
      {/* <div className="btnContainer">
        <button
          className="btn btn-primary"
          type="submit"
          onClick={handleSubmit((data) => {
            const isPublished = watch('publish');
            setValue('notifyUsers', isPublished ? true : false);
            onSave(watch());
            reset(data, {
              keepDirty: false,
            });
          })}
        >
          Save & Next
        </button>
      </div> */}
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
