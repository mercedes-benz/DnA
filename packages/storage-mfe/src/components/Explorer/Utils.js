export const serializeFolderChain = (list) => {
  return list
    ?.map((item, index) => {
      // ignore root folder;
      if (index === 0) {
        return '';
      } else {
        return item.objectName;
      }
    })
    ?.filter((x) => !!x); //filter falsy value
};

// serialize characters to ensure valid object key
const setObjectKey = (item) => item.replaceAll(/(\.|\/)/g, '').replaceAll(' ', '');

export const serializeAllObjects = (data, bucketName) => {
  let result = {};
  let children = [];
  let childrenIds = [];
  data?.bucketObjects?.forEach((item) => {
    const validKeys = setObjectKey(item.objectName);
    childrenIds.push(validKeys);

    children.push({
      parentId: bucketName,
      name: item.objectName.replaceAll('/', ''),
      id: validKeys,
      ...item,
    });

    // update parent folder object with corresponding childrens
    result[bucketName] = {
      name: bucketName,
      id: bucketName,
      isDir: true,
      childrenCount: data.bucketObjects?.length,
      childrenIds,
      objectName: `${bucketName}/`,
    };
    return item;
  });

  // build children object
  children?.forEach((child) => {
    result[child.id] = {
      id: child.id,
      name: child.name,
      parentId: child.parentId,
      modDate: child.lastModified,
      ...child,
    };
    return child;
  });
  return result;
};

export const serializeObjects = (data, fileToOpen) => {
  let result = {};
  let children = [];
  let childrenIds = [];
  data?.bucketObjects?.forEach((item) => {
    const splitName = item?.objectName.split('/');
    const name = item.isDir ? splitName[splitName?.length - 2] : splitName[splitName?.length - 1];
    const validKeys = setObjectKey(item.objectName);

    childrenIds.push(validKeys);

    children.push({
      parentId: fileToOpen.id,
      name: name,
      id: validKeys,
      ...item,
    });

    // update parent folder
    result[fileToOpen.id] = {
      name: fileToOpen.name,
      id: fileToOpen.id,
      isDir: true,
      childrenCount: data.bucketObjects.length,
      childrenIds,
      parentId: fileToOpen?.parentId,
      objectName: fileToOpen.objectName,
    };
    return item;
  });

  children?.forEach((child) => {
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

  return result;
};
