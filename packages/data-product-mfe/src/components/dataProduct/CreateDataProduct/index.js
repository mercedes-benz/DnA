import React, { createRef, useRef, useState, useEffect } from 'react';
import classNames from 'classnames';
import Styles from './Form.style.scss';
import { tabs } from '../../dataTransfer/ProviderForm';

import Tabs from '../../../common/modules/uilab/js/src/tabs';
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

// Form Components
import Description from './Description';
import ContactInformation from '../../dataTransfer/ProviderForm/ContactInformation';
import Classification from '../../dataTransfer/ProviderForm/ClassificationAndConfidentiality';
import PersonalRelatedData from '../../dataTransfer/ProviderForm/PersonalRelatedData';
import TransNationalDataTransfer from '../../dataTransfer/ProviderForm/TransNationalDataTransfer';
import DeletionRequirements from '../../dataTransfer/ProviderForm/DeletionRequirements';
import { setDivisionList, setSelectedData } from '../redux/dataProductSlice';
import { UpdateDataProduct, SetDataProduct } from '../redux/dataProduct.services';
import { getAgileReleaseTrains, getCarlaFunctions, getCorporateDataCatalogs } from '../../redux/getDropdowns.services';

const dataForms = {
  description: {
    dataProductName: '',
    carLAFunction: '',
    ART: '',
    corporateDataCatalog: '',
    description: '',
    dateOfDataProduct: '',
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

  const dispatch = useDispatch();
  const { agileReleaseTrains, carLAFunctions, corporateDataCatalogs } = useSelector((state) => state.dropdowns);

  const { id: dataProductId } = useParams();
  const createCopyId = history.location?.state?.copyId;

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
    const id = createCopyId || dataProductId || data?.selectedData?.id;
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
          res.data.contactInformation.name = userInfo;
          delete res.data.createdBy;
          delete res.data.createdDate;
          delete res.data.lastModifiedDate;
          delete res.data.modifiedBy;
        }

        if (res.status === 204) {
          return history.push('/NotFound');
        } else {
          const data = deserializeFormData({ item: res.data, isDataProduct: true });
          dispatch(setSelectedData(data));
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
      })
      .catch((e) => {
        Notification.show(
          e?.response?.data?.errors?.[0]?.message || 'Error while fetching selected data product',
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
    const { id } = data.selectedData;
    if (isCreatePage && !createCopyId) {
      if (id) {
        let defaultValues = { ...data.selectedData };
        reset(defaultValues); // setting default values
      } else {
        const data = dataForms['description'];
        dataForms['contact-info'].name = userInfo;
        reset({ ...data, ...dataForms['contact-info'] }); // setting default values
      }
    }

    //eslint-disable-next-line
  }, [dispatch, data.selectedData, isCreatePage]);

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
      dispatch(setSelectedData({}));
    };
  }, [dispatch]);

  useEffect(() => {
    dispatch(getAgileReleaseTrains());
    dispatch(getCarlaFunctions());
    dispatch(getCorporateDataCatalogs());
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
    const tabIndex = Object.keys(dataForms).indexOf(currentTab) + 1;
    setSavedTabs([...new Set([...savedTabs, currentTab])]);
    if (currentTab !== 'deletion-requirements') {
      setCurrentTab(Object.keys(dataForms)[tabIndex]);
      elementRef.current[tabIndex].click();
    }
  };

  const onSave = (currentTab, values, callbackFn) => {
    const saveSegments = mapOpenSegments[currentTab];
    const openSegments = data.selectedData?.openSegments || [];
    values.openSegments = [...openSegments];

    if (isCreatePage && !createCopyId && !data?.selectedData?.id && currentTab === 'description') {
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
      const { id } = data.selectedData;

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
          <h3 className={classNames(Styles.title)}>Add a new Data Product</h3>
          <div id="data-product-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={savedTabs?.includes('description') ? 'tab valid' : 'tab active'}>
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
                  <li className={savedTabs?.includes('contact-info') ? 'tab valid' : 'tab disabled'}>
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
                      Data Description & Classification
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
                      Personal Related Data
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
                      Trans-national Data
                    </a>
                  </li>
                  <li className={savedTabs?.includes('deletion-requirements') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-6"
                      id="deletion-requirements"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[5] = ref;
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
                <Description
                  onSave={(values) => onSave('description', values)}
                  artList={agileReleaseTrains}
                  carlaFunctionList={carLAFunctions}
                  dataCatalogList={corporateDataCatalogs}
                  userInfo={userInfo}
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
