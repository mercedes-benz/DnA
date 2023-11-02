import React from 'react';
import Styles from './GenAI.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/CarLA-Landing.png';

import { GenAILandingPageElements } from 'globals/landingPageElements';

const GenAI = () => {
  const cards = GenAILandingPageElements;

  return (
    <LandingSummary
      title={'GenAI'}
      subTitle={
        'GenAI is the core element of the BI & Analytics landscape in Finance Controlling at MBC. It bundles All the Tools and insight you need to start with Artificial Intelligence.'
      }
      headerImage={headerImageURL}
      isBackButton={true}
    >
      <div className={Styles.genAIWrapper}>
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
              className="genAI"
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default GenAI;
