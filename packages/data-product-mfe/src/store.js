import { configureStore } from '@reduxjs/toolkit';
import { createHashHistory } from 'history';

import logger from 'redux-logger';

import provideDataTransfers from './components/dataTransfer/redux/dataTransferSlice';
import dropdownsSlice from './components/dataTransfer/redux/getDropdownsSlice';

import dataSlice from './components/data/redux/dataSlice';

export const history = createHashHistory({
  basename: '/data',
});

const isDev = process.env.NODE_ENV === 'development';

export default configureStore({
  reducer: {
    provideDataTransfers: provideDataTransfers,
    dropdowns: dropdownsSlice,
    data: dataSlice,
  },
  middleware: (getDefaultMiddleware) =>
    isDev
      ? getDefaultMiddleware({ serializableCheck: false }).concat(logger)
      : getDefaultMiddleware({ serializableCheck: false }),
});
