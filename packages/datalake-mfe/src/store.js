import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';

import graphSlice from './redux/graphSlice';

import logger from 'redux-logger';
import projectsSlice from './redux/projectsSlice';

export const history = createHashHistory({
  basename: '/datalake',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    graph: graphSlice,
    graphs: projectsSlice
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
