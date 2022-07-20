import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';

const DeletionRequirements = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    resetField,
    reset,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const validateTextArea = (v) =>
    watch('deletionRequirement') == 'No' || watch('deletionRequirement') === null || v?.length > 0 || '*Missing entry';

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3> Specify deletion requirements</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div
              className={classNames(`input-field-group include-error ${errors?.deletionRequirement ? 'error' : ''}`)}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Are there specific deletion requirements for this data? <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('deletionRequirement', {
                        required: '*Missing entry',
                        onChange: () => resetField('deletionRequirementDescription'),
                      })}
                      type="radio"
                      className="ff-only"
                      name="deletionRequirement"
                      value="No"
                    />
                  </span>
                  <span className="label">No</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('deletionRequirement', {
                        required: '*Missing entry',
                      })}
                      type="radio"
                      className="ff-only"
                      name="deletionRequirement"
                      value="Yes"
                    />
                  </span>
                  <span className="label">Yes</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.deletionRequirement?.message}</span>
            </div>
            <div
              id="personalRelatedDataDescription"
              className={classNames(
                'input-field-group include-error area',
                errors.deletionRequirementDescription ? 'error' : '',
              )}
            >
              <label className="input-label" htmlFor="deletionRequirementDescription">
                If yes, briefly describe these requirements <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('deletionRequirementDescription', {
                  validate: validateTextArea,
                })}
                rows={50}
                id="deletionRequirementDescription"
              />
              <span className={classNames('error-message')}>{errors?.deletionRequirementDescription?.message}</span>
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

export default DeletionRequirements;
