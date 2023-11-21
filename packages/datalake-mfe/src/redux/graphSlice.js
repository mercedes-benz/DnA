import { createSlice } from '@reduxjs/toolkit';
import { getProjectDetails } from './graph.services';

const graphInitialState = {
  isLoading: false,
  box: { x: 0, y: 0, w: 0, h: 0, clientW: 0, clientH: 0 },
  version: 'currentVersion',
  project: {
    tables: []
  },
  errors: '',
  editingTable: {},
};

export const graphSlice = createSlice({
  name: 'graph',
  initialState: graphInitialState,
  reducers: {
    setTables: (state, action) => {
      state.project.tables = [...action.payload];
    },
    setBox: (state, action) => {
      state.box = {...action.payload};
    },
    setVersion: (state, action) => {
      state.version = {...action.payload};
    },
    // setEditingTable: (state, action) => {
    //   state.editingTable = {...action.payload};
    // },
    // setEditingField: (state, action) => {
    //   state.editingField = {...action.payload};
    // },
    // setAddingField: (state, action) => {
    //   state.addingField = {...action.payload};
    // },
    addTable: (state, action) => {
      const project = action.payload;
      const projects = localStorage.getItem('graphs') ? JSON.parse(localStorage.getItem('graphs')) : [];
      const updatedProjects = projects.filter((graph) => {
        return graph.id !== project.id;
      });
      localStorage.setItem('graphs', JSON.stringify([...updatedProjects, project]));
    },
  },
  extraReducers: {
    [getProjectDetails.pending]: (state) => {
      state.isLoading = true;
    },
    [getProjectDetails.fulfilled]: (state, action) => {
      state.isLoading = false;
      state.project = action.payload.data;
      state.errors = '';
    },
    [getProjectDetails.rejected]: (state, action) => {
      state.isLoading = false;
      state.project = {};
      state.errors = action.payload;
    },
  },
});

export const { 
  setTables,
  setBox,
  setVersion
} = graphSlice.actions;
export default graphSlice.reducer;
