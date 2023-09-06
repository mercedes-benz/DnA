import { createSlice } from '@reduxjs/toolkit';
import { getConfigFiles } from './chronosForm.services';

const initialState = {
  isLoading: false,
  keepForFuture: false,
  inputFile: {},
  configFiles: [],
  errors: '',
};

export const chronosFormSlice = createSlice({
  name: 'chronosForm',
  initialState,
  reducers: {
    reset: state => {
      state.keepForFuture = false;
      state.inputFile = {};
    },
    set: (state, action) => {
      state.keepForFuture = action.payload;
    },
    setInputFile: (state, action) => {
      state.inputFile = action.payload;
    }
  },
  extraReducers: builder => {
    builder.addCase(getConfigFiles.pending, state => {
      state.isLoading = true;
    });

    builder.addCase(getConfigFiles.fulfilled, (state, action) => {
      state.isLoading = false;
      if(typeof action.payload === 'string') {
        state.configFiles = [];
        state.errors = action.payload;
      } else {
        state.configFiles = action.payload;
        state.errors = '';
      }
    });

    builder.addCase(getConfigFiles.rejected, (state, action) => {
      state.isLoading = false;
      state.configFiles = [];
      state.errors = action.error.message;
    })
  },
});

export default chronosFormSlice.reducer;
export const { reset, set, setInputFile } = chronosFormSlice.actions
