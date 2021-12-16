import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconWidgetArrowRightProps {
  className?: string;
}

export const IconWidgetArrowRight = (props: IIconWidgetArrowRightProps): JSX.Element => {
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
        <path d="M1 21l10-10L1 1" transform="translate(10, 0)" />
      </g>
    </SvgIcon>
  );
};
