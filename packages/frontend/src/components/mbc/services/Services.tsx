import classNames from 'classnames';
import Styles from './Services.scss';
import React from 'react';
import { history } from '../../../router/History';
import { Envs } from '../../../globals/Envs';

const Services = () => {
  const ENABLE_MALWARE_SCAN_SERVICE = Envs.ENABLE_MALWARE_SCAN_SERVICE;
  const enableDataPipelineService = Envs.ENABLE_DATA_PIPELINE_SERVICE;
  const enableMLPipelineService = Envs.ENABLE_ML_PIPELINE_SERVICE;
  const enableStorageService = Envs.ENABLE_STORAGE_SERVICE;
  const MLPipelineUrl = enableMLPipelineService ? Envs.ML_PIPELINE_URL : '#/comingsoon';

  const malwareNav = () => {
    if (ENABLE_MALWARE_SCAN_SERVICE) {
      history.push('/malwarescanservice');
    } else {
      history.push('/comingsoon');
    }
  };
  const modelRegistryNav = () => {
    history.push('/modelregistry');
  };
  const pipelineNav = () => {
    if (enableDataPipelineService) {
      history.push('/pipeline');
    } else {
      history.push('/comingsoon');
    }
  };
  const storageNav = () => {
    if (enableStorageService) {
      history.push('/storage');
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
                <span> Malware Scan {!ENABLE_MALWARE_SCAN_SERVICE && <label> ( Coming Soon ) </label>} </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right "></i>
                </span>
              </div>
            </div>
            <div className={Styles.WorkspacesNavigation} onClick={pipelineNav}>
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> Data Pipeline {!enableDataPipelineService && <label> ( Coming Soon ) </label>} </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right"></i>
                </span>
              </div>
            </div>
            <a
              className={classNames('wrapper-link', Styles.WorkspacesNavigation)}
              href={MLPipelineUrl}
              target="_blank"
              rel="noreferrer"
            >
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> ML Pipeline {!enableMLPipelineService && <label> ( Coming Soon ) </label>} </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right"></i>
                </span>
              </div>
            </a>
          </div>
          <div className={Styles.Workspaces}>
            <div className={Styles.WorkspacesNavigation} onClick={storageNav}>
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> My Storage {!enableStorageService && <label> ( Coming Soon ) </label>} </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right"></i>
                </span>
              </div>
            </div>
            <div className={Styles.WorkspacesNavigation} onClick={modelRegistryNav}>
              <div className={Styles.WorkspacesNavigationVisual}></div>
              <div className={Styles.WorkspacesNavigationTitle}>
                <span> My Model Registry </span>
                <span>
                  {' '}
                  <i className="icon mbc-icon arrow small right"></i>
                </span>
              </div>
            </div>
            <div className={Styles.EmptyCard}>&nbsp;</div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Services;
