import { createAsyncThunk } from '@reduxjs/toolkit';
import { dataProductApi } from '../../apis/dataproducts.api';
import { dataTransferApi } from '../../apis/datatransfers.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

export const getClassificationTypes = createAsyncThunk('dropdowns/getClassificationTypes', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataTransferApi.getAllClassificationTypes();
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
    const res = await dataTransferApi.getAllLegalBasis();
    ProgressIndicator.hide();
    return {
      data: res.data?.data,
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching list of legal basis', 'alert');
  }
});

export const getAgileReleaseTrains = createAsyncThunk('dropdowns/getAgileReleaseTrains', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllAgileReleaseTrains();
    ProgressIndicator.hide();
    return {
      data: res.data?.data || [],
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(
      e.response?.data?.errors?.[0]?.message || 'Error while fetching list of agile release trains',
      'alert',
    );
  }
});

export const getCarlaFunctions = createAsyncThunk('dropdowns/getCarlaFunctions', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllCarlaFunctions();
    ProgressIndicator.hide();
    return {
      data: res.data?.data || [],
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(
      e.response?.data?.errors?.[0]?.message || 'Error while fetching list of carLA functions',
      'alert',
    );
  }
});

export const getCorporateDataCatalogs = createAsyncThunk('dropdowns/getCorporateDataCatalogs', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllCorporateDataCatalogs();
    ProgressIndicator.hide();
    return {
      data: res.data || [],
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(
      e.response?.data?.errors?.[0]?.message || 'Error while fetching list of corporate data catalogs',
      'alert',
    );
  }
});

export const getPlatforms = createAsyncThunk('dropdowns/getPlatforms', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllPlatforms();
    ProgressIndicator.hide();
    return {
      data: res.data?.data || [],
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(e.response?.data?.errors?.[0]?.message || 'Error while fetching list of platforms', 'alert');
  }
});

export const getFrontEndTools = createAsyncThunk('dropdowns/getFrontEndTools', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllFrontEndTools();
    ProgressIndicator.hide();
    return {
      data: res.data?.data || [],
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(
      e.response?.data?.errors?.[0]?.message || 'Error while fetching list of front end tools',
      'alert',
    );
  }
});

export const getTags = createAsyncThunk('dropdowns/getTags', async () => {
  ProgressIndicator.show();
  try {
    const res = await dataProductApi.getAllTags();
    ProgressIndicator.hide();
    return {
      data: res.data || [],
    };
  } catch (e) {
    ProgressIndicator.hide();
    Notification.show(
      e.response?.data?.errors?.[0]?.message || 'Error while fetching list of tags',
      'alert',
    );
  }
});
