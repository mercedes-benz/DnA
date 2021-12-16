import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconIdeaProps {
  className?: string;
}

export const IconSort = (props: IIconIdeaProps): JSX.Element => {
  return (
    <SvgIcon {...props} viewbox={'0 0 12 14'}>
      <g style={{ fill: '#C0C8D0', fillRule: 'evenodd' }}>
        <path
          d="M4.426 4.917L.15.167A.1.1 0 0 1 .225 0h8.55a.1.1 0 0 1 .075.167l-4.276 4.75a.1.1 0 0 1-.148 0z"
          transform="translate(0.000000, 7.000000)"
        />
        <path d="M4.574.083l4.276 4.75A.1.1 0 0 1 8.775 5H.225a.1.1 0 0 1-.075-.167L4.426.083a.1.1 0 0 1 .148 0z" />
      </g>
    </SvgIcon>
  );
};
