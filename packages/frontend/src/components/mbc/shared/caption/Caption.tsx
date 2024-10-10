import cn from 'classnames';
import React from 'react';
import { useHistory } from "react-router-dom";
import Styles from './Caption.scss';

const classNames = cn.bind(Styles);

export interface ICaptionProps {
  title: string;
  disableTitle?: boolean;
  children?: React.ReactNode;
  onBackClick?: () => void;
}

const Caption:React.FC<ICaptionProps> = ({ title, disableTitle, children, onBackClick }) => {
  const history = useHistory();
  const goback = () => {
    onBackClick ? onBackClick() : history.goBack();
  };
  return (
    <div className={Styles.caption}>
      <button className={classNames('btn btn-text back arrow')} type="submit" onClick={goback}>Back</button><br/>
      { disableTitle ? '' : <h3>{title}</h3> }
      {children && children}
    </div>
  );
}

export default Caption;
