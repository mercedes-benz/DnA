import { configureStore } from '@reduxjs/toolkit';

import provideDataProducts from './components/redux/dataProductSlice';
import consumeDataProductSlice from './components/redux/consumeDataProductSlice';

import logger from 'redux-logger';

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    provideDataProducts: provideDataProducts,
    consumeDataProducts: consumeDataProductSlice,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
