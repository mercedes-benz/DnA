import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './style.scss';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';

import { hostServer } from '../../server/api';
import { useForm, FormProvider } from 'react-hook-form';
import { deserializeFormData, consumerOpenSegments } from '../../Utility/formData';
import { UpdateDataProducts } from '../redux/dataProduct.services';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

import ContactInformation from './ContactInformation';
import PersonalRelatedData from './PersonalRelatedData';
import TourGuide from '../TourGuide';
import ProviderSummary from './ProviderSummary';
import { dataProductsApi } from '../../apis/dataproducts.api';
import { useParams, withRouter } from 'react-router-dom';
import { setDataProduct, setDivisionList } from '../redux/dataProductSlice';

const tabs = {
  'provider-summary': {},
  'consumer-contact-info': {
    businessOwner: '',
    division: '0',
    subDivision: '0',
    department: '',
    planningIT: 'APP-',
  },
  'consumer-personal-data': {
    personalRelatedDataTransferred: '',
    personalRelatedDataPurpose: '',
    personalRelatedDataLegalBasis: '',
    LCOCheckedLegalBasis: '',
    LCOComments: '',
  },
};

const ConsumerForm = ({ user, history }) => {
  const [currentTab, setCurrentTab] = useState('provider-summary');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { setValue, formState, reset, watch } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

  const dispatch = useDispatch();
  const provideDataProducts = useSelector((state) => state.provideDataProducts);
  const { id: dataProductId } = useParams();

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
    const defaultValues = Object.values(tabs).reduce((acc, curr) => ({ ...acc, ...curr }), {});
    Object.entries(defaultValues)?.map(([key, value]) => {
      setValue(key, value); // setting default values
    });
  }, [setValue]);

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      setDivisions(res.data);
      dispatch(setDivisionList(res.data));
      SelectBox.defaultSetup();
      ProgressIndicator.hide();
    });
  }, [dispatch]);

  useEffect(() => {
    getDataProductById();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    return () => {
      dispatch(setDataProduct({}));
    };
  }, [dispatch]);

  const getDataProductById = () => {
    dataProductsApi.getDataProductById(dataProductId).then((res) => {
      const data = deserializeFormData(res.data);
      dispatch(setDataProduct(data));
      // reset(data);
      let segments = [];
      res.data.openSegments?.map((seg) => {
        for (let key in consumerOpenSegments) {
          if (consumerOpenSegments[key] === seg) {
            segments.push(key);
          }
        }
      });
      setSavedTabs(segments);
    });
  };

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
    if (currentTab !== 'consumer-personal-data') {
      setCurrentTab(Object.keys(tabs)[tabIndex]);
      elementRef.current[tabIndex].click();
    }
  };

  const onSave = (currentTab, values) => {
    const saveSegments = consumerOpenSegments[currentTab];
    if (currentTab === 'consumer-contact-info') {
      // values.openSegments = ['CustomerContactInformation'];
    } else if (values.openSegments?.indexOf(saveSegments) === -1) {
      // values.openSegments.push(saveSegments);
    }

    if (currentTab !== 'consumer-contact-info') {
      const value = { ...provideDataProducts.selectedDataProduct };
      const data = {
        values: { ...value, ...values },
        onSave: () => history.push('/'),
        provideDataProducts,
      };

      dispatch(UpdateDataProducts(data));
    }

    switchTabs(currentTab);
  };

  return (
    <>
      <button
        className={classNames('btn btn-text back arrow', Styles.backBtn)}
        type="submit"
        onClick={() => history.push('/')}
      >
        Back
      </button>
      <FormProvider {...methods}>
        <div className={classNames(Styles.mainPanel)}>
          <h3 className={classNames(Styles.title)}>Data Receiving Side</h3>
          <div id="data-product-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={savedTabs?.includes('provider-summary') ? 'tab valid' : 'tab active'}>
                    <a
                      href="#tab-content-1"
                      id="provider-summary"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[0] = ref;
                      }}
                      onClick={setTab}
                    >
                      Provider Summary
                    </a>
                  </li>
                  <li className={savedTabs?.includes('consumer-contact-info') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-2"
                      id="consumer-contact-info"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[1] = ref;
                      }}
                      onClick={setTab}
                    >
                      Contact Information
                    </a>
                  </li>
                  <li className={savedTabs?.includes('consumer-personal-data') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-3"
                      id="consumer-personal-data"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[2] = ref;
                      }}
                      onClick={setTab}
                    >
                      Personal Related Data
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                <ProviderSummary onSave={() => switchTabs('provider-summary')} />
              </div>
              <div id="tab-content-2" className="tab-content">
                {currentTab === 'consumer-contact-info' && (
                  <ContactInformation
                    onSave={(values) => onSave('consumer-contact-info', values)}
                    divisions={divisions}
                    setSubDivisions={setSubDivisions}
                    subDivisions={subDivisions}
                  />
                )}
              </div>
              <div id="tab-content-3" className="tab-content">
                {currentTab === 'consumer-personal-data' && (
                  <PersonalRelatedData onSave={(values) => onSave('consumer-personal-data', values)} />
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
              reset(watch()); // to reset different states of the form.
              const defaultValues = watch(); // update api response to reset
              // defaultValues['personalRelatedDataDescription'] = 'sample code';
              Object.entries(defaultValues)?.map(([key, value]) => {
                setValue(key, value); // setting default values
              });
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
        <div className={Styles.mandatoryInfo}>* mandatory fields</div>
      </FormProvider>
      <TourGuide type="ConsumerForm" />
    </>
  );
};

export default withRouter(ConsumerForm);
