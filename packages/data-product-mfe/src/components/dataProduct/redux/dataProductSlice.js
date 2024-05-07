import { createSlice } from '@reduxjs/toolkit';
import { SESSION_STORAGE_KEYS } from '../../../Utility/constants';
import {
  GetDataProducts,
  SetDataProduct,
  UpdateDataProduct,
  CompleteDataProductMinimumInfo,
  SetAllAssociatedDataTransfers,
  SetMyAssociatedDataTransfers,
} from './dataProduct.services';

const dataInitialState = {
  data: [],
  totalNumberOfRecords: 1,
  isLoading: false,
  errors: '',
  pagination: {
    dataListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  },
  selectedDataProduct: {},
  divisionList: [],
  allDataTransfer: {
    totalCount: 0,
    records: [],
  },
  myDataTransfer: {
    totalCount: 0,
    records: [],
  },
  FilterQueryParams: {
    arts: '',
    platforms: '',
    frontendTools: '',
    divisions: '',
    departments: '',
    productOwners: '',
    informationOwners: '',
    dataStewards: '',
    offset: 0,
    limit: 15
  }
};

export const dataSlice = createSlice({
  name: 'products',
  initialState: dataInitialState,
  extraReducers: {
    [GetDataProducts.pending]: (state) => {
      state.isLoading = true;
    },
    [GetDataProducts.fulfilled]: (state, action) => {
      const totalNumberOfPages = Math.ceil(action.payload?.totalNumberOfRecords / action.payload?.pagination.maxItemsPerPage);
      const modifiedData = action.payload?.data ? action.payload.data: []
      state.data = modifiedData;
      state.isLoading = false;
      state.errors = '';
      state.pagination.dataListResponse = action.payload?.data || [];
      state.pagination.totalNumberOfPages = totalNumberOfPages;
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
      state.selectedDataProduct = action.payload?.data;
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
      state.selectedDataProduct = action.payload.data;
      state.errors = '';
    },
    [UpdateDataProduct.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
    [CompleteDataProductMinimumInfo.pending]: (state) => {
      state.isLoading = true;
    },
    [CompleteDataProductMinimumInfo.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedDataProduct = action.payload.data;
      state.errors = '';
    },
    [CompleteDataProductMinimumInfo.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
    [SetAllAssociatedDataTransfers.pending]: (state) => {
      state.isLoading = true;
    },
    [SetAllAssociatedDataTransfers.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.allDataTransfer = action.payload?.data;
      state.errors = '';
    },
    [SetAllAssociatedDataTransfers.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
    [SetMyAssociatedDataTransfers.pending]: (state) => {
      state.isLoading = true;
    },
    [SetMyAssociatedDataTransfers.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.myDataTransfer = action.payload?.data;
      state.errors = '';
    },
    [SetMyAssociatedDataTransfers.rejected]: (state, action) => {
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
    setSelectedDataProduct: (state, action) => {
      state.selectedDataProduct = action.payload;
    },
    setPagination: (state, action) => {
      state.pagination = {
        ...state.pagination,
        ...action.payload,
      };
    },
    setFilterQueryParams : (state, action) => {
      state.FilterQueryParams = {
        ...state.FilterQueryParams,
        ...action.payload,
      };
    },
    setTotalNumberOfRecords: (state, action) =>{
      state.totalNumberOfRecords = action.payload;
    },
    resetDataTransferList: (state) => {
      state.allDataTransfer = {
        totalCount: 0,
        records: [],
      };
      state.myDataTransfer = {
        totalCount: 0,
        records: [],
      };
    },
  },
});

export const { setPagination, setDataProductList, setSelectedDataProduct, setDivisionList, setFilterQueryParams } =
  dataSlice.actions;
export default dataSlice.reducer;
