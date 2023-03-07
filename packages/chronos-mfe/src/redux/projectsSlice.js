import { createSlice } from '@reduxjs/toolkit';
import { GetProjects } from './projects.services';

const projectsInitialState = {
  isLoading: false,
  projects: [],
  errors: '',
};

export const projectsSlice = createSlice({
  name: 'projects',
  initialState: projectsInitialState,
  extraReducers: {
    [GetProjects.pending]: (state) => {
      state.isLoading = true;
    },
    [GetProjects.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.projects = action.payload.data;
      state.errors = '';
    },
    [GetProjects.rejected]: (state, action) => {
      state.isLoading = false;
      state.projects = [];
      state.errors = action.payload;
    },
  },
  reducers: {
    setProjects: (state, action) => {
      state.projects = action.payload;
    },
  },
});

export const { setProjects } = projectsSlice.actions;
export default projectsSlice.reducer;
