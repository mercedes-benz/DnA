import { createSlice } from '@reduxjs/toolkit';
import { SESSION_STORAGE_KEYS } from '../../../Utility/constants';
import { GetDataTransfers, SetDataTransfers, UpdateDataTransfers } from './dataTransfer.services';

const dataProductsInitialState = {
  dataTransfers: [],
  isLoading: false,
  firstDataLoaded: false,
  errors: '',
  pagination: {
    dataProductListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  },
  selectedDataTransfer: {},
  divisionList: [],
};

export const provideDataTransferSlice = createSlice({
  name: 'transfers',
  initialState: dataProductsInitialState,
  extraReducers: {
    [GetDataTransfers.pending]: (state) => {
      state.isLoading = true;
    },
    [GetDataTransfers.fulfilled]: (state, action) => {
      const totalNumberOfPages = Math.ceil(action.payload.data?.length / action.payload.pagination.maxItemsPerPage);
      const modifiedData = action.payload.data
        ? action.payload.data?.slice(0, action.payload.pagination.maxItemsPerPage)
        : [];
      state.dataTransfers = modifiedData;
      state.isLoading = false;
      state.firstDataLoaded = true;
      state.errors = '';
      state.pagination.dataProductListResponse = action.payload.data;
      state.pagination.totalNumberOfPages = totalNumberOfPages;
      state.pagination.currentPageNumber = 1;
    },
    [GetDataTransfers.rejected]: (state, action) => {
      state.dataTransfers = [];
      state.isLoading = false;
      state.errors = action.payload;
    },
    [SetDataTransfers.pending]: (state) => {
      state.isLoading = true;
    },
    [SetDataTransfers.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedDataTransfer = action.payload?.data;
      state.errors = '';
    },
    [SetDataTransfers.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
    [UpdateDataTransfers.pending]: (state) => {
      state.isLoading = true;
    },
    [UpdateDataTransfers.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedDataTransfer = action.payload.data;
      state.errors = '';
    },
    [UpdateDataTransfers.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
  },
  reducers: {
    setDivisionList: (state, action) => {
      state.divisionList = action.payload;
    },
    setDataTransferList: (state, action) => {
      state.dataTransfers = action.payload;
    },
    setSelectedDataTransfer: (state, action) => {
      state.selectedDataTransfer = action.payload;
    },
    setPagination: (state, action) => {
      state.pagination = {
        ...state.pagination,
        ...action.payload,
      };
    },
  },
});

export const { setDivisionList, setDataTransferList, setSelectedDataTransfer, setPagination } =
  provideDataTransferSlice.actions;
export default provideDataTransferSlice.reducer;
