import Styles from './Services.scss';
import React, { useState, useEffect } from 'react';
import { history } from '../../../router/History';
import { Envs } from '../../../globals/Envs';

const Services = () => {
  const [enableMalwareService, setEnableMalwareService] = useState(true);
  const [enablePipelineService, setEnablePipelineService] = useState(true);

  useEffect(() => {
    setEnableMalwareService(Envs.ENABLEMALWARESERVICE);
    setEnablePipelineService(Envs.ENABLEPIPELINSERVICE);
  });

  const malwareNav = () => {
    if (enableMalwareService) {
      history.push('/malwarescanservice');
    } else {
      history.push('/comingsoon');
    }
  };
  const pipelineNav = () => {
    if (enablePipelineService) {
      history.push('/pipeline');
    } else {
      history.push('/comingsoon');
    }
  };

  return (
    <React.Fragment>
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.caption}>
            <h3>Services</h3>
          </div>
        </div>
        <div className={Styles.content}>
          <div className={Styles.Workspaces}>
            <div className={Styles.WorkspacesNavigation} onClick={malwareNav}>
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> Malware Scan {!enableMalwareService && <label> ( Coming Soon ) </label>} </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right "></i>
                </span>
              </div>
            </div>
            <div className={Styles.WorkspacesNavigation} onClick={pipelineNav}>
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> Pipeline {!enablePipelineService && <label> ( Coming Soon ) </label>} </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right"></i>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Services;
