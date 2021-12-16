/**
 * @author Philipp Laube <philipp.laube@etecture.de>
 */

import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconPhaseSeriesProps {
  className?: string;
}

export const IconPhaseSeries = (props: IIconPhaseSeriesProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <g style={{ fill: 'none', fillRule: 'evenodd', stroke: 'currentColor' }}>
        <path
          d="M8.39067874,12.0637495 L12.5028811,9.00274142 L12.4954753,11.9995919 L16.5257169,9.00580834 L16.5257169,11.9920072 L20.4955059,8.99639398 L20.4955059,20.5241425 L3.49051844,20.5241425 L3.49051844,20.0241425 L3.49355514,12.9406695 L4.64982519,2.49723369 L7.49155781,2.49723369 L8.39067874,12.0637495 Z"
          strokeLinejoin="round"
        />
        <rect strokeWidth="0.5" x="11.25" y="14.25" width="2.5" height="2.5" rx="1" />
        <rect strokeWidth="0.5" x="15.25" y="14.25" width="2.5" height="2.5" rx="1" />
      </g>
    </SvgIcon>
  );
};
