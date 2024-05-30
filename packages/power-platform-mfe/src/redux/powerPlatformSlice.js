import { createSlice } from '@reduxjs/toolkit';

const powerPlatformInitialState = {
  isLoading: false,
  workspace: {},
  errors: '',
};

export const powerPlatformSlice = createSlice({
  name: 'power-platform',
  initialState: powerPlatformInitialState,
});

export default powerPlatformSlice.reducer;
