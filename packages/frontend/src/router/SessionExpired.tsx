import * as React from 'react';
import { Pkce } from '../services/Pkce';

export const SessionExpired = () => {
  window.location.href = Pkce.getLogoutUrl();
  return <></>;
};
