import React from 'react';
import Styles from './Carla.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/CarLA-Landing.png';

import { CarlaLandingPageElements } from 'globals/landingPageElements';

const Carla = () => {
  const cards = CarlaLandingPageElements;

  return (
    <LandingSummary
      title={'CarLA'}
      subTitle={
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.'
      }
      headerImage={headerImageURL}
      isBackButton={false}
    >
      <div className={Styles.carlaWrapper}>
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
              svgIcon={card.svgIconId}
              className="carla"
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default Carla;
