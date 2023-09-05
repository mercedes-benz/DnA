import { createSlice } from '@reduxjs/toolkit';
import { getProjectDetails } from './projectDetails.services';

const initialState = {
  isLoading: false,
  data: {},
  errors: '',
};

export const projectDetailsSlice = createSlice({
  name: 'projectDetails',
  initialState,
  extraReducers: builder => {
    builder.addCase(getProjectDetails.pending, state => {
      state.isLoading = true;
    });

    builder.addCase(getProjectDetails.fulfilled, (state, action) => {
      state.isLoading = false;
      if(typeof action.payload === 'string') {
        state.data = {};
        state.errors = action.payload;
      } else {
        state.data = action.payload;
        state.errors = '';
      }
    });

    builder.addCase(getProjectDetails.rejected, (state, action) => {
      state.isLoading = false;
      state.data = {};
      state.errors = action.error.message;
    })
  },
});

export default projectDetailsSlice.reducer;
