import React, { useState, useEffect } from 'react';
import Styles from './Workspaces.scss';
import { Envs } from '../../../globals/Envs';
import Tile from '../shared/tile/Tile';
import MainPanel from '../shared/mainPanel/MainPanel';
import jupyter from '../../../assets/images/Jupyter.png';
import dataiku from '../../../assets/images/Dataiku.png';
import sap from '../../../assets/images/Sap.png';

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
    <MainPanel title={'Workspaces'}>
      <div className={Styles.Workspaces}>
        <Tile
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
        { enableSapAnalyticsCloud &&
          <Tile
            title={'SAP Analytics Cloud'}
            background={sap}
            bgPosition={'center'}
            link={sapAnalyticsUrl}
            isEnabled={enableSapAnalyticsCloud}
          />
        }
        <Tile
          title={'My Code Space'}
          route={'/codespaces'}
          isEnabled={enableCodeSpace}
        />
      </div>
    </MainPanel>
  );
};

export default Workspaces;
