import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconArrowRightProps {
  className?: string;
}

export const IconArrowRight = (props: IIconArrowRightProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g style={{ fill: 'none', fillRule: 'evenodd', strokeWidth: '1.5' }}>
        <path d="M1 1l10 10L1 21" />
      </g>
    </SvgIcon>
  );
};
