import React, { useState } from 'react';
import Upload from 'rc-upload';

import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { baseURL } from '../../server/api';
import { getFiles } from './redux/fileExplorer.actions';
import { useDispatch, useSelector } from 'react-redux';
import Notification from '../../common/modules/uilab/js/src/notification';
import { getFilePath, setObjectKey } from './Utils';
import { SESSION_STORAGE_KEYS } from '../Utility/constants';
import { refreshToken } from 'dna-container/RefreshToken';

const FileUpload = ({ uploadRef, bucketName, folderChain, enableFolderUpload = false }) => {
  const dispatch = useDispatch();
  const { files } = useSelector((state) => state.fileExplorer);

  const [fileListInFolder, setFolderFiles] = useState([]);

  const objectPath = getFilePath(folderChain);

  const onSuccess = (response, uploadFile) => {
    const currentFolder = folderChain?.[folderChain?.length - 1];
    const folderId = {
      objectName: objectPath,
      id: currentFolder?.id,
      name: currentFolder?.name,
      parentId: currentFolder?.parentId,
    };

    if (enableFolderUpload) {
      const finalFile = fileListInFolder[fileListInFolder.length - 1]?.webkitRelativePath; // last file uploaded
      const folderName = uploadFile.webkitRelativePath?.split('/')[0];
      if (finalFile === uploadFile.webkitRelativePath) {
        Notification.show(`${folderName} uploaded successfully.`);
      }
    } else {
      Notification.show(`${uploadFile.name} uploaded successfully.`);
    }

    dispatch(getFiles(files.fileMap, bucketName, folderId));
  };
  const onError = (err, errResponse) => {
    if (errResponse.errors?.length) {
      Notification.show(errResponse.errors[0].message, 'alert');
    }
    ProgressIndicator.hide();
  };

  const uploaderProps = {
    headers: {
      Authorization: sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT),
    },
    action: `${baseURL}/buckets/${bucketName}/upload`,
    data: (file) => {
      const relativeFilePath = file.webkitRelativePath;
      const relativeFilePathIndex = relativeFilePath.lastIndexOf('/');
      const folderPath = relativeFilePath.substring(0, relativeFilePathIndex);
      return {
        ...(!enableFolderUpload
          ? { prefix: folderChain.length === 1 ? '/' : objectPath }
          : { prefix: folderChain.length === 1 ? folderPath : objectPath + folderPath }),
      };
    },
    method: 'POST',
    onStart: () => {
      ProgressIndicator.show(1);
    },
    multiple: true,
    onSuccess,
    onProgress(step) {
      ProgressIndicator.show(Math.round(step.percent));
    },
    beforeUpload: async (file, fileList) => {
      if (process.env.NODE_ENV === 'production') {
        const jwt = sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
        await refreshToken(jwt);
      }
      let isValid = true;
      setFolderFiles(fileList);
      // Folder upload
      if (enableFolderUpload) {
        let nestedFolder = false;
        const folderName = file.webkitRelativePath?.split('/')[0];
        const finalFilePath = fileList[fileList.length - 1].webkitRelativePath; // last file uploaded
        const isLastFileUploaded = finalFilePath === file.webkitRelativePath;

        if (objectPath) {
          const objectName = objectPath.replaceAll('/', '');
          const key = setObjectKey(objectName + folderName);

          // check whether the file already exists
          if (Object.prototype.hasOwnProperty.call(files.fileMap, key)) {
            if (isLastFileUploaded) Notification.show(`Folder not uploaded. ${folderName} already exists`, 'alert');
            nestedFolder = true;
            isValid = false;
          }
        } else {
          Object.keys(files.fileMap).find((item) => {
            const key = setObjectKey(folderName);
            if (item === key) {
              if (isLastFileUploaded) Notification.show(`Folder not uploaded. ${folderName} already exists`, 'alert');
              nestedFolder = true;
              isValid = false;
              return false;
            }
          });
        }
        if (file.name === '.DS_Store') {
          isValid = false;
        } else if (!nestedFolder && /(publishedparquet|published parquet)/gi.test(file.webkitRelativePath)) {
          isValid = false;
          Notification.show(
            `Folder name containing "Published Parquet" text is not allowed.\n${file.webkitRelativePath}`,
            'alert',
          );
        }
      } else {
        // file Upload
        // nested folder
        if (objectPath) {
          const objectName = objectPath.replaceAll('/', '');
          const key = setObjectKey(objectName + file.name);
          // check whether the file already exists
          if (Object.prototype.hasOwnProperty.call(files.fileMap, key)) {
            Notification.show(`File not uploaded. ${file.name} already exists`, 'alert');
            isValid = false;
          }
        } else {
          // root folder
          Object.entries(files.fileMap).find(([, objVal]) => {
            // check whether the file name already exists in root
            if (objVal.name === file.name) {
              Notification.show(`File not uploaded. ${file.name} already exists`, 'alert');
              isValid = false;
              return false;
            }
          });
        }
      }
      return isValid;
    },
    onError,
    ...(enableFolderUpload
      ? {
        directory: true,
        webkitdirectory: true,
      }
      : {}),
  };
  return (
    <Upload {...uploaderProps}>
      <button ref={uploadRef} style={{ display: 'none' }}></button>
    </Upload>
  );
};

export default FileUpload;
