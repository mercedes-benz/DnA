import { createSlice } from '@reduxjs/toolkit';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import { GetDataProducts, SetDataProducts, UpdateDataProducts } from './dataProduct.services';

const dataProductsInitialState = {
  dataProducts: [],
  isLoading: false,
  errors: '',
  pagination: {
    dataProductListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  },
  selectedDataProduct: {},
  divisionList: [],
};

export const provideDataProductSlice = createSlice({
  name: 'products',
  initialState: dataProductsInitialState,
  extraReducers: {
    [GetDataProducts.pending]: (state) => {
      state.isLoading = true;
    },
    [GetDataProducts.fulfilled]: (state, action) => {
      const totalNumberOfPages = Math.ceil(action.payload.data?.length / action.payload.pagination.maxItemsPerPage);
      const modifiedData = action.payload.data
        ? action.payload.data?.slice(0, action.payload.pagination.maxItemsPerPage)
        : [];
      state.dataProducts = modifiedData;
      state.isLoading = false;
      state.errors = '';
      state.pagination.dataProductListResponse = action.payload.data;
      state.pagination.totalNumberOfPages = totalNumberOfPages;
      state.pagination.currentPageNumber = 1;
    },
    [GetDataProducts.rejected]: (state, action) => {
      state.dataProducts = [];
      state.isLoading = false;
      state.errors = action.payload;
    },
    [SetDataProducts.pending]: (state) => {
      state.isLoading = true;
    },
    [SetDataProducts.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedDataProduct = action.payload?.data;
      state.errors = '';
    },
    [SetDataProducts.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
    [UpdateDataProducts.pending]: (state) => {
      state.isLoading = true;
    },
    [UpdateDataProducts.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedDataProduct = action.payload.data;
      state.errors = '';
    },
    [UpdateDataProducts.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
  },
  reducers: {
    setDivisionList: (state, action) => {
      state.divisionList = action.payload;
    },
    setDataProducts: (state, action) => {
      state.dataProducts = action.payload;
    },
    setDataProduct: (state, action) => {
      state.selectedDataProduct = action.payload;
    },
    setPagination: (state, action) => {
      state.pagination = {
        ...state.pagination,
        ...action.payload,
      };
    },
  },
});

export const { setDivisionList, setDataProducts, setDataProduct, setPagination } = provideDataProductSlice.actions;
export default provideDataProductSlice.reducer;
