import { createSlice } from '@reduxjs/toolkit';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import { GetDataProducts } from './dataProduct.services';

const dataProductsInitialState = {
  dataProducts: [],
  isLoading: false,
  errors: '',
  pagination: {
    dataProductListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 1) || 1,
  },
};

export const dataProductSlice = createSlice({
  name: 'products',
  initialState: dataProductsInitialState,
  extraReducers: {
    [GetDataProducts.pending]: (state) => {
      state.isLoading = true;
    },
    [GetDataProducts.fulfilled]: (state, action) => {
      const totalNumberOfPages = Math.ceil(
        action.payload.data?.data.length / action.payload.pagination.maxItemsPerPage,
      );
      const modifiedData = action.payload.data
        ? action.payload.data.data.slice(0, action.payload.pagination.maxItemsPerPage)
        : [];
      state.dataProducts = modifiedData;
      state.isLoading = false;
      state.errors = '';
      state.pagination.dataProductListResponse = action.payload.data.data;
      state.pagination.totalNumberOfPages = totalNumberOfPages;
      state.pagination.currentPageNumber = 1;
    },
    [GetDataProducts.rejected]: (state, action) => {
      state.dataProducts = [];
      state.isLoading = false;
      state.errors = action.payload;
    },
  },
  reducers: {
    setDataProducts: (state, action) => {
      state.dataProducts = action.payload;
    },
    setPagination: (state, action) => {
      state.pagination = {
        ...state.pagination,
        ...action.payload,
      };
    },
  },
});
export const { setDataProducts, setPagination } = dataProductSlice.actions;
export default dataProductSlice.reducer;
