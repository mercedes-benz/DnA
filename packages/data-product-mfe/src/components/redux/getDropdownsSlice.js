import { createSlice } from '@reduxjs/toolkit';
import { getClassificationTypes, getLegalBasis } from './getDropdowns.services';

const consumerProductsInitialState = {
  isLegalBasisListLoading: false,
  legalBasisList: [],
  isClassificationTypesLoading: false,
  classificationTypes: [],
};

export const getDropdownsSlice = createSlice({
  name: 'dropdowns',
  initialState: consumerProductsInitialState,
  extraReducers: {
    [getLegalBasis.pending]: (state) => {
      state.isLegalBasisListLoading = true;
    },
    [getLegalBasis.fulfilled]: (state, action) => {
      state.isLegalBasisListLoading = false;
      state.legalBasisList = action.payload.data;
    },
    [getClassificationTypes.pending]: (state) => {
      state.isClassificationTypesLoading = true;
    },
    [getClassificationTypes.fulfilled]: (state, action) => {
      state.isClassificationTypesLoading = false;
      state.classificationTypes = action.payload.data;
    },
  },
});

export default getDropdownsSlice.reducer;
