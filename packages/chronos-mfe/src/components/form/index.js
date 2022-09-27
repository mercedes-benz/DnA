import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './Form.style.scss';

import { useForm, FormProvider } from 'react-hook-form';
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

const tabs = {
  runForecast: { runName: '', configuration: '0', frequency: '0', forecastHorizon: '0', comment: '' },
  forecastResults: {},
  manageConnections: {},
  projectDetails: {},
};

const ForecastForm = ({ user }) => {
  const { id: projectId } = useParams();

  const [currentTab, setCurrentTab] = useState('runForecast');
  const [savedTabs, setSavedTabs] = useState(['forecastResults','manageConnections','projectDetails']);
  const methods = useForm();
  // const { formState, reset } = methods;

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

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
      setProject(res);
      ProgressIndicator.hide();
    }).catch(error => {
      console.log(error.message);
      setProject({
        "id": "1",
        "name": "Forecast Two",
        "bucketName": "chronos-Forecast One",
        "permission": {
          "read": true,
          "write": true
        },
        "collaborators": [
          {
            "id": "1",
            "permissions": {
              "read": true,
              "write": true
            },
            "firstName": "Aniruddha",
            "lastName": "Khartade",
            "department": "IT",
            "email": "anirudh@mercedes-benz.com",
            "mobileNumber": "123456780"
          }
        ],
        "apiKey": "28392839283nfsdisdi823",
        "createdBy": {
          "id": "DEMUSER",
          "firstName": "Demo",
          "lastName": "User",
          "department": "TE/ST",
          "email": "demouser@web.de",
          "mobileNumber": "01701234567"
        },
        "createdOn": "2022-09-24T12:14:47.511+00:00",
        "savedInputs": [
          {
            "id": null,
            "name": "sample",
            "path": "path/sample",
            "createdBy": "DEMUSER",
            "createdOn": "2022-09-24T12:36:36.301+00:00"
          }
        ],
        "runs": [
          {
            "id": "1",
            "jobId": 1,
            "ranBy": "DEMUSER",
            "runId": 1,
            "state": {
              "result_state": "IN PROGRESS",
              "state_message": "string",
              "life_cycle_state": "INTERNAL_ERROR",
              "user_cancelled_or_timedout": true
            },
            "comment": "test run",
            "endTime": null,
            "runName": "runtest",
            "createdBy": "DEMUSER",
            "frequency": "Daily",
            "inputFile": "path/file",
            "startTime": "2022-09-24T12:14:47.511+00:00",
            "setupDuration": null,
            "forecastHorizon": "12",
            "configurationFile": "Default-Settings",
            "executionDuration": null
          },
          {
            "id": "2",
            "jobId": 2,
            "ranBy": "DEMUSER",
            "runId": 2,
            "state": {
              "result_state": "IN PROGRESS",
              "state_message": "string",
              "life_cycle_state": "INTERNAL_ERROR",
              "user_cancelled_or_timedout": true
            },
            "comment": "test run",
            "endTime": null,
            "runName": "runtest",
            "createdBy": "DEMUSER",
            "frequency": "Daily",
            "inputFile": "path/file",
            "startTime": "2022-09-24T12:14:47.511+00:00",
            "setupDuration": null,
            "forecastHorizon": "12",
            "configurationFile": "Default-Settings",
            "executionDuration": null
          },
          {
            "id": "3",
            "jobId": 3,
            "ranBy": "DEMUSER",
            "runId": 3,
            "state": {
              "result_state": "CANCELED",
              "state_message": "string",
              "life_cycle_state": "INTERNAL_ERROR",
              "user_cancelled_or_timedout": true
            },
            "comment": "test run",
            "endTime": null,
            "runName": "runtest",
            "createdBy": "DEMUSER",
            "frequency": "Daily",
            "inputFile": "path/file",
            "startTime": "2022-09-24T12:14:47.511+00:00",
            "setupDuration": null,
            "forecastHorizon": "12",
            "configurationFile": "Default-Settings",
            "executionDuration": null
          },
          {
            "id": "4",
            "jobId": 4,
            "ranBy": "DEMUSER",
            "runId": 4,
            "state": {
              "result_state": "SUCCESS",
              "state_message": "string",
              "life_cycle_state": "INTERNAL_ERROR",
              "user_cancelled_or_timedout": true
            },
            "comment": "test run",
            "endTime": null,
            "runName": "runtest",
            "createdBy": "DEMUSER",
            "frequency": "Daily",
            "inputFile": "path/file",
            "startTime": "2022-09-24T12:14:47.511+00:00",
            "setupDuration": null,
            "forecastHorizon": "12",
            "configurationFile": "Default-Settings",
            "executionDuration": null
          },
        ]
      });
      ProgressIndicator.hide();
    });
  };

  const setTab = (e) => {
    setCurrentTab(e.target.id);
  };

  const switchTabs = (currentTab) => {
    const tabIndex = Object.keys(tabs).indexOf(currentTab) + 1;
    setSavedTabs([...new Set([...savedTabs, currentTab])]);
    if (currentTab !== 'projectDetails') {
      setCurrentTab(Object.keys(tabs)[tabIndex]);
      elementRef.current[tabIndex].click();
    }
  };

  return (
    <FormProvider {...methods}>
      <div className={classNames(Styles.mainPanel)}>
        <Breadcrumb>
          <li><Link to='/'>Chronos Forecasting</Link></li>
          <li>Project Name</li>
        </Breadcrumb>
        <h3 className={classNames(Styles.title)}>Forecasting Project Name</h3>
        <div id="data-product-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={savedTabs?.includes('runForecast') ? 'tab valid' : 'tab active'}>
                  <a href="#tab-content-1" id="runForecast" ref={elementRef} onClick={setTab}>
                    Run Forecast
                  </a>
                </li>
                <li className={savedTabs?.includes('forecastResults') ? 'tab valid' : 'tab disabled'}>
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
                <li className={savedTabs?.includes('projectDetails') ? 'tab valid' : 'tab disabled'}>
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
              <RunForecast onSave={() => switchTabs('runForecast')} />
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'forecastResults' && (
                <ForecastResults forecastRuns={project?.runs ? project?.runs : []} />
              )}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'manageConnections' && (
                <ManageConnections />
              )}
            </div>
            <div id="tab-content-4" className="tab-content">
              {currentTab === 'projectDetails' && (
                <ProjectDetails project={project} />
              )}
            </div>
          </div>
        </div>
      </div>
      {currentTab !== 'basic-info' && <div className={Styles.mandatoryInfo}>* mandatory fields</div>}
    </FormProvider>
  );
};
export default ForecastForm;
