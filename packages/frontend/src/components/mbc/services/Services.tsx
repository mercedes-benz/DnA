import React from 'react';
import Styles from './Services.scss';
import { Envs } from '../../../globals/Envs';
import Tile from '../shared/tile/Tile';
import MainPanel from '../shared/mainPanel/MainPanel';
import malwarescan from '../../../assets/images/Malware_Scan.jpg';
import datapipeline from '../../../assets/images/Data_Pipeline.jpg';
import mlpipeline from '../../../assets/images/ML_Pipeline.jpg';
import mystorage from '../../../assets/images/My_Storage.jpg';
import chronosforecasting from '../../../assets/images/Chronos_Forecasting.jpg';

const Services = () => {
  const enableMalwareScanService = Envs.ENABLE_MALWARE_SCAN_SERVICE;
  const enableDataPipelineService = Envs.ENABLE_DATA_PIPELINE_SERVICE;
  const enableMyModelRegistryService = Envs.ENABLE_MY_MODEL_REGISTRY_SERVICE;
  const enableMLPipelineService = Envs.ENABLE_ML_PIPELINE_SERVICE;
  const enableStorageService = Envs.ENABLE_STORAGE_SERVICE;
  const mLPipelineUrl = enableMLPipelineService ? Envs.ML_PIPELINE_URL : '#/comingsoon';
  const enableChronosForecastingService = Envs.ENABLE_CHRONOS_FORECASTING_SERVICE;

  return (
    <MainPanel title={'Services'}>
      <div className={Styles.Workspaces}>
        <Tile
          title={'Malware Scan'}
          background={malwarescan}
          bgPosition={'center'}
          route={'/malwarescanservice'}
          isEnabled={enableMalwareScanService}
        />
        <Tile
          title={'Data Pipeline'}
          background={datapipeline}
          bgPosition={'center'}
          route={'/pipeline'}
          isEnabled={enableDataPipelineService}
        />
        <Tile
          title={'ML Pipeline'}
          background={mlpipeline}
          bgPosition={'center'}
          link={mLPipelineUrl}
          isEnabled={enableMLPipelineService}
        />
        <Tile
          title={'My Storage'}
          background={mystorage}
          bgPosition={'center'}
          route={'/storage'}
          isEnabled={enableStorageService}
        />
        <Tile title={'My Model Registry'} route={'/modelregistry'} isEnabled={enableMyModelRegistryService} />
        <Tile
          title={'Chronos Forecasting'}
          background={chronosforecasting}
          bgPosition={'center'}
          route={'/chronos'}
          isEnabled={enableChronosForecastingService}
        />
      </div>
    </MainPanel>
  );
};

export default Services;
