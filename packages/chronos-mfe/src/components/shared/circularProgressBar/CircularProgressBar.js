import classnames from 'classnames';
import React from 'react';
import Styles from './CircularProgressBar.scss';

const classNames = classnames.bind(Styles);

const CircularProgressBar = () => {
  return (
    <div className={classNames('text-center', Styles.spinner)}>
      <div className="progress infinite" />
    </div>
  )
}

export default CircularProgressBar; 