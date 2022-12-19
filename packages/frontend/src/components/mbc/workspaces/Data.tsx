import React from 'react';
import Styles from './Data.scss';
import DNACard from 'components/card/Card';
import IconDataProducts from 'components/icons/IconDataProducts';
import IconDataLayer from 'components/icons/IconDataLayer';
import IconDataGovernance from 'components/icons/IconDataGovernance';
import headerImageURL from '../../../assets/images/Data-Landing.png';
import { DataLandingPageElements } from 'globals/landingPageElements';
import LandingSummary from '../shared/landingSummary/LandingSummary';

export interface DataLandingPageIconsType {
  svgIconId: string;
  svgIcon: JSX.Element;
}

const Data = () => {
  const DataLandingPageIcons: DataLandingPageIconsType[] = [
    {
      svgIconId: 'IconDataProducts',
      svgIcon: <IconDataProducts />,
    },
    {
      svgIconId: 'IconDataLayer',
      svgIcon: <IconDataLayer />,
    },
    {
      svgIconId: 'IconDataGovernance',
      svgIcon: <IconDataGovernance />,
    },
  ];

  const cards = DataLandingPageElements;
  return (
    <LandingSummary
      title={'Data'}
      subTitle={
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.'
      }
      tags={['Lorem Ipsum', 'ABC', 'XYZ']}
      headerImage={headerImageURL}
      isBackButton={true}
    >
      <div className={Styles.workspacesWrapper}>
        {cards.map((card, index) => {
          const iconValue = DataLandingPageIcons.filter(
            (item: DataLandingPageIconsType) => card.svgIconId === item.svgIconId,
          )[0].svgIcon;
          return (
            <DNACard
              key={index}
              title={card.name}
              description={card.description}
              url={card.url}
              isExternalLink={card.isExternalLink}
              isTextAlignLeft={card.isTextAlignLeft}
              isDisabled={card.isDisabled}
              isSmallCard={card.isSmallCard}
              isMediumCard={card.isMediumCard}
              svgIcon={iconValue}
              className="data"
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default Data;
