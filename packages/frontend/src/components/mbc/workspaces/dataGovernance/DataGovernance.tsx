import React, { useEffect } from 'react';
import Styles from './DataGovernance.scss';
// import { Envs } from 'globals/Envs';
import DNACard from 'components/card/Card';
import LandingSummary from 'components/mbc/shared/landingSummary/LandingSummary';

const DataGovernance = () => {
  

  useEffect(() => {
  });

  return (
    <LandingSummary title={'Data Governance'} 
    subTitle={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.'}
    tags={['Lorem Ipsum', 'ABC', 'XYZ']}>
      <div className={Styles.Workspaces}>
        <DNACard
            title={'DGO Social Intranet'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={''}
            isExternalLink={true}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} />   
        <DNACard
            title={'Minimum Information'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={'/dataproduct/create'}
            isTextAlignLeft={false}
            isDisabled={false}
            isSmallCard={false}
            isMediumCard={true} />
        <DNACard
            title={'LCO/LCR Contacts'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={''}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} />
        
      </div>
    </LandingSummary>
  );
};

export default DataGovernance;
