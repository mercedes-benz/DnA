import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataProductsApi } from '../../apis/dataproducts.api';
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { deserializeFormData, serializeDivisionSubDivision, serializeFormData } from '../../Utility/formData';

export const GetDataProducts = createAsyncThunk('products/GetDataProducts', async (arg, { getState }) => {
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.getAllDataProducts();
    ProgressIndicator.hide();
    const {
      provideDataProducts: { pagination },
    } = getState(); // redux store method
    return {
      data: res.data.records,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching data products', 'alert');
  }
});

export const SetDataProducts = createAsyncThunk('products/SetDataProducts', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    provideDataProducts: { divisionList, pagination },
  } = data;

  const division = serializeDivisionSubDivision(divisionList, values);

  const requestBody = serializeFormData(values, division);
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.createDataProduct(requestBody);
    onSave();
    const data = deserializeFormData(res?.data?.data);
    ProgressIndicator.hide();
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

export const UpdateDataProducts = createAsyncThunk('products/SetDataProducts', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    provideDataProducts: { divisionList, pagination },
  } = data;

  const division = serializeDivisionSubDivision(divisionList, values);
  const requestBody = serializeFormData(values, division);
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.updateDataProduct(requestBody);
    ProgressIndicator.hide();
    onSave();
    const data = deserializeFormData(res?.data?.data);
    if (values.publish) {
      Notification.show('Your Data Product is now available!');
    } else if (data.openSegments.length === 5) {
      // on saving last tab
      Notification.show('Progress saved in Data Transfer Overview');
    }
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
