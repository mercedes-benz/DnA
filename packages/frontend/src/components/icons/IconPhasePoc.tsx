/**
 * @author Philipp Laube <philipp.laube@etecture.de>
 */

import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconPhasePocProps {
  className?: string;
}

export const IconPhasePoc = (props: IIconPhasePocProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g style={{ fill: 'none', fillRule: 'evenodd', stroke: 'currentColor' }}>
        <g transform="translate(5.000000, 12.000000)">
          <path d="M9.4979073,7.50703351 L9.4979073,-0.5 L14.5213639,-0.5 L14.5213639,7.50703351 L9.4979073,7.50703351 Z M4.51346702,7.50703351 L4.51346702,4.49644669 L9.52414829,4.49644669 L9.52414829,7.50703351 L4.51346702,7.50703351 Z M-0.488927414,7.50703351 L-0.488927414,2.50831248 L4.50044912,2.50831248 L4.50044912,7.50703351 L-0.488927414,7.50703351 Z" />
        </g>
        <g transform="translate(4.000000, 4.000000)" strokeLinecap="round">
          <polyline
            strokeLinejoin="round"
            points="0 7.45828008 2.98575451 4.75688054 5.34823515 8 7.7032232 4 9.0694965 5.31495904 14 0"
          />
          <polyline points="11 0 14.4509264 0 15 3" />
        </g>
      </g>
    </SvgIcon>
  );
};
