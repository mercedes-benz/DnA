import classnames from 'classnames';
import React from 'react';
import styles from './circular-progress-bar.scss';

const classNames = classnames.bind(styles);

const CircularProgressBar = () => {
  return (
    <div className={classNames('text-center', styles.spinner)}>
      <div className="progress infinite" />
    </div>
  )
}

export default CircularProgressBar; 