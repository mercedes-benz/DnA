import { createAsyncThunk } from '@reduxjs/toolkit';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

export const GetProjects = createAsyncThunk('projects/GetProjects', async (arg, { getState }) => {
  ProgressIndicator.show();
  try {
    // const res = await chronosApi.getAllProjects();
    ProgressIndicator.hide();
    const {
      projects: { projects, pagination },
    } = getState(); // redux store method
    return {
      data: projects,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching forecasting projects', 'alert');
  }
});
