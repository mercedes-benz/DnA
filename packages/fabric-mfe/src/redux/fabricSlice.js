import { createSlice } from '@reduxjs/toolkit';
import { getWorkspaces } from './fabric.services';

const fabricInitialState = {
  isLoading: false,
  workspace: {},
  errors: '',
};

export const fabricSlice = createSlice({
  name: 'fabric',
  initialState: fabricInitialState,
  extraReducers: (builder) => {
    builder.addCase(getWorkspaces.pending, (state) => {
      state.isLoading = true;
    }),
    builder.addCase(getWorkspaces.fulfilled, (state, action) => {
      state.isLoading = false;
      state.workspace = action.payload.data;
    }),
    builder.addCase(getWorkspaces.rejected, (state, action) => {
      state.isLoading = false;
      state.workspace = {};
      state.errors = action.payload;
    })
  },
});

export default fabricSlice.reducer;
