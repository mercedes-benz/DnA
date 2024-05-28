import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';
import powerPlatformSlice from './redux/powerPlatformSlice';
import logger from 'redux-logger';

export const history = createHashHistory({
  basename: '/powerplatform',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    powerPlatform: powerPlatformSlice,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
