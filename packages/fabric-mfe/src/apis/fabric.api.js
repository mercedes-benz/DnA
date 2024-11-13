import { server, hostServer, reportsServer, storageServer } from '../server/api';

const getFabricWorkspaces = (offset, limit) => {
  return server.get(`/fabric-workspaces?limit=${limit}&offset=${offset}`, {
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

export const fabricApi = {
  getFabricWorkspaces,
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
  getAllSolutions,
  getAllBuckets,
  getConnectionInfo,
  getLovData,
};
