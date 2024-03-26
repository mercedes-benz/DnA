import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';

import fabricSlice from './redux/fabricSlice';

import logger from 'redux-logger';

export const history = createHashHistory({
  basename: '/fabric',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    fabric: fabricSlice,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
