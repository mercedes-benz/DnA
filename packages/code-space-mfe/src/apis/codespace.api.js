import { server, hostServer, reportsServer, vaultServer, storageServer, baseURL, readJwt} from '../server/api';
import { EventSourcePolyfill } from 'event-source-polyfill';

const getCodeSpacesList = () => { //tested
    return server.get(`workspaces`, {
        data: {},
    });
};

const createCodeSpace = (data) => { //TESTED
    return server.post(`workspaces`,
        data,
    );
};

const getReadMeFile = (id) => { 
    return server.get(`workspaces/${id}/readme`,{
        data: {},
    });
};

const editCodeSpace = (id, data) => { //tested
    return server.patch(`workspaces/${id}/datagovernance`, 
        data
    );
};

const getCodeSpaceStatus = (id) => { //tested
    return server.get(`workspaces/status/${id}`, {
        data: {},
    });
};
  
const deleteCodeSpace = (id) => { //tested
    return server.delete(`workspaces/${id}`, {
        data: {},
    });
};
  
const getCodeSpacesGitBranchList = (repoName) => { 
    return server.get(`workspaces/branches?repoDetail=${repoName}`, {
        data: {},
    });
};
  
const deployCodeSpace = (id, data) => { //tested
    return server.post(`workspaces/${id}/deploy`, 
        data,
    );
};
  
const unDeployCodeSpace = (id, data) => { //not implemented yet
    return server.delete(`workspaces/${id}/deploy`, {
        data,
    });
};
  
const onBoardCollaborator = (id, data) => { //tested
    return server.put(`workspaces/${id}`, 
        data,
    );
};

const addCollaborator = (id, data) => { //tested
    return server.post(`workspaces/${id}/collaborator`, 
        data,
    );
};

const deleteCollaborator = (id, userId) => { //tested
    return server.delete(`workspaces/${id}/collaborator/${userId}`, {
        data: {},
    });
};

const transferOwnership = (id, data) => { //tested
    return server.patch(`workspaces/${id}/projectowner`, 
        data,
    );
};

const assignAdminRole = (id, userId, data) => {
  return server.post(`workspaces/${id}/collaborator/${userId}/admin?isAdmin=${data}`, {
    data: {},
  });
};

//   // Usage statistics
// const getWorkSpacesTransparency = () => { //for frontend. not used here
//     return server.get(`workspaces/transparency`, {
//         data: {},
//     });
// };

const createOrUpdateCodeSpaceConfig = (id, data, env) => { //tested
    return server.patch(`workspaces/${id}/config?env=${env}`, 
        data,
    );
};

const getCodeSpaceConfig = (id, env) => { //tested
    return server.get(`/workspaces/${id}/config?env=${env}`, {
        data: {},
    });
};

const getPublishedConfig = (id, env) => { //tested
    return server.get(`/workspaces/${id}/config/publish?env=${env}`, {
        data: {},
    });
};

const getEntitlements = (id) => { //not needed for now
    return server.get(`/workspaces/${id}/config/entitlements`, {
        data: {},
    });
};

const getRoles = (id) => { //not needed for now
    return server.get(`/workspaces/${id}/config/roles`, {
        data: {},
    });
};

const getRolesMappings = (id) => { //not needed for now
    return server.get(`/workspaces/${id}/config/mappings`, {
        data: {},
    });
};

const addCodeSpaceRequest = (id, env) => { //tested
    return server.post(`/workspaces/${id}/config/publish?env=${env}`, {
        data: {},
    });
};

const createCodeSpaceRecipe = (data) => {
    return server.post(`recipeDetails`, 
        data,
    );
};

const updateCodeSpaceRecipe = (id, data) => {
    return server.put(`recipeDetails/${id}`, 
        data,
    );
};
  
const getCodeSpaceRecipes = () => {
    return server.get(`recipeDetails`, {
        data: {},
    });
};

const getCodeSpaceRecipe = (id) => {
    return server.get(`recipeDetails/${id}`, {
        data: {},
    });
};

const getCodeSpaceRecipesStatus = () => { //not used right now
    return server.get(`recipeDetails/recipesByStatus`, {
        data: {},
    });
};

const acceptCodeSpaceRecipeRequest = (name) => { //not used right now
    return server.post(`recipeDetails/${name}/accept`, {
        data: {},
    });
};

const publishCodeSpaceRecipeRequest = (name) => { //not used right now
    return server.post(`recipeDetails/${name}/publish`, {
        data: {},
    });
};

const getRecipeLov = () => {
    return server.get('recipeDetails/recipelov', {
        data: {},
    });
};

const  deleteCodeSpaceRecipe = (name) => {
    return server.delete(`recipeDetails/${name}`, { 
        data: {},
    });
}

const verifyGitUser = (data)  => {
    return server.post('recipeDetails/validate', 
        data,
    );
};

const getSoftwareLov = () => {
    return server.get(`recipeDetails/softwareLov`, {
        data: {},
    });
};

const getAdditionalServicesLov = () => {
    return server.get(`recipeDetails/additionalServiceLov`, {
        data: {},
    });
};

const getLovData = () => { //tested
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
  
const getWorkspaceConfigs = () => { //not needed
    return server.get(`/workspaces/configs`, {
        data: {},
    });
};

const acceptSecurityConfigRequest = (id) => { //not needed
    return server.post(`/workspaces/${id}/config/accept`, {
        data: {},
    });
};

const publishSecurityConfigRequest = (id) => { //not needed
    return server.post(`/workspaces/${id}/config/publish`, {
        data: {},
    });
};

const read_secret = (codeSpaceName, env) => { //tested
    return vaultServer.get(`/secret/${codeSpaceName}/${env}`, {
        data: {},
    });
};

const update_secret = (path, secret_value, env) => { //tested
    return vaultServer.put(`/secret/${path}/${env}`, 
        secret_value,
    );
};

const startStopWorkSpace = (id, serverStarted) => { //tested
    if (serverStarted) return server.delete(`/workspaces/server/${id}`, {data: {},});
    return server.post(`/workspaces/startserver/${id}`, {data: {},});
};

const workSpaceStatus = () => {
    return server.get(`/workspaces/serverstatus`, {
        data: {},
    });
};

const getUrlHub = (endpoint) => { //tested
    return `${new URL('../hub/api/', baseURL).href}${endpoint}`;
};

const serverStatusFromHub = (userId, workspaceId, onMessageCB, onCloseCB) => { //tested
    const sse = new EventSourcePolyfill(getUrlHub(`users/${userId}/servers/${workspaceId}/progress`), {
      withCredentials: true,
      headers: { Authorization: readJwt() },
    });

    sse.onmessage = onMessageCB;

    sse.onerror = (e) => {
      onCloseCB && onCloseCB(e);
      console.log(e);
      console.error('Event stream closed');
      sse.close();
    };
}

const restartDeployments = (id, env) => {
    return server.post(`/workspaces/${id}/restart?env=${env}`, {data: {},});
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
    assignAdminRole,
    // getWorkSpacesTransparency,
    createOrUpdateCodeSpaceConfig,
    getCodeSpaceConfig,
    getPublishedConfig,
    getEntitlements,
    getReadMeFile,
    getRoles,
    getRolesMappings,
    addCodeSpaceRequest,
    createCodeSpaceRecipe,
    updateCodeSpaceRecipe,
    getCodeSpaceRecipes,
    getCodeSpaceRecipe,
    getRecipeLov,
    deleteCodeSpaceRecipe,
    verifyGitUser,
    getCodeSpaceRecipesStatus,
    acceptCodeSpaceRecipeRequest,
    publishCodeSpaceRecipeRequest,
    getSoftwareLov,
    getAdditionalServicesLov,
    getLovData,
    getWorkspaceConfigs,
    acceptSecurityConfigRequest,
    publishSecurityConfigRequest,
    read_secret,
    update_secret,
    startStopWorkSpace,
    workSpaceStatus,
    serverStatusFromHub,
    restartDeployments,
};
