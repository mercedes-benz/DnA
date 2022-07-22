import cn from 'classnames';
import React from 'react';
import Styles from './Spinner.scss';

const classNames = cn.bind(Styles);

const Spinner:React.FC = () => {
  return (
    <div className={classNames('text-center', Styles.spinner)}>
      <div className="progress infinite" />
    </div>
  );
}

export default Spinner;
