import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataProductApi } from '../../../apis/dataproducts.api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import {
  deserializeFormData,
  serializeDivisionSubDivision,
  serializeFormData,
  stringArrayToObjectArray,
} from '../../../Utility/formData';

export const GetDataProducts = createAsyncThunk('products/GetDataProducts', async (arg, { getState }) => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllDataProductList('dataProductName', 'asc', arg);
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
      currentTab // "provider" form or "consumer" form
    } = data;

    const division = serializeDivisionSubDivision(divisionList, values);
    const { dropdowns } = getState();

    const platforms = stringArrayToObjectArray(values.platform, dropdowns.platforms);
    const frontEndTools = stringArrayToObjectArray(values.frontEndTools, dropdowns.frontEndTools);

    // Update few of the values
    values['platform'] = platforms;
    values['frontEndTools'] = frontEndTools;

    const requestBody = serializeFormData({ values, division, isDataProduct: true, dropdowns, currentTab });

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
      type,
      currentTab // "provider" form or "consumer" form
    } = data;

    const isProviderForm = type === 'provider';
    const { dropdowns } = getState();

    const division = serializeDivisionSubDivision(divisionList, values);

    const platforms = stringArrayToObjectArray(values.platform, dropdowns.platforms);
    const frontEndTools = stringArrayToObjectArray(values.frontEndTools, dropdowns.frontEndTools);

    if (isProviderForm && values.consumer) {
      values.consumer['serializedDivision'] = serializeDivisionSubDivision(divisionList, values?.consumer);
    }

    // Update few of the values
    values['platform'] = platforms;
    values['frontEndTools'] = frontEndTools;

    if(values?.informationOwner == ''){
      delete values?.informationOwner
    }

    const requestBody = serializeFormData({ values, division, type, isDataProduct: true, dropdowns, currentTab });
    ProgressIndicator.show();

    try {
      const res = await dataProductApi.updateDataProduct(requestBody);

      ProgressIndicator.hide();
      onSave();
      const responseData = res?.data?.data;

      const data = deserializeFormData({ item: responseData, type, isDataProduct: true });

      if (responseData?.isPublish) {
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

export const CompleteDataProductMinimumInfo = createAsyncThunk(
  'products/CompleteDataProductMinimumInfo',
  async (data, { rejectWithValue, getState }) => {
    const {
      values,
      onSave,
      provideDataTransfers: { divisionList, pagination },
      type, // "provider" form or "consumer" form
      // state, // "edit" or "create"
      isDataProduct,
    } = data;

    const {
      dataProduct: { selectedDataProduct },
    } = getState();

    const isProviderForm = type === 'provider';
    // const isEdit = state === 'edit';
    const division = serializeDivisionSubDivision(divisionList, values);
    if (isProviderForm && values.consumer) {
      values.consumer['serializedDivision'] = serializeDivisionSubDivision(divisionList, values?.consumer);
    }
    const requestBody = serializeFormData({ values, division, type, isDataProduct });

    try {
      const id = values.id;
      if (requestBody?.consumerInformation?.openSegments?.includes('IdentifyingPersonalRelatedData')) {
        ProgressIndicator.show();
        const res = await dataProductApi.createDataTransfer(id, requestBody);
        ProgressIndicator.hide();
        const responseData = res?.data?.data;
        const data = deserializeFormData({ item: responseData, type, isDataProduct });
        return {
          data,
          pagination,
        };
      } else {
        onSave();
        return {
          data: selectedDataProduct,
          pagination,
        };
      }
    } catch (e) {
      ProgressIndicator.hide();
      Notification.show(e?.response?.data?.errors[0]?.message, 'alert');
      return rejectWithValue(e?.response?.data?.errors[0]?.message);
    }
  },
);

export const SetAllAssociatedDataTransfers = createAsyncThunk(
  'products/SetAllAssociatedDataTransfers',
  async (data, { getState }) => {
    const {
      dataProduct: { pagination },
    } = getState(); // redux store method
    const ids = data?.reduce((acc, curr, index, arr) => {
      acc += `'${curr.datatransferId}'${index === arr.length - 1 ? '' : ','}`;
      return acc;
    }, '');

    ProgressIndicator.show();
    try {
      const res = await dataProductApi.getAllDataTransfers(ids, 'dataTransferId', 'desc');
      ProgressIndicator.hide();
      return {
        data: res?.data,
        pagination,
      };
    } catch (e) {
      ProgressIndicator.hide();
      Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching data products', 'alert');
    }
  },
);

export const SetMyAssociatedDataTransfers = createAsyncThunk(
  'products/SetMyAssociatedDataTransfers',
  async (data, { getState }) => {
    const {
      dataProduct: { pagination },
    } = getState(); // redux store method
    const ids = data?.reduce((acc, curr, index, arr) => {
      acc += `'${curr.datatransferId}'${index === arr.length - 1 ? '' : ','}`;
      return acc;
    }, '');

    ProgressIndicator.show();
    try {
      const res = await dataProductApi.getMyDataTransfers(ids, 'dataTransferId', 'desc');
      ProgressIndicator.hide();
      return {
        data: res?.data,
        pagination,
      };
    } catch (e) {
      ProgressIndicator.hide();
      Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching data products', 'alert');
    }
  },
);
