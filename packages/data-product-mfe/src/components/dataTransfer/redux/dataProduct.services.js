import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataTransferApi } from '../../../apis/dataproducts.api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { deserializeFormData, serializeDivisionSubDivision, serializeFormData } from '../../../Utility/formData';

export const GetDataProducts = createAsyncThunk('products/GetDataProducts', async (arg, { getState }) => {
  ProgressIndicator.show();
  try {
    const res = await dataTransferApi.getAllDataProducts('dataProductId', 'desc');
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
    const res = await dataTransferApi.createDataProduct(requestBody);
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

export const UpdateDataProducts = createAsyncThunk('products/SetDataProducts', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    provideDataProducts: { divisionList, pagination },
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
      res = await dataTransferApi.updateProvider(requestBody);
    } else {
      res = await dataTransferApi.updateConsumer(requestBody);
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
