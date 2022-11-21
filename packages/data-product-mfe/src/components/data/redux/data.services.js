import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataProductsApi } from '../../../apis/dataproducts.api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { deserializeFormData, serializeDivisionSubDivision, serializeFormData } from '../../../Utility/formData';

import dummyData from '../data.json';

export const GetData = createAsyncThunk('data/GetData', async (arg, { getState }) => {
  ProgressIndicator.show();
  try {
    // const res = await dataProductsApi.getAllDataProducts('dataProductId', 'desc');
    ProgressIndicator.hide();
    const {
      data: { pagination },
    } = getState(); // redux store method
    return {
      data: dummyData,
      pagination,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching data products', 'alert');
  }
});

export const SetData = createAsyncThunk('data/SetData', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    data: { divisionList, pagination },
  } = data;

  const division = serializeDivisionSubDivision(divisionList, values);

  const requestBody = serializeFormData(values, division);
  ProgressIndicator.show();
  try {
    const res = await dataProductsApi.createDataProduct(requestBody);
    onSave();
    const data = deserializeFormData(res?.data?.data);
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

export const UpdateData = createAsyncThunk('data/SetData', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    data: { divisionList, pagination },
    type, // "provider" form or "consumer" form
    state, // "edit" or "create"
  } = data;

  const isProviderForm = type === 'provider';
  const isEdit = state === 'edit';
  const division = serializeDivisionSubDivision(divisionList, values);
  if (isProviderForm && values.consumer) {
    values.consumer['serializedDivision'] = serializeDivisionSubDivision(divisionList, values?.consumer);
  }
  const requestBody = serializeFormData(values, division, type);
  ProgressIndicator.show();
  try {
    let res = {};
    if (isProviderForm) {
      res = await dataProductsApi.updateProvider(requestBody);
    } else {
      res = await dataProductsApi.updateConsumer(requestBody);
    }
    ProgressIndicator.hide();
    onSave();
    const responseData = res?.data?.data;
    const data = deserializeFormData(responseData, type);
    // Provider Form
    if (isProviderForm) {
      if (responseData?.providerInformation?.providerFormSubmitted) {
        Notification.show(
          responseData?.notifyUsers
            ? `Information saved${
                responseData?.publish ? ' and published' : ''
              } sucessfully.\n Members will be notified${responseData?.publish ? '.' : ' on the data transfer.'}`
            : isEdit
            ? 'Information saved sucessfully.'
            : 'Progress saved in Data Transfer Overview',
        );
      } else {
        Notification.show('Draft saved successfully.');
      }
    }
    // Consumer Form
    if (!isProviderForm) {
      if (responseData?.publish) {
        Notification.show(
          responseData?.notifyUsers
            ? 'Information saved and published sucessfully.\n Members will be notified.'
            : isEdit
            ? 'Information saved sucessfully.'
            : 'Transfer is now complete!',
        );
      } else Notification.show('Draft saved successfully.');
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
