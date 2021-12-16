import React from 'react';
import Styles from './DataNotExist.scss';

export interface IDataNotExistedProps {
  message: any;
  height: string;
}

function DataNotExist(props: IDataNotExistedProps) {
  return (
    <React.Fragment>
      <div className={Styles.emptyList} style={{ height: props.height }}>
        {props.message}
      </div>
    </React.Fragment>
  );
}

export default DataNotExist;
