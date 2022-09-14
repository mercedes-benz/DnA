import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './style.scss';
import { useDispatch, useSelector } from 'react-redux';

import { hostServer } from '../../server/api';
import { useForm, FormProvider } from 'react-hook-form';
import { deserializeFormData, consumerOpenSegments, serializeDivisionSubDivision } from '../../Utility/formData';
import { UpdateDataProducts } from '../redux/dataProduct.services';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';

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
    businessOwnerName: '',
    division: '0',
    subDivision: '0',
    department: '',
    planningIT: 'APP-',
    lcoNeeded: '',
    complianceOfficer: '',
  },
  'consumer-personal-data': {
    personalRelatedData: '',
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
  const { formState, reset } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const [isEditing, setIsEditing] = useState(false);
  const [isFormMounted, setFormMounted] = useState(false);
  const [providerFormIsDraft, setProviderDraftState] = useState(false);

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

  const dispatch = useDispatch();
  const provideDataProducts = useSelector((state) => state.provideDataProducts);
  const divisionList = useSelector((state) => state.provideDataProducts.divisionList);
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
    const { id } = provideDataProducts.selectedDataProduct;
    if (id) {
      let defaultValues = { ...provideDataProducts.selectedDataProduct.consumer };
      reset(defaultValues); // setting default values
    }
    //eslint-disable-next-line
  }, [dispatch, provideDataProducts.selectedDataProduct]);

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      setDivisions(res.data);
      ProgressIndicator.hide();
      dispatch(setDivisionList(res.data));
      SelectBox.defaultSetup();
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

  useEffect(() => {
    if (providerFormIsDraft)
      Notification.show('Provider form is still in Draft. Please reach out to Information Owner', 'warning');
  }, [providerFormIsDraft]);

  const getDataProductById = () => {
    dataProductsApi.getDataProductById(dataProductId).then((res) => {
      const isCreator = res.data?.createdBy.id === user.id;
      const isValidUser =
        res.data.users?.find((item) => user.id === item.shortId || user.eMail === item.email) || false;
      if (res.status === 204) {
        return history.push('/NotFound');
      } else if (isCreator || (res.data.users?.length > 0 && !isValidUser)) {
        return history.push('/Unauthorized');
      } else {
        const data = deserializeFormData(res.data, 'consumer');
        dispatch(setDataProduct(data));
        setIsEditing(data.publish);
        setFormMounted(true);
        setProviderDraftState(!res.data.providerFormSubmitted);
        reset({ ...data.consumer });
        let segments = [];
        res.data.consumerInformation?.openSegments?.map((seg) => {
          for (let key in consumerOpenSegments) {
            if (consumerOpenSegments[key] === seg) {
              segments.push(key);
            }
          }
        });
        setSavedTabs(segments);
      }
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

  const onSave = (currentTab, values, callbackFn) => {
    const saveSegments = consumerOpenSegments[currentTab];
    if (!values.openSegments?.length && currentTab === 'consumer-contact-info') {
      values.openSegments = ['ContactInformation'];
    } else if (values.openSegments?.indexOf(saveSegments) === -1) {
      values.openSegments.push(saveSegments);
    }

    const division = serializeDivisionSubDivision(divisionList, values);
    const value = { ...provideDataProducts.selectedDataProduct };

    value.notifyUsers = values.notifyUsers || false;
    value.publish = values.publish || false;

    const formValues = {
      consumerInformation: {
        contactInformation: {
          appId: values.planningIT,
          department: values.department,
          division,
          lcoNeeded: values.lcoNeeded ? true : false,
          localComplianceOfficer: values.complianceOfficer?.toString(),
          ownerName: values.businessOwnerName,
        },
        openSegments: values.openSegments,
        personalRelatedData: {
          comment: values.LCOComments,
          lcoChecked: values.LCOCheckedLegalBasis,
          legalBasis: values.personalRelatedDataLegalBasis,
          personalRelatedData: values.personalRelatedData === 'Yes' ? true : false, //boolean,
          purpose: values.personalRelatedDataPurpose,
        },
      },
    };

    const data = {
      values: { ...value, formValues },
      onSave: () => {
        switchTabs(currentTab);
        if (typeof callbackFn === 'function') callbackFn();
      },
      provideDataProducts,
      type: 'consumer',
      state: isEditing ? 'edit' : 'create',
    };

    dispatch(UpdateDataProducts(data));
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
                  <li
                    className={
                      savedTabs?.includes('provider-summary') ||
                      provideDataProducts.selectedDataProduct?.consumer?.openSegments?.length
                        ? 'tab valid'
                        : 'tab active'
                    }
                  >
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
                  <li
                    className={
                      savedTabs?.includes('consumer-contact-info') && !providerFormIsDraft
                        ? 'tab valid'
                        : 'tab disabled'
                    }
                  >
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
                  <li
                    className={
                      savedTabs?.includes('consumer-personal-data') && !providerFormIsDraft
                        ? 'tab valid'
                        : 'tab disabled'
                    }
                  >
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
                <ProviderSummary
                  onSave={() => switchTabs('provider-summary')}
                  providerFormIsDraft={providerFormIsDraft}
                />
              </div>
              <div id="tab-content-2" className="tab-content">
                {currentTab === 'consumer-contact-info' && (
                  <ContactInformation
                    onSave={(values, callbackFn) => onSave('consumer-contact-info', values, callbackFn)}
                    divisions={divisions}
                    setSubDivisions={setSubDivisions}
                    subDivisions={subDivisions}
                    isFormMounted={isFormMounted}
                  />
                )}
              </div>
              <div id="tab-content-3" className="tab-content">
                {currentTab === 'consumer-personal-data' && (
                  <PersonalRelatedData
                    onSave={(values, callbackFn) => onSave('consumer-personal-data', values, callbackFn)}
                    setIsEditing={(val) => setIsEditing(val)}
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
      <TourGuide type="ConsumerForm" />
    </>
  );
};

export default withRouter(ConsumerForm);
