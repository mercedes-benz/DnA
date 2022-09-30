import * as React from 'react';
import { render } from 'react-dom';

import './assets/modules/uilab/bundle/css/uilab.min.css';
import 'globals/css/main.scss';

import { Routes } from './router/Routes';

render(<Routes />, document.getElementById('root'));
