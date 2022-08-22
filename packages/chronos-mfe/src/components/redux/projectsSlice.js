import { createSlice } from '@reduxjs/toolkit';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import { GetProjects } from './projects.services';

const projectsInitialState = {
  projects: [],
  isLoading: false,
  errors: '',
  pagination: {
    projectListResponse: [],
    totalNumberOfPages: 1,
    currentPageNumber: 1,
    maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
  },
  selectedProject: {},
};

export const projectsSlice = createSlice({
  name: 'projects',
  initialState: projectsInitialState,
  extraReducers: {
    [GetProjects.pending]: (state) => {
      state.isLoading = true;
    },
    [GetProjects.fulfilled]: (state, action) => {
      const totalNumberOfPages = Math.ceil(action.payload.data?.length / action.payload.pagination.maxItemsPerPage);
      const modifiedData = action.payload.data
        ? action.payload.data?.slice(0, action.payload.pagination.maxItemsPerPage)
        : [];
      state.projects = modifiedData;
      state.isLoading = false;
      state.errors = '';
      state.pagination.projectListResponse = action.payload.data;
      state.pagination.totalNumberOfPages = totalNumberOfPages;
      state.pagination.currentPageNumber = 1;
    },
    [GetProjects.rejected]: (state, action) => {
      state.projects = [];
      state.isLoading = false;
      state.errors = action.payload;
    },
  },
  reducers: {
    setSelectedProject: (state, action) => {
      state.selectedProject = action.payload;
    },
    setProjects: (state, action) => {
      state.projects = action.payload;
    },
    setPagination: (state, action) => {
      state.pagination = {
        ...state.pagination,
        ...action.payload,
      };
    },
  },
});

export const { setSelectedProject, setProjects, setPagination } = projectsSlice.actions;
export default projectsSlice.reducer;
