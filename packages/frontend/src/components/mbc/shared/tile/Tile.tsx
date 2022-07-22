import React, { useState, useEffect } from 'react';
import Styles from './Tile.scss';
import { history } from '../../../../router/History';
import classNames from 'classnames';
import defaultBg from '../../../../assets/images/quick-links-bg.png';

export interface ITileProps {
  title: string;
  link?: string;
  route?: string;
  isEnabled?: boolean;
  background?: string;
  bgPosition?: string;
}

const Tile = (props: ITileProps) => {

  const [bg, setBg] = useState(defaultBg);
  const [bgPosition, setBgPosition] = useState('right');

  useEffect(() => {
    if(props.background !== undefined) {
      setBg(props.background);
    }
    if(props.bgPosition !== undefined) {
      setBgPosition(props.bgPosition);
    }
  }, []);

  const handleClick = () => {
    if (props.isEnabled) {
      history.push(props.route);
    } else {
      history.push('/comingsoon');
    }
  };

  return (
    props.link ? 
      <a
        className={classNames('wrapper-link', Styles.WorkspacesNavigation)}
        href={props.link}
        target="_blank"
        rel="noreferrer"
      >
        <div className={Styles.WorkspacesNavigationVisual} style={{backgroundImage: `url(${bg})`, backgroundPosition: bgPosition}}></div>
        <div className={Styles.WorkspacesNavigationTitle}>
          <span> {props.title} { !props.isEnabled && <label> ( Coming Soon ) </label>} </span>
          <span>
            {' '}
            <i className="icon mbc-icon arrow small right "></i>
          </span>
        </div>
      </a>
      :
      <div className={Styles.WorkspacesNavigation} onClick={handleClick}>
        <div className={Styles.WorkspacesNavigationVisual} style={{backgroundImage: `url(${bg})`, backgroundPosition: bgPosition}}></div>
        <div className={Styles.WorkspacesNavigationTitle}>
          <span> {props.title} { !props.isEnabled && <label> ( Coming Soon ) </label>} </span>
          <span>
            {' '}
            <i className="icon mbc-icon arrow small right "></i>
          </span>
        </div>
      </div>
  );
};

export default Tile;