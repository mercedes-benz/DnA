import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';
import logger from 'redux-logger';
import lovsReducer from './redux/lovsSlice';

export const history = createHashHistory({
  basename: '/fabric',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    lovs: lovsReducer,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
