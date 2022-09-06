import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';

import InfoModal from 'dna-container/InfoModal';
import { useDispatch, useSelector } from 'react-redux';
import { getLegalBasis } from '../../redux/consumeDataProduct.services';

const PersonalRelatedData = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
    clearErrors,
    setValue,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const dispatch = useDispatch();
  const { legalBasisList } = useSelector((state) => state.consumeDataProducts);

  const isValid = (value) =>
    !watch('personalRelatedData') || watch('personalRelatedData') === 'No' || value?.length > 0 || '*Missing entry';
  const isDisabled = !watch('personalRelatedData') || watch('personalRelatedData') === 'No';

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
                          ]);
                          setValue('personalRelatedDataDescription', '');
                          setValue('personalRelatedDataPurpose', '');
                          setValue('personalRelatedDataLegalBasis', '');
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
            </div>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <button
          className="btn btn-primary"
          type="submit"
          onClick={handleSubmit((data) => {
            onSave(data);
            reset(data, {
              keepDirty: false,
            });
          })}
        >
          Save & Next
        </button>
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
