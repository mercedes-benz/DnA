import * as React from 'react';
import { render } from 'react-dom';
import { hot } from 'react-hot-loader';
import './assets/modules/uilab/bundle/css/uilab.min.css';
import './globals/css/main.scss';
import { Envs } from './globals/Envs';
import { Routes } from './router/Routes';

const app = render(<Routes />, document.getElementById('root'));
export default Object.is(Envs.NODE_ENV, 'production') ? app : hot(module)(app);
