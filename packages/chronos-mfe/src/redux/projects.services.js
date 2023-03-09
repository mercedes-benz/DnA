import { createAsyncThunk } from '@reduxjs/toolkit';
import Notification from '../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../apis/chronos.api';

export const GetProjects = createAsyncThunk('projects/GetProjects', async () => {
  ProgressIndicator.show();
  try {
    const res = await chronosApi.getAllForecastProjects();
    ProgressIndicator.hide();
    const projects = res.status === 204 ? [] : res.data.records;
    return {
      data: projects,
    };
  } catch (e) {
    ProgressIndicator.hide();
    // Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching forecasting projects', 'alert');
    return {
      data: [],
    };
  }
});

export const SetProject = createAsyncThunk('projects/SetProject', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    projects: { pagination },
  } = data;

  const requestBody = values;
  ProgressIndicator.show();
  try {
    const res = await chronosApi.createForecastProject(requestBody);
    onSave();
    const data = res?.data?.data;
    ProgressIndicator.hide();
    Notification.show('Draft saved successfully.');
    return {
      data,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e?.response?.data?.errors[0]?.message, 'alert');
    return rejectWithValue(e?.response?.data?.errors[0]?.message);
  }
});