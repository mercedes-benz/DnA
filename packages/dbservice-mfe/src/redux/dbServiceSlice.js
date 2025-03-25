import { createSlice } from '@reduxjs/toolkit';
import { getDBService } from './dbservice.services';

const dbServiceInitialState = {
  loading: true,
  service: {},
  error: '',
};

export const dbServiceSlice = createSlice({
  name: 'dbservice',
  initialState: dbServiceInitialState,
  extraReducers: (builder) => {
    builder.addCase(getDBService.pending, (state) => {
      state.loading = true;
    }),
    builder.addCase(getDBService.fulfilled, (state, action) => {
      state.loading = false;
      state.service = action.payload.data;
    }),
    builder.addCase(getDBService.rejected, (state, action) => {
      state.loading = false;
      state.service = {};
      state.error = action.payload;
    })
  },
});

export default dbServiceSlice.reducer;
