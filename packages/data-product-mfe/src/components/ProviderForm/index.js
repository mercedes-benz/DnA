import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './Form.style.scss';

import { useForm, FormProvider } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';

import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

import { hostServer } from '../../server/api';
import { dataProductsApi } from '../../apis/dataproducts.api';

import { setDataProduct, setDivisionList } from '../redux/dataProductSlice';
import { SetDataProducts, UpdateDataProducts } from '../redux/dataProduct.services';
import { deserializeFormData, mapOpenSegments } from '../../Utility/formData';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

// Form Components
import ContactInformation from './ContactInformation';
import Classification from './ClassificationAndConfidentiality';
import PersonalRelatedData from './PersonalRelatedData';
import TransNationalDataTransfer from './TransNationalDataTransfer';
import DeletionRequirements from './DeletionRequirements';

import TourGuide from '../TourGuide';

const tabs = {
  'contact-info': {
    productName: '',
    dateOfDataTransfer: '',
    name: '',
    division: '0',
    subDivision: '0',
    department: '',
    complianceOfficer: '',
    planningIT: 'APP-',
  },
  'classification-confidentiality': { classificationOfTransferedData: '', confidentiality: 'Public' },
  'personal-data': {
    personalRelatedData: '',
    personalRelatedDataDescription: '',
    personalRelatedDataPurpose: '',
    personalRelatedDataLegalBasis: '',
  },
  'trans-national-data-transfer': {
    transnationalDataTransfer: '',
    transnationalDataTransferNotWithinEU: '',
    LCOApprovedDataTransfer: '',
    insiderInformation: '',
    dataOriginatedFromChina: '',
  },
  'deletion-requirements': { deletionRequirement: '', deletionRequirementDescription: '', otherRelevantInfo: '' },
};

const ProviderForm = ({ user, history }) => {
  const isCreatePage = history.location.pathname === '/create';
  const isEditPage = /^\/edit/.test(history?.location?.pathname);

  const provideDataProducts = useSelector((state) => state.provideDataProducts);

  const [currentTab, setCurrentTab] = useState('contact-info');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { formState, reset } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));
  const dispatch = useDispatch();

  const { id: dataProductId } = useParams();
  const createCopyId = history.location?.state?.copyId;

  const getDataProductById = () => {
    const id = createCopyId || dataProductId;
    dataProductsApi.getDataProductById(id).then((res) => {
      if (createCopyId) {
        // creating copy of existing data product
        // below properties needs to be reset to new ones for the copy
        res.data.dataProductId = '';
        res.data.id = '';
        res.data.notifyUsers = false;
        res.data.publish = false;
        res.data.providerInformation.providerFormSubmitted = false;
        res.data.providerInformation.users = [];
        delete res.data.providerInformation.createdBy;
        delete res.data.providerInformation.createdDate;
        delete res.data.providerInformation.lastModifiedDate;
        delete res.data.providerInformation.modifiedBy;
        delete res.data.consumerInformation;
      }
      const data = deserializeFormData(res.data);
      dispatch(setDataProduct(data));
      reset(data);
      let segments = [];
      res.data.providerInformation?.openSegments?.map((seg) => {
        for (let key in mapOpenSegments) {
          if (mapOpenSegments[key] === seg) {
            segments.push(key);
          }
        }
      });
      setSavedTabs(segments);
    });
  };

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
    const { id } = provideDataProducts.selectedDataProduct;
    if (isCreatePage && !createCopyId) {
      if (id) {
        let defaultValues = { ...provideDataProducts.selectedDataProduct };
        reset(defaultValues); // setting default values
      } else {
        const data = tabs['contact-info'];
        // set default "Your name" as logged in user name
        const userInfo = {
          addedByProvider: true,
          company: user.company || '',
          department: user.department,
          email: user.eMail || user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          mobileNumber: user.mobileNumber,
          shortId: user.id || user.shortId,
          teamMemberPosition: user.teamMemberPosition || '',
          userType: user.userType || '',
        };
        data.name = userInfo;
        reset(data); // setting default values
      }
    }
    //eslint-disable-next-line
  }, [dispatch, provideDataProducts.selectedDataProduct, isCreatePage]);

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      setDivisions(res.data);
      ProgressIndicator.hide();
      dispatch(setDivisionList(res.data));
      SelectBox.defaultSetup();
    });
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch]);

  useEffect(() => {
    if (isEditPage || createCopyId) {
      divisions.length > 0 && getDataProductById();
    }
    //eslint-disable-next-line
  }, [divisions]);

  useEffect(() => {
    return () => {
      dispatch(setDataProduct({}));
    };
  }, [dispatch]);

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
    if (currentTab !== 'deletion-requirements') {
      setCurrentTab(Object.keys(tabs)[tabIndex]);
      elementRef.current[tabIndex].click();
    }
  };

  const onSave = (currentTab, values, callbackFn) => {
    const saveSegments = mapOpenSegments[currentTab];
    if (isCreatePage && !createCopyId && currentTab === 'contact-info') {
      values.openSegments = ['ContactInformation'];
    } else if (values.openSegments?.indexOf(saveSegments) === -1) {
      values.openSegments.push(saveSegments);
    }
    const data = {
      values,
      onSave: () => {
        switchTabs(currentTab);
        if (typeof callbackFn === 'function') callbackFn();
      },
      provideDataProducts,
    };
    if (isCreatePage) {
      const { id } = provideDataProducts.selectedDataProduct;
      if (id) {
        data.values['id'] = id;
        data.type = 'provider';
        dispatch(UpdateDataProducts(data));
      } else dispatch(SetDataProducts(data));
    } else if (isEditPage) {
      data.type = 'provider';
      data.state = 'edit';
      dispatch(UpdateDataProducts(data));
    }
  };

  return (
    <>
      <button
        className={classNames('btn btn-text back arrow', Styles.backBtn)}
        type="submit"
        onClick={() => history.goBack()}
      >
        Back
      </button>
      <FormProvider {...methods}>
        <div className={classNames(Styles.mainPanel)}>
          <h3 className={classNames(Styles.title)}>Data Providing Side</h3>
          <div id="data-product-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={savedTabs?.includes('contact-info') ? 'tab valid' : 'tab active'}>
                    <a
                      href="#tab-content-1"
                      id="contact-info"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[0] = ref;
                      }}
                      onClick={setTab}
                    >
                      Contact Information
                    </a>
                  </li>
                  <li className={savedTabs?.includes('classification-confidentiality') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-2"
                      id="classification-confidentiality"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[1] = ref;
                      }}
                      onClick={setTab}
                    >
                      Data Description & Classification
                    </a>
                  </li>
                  <li className={savedTabs?.includes('personal-data') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-3"
                      id="personal-data"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[2] = ref;
                      }}
                      onClick={setTab}
                    >
                      Personal Related Data
                    </a>
                  </li>
                  <li className={savedTabs?.includes('trans-national-data-transfer') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-4"
                      id="trans-national-data-transfer"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[3] = ref;
                      }}
                      onClick={setTab}
                    >
                      Trans-national Data
                    </a>
                  </li>
                  <li className={savedTabs?.includes('deletion-requirements') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-5"
                      id="deletion-requirements"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[4] = ref;
                      }}
                      onClick={setTab}
                    >
                      Deletion Requirements & Other
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                <ContactInformation
                  onSave={(values) => onSave('contact-info', values)}
                  divisions={divisions}
                  setSubDivisions={setSubDivisions}
                  subDivisions={subDivisions}
                />
              </div>
              <div id="tab-content-2" className="tab-content">
                {currentTab === 'classification-confidentiality' && (
                  <Classification onSave={(values) => onSave('classification-confidentiality', values)} />
                )}
              </div>
              <div id="tab-content-3" className="tab-content">
                {currentTab === 'personal-data' && (
                  <PersonalRelatedData onSave={(values) => onSave('personal-data', values)} />
                )}
              </div>
              <div id="tab-content-4" className="tab-content">
                {currentTab === 'trans-national-data-transfer' && (
                  <TransNationalDataTransfer onSave={(values) => onSave('trans-national-data-transfer', values)} />
                )}
              </div>
              <div id="tab-content-5" className="tab-content">
                {currentTab === 'deletion-requirements' && (
                  <DeletionRequirements
                    onSave={(values, callbackFn) => onSave('deletion-requirements', values, callbackFn)}
                    user={user}
                  />
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
        <div className={Styles.mandatoryInfo}>* mandatory fields</div>
      </FormProvider>
      {!isEditPage && <TourGuide />}
    </>
  );
};
export default ProviderForm;
