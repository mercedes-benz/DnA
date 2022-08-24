```jsx 
import React, { useEffect } from 'react';
import AllBuckets from './Bucket';

import { Provider } from 'react-redux';
import { store } from '../../store/configureStore';
import { createHashHistory } from 'history';
import { ConnectedRouter } from 'connected-react-router';
 const history = createHashHistory({
  hashType: 'noslash',
});

 <div style={{background:'#000'}}>
    <AllBuckets/>
  </div>
