import { createSlice } from '@reduxjs/toolkit';
import { getLegalBasis } from './consumeDataProduct.services';

const consumerProductsInitialState = {
  isLoading: false,
  legalBasisList: [],
};

export const consumeDataProductSlice = createSlice({
  name: 'consumeDataProducts',
  initialState: consumerProductsInitialState,
  extraReducers: {
    [getLegalBasis.pending]: (state) => {
      state.isLoading = true;
    },
    [getLegalBasis.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.legalBasisList = action.payload.data;
    },
  },
});

export default consumeDataProductSlice.reducer;
