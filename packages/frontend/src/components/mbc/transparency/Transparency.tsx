import React from 'react';
import Styles from './Transparency.scss';
import MainPanel from '../shared/mainPanel/MainPanel';
import DNACard from 'components/card/Card';

import { TranparencyLandingPageElements } from 'globals/landingPageElements';

const Transparency = () => {

  const cards = TranparencyLandingPageElements;

  return (
    <MainPanel title={'Transparency'} subTitle={'Lorem ipsum dolor sit amet'}>
      <div className={Styles.transparencyWrapper}>
        {cards.map((card, index)=> {
          return <DNACard
          key={index}
          title={card.name}
          description={card.description}
          url={card.url}
          isExternalLink={card.isExternalLink}
          isTextAlignLeft={card.isTextAlignLeft}
          isDisabled={card.isDisabled}
          isSmallCard={card.isSmallCard}
          isMediumCard={card.isMediumCard} />
        })}
      </div>
    </MainPanel>
  );
};

export default Transparency;
