import React from 'react';
import Upload from 'rc-upload';

import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { baseURL } from '../../server/api';
import { getFiles } from './redux/fileExplorer.actions';
import { useDispatch, useSelector } from 'react-redux';
import Notification from '../../common/modules/uilab/js/src/notification';
import { getFilePath, setObjectKey } from './Utils';
import { SESSION_STORAGE_KEYS } from '../Utility/constants';

const FileUpload = ({ uploadRef, bucketName, folderChain, enableFolderUpload = false }) => {
  const dispatch = useDispatch();
  const { files } = useSelector((state) => state.fileExplorer);

  const objectPath = getFilePath(folderChain);

  const onSuccess = (response, uploadFile) => {
    const currentFolder = folderChain?.[folderChain?.length - 1];
    const folderId = {
      objectName: objectPath,
      id: currentFolder?.id,
      name: currentFolder?.name,
      parentId: currentFolder?.parentId,
    };

    Notification.show(`${uploadFile.name} uploaded successfully.`);

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
    beforeUpload: (file) => {
      let isValid = true;
      // Folder upload
      if (enableFolderUpload) {
        if (file.name === '.DS_Store') {
          isValid = false;
        } else if (/(publishedparquet|published parquet)/gi.test(file.webkitRelativePath)) {
          isValid = false;
          Notification.show('Folder name containing "Published Parquet" text is not allowed.', 'alert');
        }
      } else {
        // file Upload
        // nested folder
        if (objectPath) {
          const objectName = objectPath.replaceAll('/', '');
          const key = objectName + setObjectKey(file.name);
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
