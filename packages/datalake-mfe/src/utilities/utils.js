import { Envs } from './envs';

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

export const getDateFromTimestampForDifference = (givenDate) => {
  const d = new Date(givenDate).toUTCString();
  return d;
};

export const getDateDifferenceFromToday = (dateFrom) => {
  const date1 = new Date(dateFrom);
  const date2 = new Date();
  const diffTime = Math.abs(date2.getTime() - date1.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 3600 * 24));
  return diffDays;
};

export const getDifferenceFromToday = (timestamp) => {
  const currentTimestamp = Date.now();
  const difference = currentTimestamp - timestamp;
  const daysAgo = Math.floor(difference / (24 * 60 * 60 * 1000));
  return daysAgo;
};

export const getDateFromTimestamp = (givenDate) => {
  const d = new Date(givenDate);
  return regionalDateAndTimeConversionSolution(d);
};

export const convertTextToLink = (text, env) => {
  if (text.lastIndexOf('[') === -1 && text.lastIndexOf(']') === -1) {
    return text;
  }
  const tempArray = [{ data: '', link: '' }];
  const a = '](';
  let linkData = '';
  let getLinkFromString = '';
  text.split(a).forEach((item) => {
    if (item.includes('[')) {
      if (item.includes('[') && item.includes('/)')) {
        tempArray.push({ data: item.split('[')[1], link: item.split('/)')[0] });
      } else {
        linkData = item.split('[')[1];
      }
    } else if (item.includes('/)')) {
      getLinkFromString = item.split('/)')[0];
      tempArray.push({ data: linkData, link: getLinkFromString });
    }
  });
  tempArray.forEach((item) => {
    const searchString = '[' + item.data + '](' + item.link + '/)';
    const formattedURL =
      (env === 'production' ? Envs.DATAIKU_LIVE_APP_URL : Envs.DATAIKU_TRAINING_APP_URL) + '/' + item.link;
    const anchor = `<a href=${formattedURL} target="_blank">${item.data}</a>`;
    text = text.replace(searchString, anchor);
  });
  return text;
};
