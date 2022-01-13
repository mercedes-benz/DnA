export enum HTTP_METHOD {
  POST = 'POST',
  GET = 'GET',
  PUT = 'PUT',
  PATCH = 'PATCH',
  DELETE = 'DELETE',
  MKCOL = 'MKCOL',
}

export enum LOCAL_STORAGE_KEY {
  TOKEN = 'TOKEN',
}
export enum USER_ROLE {
  GUEST = '0',
  USER = '1',
  EXTENDED = '2',
  ADMIN = '3',
}

export enum SESSION_STORAGE_KEYS {
  PKCE = 'pkce_verifier',
  CODE = 'code',
  JWT = 'jwt',
  ACCESS_TOKEN = 'access_token',
  USER_ID = 'user_id',
  REDIRECT_URL = 'redirect_url',
  PORTFOLIO_FILTER_VALUES = 'portfolioFilterValues',
  PAGINATION_MAX_ITEMS_PER_PAGE = 'paginationMaxItemsPerPage',
  APPREDIRECT_URL = 'appredirect_url',
  LISTVIEW_MODE_ENABLE = 'listViewModeEnable',
}

export enum LOCAL_STORAGE_KEYS {
  SHOW_DISCLAIMER_STATUS = 'showDisclaimerStatus',
}

export enum ENV {
  LOCAL = 'local',
  DEV = 'development',
  INT = 'integration',
  PROD = 'production',
}

export enum SOLUTION_LOGO_IMAGE_TYPES {
  THUMBNAIL = 'thumbnails',
  TILE = 'tiles',
  BANNER = 'banners',
}

export const TEAMS_PROFILE_LINK_URL_PREFIX = 'https://xxxxx';

export const ATTACH_FILES_TO_ACCEPT =
  '.doc,.docx,.odt,.pptx,.rtf,.pdf,.bmp,.gif,.png,.jpg,.jpeg,.csv,.xsl,.xlsx,.ppt,.txt,.zip';

export const PredefinedSolutionLogoImagesInfo = {
  folder: 'images/solutionLogoImages', // Path inside frontend/public folder
  images: [
    {
      id: 'default',
      name: 'Default',
    },
  ],
};
