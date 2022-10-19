import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataProductsApi } from '../../apis/dataproducts.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

export const getClassificationTypes = createAsyncThunk('dropdowns/getClassificationTypes', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.getAllClassificationTypes();
    ProgressIndicator.hide();
    return {
      data: res.data?.data,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(
      e.response?.data?.errors?.[0]?.message || 'Error while fetching list of classification types',
      'alert',
    );
  }
});

export const getLegalBasis = createAsyncThunk('dropdowns/getLegalBasis', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.getAllLegalBasis();
    ProgressIndicator.hide();
    return {
      data: res.data?.data,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching list of legal basis', 'alert');
  }
});
