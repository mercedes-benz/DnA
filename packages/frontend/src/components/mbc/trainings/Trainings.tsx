import React from 'react';
import Styles from './Trainings.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/Trainings-Landing.png';

import { TrainingsLandingPageElements } from 'globals/landingPageElements';

const Trainings = () => {
  const cards = TrainingsLandingPageElements;

  return (
    <LandingSummary
      title={'Trainings'}
      subTitle={
        'Data and Tools are not enough - here we enable you to become even more productive.'
      }
      headerImage={headerImageURL}
      isBackButton={true}
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
              svgIcon={card.svgIconId}
              className="trainings"
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default Trainings;
