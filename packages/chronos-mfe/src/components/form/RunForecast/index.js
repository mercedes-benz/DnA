import classNames from 'classnames';
import React, { useEffect, useState } from 'react';

import Styles from './styles.scss';
import { useForm } from 'react-hook-form';
import { useParams } from 'react-router-dom';

// Container components
import SelectBox from 'dna-container/SelectBox';
import Modal from 'dna-container/Modal';

import { regionalDateAndTimeConversionSolution } from '../../../Utility/utils';

import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../common/modules/uilab/js/src/notification';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import { Link } from 'react-router-dom';

import IconUpload from '../../../assets/icon_upload.png';
import { chronosApi } from '../../../apis/chronos.api';

const SelectedFile = ({ selectedFile, setSelected, setFileValid }) => {
  return (
    <>
      <div className={Styles.selectedFile}>
        <div>
          <span>Input File</span>
          <span>{selectedFile.name}</span>
        </div>
        <span>
          <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} />
          <span>File is ready to use.</span>
        </span>
        <span>
          <i onClick={() => { setSelected(false); setFileValid(true); }} className={classNames('icon delete', Styles.deleteIcon)} />
        </span>
      </div>
    </>
  );
};

const RunForecast = () => {
  const { id: projectId } = useParams();

  const { register, handleSubmit, isSubmitting, reset, formState: { errors } } = useForm();

  const [keepExistingFiles, setKeepExistingFiles] = useState(false);
  const [showExistingFiles, setShowExistingFiles] = useState(false);
  const [expertView, setExpertView] = useState(false);
  const [savedFiles, setSavedFiles] = useState([]);
  const [isExistingFile, setIsExistingFile] = useState(false);
  

  const isValidFile = (file) => ['csv', 'xlsx'].includes(file?.name?.split('.')[1]);

  const frequencyTooltipContent =
    'Please select a frequency for your data in the field below.\n Make sure the datetimes in the first column of the data you upload matches the frequency selected here.\n If your data has no inherent frequency or the frequency is not available in the list, select "No frequency".\n In this case, the first column of your data should contain sortable indices like [1, 2, 3...].';
  const forecastHorizonTooltipContent =
    'Select how many data points in the future the forecast should predict.\n Note that this number should not be more than 1/5th the length of your existing data, ideally less.\n Also, forecasting gets less precise over time, so try to not predict too many points in the future.';

  useEffect(() => {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
    //eslint-disable-next-line
  }, []);

  const [selectedInputFile, setSelectedInputFile] = useState();
  const selectedSavedFile = (e) => {
    const selectedOne = savedFiles.filter(item => item.path === e.target.value);
    const selectedInput = {...selectedOne[0]};
    setSelectedInputFile(selectedInput);
  }
  const setSelectedInput = (value) => {
    setIsSelectedFile(value);
    setSelectedInputFile({});
  }

  const [isSelectedFile, setIsSelectedFile] = useState(false);

  useEffect(() => {
    getInputFiles();
    //eslint-disable-next-line
  }, []);
  
  const getInputFiles = () => {
    chronosApi.getAllInputFiles(projectId).then((res) => {
      if(res.data !== '') {
        setSavedFiles(res.data.files);
      }
      SelectBox.defaultSetup();
    }).catch(error => {
      if(error?.response?.data?.errors[0]?.message) {
        Notification.show(error?.response?.data?.errors[0]?.message, 'alert');
      } else {
        Notification.show(error.message, 'alert');
      }
    });
  }
  
  const existingFilesContent = (
    <div className={Styles.existingFilesContainer}>
      <div className={Styles.flexLayout}>
        {' '}
        <div className={classNames(`input-field-group include-error ${errors?.savedInputPath ? 'error' : ''}`)}>
          <label id="savedInputPathLabel" htmlFor="existingFilenField" className="input-label">
            Input File <sup>*</sup>
          </label>
          <div className="custom-select" 
            // onBlur={() => trigger('savedInputPath')}
            >
            <select
              id="savedInputPathField"
              required={true}
              {...register('savedInputPath', {
                // required: true,
                // validate: (value) => value !== '0' || '*Missing entry',
                onChange: (e) => { selectedSavedFile(e) }
              })}
            >
              {
                savedFiles.length === 0 ? (
                  <option id="savedInputPathOption" value={0}>
                    None
                  </option>
                  ) : (
                    <>
                      <option id="savedInputPathOption" value={0}>
                        Choose
                      </option>
                      {savedFiles.map((file) => (
                        <option id={file.name} key={file.id} value={file.path}>
                          {file.name}
                        </option>
                      ))}
                    </>
                  )
              }
            
            
            </select>
          </div>
          <span className={classNames('error-message')}>{errors?.savedInputPath && '*Missing entry'}</span>
        </div>
      </div>
      {savedFiles.length === 0 && <span>No saved input files</span>}
      {
        selectedInputFile?.path !== undefined &&
          <>
            <p>{selectedInputFile?.name}</p>
            <div className={Styles.flexLayout}>
              <div className={Styles.fullWidth}>
                <div className={Styles.uploadInfo}>
                  <span>Uploaded On</span>
                  <span>{regionalDateAndTimeConversionSolution(selectedInputFile?.createdOn)}</span>
                </div>
                <div className={Styles.uploadInfo}>
                  <span>Uploaded By</span>
                  <span>{selectedInputFile?.createdBy}</span>
                </div>
              </div>
            </div>
          </>
      }
      <hr />
      <div className={Styles.btnContinue}>
        <button
          className="btn btn-primary"
          type="submit"
          disabled={isSubmitting}
          onClick={() => {
            setShowExistingFiles(false);
            setIsSelectedFile(true);
          }}
        >
          Continue with file
        </button>
      </div>
    </div>
  );

  const onDrop = (e) => {
    console.log('Dropped files', e.dataTransfer.files);
    const file = e.dataTransfer.files?.[0];
    const isValid = isValidFile(file);
    if (!isValid) Notification.show('File is not valid.', 'alert');
    setIsSelectedFile(true);
    setSelectedInputFile({name: e.dataTransfer.files?.[0].name});
  };

  const onFileDrop = (e) => {
    e.preventDefault();
    if (e.type === 'drop') {
      onDrop?.(e);
    }
  };

  const [fileValid, setFileValid] = useState(true);

  const validateFile = (file) => {
    const fileName = file.name.split('.');
    const extension = fileName[fileName.length - 1];
    if(extension === 'csv' || extension === 'xlsx') {
      setFileValid(true);
    } else {
      setFileValid(false);
    }
  }

  const onSubmit = (data) => {
    const formData = new FormData();
    if(selectedInputFile?.path !== undefined) {
      formData.append("file", '');
    } else {
      formData.append("file", data.file[0]);
    }
    formData.append("runName", data.runName);
    formData.append("configurationFile", data.configurationFile);
    formData.append("frequency", data.frequency);
    formData.append("forecastHorizon", data.forecastHorizon);
    formData.append("comment", data.comment);
    formData.append("saveRequestPart", keepExistingFiles.toString());
    if(selectedInputFile?.path !== undefined) {
      formData.append("savedInputPath", selectedInputFile.path);
    } else {
      formData.append("savedInputPath", null); // todo file path
    }

    ProgressIndicator.show();
    chronosApi.createForecastRun(formData, projectId).then((res) => {
        console.log(res);
        Notification.show('Run created successfully');
        document.body.scrollTop = 0;
        document.documentElement.scrollTop = 0;
        reset({
          runName: '',
          configurationFile: 0,
          frequency: 0,
          forecastHorizon: 1,
          comment: '',
        });
        SelectBox.defaultSetup();
        setIsSelectedFile(false);
        ProgressIndicator.hide();
      }).catch(error => {
        ProgressIndicator.hide();
        if(error?.response?.data?.errors[0]?.message) {
          Notification.show(error?.response?.data?.errors[0]?.message, 'alert');
        } else {
          Notification.show(error.message, 'alert');
        }
      });
  }

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)}>
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
                  <Link to="/help">forecasting guidelines</Link>.
                </p>
                <p>
                  For a quick start you can download the default template (.xlsx) <a href={`/chronos-templates/Chronos_Forecasting_Template.xltx`} download={true}>right here</a>.
                </p>
              </div>
              {!isSelectedFile ? (
                <div className={Styles.container}>
                  <div
                    onDrop={onFileDrop}
                    onDragOver={onFileDrop}
                    onDragLeave={onFileDrop}
                    className={classNames('upload-container', Styles.uploadContainer)}
                  >
                    <input type="file" id="file" name="file" 
                      {...register('file', { required: '*Missing entry', onChange: (e) => { setIsSelectedFile(true); setSelectedInputFile({name: e.target.files[0].name}); validateFile(e.target.files[0]); setIsExistingFile(false); }})}
                      accept=".csv, .xlsx"
                      />
                    <div className={Styles.rcUpload}>
                      <div className={Styles.dragDrop}>
                        <div className={Styles.icon}>
                          <img src={IconUpload} />
                        </div>
                        <h4>Drag & Drop your Input File here to upload</h4>
                      </div>
                      <div className={Styles.helperTextContainer}>
                        <div className={Styles.browseHelperText}>
                          You can also <label htmlFor="file" className={Styles.selectExisitingFiles}>browse local files</label> (.xlsx)
                        </div>
                        <div
                          className={Styles.browseHelperText}
                          onClick={(e) => {
                            e.stopPropagation();
                            setShowExistingFiles(true);
                            setKeepExistingFiles(false);
                            setIsExistingFile(true);
                          }}
                        >
                          <p>
                            or<button className={Styles.selectExisitingFiles}>select an existing file</button>to run
                            forecast
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                  {errors.file && <span className={Styles.errorMessage}>{errors.file?.message}</span>}
                </div>
              ) : (
                <SelectedFile selectedFile={selectedInputFile} setSelected={setSelectedInput} setFileValid={setFileValid} />
              )}
              {!fileValid && <span className={Styles.errorMessage}>Only .xlsx files allowed</span>}
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
                      disabled={isExistingFile}
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
                    value={expertView}
                    type="checkbox"
                    className="ff-only"
                    onChange={() => setExpertView(!expertView)}
                    checked={expertView}
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
                    {...register('runName', { required: '*Missing entry', pattern: /^[a-z]+$/ })}
                    type="text"
                    className="input-field"
                    id="runNameInput"
                    maxLength={200}
                    placeholder="Type here"
                    autoComplete="off"
                  />
                  <span className={classNames('error-message')}>{errors.runName?.message}{errors.runName?.type === 'pattern' && 'Only lowercase letters without spaces are allowed'}</span>
                </div>
                <div className={Styles.configurationContainer}>
                  <div
                    className={classNames(
                      `input-field-group include-error ${errors?.configurationFile ? 'error' : ''}`,
                    )}
                  >
                    <label id="configurationLabel" htmlFor="configurationField" className="input-label">
                      Configuration File <sup>*</sup>
                    </label>
                    <div className="custom-select" 
                      // onBlur={() => trigger('configurationFile')}
                      >
                      <select
                        id="configurationField"
                        required={true}
                        {...register('configurationFile', {
                          required: '*Missing entry',
                          validate: (value) => value !== '0' || '*Missing entry',
                        })}
                      >
                        <option id="configurationOption" value={0}>
                          Choose
                        </option>
                        <option value={'Default-Settings'}>Default Configuration</option>
                      </select>
                    </div>
                    <span className={classNames('error-message')}>{errors?.configurationFile?.message}</span>
                  </div>
                </div>
              </div>
              <div className={Styles.flexLayout}>
                <div className={Styles.frequencyContainer}>
                  <div
                    className={classNames(
                      `input-field-group include-error ${errors?.frequency ? 'error' : ''}`,
                      Styles.tooltipIcon,
                    )}
                  >
                    <label id="frequencyLabel" htmlFor="frequencyField" className="input-label">
                      Frequency <sup>*</sup>
                      <i className="icon mbc-icon info" tooltip-data={frequencyTooltipContent} />
                    </label>
                    <div className="custom-select" 
                      // onBlur={() => trigger('frequency')}
                      >
                      <select
                        id="frequencyField"
                        required={true}
                        {...register('frequency', {
                          required: '*Missing entry',
                          validate: (value) => value !== '0' || '*Missing entry',
                        })}
                      >
                        <option id="frequencyOption" value={0}>
                          Choose
                        </option>
                        <option value={'Daily'}>Daily</option>
                        <option value={'Weekly'}>Weekly</option>
                        <option value={'Monthly'}>Monthly</option>
                        <option value={'Yearly'}>Yearly</option>
                        <option value={'No_Frequency'}>No Frequency</option>
                      </select>
                    </div>
                    <span className={classNames('error-message')}>{errors?.frequency?.message}</span>
                  </div>
                </div>
                <div className={Styles.forecastHorizonContainer}>
                  <div className={classNames('input-field-group include-error', errors.forecastHorizon ? 'error' : '', Styles.tooltipIcon)}>
                    <label id="forecastHorizonLabel" htmlFor="forecastHorizonField" className="input-label">
                      Forecast Horizon <sup>*</sup>
                      <i className="icon mbc-icon info" tooltip-data={forecastHorizonTooltipContent} />
                    </label>
                    <input
                      {...register('forecastHorizon', { required: '*Missing entry' })}
                      type="number"
                      className="input-field"
                      id="forecastHorizonField"
                      defaultValue={1}
                      placeholder="eg. 1"
                      autoComplete="off"
                    />
                    <span className={classNames('error-message')}>{errors.forecastHorizon?.message}</span>
                  </div>
                </div>
              </div>
              <div>
                <div
                  id="comment"
                  className={classNames('input-field-group include-error area')}
                >
                  <label className="input-label" htmlFor="comment">
                    Add comment
                  </label>
                  <textarea className="input-field-area" type="text" {...register('comment')} rows={50} id="comment" />
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
          >
            Run Forecast
          </button>
        </div>

        <Modal
          title={'Select existing input file'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'35%'}
          buttonAlignment="right"
          show={showExistingFiles}
          content={existingFilesContent}
          scrollableContent={false}
          onCancel={() => {
            setShowExistingFiles(false);
          }}
        />
      </form>
    </>
  );
};

export default RunForecast;
