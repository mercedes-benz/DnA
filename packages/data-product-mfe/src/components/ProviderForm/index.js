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

// Form Components
import BasicInformation from './BasicInformation';
import ContactInformation from './ContactInformation';
import Classification from './ClassificationAndConfidentiality';
import PersonalRelatedData from './PersonalRelatedData';
import TransNationalDataTransfer from './TransNationalDataTransfer';
import DataOriginating from './DataOriginating';
import OtherRelevant from './OtherRelavantInfo';
import DeletionRequirements from './DeletionRequirements';

const tabs = {
  'basic-info': {},
  'contact-info': {
    productName: '',
    dateOfDataTransfer: '',
    name: '',
    division: '0',
    subDivision: '0',
    department: '',
    complianceOfficer: '',
    planningIT: '',
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
  },
  'data-originating-china': { dataOriginatedFromChina: '' },
  'deletion-requirements': { deletionRequirement: '', deletionRequirementDescription: '' },
  'other-information': { otherRelevantInfo: '' },
};

const ProviderForm = ({ user, history }) => {
  const isCreatePage = history.location.pathname === '/create';
  const isEditPage = /^\/edit/.test(history?.location?.pathname);

  const provideDataProducts = useSelector((state) => state.provideDataProducts);

  const [currentTab, setCurrentTab] = useState(isEditPage ? 'contact-info' : 'basic-info');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { setValue, formState, reset, watch } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));
  const dispatch = useDispatch();

  const { id: dataProductId } = useParams();

  const getDataProductById = () => {
    dataProductsApi.getDataProductById(dataProductId).then((res) => {
      const data = deserializeFormData(res.data);
      dispatch(setDataProduct(data));
      reset(data);
      let segments = [];
      res.data.openSegments?.map((seg) => {
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
    if (isCreatePage) {
      if (id) {
        let defaultValues = { ...provideDataProducts.selectedDataProduct };
        reset(defaultValues); // setting default values
      } else {
        const data = watch();
        reset(data); // setting default values
      }
    } //eslint-disable-next-line
  }, [dispatch, setValue, provideDataProducts.selectedDataProduct, isCreatePage]);

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      setDivisions(res.data);
      ProgressIndicator.hide();
      dispatch(setDivisionList(res.data));
    });
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch]);

  useEffect(() => {
    if (isEditPage) {
      getDataProductById();
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
    if (currentTab !== 'other-information') {
      setCurrentTab(Object.keys(tabs)[tabIndex]);
      elementRef.current[tabIndex].click();
    }
  };

  const onSave = (currentTab, values) => {
    const saveSegments = mapOpenSegments[currentTab];
    if (isCreatePage && currentTab === 'contact-info') {
      values.openSegments = ['ContactInformation'];
    } else if (values.openSegments.indexOf(saveSegments) === -1) {
      values.openSegments.push(saveSegments);
    }
    const data = {
      values,
      onSave: () => switchTabs(currentTab),
      provideDataProducts,
    };
    if (isCreatePage) {
      const { id } = provideDataProducts.selectedDataProduct;
      if (id) {
        data.values['id'] = id;
        dispatch(UpdateDataProducts(data));
      } else dispatch(SetDataProducts(data));
    } else if (isEditPage) {
      dispatch(UpdateDataProducts(data));
    }
  };

  return (
    <FormProvider {...methods}>
      <div className={classNames(Styles.mainPanel)}>
        <h3 className={classNames(Styles.title)}>Provide Data Product</h3>
        <div id="data-product-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                {!isEditPage ? (
                  <li className={savedTabs?.includes('basic-info') ? 'tab valid' : 'tab active'}>
                    <a href="#tab-content-1" id="basic-info" ref={elementRef} onClick={setTab}>
                      Basic Information
                    </a>
                  </li>
                ) : null}
                <li
                  className={
                    savedTabs?.includes('contact-info') ? 'tab valid' : isEditPage ? 'tab active' : 'tab disabled'
                  }
                >
                  <a
                    href="#tab-content-2"
                    id="contact-info"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[1] = ref;
                    }}
                    onClick={setTab}
                  >
                    Contact Information
                  </a>
                </li>
                <li className={savedTabs?.includes('classification-confidentiality') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-3"
                    id="classification-confidentiality"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[2] = ref;
                    }}
                    onClick={setTab}
                  >
                    Classification & Confidentiality
                  </a>
                </li>
                <li className={savedTabs?.includes('personal-data') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-4"
                    id="personal-data"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[3] = ref;
                    }}
                    onClick={setTab}
                  >
                    Identifying personal related data
                  </a>
                </li>
                <li className={savedTabs?.includes('trans-national-data-transfer') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-5"
                    id="trans-national-data-transfer"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[4] = ref;
                    }}
                    onClick={setTab}
                  >
                    Identifiying Trans-national Data Transfer
                  </a>
                </li>
                <li className={savedTabs?.includes('data-originating-china') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-6"
                    id="data-originating-china"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[5] = ref;
                    }}
                    onClick={setTab}
                  >
                    Identifying data originating from China
                  </a>
                </li>
                <li className={savedTabs?.includes('deletion-requirements') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-7"
                    id="deletion-requirements"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[6] = ref;
                    }}
                    onClick={setTab}
                  >
                    Specify deletion requirements
                  </a>
                </li>
                <li className={savedTabs?.includes('other-information') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-8"
                    id="other-information"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[7] = ref;
                    }}
                    onClick={setTab}
                  >
                    Specifying other relevant information
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            {!isEditPage ? (
              <div id="tab-content-1" className="tab-content">
                <BasicInformation onSave={() => switchTabs('basic-info')} />
              </div>
            ) : null}
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'contact-info' && (
                <ContactInformation
                  onSave={(values) => onSave('contact-info', values)}
                  divisions={divisions}
                  setSubDivisions={setSubDivisions}
                  subDivisions={subDivisions}
                />
              )}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'classification-confidentiality' && (
                <Classification onSave={(values) => onSave('classification-confidentiality', values)} />
              )}
            </div>
            <div id="tab-content-4" className="tab-content">
              {currentTab === 'personal-data' && (
                <PersonalRelatedData onSave={(values) => onSave('personal-data', values)} />
              )}
            </div>
            <div id="tab-content-5" className="tab-content">
              {currentTab === 'trans-national-data-transfer' && (
                <TransNationalDataTransfer onSave={(values) => onSave('trans-national-data-transfer', values)} />
              )}
            </div>
            <div id="tab-content-6" className="tab-content">
              {currentTab === 'data-originating-china' && (
                <DataOriginating onSave={(values) => onSave('data-originating-china', values)} />
              )}
            </div>
            <div id="tab-content-7" className="tab-content">
              {currentTab === 'deletion-requirements' && (
                <DeletionRequirements onSave={(values) => onSave('deletion-requirements', values)} />
              )}
            </div>
            <div id="tab-content-8" className="tab-content">
              {currentTab === 'other-information' && (
                <OtherRelevant onSave={(values) => onSave('other-information', values)} />
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
export default ProviderForm;
