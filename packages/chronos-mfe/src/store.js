import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';

import projectDetails from './redux/projectDetailsSlice';

import logger from 'redux-logger';

export const history = createHashHistory({
  basename: '/chronos',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    projectDetails,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
