import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconStarProps {
  className?: string;
}

export const IconStar = (props: IIconStarProps): JSX.Element => {
  return (
    <SvgIcon {...props} viewbox={'0 0 17 17'}>
      <g style={{ fill: '#C0C8D0', fillRule: 'evenodd' }}>
        <path d="M8.5 0L6.143 5.633 0 6.111l4.685 3.96L3.248 16h.002l5.25-3.185L13.75 16h.002l-1.437-5.928L17 6.112l-6.142-.479z" />
      </g>
    </SvgIcon>
  );
};
