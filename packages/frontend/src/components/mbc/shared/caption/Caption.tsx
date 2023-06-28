import cn from 'classnames';
import React from 'react';
import { useHistory } from "react-router-dom";
import Styles from './Caption.scss';

const classNames = cn.bind(Styles);

export interface ICaptionProps {
  title: string;
  disableTitle?: boolean;
}

const Caption:React.FC<ICaptionProps> = ({ title, disableTitle, children }) => {
  const history = useHistory();
  const goback = () => {
    history.goBack();
  };
  return (
    <div className={Styles.caption}>
      <button className={classNames('btn btn-text back arrow', history.length === 1 ? 'hidden' : '')} type="submit" onClick={goback}>Back</button><br/>
      { disableTitle ? '' : <h3>{title}</h3> }
      {children && children}
    </div>
  );
}

export default Caption;
