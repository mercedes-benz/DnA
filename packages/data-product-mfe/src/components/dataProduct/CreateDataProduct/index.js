import React, { createRef, useRef, useState, useEffect } from 'react';
import classNames from 'classnames';
import Styles from './Form.style.scss';
import { tabs } from '../../dataTransfer/ProviderForm';

// import Tabs from '../../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../common/modules/uilab/js/src/notification';

import { useForm, FormProvider } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

import { hostServer } from '../../../server/api';
import { dataProductApi } from '../../../apis/dataproducts.api';

import { deserializeFormData, mapOpenSegments } from '../../../Utility/formData';
import OtherRelevant from '../../dataTransfer/ProviderForm/OtherRelavantInfo';

// Form Components
import Description from './Description';
import ContactInformation from '../../dataTransfer/ProviderForm/ContactInformation';
import Classification from '../../dataTransfer/ProviderForm/ClassificationAndConfidentiality';
import PersonalRelatedData from '../../dataTransfer/ProviderForm/PersonalRelatedData';
import TransNationalDataTransfer from '../../dataTransfer/ProviderForm/TransNationalDataTransfer';
import DeletionRequirements from '../../dataTransfer/ProviderForm/DeletionRequirements';
import { setDivisionList, setSelectedDataProduct } from '../redux/dataProductSlice';
import { UpdateDataProduct, SetDataProduct } from '../redux/dataProduct.services';
import {
  getAgileReleaseTrains,
  getCarlaFunctions,
  getCorporateDataCatalogs,
  getFrontEndTools,
  getPlatforms,
  getTags
} from '../../redux/getDropdowns.services';

const dataForms = {
  description: {
    dataProductName: '',
    carLAFunction: '',
    ART: '',
    corporateDataCatalog: '',
    description: '',
  },
  ...tabs,
};

const CreateDataProduct = ({ user, history }) => {
  const isCreatePage = history.location.pathname === '/dataproduct/create';
  const isEditPage = /^\/dataproduct\/edit/.test(history?.location?.pathname);

  const [currentTab, setCurrentTab] = useState('description');
  const [savedTabs, setSavedTabs] = useState([]);

  const data = useSelector((state) => state.dataProduct);

  const elementRef = useRef(Object.keys(dataForms)?.map(() => createRef()));
  const methods = useForm();
  const { formState, reset } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });
  
  const [errorsInPublish, setErrorsInPublish] = useState({
    descriptionTabError: [],
    contactInformationTabError: [],
    dataDescriptionClassificationTabError: [],
    personalRelatedDataTabError: [],
    transnationalDataTabError: [],
    deletionRequirementsTabError: [],
    saveTabError:[]
  });

  const [isTouChecked, setIsTouChecked] = useState(false);
  const [showAllTabsError, setShowAllTabsError] = useState(false);
  const [showDescriptionTabError, setShowDescriptionTabError] = useState(false);
  // const [actionButtonName, setActionButtonName] = useState('');

  const dispatch = useDispatch();
  const { agileReleaseTrains, carLAFunctions, corporateDataCatalogs, platforms, frontEndTools, tags } = useSelector(
    (state) =>state.dropdowns,
  );

  const { id: dataProductId } = useParams();
  let createCopyId = history.location?.state?.copyId;

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
    const id = createCopyId || dataProductId || data?.selectedDataProduct?.id;
    ProgressIndicator.show();
    dataProductApi
      .getDataProductById(id)
      .then((res) => {
        if (createCopyId) {
          // creating copy of existing data product
          // below properties needs to be reset to new ones for the copy
          res.data.dataProductId = '';
          res.data.id = '';
          res.data.notifyUsers = false;
          res.data.publish = false;
          if (res.data.contactInformation?.name) res.data.contactInformation.name = userInfo;
          else res.data['contactInformation'] = { name: userInfo };
          delete res.data.createdBy;
          delete res.data.createdDate;
          delete res.data.lastModifiedDate;
          delete res.data.modifiedBy;
        }

        if (res.status === 204) {
          return history.push('/NotFound');
        } else {
          if (isEditPage) {
            if (!res.data?.contactInformation) {
              // set logged in user name as default value
              res.data['contactInformation'] = { name: userInfo };
            }
          }
          const data = deserializeFormData({ item: res.data, isDataProduct: true });
          dispatch(setSelectedDataProduct(data));
          reset(data);
          let segments = [];
          res.data?.openSegments?.map((seg) => {
            for (let key in mapOpenSegments) {
              if (mapOpenSegments[key] === seg) {
                segments.push(key);
              }
            }
          });
          setSavedTabs(segments);
        }
        ProgressIndicator.hide();
        // SelectBox?.defaultSetup();
      })
      .catch((e) => {
        console.log(e);
        Notification.show(
          e?.response?.data?.errors?.[0]?.message || 'Error while fetching selected data product',
          'alert',
        );
        ProgressIndicator.hide();
      });
  };

  // useEffect(() => {
  //   if (user?.roles?.length) {
  //     Tabs.defaultSetup();
  //   } else {
  //     setTimeout(() => {
  //       Tabs.defaultSetup();
  //     }, 100);
  //   }
  // }, [user]);

  useEffect(() => {
    const { id } = data.selectedDataProduct;
    if (isCreatePage && !createCopyId) {
      if (id) {
        let defaultValues = { ...data.selectedDataProduct };
        reset(defaultValues); // setting default values
      } else {
        const data = dataForms['description'];
        dataForms['contact-info'].name = userInfo;
        reset({ ...data, ...dataForms['contact-info'] }); // setting default values
      }
    }
    if (id) {
      let defaultValues = { ...data.selectedDataProduct };
      reset(defaultValues); // setting default values
    }

    if(data.selectedDataProduct.isPublish){
      setIsTouChecked(true)
    }

    //eslint-disable-next-line
  }, [dispatch, data.selectedDataProduct, isCreatePage]);

  useEffect(() => {
    validatePublishRequest(data.selectedDataProduct);
    //eslint-disable-next-line
  },[data.selectedDataProduct])

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
      dispatch(setSelectedDataProduct({}));     
    };
    //eslint-disable-next-line
  }, [dispatch]);

  useEffect(() => {
    dispatch(getAgileReleaseTrains());
    dispatch(getCarlaFunctions());
    dispatch(getCorporateDataCatalogs());
    dispatch(getPlatforms());
    dispatch(getFrontEndTools());
    dispatch(getTags());
  }, [dispatch]);

  useEffect(() => {
    if (isCreatePage) {
      SelectBox.defaultSetup();
    }
  }, [isCreatePage, agileReleaseTrains, carLAFunctions, corporateDataCatalogs, platforms, frontEndTools, tags]);

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      // const isFieldsDirty = formState.isDirty || Object.keys(formState.dirtyFields).length > 0;
      const isFieldsDirty = formState.isDirty;
      if (isFieldsDirty) {
        setShowChangeAlert({ modal: true, switchingTab: id });
      } else {
        setCurrentTab(id);
      }
    }
  };

  const switchTabs = (currentTab) => {
    const tabIndex = Object.keys(dataForms).indexOf(currentTab) + 1;
    setSavedTabs([...new Set([...savedTabs, currentTab])]);
    if (currentTab !== 'deletion-requirements') {
      setCurrentTab(Object.keys(dataForms)[tabIndex]);
      elementRef.current[tabIndex].click();
    }
  };

  function validatePublishRequest (reqObj) {
    let formValid = true;
    const errorObject = {
      descriptionTabError: [],
      contactInformationTabError: [],
      dataDescriptionClassificationTabError: [],
      personalRelatedDataTabError: [],
      transnationalDataTabError: [],
      deletionRequirementsTabError: [],
      saveTabError:[]
    }

    if(!savedTabs?.includes('description')){
      errorObject.saveTabError.push('Description');
      formValid = false;
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

    if (!reqObj?.description || reqObj?.description === '') {
      errorObject.descriptionTabError.push('Description');
      formValid = false;
    }

    if (!reqObj?.productName || reqObj?.productName === '') {
      errorObject.descriptionTabError.push('Name of Data Product');
      formValid = false;
    }

    // if (!reqObj?.howToAccessText || reqObj?.howToAccessText === '') {
    //   errorObject.descriptionTabError.push('How to access');
    //   formValid = false;
    // }

    if (!reqObj?.informationOwner || reqObj?.informationOwner === '') {
      errorObject.contactInformationTabError.push('Information Owner');
      formValid = false;
    }

    if (!reqObj?.name?.firstName || reqObj?.name?.firstName === '') {
      errorObject.contactInformationTabError.push('Your Name');
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

    if (reqObj?.personalRelatedDataContactAwareTransfer == 'Yes' ) {
      if (!reqObj?.personalRelatedDataObjectionsTransfer || reqObj?.personalRelatedDataObjectionsTransfer === '') {
        errorObject.personalRelatedDataTabError.push('Has s/he any objections to this transfer?');
        formValid = false;
      }
    }

    if(reqObj?.personalRelatedDataObjectionsTransfer === 'Yes') {
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

    if (!reqObj?.insiderInformation || reqObj?.insiderInformation === '') {
      errorObject.deletionRequirementsTabError.push('Does data product contain (potential) insider information?');
      formValid = false;
    }

    if (!reqObj?.deletionRequirement || reqObj?.deletionRequirement === '') {
      errorObject.deletionRequirementsTabError.push('Are there specific deletion requirements for this data?');
      formValid = false;
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
  
  function validateDescriptionTab (reqObj) {
    let formValid = true;
    const errorObject = {
      descriptionTabError: [],
      deletionRequirementsTabError: [],     
      ...errorsInPublish
    }
    if (reqObj?.description?.message === '*Missing entry') {
      !errorObject.descriptionTabError.includes('Description')?
      errorObject.descriptionTabError.push('Description')
      :'';
      formValid = false;
    }

    if (reqObj?.productName?.message === '*Missing entry') {
      !errorObject.descriptionTabError.includes('Name of Data Product')?
      errorObject.descriptionTabError.push('Name of Data Product')
      :'';
      formValid = false;
    }

    // if (reqObj?.howToAccessText?.message === '*Missing entry') {
    //   !errorObject.descriptionTabError.includes('How to access')?
    //   errorObject.descriptionTabError.push('How to access')
    //   :'';
    //   formValid = false;
    // }

    if (reqObj?.tou?.message === '*Missing entry') {
      errorObject.deletionRequirementsTabError.push('Terms and conditions acknowledgement');
      formValid = false;
    }

    if(reqObj?.tou === true || data.isPublish){
      setIsTouChecked(true)
    }

    setErrorsInPublish(errorObject);

    if(currentTab === 'description'){
      validatePublishRequest(reqObj)
    }

    return formValid;
  };

  const proceedToSave = (currentTab, values, callbackFn) => {
    const saveSegments = mapOpenSegments[currentTab];
      const openSegments = data.selectedDataProduct?.openSegments || [];
      values.openSegments = [...openSegments];

      if (isCreatePage && !createCopyId && !data?.selectedDataProduct?.id && currentTab === 'description') {
        values.openSegments = ['Description'];
      } else if (values?.openSegments?.indexOf(saveSegments) === -1) {
        values.openSegments.push(saveSegments);
      }

      const dataObj = {
        values,
        onSave: () => {
          switchTabs(currentTab);
          if (typeof callbackFn === 'function') callbackFn();
        },
        data,
      };
      if (isCreatePage) {
        const { id } = data.selectedDataProduct;

        if (id) {
          dataObj.values['id'] = id;
          dataObj.type = 'provider';
          dispatch(UpdateDataProduct(dataObj));
        } else dispatch(SetDataProduct(dataObj));
      } else if (isEditPage) {
        dataObj.type = 'provider';
        dataObj.state = 'edit';
        dispatch(UpdateDataProduct(dataObj));
      }
      if (history.location.state && history.location.state.copyId) {
        let state = { ...history.location.state };
        delete state.copyId;
        history.replace({ ...history.location, state });
        createCopyId=null;
      }
  }

  const onSave = (currentAction, currentTab, values, callbackFn) => {

    const howToAccessObj = {
      "accessDetailsCollectionVO": [
        {
          "accessType": "access-via-kafka",
          "stepCollectionVO": values['kafkaArray']
        },
        {
          "accessType": "live-access",
          "stepCollectionVO": values['liveAccessArray']
        },
        {
          "accessType": "api-access",
          "stepCollectionVO": values['apiArray']
        }
      ],
      "useTemplate": values['useTemplate']
    };

    values['howToAccessTemplate'] = howToAccessObj

    setShowAllTabsError(false);
    if(currentAction === 'publish'){
      // if(!values.id && values.id!='' && currentTab!='description'){
      //   setShowDescriptionTabError(true);
      // } else{
        if(validatePublishRequest(values)){
          proceedToSave(currentTab, values, callbackFn)
        } else {
          setShowAllTabsError(true);
        }
      // }
    } else { 
      // if(!values.id && values.id!='' && currentTab!='description'){
      //   setShowDescriptionTabError(true);
      // } else{
        if(validateDescriptionTab(values)){
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
          <div className={classNames(Styles.screenLabel)}>
            {isEditPage ? 'Edit Data Product' : 'Add a new Data Product'}
          </div>
          <h3 className={classNames(Styles.title, currentTab !== 'description' ? '' : 'hidden')}>
            {data?.selectedDataProduct?.productName}
          </h3>
          <div id="data-product-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  {/* <li className={savedTabs?.includes('description') ? 'tab valid' : 'tab valid active'}> */}
                  <li id='firstElementTab' className={
                    (savedTabs?.includes('description') && 
                  errorsInPublish?.descriptionTabError?.length < 1 ? 'tab valid' : 'tab')+' active'}>
                    <a
                      href="#tab-content-1"
                      id="description"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[0] = ref;
                      }}
                      onClick={setTab}
                    >
                      Description
                    </a>
                  </li>
                  {/* <li className={savedTabs?.includes('contact-info') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={savedTabs?.includes('contact-info') && 
                  errorsInPublish?.contactInformationTabError?.length < 1 ? 'tab valid' :'tab'}>
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
                  {/* <li className={savedTabs?.includes('classification-confidentiality') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={ savedTabs?.includes('classification-confidentiality') && 
                  errorsInPublish?.dataDescriptionClassificationTabError?.length < 1 ? 'tab valid' :'tab'}>
                    <a
                      href="#tab-content-3"
                      id="classification-confidentiality"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[2] = ref;
                      }}
                      onClick={setTab}
                    >
                      Data Description & Classification
                    </a>
                  </li>
                  {/* <li className={savedTabs?.includes('personal-data') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={savedTabs?.includes('personal-data') && 
                  errorsInPublish?.personalRelatedDataTabError?.length < 1 ? 'tab valid' :'tab'}>
                    <a
                      href="#tab-content-4"
                      id="personal-data"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[3] = ref;
                      }}
                      onClick={setTab}
                    >
                      Personal Related Data
                    </a>
                  </li>
                  {/* <li className={savedTabs?.includes('trans-national-data-transfer') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={savedTabs?.includes('trans-national-data-transfer') && 
                  errorsInPublish?.transnationalDataTabError?.length < 1 ? 'tab valid' :'tab'}>
                    <a
                      href="#tab-content-5"
                      id="trans-national-data-transfer"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[4] = ref;
                      }}
                      onClick={setTab}
                    >
                      Transnational Data
                    </a>
                  </li>
                  {/* <li className={savedTabs?.includes('deletion-requirements') ? 'tab valid' : 'tab disabled'}> */}
                  <li className={
                    savedTabs?.includes('deletion-requirements') &&
                    errorsInPublish?.deletionRequirementsTabError?.length < 1
                    ?  'tab valid'
                    : errorsInPublish?.deletionRequirementsTabError?.includes('Terms and conditions acknowledgement') && !isTouChecked
                        ?'tab': 'tab valid'
                    }>
                    <a
                      href="#tab-content-6"
                      id="deletion-requirements"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[5] = ref;
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
                <Description
                  onSave={(values) => {
                    onSave('description', values)}}
                  artList={agileReleaseTrains}
                  carlaFunctionList={carLAFunctions}
                  dataCatalogList={corporateDataCatalogs}
                  userInfo={userInfo}
                  platformList={platforms}
                  frontEndToolList={frontEndTools}
                  tagsList={tags}
                  isDataProduct={true}
                />
              </div>
              <div id="tab-content-2" className="tab-content">
                {currentTab === 'contact-info' && (
                  <ContactInformation
                    onSave={(values) => onSave('contact-info', values)}
                    divisions={divisions}
                    setSubDivisions={setSubDivisions}
                    subDivisions={subDivisions}
                    isDataProduct={true}
                    userInfo={userInfo}
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
                {currentTab === 'deletion-requirements' && (
                  <DeletionRequirements
                    onSave={(values, callbackFn) => onSave('deletion-requirements', values, callbackFn)}
                    user={user}
                    isDataProduct={true}
                  />
                )}
              </div>
            </div>
            { showAllTabsError && 
              (errorsInPublish?.descriptionTabError?.length > 0 ||
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
                  {displayErrorOfAllTabs('Description Tab', errorsInPublish?.descriptionTabError)}
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
                    Please save following tabs details before publishing
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

              { showDescriptionTabError &&
                errorsInPublish?.descriptionTabError?.length > 0 ? 
              (
                <div>
                  <h3 className={classNames('error-message')}>
                    Please fill and save Description Tab first
                  </h3>
                  <ul>
                  {displayErrorOfAllTabs('Description Tab', errorsInPublish?.descriptionTabError)}
                  </ul>
                </div>
              ) : ''} 
            <OtherRelevant onSave={(values) => {
              // setActionButtonName('save');
              onSave('save',currentTab, values)}} 
            onDescriptionTabErrors={(errorObj) => {
              setShowDescriptionTabError(false);
              // validateDescriptionTab(errorObj) ? 
              // setShowDescriptionTabError(false) : setShowDescriptionTabError(true)
              (!validateDescriptionTab(errorObj) && (currentTab != 'description')) ? 
                setShowDescriptionTabError(true) 
                :  
                setShowDescriptionTabError(false)
              }
              
              }
            onPublish={(values, callbackFn) => {
              // setActionButtonName('publish');
              setShowDescriptionTabError(false);
              onSave('publish',currentTab, values, callbackFn)}} 
            user={userInfo} isDataProduct={true} currentTab={currentTab}/>
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
              const id = createCopyId || dataProductId || data?.selectedDataProduct?.id;
              if(id){
                getDataProductById();
              }else{
                const data = tabs[currentTab];
                reset(data);
              }                
              setCurrentTab(showChangeAlert.switchingTab);
              elementRef.current[Object.keys(dataForms).indexOf(showChangeAlert.switchingTab)].click();
              setShowChangeAlert({ modal: false, switchingTab: '' });
            }}
            onAccept={() => {
              setShowChangeAlert({ modal: false, switchingTab: '' });
              elementRef.current[Object.keys(dataForms).indexOf(currentTab)].click();
            }}
          />
        </div>
        <div className={Styles.mandatoryInfo}>* mandatory fields</div>
      </FormProvider>
    </>
  );
};

export default CreateDataProduct;
