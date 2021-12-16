import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconKickOffProps {
  className?: string;
}

export const IconKickOff = (props: IIconKickOffProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="50" viewbox={'-7 -5 50 50'}>
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="09_pictograms/kick_off" transform="translate(-8.000000, -7.000000)">
          <g id="Group" transform="translate(9.200000, 8.000000)">
            <polyline id="Path" stroke="#C0C8D0" strokeWidth="2" points="2.76 0 28.52 0 28.52 11 28.52 20 2.76 20" />
            <polyline id="Line" stroke="#C0C8D0" strokeWidth="2" strokeLinecap="square" points="2.76 0 0 0 0 38" />
            <polyline
              id="Path"
              stroke="#00ADEF"
              strokeWidth="2"
              points="15.64 5.7 20.0451765 10.0793103 15.6659765 14.6655172"
            />
            <polyline
              id="Path-Copy"
              stroke="#00ADEF"
              strokeWidth="2"
              points="9.2 5.7 13.6051765 10.0793103 9.22597647 14.6655172"
            />
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
