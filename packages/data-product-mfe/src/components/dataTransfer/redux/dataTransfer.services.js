import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataTransferApi } from '../../../apis/datatransfers.api';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { deserializeFormData, serializeDivisionSubDivision, serializeFormData } from '../../../Utility/formData';
import { omit, pick } from 'lodash';

export const GetDataTransfers = createAsyncThunk('transfers/GetDataTransfers', async (isProviderCreatorFilter, { getState }) => {
  ProgressIndicator.show();
  try {
    const res = await dataTransferApi.getAllDataProducts('dataTransferId', 'desc',isProviderCreatorFilter);
    ProgressIndicator.hide();
    const {
      provideDataTransfers: { pagination },
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

export const SetDataTransfers = createAsyncThunk('transfers/SetDataTransfers', async (data, { rejectWithValue }) => {
  const {
    values,
    onSave,
    provideDataTransfers: { divisionList, pagination },
  } = data;

  const division = serializeDivisionSubDivision(divisionList, values);

  const requestBody = serializeFormData({ values, division });
  ProgressIndicator.show();
  try {
    const res = await dataTransferApi.createDataProduct(requestBody);
    onSave();
    const data = deserializeFormData({ item: res?.data?.data });
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

export const UpdateDataTransfers = createAsyncThunk(
  'transfers/UpdateDataTransfers',
  async (data, { rejectWithValue, getState }) => {
    const {
      values,
      onSave,
      provideDataTransfers: { divisionList, pagination },
      type, // "provider" form or "consumer" form
      state, // "edit" or "create"
      isDataProduct,
    } = data;

    const isProviderForm = type === 'provider';
    const isEdit = state === 'edit';
    const division = serializeDivisionSubDivision(divisionList, values);
    if (isProviderForm && values.consumer) {
      values.consumer['serializedDivision'] = serializeDivisionSubDivision(divisionList, values?.consumer);
    }
    const requestBody = serializeFormData({ values, division, type, isDataProduct });

    if (!isDataProduct) {
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
        let data;
        if (type === 'consumer') {
          // access provider info from the store
          const producerData = omit(getState().provideDataTransfers.selectedDataTransfer, ['consumer']);
          // deserialize response and extract only the consumer details
          const consumerData = pick(deserializeFormData({ item: responseData, type }), ['consumer']);
          data = { ...producerData, ...consumerData };
        } else {
          data = deserializeFormData({ item: responseData, type });
        }

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
    }
  },
);
