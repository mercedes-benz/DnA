// history.js
import { createHashHistory as createHistory } from 'history';

// here we could add logic to decide which history to use, hash, memory or browser

export const history = createHistory({
  /* pass a configuration object here if needed */
});
