import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconConceptDevelopmentProps {
  className?: string;
}

export const IconConceptDevelopment = (props: IIconConceptDevelopmentProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="50">
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="09_pictograms/POC" transform="translate(-4.000000, -7.000000)">
          <g id="Group" strokeWidth="1" fillRule="evenodd" transform="translate(5.640000, 7.000000)" stroke="#C0C8D0">
            <path
              d="M11.7690669,-0.622574217 C11.7856273,-1.66170207 21.3881374,-1.36471722 21.4563337,-0.31901626 C21.5240066,2.64194885 21.8811453,14.0510126 22.5277499,33.9081751 L17.8358955,38.6000296 L12.8404831,33.6046172 C12.2079566,12.3800496 11.8508178,0.970985807 11.7690669,-0.622574217 Z"
              id="Rectangle"
              strokeWidth="2"
              transform="translate(17.148408, 18.665709) rotate(-315.000000) translate(-17.148408, -18.665709) "
            />
          </g>
          <path d="M5.64,46 L41.36,46" id="Line-5" stroke="#00ADEF" strokeWidth="2" strokeLinecap="square" />
        </g>
      </g>
    </SvgIcon>
  );
};
