import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  keepForFuture: false,
  inputFile: {},
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
  }
});

export default chronosFormSlice.reducer;
export const { reset, set, setInputFile } = chronosFormSlice.actions
