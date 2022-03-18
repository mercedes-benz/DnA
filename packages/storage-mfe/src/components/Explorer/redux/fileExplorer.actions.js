export const moveFiles = (data) => {
  return (dispatch) => {
    dispatch({
      type: 'MOVE_FILES',
      payload: data,
    });
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

export const getFiles = (data, files, folderId) => {
  const res = { ...files };
  const childrenIds = data?.map((item) => item.name.replaceAll('/', '').replaceAll('.', ''));

  data?.forEach((item) => {
    const name = item.name.replace('/', '');
    const isDir = item.directory;
    const modDate = item.lastModified;
    delete item.directory;
    delete item.lastModified;
    item.originalName = item.name;
    item.name = item.name.replaceAll('/', '');

    if (item.directory) {
      res[name] = {
        ...item,
        id: name,
        isDir,
        modDate,
        parentId: folderId,
        childrenIds: [],
        childrenCount: 0,
      };
    }
    if (!item.directory) {
      const name = item.name.replace('.', '');
      res[name] = {
        ...item,
        id: name,
        isDir,
        modDate,
        parentId: folderId,
      };
    }
  });
  res[folderId] = {
    ...res[folderId],
    childrenIds: childrenIds,
    childrenCount: childrenIds.length,
  };

  return (dispatch) => {
    dispatch({
      type: 'SET_FILES',
      payload: res,
    });
  };
};
