import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';

import projects from './redux/projectsSlice';

import logger from 'redux-logger';

export const history = createHashHistory({
  basename: '/chronos',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    projects,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
