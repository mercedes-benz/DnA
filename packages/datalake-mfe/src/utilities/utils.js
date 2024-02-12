import {
  tableWidth,
  tableMarginLeft,
  tableMarginTop,
  tableRowNumbers,
  fieldHeight,
  titleHeight,
  commentHeight,
} from '../data/settings';

export const regionalDateAndTimeConversionSolution = (dateString) => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat(navigator.language, {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric',
    hour12: false,
  }).format(date);
};

export function getQueryParameterByName(name, url) {
  if (!url) {
    url = window.location.href;
  }
  name = name.replace(/[[\]]/g, '\\$&');
  const regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
  const results = regex.exec(url);
  if (!results) {
    return null;
  }
  if (!results[2]) {
    return '';
  }
  return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

export const calcXY = (tables, box) => {
  const index = Math.max(1, tables.length);
  let x, y;
  if (!tables.length) {
      x = box.x + 196 + 72;
      y = box.y + 72;
  } else {
      if (index < tableRowNumbers) {
          const lastTable = tables[index - 1];
          x = lastTable.xcoOrdinate + tableWidth + tableMarginLeft;
          y = lastTable.ycoOrdinate;
      } else {
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
  }
  return [x, y];
};