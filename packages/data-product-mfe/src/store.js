import { configureStore } from '@reduxjs/toolkit';
import dataProductSlice from './components/redux/dataProductSlice';
import logger from 'redux-logger';

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    dataProducts: dataProductSlice,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
