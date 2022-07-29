import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';

const Classification = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    reset,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Classification & Confidentiality</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div
              className={classNames(
                'input-field-group include-error area',
                errors.classificationOfTransferedData ? 'error' : '',
              )}
            >
              <label
                id="classificationOfTransferedDataLabel"
                className="input-label"
                htmlFor="classificationOfTransferedData"
              >
                Description & classification of the transfered data <sup>*</sup>
              </label>
              <textarea
                id="classificationOfTransferedData"
                className="input-field-area"
                type="text"
                {...register('classificationOfTransferedData', { required: '*Missing entry' })}
                rows={50}
              />
              <span className={classNames('error-message')}>{errors?.classificationOfTransferedData?.message}</span>
            </div>
            <div
              className={classNames(`input-field-group include-error ${errors?.confidentiality ? 'error' : ''}`)}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
                Confidentiality <sup>*</sup>
              </label>
              <div className={Styles.radioBtns}>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('confidentiality')}
                      type="radio"
                      className="ff-only"
                      name="confidentiality"
                      value="Public"
                      defaultChecked={watch('confidentiality') === 'Public'}
                    />
                  </span>
                  <span className="label">Public</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('confidentiality', { required: '*Missing entry' })}
                      type="radio"
                      className="ff-only"
                      name="confidentiality"
                      value="Confidential"
                    />
                  </span>
                  <span className="label">Confidential</span>
                </label>
              </div>
              <div>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('confidentiality')}
                      type="radio"
                      className="ff-only"
                      name="confidentiality"
                      value="Internal"
                    />
                  </span>
                  <span className="label">Internal</span>
                </label>
                <label className={'radio'}>
                  <span className="wrapper">
                    <input
                      {...register('confidentiality')}
                      type="radio"
                      className="ff-only"
                      name="confidentiality"
                      value="Secret"
                    />
                  </span>
                  <span className="label">Secret</span>
                </label>
              </div>
              <span className={classNames('error-message')}>{errors?.confidentiality?.message}</span>
            </div>
          </div>
          <div className="btnContainer">
            <button
              className="btn btn-primary"
              type="submit"
              onClick={handleSubmit((data) => {
                console.log(data);
                onSave();
                reset(data, {
                  keepDirty: false,
                  keepSubmitCount: false,
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

export default Classification;
