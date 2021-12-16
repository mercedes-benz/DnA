import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconPortfolioProps {
  className?: string;
}

export const IconPortfolio = (props: IIconPortfolioProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g id="Data@MBC-App---dark" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Portfolio_Overview_navigation_2nd" transform="translate(-21.000000, -180.000000)" stroke="#C0C8D0">
          <g id="Nav---expand---2st-">
            <g id="Nav---Icons">
              <g id="01-Portfolio-Overview" transform="translate(10.000000, 169.000000)">
                <g id="Icon_book">
                  <g id="09_icons/portfolio">
                    <g id="Group" transform="translate(12.000000, 12.000000)">
                      <polyline id="Path" strokeWidth="1.5" points="0 14 0 0 16 0 16 14 13.7142857 14" />
                      <polyline
                        id="Path"
                        strokeWidth="1.5"
                        points="2.50111043e-12 0.5 11 3.27272727 11 18 0 14.7272727 0 13"
                      />
                    </g>
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
