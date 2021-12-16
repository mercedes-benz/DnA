import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconProfessionalizationProps {
  className?: string;
}

export const IconProfessionalization = (props: IIconProfessionalizationProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="50">
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="09_pictograms/professionalization" transform="translate(-9.000000, -7.000000)" strokeWidth="2">
          <rect id="Rectangle" stroke="#C0C8D0" x="10.34" y="8" width="27.26" height="36" rx="1" />
          <polyline id="Path" stroke="#00ADEF" points="26.32 35.6739927 28.565941 38 32.9 33" />
          <polyline id="Path-Copy" stroke="#00ADEF" points="26.32 25.6739927 28.565941 28 32.9 23" />
          <polyline id="Path-Copy-2" stroke="#00ADEF" points="26.32 15.6739927 28.565941 18 32.9 13" />
        </g>
      </g>
    </SvgIcon>
  );
};
