import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';

import logger from 'redux-logger';

export const history = createHashHistory({
  basename: '/codespaces',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {},
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});