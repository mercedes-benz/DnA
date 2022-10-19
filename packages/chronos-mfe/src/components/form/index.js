import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './Form.style.scss';

import { Link } from 'react-router-dom';

import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

// Form Components
import RunForecast from './RunForecast';
import ForecastResults from './ForecastResults';
import ManageConnections from './ManageConnections';
import ProjectDetails from './ProjectDetails';
import Breadcrumb from '../shared/breadcrumb/Breadcrumb';
import { chronosApi } from '../../apis/chronos.api';
import Notification from '../../common/modules/uilab/js/src/notification';

const tabs = {
  runForecast: {},
  forecastResults: {},
  manageConnections: {},
  projectDetails: {},
};

const ForecastForm = ({ user }) => {
  const { id: projectId } = useParams();

  const [currentTab, setCurrentTab] = useState('runForecast');

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

  const [loading, setLoading] = useState(true);
  const [project, setProject] = useState();

  useEffect(() => {
    if (user?.roles?.length) {
      Tabs.defaultSetup();
    } else {
      setTimeout(() => {
        Tabs.defaultSetup();
      }, 100);
    }
  }, [user]);

  useEffect(() => {
    getProjectById();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectById = () => {
    ProgressIndicator.show();
    chronosApi.getForecastProjectById(projectId).then((res) => {
      setProject(res.data);
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(error.message, 'alert');
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  const setTab = (e) => {
    setCurrentTab(e.target.id);
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Breadcrumb>
          <li><Link to='/'>Chronos Forecasting</Link></li>
          <li>{!loading && project?.name}</li>
        </Breadcrumb>
        <h3 className={classNames(Styles.title)}>{!loading && project?.name}</h3>
        <div id="data-product-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={'tab active'}>
                  <a href="#tab-content-1" id="runForecast" ref={elementRef} onClick={setTab}>
                    Run Forecast
                  </a>
                </li>
                <li className={'tab valid'}>
                  <a
                    href="#tab-content-2"
                    id="forecastResults"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[1] = ref;
                    }}
                    onClick={setTab}
                  >
                    Forecast Results
                  </a>
                </li>
                {/* <li className={savedTabs?.includes('manageConnections') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-3"
                    id="manageConnections"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[2] = ref;
                    }}
                    onClick={setTab}
                  >
                    Manage Connections
                  </a>
                </li> */}
                <li className={'tab valid'}>
                  <a
                    href="#tab-content-4"
                    id="projectDetails"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[3] = ref;
                    }}
                    onClick={setTab}
                  >
                    Project Details
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            <div id="tab-content-1" className="tab-content">
              <RunForecast />
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'forecastResults' && (
                <ForecastResults />
              )}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'manageConnections' && (
                <ManageConnections />
              )}
            </div>
            <div id="tab-content-4" className="tab-content">
              {currentTab === 'projectDetails' && (
                <ProjectDetails />
              )}
            </div>
          </div>
        </div>
      </div>
      {currentTab !== 'basic-info' && <div className={Styles.mandatoryInfo}>* mandatory fields</div>}
    </>
  );
};
export default ForecastForm;
