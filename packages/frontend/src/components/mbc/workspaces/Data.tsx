import React from 'react';
import Styles from './Data.scss';
import MainPanel from '../shared/mainPanel/MainPanel';
import DNACard from 'components/card/Card';
import IconDataProducts from 'components/icons/IconDataProducts';
import IconDataLayer from 'components/icons/IconDataLayer';
import IconDataGovernance from 'components/icons/IconDataGovernance';

import { DataLandingPageElements } from 'globals/landingPageElements';

export interface DataLandingPageIconsType {
  svgIconId: string;
  svgIcon: JSX.Element;
}

const Data = () => {
  

  const DataLandingPageIcons:DataLandingPageIconsType[] = [
                                {
                                  svgIconId: 'IconDataProducts',
                                  svgIcon: <IconDataProducts />
                                },
                                {
                                  svgIconId: 'IconDataLayer',
                                  svgIcon: <IconDataLayer />
                                },
                                {
                                  svgIconId: 'IconDataGovernance',
                                  svgIcon: <IconDataGovernance />
                                },
                              ];

  const cards = DataLandingPageElements;
  return (
    <MainPanel title={'Data'} subTitle={'Lorem ipsum dolor sit amet'}>
      <div className={Styles.workspacesWrapper}>
      {cards.map((card, index)=> {
        const iconValue = DataLandingPageIcons.filter((item: DataLandingPageIconsType) => card.svgIconId === item.svgIconId)[0].svgIcon;
          return <DNACard
          key={index}
          title={card.name}
          description={card.description}
          url={card.url}
          isExternalLink={card.isExternalLink}
          isTextAlignLeft={card.isTextAlignLeft}
          isDisabled={card.isDisabled}
          isSmallCard={card.isSmallCard}
          isMediumCard={card.isMediumCard}
          svgIcon={iconValue} />
        })}
      </div>
    </MainPanel>
  );
};

export default Data;
