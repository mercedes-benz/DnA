import Notification from '../../../common/modules/uilab/js/src/notification';
import { history } from '../../../store/storeRoot';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { bucketsObjectApi } from '../../../apis/fileExplorer.api';
import { serializeAllObjects, serializeObjects } from '../Utils';
import { ChonkyActions } from 'chonky';
import { CustomActions } from '../CustomFileActions';
import { Envs } from '../../Utility/envs';

export const setFiles = (bucketName, historyPush = true) => {
  return async (dispatch) => {
    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });
    ProgressIndicator.show();

    try {
      const res = await bucketsObjectApi.getAllBucketObjects(bucketName);
      const { data } = res.data;
      if (data?.bucketObjects?.length) {
        const result = serializeAllObjects(data, bucketName);
        dispatch({
          type: 'UPDATE_ROOT_FOLDER',
          payload: bucketName,
        });
        dispatch({
          type: 'SET_FILES',
          payload: result,
        });
        dispatch({
          type: 'SET_BUCKET_PERMISSION',
          payload: res.data.data.bucketPermission,
        });
        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
      } else {
        dispatch({
          type: 'UPDATE_ROOT_FOLDER',
          payload: bucketName,
        });
        dispatch({
          type: 'SET_BUCKET_PERMISSION',
          payload: res.data.data.bucketPermission,
        });
        dispatch({
          type: 'SET_FILES',
          payload: {
            [bucketName]: {
              id: bucketName,
              name: bucketName,
              isDir: true,
              childrenIds: [],
            },
          },
        });
        dispatch({
          type: 'SET_BUCKET_OBJECTS',
          payload: res?.data?.data?.bucketObjects || [],
        });
        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
      }
      dispatch(setActionButtons(res?.data?.data?.bucketPermission, res?.data?.data?.bucketObjects));
      historyPush && history.push(`explorer/${bucketName}`);
    } catch (e) {
      dispatch({
        type: 'FILE_LOADING',
        payload: false,
      });
      ProgressIndicator.hide();
      Notification.show(
        e.response.data.errors?.length ? e.response.data.errors[0].message : 'Error fetching bucket list.',
        'alert',
      );
      // history.push('/');
    }
  };
};

export const getFiles = (files, bucketName, fileToOpen) => {
  return async (dispatch) => {
    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      const prefix = !fileToOpen.objectName ? '/' : fileToOpen.objectName;
      const res = await bucketsObjectApi.getObjects(bucketName, prefix);

      if (res?.data) {
        const { data } = res.data;
        const result = serializeObjects(data, fileToOpen);
        if (Object.keys(result)?.length) {
          dispatch({
            type: 'SET_FILES',
            payload: { ...files, ...result },
          });
        } else {
          dispatch({
            type: 'SET_FILES',
            payload: files,
          });
        }

        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
        dispatch({
          type: 'SET_BUCKET_OBJECTS',
          payload: res?.data?.data?.bucketObjects || [],
        });
        dispatch(setActionButtons(res?.data?.data?.bucketPermission, res?.data?.data?.bucketObjects));
        ProgressIndicator.hide();
      } else {
        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
      }
    } catch (e) {
      dispatch({
        type: 'FILE_LOADING',
        payload: false,
      });
      ProgressIndicator.hide();
      Notification.show(
        e.response.data.errors?.length ? e.response.data.errors[0].message : 'Error while fetching bucket objects',
        'alert',
      );
      history.push('/');
    }
  };
};

export const downloadFoldersOrFiles = (bucketName, fileOrFolder) => {
  return () => {
    ProgressIndicator.show();
    bucketsObjectApi
      .downloadObjects(bucketName, fileOrFolder.objectName)
      .then((res) => {
        const url = window.URL.createObjectURL(new Blob([res.data], { 'Content-Type': res.headers['Content-Type'] }));
        const link = document.createElement('a');
        link.download = fileOrFolder.name;
        link.href = url;
        link.click();
        ProgressIndicator.hide();
      })
      .catch(() => {
        ProgressIndicator.hide();
        Notification.show('Error while downloading. Please try again later.', 'alert');
      });
  };
};

export const deleteFiles = (bucketName, filesPath, files, bucketObjects) => {
  return async (dispatch) => {
    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      bucketsObjectApi.deleteObjects(bucketName, filesPath).then(() => {
        dispatch({
          type: 'SET_FILES',
          payload: files,
        });
        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
        dispatch({
          type: 'SET_BUCKET_OBJECTS',
          payload: bucketObjects.filter((item) => item.objectName !== filesPath) || [],
        });
        ProgressIndicator.hide();
        const pathList = filesPath?.split(',');
        const path = pathList?.map((item) => item?.split('/')?.filter((x) => x));

        const fileList = path?.map((item) => {
          return item[item.length - 1];
        });

        fileList?.map((file) => {
          const isFile = /\./g.test(file);
          const name = isFile ? file : `Folder ${file}`;
          Notification.show(`${name} deleted successfully.`);
        });
      });
    } catch (e) {
      ProgressIndicator.hide();
      Notification.show(
        e?.response?.data?.message ? e.response.data.message : 'Error while deleting an object. Please try again.',
        'alert',
      );
    }
  };
};

export const setActionButtons = (bucketPermission, bucketObjects, showPublish = true) => {
  return async (dispatch) => {
    const hasParquetFile =
      bucketObjects.filter((item) => item.objectName.toLowerCase()?.split('.')?.[1] === 'parquet')?.length > 0;
    const publishedParquetFolder =
      bucketObjects?.filter((item) => !item.isDir && /(PublishedParquet)/i.test(item.objectName))?.length > 0;

    dispatch({
      type: 'SET_ACTION_BUTTONS',
      payload: [
        ...(bucketPermission.write && !publishedParquetFolder ? [ChonkyActions.UploadFiles] : []),
        ...(bucketPermission.write && !publishedParquetFolder ? [CustomActions.UploadFolder] : []),
        ...(bucketPermission.write && !publishedParquetFolder ? [ChonkyActions.CreateFolder] : []),
        CustomActions.DownloadFiles,
        ...(bucketPermission.write ? [CustomActions.DeleteFiles] : []),
        ...(bucketPermission.write && Envs.ENABLE_TRINO_PUBLISH && showPublish && hasParquetFile
          ? [CustomActions.PublishFolder]
          : []),
        CustomActions.CopyPath,
        CustomActions.ResetSearchInput,
      ],
    });
  };
};

export const resetFiles = () => {
  return async (dispatch) => {
    dispatch({
      type: 'UPDATE_ROOT_FOLDER',
      payload: '',
    });
    dispatch({
      type: 'SET_FILES',
      payload: {},
    });
  };
};
