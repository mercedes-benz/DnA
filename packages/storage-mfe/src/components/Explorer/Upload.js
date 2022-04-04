import React from 'react';
import Upload from 'rc-upload';

import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { baseURL } from '../../server/api';
import { getFiles } from './redux/fileExplorer.actions';
import { useDispatch, useSelector } from 'react-redux';
import Notification from '../../common/modules/uilab/js/src/notification';

const ATTACH_FILES_TO_ACCEPT =
  '.doc,.docx,.odt,.pptx,.rtf,.pdf,.bmp,.gif,.png,.jpg,.jpeg,.csv,.xsl,.xlsx,.ppt,.txt,.zip';

const FileUpload = ({ uploadRef, bucketName, currentFolderId }) => {
  const dispatch = useDispatch();
  const { files } = useSelector((state) => state.fileExplorer);

  const prefix = currentFolderId
    ?.map((item, index) => {
      if (index === 0) {
        return '';
      } else {
        return item.objectName;
      }
    })
    ?.filter((x) => !!x); //filter falsy value

  const onSuccess = () => {
    const folderId = {
      objectName: [...new Set(prefix.join('').split('/'))].join('/'),
      id: currentFolderId?.[currentFolderId.length - 1]?.id,
      name: currentFolderId?.[currentFolderId.length - 1]?.name,
      parentId: currentFolderId?.[currentFolderId.length - 1]?.parentId,
    };

    dispatch(getFiles(files.fileMap, bucketName, folderId));

    ProgressIndicator.hide();
  };
  const onError = (err, errResponse) => {
    if (errResponse.errors) {
      if (errResponse.errors.length > 0) {
        Notification.show(errResponse.error, 'alert');
      }
    }
    ProgressIndicator.hide();
  };

  const uploaderProps = {
    accept: ATTACH_FILES_TO_ACCEPT,
    headers: {
      Authorization: sessionStorage.getItem('jwt'),
    },
    action: `${baseURL}/buckets/${bucketName}/upload`,
    data: {
      prefix: currentFolderId.length === 1 ? '/' : [...new Set(prefix.join('').split('/'))].join('/'),
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
    // directory: true,
    // webkitdirectory: true,
  };
  return (
    <Upload {...uploaderProps}>
      <button ref={uploadRef} style={{ display: 'none' }}></button>
    </Upload>
  );
};

export default FileUpload;
