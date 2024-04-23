import React from 'react';
// import ReactDOM from 'react-dom';
import { createRoot } from 'react-dom/client';

// import css
import './common/modules/uilab/bundle/css/uilab.min.css';
import './common/globals/css/main.scss';

import App from './App';

// ReactDOM.render(<App />, document.getElementById('root'));

const container = document.getElementById('root');
const root = createRoot(container); // createRoot(container!) if you use TypeScript
root.render(<App />);

