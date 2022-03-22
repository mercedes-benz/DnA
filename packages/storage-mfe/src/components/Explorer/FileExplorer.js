import React, { useEffect, useRef, useState, useMemo, useCallback } from 'react';
import Styles from './FileExplorer.scss';

import { useDispatch, useSelector } from 'react-redux';

import { FullFileBrowser, ChonkyActions, FileHelper } from 'chonky';

import Modal from 'dna-container/Modal';
import { deleteFiles, getFiles, moveFiles } from './redux/fileExplorer.actions';
import { useParams } from 'react-router-dom';

import { setChonkyDefaults } from 'chonky';
import { ChonkyIconFA } from 'chonky-icon-fontawesome';

// inform chonky on which iconComponent to use
setChonkyDefaults({ iconComponent: ChonkyIconFA });

const myFileActions = [
  ChonkyActions.UploadFiles,
  ChonkyActions.DownloadFiles,
  ChonkyActions.DeleteFiles,
  ChonkyActions.CreateFolder,
  ChonkyActions.MoveFiles,
];

const FileExplorer = () => {
  const dispatch = useDispatch();
  const files = useSelector((state) => state.fileExplorer.files);
  const { fileName } = useParams();

  const [currentFolderId, setCurrentFolderId] = useState(files?.rootFolderId);
  const [newFolderName, setNewFolderName] = useState('');
  const [folderName, setFolderName] = useState('');
  const [show, setShow] = useState(false);

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

  const currentFolderIdRef = useRef(currentFolderId);
  useEffect(() => {
    currentFolderIdRef.current = currentFolderId;
  }, [currentFolderId]);

  useEffect(() => {
    if (newFolderName) {
      createFolder(newFolderName);
    }
  }, [newFolderName]);

  useEffect(() => {
    dispatch({
      type: 'UPDATE_ROOT_NAME',
      payload: {
        rootId: files.rootFolderId,
        name: fileName,
      },
    });
  }, []);

  const createFolder = useCallback((newFolderName) => {
    const newFileMap = { ...files.fileMap };

    // Create the new folder
    const newFolderId = `new-folder-${newFolderName}`;

    newFileMap[newFolderId] = {
      id: newFolderId,
      name: newFolderName,
      isDir: true,
      modDate: new Date(),
      parentId: currentFolderIdRef.current,
      childrenIds: [],
      childrenCount: 0,
    };

    // Update parent folder to reference the new folder.
    const parent = newFileMap[currentFolderIdRef.current];

    newFileMap[currentFolderIdRef.current] = {
      ...parent,
      childrenIds: [...parent.childrenIds, newFolderId],
    };
    dispatch({
      type: 'CREATE_FOLDER',
      payload: newFileMap,
    });
    return newFileMap;
  }, []);

  const handleMoveFiles = (selectedFiles, source, destination) => {
    const newFileMap = { ...files.fileMap };
    const moveFileIds = new Set(selectedFiles.map((f) => f.id));

    // Delete files from their source folder.
    const newSourceChildrenIds = source.childrenIds.filter((id) => !moveFileIds.has(id));
    newFileMap[source.id] = {
      ...source,
      childrenIds: newSourceChildrenIds,
      childrenCount: newSourceChildrenIds.length,
    };

    // Add the files to their destination folder.
    const newDestinationChildrenIds = [...destination.childrenIds, ...selectedFiles.map((f) => f.id)];
    newFileMap[destination.id] = {
      ...destination,
      childrenIds: newDestinationChildrenIds,
      childrenCount: newDestinationChildrenIds.length,
    };

    // Finally, update the parent folder ID on the files from source folder
    // ID to the destination folder ID.
    selectedFiles.forEach((file) => {
      newFileMap[file.id] = {
        ...file,
        parentId: destination.id,
      };
    });
    dispatch(moveFiles(newFileMap));
  };

  const handleAction = (data) => {
    if (data.id === ChonkyActions.UploadFiles.id) {
      // TBD
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
      // TBD
    } else if (data.id === ChonkyActions.OpenFiles.id) {
      const { targetFile, files: sFiles } = data.payload;
      const fileToOpen = targetFile ?? sFiles[0];
      if (fileToOpen && FileHelper.isDirectory(fileToOpen)) {
        setCurrentFolderId(fileToOpen.id);
        const mockData = [
          {
            directory: false,
            lastModified: '2022-02-07T05:41:18.336Z',
            name: 'Time sheet_NAME_JAN_2022.xlsx',
            size: 292901,
          },
          {
            directory: false,
            lastModified: '2022-02-07T06:18:30.410Z',
            name: 'bulk_role_remove.csv',
            size: 1931,
          },

          {
            directory: true,
            lastModified: null,
            name: 'TestParent/',
            size: null,
          },
          {
            directory: true,
            lastModified: null,
            name: 'common/',
            size: null,
          },
        ];

        fileToOpen.name.includes('/') && dispatch(getFiles(mockData, files.fileMap, fileToOpen.id));
        return;
      }
    } else if (data.id === ChonkyActions.CreateFolder.id) {
      setShow(true);
    } else if (data.id === ChonkyActions.MoveFiles.id) {
      handleMoveFiles(data.payload.files, data.payload.source, data.payload.destination);
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
