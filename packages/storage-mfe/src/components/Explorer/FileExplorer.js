import React, { useEffect, useRef, useState, useMemo, useCallback } from 'react';
import classNames from 'classnames';
import Styles from './FileExplorer.scss';

import { useDispatch, useSelector } from 'react-redux';

import ConfirmModal from 'dna-container/ConfirmModal';
import Modal from 'dna-container/Modal';

import { FullFileBrowser, ChonkyActions, FileHelper } from 'chonky';

import { deleteFiles, downloadFoldersOrFiles, getFiles, setFiles } from './redux/fileExplorer.actions';
import { useParams } from 'react-router-dom';

import { setChonkyDefaults } from 'chonky';
import { ChonkyIconFA } from 'chonky-icon-fontawesome';
import { getDateTimeFromTimestamp } from '../Utility/utils';

import FileUpload from './Upload';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';

import { Document, Page } from 'react-pdf/dist/esm/entry.webpack';
import 'react-pdf/dist/esm/Page/AnnotationLayer.css';

import AceEditor from 'react-ace';
//import theme
import 'ace-builds/src-noconflict/theme-solarized_dark';
//import modes
import 'ace-builds/src-noconflict/mode-typescript';
import 'ace-builds/src-noconflict/mode-javascript';
import 'ace-builds/src-noconflict/mode-java';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/mode-css';
import 'ace-builds/src-noconflict/mode-scss';
import 'ace-builds/src-noconflict/mode-text';
import 'ace-builds/src-noconflict/mode-yaml';
import 'ace-builds/src-noconflict/mode-plain_text';
import 'ace-builds/src-noconflict/mode-html';
import 'ace-builds/src-noconflict/mode-markdown';
import 'ace-builds/src-noconflict/mode-less';
import 'ace-builds/src-noconflict/mode-sql';
import 'ace-builds/src-noconflict/mode-kotlin';
import 'ace-builds/src-noconflict/mode-golang';

import { bucketsObjectApi } from '../../apis/fileExplorer.api';
import { serializeFolderChain } from './Utils';
import { aceEditorMode, IMAGE_EXTNS, PREVIEW_ALLOWED_EXTNS } from '../Utility/constants';
import { history } from '../../store/storeRoot';

import { v4 as uuidv4 } from 'uuid';

// inform chonky on which iconComponent to use
setChonkyDefaults({ iconComponent: ChonkyIconFA });

// const UploadFolder = defineFileAction({
//   id: 'upload_folder',
//   button: {
//     name: 'Upload folder',
//     toolbar: true,
//     tooltip: 'Upload folder',
//     icon: 'upload',
//   },
// });

const FileExplorer = () => {
  const dispatch = useDispatch();
  const { bucketPermission } = useSelector((state) => state.fileExplorer);

  const { bucketName } = useParams();

  const { files } = useSelector((state) => state.fileExplorer);

  const [currentFolderId, setCurrentFolderId] = useState(bucketName);
  const [newFolderName, setNewFolderName] = useState('');
  const [folderName, setFolderName] = useState('');
  const [folderNameError, setFolderNameError] = useState('');
  const [showCreateNewFolderModal, setShowCreateNewFolderModal] = useState(false);

  const [pdfDocsNumPages, setPDFDocsNumPages] = useState(0);

  // pdf password protected states
  const [pdfPassword, setPdfPassword] = useState('');
  const [pdfcallback, setPdfCallback] = useState(() => () => {});
  const [showPdfModal, setPdfModal] = useState(false);
  const [pdfPwdError, setPDFPwdError] = useState('');
  const [showPwdText, setShowPwdText] = useState(false);

  // track of newly created folder name
  const [newlyCreatedFolder, setNewlyCreatedFolder] = useState('');

  const [folderExists, setFolderExists] = useState({
    modal: false,
    data: {},
  });
  const [isEmptyFolder, setIsEmptyFolder] = useState({
    modal: false,
    fileToOpen: {},
  });

  const [showDeleteModal, setShowDeleteModal] = useState({
    modal: false,
    data: {},
  });

  const currentFolderIdRef = useRef(currentFolderId);
  const uploadRef = useRef(null);
  const inputRef = useRef(null);
  const pwdInputRef = useRef(null);

  const [showPreview, setPreview] = useState({
    modal: false,
    fileName: '',
    isImage: false,
    blobURL: '',
  });

  const myFileActions = [
    ...(bucketPermission.write ? [ChonkyActions.UploadFiles] : []),
    // ...(bucketPermission.write ? [UploadFolder] : []),
    ...(bucketPermission.write ? [ChonkyActions.CreateFolder] : []),
    ChonkyActions.DownloadFiles,
    ...(bucketPermission.write ? [ChonkyActions.DeleteFiles] : []),
  ];

  // localization
  const i18n = {
    // set date format
    formatters: {
      formatFileModDate: (intl, file) => {
        const safeModDate = FileHelper.getModDate(file);
        if (safeModDate) {
          return `${getDateTimeFromTimestamp(safeModDate, '.')}`;
        } else {
          return null;
        }
      },
    },
  };

  useEffect(() => {
    currentFolderIdRef.current = currentFolderId;
  }, [currentFolderId]);

  useEffect(() => {
    if (newFolderName) {
      createFolder(files, newFolderName);
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [newFolderName]);

  useEffect(() => {
    if (!files?.rootFolderId) {
      dispatch(setFiles(bucketName, false));
    }
  }, [dispatch, bucketName, files]);

  useEffect(() => {
    showCreateNewFolderModal && inputRef.current.focus();
  }, [showCreateNewFolderModal]);

  const useFolderChain = (fileMap, currentFolderId) => {
    return useMemo(() => {
      const currentFolder = fileMap?.[currentFolderId];
      const folderChain = [currentFolder];

      let parentId = currentFolder?.parentId;
      while (parentId) {
        const parentFile = fileMap[parentId];
        if (parentFile) {
          folderChain.unshift(parentFile);
          parentId = parentFile.parentId;
        } else {
          break;
        }
      }

      return folderChain;
    }, [currentFolderId, fileMap]);
  };
  const folderChain = useFolderChain(files?.fileMap, currentFolderId);

  const openExistingFolder = (files, newFolderName) => {
    const newFileMap = { ...files.fileMap };
    const objectName = `${newFileMap[currentFolderIdRef.current].objectName}${newFolderName}/`;
    setFolderExists({ modal: false });
    const expression = `^${bucketName}`;
    const regExp = new RegExp(expression);
    dispatch(
      getFiles(newFileMap, bucketName, {
        id: objectName.replaceAll('/', ''),
        name: newFolderName,
        isDir: true,
        parentId: currentFolderIdRef.current,
        objectName: regExp.test(objectName) ? objectName.replace(`${bucketName}/`, '') : objectName,
      }),
    );
    setCurrentFolderId(objectName.replaceAll('/', ''));
  };

  const createFolder = useCallback(
    (files, newFolderName) => {
      const newFileMap = { ...files.fileMap };
      const folderExistsInCurrentDirectory = newFileMap[currentFolderIdRef.current]?.childrenIds?.some(
        (item) =>
          item === newFolderName ||
          item === newFileMap[currentFolderIdRef.current].objectName.split('/').join('') + newFolderName,
      );

      if (folderExistsInCurrentDirectory) {
        setFolderExists({ modal: true, data: { files, newFolderName } });
      } else {
        // Create the new folder
        // append unique ids to handle duplicate folder names
        const newFolder = (newFolderName + uuidv4()).replaceAll('-', '');

        newFileMap[newFolder] = {
          id: newFolder,
          name: newFolderName,
          isDir: true,
          parentId: currentFolderIdRef.current,
          objectName: `${newFolderName}/`,
        };

        // Update parent folder to reference the new folder.
        const parent = newFileMap[currentFolderIdRef.current];

        newFileMap[currentFolderIdRef.current] = {
          ...parent,
          childrenIds: [...(parent.childrenIds ? [...parent.childrenIds] : []), newFolder],
        };

        dispatch({
          type: 'CREATE_FOLDER',
          payload: newFileMap,
        });

        setCurrentFolderId(newFolder);
        setNewlyCreatedFolder(newFolder);
      }

      setNewFolderName('');
      return newFileMap;
    },
    [dispatch],
  );

  const goBack = () => {
    history.replace('/');
  };

  const onDelete = (data) => {
    // Delete the files
    const newFileMap = { ...files.fileMap };

    const fileList = [];
    data.state.selectedFiles?.forEach((file) => {
      // Delete file from the file map.
      delete newFileMap[file.id];

      // Update the parent folder to make sure it doesn't try to load the
      // file we just deleted.
      if (file.parentId) {
        const parent = newFileMap[file.parentId];
        const newChildrenIds = parent.childrenIds.filter((id) => id !== file.id);
        newFileMap[file.parentId] = {
          ...parent,
          childrenIds: newChildrenIds,
          childrenCount: newChildrenIds.length,
        };

        // Update root folder childrens list
        if (newFileMap[file.parentId].isDir && newFileMap[file.parentId].childrenCount === 0) {
          if (newFileMap[file.parentId].parentId) {
            setCurrentFolderId(newFileMap[file.parentId]?.parentId);
            const parent = newFileMap[newFileMap[file.parentId]?.parentId];
            const newChildrenIds = parent?.childrenIds?.filter((id) => id !== file.parentId);

            newFileMap[newFileMap[file.parentId]?.parentId] = {
              ...parent,
              childrenIds: newChildrenIds,
              childrenCount: newChildrenIds?.length,
            };
            delete newFileMap[file.parentId];
          }
        }
      }
      fileList.push(file.objectName);
    });

    dispatch(deleteFiles(bucketName, fileList.join(','), newFileMap));
  };

  const onOpenFolder = (fileToOpen, isEmptyFolder = false) => {
    setCurrentFolderId(fileToOpen.id);

    const moveBackward = (files) => Object.prototype.hasOwnProperty.call(files, 'childrenCount');
    // create nested folder upload file and move back
    const inDraftFolderMoveBackward = (files) => Object.prototype.hasOwnProperty.call(files, 'childrenIds');

    const serializeObjectName = (files) => {
      files['objectName'] = fileToOpen.objectName;
      const objectNameArray = files.objectName?.split('/')?.filter((x) => x);
      const currentFolderIndex = objectNameArray.indexOf(fileToOpen.name);
      const folderPath = serializeFolderChain(folderChain);
      if (currentFolderIndex === 0) {
        const draftInitiatedFolderPath = folderChain.filter((item) => item?.childrenCount);
        const draftInitiatedFolderPathObjectName =
          draftInitiatedFolderPath[draftInitiatedFolderPath.length - 1]?.objectName;
        const index = folderPath.findIndex((item) => item === draftInitiatedFolderPathObjectName);

        if (!fileToOpen.childrenCount) {
          folderPath.push(fileToOpen.objectName);
          const folderPathArray = folderPath;

          const folder = folderPathArray.splice(index > 0 ? index : 0)?.join('');

          files['objectName'] = folder;
        } else {
          files['objectName'] = fileToOpen.objectName;
        }
      } else if (currentFolderIndex !== -1) {
        // const objectName = objectNameArray.slice(0, currentFolderIndex + 1).join('/');
        const objectName = objectNameArray.join('/');
        files['objectName'] = `${objectName}/`;
      }
    };

    if (fileToOpen.id === bucketName) {
      dispatch(setFiles(bucketName, false));
    } else if (fileToOpen?.objectName) {
      const copyFilesToOpen = { ...fileToOpen };

      if (moveBackward(copyFilesToOpen)) {
        serializeObjectName(copyFilesToOpen);
      } else if (inDraftFolderMoveBackward(copyFilesToOpen)) {
        // still in draft stage i.e newly created folders
        if (folderChain?.length > 2 && copyFilesToOpen.objectName?.split('/')?.filter((x) => x).length === 1) {
          const draftInitiatedFolderPath = folderChain.filter((item) => item?.childrenCount);
          const folderPath = serializeFolderChain(folderChain);
          const draftInitiatedFolderPathObjectName =
            draftInitiatedFolderPath[draftInitiatedFolderPath.length - 1]?.objectName;
          const index = folderPath.findIndex((item) => item === draftInitiatedFolderPathObjectName);
          folderPath.pop();

          const folderPathArray = folderPath;
          if (index === -1) {
            copyFilesToOpen.objectName = folderPathArray.join('');
          } else if (folderPathArray[index] === undefined) {
            const index = folderPathArray.lastIndexOf(fileToOpen.objectName);
            copyFilesToOpen.objectName = folderPathArray.slice(index > 0 ? index - 1 : 0, index + 1).join('');
          } else {
            copyFilesToOpen.objectName = folderPathArray.slice(index)?.join('');
          }
        }
      }

      if (isEmptyFolder) {
        const newFileMap = { ...files.fileMap };
        const parent = newFileMap[newFileMap[currentFolderId]?.parentId];

        const newChildrenIds = parent.childrenIds?.filter((id) => id !== currentFolderId);
        const copyParent = { ...parent };

        delete copyParent.childrenIds;
        newFileMap[copyParent.id] = {
          ...parent,
          childrenIds: newChildrenIds,
          childrenCount: newChildrenIds?.length,
        };

        delete newFileMap[currentFolderId];

        dispatch(getFiles(newFileMap, bucketName, copyFilesToOpen));
      } else {
        dispatch(getFiles(files.fileMap, bucketName, copyFilesToOpen));
      }
    }
    setNewlyCreatedFolder('');
    return;
  };

  // on opening selected file show preview based on the file extensions
  const onOpenFile = (data, fileToOpen) => {
    if (data.state.selectedFiles.length) {
      if (data.state.selectedFiles.length === 1) {
        const extension = fileToOpen.name.toLowerCase()?.split('.')?.[1];
        const isImage = IMAGE_EXTNS.includes(extension);
        const allowedExt = PREVIEW_ALLOWED_EXTNS.includes(extension);
        const isPDF = extension === 'pdf';

        if (allowedExt) {
          ProgressIndicator.show();
          bucketsObjectApi
            .previewFiles(bucketName, fileToOpen.objectName, extension)
            .then((res) => {
              let blobURL;
              if (isImage || isPDF) {
                const url = window.URL.createObjectURL(new Blob([res.data], { type: res.headers['Content-Type'] }));
                blobURL = url;
              } else {
                blobURL = res.data;
              }
              setPreview({
                fileName: fileToOpen.name,
                isImage,
                modal: true,
                blobURL,
              });
              ProgressIndicator.hide();
            })
            .catch(() => {
              ProgressIndicator.hide();
              Notification.show('Error while previewing file. Please try again later.', 'alert');
            });
        } else {
          Notification.show('Preview not supported', 'alert');
        }
      } else {
        Notification.show('Open selection is for one file at a time.', 'alert');
      }
    }
  };

  const handleAction = (data) => {
    if (data.id === ChonkyActions.CreateFolder.id) {
      setShowCreateNewFolderModal(true);
    } else if (data.id === ChonkyActions.UploadFiles.id) {
      uploadRef.current.click();
    } else if (data.id === ChonkyActions.DeleteFiles.id) {
      setShowDeleteModal({
        modal: true,
        data,
      });
    } else if (data.id === ChonkyActions.DownloadFiles.id) {
      data.state.selectedFiles?.forEach((item) => {
        // if selected multiple items, download each file
        dispatch(downloadFoldersOrFiles(bucketName, item));
      });
    } else if (data.id === ChonkyActions.OpenFiles.id) {
      const { targetFile, files: sFiles } = data.payload;
      const fileToOpen = targetFile ?? sFiles[0];
      if (fileToOpen && FileHelper.isDirectory(fileToOpen)) {
        if (files.fileMap[currentFolderId].isDir && files.fileMap[currentFolderId].childrenCount === 0) {
          setIsEmptyFolder({ modal: true, fileToOpen });
        } else if (files.fileMap[newlyCreatedFolder] && !files.fileMap?.[newlyCreatedFolder]?.childrenIds?.length) {
          setIsEmptyFolder({ modal: true, fileToOpen });
        } else {
          // on opening directory
          onOpenFolder(fileToOpen);
        }
      } else if (fileToOpen) {
        // on opening files
        onOpenFile(data, fileToOpen);
      }
    }
  };

  // display current folder path in create folder modal
  const folderPath = serializeFolderChain(folderChain);
  let currentFolderPath = '';
  // const currentFolderPath = [...new Set(folderPath.join('').split('/'))].join('/');
  const existingFolder = folderChain.filter((item) => item?.childrenCount && item.objectName);
  if (existingFolder?.length && existingFolder.length !== 1) {
    const existingFolderIndex = folderPath.indexOf(existingFolder[existingFolder.length - 1]?.objectName);

    currentFolderPath = folderPath.slice(existingFolderIndex).join('');
  } else {
    currentFolderPath = folderPath.join('');
  }

  const folderNameValidation = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (folderName === '') {
      setFolderNameError(errorMissingEntry);
      formValid = false;
    }
    const currentFolderPathArr = currentFolderPath?.split('/')?.filter((x) => x);

    if (folderName === currentFolderPath.replace('/', '')) {
      setFolderNameError(`Currently in ${folderName} folder`);
      formValid = false;
    }
    if (folderName === currentFolderId) {
      setFolderNameError(`Currently in ${folderName} folder`);
      formValid = false;
    }

    if (!currentFolderPath && folderName === bucketName) {
      setFolderNameError(`Currently in ${folderName} folder`);
      formValid = false;
    }

    if (folderName === currentFolderPathArr[currentFolderPathArr.length - 1]) {
      setFolderNameError(`Currently in ${folderName} folder`);
      formValid = false;
    }

    return formValid;
  };

  const addFolderContent = (
    <div className={Styles.formGroup}>
      <div className={classNames('input-field-group', Styles.inputGrp, folderNameError?.length ? 'error' : '')}>
        <label className={classNames('input-label', Styles.inputLabel)}>
          New Folder Name <sup>*</sup>
        </label>
        <div className={Styles.folderPath}>
          <label
            className={classNames('input-label', Styles.inputLabel, Styles.folderPathLabel)}
          >{`${bucketName}/${currentFolderPath}`}</label>
        </div>
        <div style={{ maxHeight: 40 }}>
          <input
            type="text"
            className="input-field"
            required={true}
            maxLength={64}
            placeholder="Enter new folder path"
            autoComplete="off"
            onChange={(e) => {
              setFolderName(e.target.value.replace('/', ''));
              if (e.target.value) setFolderNameError('');
            }}
            value={folderName}
            ref={inputRef}
          />
          <span className={classNames('error-message', folderNameError?.length ? '' : 'hide')}>{folderNameError}</span>
        </div>
      </div>
    </div>
  );

  const folderExistsContent = (
    <div>
      <h4>Folder already exists. Do you wish to open the folder?</h4>
    </div>
  );

  const folderExistsClose = () => {
    setFolderExists({ modal: false });
  };

  const folderExistsAccept = () => {
    const { files, newFolderName } = folderExists.data;
    openExistingFolder(files, newFolderName);
    setFolderExists({ modal: false, data: {} });
  };

  const emptyFolderWarningContent = (
    <div>
      <h4>Folder is empty.</h4>
      <h5>Empty folders will be removed.</h5>
    </div>
  );

  const emptyFolderClose = () => {
    setIsEmptyFolder({ modal: false, fileToOpen: {} });
  };

  const emptyFolderAccept = () => {
    const { fileToOpen } = isEmptyFolder;
    // on opening directory
    onOpenFolder(fileToOpen, true);
    setIsEmptyFolder({ modal: false, fileToOpen });
  };

  const deleteFileOrFolderContent = (
    <div>
      <h4>Are you sure you want to delete selected file(s)/folder(s)?</h4>
    </div>
  );

  const deleteClose = () => {
    setShowDeleteModal({ modal: false, data: {} });
  };

  const deleteAccept = () => {
    onDelete(showDeleteModal.data);
    setShowDeleteModal({ modal: false });
  };

  const serializedFileName = showPreview.fileName?.split('.')?.filter((x) => x);
  const fileExt = serializedFileName[serializedFileName.length - 1];
  const isPDF = fileExt === 'pdf';

  const passwordInputContent = (
    <div className={classNames('input-field-group', pdfPwdError?.length ? 'error' : '')}>
      <label className={classNames('input-label', Styles.pwdLabel)}>Enter the password to open this PDF file.</label>
      <div className={Styles.pdfPwdContainer}>
        <input
          className="input-field"
          type={showPwdText ? 'text' : 'password'}
          value={pdfPassword || ''}
          onChange={(e) => setPdfPassword(e.target.value)}
          style={{ width: '350px' }}
          ref={pwdInputRef}
        />
        {showPwdText ? (
          <i className={'icon mbc-icon visibility-hide'} onClick={() => setShowPwdText(false)} tooltip-data="Hide" />
        ) : (
          <i className={'icon mbc-icon visibility-show '} onClick={() => setShowPwdText(true)} tooltip-data="Show" />
        )}
      </div>
      <span className={classNames('error-message', pdfPwdError?.length ? '' : 'hide')}>{pdfPwdError}</span>
    </div>
  );

  const onPDFLoadSuccess = ({ numPages }) => {
    setPDFDocsNumPages(numPages);
  };

  const onPdfInputCancel = () => {
    setPdfModal(false);
    setPdfPassword('');
    setPreview({
      modal: false,
      fileName: '',
      isImage: false,
      blobURL: '',
    });
    setPDFPwdError('');
  };
  const onPdfInputAccept = () => {
    setPdfModal(false);
    pdfcallback(pdfPassword);
    setPDFPwdError('');
    if (!pdfPassword) {
      setPDFPwdError('Please enter the password');
    }
  };

  const handlePassword = (callback, reason) => {
    setPdfCallback(() => callback);
    setPdfModal(true);
    pwdInputRef.current.focus();
    switch (reason) {
      case 1:
        setPdfPassword('');
        break;
      case 2:
        setPDFPwdError('Invalid password. Please try again.');
        break;
      default:
    }
  };

  return (
    <>
      <button className={classNames('btn btn-text back arrow', Styles.backBtn)} type="submit" onClick={goBack}>
        Back
      </button>
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>{`Bucket - ${bucketName}`}</h3>
          </div>
        </div>
        <div className={'explorer-content'}>
          <FileUpload uploadRef={uploadRef} bucketName={bucketName} folderChain={folderChain} />
          <FullFileBrowser
            files={files?.fileMap?.[currentFolderId]?.childrenIds?.map((item) => files.fileMap[item])}
            fileActions={myFileActions}
            onFileAction={handleAction}
            folderChain={folderChain}
            darkMode={true}
            defaultFileViewActionId={ChonkyActions.EnableListView.id}
            disableDragAndDrop={true}
            i18n={i18n}
          />
          {showPreview.modal && (
            <Modal
              title={`Preview - ${showPreview.fileName}`}
              onCancel={() => {
                setPreview({
                  ...showPreview,
                  modal: false,
                });
              }}
              modalWidth={isPDF ? '45vw' : '80vw'}
              showAcceptButton={false}
              showCancelButton={false}
              show={showPreview.modal}
              content={
                showPreview.isImage ? (
                  <img width={'100%'} className={Styles.previewImg} src={showPreview.blobURL} />
                ) : isPDF ? (
                  <div id={'pdfDoc'} className="mbc-scroll">
                    <Document
                      file={showPreview.blobURL}
                      onLoadSuccess={onPDFLoadSuccess}
                      externalLinkTarget="_blank"
                      onPassword={handlePassword}
                    >
                      {Array.from(new Array(pdfDocsNumPages), (el, index) => (
                        <Page className={'pdf-doc-page'} key={`page_${index + 1}`} pageNumber={index + 1} />
                      ))}
                    </Document>
                  </div>
                ) : (
                  <AceEditor
                    width="100%"
                    name="storagePreview"
                    mode={aceEditorMode[fileExt]}
                    theme="solarized_dark"
                    fontSize={16}
                    showPrintMargin={false}
                    showGutter={false}
                    highlightActiveLine={false}
                    value={
                      typeof showPreview.blobURL === 'object' && fileExt === 'json'
                        ? JSON.stringify(showPreview.blobURL, undefined, 2)
                        : showPreview.blobURL
                    }
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
                )
              }
            />
          )}
        </div>
      </div>
      <Modal
        title={'Create New Folder'}
        showAcceptButton={true}
        acceptButtonTitle="Create"
        onAccept={() => {
          if (folderNameValidation()) {
            setNewFolderName(folderName);
            setFolderName('');
            setShowCreateNewFolderModal(false);
          }
        }}
        showCancelButton={true}
        cancelButtonTitle="Cancel"
        onCancel={() => {
          setNewFolderName('');
          setFolderName('');
          setFolderNameError('');
          setShowCreateNewFolderModal(false);
        }}
        modalWidth={'60%'}
        buttonAlignment="right"
        show={showCreateNewFolderModal}
        content={addFolderContent}
        scrollableContent={false}
      />
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal.modal}
        content={deleteFileOrFolderContent}
        onCancel={deleteClose}
        onAccept={deleteAccept}
      />
      <ConfirmModal
        title={''}
        acceptButtonTitle="Proceed"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={folderExists.modal}
        content={folderExistsContent}
        onCancel={folderExistsClose}
        onAccept={folderExistsAccept}
      />
      <ConfirmModal
        title={''}
        acceptButtonTitle="Proceed"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={isEmptyFolder.modal}
        content={emptyFolderWarningContent}
        onCancel={emptyFolderClose}
        onAccept={emptyFolderAccept}
      />
      <Modal
        title={'Password'}
        hiddenTitle={true}
        acceptButtonTitle="Confirm"
        cancelButtonTitle="Cancel"
        showAcceptButton={true}
        showCancelButton={true}
        show={showPdfModal}
        content={passwordInputContent}
        onCancel={onPdfInputCancel}
        onAccept={onPdfInputAccept}
        modalWidth={'30vw'}
        buttonAlignment={'center'}
      />
    </>
  );
};

export default FileExplorer;
