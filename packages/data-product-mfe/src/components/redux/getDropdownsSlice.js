import { createSlice } from '@reduxjs/toolkit';
import {
  getAgileReleaseTrains,
  getCarlaFunctions,
  getClassificationTypes,
  getCorporateDataCatalogs,
  getLegalBasis,
} from './getDropdowns.services';

const consumerProductsInitialState = {
  isLegalBasisListLoading: false,
  legalBasisList: [],
  isClassificationTypesLoading: false,
  classificationTypes: [],
  isAgileReleaseTrainsLoading: false,
  agileReleaseTrains: [],
  isCarLAFunctionsLoading: false,
  carLAFunctions: [],
  isCorporateDataCatalogLoading: false,
  corporateDataCatalogs: [],
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
    [getAgileReleaseTrains.pending]: (state) => {
      state.isAgileReleaseTrainsLoading = true;
    },
    [getAgileReleaseTrains.fulfilled]: (state, action) => {
      state.isAgileReleaseTrainsLoading = false;
      state.agileReleaseTrains = action.payload.data;
    },
    [getCarlaFunctions.pending]: (state) => {
      state.isCarLAFunctionsLoading = true;
    },
    [getCarlaFunctions.fulfilled]: (state, action) => {
      state.isCarLAFunctionsLoading = false;
      state.carLAFunctions = action.payload.data;
    },
    [getCorporateDataCatalogs.pending]: (state) => {
      state.isCorporateDataCatalogLoading = true;
    },
    [getCorporateDataCatalogs.fulfilled]: (state, action) => {
      state.isCorporateDataCatalogLoading = false;
      state.corporateDataCatalogs = action.payload.data;
    },
  },
});

export default getDropdownsSlice.reducer;
