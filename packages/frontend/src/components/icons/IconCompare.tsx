import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconCompareProps {
  className?: string;
}

export const IconCompare = (props: IIconCompareProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g id="Data@MBC-App---dark" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Portfolio_Overview_navigation_2nd" transform="translate(-22.000000, -391.000000)">
          <g id="Nav---expand---2st-">
            <g id="Nav---Icons">
              <g id="03-Compare-Solutions" transform="translate(10.000000, 380.000000)">
                <g id="Icon_book">
                  <g id="09_icons/solutions_comparison">
                    <polygon
                      id="Path"
                      fill="#C0C8D0"
                      fillRule="nonzero"
                      transform="translate(15.823922, 14.718592) rotate(-45.000000) translate(-15.823922, -14.718592) "
                      points="20.1989223 13.9685922 20.1989223 15.4685922 12.948068 15.4677379 11.4489223 13.9685922"
                    />
                    <polygon
                      id="Path"
                      fill="#C0C8D0"
                      fillRule="nonzero"
                      transform="translate(25.123922, 25.323922) scale(-1, -1) rotate(-45.000000) translate(-25.123922, -25.323922) "
                      points="29.4989223 24.5739223 29.4989223 26.0739223 22.248068 26.073068 20.7489223 24.5739223"
                    />
                    <path d="M13,18 L28,18" id="Line" stroke="#C0C8D0" strokeWidth="1.5" strokeLinecap="square" />
                    <path d="M13,22 L28,22" id="Line-Copy" stroke="#C0C8D0" strokeWidth="1.5" strokeLinecap="square" />
                  </g>
                </g>
              </g>
            </g>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
