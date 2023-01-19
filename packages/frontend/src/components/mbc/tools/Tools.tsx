import React, { useState } from 'react';
import Styles from './Tools.scss';
import DNACard from 'components/card/Card';
import LandingSummary from '../shared/landingSummary/LandingSummary';
import headerImageURL from '../../../assets/images/Tools-Landing.png';

import { ToolsLandingPageElements } from 'globals/landingPageElements';

const Tools = (props: any) => {
  const tag = props.match.params.tag;
  let toolsToShowOnLoad: any[] = [];
  let selectedTags: string[] = [];
  switch (tag) {
    case 'lowcode':
      selectedTags = ['No / Low Code'];
      toolsToShowOnLoad = ToolsLandingPageElements.filter((item: any) => item.tags.some((tag: any) => selectedTags.includes(tag)));
      break;
    case 'prodev':
      selectedTags = ['Coding'];
      toolsToShowOnLoad = ToolsLandingPageElements.filter((item: any) => item.tags.some((tag: any) => selectedTags.includes(tag)));
      break;
    default:
      toolsToShowOnLoad = ToolsLandingPageElements
      break;    
  }
  const [cards, setcards] = useState(toolsToShowOnLoad);

  const allTags = Array.from(new Set(Array.prototype.concat.apply([], ToolsLandingPageElements.map((item: any) => item.tags)))) as string[];

  const onTagsFilterSelected = (selectedTags: string[]) => {
    if(selectedTags.length) {
      setcards(ToolsLandingPageElements.filter((item: any) => item.tags.some((tag: any) => selectedTags.includes(tag))));
    } else {
      setcards(ToolsLandingPageElements);
    }
  };

  return (
    <LandingSummary
      title={'Tools'}
      subTitle={
        'Our standard Data & Analytics for both FC Users and Pro Developers.'
      }
      selectedTags={selectedTags}
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
