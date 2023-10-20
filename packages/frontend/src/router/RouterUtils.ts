import { matchPath } from 'react-router';
import { routes } from './Routes';

export interface IParams {
  id: string;
  app: string;
  editable: string;
  query: string;
}

export const getParams = () => {
  for (const route of routes) {
    const match = matchPath<IParams>(window.location.hash, {
      path: `#${route.path}`,
    });
    if (match && match.isExact) {
      return match.params;
    }
  }
};

export const getQueryParam = (paramName: string) => {
  const hashParts = window.location.hash?.split('?');
  const queryString = hashParts[1] || "";

  const params = new URLSearchParams(queryString);
  return params.get(paramName);
}


export const getPath = () => {
  return window.location.hash;
};
