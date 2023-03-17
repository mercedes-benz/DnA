import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';

// import OtherRelevant from '../OtherRelavantInfo';
import { Envs } from '../../../../Utility/envs';

const DeletionRequirements = (
  // { onSave, user, isDataProduct = false }
  ) => {
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
            {showInfoModal && (
              <div className={Styles.infoIcon}>
                <i className={'icon mbc-icon info'} onClick={() => {}} />
              </div>
            )}
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
      {/* <OtherRelevant onSave={onSave} user={user} isDataProduct={isDataProduct} /> */}


      <>
        <div className={Styles.wrapper}>
          <div className={Styles.firstPanel}>
            <div>
              <h3>Specifying other relevant information</h3>
              {showInfoModal && (
                <div className={Styles.infoIcon}>
                  <i className={'icon mbc-icon info'} onClick={() => {}} />
                </div>
              )}
            </div>
            <div className={Styles.formWrapper}>
              <div id="otherRelevantInfoDescription" className={classNames('input-field-group area')}>
                <label className="input-label" htmlFor="otherRelevantInfo">
                  Please provide any other relevant & app specific restrictions that might apply to the corresponding data, examples being antitrust regulations, contractual restrictions etc.
                </label>
                <textarea
                  className="input-field-area"
                  type="text"
                  {...register('otherRelevantInfo')}
                  rows={50}
                  id="otherRelevantInfo"
                />
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.wrapper}>
          <div className={Styles.firstPanel}>
            <div className={Styles.termsOfUseContainer}>
              <div className={classNames(Styles.termsOfUseContent)}>
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
      </>
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
