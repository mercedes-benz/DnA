import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './Form.style.scss';

import { useForm, FormProvider } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';

import Tabs from '../../common/modules/uilab/js/src/tabs';
// import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

import { mapOpenSegments } from '../../Utility/formData';

// Container Components
import ConfirmModal from 'dna-container/ConfirmModal';

// Form Components
import RunForecast from './RunForecast';
import ForecastResults from './ForecastResults';
import ManageConnections from './ManageConnections';
import ProjectDetails from './ProjectDetails';

const tabs = {
  runForecast: { runName: '', configuration: '0', frequency: '0', forecastHorizon: '0', comment: '' },
  forecastResults: {},
  manageConnections: {},
  projectDetails: {},
};

const ForecastForm = ({ user }) => {
  const projects = useSelector((state) => state.projects);

  const [currentTab, setCurrentTab] = useState('runForecast');
  // const [savedTabs, setSavedTabs] = useState([]);
  const [savedTabs, setSavedTabs] = useState(['forecastResults','manageConnections','projectDetails']);
  const methods = useForm();
  const { formState, reset } = methods;

  const [configurationFile, setConfigurationFile] = useState([]);
  const [frequency, setFrequency] = useState([]);
  const [forecastHorizon, setForecastHorizon] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));
  const dispatch = useDispatch();

  const getDataProductById = () => {};

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
    // const { id } = provideDataProducts.selectedProject;
    // if (isCreatePage) {
    //   if (id) {
    //     let defaultValues = { ...provideDataProducts.selectedProject };
    //     reset(defaultValues); // setting default values
    //   } else {
    const data = tabs[currentTab];
    reset(data); // setting default values
    // }
    // }
    //eslint-disable-next-line
  }, [dispatch, projects]);

  useEffect(() => {
    setConfigurationFile([]);
    setFrequency([]);
    setForecastHorizon([]);
  }, []);

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      const isFieldsDirty = formState.isDirty || Object.keys(formState.dirtyFields).length > 0;
      if (isFieldsDirty) {
        setShowChangeAlert({ modal: true, switchingTab: id });
      } else {
        setCurrentTab(id);
      }
    }
  };

  const switchTabs = (currentTab) => {
    const tabIndex = Object.keys(tabs).indexOf(currentTab) + 1;
    setSavedTabs([...new Set([...savedTabs, currentTab])]);
    if (currentTab !== 'projectDetails') {
      setCurrentTab(Object.keys(tabs)[tabIndex]);
      elementRef.current[tabIndex].click();
    }
  };

  const onSave = (currentTab, values) => {
    const saveSegments = mapOpenSegments[currentTab];
    if (values.openSegments.indexOf(saveSegments) === -1) {
      values.openSegments.push(saveSegments);
    }
  };

  return (
    <FormProvider {...methods}>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.breadcrumb)}>
          <ol>
            <li><a href='#/'>Start</a></li>
            <li><a href='#/services'>My Services</a></li>
            <li><Link to='/'>Chronos Forecasting</Link></li>
            <li>Project Name</li>
          </ol>
        </div>
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
                <ForecastResults
                  onSave={(values) => onSave('forecastResults', values)}
                  configurationFile={configurationFile}
                  frequency={frequency}
                  forecastHorizon={forecastHorizon}
                />
              )}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'manageConnections' && (
                <ManageConnections onSave={(values) => onSave('manageConnections', values)} />
              )}
            </div>
            <div id="tab-content-4" className="tab-content">
              {currentTab === 'projectDetails' && (
                <ProjectDetails onSave={(values) => onSave('projectDetails', values)} />
              )}
            </div>
          </div>
        </div>
        <ConfirmModal
          title="Save Changes?"
          acceptButtonTitle="Close"
          cancelButtonTitle="Cancel"
          showAcceptButton={true}
          showCancelButton={true}
          show={showChangeAlert?.modal}
          content={
            <div id="contentparentdiv">
              Press &#187;Close&#171; to save your changes or press
              <br />
              &#187;Cancel&#171; to discard changes.
            </div>
          }
          onCancel={() => {
            getDataProductById();
            setCurrentTab(showChangeAlert.switchingTab);
            elementRef.current[Object.keys(tabs).indexOf(showChangeAlert.switchingTab)].click();
            setShowChangeAlert({ modal: false, switchingTab: '' });
          }}
          onAccept={() => {
            setShowChangeAlert({ modal: false, switchingTab: '' });
            elementRef.current[Object.keys(tabs).indexOf(currentTab)].click();
          }}
        />
      </div>
      {currentTab !== 'basic-info' && <div className={Styles.mandatoryInfo}>* mandatory fields</div>}
    </FormProvider>
  );
};
export default ForecastForm;
