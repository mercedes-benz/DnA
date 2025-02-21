import cn from 'classnames';
import React from 'react';
import Styles from './spinner.scss';

const classNames = cn.bind(Styles);

const Spinner = () => {
  return (
    <div className={classNames('text-center', Styles.csSpinner)}>
      <div className="progress infinite" />
    </div>
  );
}

export default Spinner;