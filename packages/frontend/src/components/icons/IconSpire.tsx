import * as React from 'react';
import SvgIcon from '../svgIcon/SvgIcon';

export interface IIconSpire {
  className?: string;
  fill?: string;
  size?: string;
}

const IconSpire = (props: IIconSpire): JSX.Element => {
    const size = props.size || '125'
  return (
    <SvgIcon {...props} size={size}>
      <svg width={size} viewBox="0 0 129 100"><polygon fill="#FFF" fillRule="evenodd" points=".468 58.459 57.424 .435 70.865 .435 128.468 58.459 128.468 99.435 117.045 99.435 64.468 46.848 12.465 99.435 .468 99.435"/></svg>
    </SvgIcon>
  );
};

export default IconSpire;
