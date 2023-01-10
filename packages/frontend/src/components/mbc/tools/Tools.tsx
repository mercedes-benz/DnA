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
        'Our standard Data & Analytics for both FC Users and Pro Developers.'
      }
      tags={['Frontend Reporting', 'Data Engineering', 'Data Pipeline', 'Data Science', 'Data Storage', 'Machine Learning', 'No / Low Code', 'Coding', 'Cloud', 'Onprem']}
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
