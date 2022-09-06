import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';
import { useDispatch, useSelector } from 'react-redux';
import { getClassificationTypes } from '../../redux/consumeDataProduct.services';

const Classification = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const dispatch = useDispatch();
  const { classificationTypes } = useSelector((state) => state.consumeDataProducts);

  useEffect(() => {
    dispatch(getClassificationTypes());
  }, [dispatch]);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Data Description & Classification</h3>
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
              <div className={Styles.radioBtnsGrid}>
                {classificationTypes?.map((item) => {
                  return (
                    <div key={item.id}>
                      <label className={'radio'}>
                        <span className="wrapper">
                          <input
                            {...register('confidentiality')}
                            type="radio"
                            className="ff-only"
                            name="confidentiality"
                            value={item.name}
                          />
                        </span>
                        <span className="label">{item.name}</span>
                      </label>
                    </div>
                  );
                })}
              </div>
              <span className={classNames('error-message')}>{errors?.confidentiality?.message}</span>
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
              keepSubmitCount: false,
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

export default Classification;
