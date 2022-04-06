import React from 'react';
import Upload from 'rc-upload';

import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { baseURL } from '../../server/api';
import { getFiles } from './redux/fileExplorer.actions';
import { useDispatch, useSelector } from 'react-redux';
import Notification from '../../common/modules/uilab/js/src/notification';
import { serializeFolderChain } from '../Bucket/Utils';
import { SESSION_STORAGE_KEYS } from '../Utility/constants';

const ATTACH_FILES_TO_ACCEPT =
  '.doc,.docx,.odt,.pptx,.rtf,.pdf,.bmp,.gif,.png,.jpg,.jpeg,.csv,.xsl,.xlsx,.ppt,.txt,.zip,.js,.py,.ts,.tsx,.jsx,.json,.scss,.css,.java,.yml,.yaml';

const FileUpload = ({ uploadRef, bucketName, folderChain, enableFolderUpload = false }) => {
  const dispatch = useDispatch();
  const { files } = useSelector((state) => state.fileExplorer);

  const prefix = serializeFolderChain(folderChain);
  const objectPath = [...new Set(prefix.join('').split('/'))].join('/');

  const onSuccess = () => {
    const currentFolder = folderChain?.[folderChain?.length - 1];
    const folderId = {
      objectName: objectPath,
      id: currentFolder?.id,
      name: currentFolder?.name,
      parentId: currentFolder?.parentId,
    };

    dispatch(getFiles(files.fileMap, bucketName, folderId));
  };
  const onError = (err, errResponse) => {
    if (errResponse.errors?.length) {
      Notification.show(errResponse.errors[0].message, 'alert');
    }
    ProgressIndicator.hide();
  };

  const uploaderProps = {
    accept: ATTACH_FILES_TO_ACCEPT,
    headers: {
      Authorization: sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT),
    },
    action: `${baseURL}/buckets/${bucketName}/upload`,
    data: {
      prefix: folderChain.length === 1 ? '/' : objectPath,
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
