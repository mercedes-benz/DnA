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

export const getPath = () => {
  return window.location.hash;
};
