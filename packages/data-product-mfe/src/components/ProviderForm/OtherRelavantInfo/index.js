import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './styles.scss';

import { useFormContext } from 'react-hook-form';

import InfoModal from 'dna-container/InfoModal';
import ConfirmModal from 'dna-container/ConfirmModal';

import { Envs } from '../../../Utility/envs';
import { withRouter } from 'react-router-dom';

const OtherRelevantInfo = ({ onSave }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
    setValue,
  } = useFormContext();

  const [showInfoModal, setShowInfoModal] = useState(false);
  const [showPublishModal, setPublishModal] = useState(false);
  const [touChecked, setTOUChecked] = useState(false);

  const publishContent = (
    <div>
      <h3>Publish Data Product</h3>
      <div className={Styles.termsOfUseContainer}>
        <div className={Styles.termsOfUseContent}>
          <label className="checkbox">
            <span className="wrapper">
              <input
                value={touChecked}
                type="checkbox"
                className="ff-only"
                onChange={() => setTOUChecked(!touChecked)}
                checked={touChecked}
              />
            </span>
            <div
              className={classNames(Styles.termsOfUseText, 'mbc-scroll')}
              dangerouslySetInnerHTML={{
                __html: Envs.DATA_PRODUCT_TOU_HTML,
              }}
            ></div>
          </label>
        </div>
      </div>
    </div>
  );

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <div>
            <h3>Specifying other relevant information</h3>
            <div className={Styles.infoIcon}>
              <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
            </div>
          </div>
          <div className={Styles.formWrapper}>
            <div
              id="personalRelatedDataDescription"
              className={classNames('input-field-group include-error area', errors.otherRelevantInfo ? 'error' : '')}
            >
              <label className="input-label" htmlFor="otherRelevantInfo">
                Please provide any other relevant & app specific restrictions that might apply to the corresponding
                data, examples being individual deletion requirements, antitrust regulations, contractual restrictions
                etc. <sup>*</sup>
              </label>
              <textarea
                className="input-field-area"
                type="text"
                {...register('otherRelevantInfo', {
                  required: '*Missing entry',
                })}
                rows={50}
                id="otherRelevantInfo"
              />
              <span className={classNames('error-message')}>{errors?.otherRelevantInfo?.message}</span>
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
                className={'btn btn-tertiary'}
                type="button"
                onClick={handleSubmit(() => {
                  setPublishModal(true);
                })}
              >
                Publish Data Product
              </button>
            </div>
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
      <ConfirmModal
        title={''}
        acceptButtonTitle="Publish"
        showAcceptButton={true}
        showCancelButton={true}
        show={showPublishModal}
        content={publishContent}
        onAccept={() => {
          setValue('tou', true); // set tou to true;
          setValue('publish', true); // set publish to true;
          onSave(watch());
          setTOUChecked(false);
          setPublishModal(false);
        }}
        onCancel={() => setPublishModal(false)}
        acceptButtonDisabled={!touChecked}
      />
    </>
  );
};

export default withRouter(OtherRelevantInfo);
