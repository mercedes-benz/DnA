import React, { useState } from 'react';
import Styles from './Tools.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/Tools-Landing.png';

import { ToolsLandingPageElements } from 'globals/landingPageElements';

const Tools = () => {
  const [cards, setcards] = useState(ToolsLandingPageElements);

  const allTags = Array.from(new Set(Array.prototype.concat.apply([], ToolsLandingPageElements.map((item: any) => item.tags)))) as string[];

  const onTagsFilterSelected = (selectedTags: string[]) => {
    setcards(ToolsLandingPageElements.filter((item: any) => item.tags.some((tag: any) => selectedTags.includes(tag))));
  };

  return (
    <LandingSummary
      title={'Tools'}
      subTitle={
        'Our standard Data & Analytics for both FC Users and Pro Developers.'
      }
      tags={allTags}
      headerImage={headerImageURL}
      isBackButton={true}
      isTagsFilter={true}
      onTagsFilterSelected={onTagsFilterSelected}
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
              animation={card.animation}
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
