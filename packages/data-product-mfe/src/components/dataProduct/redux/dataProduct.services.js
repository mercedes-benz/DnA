import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataProductApi } from '../../../apis/dataproducts.api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { deserializeFormData, serializeDivisionSubDivision, serializeFormData } from '../../../Utility/formData';

export const GetDataProducts = createAsyncThunk('products/GetDataProducts', async (arg, { getState }) => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllDataProductList('dataProductId', 'desc');
    ProgressIndicator.hide();

    const {
      dataProduct: { pagination },
    } = getState(); // redux store method

    return {
      data: res?.data.records,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching data products', 'alert');
  }
});

export const SetDataProduct = createAsyncThunk(
  'products/SetDataProduct',
  async (data, { rejectWithValue, getState }) => {
    const {
      values,
      onSave,
      data: { divisionList, pagination },
    } = data;

    const division = serializeDivisionSubDivision(divisionList, values);
    const { dropdowns } = getState();

    const requestBody = serializeFormData({ values, division, isDataProduct: true, dropdowns });

    ProgressIndicator.show();
    try {
      const res = await dataProductApi.createDataProduct(requestBody);
      onSave();
      const data = deserializeFormData({ item: res?.data?.data, isDataProduct: true });
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
  },
);

export const UpdateDataProduct = createAsyncThunk(
  'products/UpdateDataProduct',
  async (data, { rejectWithValue, getState }) => {
    const {
      values,
      onSave,
      data: { divisionList, pagination },
      type, // "provider" form or "consumer" form
    } = data;

    const isProviderForm = type === 'provider';

    const division = serializeDivisionSubDivision(divisionList, values);

    if (isProviderForm && values.consumer) {
      values.consumer['serializedDivision'] = serializeDivisionSubDivision(divisionList, values?.consumer);
    }
    const { dropdowns } = getState();
    const requestBody = serializeFormData({ values, division, type, isDataProduct: true, dropdowns });
    ProgressIndicator.show();

    try {
      const res = await dataProductApi.updateDataProduct(requestBody);

      ProgressIndicator.hide();
      onSave();
      const responseData = res?.data?.data;

      const data = deserializeFormData({ item: responseData, type, isDataProduct: true });

      if (responseData?.publish) {
        Notification.show(`Information saved and published sucessfully.`);
      } else {
        Notification.show('Draft saved successfully.');
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
  },
);
