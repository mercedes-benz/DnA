import React from 'react';
import Styles from './Trainings.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/Tools-Landing.png';

import { TrainingsLandingPageElements } from 'globals/landingPageElements';

const Trainings = () => {
  const cards = TrainingsLandingPageElements;

  return (
    <LandingSummary
      title={'Trainings'}
      subTitle={
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.'
      }
      headerImage={headerImageURL}
      isBackButton={false}
    >
      <div className={Styles.trainingsWrapper}>
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
              className="trainings"
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default Trainings;
