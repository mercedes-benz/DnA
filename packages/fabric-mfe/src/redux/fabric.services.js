import { createAsyncThunk } from '@reduxjs/toolkit';
import { fabricApi } from '../apis/fabric.api';

export const getWorkspaces = createAsyncThunk('projects/getWorkspaces', async () => {
  try {
    const res = await fabricApi.getWorkspaces();
    return res;
  } catch (e) {
    return e.message;
  }
});
