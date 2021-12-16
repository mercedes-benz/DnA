import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconTickProps {
  className?: string;
}

export const IconTick = (props: IIconTickProps): JSX.Element => {
  return (
    <SvgIcon {...props} viewbox={'0 0 9 8'}>
      <g id="Symbols" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g
          id="09_pictograms/professionalization"
          transform="translate(-27.000000, -12.000000)"
          stroke="#00ADEF"
          strokeWidth="1"
        >
          <polyline id="Path-Copy-2" points="28 15.6739927 30.3892989 18 35 13" />
        </g>
      </g>
    </SvgIcon>
  );
};
