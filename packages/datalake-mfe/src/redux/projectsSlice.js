import { createSlice } from '@reduxjs/toolkit';
import { getProjects } from './projects.services';

const projectsInitialState = {
  isLoading: false,
  projects: [],
  errors: '',
};

export const projectsSlice = createSlice({
  name: 'projects',
  initialState: projectsInitialState,
  extraReducers: {
    [getProjects.pending]: (state) => {
      state.isLoading = true;
    },
    [getProjects.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.projects = action.payload.data;
      state.errors = '';
    },
    [getProjects.rejected]: (state, action) => {
      state.isLoading = false;
      state.projects = [];
      state.errors = action.payload;
    },
  },
  reducers: {
    addProject: (state, action) => {
      const project = action.payload;
      const projects = localStorage.getItem('graphs') ? JSON.parse(localStorage.getItem('graphs')) : [];
      localStorage.setItem('graphs', JSON.stringify([...projects, project]));
    },
    deleteProject: (state, action) => {
      const id = action.payload;
      const projects = localStorage.getItem('graphs') ? JSON.parse(localStorage.getItem('graphs')) : [];
      const removeItem = projects.filter((graph) => {
        return graph.id !== id;
      });
      localStorage.setItem('graphs', JSON.stringify([...removeItem]));
    },
  },
});

export const { addProject, deleteProject } = projectsSlice.actions;
export default projectsSlice.reducer;
