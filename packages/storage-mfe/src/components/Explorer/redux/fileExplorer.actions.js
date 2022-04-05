import server from '../../../server/api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import { history } from '../../../store/storeRoot';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';

export const setFiles = (bucketName, historyPush = true) => {
  return async (dispatch) => {
    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });
    ProgressIndicator.show();

    try {
      const res = await server.get(`/buckets/${bucketName}/objects`, { data: {} });
      if (res?.data?.data?.bucketObjects?.length) {
        let result = {};
        let childrenIds = [];
        let ids = [];
        res.data.data.bucketObjects.forEach((item) => {
          childrenIds.push({
            parentId: bucketName,
            name: item.objectName.replaceAll('/', ''),
            id: item.objectName.replaceAll(/(\.|\/)/g, '').replaceAll(' ', ''),
            ...item,
          });
          ids.push(item.objectName.replaceAll(/(\.|\/)/g, '').replaceAll(' ', ''));

          result[bucketName] = {
            name: bucketName,
            id: bucketName,
            isDir: true,
            childrenCount: res.data.data.bucketObjects.length,
            childrenIds: ids,
            objectName: `${bucketName}/`,
          };
          return item;
        });
        childrenIds.forEach((child) => {
          result[child.id] = {
            id: child.id,
            name: child.name,
            parentId: child.parentId,
            modDate: child.lastModified,
            // childrenIds: [],
            ...child,
          };
          return child;
        });
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
          type: 'FILE_LOADING',
          payload: false,
        });
        ProgressIndicator.hide();
      }
      historyPush && history.push(`explorer/${bucketName}`);
    } catch (e) {
      dispatch({
        type: 'FILE_LOADING',
        payload: false,
      });
      ProgressIndicator.hide();
      Notification.show(e?.response?.data?.message ? e.response.data.message : 'Something went wrong', 'alert');
      history.push('/');
    }
  };
};

export const deleteFiles = (bucketName, filesName, files) => {
  return async (dispatch) => {
    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      server
        .delete(`/buckets/${bucketName}/objects`, {
          params: {
            prefix: `${filesName}`,
          },
          data: {},
        })
        .then(() => {
          dispatch({
            type: 'SET_FILES',
            payload: files,
          });
          dispatch({
            type: 'FILE_LOADING',
            payload: false,
          });
          ProgressIndicator.hide();
        });
    } catch (e) {
      ProgressIndicator.hide();
      Notification.show(
        e?.response?.data?.message ? e.response.data.message : 'Error while deleting. Please try again.',
      );
    }
  };
};

export const getFiles = (files, bucketName, fileToOpen) => {
  return async (dispatch) => {
    const copyFiles = { ...files };

    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });
    ProgressIndicator.show();
    try {
      const res = await server.get(`/buckets/${bucketName}/objects`, {
        data: {},
        params: {
          prefix: fileToOpen.name === bucketName ? '/' : fileToOpen.objectName,
        },
      });

      if (res?.data) {
        let result = {};
        let childrenIds = [];
        let ids = [];
        res.data.data.bucketObjects.forEach((item) => {
          const splitName = item?.objectName.split('/');
          const name = item.isDir ? splitName[splitName?.length - 2] : splitName[splitName?.length - 1];

          childrenIds.push({
            parentId: fileToOpen.id,
            name: name,
            id: item.objectName.replaceAll(/(\.|\/)/g, '').replaceAll(' ', ''),
            ...item,
          });
          ids.push(item.objectName.replaceAll(/(\.|\/)/g, '').replaceAll(' ', ''));

          result[fileToOpen.id] = {
            name: fileToOpen.name,
            id: fileToOpen.id,
            isDir: true,
            childrenCount: res.data.data.bucketObjects.length,
            childrenIds: ids,
            parentId: fileToOpen?.parentId,
            objectName: fileToOpen.objectName,
          };
          return item;
        });

        childrenIds.forEach((child) => {
          result[child.id] = {
            id: child.id,
            name: child.name,
            parentId: child.parentId,
            modDate: child.lastModified,
            isDir: child.isDir,
            ...child,
          };
          return child;
        });
        dispatch({
          type: 'SET_FILES',
          payload: { ...copyFiles, ...result },
        });
        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
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
        e?.response?.data?.message ? e.response.data.message : 'Error while fetching bucket objects',
        'alert',
      );
      history.push('/');
    }
  };
};

export const downloadFoldersOrFiles = (fileName, fileOrFolder) => {
  return () => {
    server
      .get(`/buckets/${fileName}/objects/metadata`, {
        data: {},
        params: {
          prefix: fileOrFolder.objectName,
        },
        responseType: 'blob',
      })
      .then((res) => {
        const url = window.URL.createObjectURL(new Blob([res.data], { 'Content-Type': res.headers['Content-Type'] }));
        const link = document.createElement('a');
        link.download = fileOrFolder.name;
        link.href = url;
        link.click();
      })
      .catch(() => {
        Notification.show('Error while downloading. Please try again later.', 'alert');
      });
  };
};
