import React from 'react';
import Styles from './Workspaces.scss';
import MainPanel from '../shared/mainPanel/MainPanel';
import DNACard from 'components/card/Card';

const Workspaces = () => {
  return (
    <MainPanel title={'Data'} subTitle={'Lorem ipsum dolor sit amet'}>
      <div className={Styles.workspacesWrapper}>
        <DNACard
          title={'Data Products'}
          description={
            'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'
          }
          url={'/data/dataproductlist'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
        />
        <DNACard
          title={'Data Layer'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/data/datalayer'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
        />
        <DNACard
          title={'Data Governance'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/data/datagovernance'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true}
        />
      </div>
    </MainPanel>
  );
};

export default Workspaces;
