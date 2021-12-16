import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconImageProps {
  className?: string;
}

export const IconImage = (props: IIconImageProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="57">
      <g stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g transform="translate(1.000000, 1.000000)">
          <g>
            <path
              d="M54.9956983,27.0382375 C54.6100911,11.3931981 41.7153208,-0.673365854 26.2426263,0.0291847381 C10.9903377,0.721846055 -0.656564861,13.6298225 0.0287530429,28.7130274 C0.709951212,43.708259 13.3089245,55.6880858 28.7731735,54.9692591 C43.8680882,54.2675326 55.2537996,41.6716864 54.9956983,27.0382375"
              id="Path"
              stroke="#99A5B3"
              strokeWidth="2"
            />
            <g transform="translate(12.500000, 16.500000)" stroke="#00ADEF" strokeWidth="1.5">
              <g>
                <rect id="Rectangle" x="0.75" y="0.75" width="28.5" height="19.5" />
                <polyline id="Path-8" points="5 20 13.751683 12.9405068 17.6205899 16.4702534 24.3556451 10 29 15" />
              </g>
              <circle id="Oval" cx="8.5" cy="7.5" r="2.75" />
            </g>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};
