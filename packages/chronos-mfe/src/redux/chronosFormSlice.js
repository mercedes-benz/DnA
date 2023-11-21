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
        const filteredConfigFiles = bucketObjects.filter(file => file.objectName === 'chronos-core/configs/default_config.yml');
        if(filteredConfigFiles.length === 1) {
          bucketObjects.sort((a, b) => {
            let fa = a.objectName.toLowerCase(),
                fb = b.objectName.toLowerCase();
            const first = 'chronos-core/configs/default_config.yml';
            return fa == first ? -1 : fb == first ? 1 : 0;
          });
        }
        const filteredBucketObjects = bucketObjects.filter(file => file.objectName !== 'chronos-core/configs/OPTIMISATION_CONFIG.yml'); 
        filteredBucketObjects.push({ objectName: 'chronos-core/configs/OPTIMISATION_CONFIG.yml' });
        state.configFiles = [...filteredBucketObjects];
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
