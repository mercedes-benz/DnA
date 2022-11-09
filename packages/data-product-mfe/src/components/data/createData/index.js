import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './createData.style.scss';

import { useForm, FormProvider } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';

import Tabs from '../../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';

import { hostServer } from '../../../server/api';

import { setSelectedData, setDivisionList } from '../redux/dataSlice';
import { SetData, UpdateData } from '../redux/data.services';
import { mapOpenSegments } from '../../../Utility/formData';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

// Form Components
import Form1 from './Tabs/form1';
import Form2 from './Tabs/form2';

const tabs = {
  'form-1': {
    productName: '',
    dateOfDataTransfer: '',
    name: '',
    division: '0',
    subDivision: '0',
    department: '',
    complianceOfficer: '',
    planningIT: '',
  },
  'form-2': { classificationOfTransferedData: '', confidentiality: 'Public' },
};

const CreateData = ({ user, history }) => {
  const isCreatePage = history.location.pathname === '/createData';
  const isEditPage = /^\/editData/.test(history?.location?.pathname);

  const provideDataProducts = useSelector((state) => state.provideDataProducts);

  const [currentTab, setCurrentTab] = useState('form-1');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { formState, reset } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));
  const dispatch = useDispatch();

  const createCopyId = history.location?.state?.copyId;

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
    dispatch(setSelectedData({}));
    // reset(data);
    let segments = [];
    setSavedTabs(segments);
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
        const data = tabs['form-1'];
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
      dispatch(setSelectedData({}));
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
    const openSegments = provideDataProducts.selectedDataProduct?.openSegments || [];
    values.openSegments = [...openSegments];
    if (isCreatePage && !createCopyId && !provideDataProducts?.selectedDataProduct?.id && currentTab === 'form-1') {
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
      provideDataProducts,
    };
    if (isCreatePage) {
      const { id } = provideDataProducts.selectedDataProduct;
      if (id) {
        data.values['id'] = id;
        data.type = 'provider';
        dispatch(UpdateData(data));
      } else dispatch(SetData(data));
    } else if (isEditPage) {
      data.type = 'provider';
      data.state = 'edit';
      dispatch(UpdateData(data));
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
          <h3 className={classNames(Styles.title)}>Create Data Product</h3>
          <div id="data-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={savedTabs?.includes('form-1') ? 'tab valid' : 'tab active'}>
                    <a
                      href="#tab-content-1"
                      id="form-1"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[0] = ref;
                      }}
                      onClick={setTab}
                    >
                      Form 1 (TBD)
                    </a>
                  </li>
                  <li className={savedTabs?.includes('form-2') ? 'tab valid' : 'tab disabled'}>
                    <a
                      href="#tab-content-2"
                      id="form-2"
                      ref={(ref) => {
                        if (elementRef.current) elementRef.current[1] = ref;
                      }}
                      onClick={setTab}
                    >
                      Form 2 (TBD)
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                <Form1
                  onSave={(values) => onSave('form-1', values)}
                  divisions={divisions}
                  setSubDivisions={setSubDivisions}
                  subDivisions={subDivisions}
                />
              </div>
              <div id="tab-content-2" className="tab-content">
                {currentTab === 'form-2' && <Form2 onSave={(values) => onSave('form-2', values)} />}
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
    </>
  );
};
export default CreateData;
