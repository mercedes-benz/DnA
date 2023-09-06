import { createAsyncThunk } from '@reduxjs/toolkit';
import { chronosApi } from '../apis/chronos.api';

export const getConfigFiles = createAsyncThunk('configFiles/fetchConfigFiles', async (projectId) => {
  try {
    const res = await chronosApi.getConfigurationFiles(projectId);
    const bucketObjects = res.data.bucketObjects ? [...res.data.bucketObjects] : [];
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
    return bucketObjects;
  } catch (e) {
    return e.message;
  }
});

