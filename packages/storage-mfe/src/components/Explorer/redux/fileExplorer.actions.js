import server from '../../../server/api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import { history } from '../../../store/storeRoot';

export const setFiles = (fileName, historyPush = true) => {
  return async (dispatch) => {
    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });

    try {
      const res = await server.get(`/buckets/${fileName}/objects`, { data: {} });
      if (res?.data) {
        let result = {};
        let childrenIds = [];
        let ids = [];
        res.data.data.bucketObjects.forEach((item) => {
          childrenIds.push({
            parentId: fileName,
            name: item.objectName.replaceAll('/', ''),
            id: item.objectName.replaceAll(/(\.|\/)/g, '').replaceAll(' ', ''),
            ...item,
          });
          ids.push(item.objectName.replaceAll(/(\.|\/)/g, '').replaceAll(' ', ''));

          result[fileName] = {
            name: fileName,
            id: fileName,
            isDir: true,
            childrenCount: res.data.data.bucketObjects.length,
            childrenIds: ids,
            objectName: `${fileName}/`,
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
          payload: fileName,
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
      } else {
        dispatch({
          type: 'UPDATE_ROOT_FOLDER',
          payload: fileName,
        });
        dispatch({
          type: 'SET_FILES',
          payload: {
            [fileName]: {
              id: fileName,
              name: fileName,
              isDir: true,
              childrenIds: [],
            },
          },
        });
        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
      }
      historyPush && history.push(`explorer/${fileName}`);
    } catch (e) {
      dispatch({
        type: 'FILE_LOADING',
        payload: false,
      });
      Notification.show(e?.response?.data?.message ? e.response.data.message : 'Something went wrong', 'alert');
      history.push('/');
    }
  };
};

export const deleteFiles = (data) => {
  return (dispatch) => {
    dispatch({
      type: 'DELETE_FILES',
      payload: data,
    });
  };
};

export const getFiles = (files, fileName, fileToOpen) => {
  return async (dispatch) => {
    const copyFiles = { ...files };

    dispatch({
      type: 'FILE_LOADING',
      payload: true,
    });
    try {
      const res = await server.get(`/buckets/${fileName}/objects`, {
        data: {},
        params: {
          prefix: fileToOpen.objectName,
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
      } else {
        dispatch({
          type: 'FILE_LOADING',
          payload: false,
        });
      }
    } catch (e) {
      dispatch({
        type: 'FILE_LOADING',
        payload: false,
      });
      Notification.show(e?.response?.data?.message ? e.response.data.message : 'Something went wrong', 'alert');
      history.push('/');
    }
  };
};

export const downloadFoldersOrFiles = (fileName, fileOrFolder) => {
  return async () => {
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
