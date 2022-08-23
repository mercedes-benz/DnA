import { createAsyncThunk } from '@reduxjs/toolkit';

export const getClassificationTypes = createAsyncThunk('consumeDataProducts/getClassificationTypes', async () => {
  const data = [
    { id: '1', name: 'Public' },
    { id: '2', name: 'Confidential' },
    { id: '3', name: 'Internal' },
    { id: '4', name: 'Secret' },
  ];
  return {
    data,
  };
});

export const getLegalBasis = createAsyncThunk('consumeDataProducts/getLegalBasis', async () => {
  const data = [
    { id: 1, name: 'Consent' },
    { id: 2, name: 'Vital interest' },
    { id: 3, name: 'Contract fulfillment' },
    { id: 4, name: 'Public interests' },
    { id: 5, name: 'Legal obligation' },
    { id: 6, name: 'Legitimate interests' },
  ];
  return {
    data,
  };
});
