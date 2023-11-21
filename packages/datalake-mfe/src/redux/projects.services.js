import { createAsyncThunk } from '@reduxjs/toolkit';
import { datalakeApi } from '../apis/datalake.api';

export const getProjects = createAsyncThunk('projects/getProjects', async () => {
  try {
    const res = await datalakeApi.getDatalakeProjectsList();
    return res;
  } catch (e) {
    return e.message;
  }
});
