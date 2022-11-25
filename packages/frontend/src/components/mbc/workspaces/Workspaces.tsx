import React, { useState, useEffect } from 'react';
import Styles from './Workspaces.scss';
import { Envs } from 'globals/Envs';
// import Tile from '../shared/tile/Tile';
import MainPanel from '../shared/mainPanel/MainPanel';
// import jupyter from '../../../assets/images/Jupyter.png';
// import dataiku from '../../../assets/images/Dataiku.png';
// import sap from '../../../assets/images/Sap.png';
import DNACard from 'components/card/Card';

const Workspaces = () => {
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
    <MainPanel title={'Data'} subTitle={'Lorem ipsum dolor sit amet'}>
      <div className={Styles.Workspaces}>
        <DNACard
          title={'Data Products'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/notebook'}
          isTextAlignLeft={false}
          isDisabled={!enableJupiyterNoteWorkspace}
          isSmallCard={false}
          isMediumCard={true} />
        <DNACard
          title={'Data Layer'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/datalayer'}
          isTextAlignLeft={false}
          isDisabled={false}
          isSmallCard={false}
          isMediumCard={true} />
        <DNACard
          title={'Data Governance'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/notebook'}
          isTextAlignLeft={false}
          isDisabled={!enableDataikuWorkspace}
          isSmallCard={false}
          isMediumCard={true} />  



        <DNACard
          title={'Jupyter Notebook'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/notebook'}
          isTextAlignLeft={false}
          isDisabled={!enableJupiyterNoteWorkspace}
          isSmallCard={false}
          isMediumCard={true} />
        <DNACard
          title={'Dataiku'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/notebook'}
          isTextAlignLeft={false}
          isDisabled={!enableDataikuWorkspace}
          isSmallCard={false}
          isMediumCard={true} />
        {enableSapAnalyticsCloud && (  
          <DNACard
            title={'SAP Analytics Cloud'}
            description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
            url={sapAnalyticsUrl}
            isTextAlignLeft={false}
            isDisabled={!enableSapAnalyticsCloud}
            isSmallCard={false}
            isMediumCard={true} />
        )}   
        <DNACard
          title={'My Code Space (Beta)'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/codespaces'}
          isTextAlignLeft={false}
          isDisabled={!enableCodeSpace}
          isSmallCard={false}
          isMediumCard={true} /> 

        {/* <Tile
          title={'Jupyter Notebook'}
          background={jupyter}
          bgPosition={'center'}
          route={'/notebook'}
          isEnabled={enableJupiyterNoteWorkspace}
        />
        <Tile
          title={'Dataiku'}
          background={dataiku}
          bgPosition={'center'}
          route={'/mydataiku'}
          isEnabled={enableDataikuWorkspace}
        />
        {enableSapAnalyticsCloud && (
          <Tile
            title={'SAP Analytics Cloud'}
            background={sap}
            bgPosition={'center'}
            link={sapAnalyticsUrl}
            isEnabled={enableSapAnalyticsCloud}
          />
        )} 
        <Tile title={'My Code Space (Beta)'} route={'/codespaces'} isEnabled={enableCodeSpace} /> */}
      </div>
    </MainPanel>
  );
};

export default Workspaces;
