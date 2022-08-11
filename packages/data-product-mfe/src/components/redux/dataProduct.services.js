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
    provideDataProducts: { divisions, pagination },
  } = data;

  const division = serializeDivisionSubDivision(divisions, values);

  const requestBody = {
    contactInformation: {
      appId: values.planningIT,
      dataTransferDate: new Date(),
      department: values.department,
      division,
      localComplianceOfficer: values.complainceOfficer,
      name: values.name,
    },
    dataProductName: values.productName,
    openSegments: values.openSegments,
    publish: values.publish || false,
  };

  try {
    const res = await dataProductsApi.createDataProduct(requestBody);
    onSave();
    const data = deserializeFormData(res?.data?.data);
    return {
      data,
      pagination,
    };
  } catch (e) {
    Notification.show(e?.response?.data?.errors[0]?.message, 'alert');
    return rejectWithValue(e?.response?.data?.errors[0]?.message);
  }
});

export const UpdateDataProducts = createAsyncThunk('products/SetDataProducts', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    provideDataProducts: { divisions, pagination },
  } = data;

  const division = serializeDivisionSubDivision(divisions, values);
  const requestBody = serializeFormData(values, division);

  try {
    const res = await dataProductsApi.updateDataProduct(requestBody);
    onSave();
    const data = deserializeFormData(res?.data?.data);
    if (values.publish) {
      Notification.show('Your Data Product is now available!');
    }
    return {
      data,
      pagination,
    };
  } catch (e) {
    Notification.show(e?.response?.data?.errors[0]?.message, 'alert');
    return rejectWithValue(e?.response?.data?.errors[0]?.message);
  }
});
