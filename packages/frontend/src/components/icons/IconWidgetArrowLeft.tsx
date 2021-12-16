import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconWidgetArrowLeftProps {
  className?: string;
}

export const IconWidgetArrowLeft = (props: IIconWidgetArrowLeftProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g
        style={{
          stroke: '#252A33',
          fillRule: 'evenodd',
          strokeWidth: '1.5',
          fill: 'none',
          strokeLinecap: 'round',
          strokeLinejoin: 'round',
        }}
      >
        <path d="M11 21L1 11 11 1" />
      </g>
    </SvgIcon>
  );
};
