import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './style.scss';
import { useDispatch, useSelector } from 'react-redux';

import { hostServer } from '../../../server/api';
import { useForm, FormProvider } from 'react-hook-form';
import { deserializeFormData, consumerOpenSegments, serializeDivisionSubDivision } from '../../../Utility/formData';
import { UpdateDataTransfers } from '../redux/dataTransfer.services';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

import Tabs from '../../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../common/modules/uilab/js/src/notification';

import ContactInformation from './ContactInformation';
import PersonalRelatedData from './PersonalRelatedData';
import TourGuide from '../TourGuide';
import ProviderSummary from './ProviderSummary';
import { dataTransferApi } from '../../../apis/datatransfers.api';
import { useParams, withRouter } from 'react-router-dom';
import { setSelectedDataTransfer, setDivisionList } from '../redux/dataTransferSlice';

import { omit } from 'lodash';
import { CompleteDataProductMinimumInfo } from '../../dataProduct/redux/dataProduct.services';

const tabs = {
  'provider-summary': {},
  'consumer-contact-info': {
    businessOwnerName: '',
    division: '0',
    subDivision: '0',
    department: '',
    dateOfAgreement: '',
    planningIT: '',
    lcoNeeded: 'No',
    complianceOfficer: '',
  },
  'consumer-personal-data': {
    personalRelatedData: '',
    personalRelatedDataPurpose: '',
    personalRelatedDataLegalBasis: '',
    LCOCheckedLegalBasis: '',
    LCOComments: '',
    personalRelatedDataContactAwareTransfer: '',
    personalRelatedDataObjectionsTransfer: '',
    personalRelatedDataTransferingNonetheless: '',
    personalRelatedDataTransferingObjections: '',
  },
};

const ConsumerForm = ({ user, history, isDataProduct = false, callbackFn }) => {
  const [currentTab, setCurrentTab] = useState(isDataProduct ? 'consumer-contact-info' : 'provider-summary');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { formState, reset } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const [isEditing, setIsEditing] = useState(false);
  const [isFormMounted, setFormMounted] = useState(false);
  const [providerFormIsDraft, setProviderDraftState] = useState(false);

  const tabNames = isDataProduct ? omit(tabs, ['provider-summary']) : tabs;
  const elementRef = useRef(Object.keys(tabNames)?.map(() => createRef()));

  const dispatch = useDispatch();
  const provideDataTransfers = useSelector((state) =>
    !isDataProduct ? state.provideDataTransfers : state.dataProduct,
  );
  const divisionList = useSelector((state) =>
    !isDataProduct ? state.provideDataTransfers.divisionList : state.dataProduct.divisionList,
  );
  const { id: dataTransferId } = useParams();

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
    const { id } = !isDataProduct
      ? provideDataTransfers.selectedDataTransfer
      : provideDataTransfers.selectedDataProduct;
    if (id) {
      const data = !isDataProduct
        ? provideDataTransfers.selectedDataTransfer.consumer
        : provideDataTransfers.selectedDataProduct.consumer || tabs['consumer-contact-info'];
      let defaultValues = { ...data };
      reset(defaultValues); // setting default values
    }
    //eslint-disable-next-line
  }, [dispatch, provideDataTransfers.selectedDataTransfer, isDataProduct, provideDataTransfers.selectedDataProduct]);

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
    if (!isDataProduct) {
      getDataProductById();
    } else {
      setFormMounted(true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    return () => {
      dispatch(setSelectedDataTransfer({}));
    };
  }, [dispatch]);

  useEffect(() => {
    if (providerFormIsDraft)
      Notification.show('Provider form is still in Draft. Please reach out to Data responsible IO and/or Business Owner for application', 'warning');
  }, [providerFormIsDraft]);

  const getDataProductById = () => {
    ProgressIndicator.show();
    dataTransferApi
      .getDataTransferById(dataTransferId)
      .then((res) => {
        const isCreator = res.data?.providerInformation?.createdBy?.id === user.id;
        const isValidUser =
          res.data.providerInformation?.users?.find((item) => user.id === item.shortId || user.eMail === item.email) ||
          false;
        if (res.status === 204) {
          return history.push('/NotFound');
        } else if (isCreator || (res.data.providerInformation?.users?.length > 0 && !isValidUser)) {
          return history.push('/Unauthorized');
        } else {
          const data = deserializeFormData({ item: res.data, type: 'consumer' });
          dispatch(setSelectedDataTransfer(data));
          setIsEditing(data.publish);
          setFormMounted(true);
          setProviderDraftState(!res.data.providerInformation?.providerFormSubmitted);
          reset({ ...data.consumer });
          let segments = [];
          res.data?.consumerInformation?.openSegments?.map((seg) => {
            for (let key in consumerOpenSegments) {
              if (consumerOpenSegments[key] === seg) {
                segments.push(key);
              }
            }
          });
          setSavedTabs(segments);
        }
        ProgressIndicator.hide();
      })
      .catch((e) => {
        Notification.show(e?.message || 'Error while fetching the Data Product Information', 'alert');
        ProgressIndicator.hide();
      });
  };

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      const isFieldsDirty = formState.isDirty || Object.keys(formState.dirtyFields).length > 0;
      if (!isDataProduct) {
        if (isFieldsDirty) {
          setShowChangeAlert({ modal: true, switchingTab: id });
        } else {
          setCurrentTab(id);
        }
      } else {
        setCurrentTab(id);
      }
    }
  };

  const switchTabs = (currentTab) => {
    const tabIndex = Object.keys(tabNames).indexOf(currentTab) + 1;
    setSavedTabs([...new Set([...savedTabs, currentTab])]);
    if (currentTab !== 'consumer-personal-data') {
      setCurrentTab(Object.keys(tabNames)[tabIndex]);
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

    const selectedData = !isDataProduct
      ? provideDataTransfers.selectedDataTransfer
      : provideDataTransfers.selectedDataProduct;
    const value = { ...selectedData };

    value.notifyUsers = values.notifyUsers || false;
    value.publish = values.publish || false;

    if (isDataProduct) {
      value.dataTransferName = `${value.productName} - ${values.dataTransferName || value.dataTransferName}`;
    }

    const openSegments = !isDataProduct
      ? values.openSegments
      : ['ContactInformation', currentTab === 'consumer-personal-data' && 'IdentifyingPersonalRelatedData'];
      const consumerFormValues = {
      consumerInformation: {
        contactInformation: {
          appId: values.planningIT,
          department: values.department?.toString(),
          division,
          lcoNeeded: values.lcoNeeded === 'Yes' ? true : false,
          localComplianceOfficer: values.complianceOfficer?.toString(),
          ownerName: values.businessOwnerName,
          agreementDate: new Date(values.dateOfAgreement),
        },
        openSegments,
        personalRelatedData: {
          comment: values.LCOComments || '',
          lcoChecked: values.LCOCheckedLegalBasis || '',
          legalBasis: values.personalRelatedDataLegalBasis || '',
          personalRelatedData: values.personalRelatedData === 'Yes' ? true : false, //boolean,
          purpose: values.personalRelatedDataPurpose || '',
          contactAwareTransfer: values?.personalRelatedDataContactAwareTransfer === 'Yes' ? true : false, //boolean,
          objectionsToTransfer: values?.personalRelatedDataObjectionsTransfer === 'Yes' ? true : false, //boolean,
          transferringNonetheless: values?.personalRelatedDataTransferingNonetheless || '',
          objections: values?.personalRelatedDataTransferingObjections || '',
        },
      },
    };

    const data = {
      values: { ...value, consumerFormValues },
      onSave: () => {
        switchTabs(currentTab);
        if (typeof callbackFn === 'function') callbackFn();
      },
      provideDataTransfers,
      type: 'consumer',
      state: isEditing ? 'edit' : 'create',
      isDataProduct,
    };

    if (isDataProduct) {
      dispatch(CompleteDataProductMinimumInfo(data));
    } else {
      dispatch(UpdateDataTransfers(data));
    }
  };

  return (
    <>
      {!isDataProduct ? (
        <button
          className={classNames('btn btn-text back arrow', Styles.backBtn)}
          type="submit"
          onClick={() => history.push('/datasharing')}
        >
          Back
        </button>
      ) : null}
      <FormProvider {...methods}>
        <div className={classNames(Styles.mainPanel)}>
          {!isDataProduct ? (
            <>
              <div className={classNames(Styles.screenLabel)}>Data Receiving Side</div>
              <h3 className={classNames(Styles.title, currentTab !== 'provider-summary' ? '' : 'hidden')}>
                {provideDataTransfers.selectedDataTransfer?.productName}
              </h3>{' '}
            </>
          ) : null}
          <div id="data-product-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  {!isDataProduct ? (
                    <li
                      className={
                        savedTabs?.includes('provider-summary') ||
                        provideDataTransfers.selectedDataTransfer?.consumer?.openSegments?.length
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
                  ) : null}
                  <li
                    className={
                      savedTabs?.includes('consumer-contact-info') && !providerFormIsDraft
                        ? 'tab valid'
                        : isDataProduct
                        ? 'tab active'
                        : 'tab disabled'
                    }
                  >
                    <a
                      href="#tab-content-2"
                      id="consumer-contact-info"
                      ref={(ref) => {
                        if (elementRef.current) {
                          isDataProduct ? (elementRef.current[0] = ref) : (elementRef.current[1] = ref);
                        }
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
                        if (elementRef.current) {
                          isDataProduct ? (elementRef.current[1] = ref) : (elementRef.current[2] = ref);
                        }
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
              {!isDataProduct ? (
                <div id="tab-content-1" className="tab-content">
                  <ProviderSummary
                    onSave={() => switchTabs('provider-summary')}
                    providerFormIsDraft={providerFormIsDraft}
                  />
                </div>
              ) : null}
              <div id="tab-content-2" className="tab-content">
                {currentTab === 'consumer-contact-info' && (
                  <ContactInformation
                    onSave={(values, callbackFn) => onSave('consumer-contact-info', values, callbackFn)}
                    divisions={divisions}
                    setSubDivisions={setSubDivisions}
                    subDivisions={subDivisions}
                    isFormMounted={isFormMounted}
                    isDataProduct={isDataProduct}
                  />
                )}
              </div>
              <div id="tab-content-3" className="tab-content">
                {currentTab === 'consumer-personal-data' && (
                  <PersonalRelatedData
                    onSave={(values, callbackFn) => onSave('consumer-personal-data', values, callbackFn)}
                    setIsEditing={(val) => setIsEditing(val)}
                    isDataProduct={isDataProduct}
                    callbackFn={callbackFn}
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
              elementRef.current[Object.keys(tabNames).indexOf(showChangeAlert.switchingTab)].click();
              setShowChangeAlert({ modal: false, switchingTab: '' });
            }}
            onAccept={() => {
              setShowChangeAlert({ modal: false, switchingTab: '' });
              elementRef.current[Object.keys(tabNames).indexOf(currentTab)].click();
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
