import React, { useEffect } from 'react';
import Styles from './DataLayer.scss';
// import { Envs } from 'globals/Envs';
import DNACard from 'components/card/Card';
import LandingSummary from 'components/mbc/shared/landingSummary/LandingSummary';
import headerImageURL from '../../../../assets/images/Data-Layer-Landing.png';

const DataLayer = () => {
  

  useEffect(() => {
  });

  return (
    <LandingSummary title={'Data Layer'} 
    subTitle={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. eiusmod tempor incididunt ut labore et dolore magna aliqua.'}
    tags={['Lorem Ipsum', 'ABC', 'XYZ']}
    headerImage={headerImageURL}
    isBackButton={true}>
      <div className={Styles.Workspaces}>
        <DNACard
            title={'Data Model'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={''}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} />
        <DNACard
            title={'KPI Wiki'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={''}
            isExternalLink={true}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} /> 
        <DNACard
            title={'CarLA Economic Model'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={''}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} />
        <DNACard
            title={'Corporate Data Catalogue'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={'/codespaces'}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} /> 
        <DNACard
            title={'SAP Connection Book'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={''}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} />
        <DNACard
            title={'Smart Data Governance'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={'/codespaces'}
            isTextAlignLeft={false}
            isDisabled={true}
            isSmallCard={false}
            isMediumCard={true} />     
      </div>
    </LandingSummary>
  );
};

export default DataLayer;
