import React, { useState, useEffect } from 'react';
import Styles from './DataLayer.scss';
import { Envs } from 'globals/Envs';
import MainPanel from '../../shared/mainPanel/MainPanel';
import DNACard from 'components/card/Card';

const DataLayer = () => {
  const [enableJupiyterNoteWorkspace, setEnableJupiyterNoteWorkspace] = useState(true);
  const [enableDataikuWorkspace, setEnableDataikuWorkspace] = useState(true);
  const enableSapAnalyticsCloud = Envs.ENABLE_SAP_ANALYTICS_CLOUD;
  const sapAnalyticsUrl = Envs.SAP_ANALYTICS_CLOUD_URL;
  const enableCodeSpace = Envs.ENABLE_CODE_SPACE;

  useEffect(() => {
    setEnableJupiyterNoteWorkspace(Envs.ENABLE_JUPYTER_WORKSPACE);
    setEnableDataikuWorkspace(Envs.ENABLE_DATAIKU_WORKSPACE);
  });

  return (
    <MainPanel title={'Data Layer'} subTitle={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.'}>
      <div className={Styles.Workspaces}>
        <DNACard
            title={'Data Model'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={'/notebook'}
            isTextAlignLeft={false}
            isDisabled={!enableJupiyterNoteWorkspace}
            isSmallCard={false}
            isMediumCard={true} />
        <DNACard
            title={'KPI Wiki'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={'/notebook'}
            isTextAlignLeft={false}
            isDisabled={!enableDataikuWorkspace}
            isSmallCard={false}
            isMediumCard={true} /> 
        <DNACard
            title={'CarLA Economic Model'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={sapAnalyticsUrl}
            isTextAlignLeft={false}
            isDisabled={!enableSapAnalyticsCloud}
            isSmallCard={false}
            isMediumCard={true} />
        <DNACard
            title={'Corporate Data Catalogue'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={'/codespaces'}
            isTextAlignLeft={false}
            isDisabled={!enableCodeSpace}
            isSmallCard={false}
            isMediumCard={true} /> 
        <DNACard
            title={'SAP Connection Book'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={sapAnalyticsUrl}
            isTextAlignLeft={false}
            isDisabled={!enableSapAnalyticsCloud}
            isSmallCard={false}
            isMediumCard={true} />
        <DNACard
            title={'Smart Data Governance'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={'/codespaces'}
            isTextAlignLeft={false}
            isDisabled={!enableCodeSpace}
            isSmallCard={false}
            isMediumCard={true} />     
      </div>
    </MainPanel>
  );
};

export default DataLayer;
