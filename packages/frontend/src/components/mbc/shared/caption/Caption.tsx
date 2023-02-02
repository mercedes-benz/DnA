import cn from 'classnames';
import React from 'react';
import { useHistory } from "react-router-dom";
import Styles from './Caption.scss';

const classNames = cn.bind(Styles);

export interface ICaptionProps {
  title: string;
  disableTitle?: boolean;
}

const Caption:React.FC<ICaptionProps> = ({ title, disableTitle }) => {
  const history = useHistory();
  const goback = () => {
    history.goBack();
  };
  return (
    <div className={Styles.caption}>
      <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>Back</button>
      { disableTitle ? '' : <h3>{title}</h3> }
    </div>
  );
}

export default Caption;
