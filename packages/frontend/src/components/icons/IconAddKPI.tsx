import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconAddKPIProps {
  className?: string;
  fill?: string;
}

const IconAddKPI = (props: IIconAddKPIProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="55">
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <circle cx="27.5" cy="27.5" r="26.5" stroke="#99A5B3" strokeWidth="2" />
        <g transform="translate(18, 14)" fill="#00ADEF">
          <g>
            <rect id="Rectangle" x="0" y="15" width="2" height="11"></rect>
            <rect id="Rectangle" x="5" y="8" width="2" height="18"></rect>
            <rect id="Rectangle" x="10" y="12" width="2" height="14"></rect>
            <rect id="Rectangle" x="15" y="0" width="2" height="26"></rect>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};

export default IconAddKPI;
