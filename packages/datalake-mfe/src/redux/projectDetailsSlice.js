import { createSlice } from '@reduxjs/toolkit';
import { getProjectDetails } from './projectDetails.services';

const initialState = {
  isLoading: false,
  data: {
    id: '',
    projectName: '',
    connectorType: 'iceberg',
    description: '',
    divisionId: '',
    divisionName: '',
    subdivisionId: '',
    subdivisionName: '',
    department: '',
    status: '',
    classificationType: '',
    hasPii: false,
    createdOn: '',
    createdBy: {
      id: '',
      firstName: '',
      lastName: '',
      mobileNumber: '',
      email: '',
      department: '',
    },
    dataProductDetails: {
      id: '',
      dataProductId: '',
      dataProductName: '',
      invalidstate: false,
    },
    collabs: [
      {
        collaborator: {
          id: '',
          firstName: '',
          lastName: '',
          department: '',
          email: '',
          mobileNumber: '',
        },
        hasWritePermission: true
      }
    ],
    tables: [
      {
        tableName: '',
        dataFormat: '',
        description: '',
        externalLocation: '',
        xcoOrdinate: '',
        ycoOrdinate: '',
        columns: [
          {
            columnName: '',
            dataType: '',
            comment: '',
            notNullConstraintEnabled: true,
          }
        ],
        // collabs: [
        //   {
        //     collaborator: {
        //       id: '',
        //       firstName: '',
        //       lastName: '',
        //       department: '',
        //       email: '',
        //       mobileNumber: '',
        //     },
        //     hasWritePermission: true
        //   }
        // ]
      }
    ]
  },
  errors: '',
};

export const projectDetailsSlice = createSlice({
  name: 'projectDetails',
  initialState,
  extraReducers: builder => {
    builder.addCase(getProjectDetails.pending, state => {
      state.isLoading = true;
    });

    builder.addCase(getProjectDetails.fulfilled, (state, action) => {
      state.isLoading = false;
      if(typeof action.payload === 'string') {
        state.data = {};
        state.errors = action.payload;
      } else {
        state.data = action.payload;
        state.errors = '';
      }
    });

    builder.addCase(getProjectDetails.rejected, (state, action) => {
      state.isLoading = false;
      state.data = {};
      state.errors = action.error.message;
    })
  },
});

export default projectDetailsSlice.reducer;
