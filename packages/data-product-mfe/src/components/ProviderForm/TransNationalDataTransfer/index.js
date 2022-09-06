import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';
import DataOriginating from '../DataOriginating';

const TransNationalDataTransfer = ({ onSave }) => {
  const {
    register,
    formState: { errors },
    watch,
    setValue,
    clearErrors,
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

  const validateLCOApproved = (value) => {
    return (
      watch('transnationalDataTransfer') === null ||
      watch('transnationalDataTransfer') === 'No' ||
      watch('transnationalDataTransferNotWithinEU') === null ||
      watch('transnationalDataTransferNotWithinEU') === 'No' ||
      value?.length > 0 ||
      '*Missing entry'
    );
  };

  const isLCOApproveOptionsDisabled =
    !watch('transnationalDataTransfer') ||
    watch('transnationalDataTransfer') === 'No' ||
    watch('transnationalDataTransferNotWithinEU') === 'No' ||
    !watch('transnationalDataTransferNotWithinEU');

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Identifiying Trans-national Data Transfer</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
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
                            setValue('transnationalDataTransferNotWithinEU', '');
                            setValue('LCOApprovedDataTransfer', '');
                            clearErrors(['transnationalDataTransferNotWithinEU', 'LCOApprovedDataTransfer']);
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
                )}
                style={{ minHeight: '50px' }}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Only if yes, is one of these countries not within the EU? <sup>*</sup>
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
                            setValue('LCOApprovedDataTransfer', '');
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
            <p>
              If both yes, the corresponding LCO/LCR of the data providing side needs to check all relevant local laws
              and regulations and complete the following question
            </p>
            <div
              className={classNames(
                `input-field-group include-error ${errors?.LCOApprovedDataTransfer ? 'error' : ''}`,
              )}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Has LCO/LCR approved this data transfer? <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('LCOApprovedDataTransfer', {
                        validate: validateLCOApproved,
                        disabled: isLCOApproveOptionsDisabled,
                        setValueAs: (value) => {
                          if (watch('transnationalDataTransfer') === 'No') return undefined;
                          return value;
                        },
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOApprovedDataTransfer"
                      value="N.A"
                    />
                  </span>
                  <span className="label">N.A</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('LCOApprovedDataTransfer', {
                        validate: validateLCOApproved,
                        disabled: isLCOApproveOptionsDisabled,
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOApprovedDataTransfer"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('LCOApprovedDataTransfer', {
                        validate: validateLCOApproved,
                        disabled: isLCOApproveOptionsDisabled,
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOApprovedDataTransfer"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.LCOApprovedDataTransfer?.message}</span>
            </div>
          </div>
        </div>
      </div>
      <DataOriginating onSave={onSave} />
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
