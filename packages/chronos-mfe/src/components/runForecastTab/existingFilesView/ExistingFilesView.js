import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useFormContext } from 'react-hook-form';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';
import Styles from './existing-files-view.scss';
// Container components
import SelectBox from 'dna-container/SelectBox';
import { regionalDateAndTimeConversionSolution } from '../../../utilities/utils';
import { setInputFile } from '../../../redux/chronosFormSlice';


const ExistingFilesView = ({setShowExistingFiles, setIsExistingInputFile}) => {
  const dispatch = useDispatch();
  const project = useSelector(state => state.projectDetails);
  const {register} = useFormContext();
  const [savedFiles] = useState(project.data.savedInputs !== null ? [...project.data.savedInputs] : []);
  const [selectedInputFile, setSelectedInputFile] = useState('');
  const [error] = useState(false);


  useEffect(() => {
    SelectBox.defaultSetup();
    //eslint-disable-next-line
  }, []);

  const selectedSavedFile = (e) => {
    const selectedOne = savedFiles.filter(item => item.path === e.target.value);
    const selectedInput = {...selectedOne[0]};
    setSelectedInputFile(selectedInput);
  }
  
  return (
    <div className={Styles.existingFilesContainer}>
      <div>
        {savedFiles.length === 0 && <span>No saved input files</span>}
        {
          savedFiles.length !== 0 ?
            <div className={classNames(`input-field-group include-error ${error ? 'error' : ''}`, Styles.mw)}>
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
        { savedFiles.length !== 0 && selectedInputFile?.path !== undefined &&
          <>
            <hr />
            <div className={Styles.btnContinue}>
              <button
                className="btn btn-primary"
                type="submit"
                onClick={() => {
                  setShowExistingFiles(false);
                  dispatch(setInputFile([selectedInputFile]));
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
    </div>
  );
}

export default ExistingFilesView;