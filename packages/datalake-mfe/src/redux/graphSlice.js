import { createSlice } from '@reduxjs/toolkit';
import { getProjectDetails } from './graph.services';
import { tableRowNumbers, tableMarginLeft, tableWidth, fieldHeight, titleHeight, commentHeight, tableMarginTop } from '../data/settings';

const graphInitialState = {
  isLoading: false,
  box: { x: 0, y: 0, w: 0, h: 0, clientW: 0, clientH: 0 },
  version: 'currentVersion',
  project: {
    tables: []
  },
  errors: '',
  editingTable: {},
};

export const graphSlice = createSlice({
  name: 'graph',
  initialState: graphInitialState,
  reducers: {
    setTables: (state, action) => {
      state.project.tables = [...action.payload];
    },
    setBox: (state, action) => {
      state.box = {...action.payload};
    },
    setVersion: (state, action) => {
      state.version = {...action.payload};
    },
    // setEditingTable: (state, action) => {
    //   state.editingTable = {...action.payload};
    // },
    // setEditingField: (state, action) => {
    //   state.editingField = {...action.payload};
    // },
    // setAddingField: (state, action) => {
    //   state.addingField = {...action.payload};
    // },
  },
  extraReducers: {
    [getProjectDetails.pending]: (state) => {
      state.isLoading = true;
    },
    [getProjectDetails.fulfilled]: (state, action) => {
      state.isLoading = false;
      // state.project = action.payload.data;
      const tables = [...action.payload.data.tables];
      if(tables?.filter(item => item.xcoOrdinate === 0).length > 0) {
        tables.forEach((table, index) => {
          if(index === 0) {
            tables[index].xcoOrdinate = 36;
            tables[index].ycoOrdinate = 36;
          } else {
            let x = 0, y = 0;
            if (index < tableRowNumbers) {
              const lastTable = tables[index - 1];
              x = lastTable.xcoOrdinate + tableWidth + tableMarginLeft;
              y = lastTable.ycoOrdinate;
            } 
            else {
                const lastTable = tables[index - tableRowNumbers];
                const { columns } = lastTable;
                x = lastTable.xcoOrdinate;
                y =
                    lastTable.ycoOrdinate +
                    columns.length * fieldHeight +
                    titleHeight +
                    commentHeight +
                    tableMarginTop;
            }
            tables[index].xcoOrdinate = x;
            tables[index].ycoOrdinate = y;
          }
        });
        state.project = {...action.payload.data, tables: [...tables]};
        state.errors = '';
      } else {
        state.project = {...action.payload.data};
        state.errors = '';
      }
    },
    [getProjectDetails.rejected]: (state, action) => {
      state.isLoading = false;
      state.project = {};
      state.errors = action.payload;
    },
  },
});

export const { 
  setTables,
  setBox,
  setVersion
} = graphSlice.actions;
export default graphSlice.reducer;
