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
            "0": {
                "v": "Questions",
                "t": 1,
                "s": "0tfFLd"
            },
            "1": {
                "v": "Answers",
                "t": 1,
                "s": "0tfFLd"
            },
            "2": {},
            "3": {}
        }
      },
      name: 'Survey',
      hidden: BooleanNumber.FALSE,
      rowCount: 100,
      columnCount: 2,
      columnData: {
        "0": {
            "w": 416,
            "hd": 0
        },
        "1": {
            "w": 897,
            "hd": 0
        }
      },
      zoomRatio: 1,
      scrollTop: 200,
      scrollLeft: 100,
      defaultColumnWidth: 200,
      defaultRowHeight: 25,
      defaultStyle: {
        tb: 3
      },
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
      styles: {
        "OZkXvs": {
            "bl": 1,
            "vt": null
        },
        "yWaw8b": {
            "bl": 1,
            "vt": 2
        },
        "Nfcz58": {
            "vt": 2
        }
      }
    },
  },
  styles: {
    "3PshHC": {
        "bl": 1,
        "vt": null
    },
    "0tfFLd": {
        "bl": 1,
        "vt": 2
    }
  },
};