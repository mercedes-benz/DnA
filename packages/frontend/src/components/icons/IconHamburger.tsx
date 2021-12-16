import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconHamburgerProps {
  className?: string;
}

export const IconHamburger = (props: IIconHamburgerProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g id="Data@MBC-App---dark" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Portfolio_overview_widget" transform="translate(-20.000000, -28.000000)" fill="#C0C8D0">
          <g id="Nav---default">
            <g id="Nav_burger" transform="translate(10.000000, 17.000000)">
              <g id="03_navigation/menu/desktop/burger">
                <rect id="Rectangle-2" x="10" y="11" width="20" height="2" />
                <rect id="Rectangle-2-Copy" x="10" y="19" width="20" height="2" />
                <rect id="Rectangle-2-Copy-3" x="10" y="27" width="20" height="2" />
              </g>
            </g>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
