import { server, hostServer, reportsServer, vaultServer, storageServer } from '../server/api';

const getCodeSpacesList = () => {
    return server.get(`workspaces`, {
        data: {},
    });
};

const createCodeSpace = (data) => {
    return server.post(`workspaces`, {
        data,
    });
};

const editCodeSpace = (id, data) => {
    return server.patch(`workspaces/${id}/datagovernance`, {
        data,
    });
};

const getCodeSpaceStatus = (id) => {
    return server.get(`workspaces/status/${id}`, {
        data: {},
    });
};
  
const deleteCodeSpace = (id) => {
    return server.delete(`workspaces/${id}`, {
        data: {},
    });
};
  
const getCodeSpacesGitBranchList = (repoName) => {
    return server.get(`workspaces/${repoName}/branches`, {
        data: {},
    });
};
  
const deployCodeSpace = (id, data) => {
    return server.post(`workspaces/${id}/deploy`, {
        data,
    });
};
  
const unDeployCodeSpace = (id, data) => {
    return server.delete(`workspaces/${id}/deploy`, {
        data,
    });
};
  
const onBoardCollaborator = (id, data) => {
    return server.put(`workspaces/${id}`, {
        data,
    });
};

const addCollaborator = (id, data) => {
    return server.post(`workspaces/${id}/collaborator`, {
        data,
    });
};

const deleteCollaborator = (id, userId) => {
    return server.delete(`workspaces/${id}/collaborator/${userId}`, {
        data: {},
    });
};

const transferOwnership = (id, data) => {
    return server.patch(`workspaces/${id}/projectowner`, {
        data,
    });
};

  // Usage statistics
const getWorkSpacesTransparency = () => {
    return server.get(`workspaces/transparency`, {
        data: {},
    });
};

const createOrUpdateCodeSpaceConfig = (id, data, env) => {
    return server.patch(`workspaces/${id}/config?env=${env}`, {
        data,
    });
};

const getCodeSpaceConfig = (id, env) => {
    return server.get(`/workspaces/${id}/config?env=${env}`, {
        data: {},
    });
};

const getPublishedConfig = (id, env) => {
    return server.get(`/workspaces/${id}/config/publish?env=${env}`, {
        data: {},
    });
};

const getEntitlements = (id) => {
    return server.get(`/workspaces/${id}/config/entitlements`, {
        data: {},
    });
};

const getRoles = (id) => {
    return server.get(`/workspaces/${id}/config/roles`, {
        data: {},
    });
};

const getRolesMappings = (id) => {
    return server.get(`/workspaces/${id}/config/mappings`, {
        data: {},
    });
};

const addCodeSpaceRequest = (id, env) => {
    return server.post(`/workspaces/${id}/config/publish?env=${env}`, {
        data: {},
    });
};

const createCodeSpaceRecipe = (data) => {
    return server.post(`recipeDetails`, {
        data,
    });
};
  
const getCodeSpaceRecipeRequests = () => {
    return server.get(`recipeDetails`, {
        data: {},
    });
};

const getCodeSpaceRecipe = (id) => {
    return server.get(`recipeDetails/${id}`, {
        data: {},
    });
};

const getCodeSpaceRecipesStatus = () => {
    return server.get(`recipeDetails/recipesByStatus`, {
        data: {},
    });
};

const acceptCodeSpaceRecipeRequest = (name) => {
    return server.post(`recipeDetails/${name}/accept`, {
        data: {},
    });
};

const publishCodeSpaceRecipeRequest = (name) => {
    return server.post(`recipeDetails/${name}/publish`, {
        data: {},
    });
};

// const getRecipeLov = () => {
//     return server.get('recipeDetails/recipeLov', {
//         data: {},
//     });
// };

const getSoftwareLov = () => {
    return server.get(`recipeDetails/softwareLov`, {
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
};
  
const getWorkspaceConfigs = () => {
    return server.get(`/workspaces/configs`, {
        data: {},
    });
};

const acceptSecurityConfigRequest = (id) => {
    return server.post(`/workspaces/${id}/config/accept`, {
        data: {},
    });
};

const publishSecurityConfigRequest = (id) => {
    return server.post(`/workspaces/${id}/config/publish`, {
        data: {},
    });
};

const read_secret = (codeSpaceName, env) => {
    return vaultServer.get(`/secret/${codeSpaceName}/${env}`, {
        data: {},
    });
};

const update_secret = (path, secret_value, env) => {
    return vaultServer.put(`/secret/${path}/${env}`, {
        secret_value,
    });
};

const startStopWorkSpace = (id, serverStarted) => {
    if (serverStarted) return server.delete(`/workspaces/server/${id}`, {data: {},});
    return server.post(`/workspaces/startserver/${id}`, {data: {},});
};

const workSpaceStatus = () => {
    return server.get(`/workspaces/serverstatus`, {
        data: {},
    });
};

export const CodeSpaceApiClient = {
    getCodeSpacesList,
    createCodeSpace,
    editCodeSpace,
    getCodeSpaceStatus,
    deleteCodeSpace,
    getCodeSpacesGitBranchList,
    deployCodeSpace,
    unDeployCodeSpace,
    onBoardCollaborator,
    addCollaborator,
    deleteCollaborator,
    transferOwnership,
    getWorkSpacesTransparency,
    createOrUpdateCodeSpaceConfig,
    getCodeSpaceConfig,
    getPublishedConfig,
    getEntitlements,
    getRoles,
    getRolesMappings,
    addCodeSpaceRequest,
    createCodeSpaceRecipe,
    getCodeSpaceRecipeRequests,
    getCodeSpaceRecipe,
    getCodeSpaceRecipesStatus,
    acceptCodeSpaceRecipeRequest,
    publishCodeSpaceRecipeRequest,
    getSoftwareLov,
    getLovData,
    getWorkspaceConfigs,
    acceptSecurityConfigRequest,
    publishSecurityConfigRequest,
    read_secret,
    update_secret,
    startStopWorkSpace,
    workSpaceStatus,
};
