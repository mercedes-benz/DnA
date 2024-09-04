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
  requestRoles,
  getAllReports,
  getAllSolutions,
  getLovData,
};
