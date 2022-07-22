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
