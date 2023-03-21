import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from '../Form.common.styles.scss';

import { useFormContext } from 'react-hook-form';
import InfoModal from 'dna-container/InfoModal';
import { useDispatch, useSelector } from 'react-redux';
import { getClassificationTypes } from '../../../redux/getDropdowns.services';

const Classification = (
  // { onSave }
  ) => {
  const {
    register,
    // handleSubmit,
    formState: { errors },
    // reset,
    // setValue,
    // watch,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const dispatch = useDispatch();
  const { classificationTypes } = useSelector((state) => state.dropdowns);

  useEffect(() => {
    dispatch(getClassificationTypes());
  }, [dispatch]);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Data Description &amp; Classification</h3>
            {showInfoModal && (
              <div className={Styles.infoIcon}>
                <i className={'icon mbc-icon info'} onClick={() => {}} />
              </div>
            )}
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
                Description of transferred data <sup>*</sup>
              </label>
              <textarea
                id="classificationOfTransferedData"
                className="input-field-area"
                type="text"
                {...register('classificationOfTransferedData', { required: '*Missing entry' })}
                rows={50}
                placeholder="Please describe brieﬂy (1-2 sentences) what data is to be transferred (e.g. accounting, controlling, year-end reporting etc.) from a business point of view"
              />
              <span className={classNames('error-message')}>{errors?.classificationOfTransferedData?.message}</span>
            </div>
            <div
              className={classNames(`input-field-group include-error ${errors?.confidentiality ? 'error' : ''}`)}
              style={{ minHeight: '50px' }}
            >
              <label className={classNames(Styles.inputLabel, 'input-label')}>
              Confidentiality classification of transferred data (based on Information classification) <sup>*</sup>
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
              <div>
                <p>To ensure the safe and secure handling of information, Mercedes-Benz has standardized the classification of information into one of the following four classifications for CONFIDENTIALITY:</p>
                <ul>
                  <li>INTERNAL information is the most common classification at Mercedes-Benz. Distribution of internal information is normally allowed to be shared with larger groups of people.</li>
                  <li>PUBLIC information is not confidential and is intended for general use inside and outside of Mercedes-Benz. Please note, only MB communications can classify data as public.</li>
                  <li>CONFIDENTIAL information is the second highest level of information classification. Distribution of confidential information must be restricted to a small group of people.</li>
                  <li>SECRET information is the most confidential information of Mercedes-Benz. Distribution of secret information must be restricted to a very small group of identified people.</li>
                </ul>
              </div>
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
              keepSubmitCount: false,
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

export default Classification;
