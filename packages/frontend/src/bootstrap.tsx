import * as React from 'react';
// import { render } from 'react-dom';
import { createRoot } from 'react-dom/client';

import './assets/modules/uilab/bundle/css/uilab.min.css';
import 'globals/css/main.scss';

import { Routes } from './router/Routes';

// render(<Routes />, document.getElementById('root'));

const container = document.getElementById('root');
const root = createRoot(container); // createRoot(container!) if you use TypeScript
root.render(<Routes />);
