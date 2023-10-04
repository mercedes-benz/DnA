import { createAsyncThunk } from '@reduxjs/toolkit';

export const getProjectDetails = createAsyncThunk('projects/getProjectDetails', (id) => {
  const res = localStorage.getItem('graphs') ? JSON.parse(localStorage.getItem('graphs')) : [];
  let filteredProject;
  if(res.length > 0) {
    filteredProject = res.filter((project) => {
        return project.id === id;
    });
  }
  return {
    data: filteredProject[0],
  };
});
