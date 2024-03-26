import React from 'react';
import { PredefinedSolutionLogoImagesInfo } from 'globals/constants';
import { ILogoDetails } from 'globals/types';

export interface ILogoImageProps {
  displayType: string;
  logoDetails: ILogoDetails;
  width?: string;
  height?: string;
  className?: string;
}

const LogoImage = (props: ILogoImageProps) => {
  const logoId = props.logoDetails
    ? PredefinedSolutionLogoImagesInfo.images.some((image) => image.id === props.logoDetails.id)
      ? props.logoDetails.id
      : 'default'
    : 'default';
  const predefinedImageUrl = `./${PredefinedSolutionLogoImagesInfo.folder}/${props.displayType}/${logoId}.jpg`;
  return <img width={props.width} height={props.height} className={props.className} src={predefinedImageUrl} />;
};

export default LogoImage;
