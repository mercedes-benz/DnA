import React from 'react';
import Styles from './ProgressWithMessage.scss';

export interface IProgressWithMessageProps {
  message: any;
}

function ProgressWithMessage(props: IProgressWithMessageProps) {
  return (
    <React.Fragment>
      <div className={Styles.alertmessage}>{props.message}</div>
    </React.Fragment>
  );
}

export default ProgressWithMessage;
