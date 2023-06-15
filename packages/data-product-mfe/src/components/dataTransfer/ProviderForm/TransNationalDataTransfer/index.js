import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';

const TransNationalDataTransfer = (
  // { onSave }
  ) => {
  const {
    register,
    formState: { errors, 
      // isSubmitting 
    },
    watch,
    setValue,
    clearErrors,
    // handleSubmit,
    // reset,
    getValues,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const validateNotWithinEU = (value) => {
    return (
      !watch('transnationalDataTransfer') ||
      watch('transnationalDataTransfer') === 'No' ||
      watch('transnationalDataTransferNotWithinEU') === 'null' ||
      value?.length > 0 ||
      '*Missing entry'
    );
  };


  // const isLCOApproveOptionsDisabled =
  //   !watch('transnationalDataTransfer') ||
  //   watch('transnationalDataTransfer') === 'No' ||
  //   watch('transnationalDataTransferNotWithinEU') === 'No' ||
  //   !watch('transnationalDataTransferNotWithinEU');

  // const isDisabledContactAwareTransfer = isLCOApproveOptionsDisabled
  //   || !watch('transnationalDataContactAwareTransfer') ||
  //   watch('transnationalDataContactAwareTransfer') === 'No';

  // const isDisabledTransferingComments = isDisabledContactAwareTransfer || 
  // !watch('transnationalDataObjectionsTransfer') ||
  //   watch('transnationalDataObjectionsTransfer') === 'No';

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Identifiying Transnational Data Transfer</h3>
            {showInfoModal && (
              <div className={Styles.infoIcon}>
                <i className={'icon mbc-icon info'} onClick={() => {}} />
              </div>
            )}
          </div>
          <div className={Styles.formWrapper}>
            <div className={Styles.flexLayout}>
              <div
                className={classNames(
                  `input-field-group include-error ${errors?.transnationalDataTransfer ? 'error' : ''}`,
                )}
                style={{ minHeight: '50px' }}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Is data being transferred from one country to another? <sup>*</sup>
                </label>
                <div className={Styles.radioBtns}>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('transnationalDataTransfer', {
                          required: '*Missing entry',
                          onChange: () => {
                            clearErrors([
                              'transnationalDataTransferNotWithinEU',
                              'transnationalDataContactAwareTransfer',
                              'transnationalDataObjectionsTransfer',
                              'transnationalDataTransferingNonetheless',
                              'transnationalDataTransferingObjections',
                            ]);
                            setValue('transnationalDataTransferNotWithinEU', '');
                            setValue('transnationalDataContactAwareTransfer', '');
                            setValue('transnationalDataObjectionsTransfer', '');
                            setValue('transnationalDataTransferingNonetheless', '');
                            setValue('transnationalDataTransferingObjections', '');
                          },
                        })}
                        type="radio"
                        className="ff-only"
                        name="transnationalDataTransfer"
                        value="No"
                      />
                    </span>
                    <span className="label">No</span>
                  </label>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('transnationalDataTransfer')}
                        type="radio"
                        className="ff-only"
                        name="transnationalDataTransfer"
                        value="Yes"
                      />
                    </span>
                    <span className="label">Yes</span>
                  </label>
                </div>
                <span className={classNames('error-message')}>{errors?.transnationalDataTransfer?.message}</span>
              </div>
              <div
                className={classNames(
                  `input-field-group include-error ${errors?.transnationalDataTransferNotWithinEU ? 'error' : ''}`,
                  !watch('transnationalDataTransfer')|| watch('transnationalDataTransfer') === 'No' ? 'disabled' : '',
                )}
                style={{ minHeight: '50px' }}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Only if yes, is one of these countries outside the EU?{' '}
                  {getValues('transnationalDataTransfer') === 'Yes' ? <sup>*</sup> : null}
                </label>
                <div className={Styles.radioBtns}>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('transnationalDataTransferNotWithinEU', {
                          validate: validateNotWithinEU,
                          disabled:
                            watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No',
                          setValueAs: (value) => {
                            if (watch('transnationalDataTransfer') === 'No') return undefined;
                            return value;
                          },
                          onChange: () => {
                            clearErrors([
                              'transnationalDataContactAwareTransfer',
                              'transnationalDataObjectionsTransfer',
                              'transnationalDataTransferingNonetheless',
                              'transnationalDataTransferingObjections',
                            ]);
                            setValue('transnationalDataContactAwareTransfer', '');
                            setValue('transnationalDataObjectionsTransfer', '');
                            setValue('transnationalDataTransferingNonetheless', '');
                            setValue('transnationalDataTransferingObjections', '');
                          },
                        })}
                        type="radio"
                        className="ff-only"
                        name="transnationalDataTransferNotWithinEU"
                        value="No"
                      />
                    </span>
                    <span className="label">No</span>
                  </label>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('transnationalDataTransferNotWithinEU', {
                          validate: validateNotWithinEU,
                          disabled:
                            watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No',
                        })}
                        type="radio"
                        className="ff-only"
                        name="transnationalDataTransferNotWithinEU"
                        value="Yes"
                      />
                    </span>
                    <span className="label">Yes</span>
                  </label>
                </div>
                <span className={classNames('error-message')}>
                  {errors?.transnationalDataTransferNotWithinEU?.message}
                </span>
              </div>
            </div>
            <div
              id="transnationalDataContactAwareTransfer"
              className={classNames(
                'input-field-group include-error',
                errors.transnationalDataContactAwareTransfer ? 'error' : '',
                !watch('transnationalDataTransfer')|| watch('transnationalDataTransfer') === 'No' ? 'disabled' : '',
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
                      {...register('transnationalDataContactAwareTransfer', {
                        required: '*Missing entry',
                        disabled: watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No',
                        onChange: () => {
                          clearErrors([
                            'transnationalDataObjectionsTransfer',
                            'transnationalDataTransferingNonetheless',
                            'transnationalDataTransferingObjections',
                          ]);
                          setValue('transnationalDataObjectionsTransfer', '');
                          setValue('transnationalDataTransferingNonetheless', '');
                          setValue('transnationalDataTransferingObjections', '');
                        },
                      })}
                      type="radio"
                      className="ff-only"
                      name="transnationalDataContactAwareTransfer"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('transnationalDataContactAwareTransfer', {
                        required: '*Missing entry',
                        disabled: watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No',
                      })}
                      type="radio"
                      className="ff-only"
                      name="transnationalDataContactAwareTransfer"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.transnationalDataContactAwareTransfer?.message}</span>
            </div>
            <div
              id="transnationalDataObjectionsTransfer"
              className={classNames(
                'input-field-group include-error',
                errors.transnationalDataObjectionsTransfer ? 'error' : '',
                !watch('transnationalDataTransfer')|| watch('transnationalDataTransfer') === 'No' ? 'disabled' : '',
              )}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Has s/he any objections to this transfer?{' '}
                {getValues('transnationalDataContactAwareTransfer') === 'Yes' ? <sup>*</sup> : null}
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('transnationalDataObjectionsTransfer', {
                        required: '*Missing entry',
                        disabled: watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No',
                        onChange: () => {
                          clearErrors([
                            'transnationalDataTransferingNonetheless',
                            'transnationalDataTransferingObjections',
                          ]);
                          setValue('transnationalDataTransferingNonetheless', '');
                          setValue('transnationalDataTransferingObjections', '');
                        },
                      })}
                      type="radio"
                      className="ff-only"
                      name="transnationalDataObjectionsTransfer"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('transnationalDataObjectionsTransfer', {
                        required: '*Missing entry',
                        disabled: watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No',
                      })}
                      type="radio"
                      className="ff-only"
                      name="transnationalDataObjectionsTransfer"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.transnationalDataObjectionsTransfer?.message}</span>
            </div>
            <div
              id="transnationalDataTransferingNonetheless"
              className={classNames(
                'input-field-group include-error area',
                errors.transnationalDataTransferingNonetheless ? 'error' : '',
                !watch('transnationalDataTransfer')|| watch('transnationalDataTransfer') === 'No' ? 'disabled' : '',
              )}
            >
              <label
                id="transnationalDataTransferingNonethelessLabel"
                className="input-label"
                htmlFor="transnationalDataTransferingNonetheless"
              >
                Please state your reasoning for transfering nonetheless{' '}
                {getValues('transnationalDataObjectionsTransfer') === 'Yes' ? <sup>*</sup> : null}
              </label>
              <textarea
                className="input-field-area"
                type="text"
                placeholder="Please state your reasoning for transfering nonetheless."
                {...register('transnationalDataTransferingNonetheless', { required: '*Missing entry', disabled: watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No' })}
                rows={50}
                id="transnationalDataTransferingNonetheless"
              />
              <span className={classNames('error-message')}>{errors?.transnationalDataTransferingNonetheless?.message}</span>
            </div>
            <div
              id="transnationalDataTransferingObjections"
              className={classNames(
                'input-field-group include-error area',
                errors.transnationalDataTransferingObjections ? 'error' : '',
                !watch('transnationalDataTransfer')|| watch('transnationalDataTransfer') === 'No' ? 'disabled' : '',
              )}
            >
              <label
                id="transnationalDataTransferingObjectionsLabel"
                className="input-label"
                htmlFor="transnationalDataTransferingObjections"
              >
                Please state your objections{' '}
                {getValues('transnationalDataObjectionsTransfer') === 'Yes' ? <sup>*</sup> : null}
              </label>
              <textarea
                className="input-field-area"
                type="text"
                placeholder="Please state your objections."
                {...register('transnationalDataTransferingObjections', { required: '*Missing entry', disabled: watch('transnationalDataTransfer') === '' || watch('transnationalDataTransfer') === 'No' })}
                rows={50}
                id="transnationalDataTransferingObjections"
              />
              <span className={classNames('error-message')}>{errors?.transnationalDataTransferingObjections?.message}</span>
            </div>
          </div>
        </div>
      </div>
      {/* <div className="btnContainer">
        <button
          className="btn btn-primary"
          type="submit"
          disabled={isSubmitting}
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

export default TransNationalDataTransfer;
