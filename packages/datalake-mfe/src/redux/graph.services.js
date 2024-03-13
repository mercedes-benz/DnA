import { createAsyncThunk } from '@reduxjs/toolkit';
import { datalakeApi } from '../apis/datalake.api';

export const getProjectDetails = createAsyncThunk('projects/getProjectDetails', async (id) => {
  try {
    const res = await datalakeApi.getDatalakeProject(id);
    return res;
  } catch (e) {
    return e.message;
  }
});
