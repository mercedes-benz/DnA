export const getDateFromTimestamp = (givenDate, seperator) => {
  const d = new Date(givenDate);
  const td = new Date((d.getTime() + (-d.getTimezoneOffset() * 60000)));
  const sep = seperator || '-';
  return td.getUTCDate() + sep + (td.getUTCMonth() + 1) + sep + td.getUTCFullYear();
};

export const getDateTimeFromTimestamp = (givenDate, seperator) => {
  const d = new Date(givenDate);
  const td = new Date((d.getTime() + (-d.getTimezoneOffset() * 60000)));
  const time = td.getUTCHours();
  const mins = td.getUTCMinutes();
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
