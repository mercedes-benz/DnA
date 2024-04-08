import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';

import dataEntrySlice from './redux/dataEntrySlice';

import logger from 'redux-logger';

export const history = createHashHistory({
  basename: '/dataentry',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    dataentry: dataEntrySlice,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
