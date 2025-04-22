import { createAsyncThunk } from '@reduxjs/toolkit';
import { dbServiceApi } from '../apis/dbservice.api';

export const getDBService = createAsyncThunk('service/getDBService', async (id) => {
  try {
    const res = await dbServiceApi.getWorkspace(id);
    return res;
  } catch (e) {
    return e.message;
  }
});
