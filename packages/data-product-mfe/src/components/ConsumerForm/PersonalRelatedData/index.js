import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';
import { withRouter } from 'react-router-dom';

import { useFormContext } from 'react-hook-form';

import InfoModal from 'dna-container/InfoModal';

import { Envs } from '../../../Utility/envs';
import { useDispatch, useSelector } from 'react-redux';
import { getLegalBasis } from '../../redux/consumeDataProduct.services';

const PersonalRelatedData = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
    setValue,
    resetField,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const dispatch = useDispatch();
  const { legalBasisList } = useSelector((state) => state.consumeDataProducts);

  const isValid = (value) =>
    !watch('personalRelatedDataTransferred') ||
    watch('personalRelatedDataTransferred') === 'No' ||
    value?.length > 0 ||
    '*Missing entry';

  const isDisabled = !watch('personalRelatedDataTransferred') || watch('personalRelatedDataTransferred') === 'No';

  useEffect(() => {
    dispatch(getLegalBasis());
  }, [dispatch]);

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
                      {...register('personalRelatedDataTransferred', {
                        required: '*Missing entry',
                        onChange: () => {
                          resetField('personalRelatedDataPurpose');
                          resetField('personalRelatedDataLegalBasis');
                          resetField('LCOCheckedLegalBasis');
                          resetField('LCOComments');
                        },
                      })}
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
                isDisabled ? 'disabled' : '',
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
                  disabled: isDisabled,
                })}
                rows={50}
                id="personalRelatedDataPurpose"
              />
              <span className={classNames('error-message')}>{errors?.personalRelatedDataPurpose?.message}</span>
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
                Legal basis for processing this personal related data? <sup>*</sup>
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
            </div>
            <div
              className={classNames(
                'input-field-group include-error',
                errors?.LCOCheckedLegalBasis ? 'error' : '',
                isDisabled ? 'disabled' : '',
              )}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                LCO/LCR checked legal basis of usage of personal data <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={classNames('radio', isDisabled ? 'disabled' : '')}>
                  <span className="wrapper">
                    <input
                      {...register('LCOCheckedLegalBasis', {
                        validate: isValid,
                        disabled: isDisabled,
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOCheckedLegalBasis"
                      value="N.A"
                    />
                  </span>
                  <span className="label">N.A</span>
                </label>
                <label className={classNames('radio', isDisabled ? 'disabled' : '')}>
                  <span className="wrapper">
                    <input
                      {...register('LCOCheckedLegalBasis', {
                        validate: isValid,
                        disabled: isDisabled,
                      })}
                      type="radio"
                      className="ff-only"
                      name="LCOCheckedLegalBasis"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={classNames('radio', isDisabled ? 'disabled' : '')}>
                  <span className="wrapper">
                    <input
                      {...register('LCOCheckedLegalBasis', {
                        validate: isValid,
                        disabled: isDisabled,
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
              className={classNames(
                'input-field-group include-error area',
                errors.LCOComments ? 'error' : '',
                isDisabled ? 'disabled' : '',
              )}
            >
              <label id="LCOCommentsLabel" className="input-label" htmlFor="LCOComments">
                LCO/LCR comments to data usage of personal data <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('LCOComments', {
                  validate: isValid,
                  disabled: isDisabled,
                })}
                rows={50}
                id="LCOComments"
              />
              <span className={classNames('error-message')}>{errors?.LCOComments?.message}</span>
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div className={Styles.termsOfUseContainer}>
            <div className={Styles.termsOfUseContent}>
              <label className={classNames('checkbox', errors?.tou ? 'error' : '')}>
                <span className="wrapper">
                  <input {...register('tou', { required: '*Missing entry' })} type="checkbox" className="ff-only" />
                </span>
                <div
                  className={classNames(Styles.termsOfUseText, 'mbc-scroll')}
                  style={{
                    ...(errors?.tou ? { color: '#e84d47' } : ''),
                  }}
                  dangerouslySetInnerHTML={{
                    __html: Envs.DATA_PRODUCT_TOU_HTML,
                  }}
                ></div>
              </label>
            </div>
            <span className={classNames('error-message', Styles.errorMsg)}>{errors?.tou?.message}</span>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <div className="btn-set">
          <button
            className="btn btn-primary"
            type="button"
            onClick={handleSubmit((data) => {
              onSave(data);
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
            onClick={handleSubmit((data) => {
              setValue('publish', true);
              onSave(watch());
              reset(data, {
                keepDirty: false,
              });
            })}
          >
            Finalize Minimum Information Documentation
          </button>
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

export default withRouter(PersonalRelatedData);
