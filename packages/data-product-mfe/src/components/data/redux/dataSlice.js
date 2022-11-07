import { createSlice } from '@reduxjs/toolkit';
import { SESSION_STORAGE_KEYS } from '../../../Utility/constants';
import { GetData, SetData, UpdateData } from './data.services';

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
  name: 'data',
  initialState: dataInitialState,
  extraReducers: {
    [GetData.pending]: (state) => {
      state.isLoading = true;
    },
    [GetData.fulfilled]: (state, action) => {
      const totalNumberOfPages = Math.ceil(action.payload.data?.length / action.payload.pagination.maxItemsPerPage);
      const modifiedData = action.payload.data
        ? action.payload.data?.slice(0, action.payload.pagination.maxItemsPerPage)
        : [];
      state.data = modifiedData;
      state.isLoading = false;
      state.errors = '';
      state.pagination.dataListResponse = action.payload.data;
      state.pagination.totalNumberOfPages = totalNumberOfPages;
      state.pagination.currentPageNumber = 1;
    },
    [GetData.rejected]: (state, action) => {
      state.data = [];
      state.isLoading = false;
      state.errors = action.payload;
    },
    [SetData.pending]: (state) => {
      state.isLoading = true;
    },
    [SetData.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedData = action.payload?.data;
      state.errors = '';
    },
    [SetData.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
    [UpdateData.pending]: (state) => {
      state.isLoading = true;
    },
    [UpdateData.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.selectedData = action.payload.data;
      state.errors = '';
    },
    [UpdateData.rejected]: (state, action) => {
      state.isLoading = false;
      state.errors = action.payload;
    },
  },
  reducers: {
    setDivisionList: (state, action) => {
      state.divisionList = action.payload;
    },
    setData: (state, action) => {
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

export const { setPagination, setData, setSelectedData, setDivisionList } = dataSlice.actions;
export default dataSlice.reducer;
