import * as React from 'react';
import IconWrapper from './IconWrapper';

export interface IIconTransparency {
  className?: string;
  fill?: string;
  size?: string;
}

const IconTransparency = (props: IIconTransparency): JSX.Element => {
  return (
    <IconWrapper {...props}>
      <g id="Group-21" transform="translate(43, 41)" fill="#00ADEF">
        <polygon
          id="Fill-1"
          points="20.2724178 37.3878205 23.2948271 37.3878205 23.2948271 25.0459113 20.2724178 25.0459113"
        ></polygon>
        <polygon
          id="Fill-2"
          points="14.8194147 37.3878205 17.8418241 37.3878205 17.8418241 20.7731289 14.8194147 20.7731289"
        ></polygon>
        <polygon
          id="Fill-3"
          points="9.36422788 37.3878205 12.3866373 37.3878205 12.3866373 28.8660776 9.36422788 28.8660776"
        ></polygon>
        <path
          d="M-3.58824082e-13,45 L32.659055,45 L32.659055,6.47782398 L-3.58824082e-13,6.47782398 L-3.58824082e-13,45 Z M3.02240937,42.002772 L29.6366456,42.002772 L29.6366456,9.47288635 L3.02240937,9.47288635 L3.02240937,42.002772 Z"
          id="Fill-4"
        ></path>
        <polygon
          id="Fill-5"
          points="9.36335435 1.4033219e-12 9.36335435 2.99506237 35.9797745 2.99506237 35.9797745 35.4361573 39 35.4361573 39 1.4033219e-12"
        ></polygon>
      </g>
    </IconWrapper>
  );
};

export default IconTransparency;
