import classNames from 'classnames';
import React, { useEffect, useRef, useState } from 'react';
import ReactDOM from 'react-dom';
import Upload from 'rc-upload';
import Styles from './styles.scss';
import { useFormContext } from 'react-hook-form';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';

// Container components
import SelectBox from 'dna-container/SelectBox';
import Modal from 'dna-container/Modal';

const RunForecast = ({ onSave, configurationFile, frequency, forecastHorizon }) => {
  const {
    register,
    formState: { errors, isSubmitting },
    handleSubmit,
    trigger,
    reset,
  } = useFormContext();
  const [fileList, setFiles] = useState([]);
  const [keepExistingFiles, setKeepExistingFiles] = useState(false);
  const [portal, setPortal] = useState(null);
  const uploadRef = useRef(null);

  const [showExistingFiles, setShowExistingFiles] = useState(false);
  const [existingFiles, setExistingFiles] = useState([]);

  useEffect(() => {
    SelectBox.defaultSetup();
    // reset(watch());
    //eslint-disable-next-line
  }, []);

  const uploadProps = {
    accept: '.xls,.csv',
    action: ``,
    onStart: (files) => {
      console.log(files);
      ProgressIndicator.show(1);
    },
    onSuccess() {
      ProgressIndicator.hide();
    },
    onError() {
      ProgressIndicator.hide();
    },
    onProgress: (e) => {
      ProgressIndicator.show(e.percent);
    },
    beforeUpload: (file, files) => {
      let isValid = true;
      setFiles(files);
      return isValid;
    },
  };

  const onDrop = (e) => {
    console.log('Dropped files', e.dataTransfer.files);
    setFiles(e.dataTransfer.files);
  };

  const onFileDrop = (e) => {
    if (e.type === 'drop') {
      onDrop?.(e);
    }
  };

  useEffect(() => {
    if (!fileList.length) {
      const container = document?.querySelector('.upload-container');
      const portal = ReactDOM.createPortal(
        <div className={Styles.helperTextContainer}>
          <div className={Styles.browseHelperText}>
            You can also <button className={Styles.selectExisitingFiles}>browse local files</button> (.xls, .csv)
          </div>
          <div
            className={Styles.browseHelperText}
            onClick={(e) => {
              e.stopPropagation();
              setExistingFiles([]);
              setShowExistingFiles(true);
            }}
          >
            <p>
              or <button className={Styles.selectExisitingFiles}>select an existing file</button> to run forecast
            </p>
          </div>
        </div>,
        container,
      );
      setPortal(portal);
    }
  }, [fileList.length]);

  const existingFilesContent = (
    <div className={Styles.existingFilesContainer}>
      <div className={Styles.flexLayout}>
        {' '}
        <div className={classNames(`input-field-group include-error ${errors?.existingFile?.message ? 'error' : ''}`)}>
          <label id="existingFileLabel" htmlFor="existingFilenField" className="input-label">
            Input File <sup>*</sup>
          </label>
          <div className="custom-select" onBlur={() => trigger('existingFile')}>
            <select
              id="existingFileField"
              required={true}
              required-error={'*Missing entry'}
              {...register('existingFile', {
                validate: (value) => value !== '0' || '*Missing entry',
              })}
            >
              <option id="existingFileOption" value={0}>
                Choose
              </option>
              {existingFiles?.map((obj) => (
                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                  {obj.name}
                </option>
              ))}
            </select>
          </div>
          <span className={classNames('error-message')}>{errors?.existingFile?.message}</span>
        </div>
      </div>
      <p>MS_tms_fc.xls</p>
      <div className={Styles.flexLayout}>
        <div className={Styles.flexLayout}>
          <div>
            <div className={Styles.uploadInfo}>
              <span>Uploaded On</span>
              <span>27/03/2022</span>
            </div>
            <div className={Styles.uploadInfo}>
              <span>Uploaded By</span>
              <span>John Doe</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const SelectedFile = () => {
    return (
      <>
        <div className={Styles.selectedFile}>
          <div>
            <span>Input File</span>
            <span>{'MS_tms_fc.xls'}</span>
          </div>
          <span>
            <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} />
            <span>File is ready to use.</span>
          </span>
          <span>
            <i onClick={() => setFiles([])} className={classNames('icon delete', Styles.deleteIcon)} />
          </span>
        </div>
      </>
    );
  };

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <h3>Input File</h3>
          <div className={Styles.infoIcon}>
            <i className="icon mbc-icon info" onClick={() => {}} />
          </div>
          <div className={Styles.formWrapper}>
            <div>
              <p>
                Please upload your Input File and make sure it&apos;s structured according to our{' '}
                <a href="#/chronos/project">forecasting guidelines</a>.
              </p>
              <p>
                For a quick start you can download the default template (.xls) <a href="#/">right here</a>.
              </p>
            </div>
            {!fileList.length ? (
              <div className={Styles.container}>
                <div
                  onDrop={(e) => {
                    e.preventDefault();
                    onFileDrop(e);
                  }}
                  onDragOver={(e) => {
                    e.preventDefault();
                    onFileDrop(e);
                  }}
                  onDragLeave={onFileDrop}
                  className={classNames('upload-container', Styles.uploadContainer)}
                >
                  <Upload {...uploadProps}>
                    <div>
                      <div className={Styles.icon}>
                        <button ref={uploadRef} style={{ display: 'none' }}></button>
                      </div>
                      <h4>Drag & Drop your Input File here to upload</h4>
                    </div>
                    {portal}
                  </Upload>
                </div>
              </div>
            ) : (
              <SelectedFile />
            )}
            <div className={Styles.checkbox}>
              <label className="checkbox">
                <span className="wrapper">
                  <input
                    value={keepExistingFiles}
                    type="checkbox"
                    className="ff-only"
                    onChange={() => {
                      setKeepExistingFiles(!keepExistingFiles);
                    }}
                    checked={keepExistingFiles}
                  />
                </span>
                <span className="label">Keep file for future use</span>
              </label>
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <h3>Run Parameters</h3>
          <div className={Styles.infoIcon}>
            <label className="switch">
              <span className="label" style={{ marginRight: '5px' }}>
                Enable Expert View
              </span>
              <span className="wrapper">
                <input
                  value={keepExistingFiles}
                  type="checkbox"
                  className="ff-only"
                  onChange={() => setKeepExistingFiles(!keepExistingFiles)}
                  checked={keepExistingFiles}
                />
              </span>
            </label>
          </div>
          <div className={Styles.formWrapper}>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.runName ? 'error' : '')}>
                <label id="runNameLabel" htmlFor="runNameInput" className="input-label">
                  Run Name <sup>*</sup>
                </label>
                <input
                  {...register('runName', { required: '*Missing entry' })}
                  type="text"
                  className="input-field"
                  id="runNameInput"
                  maxLength={200}
                  placeholder="eg. YYYY-MM-DD_run-topic"
                  autoComplete="off"
                />
                <span className={classNames('error-message')}>{errors.runName?.message}</span>
              </div>
              <div className={Styles.configurationContainer}>
                <div
                  className={classNames(
                    `input-field-group include-error ${errors?.configuration?.message ? 'error' : ''}`,
                  )}
                >
                  <label id="configurationLabel" htmlFor="configurationField" className="input-label">
                    Configuration File <sup>*</sup>
                  </label>
                  <div className="custom-select" onBlur={() => trigger('configuration')}>
                    <select
                      id="configurationField"
                      required={true}
                      required-error={'*Missing entry'}
                      {...register('configuration', {
                        validate: (value) => value !== '0' || '*Missing entry',
                      })}
                    >
                      <option id="configurationOption" value={0}>
                        Choose
                      </option>
                      {configurationFile?.map((obj) => (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                          {obj.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message')}>{errors?.configuration?.message}</span>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={Styles.frequencyContainer}>
                <div
                  className={classNames(`input-field-group include-error ${errors?.frequency?.message ? 'error' : ''}`)}
                >
                  <label id="frequencyLabel" htmlFor="frequencyField" className="input-label">
                    Frequency <sup>*</sup>
                  </label>
                  <div className="custom-select" onBlur={() => trigger('frequency')}>
                    <select
                      id="frequencyField"
                      required={true}
                      required-error={'*Missing entry'}
                      {...register('frequency', {
                        validate: (value) => value !== '0' || '*Missing entry',
                      })}
                    >
                      <option id="frequencyOption" value={0}>
                        Choose
                      </option>
                      {frequency?.map((obj) => (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                          {obj.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message')}>{errors?.frequency?.message}</span>
                </div>
              </div>
              <div className={Styles.forecastHorizonContainer}>
                <div
                  className={classNames(
                    `input-field-group include-error ${errors?.forecastHorizon?.message ? 'error' : ''}`,
                  )}
                >
                  <label id="forecastHorizonLabel" htmlFor="forecastHorizonField" className="input-label">
                    Forecast Horizon <sup>*</sup>
                  </label>
                  <div className="custom-select" onBlur={() => trigger('forecastHorizon')}>
                    <select
                      id="forecastHorizonField"
                      required={true}
                      required-error={'*Missing entry'}
                      {...register('forecastHorizon', {
                        validate: (value) => value !== '0' || '*Missing entry',
                      })}
                    >
                      <option id="forecastHorizonOption" value={0}>
                        Choose
                      </option>
                      {forecastHorizon?.map((obj) => (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                          {obj.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message')}>{errors?.forecastHorizon?.message}</span>
                </div>
              </div>
            </div>
            <div>
              <div
                id="comment"
                className={classNames('input-field-group include-error area', errors.comment ? 'error' : '')}
              >
                <label className="input-label" htmlFor="comment">
                  Add comment
                </label>
                <textarea className="input-field-area" type="text" {...register('comment')} rows={50} id="comment" />
                <span className={classNames('error-message')}>{errors?.comment?.message}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <button
          className="btn btn-tertiary"
          type="submit"
          disabled={isSubmitting}
          onClick={handleSubmit((values) => {
            reset(values, {
              keepDirty: false,
            });
            onSave(values);
          })}
        >
          Run Forecast
        </button>
      </div>
      <Modal
        title={'Select existing input file'}
        showAcceptButton={false}
        showCancelButton={false}
        modalWidth={'50%'}
        buttonAlignment="right"
        show={showExistingFiles}
        content={existingFilesContent}
        scrollableContent={false}
        onCancel={() => {
          setShowExistingFiles(false);
        }}
      />
    </>
  );
};

export default RunForecast;
