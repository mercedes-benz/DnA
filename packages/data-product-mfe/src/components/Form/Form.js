import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './Form.style.scss';

import { useForm, FormProvider } from 'react-hook-form';
import { hostServer } from '../../server/api';

import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

// import SelectBox from 'dna-container/SelectBox';
import ConfirmModal from 'dna-container/ConfirmModal';

// Form Components
import BasicInformation from './BasicInformation';
import ContactInformation from './ContactInformation';
import Classification from './ClassificationAndConfidentiality';
import PersonalRelatedData from './PersonalRelatedData';
import TransNationalDataTransfer from './transNationalDataTransfer';
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
    complainceOfficer: '',
    planningIT: '',
  },
  'classification-confidentiality': { classificationOfTransferedData: '', confidentiality: '' },
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

const Form = ({ user }) => {
  const [currentTab, setCurrentTab] = useState('basic-info');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { setValue, formState, reset, watch } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs).map(() => createRef()));

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
    Object.entries(defaultValues).map(([key, value]) => {
      setValue(key, value); // setting default values
    });
  }, [setValue]);

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      setDivisions(res.data);
      ProgressIndicator.hide();
    });
  }, []);

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      if (formState.isDirty) {
        setShowChangeAlert({ modal: true, switchingTab: id });
      } else {
        setCurrentTab(id);
      }
    }
  };

  const onSave = (currentTab) => {
    const tabIndex = Object.keys(tabs).indexOf(currentTab) + 1;
    setSavedTabs([...new Set([...savedTabs, currentTab])]);
    setCurrentTab(Object.keys(tabs)[tabIndex]);
    elementRef.current[tabIndex].click();
  };

  return (
    <FormProvider {...methods}>
      <div className={classNames(Styles.mainPanel)}>
        <h3 className={classNames(Styles.title)}>Provide Data Product</h3>
        <div id="data-product-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={savedTabs?.includes('basic-info') ? 'tab valid' : 'tab active'}>
                  <a href="#tab-content-1" id="basic-info" ref={elementRef} onClick={setTab}>
                    Basic Information
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
            <div id="tab-content-1" className="tab-content">
              <BasicInformation onSave={() => onSave('basic-info')} />
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'contact-info' && (
                <ContactInformation
                  onSave={() => onSave('contact-info')}
                  divisions={divisions}
                  setSubDivisions={setSubDivisions}
                  subDivisions={subDivisions}
                />
              )}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'classification-confidentiality' && (
                <Classification onSave={() => onSave('classification-confidentiality')} />
              )}
            </div>
            <div id="tab-content-4" className="tab-content">
              {currentTab === 'personal-data' && <PersonalRelatedData onSave={() => onSave('personal-data')} />}
            </div>
            <div id="tab-content-5" className="tab-content">
              {currentTab === 'trans-national-data-transfer' && (
                <TransNationalDataTransfer onSave={() => onSave('trans-national-data-transfer')} />
              )}
            </div>
            <div id="tab-content-6" className="tab-content">
              {currentTab === 'data-originating-china' && (
                <DataOriginating onSave={() => onSave('data-originating-china')} />
              )}
            </div>
            <div id="tab-content-7" className="tab-content">
              {currentTab === 'deletion-requirements' && (
                <DeletionRequirements onSave={() => onSave('deletion-requirements')} />
              )}
            </div>
            <div id="tab-content-8" className="tab-content">
              {currentTab === 'other-information' && <OtherRelevant onSave={() => onSave('other-information')} />}
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
            Object.entries(defaultValues).map(([key, value]) => {
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
      {currentTab !== 'basic-info' && <div className={Styles.mandatoryInfo}>* mandatory fields</div>}
    </FormProvider>
  );
};
export default Form;
