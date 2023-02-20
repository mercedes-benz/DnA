import React from 'react';
import Styles from './style.scss';

function DataNotExist(props) {
  return (
    <React.Fragment>
      <div className={Styles.emptyList} style={{ height: props.height }}>
        {props.message}
      </div>
    </React.Fragment>
  );
}

export default DataNotExist;
