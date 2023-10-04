import { createAsyncThunk } from '@reduxjs/toolkit';
import { chronosApi } from '../apis/chronos.api';

export const getProjectDetails = createAsyncThunk('projectDetails/fetchProjectDetails', async (projectId) => {
  try {
    const res = await chronosApi.getForecastProjectById(projectId);
    return res.data;
  } catch (e) {
    let error = '';
    if(e.response.status === 404) {
      error = 'Chronos project not found either it is deleted or not present', 'alert';
    } else if(e.response.status === 403) {
      error = 'You do not have access to this Chronos project.', 'alert';
    } else {
      error = e.message;
    }
    return error;
  }
});

