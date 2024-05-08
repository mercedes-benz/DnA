import { BooleanNumber, SheetTypes } from '@univerjs/core';

export const DEFAULT_WORKBOOK_DATA = {
  id: 'workbook-01',
  name: 'survey',
  sheetOrder: ['sheet-01'],
  appVersion: '3.0.0-alpha',
  sheets: {
    'sheet-01': {
      type: SheetTypes.GRID,
      id: 'sheet-01',
      cellData: {
          "0": {
              "0": {},
              "1": {
                  "v": "Column 1",
                  "t": 1
              },
              "2": {
                  "v": "Column 2",
                  "t": 1
              },
              "3": {
                  "v": "Column 3",
                  "t": 1
              }
          },
          "1": {
              "0": {
                  "v": "Row 1",
                  "t": 1
              },
              "1": {},
              "2": {}
          },
          "2": {
              "0": {
                  "v": "Row 2",
                  "t": 1
              }
          },
          "3": {
              "0": {
                  "v": "Row 3",
                  "t": 1
              }
          }
      },
      name: 'survey',
      hidden: BooleanNumber.FALSE,
      rowCount: 1000,
      columnCount: 20,
      zoomRatio: 1,
      scrollTop: 200,
      scrollLeft: 100,
      defaultColumnWidth: 93,
      defaultRowHeight: 27,
      status: 1,
      showGridlines: 1,
      hideRow: [],
      hideColumn: [],
      rowHeader: {
        width: 46,
        hidden: BooleanNumber.FALSE,
      },
      columnHeader: {
        height: 20,
        hidden: BooleanNumber.FALSE,
      },
      selections: ['A2'],
      rightToLeft: BooleanNumber.FALSE,
      pluginMeta: {},
    },
  },
};