import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';

import OtherRelevant from '../OtherRelavantInfo';

const DeletionRequirements = ({ onSave }) => {
  const {
    register,
    formState: { errors },
    watch,
    clearErrors,
    setValue,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const validateTextArea = (value) =>
    watch('deletionRequirement') === '' ||
    watch('deletionRequirement') === 'No' ||
    value?.length > 0 ||
    '*Missing entry';

  const isTextAreaDisabled = watch('deletionRequirement') === '' || watch('deletionRequirement') === 'No';

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
                        onChange: () => {
                          clearErrors('deletionRequirementDescription');
                          setValue('deletionRequirementDescription', '');
                        },
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
              className={classNames(
                `input-field-group include-error area ${isTextAreaDisabled ? 'disabled' : ''}`,
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
                  disabled: isTextAreaDisabled,
                })}
                rows={50}
                id="deletionRequirementDescription"
              />
              <span className={classNames('error-message')}>{errors?.deletionRequirementDescription?.message}</span>
            </div>
          </div>
        </div>
      </div>
      <OtherRelevant onSave={onSave} />
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
