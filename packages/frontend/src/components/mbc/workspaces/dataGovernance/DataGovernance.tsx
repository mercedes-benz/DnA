import React from 'react';
import Styles from './DataGovernance.scss';
import DNACard from 'components/card/Card';
import LandingSummary from 'components/mbc/shared/landingSummary/LandingSummary';
import headerImageURL from '../../../../assets/images/Data-Governance-Landing.png';

import { DataGovernanceElements } from 'globals/landingPageElements';

const DataGovernance = () => {
  const cards = DataGovernanceElements;
  return (
    <LandingSummary
      title={'Data Governance'}
      subTitle={
        'Understanding & fulfilling ever more complex Legal & Compliance requirements can be challenging. Data Governance helps you to navigate through these requirements and supports on you data journey.'
      }
      headerImage={headerImageURL}
      isBackButton={true}
    >
      <div className={Styles.Workspaces}>
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
              svgIcon={card.svgIcon ? card.svgIcon : 'dataproduct'}
              className="data"
            />
          );
        })}
      </div>
    </LandingSummary>
  );
};

export default DataGovernance;
