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

export function validateEmail(email) {
  return String(email)
    .toLowerCase()
    .match(
      /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
    );
}

export const regionalDateFormat = (dateString) => {
  let date = new Date(dateString);
  return date.toLocaleDateString(navigator.language, {
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
  });
};
