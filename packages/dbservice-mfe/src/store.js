import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';
import logger from 'redux-logger';
import dbServiceReducer from './redux/dbServiceSlice';

export const history = createHashHistory({
  basename: '/dbservice',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    service: dbServiceReducer,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
