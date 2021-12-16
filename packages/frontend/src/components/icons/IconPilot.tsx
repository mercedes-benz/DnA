import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconPilotProps {
  className?: string;
}

export const IconPilot = (props: IIconPilotProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="50">
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="09_pictograms/pilot" transform="translate(-4.000000, -7.000000)">
          <g id="Group" transform="translate(5.640000, 8.000000)">
            <path
              d="M22.56,0 L35.72,0 L35.72,28 L19.74,28 M17.86,28 L0,28 L0,0 L22.56,0"
              id="Shape"
              stroke="#C0C8D0"
              strokeWidth="2"
              strokeLinejoin="round"
              transform="translate(17.860000, 14.000000) scale(-1, 1) translate(-17.860000, -14.000000) "
            />
            <polyline
              id="Path"
              stroke="#00ADEF"
              strokeWidth="2"
              points="7.52 20 15.0134588 12.0344828 19.5144 16.4137931 28.2 7"
            />
            <polyline
              id="Line-3-Copy"
              stroke="#C0C8D0"
              strokeWidth="2"
              strokeLinecap="square"
              points="18.8 28 18.8 38 27.26 38"
            />
            <path d="M7.52,38 L16.92,38" id="Line-3" stroke="#C0C8D0" strokeWidth="2" strokeLinecap="square" />
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
