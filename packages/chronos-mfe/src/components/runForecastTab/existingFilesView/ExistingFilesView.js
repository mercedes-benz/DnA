import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import { useFormContext } from 'react-hook-form';
import { useSelector } from 'react-redux';
import Styles from './existing-files-view.scss';
// Container components
import SelectBox from 'dna-container/SelectBox';
import { regionalDateAndTimeConversionSolution } from '../../../utilities/utils';


const ExistingFilesView = ({setShowExistingFiles, setInputFile, setIsExistingInputFile}) => {
  const project = useSelector(state => state.projectDetails);
  const {register} = useFormContext();
  const [savedFiles] = useState([...project.data.savedInputs]);
  const [selectedInputFile, setSelectedInputFile] = useState('');
  const [error] = useState(false);


  useEffect(() => {
    SelectBox.defaultSetup();
    //eslint-disable-next-line
  }, []);

  const inputFileSelect = useRef();

  const selectedSavedFile = (e) => {
    const selectedOne = savedFiles.filter(item => item.path === e.target.value);
    const selectedInput = {...selectedOne[0]};
    setSelectedInputFile(selectedInput);
  }
  
  return (
    <div className={Styles.existingFilesContainer}>
      <div className={Styles.mw}>
        {savedFiles.length === 0 && <span>No saved input files</span>}
        {
          savedFiles.length !== 0 ? 
          <div className={classNames(`input-field-group include-error ${error ? 'error' : ''}`)}>
            <label id="savedInputPathLabel" htmlFor="existingFilenField" className="input-label">
              Input File <sup>*</sup>
            </label>
            <div className="custom-select" 
              // onBlur={() => trigger('savedInputPath')}
              >
              <select
                id="savedInputPath"
                required={true}
                onChange={(e) => {selectedSavedFile(e)}}
                ref={inputFileSelect}
              >
                <option id="savedInputPathOption" value={0}>
                  Choose
                </option>
                {savedFiles.map((file) => (
                  <option id={file.name} key={file.id} value={file.path}>
                    {file.name}
                  </option>
                ))}
              </select>
            </div>
            <span className={classNames('error-message')}>{error && '*Missing entry'}</span>
          </div> : null
        }
      </div>
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
      { savedFiles.length !== 0 &&
        <>
          <hr />
          <div className={Styles.btnContinue}>
            <button
              className="btn btn-primary"
              type="submit"
              onClick={() => {
                setShowExistingFiles(false);
                setInputFile([selectedInputFile]);
                register('savedInputPath', { value: selectedInputFile.path });
                setIsExistingInputFile(true);
              }}
            >
              Continue with file
            </button>
          </div>
        </>
      }
    </div>
  );
}

export default ExistingFilesView;