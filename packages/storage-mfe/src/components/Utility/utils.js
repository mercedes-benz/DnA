export const getDateFromTimestamp = (givenDate, seperator) => {
  const d = new Date(givenDate);
  const sep = seperator || '-';
  return d.getUTCDate() + sep + (d.getUTCMonth() + 1) + sep + d.getUTCFullYear();
};

export const getDateTimeFromTimestamp = (givenDate, seperator) => {
  const d = new Date(givenDate);
  const time = d.getUTCHours();
  const mins = d.getUTCMinutes();
  return (
    getDateFromTimestamp(givenDate, seperator) +
    ' at ' +
    (time < 10 ? '0' : '') +
    time +
    ':' +
    (mins < 10 ? '0' : '') +
    mins
  );
};
