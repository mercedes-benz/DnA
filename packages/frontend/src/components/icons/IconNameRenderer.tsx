import React from 'react';

interface IIconRenderer {
  name: string;
}

const IconRenderer = ({ name }: IIconRenderer) => {
  return <i className={`icon mbc-icon ${name}`} />;
};

export default IconRenderer;
