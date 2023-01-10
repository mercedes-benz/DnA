import React from 'react';
import Styles from './Carla.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/CarLA-Landing.png';

import { CarLALandingPageElements } from 'globals/landingPageElements';

const Carla = () => {
  const cards = CarLALandingPageElements;

  return (
    <LandingSummary
      title={'CarLA'}
      subTitle={
        'CarLA (Cars analysis and reporting landscape) is the core element of the BI & Analytics landscape in Finance Controlling at MBC. It bundles all data & functions, which are used for analysis and reporting of business performance and planning.'
      }
      headerImage={headerImageURL}
      isBackButton={true}
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
