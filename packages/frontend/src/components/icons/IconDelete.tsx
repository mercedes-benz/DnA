import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconDeleteProps {
  className?: string;
}

export const IconDelete = (props: IIconDeleteProps): JSX.Element => {
  return (
    <SvgIcon {...props}>
      <defs>
        <path
          d="M17.414 17l2.829 2.828-1.415 1.415L16 18.414l-2.828 2.829-1.415-1.415L14.586 17l-2.829-2.828 1.415-1.415L16 15.586l2.828-2.829 1.415 1.415L17.414 17zM18 5h1v2h6v1h-1v17H8V8H7V7h6V5h5zm0 1h-4v1h4V6zM9 8v16h14V8H9z"
          id="a"
        />
      </defs>
      <g transform="translate(-7 -5)" fill="none" fillRule="evenodd">
        <mask id="b" fill="#fff">
          <use xlinkHref="#a" />
        </mask>
        <use fill="#1C2026" xlinkHref="#a" />
        <g mask="url(#b)" fill="#697582">
          <path d="M0 0h32v32H0z" />
        </g>
      </g>
    </SvgIcon>
  );
};
