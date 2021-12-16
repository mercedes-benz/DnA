import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconMonitorProps {
  className?: string;
}

export const IconMonitor = (props: IIconMonitorProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g id="Data@MBC-App---dark" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Portfolio_Overview_navigation_2nd" transform="translate(-21.000000, -240.000000)" stroke="#C0C8D0">
          <g id="Nav---expand---2st-">
            <g id="Nav---Icons">
              <g id="02-Solutions" transform="translate(10.000000, 229.000000)">
                <g id="09_icons/solutions">
                  <g id="Group" transform="translate(12.000000, 12.000000)">
                    <polygon id="Path" strokeWidth="1.5" points="0 13 0 0 16 0 16 13" />
                    <path d="M3.5,16.5 L12.5,16.5" id="Line" strokeWidth="1.5" strokeLinecap="square" />
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
