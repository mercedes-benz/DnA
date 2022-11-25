import React, { useEffect, useState } from 'react';
import Styles from './Tools.scss';
import { Envs } from 'globals/Envs';
import MainPanel from '../shared/mainPanel/MainPanel';
import DNACard from 'components/card/Card';

const Tools = () => {
  
  const [enableJupiyterNoteWorkspace, setEnableJupiyterNoteWorkspace] = useState(true);
  const [enableDataikuWorkspace, setEnableDataikuWorkspace] = useState(true);
  const enableMalwareScanService = Envs.ENABLE_MALWARE_SCAN_SERVICE;
  const enableDataPipelineService = Envs.ENABLE_DATA_PIPELINE_SERVICE;
  const enableMyModelRegistryService = Envs.ENABLE_MY_MODEL_REGISTRY_SERVICE;
  const enableMLPipelineService = Envs.ENABLE_ML_PIPELINE_SERVICE;
  const enableStorageService = Envs.ENABLE_STORAGE_SERVICE;
  const mLPipelineUrl = enableMLPipelineService ? Envs.ML_PIPELINE_URL : '#/comingsoon';
  const enableChronosForecastingService = Envs.ENABLE_CHRONOS_FORECASTING_SERVICE;
  const enableSapAnalyticsCloud = Envs.ENABLE_SAP_ANALYTICS_CLOUD;
  const sapAnalyticsUrl = Envs.SAP_ANALYTICS_CLOUD_URL;
  const enableCodeSpace = Envs.ENABLE_CODE_SPACE;

  useEffect(() => {
    setEnableJupiyterNoteWorkspace(Envs.ENABLE_JUPYTER_WORKSPACE);
    setEnableDataikuWorkspace(Envs.ENABLE_DATAIKU_WORKSPACE);
  });

  return (
    <MainPanel title={'Tools'} subTitle={'Lorem ipsum dolor sit amet'}>
      <div className={Styles.toolsWrapper}>
        <DNACard
          title={'Malware Scan'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/malwarescanservice'}
          isTextAlignLeft={false}
          isDisabled={!enableMalwareScanService}
          isSmallCard={false}
          isMediumCard={true} />
        <DNACard
          title={'Data Pipeline'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/pipeline'}
          isTextAlignLeft={false}
          isDisabled={!enableDataPipelineService}
          isSmallCard={false}
          isMediumCard={true} />
        <DNACard
          title={'ML Pipeline'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={mLPipelineUrl}
          isTextAlignLeft={false}
          isDisabled={!enableMLPipelineService}
          isSmallCard={false}
          isMediumCard={true} />
        <DNACard
          title={'My Storage'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/storage'}
          isTextAlignLeft={false}
          isDisabled={!enableStorageService}
          isSmallCard={false}
          isMediumCard={true} />  
        <DNACard
          title={'My Model Registry'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/modelregistry'}
          isTextAlignLeft={false}
          isDisabled={!enableMyModelRegistryService}
          isSmallCard={false}
          isMediumCard={true} />  
        <DNACard
          title={'Chronos Forecasting'}
          description={'Data is one of the most valuable assets in our company,&nbsp;therefore we treat our data as a product!&nbsp;We offer you a&nbsp;growing selection of intuitive to use and well documented&nbsp;data products - check it out!'}
          url={'/chronos'}
          isTextAlignLeft={false}
          isDisabled={!enableChronosForecastingService}
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
          url={'/mydataiku'}
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

      </div>
    </MainPanel>
  );
};

export default Tools;
