import { createSlice } from '@reduxjs/toolkit';
import { getConfigFiles } from './chronosForm.services';

const initialState = {
  isLoading: false,
  keepForFuture: false,
  inputFile: {},
  configFiles: [],
  errors: '',
};

export const chronosFormSlice = createSlice({
  name: 'chronosForm',
  initialState,
  reducers: {
    reset: state => {
      state.keepForFuture = false;
      state.inputFile = {};
    },
    set: (state, action) => {
      state.keepForFuture = action.payload;
    },
    setInputFile: (state, action) => {
      state.inputFile = action.payload;
    }
  },
  extraReducers: builder => {
    builder.addCase(getConfigFiles.pending, state => {
      state.isLoading = true;
    });

    builder.addCase(getConfigFiles.fulfilled, (state, action) => {
      state.isLoading = false;
      if(typeof action.payload === 'string') {
        state.configFiles = [];
        state.errors = action.payload;
      } else {
        const bucketObjects = action.payload.data.data.bucketObjects ? [...action.payload.data.data.bucketObjects] : [];
        bucketObjects.sort((a, b) => {
          let fa = a.objectName.toLowerCase(),
              fb = b.objectName.toLowerCase();
          if (fa < fb) {
            return -1;
          }
          if (fa > fb) {
            return 1;
          }
          return 0;
        });

        const coreConfigs = bucketObjects.filter(file => file.objectName.includes('chronos-core'));
        const projectConfigs = bucketObjects.filter(file => !(file.objectName.includes('chronos-core')));

        const coreConfigsSorted = coreConfigs.filter(file => file.objectName !== 'chronos-core/configs/OPTIMISATION_CONFIG.yml');
        const coreConfigsDefault = coreConfigsSorted.filter(file => file.objectName !== 'chronos-core/configs/default_config.yml');
        coreConfigsDefault.push({ objectName: 'chronos-core/configs/OPTIMISATION_CONFIG.yml' });
        const sortedConfigs = [{ objectName: 'chronos-core/configs/default_config.yml' }, ...projectConfigs, ...coreConfigsDefault];

        state.configFiles = [...sortedConfigs];
        state.errors = '';
      }
    });

    builder.addCase(getConfigFiles.rejected, (state, action) => {
      state.isLoading = false;
      state.configFiles = [];
      state.errors = action.error.message;
    })
  },
});

export default chronosFormSlice.reducer;
export const { reset, set, setInputFile } = chronosFormSlice.actions
