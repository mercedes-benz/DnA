import { createSlice } from '@reduxjs/toolkit';
import { SESSION_STORAGE_KEYS } from '../../../Utility/constants';
import { GetDataProducts, SetDataProduct, UpdateDataProduct } from './dataProduct.services';

const dataInitialState = {
  data: [],
  isLoading: false,
  errors: '',
  pagination: {
    dataListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  },
  selectedData: {},
  divisionList: [],
};

export const dataSlice = createSlice({
  name: 'products',
  initialState: dataInitialState,
  extraReducers: {
    [GetDataProducts.pending]: (state) => {
      state.isLoading = true;
    },
    [GetDataProducts.fulfilled]: (state, action) => {
      const totalNumberOfPages = Math.ceil(action.payload?.data?.length / action.payload?.pagination.maxItemsPerPage);
      const modifiedData = action.payload?.data
        ? action.payload.data?.slice(0, action.payload.pagination.maxItemsPerPage)
        : [];
      // let objectArr = [...state.data, ...modifiedData];
      // state.data = [...new Set(objectArr.map((o) => JSON.stringify(o)))].map((str) => JSON.parse(str));
      state.data = modifiedData;
      state.isLoading = false;
      state.errors = '';
      state.pagination.dataListResponse = action.payload?.data || [];
      state.pagination.totalNumberOfPages = totalNumberOfPages;
      state.pagination.currentPageNumber = 1;
    },
    [GetDataProducts.rejected]: (state, action) => {
      state.data = [];
      state.isLoading = false;
      state.errors = action.payload;
    },
    [SetDataProduct.pending]: (state) => {
      state.isLoading = true;
    },
    [SetDataProduct.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.data = [...state.data, action.payload.data];
      state.selectedData = action.payload?.data;
      state.errors = '';
    },
    [SetDataProduct.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
    [UpdateDataProduct.pending]: (state) => {
      state.isLoading = true;
    },
    [UpdateDataProduct.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedData = action.payload.data;
      state.errors = '';
    },
    [UpdateDataProduct.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
  },
  reducers: {
    setDivisionList: (state, action) => {
      state.divisionList = action.payload;
    },
    setDataProductList: (state, action) => {
      state.data = action.payload;
    },
    setSelectedData: (state, action) => {
      state.selectedData = action.payload;
    },
    setPagination: (state, action) => {
      state.pagination = {
        ...state.pagination,
        ...action.payload,
      };
    },
  },
});

export const { setPagination, setDataProductList, setSelectedData, setDivisionList } = dataSlice.actions;
export default dataSlice.reducer;
