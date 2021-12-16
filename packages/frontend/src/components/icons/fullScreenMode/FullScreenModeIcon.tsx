import React from 'react';
import Styles from './FullScreenModeIcon.scss';


export interface IFullScreenModeIcon {
  fsNeed: boolean;
}
const FullScreenModeIcon = (props: IFullScreenModeIcon) => {
  return (
    <div
      className={props.fsNeed ? Styles.fullScreenActivated : ' '}
    >
      <div
        tooltip-data={props.fsNeed ? 'Revert to Normal Mode' : 'Full ScreenMode'}
        className={Styles.fullScr}
      >
        <i className="icon mbc-icon arrow small right" />
        <i className="icon mbc-icon arrow small right" />
      </div>
    </div>
  );
};

export default FullScreenModeIcon;
