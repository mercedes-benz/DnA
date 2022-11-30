import React from 'react';
import Styles from './Workspaces.scss';
import MainPanel from '../shared/mainPanel/MainPanel';
import DNACard from 'components/card/Card';
import IconDataProducts from 'components/icons/IconDataProducts';
import IconDataLayer from 'components/icons/IconDataLayer';
import IconDataGovernance from 'components/icons/IconDataGovernance';

const Workspaces = () => {
  return (
    <MainPanel title={'Data'} subTitle={'Lorem ipsum dolor sit amet'}>
      <div className={Styles.workspacesWrapper}>
        <DNACard
          title={'Data Products'}
          description={
            'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!'
          }
          url={'/data/dataproductlist'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
          svgIcon={<IconDataProducts />}
        />
        <DNACard
          title={'Data Layer'}
          description={'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!'}
          url={'/data/datalayer'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
          svgIcon={<IconDataLayer />}
        />
        <DNACard
          title={'Data Governance'}
          description={'Data is one of the most valuable assets in our company, therefore we treat our data as a product! We offer you a growing selection of intuitive to use and well documented data products - check it out!'}
          url={'/data/datagovernance'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
          svgIcon={<IconDataGovernance />}
        />
      </div>
    </MainPanel>
  );
};

export default Workspaces;
