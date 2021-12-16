import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconSearchProps {
  className?: string;
}

export const IconSearch = (props: IIconSearchProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g id="Data@MBC-App---dark" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Portfolio_Overview" transform="translate(-982.000000, -28.000000)">
          <g id="Search" transform="translate(972.000000, 18.000000)" stroke="#C0C8D0" strokeWidth="2">
            <g id="08_input_field/search_mbc_frame_white">
              <path
                d="M25,18 C25,21.8655546 21.866426,25 18,25 C14.1344454,25 11,21.8655546 11,18 C11,14.1344454 14.1344454,11 18,11 C21.866426,11 25,14.1344454 25,18 Z"
                id="Stroke-1"
              />
              <path d="M23,23 L28,28" id="Stroke-3" />
            </g>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
