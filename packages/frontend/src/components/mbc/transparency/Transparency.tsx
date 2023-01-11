import React from 'react';
import Styles from './Transparency.scss';
import DNACard from 'components/card/Card';
import headerImageURL from '../../../assets/images/Transparency-Landing.png';
import { TranparencyLandingPageElements } from 'globals/landingPageElements';
import LandingSummary from '../shared/landingSummary/LandingSummary';

const Transparency = () => {
  const cards = TranparencyLandingPageElements;

  return (
    <LandingSummary
      title={'Transparency'}
      subTitle={
        'Explore all Data & AI Solutions in MB and view your Portfolio.'
      }
      tags={['Lorem Ipsum', 'ABC', 'XYZ']}
      headerImage={headerImageURL}
      isBackButton={true}
    >
      <div className={Styles.transparencyWrapper}>
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
              className="transparency"
              svgIcon={card.svgIcon}
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default Transparency;
