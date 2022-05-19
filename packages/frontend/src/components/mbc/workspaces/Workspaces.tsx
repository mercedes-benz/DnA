import React, { useState, useEffect } from 'react';
import Styles from './Workspaces.scss';
import { history } from '../../../router/History';
import { Envs } from '../../../globals/Envs';

const Workspaces = () => {
  const [enableJupiyterNoteWorkspace, setEnableJupiyterNoteWorkspace] = useState(true);
  const [ENABLE_DATAIKU_WORKSPACE, setENABLE_DATAIKU_WORKSPACE] = useState(true);

  useEffect(() => {
    setEnableJupiyterNoteWorkspace(Envs.ENABLE_JUPYTER_WORKSPACE);
    setENABLE_DATAIKU_WORKSPACE(Envs.ENABLE_DATAIKU_WORKSPACE);
  });

  const jupyterNav = () => {
    if (enableJupiyterNoteWorkspace) {
      history.push('/notebook');
    } else {
      history.push('/comingsoon');
    }
  };
  const dataIkuNav = () => {
    if (ENABLE_DATAIKU_WORKSPACE) {
      history.push('/mydataiku');
    } else {
      history.push('/comingsoon');
    }
  };
  return (
    <div className={Styles.mainPanel}>
      <div className={Styles.wrapper}>
        <div className={Styles.caption}>
          <h3>Workspaces</h3>
        </div>
        <div className={Styles.content}>
          <div className={Styles.Workspaces}>
            <div className={Styles.WorkspacesNavigation} onClick={jupyterNav}>
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> Jupyter Notebook {!enableJupiyterNoteWorkspace && <label> ( Coming Soon ) </label>} </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right "></i>
                </span>
              </div>
            </div>
            <div className={Styles.WorkspacesNavigation} onClick={dataIkuNav}>
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> Dataiku {!ENABLE_DATAIKU_WORKSPACE && <label> ( Coming Soon ) </label>}</span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right "></i>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Workspaces;
