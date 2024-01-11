import classNames from 'classnames';
import React, {useState} from 'react';
import { useFormContext } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
// Container components
import Modal from 'dna-container/Modal';
import Notification from '../../../common/modules/uilab/js/src/notification';
import Styles from './input-file-area.scss';
import IconUpload from '../../../assets/icon_upload.png';
import ExistingFilesView from '../existingFilesView/ExistingFilesView';
import { Link } from 'react-router-dom';
import SelectedFileView from '../selectedFileView/SelectedFileView';
import { set, setInputFile } from '../../../redux/chronosFormSlice';

const InputFileArea = ({ showTutorial }) => {
  
  const dispatch = useDispatch();
  const chronosForm = useSelector(state => state.chronosForm);
  const { register, formState: { errors } } = useFormContext();
  const isValidFile = (file) => ['csv', 'xlsx'].includes(file?.name?.split('.')[1]);
  const [showExistingFiles, setShowExistingFiles] = useState(false);
  const [isExistingInputFile, setIsExistingInputFile] = useState(false);
  
  const onDrop = (e) => {
    setIsExistingInputFile(false);
    const file = e.dataTransfer.files;
    const isValid = isValidFile(file?.[0]);
    if (!isValid) {
      Notification.show('File is not valid. Only .xlsx files allowed.', 'alert');
    } else {
      register('droppedFile', { value: file });
      dispatch(setInputFile(file));
    }
  };
  const onFileDrop = (e) => {
    e.preventDefault();
    if (e.type === 'drop') {
      onDrop?.(e);
    }
  };

  return (
    <div className={Styles.wrapper}>
      <div className={Styles.firstPanel}>
        <h3>Input File</h3>
        <div className={Styles.infoIcon}>
          <button className={classNames('btn', Styles.btnShowTutorial)} onClick={() => showTutorial(true)}>
            <i className="icon mbc-icon info" /> 
            <p>Show Chronos Tutorial</p>
          </button>
        </div>
        <div className={Styles.formWrapper}>
          <div>
            <p>
              Please upload your Input File and make sure it&apos;s structured according to our{' '}
              <Link to="/help">forecasting guidelines</Link>.
            </p>
            <p>
              For a quick start you can download the default template (.xlsx) <a href={`/chronos-templates/Chronos_Forecasting_Template.xlsx`} download={true}>right here</a>.
            </p>
          </div>
          { chronosForm.inputFile.length > 0 ? 
            <SelectedFileView selectedFile={chronosForm.inputFile[0]} setIsExistingInputFile={setIsExistingInputFile} /> :
            <>
            <div className={Styles.container}>
              <div
                onDrop={onFileDrop}
                onDragOver={onFileDrop}
                onDragLeave={onFileDrop}
                className={classNames('upload-container', Styles.uploadContainer)}
              >
                <input type="file" id="file" name="file" 
                  {...register('file', { required: '*Missing entry', onChange: (e) => {
                    setIsExistingInputFile(false);
                    const isValid = isValidFile(e.target.files[0]);
                    if (!isValid) {
                      Notification.show('File is not valid. Only .xlsx files allowed.', 'alert');
                    } else {
                      dispatch(setInputFile(e.target.files));
                    }
                  }})}
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
                        dispatch(set(false));
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
            
            {showExistingFiles &&
              <Modal
                title={'Select existing input file'}
                showAcceptButton={false}
                showCancelButton={false}
                modalWidth={'35%'}
                buttonAlignment="right"
                show={showExistingFiles}
                content={<ExistingFilesView setShowExistingFiles={setShowExistingFiles} setIsExistingInputFile={setIsExistingInputFile} />}
                scrollableContent={false}
                onCancel={() => {
                  setShowExistingFiles(false);
                }}
            />
            }
          </>
          }

          <div className={Styles.checkbox}>
            <label className="checkbox">
              <span className="wrapper">
                <input
                  type="checkbox"
                  className="ff-only"
                  {...register('saveRequestPart', {
                    onChange: () => {
                      dispatch(set(!chronosForm.keepForFuture))
                    }
                  })}
                  checked={chronosForm.keepForFuture}
                  disabled={isExistingInputFile}
                />
              </span>
              <span className="label">Keep file for future use</span>
            </label>
          </div>
        </div>
      </div>
    </div>
  );
}

export default InputFileArea;