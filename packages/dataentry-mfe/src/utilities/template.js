import { BooleanNumber, LocaleType, SheetTypes } from '@univerjs/core';

export const DEFAULT_WORKBOOK_DATA = {
  id: 'workbook-01',
  locale: LocaleType.EN_US,
  name: 'survey',
  sheetOrder: ['sheet-01'],
  appVersion: '3.0.0-alpha',
  sheets: {
    'sheet-01': {
      type: SheetTypes.GRID,
      id: 'sheet-01',
      cellData: {
        0: {
          0: {
            v: 'Hello World',
          },
        },
      },
      name: 'survey',
      tabColor: 'red',
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