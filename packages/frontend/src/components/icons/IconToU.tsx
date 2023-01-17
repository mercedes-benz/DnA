import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconToU {
  className?: string;
  fill?: string;
  size?: string;
}

const IconToU = (props: IIconToU): JSX.Element => {
  return (
    <SvgIcon {...props} size={props.size || '125'}>
      <defs>
        <filter
          x="-68.6%"
          y="-53.0%"
          width="237.3%"
          height="206.1%"
          filterUnits="objectBoundingBox"
          id="filter-12hve-jvz3-1"
        >
          <feOffset dx="0" dy="2" in="SourceAlpha" result="shadowOffsetOuter1"></feOffset>
          <feGaussianBlur stdDeviation="7.5" in="shadowOffsetOuter1" result="shadowBlurOuter1"></feGaussianBlur>
          <feColorMatrix
            values="0 0 0 0 0   0 0 0 0 0   0 0 0 0 0  0 0 0 0.15 0"
            type="matrix"
            in="shadowBlurOuter1"
            result="shadowMatrixOuter1"
          ></feColorMatrix>
          <feMerge>
            <feMergeNode in="shadowMatrixOuter1"></feMergeNode>
            <feMergeNode in="SourceGraphic"></feMergeNode>
          </feMerge>
        </filter>
      </defs>
      <g id="Page-1" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
        <g>
          <g filter="url(#filter-12hve-jvz3-1)">
            <polyline
              stroke="#697582"
              strokeWidth="3"
              strokeLinejoin="round"
              points="0 51.4186047 0 0 34.9285714 0 51 16.3921569 51 21.1372549 51 66 0 66 0 56.5988372"
            ></polyline>
            <polygon
              stroke="#697582"
              strokeWidth="3"
              strokeLinejoin="round"
              points="51 17 35 17 35 0"
            ></polygon>
            <line
              x1="29.2037037"
              y1="36.5"
              x2="39.7962963"
              y2="36.5"
              stroke="#00ADEF"
              strokeWidth="4"
              strokeLinecap="square"
            ></line>
            <line
              x1="9.2037037"
              y1="36.5"
              x2="19.7962963"
              y2="36.5"
              stroke="#00ADEF"
              strokeWidth="4"
              strokeLinecap="square"
            ></line>
            <line
              x1="9.2037037"
              y1="44.5"
              x2="19.7962963"
              y2="44.5"
              stroke="#00ADEF"
              strokeWidth="4"
              strokeLinecap="square"
            ></line>
            <line
              x1="29.2037037"
              y1="28.5"
              x2="39.7962963"
              y2="28.5"
              stroke="#00ADEF"
              strokeWidth="4"
              strokeLinecap="square"
            ></line>
            <line
              x1="9.2037037"
              y1="28.5"
              x2="19.7962963"
              y2="28.5"
              stroke="#00ADEF"
              strokeWidth="4"
              strokeLinecap="square"
            ></line>
            <line
              x1="29.2037037"
              y1="44.5"
              x2="39.7962963"
              y2="44.5"
              stroke="#00ADEF"
              strokeWidth="4"
              strokeLinecap="square"
            ></line>
          </g>
          <g transform="translate(38.000000, 51.000000)">
            <circle id="Oval-6" fill="#00ADEF" cx="13" cy="13" r="13"></circle>
            <path
              d="M9.19669149,17.7434088 L9.19669149,15.4492912 L13.7846915,15.4488206 L13.7849268,6.27282057 L16.0790444,6.27282057 L16.0790444,17.7434088 L9.19669149,17.7434088 Z"
              fill="#252A33"
              transform="translate(12.637868, 12.008115) rotate(-315.000000) translate(-12.637868, -12.008115) "
            ></path>
          </g>
        </g>
      </g>
    </SvgIcon>
  );
};

export default IconToU;
