import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './Form.style.scss';

import { useForm, FormProvider } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';

import Tabs from '../../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../common/modules/uilab/js/src/notification';

import { hostServer } from '../../../server/api';
import { dataTransferApi } from '../../../apis/datatransfers.api';

import { setSelectedDataTransfer, setDivisionList } from '../redux/dataTransferSlice';
import { SetDataTransfers, UpdateDataTransfers } from '../redux/dataTransfer.services';
import { deserializeFormData, mapOpenSegments } from '../../../Utility/formData';
import OtherRelevant from '../../dataTransfer/ProviderForm/OtherRelavantInfo';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

// Form Components
import ContactInformation from './ContactInformation';
import Classification from './ClassificationAndConfidentiality';
import PersonalRelatedData from './PersonalRelatedData';
import TransNationalDataTransfer from './TransNationalDataTransfer';
import DeletionRequirements from './DeletionRequirements';

import TourGuide from '../TourGuide';

export const tabs = {
  'contact-info': {
    productName: '',
    name: '',
    division: '0',
    subDivision: '0',
    department: '',
    complianceOfficer: '',
    planningIT: '',
    informationOwner: '',
  },
  'classification-confidentiality': { classificationOfTransferedData: '', confidentiality: 'Public' },
  'personal-data': {
    personalRelatedData: '',
    personalRelatedDataDescription: '',
    personalRelatedDataPurpose: '',
    personalRelatedDataLegalBasis: '',
    personalRelatedDataContactAwareTransfer: '',
    personalRelatedDataObjectionsTransfer: '',
    personalRelatedDataTransferingNonetheless: '',
    personalRelatedDataTransferingObjections: '',
  },
  'trans-national-data-transfer': {
    transnationalDataTransfer: '',
    transnationalDataTransferNotWithinEU: '',
    insiderInformation: '',
    dataOriginatedFromChina: '',
    transnationalDataContactAwareTransfer: '',
    transnationalDataObjectionsTransfer: '',
    transnationalDataTransferingNonetheless: '',
    transnationalDataTransferingObjections: ''
  },
  'deletion-requirements': { deletionRequirement: '', deletionRequirementDescription: '', otherRelevantInfo: '' },
};

const ProviderForm = ({ user, history }) => {
  const isCreatePage = history.location.pathname === '/datasharing/create';
  const isEditPage = /^\/datasharing\/edit/.test(history?.location?.pathname);

  const provideDataTransfers = useSelector((state) => state.provideDataTransfers);

  const [currentTab, setCurrentTab] = useState('contact-info');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { formState, reset } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));
  const dispatch = useDispatch();

  const { id: dataTransferId } = useParams();
  let createCopyId = history.location?.state?.copyId;

  const [errorsInPublish, setErrorsInPublish] = useState({
    descriptionTabError: [],
    contactInformationTabError: [],
    dataDescriptionClassificationTabError: [],
    personalRelatedDataTabError: [],
    transnationalDataTabError: [],
    deletionRequirementsTabError: [],
    saveTabError:[]
  });

  const [showAllTabsError, setShowAllTabsError] = useState(false);
  const [showContactInformationTabError, setShowContactInformationTabError] = useState(false);
  const [isTouChecked, setIsTouChecked] = useState(false);
  // const [actionButtonName, setActionButtonName] = useState('');


  // set default value of "Name" field as logged in user name

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

  const getDataProductById = () => {
    const id = createCopyId || dataTransferId || provideDataTransfers?.selectedDataTransfer?.id;
    ProgressIndicator.show();
    dataTransferApi
      .getDataTransferById(id)
      .then((res) => {
        if (createCopyId) {
          // creating copy of existing data product
          // below properties needs to be reset to new ones for the copy
          res.data.dataTransferId = '';
          res.data.id = '';
          res.data.providerInformation.contactInformation.name = userInfo;
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
        if (res.status === 204) {
          return history.push('/NotFound');
        } else {
          const data = deserializeFormData({ item: res.data });
          dispatch(setSelectedDataTransfer(data));
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
        }
        ProgressIndicator.hide();
      })
      .catch((e) => {
        Notification.show(
          e?.response?.data?.errors?.[0]?.message || 'Error while fetching selected data transfer',
          'alert',
        );
        ProgressIndicator.hide();
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
    const { id } = provideDataTransfers.selectedDataTransfer;
    if (isCreatePage && !createCopyId) {
      if (id) {
        let defaultValues = { ...provideDataTransfers.selectedDataTransfer };
        reset(defaultValues); // setting default values
      } else {
        const data = tabs['contact-info'];
        data.name = userInfo;
        reset(data); // setting default values
      }
    }
    if (id) {
      let defaultValues = { ...provideDataTransfers.selectedDataTransfer };
      reset(defaultValues); // setting default values
    }
    if(provideDataTransfers.selectedDataTransfer.publish){
      setIsTouChecked(true)
    }
    //eslint-disable-next-line
  }, [dispatch, provideDataTransfers.selectedDataTransfer, isCreatePage]);

  useEffect(() => {
    validatePublishRequest(provideDataTransfers.selectedDataTransfer);
    //eslint-disable-next-line
  },[provideDataTransfers.selectedDataTransfer])

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
      dispatch(setSelectedDataTransfer({}));
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

  function validatePublishRequest (reqObj) {
    let formValid = true;
    const errorObject = {
      contactInformationTabError: [],
      dataDescriptionClassificationTabError: [],
      personalRelatedDataTabError: [],
      transnationalDataTabError: [],
      deletionRequirementsTabError: [],
      saveTabError:[]
    }


    if(!savedTabs?.includes('contact-info')){
      errorObject.saveTabError.push('Contact Information');
      formValid = false;
    }

    if(!savedTabs?.includes('classification-confidentiality')){
      errorObject.saveTabError.push('Data Description & Classification');
      formValid = false;
    }

    if(!savedTabs?.includes('personal-data')){
      errorObject.saveTabError.push('Personal Related Data');
      formValid = false;
    }
    
    if(!savedTabs?.includes('trans-national-data-transfer')){
      errorObject.saveTabError.push('Transnational Data');
      formValid = false;
    }

    if(!savedTabs?.includes('deletion-requirements')){
      errorObject.saveTabError.push('Other Data');
      formValid = false;
    }

    if (!reqObj?.productName || reqObj?.productName === '') {
      errorObject.contactInformationTabError.push('Data Product Name / Short description of the data transfer');
      formValid = false;
    }

    if (!reqObj?.informationOwner || reqObj?.informationOwner === '') {
      errorObject.contactInformationTabError.push('Data responsible IO and/or Business Owner for application');
      formValid = false;
    }

    if (!reqObj?.name?.firstName || reqObj?.name?.firstName === '') {
      errorObject.contactInformationTabError.push('Point of contact for data transfer');
      formValid = false;
    }

    if (!reqObj?.division || reqObj?.division === '0') {
      errorObject.contactInformationTabError.push('Division');
      formValid = false;
    }

    if (!reqObj?.department || reqObj?.department === '') {
      errorObject.contactInformationTabError.push('Department');
      formValid = false;
    }
    
    if (!reqObj?.complianceOfficer || reqObj?.complianceOfficer === '') {
      errorObject.contactInformationTabError.push('Corresponding Compliance Officer / Responsible (LCO/LCR)');
      formValid = false;
    }

    if (!reqObj?.classificationOfTransferedData || reqObj?.classificationOfTransferedData === '') {
      errorObject.dataDescriptionClassificationTabError.push('Description of transfered data');
      formValid = false;
    }

    if (!reqObj?.confidentiality || reqObj?.confidentiality === '') {
      errorObject.dataDescriptionClassificationTabError.push('Confidentiality');
      formValid = false;
    }

    if (!reqObj?.personalRelatedData || reqObj?.personalRelatedData === '') {
      errorObject.personalRelatedDataTabError.push('Is data personal related');
      formValid = false;
    }

    if (reqObj?.personalRelatedData === 'Yes') {
      if (!reqObj?.personalRelatedDataDescription || reqObj?.personalRelatedDataDescription === '') {
        errorObject.personalRelatedDataTabError.push('Description of personal related data');
        formValid = false;
      }

      if (!reqObj?.personalRelatedDataPurpose || reqObj?.personalRelatedDataPurpose === '') {
        errorObject.personalRelatedDataTabError.push('Original (business) purpose of processing this personal related data');
        formValid = false;
      }

      if (!reqObj?.personalRelatedDataLegalBasis || reqObj?.personalRelatedDataLegalBasis === '') {
        errorObject.personalRelatedDataTabError.push('Original legal basis for processing this personal related data');
        formValid = false;
      }
    
      if (!reqObj?.personalRelatedDataContactAwareTransfer || reqObj?.personalRelatedDataContactAwareTransfer === '') {
        errorObject.personalRelatedDataTabError.push('Is corresponding Compliance contact aware of this transfer?');
        formValid = false;
      }
    }

    if (reqObj?.personalRelatedDataContactAwareTransfer == 'Yes') {
      if (!reqObj?.personalRelatedDataObjectionsTransfer || reqObj?.personalRelatedDataObjectionsTransfer === '') {
        errorObject.personalRelatedDataTabError.push('Has s/he any objections to this transfer?');
        formValid = false;
      }
    }

    if (reqObj?.personalRelatedDataObjectionsTransfer === 'Yes') {
      if (!reqObj.personalRelatedDataTransferingNonetheless || reqObj.personalRelatedDataTransferingNonetheless === '') {
        errorObject.personalRelatedDataTabError.push('Please state your reasoning for transfering nonetheless');
        formValid = false;
      }
      if (!reqObj.personalRelatedDataTransferingObjections || reqObj.personalRelatedDataTransferingObjections === '') {
        errorObject.personalRelatedDataTabError.push('Please state your objections');
        formValid = false;
      }
    }


    if (!reqObj?.transnationalDataTransfer || reqObj?.transnationalDataTransfer === '') {
      errorObject.transnationalDataTabError.push('Is data being transferred from one country to another?');
      formValid = false;
    }

    if (!reqObj?.dataOriginatedFromChina || reqObj?.dataOriginatedFromChina === '') {
      errorObject.transnationalDataTabError.push('Is data from China included?');
      formValid = false;
    }

    if (!reqObj?.insiderInformation || reqObj?.insiderInformation === '') {
      errorObject.deletionRequirementsTabError.push('Does data product contain (potential) insider information?');
      formValid = false;
    }

    if (!reqObj?.deletionRequirement || reqObj?.deletionRequirement === '') {
      errorObject.deletionRequirementsTabError.push('Are there specific deletion requirements for this data?');
      formValid = false;
    }

    if (reqObj?.deletionRequirement === 'Yes') {
      if (!reqObj?.deletionRequirementDescription || reqObj?.deletionRequirementDescription === '') {
        errorObject.deletionRequirementsTabError.push('Is data from China included?');
        formValid = false;
      }
    }

    if (!reqObj?.tou || reqObj?.tou === false) {
      errorObject.deletionRequirementsTabError.push('Terms and conditions acknowledgement');
      formValid = false;
    }

    setErrorsInPublish(errorObject);
    
    // setTimeout(() => {
    //   // const anyErrorDetected = document.querySelector('.error');
    //   // anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    // }, 1000);
    return formValid;
  };

  function validateContactInformationTab (reqObj) {
    let formValid = true;
    const errorObject = {
      contactInformationTabError: [],
      deletionRequirementsTabError: [],
      ...errorsInPublish 
    }

    if (!reqObj?.productName?.message === '*Missing entry') {
      !errorObject.contactInformationTabError.includes('Data Product Name / Short description of the data transfer') ? 
      errorObject.contactInformationTabError.push('Data Product Name / Short description of the data transfer')
      :'';
      formValid = false;
    }

    if (reqObj?.informationOwner?.message === '*Missing entry') {
      !errorObject.contactInformationTabError.includes('Data responsible IO and/or Business Owner for application')?
      errorObject.contactInformationTabError.push('Data responsible IO and/or Business Owner for application')
      :'';
      formValid = false;
    }

    if (reqObj?.name?.firstName?.message === '*Missing entry') {
      !errorObject.contactInformationTabError.includes('Point of contact for data transfer')?
      errorObject.contactInformationTabError.push('Point of contact for data transfer')
      :'';
      formValid = false;
    }

    if (reqObj?.division?.message === '*Missing entry') {
      !errorObject.contactInformationTabError.includes('Division')?
      errorObject.contactInformationTabError.push('Division')
      :'';
      formValid = false;
    }

    if (reqObj?.department?.message === '*Missing entry') {
      !errorObject.contactInformationTabError.includes('Department')?
      errorObject.contactInformationTabError.push('Department')
      :'';
      formValid = false;
    }
    
    if (reqObj?.complianceOfficer?.message === '*Missing entry') {
      !errorObject.contactInformationTabError.includes('Corresponding Compliance Officer / Responsible (LCO/LCR)')?
      errorObject.contactInformationTabError.push('Corresponding Compliance Officer / Responsible (LCO/LCR)') : '';
      formValid = false;
    }

    if (reqObj?.tou?.message === '*Missing entry') {
      errorObject.deletionRequirementsTabError.push('Terms and conditions acknowledgement');
      formValid = false;
    }

    if(reqObj?.tou === true || provideDataTransfers.publish){
      setIsTouChecked(true)
    }


    setErrorsInPublish(errorObject);

    if(currentTab === 'contact-info'){
      validatePublishRequest(reqObj)
    }

    return formValid;
  };

  const proceedToSave = (currentTab, values, callbackFn) => {
    const saveSegments = mapOpenSegments[currentTab];
    const openSegments = provideDataTransfers.selectedDataTransfer?.openSegments || [];
    values.openSegments = [...openSegments];
    if (
      isCreatePage &&
      !createCopyId &&
      !provideDataTransfers?.selectedDataTransfer?.id &&
      currentTab === 'contact-info'
    ) {
      values.openSegments = ['ContactInformation'];
    } else if (values?.openSegments?.indexOf(saveSegments) === -1) {
      values.openSegments.push(saveSegments);
    }
    const data = {
      values,
      onSave: () => {
        switchTabs(currentTab);
        if (typeof callbackFn === 'function') callbackFn();
      },
      provideDataTransfers,
    };
    if (isCreatePage) {
      const { id } = provideDataTransfers.selectedDataTransfer;
      if (id) {
        data.values['id'] = id;
        data.type = 'provider';
        dispatch(UpdateDataTransfers(data));
      } else dispatch(SetDataTransfers(data));
    } else if (isEditPage) {
      data.type = 'provider';
      data.state = 'edit';
      dispatch(UpdateDataTransfers(data));
    }
    if (history.location.state && history.location.state.copyId) {
      let state = { ...history.location.state };
      delete state.copyId;
      history.replace({ ...history.location, state });
      createCopyId=null;
    }
  }

  const onSave = (currentAction, currentTab, values, callbackFn) => {
    setShowAllTabsError(false);
    if(currentAction === 'publish'){
      // if(currentTab!='contact-info'){
      //   setShowContactInformationTabError(true);
      // } else{  
        if(validatePublishRequest(values)){
          proceedToSave(currentTab, values, callbackFn)
        } else {
          setShowAllTabsError(true);
        }
      // }  
    } else {
      // if(!values.id && values.id!='' && currentTab!='contact-info'){
      //   setShowContactInformationTabError(true);
      // } else{
        if(validateContactInformationTab(values)){
          proceedToSave(currentTab, values, callbackFn)
        } else {
          setShowAllTabsError(true);
        }  
      // }           
    }
  };

  const displayErrorOfAllTabs = (tabTitle, tabErrorsList) => {
    return (
      tabErrorsList?.length > 0 ?
        <li className={classNames('error-message')}>
        {tabTitle}
          <ul>
            {tabErrorsList?.map((item) => {
              return (
                <li className={Styles.errorItem} key={item}>
                  {item}
                </li>
              );
            })}
          </ul>
        </li>             
      : ''
    )
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
          <div className={classNames(Styles.screenLabel)}>Data Providing Side</div>
          <h3 className={classNames(Styles.title, currentTab !== 'contact-info' ? '' : 'hidden')}>
            {provideDataTransfers.selectedDataTransfer?.productName}
          </h3>
          <div id="data-product-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>              
                <ul className="tabs">
                  {/* <li className={savedTabs?.includes('contact-info') ? 'tab valid' : 'tab active'}> */}
                  <li className={savedTabs?.includes('contact-info') &&
                    errorsInPublish?.contactInformationTabError?.length < 1 ? 'tab valid active' : 'tab active'}>  
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
                  {/* <li className={savedTabs?.includes('classification-confidentiality') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={savedTabs?.includes('classification-confidentiality') && 
                    errorsInPublish?.dataDescriptionClassificationTabError?.length < 1 ? 'tab valid':'tab'}>
                    <a
                      href="#tab-content-2"
                      id="classification-confidentiality"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[1] = ref;
                      }}
                      onClick={setTab}
                    >
                      Data Description &amp; Classification
                    </a>
                  </li>
                  {/* <li className={savedTabs?.includes('personal-data') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={savedTabs?.includes('personal-data') && 
                  errorsInPublish?.personalRelatedDataTabError?.length < 1 ? 'tab valid' :'tab'}>
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
                  {/* <li className={savedTabs?.includes('trans-national-data-transfer') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={savedTabs?.includes('trans-national-data-transfer') && 
                  errorsInPublish?.transnationalDataTabError?.length < 1 ? 'tab valid':'tab'}>
                    <a
                      href="#tab-content-4"
                      id="trans-national-data-transfer"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[3] = ref;
                      }}
                      onClick={setTab}
                    >
                      Transnational Data
                    </a>
                  </li>
                  {/* <li className={savedTabs?.includes('deletion-requirements') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={savedTabs?.includes('deletion-requirements') && 
                  errorsInPublish?.deletionRequirementsTabError?.length < 1
                    ?  'tab valid'                    
                    : errorsInPublish?.deletionRequirementsTabError?.includes('Terms and conditions acknowledgement') && !isTouChecked
                        ?'tab': 'tab valid'
                    }>
                    <a
                      href="#tab-content-5"
                      id="deletion-requirements"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[4] = ref;
                      }}
                      onClick={setTab}
                    >
                      Other Data
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
            { showAllTabsError && 
              (
              errorsInPublish?.contactInformationTabError?.length > 0 ||
              errorsInPublish?.dataDescriptionClassificationTabError?.length > 0 ||
              errorsInPublish?.personalRelatedDataTabError?.length > 0 ||
              errorsInPublish?.transnationalDataTabError?.length > 0 ||
              errorsInPublish?.deletionRequirementsTabError?.length > 0)
              ?
              (
                <div>
                  <h3 className={classNames('error-message')}>
                    Please fill mandatory fields before publish
                  </h3>
                  <ul>
                  {displayErrorOfAllTabs('Contact Information Tab', errorsInPublish?.contactInformationTabError)}
                  {displayErrorOfAllTabs('Data Description & Classification Tab', errorsInPublish?.dataDescriptionClassificationTabError)}
                  {displayErrorOfAllTabs('Personal Related Data Tab', errorsInPublish?.personalRelatedDataTabError)}
                  {displayErrorOfAllTabs('Transnational Data Tab', errorsInPublish?.transnationalDataTabError)}
                  {displayErrorOfAllTabs('Other Data Tab', errorsInPublish?.deletionRequirementsTabError)}  
                  </ul>
                </div>
              ) : ''}

              { showAllTabsError &&
                errorsInPublish?.saveTabError?.length > 0 ? 
              (
                <div>
                  <h3 className={classNames('error-message')}>
                    Please save following tabs details before publish
                  </h3>
                  {errorsInPublish?.saveTabError?.length > 0 ?
                    <li className={classNames('error-message')}>
                      <ul>
                        {errorsInPublish?.saveTabError?.map((item) => {
                          return (
                            <li className={Styles.errorItem} key={item}>
                              {item}
                            </li>
                          );
                        })}
                      </ul>
                    </li>             
                  : ''}
                </div>
              ) : ''}

              { showContactInformationTabError &&
                errorsInPublish?.contactInformationTabError?.length > 0 ? 
              (
                <div>
                  <h3 className={classNames('error-message')}>
                    Please fill and save Contact Information Tab first
                  </h3>
                  <ul>
                  {displayErrorOfAllTabs('Contact Information Tab', errorsInPublish?.contactInformationTabError)}
                  </ul>
                </div>
              ) : ''} 
            <OtherRelevant onSave={(values) => { 
            // setActionButtonName('save');
            onSave('save',currentTab, values);
            }} 
            onDescriptionTabErrors={(errorObj) => {
                setShowContactInformationTabError(false);
                (!validateContactInformationTab(errorObj) && (currentTab != 'contact-info')) ? 
                  setShowContactInformationTabError(true) 
                :  
                  setShowContactInformationTabError(false)
                
              }
            }
            onPublish={(values, callbackFn) => {
              // setActionButtonName('publish');
              setShowContactInformationTabError(false);
              onSave('publish',currentTab, values, callbackFn);
              }} 
            user={userInfo} isDataProduct={false} currentTab={currentTab} />
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
              const id = createCopyId || dataTransferId || provideDataTransfers?.selectedDataTransfer?.id;
              if(id){
                getDataProductById();
              }else{
                const data = tabs[currentTab];
                reset(data);
              }  
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
