import React, { useEffect, useRef, useState, useMemo, useCallback } from 'react';
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

// inform chonky on which iconComponent to use
setChonkyDefaults({ iconComponent: ChonkyIconFA });

const FileExplorer = () => {
  const dispatch = useDispatch();
  const { bucketPermission, isLoading } = useSelector((state) => state.fileExplorer);

  const { fileName } = useParams();

  const { files } = useSelector((state) => state.fileExplorer);

  const [currentFolderId, setCurrentFolderId] = useState(fileName);
  const [newFolderName, setNewFolderName] = useState('');
  const [folderName, setFolderName] = useState('');
  const [show, setShow] = useState(false);

  const currentFolderIdRef = useRef(currentFolderId);
  const uploadRef = useRef(null);
  const inputRef = useRef(null);

  const myFileActions = [
    ...(bucketPermission.write ? [ChonkyActions.UploadFiles] : []),
    ChonkyActions.DownloadFiles,
    ...(bucketPermission.write ? [ChonkyActions.DeleteFiles] : []),
    ...(bucketPermission.write ? [ChonkyActions.CreateFolder] : []),
  ];

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
      dispatch(setFiles(fileName, false));
    }
  }, [dispatch, fileName, files]);

  useEffect(() => {
    show && inputRef.current.focus();
  }, [show]);

  useEffect(() => {
    if (isLoading) {
      ProgressIndicator.show();
    } else {
      ProgressIndicator.hide();
    }
  }, [isLoading]);

  const createFolder = useCallback(
    (files, newFolderName) => {
      const newFileMap = { ...files.fileMap };

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
      return newFileMap;
    },
    [dispatch],
  );

  const handleAction = (data) => {
    if (data.id === ChonkyActions.UploadFiles.id) {
      uploadRef.current.click();
    } else if (data.id === ChonkyActions.DeleteFiles.id) {
      // Delete the files
      const newFileMap = { ...files.fileMap };

      data.state.selectedFilesForAction.forEach((file) => {
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
        }
      });
      dispatch(deleteFiles(newFileMap));
    } else if (data.id === ChonkyActions.DownloadFiles.id) {
      data.state.selectedFiles?.forEach((item) => {
        // if selected multiple items, download each file
        dispatch(downloadFoldersOrFiles(fileName, item));
      });
    } else if (data.id === ChonkyActions.OpenFiles.id) {
      const { targetFile, files: sFiles } = data.payload;
      const fileToOpen = targetFile ?? sFiles[0];
      if (fileToOpen && FileHelper.isDirectory(fileToOpen)) {
        setCurrentFolderId(fileToOpen.id);
        const moveBackward = (files) => Object.prototype.hasOwnProperty.call(files, 'childrenCount');
        // create nested folder upload file and move back
        const moveBackwardNestedFolder = (files) => Object.prototype.hasOwnProperty.call(files, 'childrenIds');

        const serializeObjectName = (files) => {
          const prefix = folderChain
            .map((item, index) => {
              if (index === 0) {
                return '';
              } else {
                return item.objectName;
              }
            })
            .filter((x) => !!x); //filter falsy value

          files['objectName'] = prefix[prefix.length - 1];
          const objectNameArray = files.objectName.split('/').filter((x) => !!x);
          const currentFolderIndex = objectNameArray.indexOf(fileToOpen.name);
          if (currentFolderIndex !== -1) {
            const objectName = objectNameArray.slice(0, currentFolderIndex + 1).join('/');
            files['objectName'] = `${objectName}/`;
          }
        };

        if (fileToOpen.id === fileName) {
          dispatch(setFiles(fileName, false));
        } else if (fileToOpen?.objectName) {
          const copyFilesToOpen = { ...fileToOpen };

          if (moveBackward(copyFilesToOpen)) {
            serializeObjectName(copyFilesToOpen);
          } else if (moveBackwardNestedFolder(copyFilesToOpen)) {
            if (folderChain.length > 2 && copyFilesToOpen.objectName?.split('/')?.filter((x) => !!x).length === 1) {
              serializeObjectName(copyFilesToOpen);
            }
          }

          dispatch(getFiles(files.fileMap, fileName, copyFilesToOpen));
        }

        return;
      }
    } else if (data.id === ChonkyActions.CreateFolder.id) {
      setShow(true);
    }
  };

  const addFolderContent = (
    <div className="formGroup">
      <div className={'inputGrp input-field-group'}>
        <label className={'inputLabel input-label'}>New Folder Name</label>
        <label className={'inputLabel input-label folderPath'}>{`${files?.fileMap?.[currentFolderId]?.name} /`}</label>
        <input
          type="text"
          className="input-field"
          required={true}
          id="PrjName"
          maxLength={64}
          placeholder="Type here"
          autoComplete="off"
          onChange={(e) => setFolderName(e.target.value)}
          value={folderName}
          ref={inputRef}
        />
      </div>
    </div>
  );

  return (
    <>
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>{`Bucket - ${fileName}`}</h3>
          </div>
        </div>
        <div className={'content'}>
          <FileUpload uploadRef={uploadRef} bucketName={fileName} currentFolderId={folderChain} />
          <FullFileBrowser
            files={files?.fileMap?.[currentFolderId]?.childrenIds?.map((item) => files.fileMap[item])}
            fileActions={myFileActions}
            onFileAction={handleAction}
            folderChain={folderChain}
            darkMode={true}
            defaultFileViewActionId={ChonkyActions.EnableListView.id}
          />
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
