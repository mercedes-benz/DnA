import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconLogOutProps {
  className?: string;
}

export const IconLogOut = (props: IIconLogOutProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g style={{ fill: 'none', fillRule: 'evenodd' }}>
        <path
          fill="#99A5B3"
          d="M14 15v-3l5 4-5 4v-3H8v-2h6zm11-8v19H9v-1h15V7H9V6h16v1z"
          transform="matrix(-1 0 0 1 26 -5)"
        />
      </g>
    </SvgIcon>
  );
};
