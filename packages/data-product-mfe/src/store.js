import { configureStore } from '@reduxjs/toolkit';
import dataProductSlice from './components/redux/dataProductSlice';

export default configureStore({
  reducer: {
    dataProducts: dataProductSlice,
  },
  middleware: (getDefaultMiddleware) => getDefaultMiddleware({ serializableCheck: false }),
});
