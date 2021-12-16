/**
 * @author Philipp Laube <philipp.laube@etecture.de>
 */

import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconPhaseIdeaProps {
  className?: string;
}

export const IconPhaseIdea = (props: IIconPhaseIdeaProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g style={{ fill: 'none', fillRule: 'evenodd', stroke: 'currentColor' }}>
        <path d="M9.50183951,15.9012292 C7.68304891,14.9736289 6.5,13.0956606 6.5,11 C6.5,7.96243388 8.96243388,5.5 12,5.5 C15.0375661,5.5 17.5,7.96243388 17.5,11 C17.5,13.0956548 16.3169576,14.9736217 14.498168,15.9012253 C14.4993882,15.9334339 14.5,15.9657301 14.5,15.9981014 L14.5,20.0018986 C14.5,21.3840345 13.385945,22.5 12,22.5 C10.6169692,22.5 9.5,21.3871288 9.5,20.0018986 L9.5,15.9981014 C9.5,15.9657367 9.50061432,15.9334416 9.50183951,15.9012292 Z" />
        <g transform="translate(1.000000, 1.000000)">
          <path d="M8.74156553,18.7483224 L13.2,18.8235294" strokeWidth="0.5" />
          <path
            d="M8.3375,7.95482473 C8.3375,7.95482473 9.02222395,6.77835414 11,6.77835414 C12.977776,6.77835414 13.6625,7.95482473 13.6625,7.95482473"
            strokeWidth="0.5"
            strokeLinecap="round"
          />
          <path d="M11,1.17647059 L11,0" strokeLinecap="round" />
          <path d="M18.3110913,3.35712164 L19.0889087,2.52523131" strokeLinecap="round" />
          <path
            d="M2.91109127,3.35712164 L3.68890873,2.52523131"
            strokeLinecap="round"
            transform="translate(3.461091, 3.113467) scale(-1, 1) translate(-3.461091, -3.113467) "
          />
          <path d="M1.64991623,10.0102661 L0.550083768,9.98973388" strokeLinecap="round" />
          <path d="M21.4499162,10.0102661 L20.3500838,9.98973388" strokeLinecap="round" />
        </g>
      </g>
    </SvgIcon>
  );
};
