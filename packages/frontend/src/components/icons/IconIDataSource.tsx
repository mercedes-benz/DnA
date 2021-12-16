import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconDataSourceProps {
  className?: string;
}

export const IconDataSource = (props: IIconDataSourceProps): JSX.Element => {
  return (
    <SvgIcon {...props} viewbox={'0 0 57 57'} size="55">
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="Group" transform="translate(1.000000, 1.000000)" strokeWidth="2">
          <path
            d="M54.9956983,27.0382375 C54.6100911,11.3931981 41.7153208,-0.673365854 26.2426263,0.0291847381 C10.9903377,0.721846055 -0.656564861,13.6298225 0.0287530429,28.7130274 C0.709951212,43.708259 13.3089245,55.6880858 28.7731735,54.9692591 C43.8680882,54.2675326 55.2537996,41.6716864 54.9956983,27.0382375"
            id="Path"
            stroke="#99A5B3"
          />
          <g id="Icon" transform="translate(14.500000, 12.000000)" stroke="#00ADEF">
            <ellipse id="Oval" cx="12.5" cy="4" rx="12" ry="4" />
            <path
              d="M0.5,15 C0.5,17.209139 5.872583,19 12.5,19 C19.127417,19 24.5,17.209139 24.5,15"
              id="Path-Copy-2"
            />
            <path d="M0.5,26 C0.5,28.209139 5.872583,30 12.5,30 C19.127417,30 24.5,28.209139 24.5,26" id="Path-Copy" />
            <path d="M24.5227273,5 L24.5227273,25" id="Line-3-Copy-3" strokeLinecap="square" />
            <path d="M0.5,5 L0.5,25" id="Line-3-Copy-2" strokeLinecap="square" />
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
