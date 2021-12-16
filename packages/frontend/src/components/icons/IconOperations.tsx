import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconOperationsProps {
  className?: string;
}

export const IconOperations = (props: IIconOperationsProps): JSX.Element => {
  return (
    <SvgIcon {...props} size="50">
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g id="09_pictograms/rollout" transform="translate(-9.000000, -5.000000)" strokeWidth="2">
          <path
            d="M23,5.99999981 C24.7131034,5.99999981 31.28,15.4999998 31.28,16.222222 C31.28,16.6884794 31.28,21.9477379 31.28,31.9999974 L31.2799999,31.9999974 C31.2799999,32.5522821 30.8322847,32.9999973 30.28,32.9999973 L15.72,32.9999973 C15.1677153,32.9999973 14.72,32.552282 14.72,31.9999973 C14.72,26.7407389 14.72,21.4814804 14.72,16.222222 C14.72,15.5515871 21.2868966,5.99999981 23,5.99999981 Z"
            id="Rectangle-Copy-2"
            stroke="#C0C8D0"
          />
          <polyline id="Path" stroke="#C0C8D0" points="28.52 36 28.52 39 17.48 39 17.48 36" />
          <polygon id="Rectangle-Copy-2" stroke="#C0C8D0" points="10.12 25.5 14.72 21 14.72 31 10.12 31" />
          <polygon
            id="Rectangle-Copy-3"
            stroke="#C0C8D0"
            transform="translate(33.580000, 26.000000) scale(-1, 1) translate(-33.580000, -26.000000) "
            points="31.28 25.5 35.88 21 35.88 31 31.28 31"
          />
          <path d="M23,43 L23,45" id="Line-4-Copy-2" stroke="#00ADEF" strokeLinecap="square" />
          <path d="M18.4,43 L18.4,45" id="Line-4-Copy-3" stroke="#00ADEF" strokeLinecap="square" />
          <path d="M27.6,43 L27.6,45" id="Line-4-Copy-4" stroke="#00ADEF" strokeLinecap="square" />
        </g>
      </g>
    </SvgIcon>
  );
};
