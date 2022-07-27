import { createAsyncThunk } from '@reduxjs/toolkit';
// import { hostServer } from '../../server/api';

export const GetDataProducts = createAsyncThunk('products/GetDataProducts', async (arg, { getState }) => {
  // const res = await hostServer.get('');
  const res = [];
  const {
    dataProducts: { pagination },
  } = getState(); // redux store method
  return {
    data: res,
    pagination,
  };
});

export const SetDataProducts = createAsyncThunk('products/SetDataProducts', async (data, { getState }) => {
  const {
    dataProducts: { pagination, dataProducts },
  } = getState();

  return await {
    data: dataProducts.length > 0 ? [...dataProducts, ...data] : [data],
    pagination,
  };
});
