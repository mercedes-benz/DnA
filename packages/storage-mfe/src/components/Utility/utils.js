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

export const regionalDateAndTimeConversionSolution = (dateString) => { 
  // const newDateString = dateString.split(/-| /);   
  // const dateUTC = newDateString[2]+'-'+newDateString[1]+'-'+newDateString[0]+'T'+newDateString[3]+'Z';
  
  const date = new Date(dateString);
  return new Intl.DateTimeFormat(navigator.language,{
    year: 'numeric', month: 'numeric', day: 'numeric',
    hour: 'numeric', minute: 'numeric', second: 'numeric',
    hour12: false,
  }).format(date);
};
