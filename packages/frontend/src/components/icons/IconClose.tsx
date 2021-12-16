import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconCloseProps {
  className?: string;
}

export const IconClose = (props: IIconCloseProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g id="Data@MBC-App---dark" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Navigation_1nd_expand" transform="translate(-22.000000, -29.000000)" fill="#C0C8D0">
          <g id="Nav---expand---1st-">
            <g id="Nav---x" transform="translate(10.000000, 17.000000)">
              <g id="navigation/menu/desktop">
                <g id="03_navigation/menu/desktop/x">
                  <rect
                    id="Rectangle-2-Copy"
                    transform="translate(20.000000, 20.000000) rotate(-315.000000) translate(-20.000000, -20.000000) "
                    x="10"
                    y="19"
                    width="20"
                    height="2"
                  />
                  <rect
                    id="Rectangle-2-Copy"
                    transform="translate(20.000000, 20.000000) rotate(-45.000000) translate(-20.000000, -20.000000) "
                    x="10"
                    y="19"
                    width="20"
                    height="2"
                  />
                </g>
              </g>
            </g>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
