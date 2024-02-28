import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Styles from './input-files.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { chronosApi } from '../../apis/chronos.api';
import Modal from 'dna-container/Modal';
import { refreshToken } from 'dna-container/RefreshToken';
import AceEditor from 'react-ace';
//import theme
import 'ace-builds/src-noconflict/theme-solarized_dark';
import 'ace-builds/src-noconflict/mode-yaml';
import { getProjectDetails } from '../../redux/projectDetails.services';
import { getConfigFiles } from '../../redux/chronosForm.services';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import { Envs } from '../../utilities/envs';

const InputFiles = ({inputFiles, showModal, addNew}) => {
  const { id: projectId } = useParams();
  const isValidFile = (file) => ['yml', 'yaml'].includes(file?.name?.slice((file?.name?.lastIndexOf(".") - 1 >>> 0) + 2));

  const [showPreview, setShowPreview] = useState(false);
  const [blobURL, setBlobUrl] = useState();
  const [selectedConfigFile, setSelectedConfigFile] = useState();

  const [showEdit, setShowEdit] = useState(false);
  const [configEditorContent, setConfigEditorContent] = useState('');

  const project = useSelector(state => state.projectDetails);
  const dispatch = useDispatch();

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const beforeUpload = () => {
    return new Promise((resolve, reject) => {
      if (!Envs.OIDC_DISABLED) {
        const jwt = sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
        refreshToken(jwt)
          .then(() => {
            // continue as usual
            resolve(true);
          })
          .catch((err) => {
            // prevent upload
            reject(err);
          });
      } else {
        // continue as usual if not in production
        resolve(true);
      }
    });
  };

  const handleUploadFile = (file) => {
    const formData = new FormData();
    formData.append('configFile', file);
    ProgressIndicator.show();
    beforeUpload().then(beforeUpload => {
      if(beforeUpload) {
        chronosApi.uploadProjectConfigFile(project?.data?.id, formData).then(() => {
          Notification.show('File uploaded successfully');
          dispatch(getProjectDetails(projectId));
          dispatch(getConfigFiles(projectId)); 
          ProgressIndicator.hide();
        }).catch(error => {
          ProgressIndicator.hide();
          Notification.show(
            error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while uploading config file',
            'alert',
          );
        });
      } else {
        ProgressIndicator.hide();
        Notification.show('Error while uploading file', 'alert');
      }
    }).catch(() => {
      ProgressIndicator.hide();
      Notification.show('Error while uploading config file', 'alert');
    });
  }

  const handlePreviewFile = (file) => {
    if(addNew) {
      setSelectedConfigFile(file);
      ProgressIndicator.show();
      chronosApi.getProjectConfigFileById(project?.data?.id, file.id).then((res) => {
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

  const downloadConfigFile = (file) => {
    ProgressIndicator.show();
    chronosApi.getConfigFile(`${project?.data?.bucketName}`, `${file.name}`).then((res) => {
      var ymlBlob = new Blob([res.data]);     
      var url = window.URL.createObjectURL(ymlBlob);

      let link = document.createElement("a");
      link.setAttribute("href", url);
      link.setAttribute("download", `${file.name}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      ProgressIndicator.hide();
    }).catch(() => {
      ProgressIndicator.hide();
    });
  }

  const downloadInputFile = (file)=>{
    ProgressIndicator.show();
    chronosApi.getInputFile(`${project?.data?.bucketName}`, `${file.name}`).then((res) => {
      var ymlBlob = new Blob([res.data]);     
      var url = window.URL.createObjectURL(ymlBlob);
      
      let link = document.createElement("a");
      link.setAttribute("href", url);
      link.setAttribute("download", `${file.name}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      ProgressIndicator.hide();
    }).catch(() => {
      ProgressIndicator.hide();
    });

  }

  const editConfigFile = (file) => {
    if(addNew) {
      setSelectedConfigFile(file);
      ProgressIndicator.show();
      chronosApi.getProjectConfigFileById(project?.data?.id, file.id).then((res) => {
          setConfigEditorContent(res.data.configFileData);
          setShowEdit(true);
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

  const onConfigCodeChange = (newValue) => {
    setConfigEditorContent(newValue);
  };

  const handleUpdateConfig = () => {
    const formData = new FormData();
    const blob = new Blob([configEditorContent], {type : 'application/octet-stream'});
    formData.append('configFile', blob, selectedConfigFile.name);
    ProgressIndicator.show();
    beforeUpload().then(beforeUpload => {
      if(beforeUpload) {
        chronosApi.uploadProjectConfigFile(project?.data?.id, formData).then(() => {
          Notification.show('File uploaded successfully');
          dispatch(getProjectDetails(projectId));
          dispatch(getConfigFiles(projectId)); 
          setConfigEditorContent('');
          ProgressIndicator.hide();
        }).catch(error => {
          ProgressIndicator.hide();
          Notification.show(
            error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while uploading config file',
            'alert',
          );
        });
      } else {
        ProgressIndicator.hide();
        Notification.show('Error while uploading file', 'alert');
      }
    }).catch(() => {
      ProgressIndicator.hide();
      Notification.show('Error while uploading config file', 'alert');
    });
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
                <tr className={classNames('data-row', Styles.dataRow)} key={inputFile?.id} onClick={() => handlePreviewFile(inputFile)}>
                  <td className={Styles.name}>{inputFile?.name}</td>
                  <td>{inputFile?.createdBy}</td>
                  <td>{regionalDateAndTimeConversionSolution(inputFile?.createdOn)}</td>
                  <td>
                    <div className={Styles.actions}>
                    <i onClick={(e) => { e.stopPropagation(); addNew ? downloadConfigFile(inputFile) : downloadInputFile(inputFile) }} className={classNames('icon mbc-icon document', Styles.deleteIcon)} tooltip-data={'Download File'} />
                      { addNew && 
                        <>
                          <i onClick={(e) => { e.stopPropagation(); editConfigFile(inputFile) }} className={classNames('icon mbc-icon edit', Styles.deleteIcon)} tooltip-data={'Edit File'} />
                        </>
                      }
                      <i onClick={(e) => { e.stopPropagation(); showModal(inputFile?.id) }} className={classNames('icon delete', Styles.deleteIcon)} tooltip-data={'Delete File'} />
                    </div>
                  </td>
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
            mode={'yaml'}
            theme="solarized_dark"
            fontSize={16}
            showPrintMargin={false}
            showGutter={false}
            highlightActiveLine={false}
            value={blobURL}
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
    {showEdit && (
      <Modal
        title={`Preview - ${selectedConfigFile.name}`}
        onCancel={() => {
          setShowEdit(false); 
          setConfigEditorContent('') 
        }}
        modalWidth={'80vw'}
        showAcceptButton={false}
        showCancelButton={false}
        show={showEdit}
        content={
          <>
            <AceEditor
              width="100%"
              placeholder="Type here"
              name="configFilePreview"
              mode={'yaml'}
              theme="solarized_dark"
              fontSize={16}
              showPrintMargin={false}
              showGutter={false}
              highlightActiveLine={true}
              value={configEditorContent}
              onChange={onConfigCodeChange}
              style={{
                height: '65vh',
              }}
              setOptions={{
                useWorker: false,
                enableBasicAutocompletion: true,
                enableLiveAutocompletion: false,
                enableSnippets: false,
                showLineNumbers: true,
                tabSize: 2,
              }}
            />
            <button className={classNames('btn btn-tertiary')} onClick={handleUpdateConfig}>Update Config</button>
          </>
        }
      />
    )}
    </>
  );
}

export default InputFiles;