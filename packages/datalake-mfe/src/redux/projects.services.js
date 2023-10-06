import { createAsyncThunk } from '@reduxjs/toolkit';

export const getProjects = createAsyncThunk('projects/getProjects', () => {
  const res = localStorage.getItem('graphs') ? JSON.parse(localStorage.getItem('graphs')) : [];
  console.log('res');
  console.log(res);
  return {
    data: res,
  };
});
