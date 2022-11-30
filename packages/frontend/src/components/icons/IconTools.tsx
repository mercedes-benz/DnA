import * as React from 'react';
import IconWrapper from './IconWrapper';

export interface IIconTools {
  className?: string;
  fill?: string;
  size?: string;
}

const IconTools = (props: IIconTools): JSX.Element => {
  return (
    <IconWrapper {...props}>
      <g transform="translate(41, 41)" fill="#00ADEF">
        <path
          d="M0,18 L18,18 L18,0 L0,0 L0,18 Z M3.0415681,14.9560613 L14.9564042,14.9560613 L14.9564042,3.03988283 L3.0415681,3.03988283 L3.0415681,14.9560613 Z"
          id="Fill-1"
        ></path>
        <path
          d="M23,18 L41,18 L41,0 L23,0 L23,18 Z M26.0419108,14.9560613 L37.9601172,14.9560613 L37.9601172,3.03988283 L26.0419108,3.03988283 L26.0419108,14.9560613 Z"
          id="Fill-2"
        ></path>
        <path
          d="M0,41 L18,41 L18,23 L0,23 L0,41 Z M3.0415681,37.9580892 L14.9564042,37.9580892 L14.9564042,26.0419108 L3.0415681,26.0419108 L3.0415681,37.9580892 Z"
          id="Fill-3"
        ></path>
        <path
          d="M23,41 L41,41 L41,23 L23,23 L23,41 Z M26.0419108,37.9580892 L37.9601172,37.9580892 L37.9601172,26.0419108 L26.0419108,26.0419108 L26.0419108,37.9580892 Z"
          id="Fill-4"
        ></path>
      </g>
    </IconWrapper>
  );
};

export default IconTools;
