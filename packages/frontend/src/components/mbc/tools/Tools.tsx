import React from 'react';
import Styles from './Tools.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/Tools-Landing.png';

import { ToolsLandingPageElements } from 'globals/landingPageElements';

const Tools = () => {
  const cards = ToolsLandingPageElements;

  return (
    <LandingSummary
      title={'Tools'}
      subTitle={
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.'
      }
      tags={['SAP', 'Self Service', 'No/Low Code', 'FOSS', 'Automation']}
      headerImage={headerImageURL}
      isBackButton={true}
      isTagsFilter={true}
    >
      <>
      <div className={Styles.toolsWrapper}>
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
              className="tools"
            />
          );
        })}
      </div>
      </>
    </LandingSummary>
  );
};

export default Tools;
