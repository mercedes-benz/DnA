import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconTrafficLightGreenProps {
  className?: string;
}

export const IconTrafficLightGreen = (props: IIconTrafficLightGreenProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g id="state/good" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="ic_status_on_track" transform="translate(0.000000, 1.000000)">
          <rect id="Rectangle" fill="#FFFFFF" opacity="0" x="0" y="0" width="24" height="24" />
          <rect id="Rectangle-2" stroke="#DDDDDD" x="7.5" y="-0.5" width="9" height="25" rx="4.5" />
          <rect id="Rectangle-2" fill="#DDDDDD" x="9" y="9" width="6" height="6" rx="3" />
          <rect id="Rectangle-2" fill="#DDDDDD" x="9" y="1" width="6" height="6" rx="3" />
          <rect id="Rectangle-2" fill="#13B50B" x="9" y="17" width="6" height="6" rx="3" />
        </g>
      </g>
    </SvgIcon>
  );
};
