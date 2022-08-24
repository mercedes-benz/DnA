import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import Styles from './style.scss';

import { hostServer } from '../../server/api';
import { useForm, FormProvider } from 'react-hook-form';

import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

import ContactInformation from './ContactInformation';
import PersonalRelatedData from './PersonalRelatedData';

const tabs = {
  'contact-info': {
    businessOwner: '',
    division: '0',
    subDivision: '0',
    department: '',
    LCONeededToBeInvolved: '',
    LCOOfficer: '',
    planningIT: '',
  },
  'personal-data': {
    personalRelatedDataTransferred: '',
    personalRelatedDataPurpose: '',
    personalRelatedDataLegalBasis: '',
    LCOCheckedLegalBasis: '',
    LCOComments: '',
  },
};

const ConsumerForm = ({ user }) => {
  const [currentTab, setCurrentTab] = useState('contact-info');
  const [savedTabs, setSavedTabs] = useState([]);
  const methods = useForm();
  const { setValue, formState, reset, watch } = methods;

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [showChangeAlert, setShowChangeAlert] = useState({ modal: false, switchingTab: '' });

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

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
      SelectBox.defaultSetup();
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
        <h3 className={classNames(Styles.title)}>Consume / Share Data Product</h3>
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
                <li className={savedTabs?.includes('personal-data') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-2"
                    id="personal-data"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[1] = ref;
                    }}
                    onClick={setTab}
                  >
                    Identifying personal related data
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            <div id="tab-content-1" className="tab-content">
              {currentTab === 'contact-info' && (
                <ContactInformation
                  onSave={() => onSave('contact-info')}
                  divisions={divisions}
                  setSubDivisions={setSubDivisions}
                  subDivisions={subDivisions}
                />
              )}
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'personal-data' && <PersonalRelatedData onSave={() => onSave('personal-data')} />}
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
  );
};

export default ConsumerForm;
