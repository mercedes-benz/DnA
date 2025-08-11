export const HTTP_METHOD = { 
    POST : 'POST',
    GET : 'GET',
    PUT : 'PUT',
    PATCH : 'PATCH',
    DELETE : 'DELETE',
    MKCOL : 'MKCOL',
  }
  
  export const USER_ROLE = { 
    GUEST : '0',
    USER : '1',
    EXTENDED : '2',
    ADMIN : '3',
    REPORTADMIN : '4',
    DIVISIONADMIN : '5',
    DATACOMPLIANCEADMIN : '6',
    CODESPACEADMIN : '7'
  }
  
  export const SESSION_STORAGE_KEYS = {
    PAGINATION_MAX_ITEMS_PER_PAGE : 'paginationMaxItemsPerPage',
    AUDIT_LOGS_MAX_ITEMS_PER_PAGE : 'auditLogsMaxItemsPerPage',
    JWT: 'jwt',
  }
  
  export const PRIVATE_RECIPES = [
    /* Uncomment and use bellow code to keep your private recipes until we make proper recipe management */
    // { id: 'private-frontend', resource: '4Gi,2000Mi,2000m,4000Mi,2000m', name: `Private Frontend (Debian 11 OS, 2GB RAM, 2CPU)`, repodetails: 'YOUR PRIVATE REPO URL' },
    // { id: 'private-backend', resource: '4Gi,3000Mi,2000m,4000Mi,2000m', name: `Private Backend (Debian 11 OS, 2GB RAM, 2CPU)`, repodetails: 'YOUR PRIVATE REPO URL' }
  ];
  
  export const DEPLOYMENT_DISABLED_RECIPE_IDS = [ 'default', 'private-user-defined' ]; 
  
  export const HTTP_OPTIONS = [{
    id: 1,
    name: 'POST'
  }, {
    id: 2,
    name: 'GET'
  }, {
    id: 3,
    name: 'PUT'
  }, {
    id: 4,
    name: 'DELETE'
  }, {
    id: 5,
    name: 'PATCH'
  }, {
    id: 6,
    name: 'HEAD'
  }, {
    id: 7,
    name: 'OPTIONS'
  }, {
    id: 8,
    name: 'TRACE'
  }, {
    id: 9,
    name: 'CONNECT'
  }];
  
  export const CODE_SPACE_STATUS = ['DRAFT', 'PUBLISHED']; 
  export const CODE_SPACE_DISABLE_DNA_PROTECT = ['PUBLISHED', 'ACCEPTED', 'REQUESTED'];
  export const CODE_SPACE_TITLE = 'Authorization Configuration'; 