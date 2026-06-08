import { server, hostServer, reportsServer, storageServer, dataProductServer } from '../server/api';

const getFabricWorkspaces = (offset, limit) => {
  return server.get(`/fabric-workspaces?limit=${limit}&offset=${offset}`, {
    data: {},
  });
};

const getFabricWorkspacesForAdmin = (offset, limit, search = '') => {
  let url = `/fabric-workspaces/admin/workspaces?limit=${limit}&offset=${offset}`;
  if (search && search.trim() !== '') {
    url += `&search=${encodeURIComponent(search.trim())}`;
  }
  return server.get(url, { data: {} });
};

const searchProjectDetails = (projectName) => {
  return server.get(`/fabric-workspaces/searchADAProjects?projectName=${projectName}`, {
    data: {},
  });
};

const createFabricWorkspace = (data) => {
  return server.post(`/fabric-workspaces`, {
    data,
  });
};

const getFabricWorkspace = (id) => {
  return server.get(`/fabric-workspaces/${id}`, {
    data: {},
  });
};

const updateFabricWorkspace = (id, data) => {
  return server.put(`/fabric-workspaces/${id}`, data);
};

const deleteFabricWorkspace = (id) => {
  return server.delete(`/fabric-workspaces/${id}`, {
    data: {},
  });
};

const getFabricWorkspaceLov = () => {
  return server.get(`/lov/fabric-workspaces?limit=0&offset=0`, {
    data: {},
  });
};

const requestRoles = (id, data) => {
  return server.post(`/fabric-workspaces/${id}/rolerequest`, {
    data,
  });
};

const getAllSolutions = () => {
  const reqQuery = `limit:0,published:true`;
  let resQuery = `totalCount
    records {
      id,
      productName
    }`;

  const apiQuery = {
    query: `query {
      solutions(${reqQuery}){
        ${resQuery}
      }
    }`,
  };

  return hostServer.post('minified', apiQuery);
}

const getAllReports = () => {
  return reportsServer.get(`/reports?limit=0&published=true`, {
    data: {},
  });
};

const getAllTags = () => {
  return server.get(`/tags`, {
    data: {},
  });
};

const createLakehouse = (id, data) => {
  return server.post(`/fabric-workspaces/${id}/lakehouses`, {
    data,
  });
};

const deleteLakehouse = (workspaceId, lakehouseId) => {
  return server.delete(`/fabric-workspaces/${workspaceId}/lakehouses/${lakehouseId}`, {
    data: {},
  });
};

const createShortcut = (workspaceId, lakehouseId, data) => {
  return server.post(`/fabric-workspaces/${workspaceId}/lakehouses/${lakehouseId}/shortcuts/`, data);
};

const deleteShortcut = (workspaceId, lakehouseId, shortcutId) => {
  return server.delete(`/fabric-workspaces/${workspaceId}/lakehouses/${lakehouseId}/shortcuts/${shortcutId}`, {
    data: {},
  });
};

const getAllShortcuts = (workspaceId, lakehouseId) => {
  return server.get(`/fabric-workspaces/${workspaceId}/lakehouses/${lakehouseId}/shortcuts?limit=0&offset=0`, {
    data: {},
  });
};

const getAllBuckets = () => {
  return storageServer.get(`/buckets?offset=0&limit=0`, {
    data: {},
  });
};

const getConnectionInfo = (bucketName) => {
  return storageServer.get(`/buckets/${bucketName}/connect`, { 
    data: {} 
  });
};

const getLovData = () => {
  return Promise.all([
    storageServer.get(`/classifications`, {
      data: {},
    }),
    hostServer.get('/divisions'),
    reportsServer.get('/departments', {
      data: {},
    }),
  ]);
}

const getLeanIX = (searchTerm) => {
  return dataProductServer.get(`/planningit?searchTerm=${searchTerm}`, { data: {} });
};

const transferOwnership = (id, data) => {
   return server.patch(`/fabric-workspaces/${id}/transferOwnership`,
    data,
  );
}

const getLakehouseTables = (workspaceId, lakehouseId) => {
  return server.get(`/fabric-workspaces/lakehouses/tables?workspaceId=${workspaceId}&lakehouseId=${lakehouseId}`, {
    data: {} 
  });
};

const getTableSchema = (workspaceId, lakehouseId, tableName, schemaName={}) => {
  return server.get(`fabric-workspaces/lakehouses/table/schema?workspaceId=${workspaceId}&lakehouseId=${lakehouseId}&tableName=${tableName}&schemaName=${schemaName}`, {
    data: {} 
  });
};

const pushSelectedTables = (workspaceId, payload) => {
  return server.post(`fabric-workspaces/catalog/${workspaceId}/publish`, payload);
};

const updateSelectedTables = (workspaceId, payload) => {
  return server.put(`fabric-workspaces/catalog/${workspaceId}/publish`, payload);
};

const getStoredCdcMetadata = (workspaceId, serviceName) => {
  return server.get(`/fabric-workspaces/catalog/${workspaceId}/${encodeURIComponent(serviceName)}`, {
    data: {},
  });
};

const takeOwnership = (id) => {
  return server.patch(`/fabric-workspaces/${id}/takeOwnership`, {
    data: {},
  });
};

const getLegalEntities = (searchTerm) => {
  return server.get(`fabric-workspaces/catalog/getLegalEntities?searchTerm=${searchTerm}`);
};

const publishDdxDataProduct = (workspaceId, lakehouseId, payload) => {
  return server.post(`fabric-workspaces/catalog/ddx/${workspaceId}/${lakehouseId}/publish`, payload);
};

const checkTableMismatch = (workspaceId, lakehouseId) => {
  return server.get(`/fabric-workspaces/catalog/${workspaceId}/check-mismatch?lakehouseId=${lakehouseId}`, {
    data: {},
  });
};

const getCatalogMetadata = (workspaceId, serviceName) => {
  return server.get(`/fabric-workspaces/catalog/${workspaceId}/metadata?serviceName=${encodeURIComponent(serviceName)}`, {
    data: {},
  });
};

const saveLakehouseSnapshot = (workspaceId, lakehouseId, payload) => {
  return server.post(`/fabric-workspaces/catalog/${workspaceId}/lakehouses/${lakehouseId}/snapshot`, payload);
};

export const fabricApi = {
  getFabricWorkspaces,
  getFabricWorkspacesForAdmin,
  searchProjectDetails,
  getFabricWorkspace,
  createFabricWorkspace,
  updateFabricWorkspace,
  deleteFabricWorkspace,
  getFabricWorkspaceLov,
  createLakehouse,
  deleteLakehouse,
  createShortcut,
  deleteShortcut,
  getAllShortcuts,
  requestRoles,
  getAllReports,
  getAllTags,
  getAllSolutions,
  getAllBuckets,
  getConnectionInfo,
  getLovData,
  getLeanIX,
  transferOwnership,
  getLakehouseTables,
  getTableSchema,
  pushSelectedTables,
  updateSelectedTables,
  getStoredCdcMetadata,
  takeOwnership,
  getLegalEntities,
  publishDdxDataProduct,
  checkTableMismatch,
  getCatalogMetadata,
  saveLakehouseSnapshot,
};
