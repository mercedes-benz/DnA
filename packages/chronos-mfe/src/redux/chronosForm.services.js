import { createAsyncThunk } from '@reduxjs/toolkit';
import { chronosApi } from '../apis/chronos.api';

export const getConfigFiles = createAsyncThunk('configFiles/fetchConfigFiles', async (projectId) => {
  try {
    const res = await chronosApi.getConfigurationFiles(projectId);
    return res;
  } catch (e) {
    return e.message;
  }
});

