import React, { useEffect, useRef, useState, useMemo, useCallback } from 'react';
import classNames from 'classnames';
import Styles from './FileExplorer.scss';

import { useDispatch, useSelector } from 'react-redux';

import { FullFileBrowser, ChonkyActions, FileHelper } from 'chonky';

import Modal from 'dna-container/Modal';
import { deleteFiles, downloadFoldersOrFiles, getFiles, setFiles } from './redux/fileExplorer.actions';
import { useParams } from 'react-router-dom';

import { setChonkyDefaults } from 'chonky';
import { ChonkyIconFA } from 'chonky-icon-fontawesome';

import FileUpload from './Upload';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';

import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/theme-solarized_dark';
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

import { bucketsObjectApi } from '../../apis/fileExplorer.api';
import { serializeFolderChain } from './Utils';
import { IMAGE_EXTNS, PREVIEW_NOT_ALLOWED_EXTNS } from '../Utility/constants';

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
  const [show, setShow] = useState(false);

  const currentFolderIdRef = useRef(currentFolderId);
  const uploadRef = useRef(null);
  const inputRef = useRef(null);

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
    show && inputRef.current.focus();
  }, [show]);

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

  const createFolder = useCallback(
    (files, newFolderName) => {
      const newFileMap = { ...files.fileMap };
      const folderExistsInCurrentDirectory = newFileMap[currentFolderIdRef.current]?.childrenIds?.some((item) =>
        item.includes(newFolderName),
      );
      if (folderExistsInCurrentDirectory) {
        const objectName = `${newFileMap[currentFolderIdRef.current].objectName}${newFolderName}'/'`;
        dispatch(
          getFiles(newFileMap, bucketName, {
            id: newFolderName,
            name: newFolderName,
            isDir: true,
            parentId: currentFolderIdRef.current,
            objectName: objectName.replace(`${bucketName}/`, ''),
          }),
        );
        setCurrentFolderId(newFolderName);
      } else {
        // Create the new folder
        newFileMap[newFolderName] = {
          id: newFolderName,
          name: newFolderName,
          isDir: true,
          parentId: currentFolderIdRef.current,
          objectName: `${newFolderName}/`,
        };

        // Update parent folder to reference the new folder.
        const parent = newFileMap[currentFolderIdRef.current];

        newFileMap[currentFolderIdRef.current] = {
          ...parent,
          childrenIds: [...(parent.childrenIds ? [...parent.childrenIds] : []), newFolderName],
        };

        dispatch({
          type: 'CREATE_FOLDER',
          payload: newFileMap,
        });

        setCurrentFolderId(newFolderName);
      }

      setNewFolderName('');
      return newFileMap;
    },
    [dispatch, bucketName],
  );

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

  const onOpenFolder = (fileToOpen) => {
    setCurrentFolderId(fileToOpen.id);

    const moveBackward = (files) => Object.prototype.hasOwnProperty.call(files, 'childrenCount');
    // create nested folder upload file and move back
    const inDraftFolderMoveBackward = (files) => Object.prototype.hasOwnProperty.call(files, 'childrenIds');

    const serializeObjectName = (files) => {
      const prefix = serializeFolderChain(folderChain);

      files['objectName'] = prefix[prefix?.length - 1];
      const objectNameArray = files.objectName?.split('/')?.filter((x) => !!x);
      const currentFolderIndex = objectNameArray.indexOf(fileToOpen.name);
      if (currentFolderIndex !== -1) {
        const objectName = objectNameArray.slice(0, currentFolderIndex + 1).join('/');
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
        if (folderChain?.length > 2 && copyFilesToOpen.objectName?.split('/')?.filter((x) => !!x).length === 1) {
          serializeObjectName(copyFilesToOpen);
        }
      }
      dispatch(getFiles(files.fileMap, bucketName, copyFilesToOpen));
    }

    return;
  };

  // on opening selected file show preview based on the file extensions
  const onOpenFile = (data, fileToOpen) => {
    if (data.state.selectedFiles.length === 1) {
      const extension = fileToOpen.name.toLowerCase()?.split('.')?.[1];
      const isImage = IMAGE_EXTNS.includes(extension);
      const allowedExt = !PREVIEW_NOT_ALLOWED_EXTNS.includes(extension);

      if (allowedExt) {
        ProgressIndicator.show();
        bucketsObjectApi
          .previewFiles(bucketName, fileToOpen.objectName, isImage)
          .then((res) => {
            let blobURL;
            if (isImage) {
              const url = window.URL.createObjectURL(
                new Blob([res.data], { 'Content-Type': res.headers['Content-Type'] }),
              );
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
  };

  const handleAction = (data) => {
    if (data.id === ChonkyActions.CreateFolder.id) {
      setShow(true);
    } else if (data.id === ChonkyActions.UploadFiles.id) {
      uploadRef.current.click();
    } else if (data.id === ChonkyActions.DeleteFiles.id) {
      onDelete(data);
    } else if (data.id === ChonkyActions.DownloadFiles.id) {
      data.state.selectedFiles?.forEach((item) => {
        // if selected multiple items, download each file
        dispatch(downloadFoldersOrFiles(bucketName, item));
      });
    } else if (data.id === ChonkyActions.OpenFiles.id) {
      const { targetFile, files: sFiles } = data.payload;
      const fileToOpen = targetFile ?? sFiles[0];
      if (fileToOpen && FileHelper.isDirectory(fileToOpen)) {
        // on opening directory
        onOpenFolder(fileToOpen);
      } else if (fileToOpen) {
        // on opening files
        onOpenFile(data, fileToOpen);
      }
    }
  };

  // display current folder path in create folder modal
  const folderPath = serializeFolderChain(folderChain);
  const currentFolderPath = [...new Set(folderPath.join('').split('/'))].join('/');

  const addFolderContent = (
    <div className={Styles.formGroup}>
      <div className={classNames('input-field-group', Styles.inputGrp)}>
        <label className={classNames('input-label', Styles.inputLabel)}>New Folder Name</label>
        <div className={Styles.folderPath}>
          <label className={classNames('input-label', Styles.inputLabel)}>{`${bucketName}/${currentFolderPath}`}</label>
        </div>
        <input
          type="text"
          className="input-field"
          required={true}
          maxLength={64}
          placeholder="Enter new folder path"
          autoComplete="off"
          onChange={(e) => setFolderName(e.target.value)}
          value={folderName}
          ref={inputRef}
        />
      </div>
    </div>
  );

  // set corresponding modes based on the file extensions
  const aceEditorMode = {
    java: 'java',
    js: 'javascript',
    jsx: 'javascript',
    ts: 'typescript',
    tsx: 'typescript',
    py: 'python',
    json: 'json',
    css: 'css',
    scss: 'scss',
    txt: 'text',
    yml: 'yaml',
    yaml: 'yaml',
  };
  const serializedFileName = showPreview.fileName.split('.').filter((x) => !!x);
  const fileExt = serializedFileName[serializedFileName.length - 1];

  return (
    <>
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
              modalWidth="80vw"
              showAcceptButton={false}
              showCancelButton={false}
              show={showPreview.modal}
              content={
                showPreview.isImage ? (
                  <img width={'100%'} className={Styles.previewImg} src={showPreview.blobURL} />
                ) : (
                  <AceEditor
                    width="100%"
                    name="storagePreview"
                    mode={aceEditorMode[fileExt] || 'plain_text'}
                    theme="solarized_dark"
                    fontSize={16}
                    showPrintMargin={false}
                    showGutter={false}
                    highlightActiveLine={false}
                    value={showPreview.blobURL}
                    readOnly={true}
                    style={{
                      height: '65vh',
                    }}
                    setOptions={{
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
          setNewFolderName(folderName);
          setFolderName('');
          setShow(false);
        }}
        showCancelButton={true}
        cancelButtonTitle="Cancel"
        onCancel={() => {
          setNewFolderName('');
          setFolderName('');
          setShow(false);
        }}
        modalWidth={'60%'}
        buttonAlignment="right"
        show={show}
        content={addFolderContent}
        scrollableContent={false}
      />
    </>
  );
};

export default FileExplorer;
