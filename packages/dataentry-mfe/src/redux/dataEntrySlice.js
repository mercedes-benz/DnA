import { createSlice } from '@reduxjs/toolkit';

const dataEntryInitialState = {
  isLoading: false,
  project: {},
  errors: '',
};

export const dataEntrySlice = createSlice({
  name: 'dataentry',
  initialState: dataEntryInitialState,
});

export default dataEntrySlice.reducer;
