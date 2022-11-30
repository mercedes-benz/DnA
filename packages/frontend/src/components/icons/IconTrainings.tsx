import * as React from 'react';
import IconWrapper from './IconWrapper';

export interface IIconTrainings {
  className?: string;
  fill?: string;
  size?: string;
}

const IconTrainings = (props: IIconTrainings): JSX.Element => {
  return (
    <IconWrapper {...props}>
      <g stroke="#00ADEF" transform="translate(35, 40)" strokeWidth="4">
        <path d="M29,2.16446273 L52.7692151,12 L29,21.8355373 L5.23078494,12 L29,2.16446273 Z"></path>
        <path
          d="M13,22 L13,33.7134782 C17.152346,37.9044927 22.4855321,40 28.9995585,40 C35.5135848,40 40.8470653,37.9044927 45,33.7134782 L45,22"
        ></path>
      </g>
    </IconWrapper>
  );
};

export default IconTrainings;
