import React, { useEffect } from 'react';
import Styles from './DataLayer.scss';
import DNACard from 'components/card/Card';
import LandingSummary from 'components/mbc/shared/landingSummary/LandingSummary';
import headerImageURL from '../../../../assets/images/Data-Layer-Landing.png';

import { DataLayerElements } from 'globals/landingPageElements';

const DataLayer = () => {
  const cards = DataLayerElements;

  useEffect(() => {});

  return (
    <LandingSummary
      title={'Data Layer'}
      subTitle={
        'More information and definitions of applications, key figures and data.'
      }
      headerImage={headerImageURL}
      isBackButton={true}
    >
      <div className={Styles.Workspaces}>
        {cards.map((card, index) => {
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
              svgIcon={card.svgIcon}
              className="data"
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default DataLayer;
