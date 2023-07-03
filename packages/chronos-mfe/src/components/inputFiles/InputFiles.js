import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './input-files.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { chronosApi } from '../../apis/chronos.api';
import Modal from 'dna-container/Modal';
import AceEditor from 'react-ace';

const InputFiles = ({inputFiles, showModal, addNew, proId, refresh}) => {
  const isValidFile = (file) => ['yml', 'yaml'].includes(file?.name?.split('.')[1]);

  const [showPreview, setShowPreview] = useState(false);
  const [blobURL, setBlobUrl] = useState();
  const [selectedConfigFile, setSelectedConfigFile] = useState();

  const handleUploadFile = (file) => {
    const formData = new FormData();
    formData.append('configFile', file);
    ProgressIndicator.show();
    chronosApi.uploadProjectConfigFile(proId, formData).then(() => {
        Notification.show('File uploaded successfully');
        refresh();
        ProgressIndicator.hide();
      }).catch(error => {
        ProgressIndicator.hide();
        Notification.show(
          error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while uploading config file',
          'alert',
        );
      });
  }

  const handlePreviewFile = (file) => {
    if(addNew) {
      setSelectedConfigFile(file);
      ProgressIndicator.show();
      chronosApi.getProjectConfigFileById(proId, file.id).then((res) => {
          // let blob = new Blob([res.data.configFileData], {type: 'application/json'});
          // const bURL = URL.createObjectURL(blob);
          setBlobUrl(res.data.configFileData);
          setShowPreview(true);
          ProgressIndicator.hide();
        }).catch(error => {
          ProgressIndicator.hide();
          Notification.show(
            error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while fetching preview file data',
            'alert',
          );
        });
    }
  }
  
  return (
    <>
    <div className={Styles.firstPanel}>
      { inputFiles.length > 0 ? 
      <>
        <table className={classNames('ul-table')}>
          <thead>
            <tr className="header-row">
              <th><label>Name</label></th>
              <th><label>Uploaded By</label></th>
              <th><label>Uploaded On</label></th>
              <th>&nbsp;</th>
            </tr>
          </thead>
          <tbody>
            { inputFiles.map(inputFile =>
                <tr className={classNames('data-row', Styles.dataRow)} key={inputFile?.id} onClick={() => handlePreviewFile(inputFile) }>
                  <td>{inputFile?.name}</td>
                  <td>{inputFile?.createdBy}</td>
                  <td>{regionalDateAndTimeConversionSolution(inputFile?.createdOn)}</td>
                  <td><i onClick={(e) => { e.stopPropagation(); showModal(inputFile?.id) }} className={classNames('icon delete', Styles.deleteIcon)} /></td>
                </tr>
              )
            }
          </tbody>
        </table>
      </> :
        <p>No input files present</p>
      }
      { addNew && 
        <div>
          <input type="file" id="fileConfig" name="fileConfig" className={Styles.fileInput} 
            onChange={
              (e) => {
                const isValid = isValidFile(e.target.files[0]);
                if (!isValid) {
                  Notification.show('File is not valid. Only .yml or .yaml files allowed.', 'alert');
                } else {
                  handleUploadFile(e.target.files[0]);
                }
              }
            }
            accept=".yml, .yaml"
          />
          <label htmlFor="fileConfig" className={Styles.selectExisitingFiles}><i className="icon mbc-icon plus" /> Upload new Config File</label>
        </div>
      }
    </div>
    {showPreview && (
      <Modal
        title={`Preview - ${selectedConfigFile.name}`}
        onCancel={() => setShowPreview(false)}
        modalWidth={'80vw'}
        showAcceptButton={false}
        showCancelButton={false}
        show={showPreview}
        content={
          <AceEditor
            width="100%"
            name="storagePreview"
            mode={'json'}
            theme="solarized_dark"
            fontSize={16}
            showPrintMargin={false}
            showGutter={false}
            highlightActiveLine={false}
            value={JSON.stringify(blobURL, undefined, 2)}
            readOnly={true}
            style={{
              height: '65vh',
            }}
            setOptions={{
              useWorker: false,
              showLineNumbers: false,
              tabSize: 2,
            }}
          />
        }
      />
    )}
    </>
  );
}

export default InputFiles;