import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconArrowDownProps {
  className?: string;
}

export const IconArrowDown = (props: IIconArrowDownProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <path
        fill="none"
        fillRule="evenodd"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M1 1l6 6 6-6"
      />
    </SvgIcon>
  );
};
