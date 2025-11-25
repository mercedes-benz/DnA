import React, { useState, useEffect } from 'react';
import classNames from 'classnames';
import Styles from './DdxPush.scss';
import AddUser from 'dna-container/AddUser';
import SelectBox from 'dna-container/SelectBox';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Notification from '../../common/modules/uilab/js/src/notification';
import { fabricApi } from '../../apis/fabric.api';import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { DIVISIONS, BUSINESS_DOMAINS, CLOUD_PROVIDERS, DATA_HUBS,TECHNOLOGIES, PURPOSES, CRITERIA_TRANSFER_PRICING, QUALIFICATION_TRANSFER_PRICING} from '../../utilities/constants';

const Step1_BasicIdentification = ({ formData, setFormData, errors, clearError }) => (
  <div className={Styles.stepForm}>
    <div className={Styles.col}>
      <div className={classNames('input-field-group include-error', errors.nameError ? 'error' : '')}>
        <label htmlFor="dataProductName" className="input-label">
          Name of the Data Product <sup>*</sup>
        </label>
        <input
          type="text"
          id="dataProductName"
          className="input-field"
          autoComplete="off"
          value={formData.dataProductName || ''}
          onChange={(e) => {
            setFormData({ ...formData, dataProductName: e?.target?.value });
            clearError('nameError');
          }}
        />
        {errors.nameError && <span className="error-message">{errors.nameError}</span>}
      </div>
    </div>

    <div className={Styles.col}>
      <div className={classNames('input-field-group include-error area', errors.descriptionError ? 'error' : '')}>
        <label htmlFor="dataProductDescription" className="input-label">
          Description <sup>*</sup>
        </label>
        <textarea
          id="dataProductDescription"
          className="input-field-area"
          rows={6}
          value={formData.dataProductDescription || ''}
          onChange={(e) => {
            setFormData({ ...formData, dataProductDescription: e?.target?.value });
            clearError('descriptionError');
          }}
        />
        {errors.descriptionError && <span className="error-message">{errors.descriptionError}</span>}
      </div>
    </div>
  </div>
);

const Step2_OwnershipGovernance = ({
  formData,
  setFormData,
  errors,
  clearError,
  workspaceOwner,
  workspaceDivision,
}) => {

  useEffect(() => {
    if (workspaceOwner) {
      setFormData((prev) => {
        const alreadyPresent = prev.dataProviders?.some((u) => u.id === workspaceOwner.id);
        if (alreadyPresent) return prev;
        const ownerData = {
          id: workspaceOwner.id,
          firstName: workspaceOwner.firstName,
          lastName: workspaceOwner.lastName,
          department: workspaceOwner.department,
          email: workspaceOwner.email,
          isOwner: true,
        };
        return {
          ...prev,
          dataProviders: [ownerData, ...(prev.dataProviders || [])],
        };
      });
    }
  }, [workspaceOwner, setFormData]);

  const isBusinessDomainDisabled = workspaceDivision && BUSINESS_DOMAINS.includes(workspaceDivision);
  const isDataProvidersLimitReached = formData.dataProviders?.length >= 5;

  useEffect(() => {
    SelectBox.defaultSetup();
    setTimeout(() => {
      if (isBusinessDomainDisabled) {
        const select = document.getElementById('businessDomain');
        if (select) {
          select.setAttribute('disabled', 'true');
          select.style.pointerEvents = 'none';
          select.style.cursor = 'not-allowed';
        }
        const customSelect = select?.closest('.custom-select');
        if (customSelect) {
          const selectSelected = customSelect.querySelector('.select-selected');
          if (selectSelected) {
            selectSelected.style.pointerEvents = 'none';
            selectSelected.style.cursor = 'not-allowed';
            selectSelected.setAttribute('aria-disabled', 'true');
          }
        }
      }
    }, 0);
  }, [isBusinessDomainDisabled]);

  const getDataProviders = (member) => {

    if (isDataProvidersLimitReached) {
      Notification.show('Maximum 4 additional data providers can be added.', 'warning');
      return;
    }

    const memberData = {
      id: member?.shortId || member?.id,
      firstName: member?.firstName,
      lastName: member?.lastName,
      department: member?.department,
      email: member?.email,
      isOwner: false,
    };

    setFormData((prev) => {
      const alreadyExists = prev.dataProviders?.some((u) => u.id === memberData.id);
      if (alreadyExists) {
        Notification.show('Data Provider already added.', 'warning');
        return prev;
      }
      const updatedProviders = [...(prev.dataProviders || []), memberData];
      if (errors.dataProvidersError && updatedProviders.length >= 2) clearError('dataProvidersError');
      return { ...prev, dataProviders: updatedProviders };
    });
  };

  const onDataProviderDelete = (userId) => {
    setFormData((prev) => {
      const updatedList = prev.dataProviders.filter((u) => u.id !== userId);
      return { ...prev, dataProviders: updatedList };
    });
  };

  return (
    <div className={Styles.stepForm}>
      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.informationOwnerError ? 'error' : '')}>
          <label htmlFor="informationOwner" className="input-label">
            Information Owner <sup>*</sup>
          </label>
          <input
            id="informationOwner"
            className="input-field"
            autoComplete="off"
            value={formData.informationOwner || ''}
            onChange={(e) => {
              setFormData({ ...formData, informationOwner: e.target.value });
              clearError('informationOwnerError');
            }}
          />
          {errors.informationOwnerError && <span className="error-message">{errors.informationOwnerError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.divisionError ? 'error' : '')}>
          <label htmlFor="divisionField" className="input-label">
            Division <sup>*</sup>
          </label>
          <div className="custom-select">
            <select
              id="divisionField"
              defaultValue={formData.divisions || ''}
              onChange={(e) => {
                setFormData((prev) => ({ ...prev, divisions: e.target.value }));
                clearError('divisionError');
              }}
            >
              <option value="">Choose</option>
              {DIVISIONS.map((name, index) => (
                <option key={index} value={name}>
                  {name}
                </option>
              ))}
            </select>
          </div>
          {errors.divisionError && <span className="error-message">{errors.divisionError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.businessDomainError ? 'error' : '')}>
          <label htmlFor="businessDomain" className="input-label">
            Business Domain <sup>*</sup>
          </label>
          <div className="custom-select">
            <select
              id="businessDomain"
              value={formData.businessDomain || ''}
              onChange={(e) => {
                setFormData((prev) => ({ ...prev, businessDomain: e?.target?.value }));
                clearError('businessDomainError');
              }}
              onFocus={isBusinessDomainDisabled ? (e) => e.target.blur() : undefined}
              disabled={isBusinessDomainDisabled}
            >
              <option value="">Choose</option>
              {BUSINESS_DOMAINS.map((bd, idx) => (
                <option key={idx} value={bd}>
                  {bd}
                </option>
              ))}
            </select>
          </div>
          {errors.businessDomainError && <span className="error-message">{errors.businessDomainError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.legalEntityError ? 'error' : '')}>
          <label htmlFor="legalEntity" className="input-label">
            Legal Entity <sup>*</sup>
          </label>
          <input
            id="legalEntity"
            className="input-field"
            autoComplete="off"
            value={formData.legalEntity || ''}
            onChange={(e) => {
              setFormData({ ...formData, legalEntity: e.target.value });
              clearError('legalEntityError');
            }}
          />
          {errors.legalEntityError && <span className="error-message">{errors.legalEntityError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.dataProvidersError ? 'error' : '')}>
          <label className="input-label">
            Data Providers <sup>*</sup>
          </label>

          <AddUser
            getCollabarators={getDataProviders}
            dagId=""
            isRequired={false}
            isUserprivilegeSearch={false}
            title="Data Provider"
          />

          <div className={Styles.dataProvidersList}>
            {formData.dataProviders?.length > 0 ? (
              <>
                <div className={Styles.colHeader}>
                  <div className={Styles.column1}>User ID</div>
                  <div className={Styles.column2}>Name</div>
                  <div className={Styles.column4}></div>
                </div>

                <div className={classNames('mbc-scroll', Styles.collaboratorContent)}>
                  {formData.dataProviders.map((user) => (
                    <div key={user.id} className={Styles.userRow}>
                      <div className={Styles.column1}>{user.id}</div>
                      <div className={Styles.column2}>
                        {user.firstName + ' ' + user.lastName}
                      </div>
                      <div className={Styles.column4}>
                        {!user.isOwner && (
                          <span tooltip-data="Remove" className={Styles.deleteEntry} onClick={() => onDataProviderDelete(user.id)}>
                            <i className="icon mbc-icon trash-outline" tooltip-data={'Delete'} />
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className={Styles.collaboratorSectionEmpty}>
                <h6>No Data Providers Added!</h6>
              </div>
            )}
          </div>

          {errors.dataProvidersError && <span className="error-message">{errors.dataProvidersError}</span>}
        </div>
      </div>
    </div>
  );
};



const Step3_TechnicalMetadata = ({
  formData,
  setFormData,
  errors,
  clearError,
}) => {

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  return (
    <div className={Styles.stepForm}>
      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.cdcDatabaseLinkError ? 'error' : '')}>
          <label htmlFor="cdcDatabaseLink" className="input-label">
            CDC Database Link <sup>*</sup>
          </label>
          <input
            id="cdcDatabaseLink"
            className="input-field"
            autoComplete="off"
            value={formData.cdcDatabaseLink || ''}
            onChange={(e) => {
              setFormData({ ...formData, cdcDatabaseLink: e?.target?.value });
              clearError('cdcDatabaseLinkError');
            }}
          />
          {errors.cdcDatabaseLinkError && <span className="error-message">{errors.cdcDatabaseLinkError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.dataHubError ? 'error' : '')}>
          <label htmlFor="dataHub" className="input-label">
            Data Hub <sup>*</sup>
          </label>
          <div className={classNames('custom-select')}>
            <select
              id="dataHub"
              defaultValue={formData.dataHubName || ''}
              onChange={(e) => {
                setFormData({ ...formData, dataHubName: e?.target?.value });
                clearError('dataHubError');
              }}
            >
              <option value="">Choose</option>
              {DATA_HUBS.map((d, i) => (
                <option key={i} value={d}>{d}</option>
              ))}
            </select>
          </div>
          {errors.dataHubError && <span className="error-message">{errors.dataHubError}</span>}
        </div>
      </div>

      <div className={Styles.flex}>
        <div className={Styles.col2}>
          <div className={classNames('input-field-group include-error', errors.cloudProviderError ? 'error' : '')}>
            <label className="input-label">
              Cloud Provider <sup>*</sup>
            </label>
            <div className={Styles.boolean}>
              {CLOUD_PROVIDERS.map((cp) => (
                <label key={cp} className="radio">
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      name="cloudProvider"
                      value={cp}
                      checked={(formData.cloudProvider || 'Azure') === cp}
                      disabled
                    />
                  </span>
                  <span className="label">{cp}</span>
                </label>
              ))}
            </div>
            {errors.cloudProviderError && <span className="error-message">{errors.cloudProviderError}</span>}
          </div>
        </div>

        <div className={Styles.col2}>
          <div className={classNames('input-field-group include-error', errors.technologyError ? 'error' : '')}>
            <label className="input-label">
              Technology <sup>*</sup>
            </label>
            <div className={Styles.boolean}>
              {TECHNOLOGIES.map((t) => (
                <label key={t} className="radio">
                  <span className="wrapper">
                    <input
                      type="radio"
                      className="ff-only"
                      name="technology"
                      value={t}
                      checked={(formData.technology || 'UnityCatalog') === t}
                      disabled
                    />
                  </span>
                  <span className="label">{t}</span>
                </label>
              ))}
            </div>
            {errors.technologyError && <span className="error-message">{errors.technologyError}</span>}
          </div>
        </div>
      </div>
    </div>
  );
};

const Step4_ComplianceUsage = ({
  formData,
  setFormData,
  errors,
  clearError,
  purposes,
  criteriaTransferPricing,
  qualificationTransferPricing
}) => {
  
  const [securityLevelDropdown, setSecurityLevelDropdown] = useState([]);

  useEffect(() => {
    ProgressIndicator.show();
    fabricApi.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setSecurityLevelDropdown(response[0]?.data?.data || []);
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className={Styles.stepForm}>
      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.securityLevelError ? 'error' : '')}>
          <label htmlFor="securityLevel" className="input-label">
            Security Level <sup>*</sup>
          </label>
          <div className={classNames('custom-select')}>
            <select
              id="securityLevel"
              defaultValue={formData.securityLevel || ''}
              onChange={(e) => {
                setFormData({ ...formData, securityLevel: e?.target?.value });
                clearError('securityLevelError');
              }}
            >
              <option value="">Choose</option>
              {securityLevelDropdown.map((item) => (
                <option
                  id={item.id}
                  key={item.id}
                  value={item.name}
                >
                  {item.name}
                </option>
              ))}
            </select>
          </div>
          {errors.securityLevelError && <span className="error-message">{errors.securityLevelError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.purposesError ? 'error' : '')}>
          <label htmlFor="purposes" className="input-label">
            Purposes <sup>*</sup>
          </label>
          <div className={classNames('custom-select')}>
          <select
            id="purposes"
            multiple={true}
            onChange={(e) => {
              const values = Array.from(e?.target?.selectedOptions, opt => opt?.value);
              setFormData({ ...formData, purposes: values });
              clearError('purposesError');
            }}
          >
            {purposes.map((p, i) => (
              <option key={i} value={p}>
                {p}
              </option>
            ))}
          </select>
          </div>
          {errors.purposesError && <span className="error-message">{errors.purposesError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.priceError ? 'error' : '')}>
          <label className="input-label">Price <sup>*</sup> 
          &nbsp;                
          <a
            target="_blank"
            rel="noopener noreferrer"
            title="Is the provided data product publicly available and free of charge?"
          >
            <i className="icon mbc-icon help" />
          </a>
          </label>
          <div className={Styles.boolean}>
            <label className="radio">
              <span className="wrapper">
                <input
                  type="radio"
                  className="ff-only"
                  name="isPricing"
                  value="true"
                  defaultChecked={formData.price === true}
                  onChange={() => {
                    setFormData({ ...formData, price: true });
                    clearError('priceError');
                  }}
                />
              </span>
              <span className="label">Yes</span>
            </label>
            <label className="radio">
              <span className="wrapper">
                <input
                  type="radio"
                  className="ff-only"
                  name="isPricing"
                  value="false"
                  defaultChecked={formData.price === false}
                  onChange={() => {
                    setFormData({ ...formData, price: false });
                    clearError('priceError');
                  }}
                />
              </span>
              <span className="label">No</span>
            </label>
          </div>
          {errors.priceError && <span className="error-message">{errors.priceError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div
          className={classNames('input-field-group include-error', errors.criteriaTransferPricingError ? 'error' : '')}
        >
          <label htmlFor="criteriaTransferPricing" className="input-label">
            Criteria Transfer Pricing <sup>*</sup>
          </label>
          <div className={classNames('custom-select')}>
          <select
            id="criteriaTransferPricing"
            multiple={true}
            onChange={(e) => {
              const values = Array.from(e?.target?.selectedOptions, opt => opt?.value);
              setFormData({ ...formData, criteriaTransferPricing: values });
              clearError('criteriaTransferPricingError');
            }}
          >
            {criteriaTransferPricing.map((c, i) => (
              <option key={i} value={c}>
                {c}
              </option>
            ))}
          </select>
          </div>
          {errors.criteriaTransferPricingError && <span className="error-message">{errors.criteriaTransferPricingError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div
          className={classNames('input-field-group include-error', errors.qualificationTransferPricingError ? 'error' : '')}
        >
          <label htmlFor="qualificationTransferPricing" className="input-label">
            Qualification Transfer Pricing <sup>*</sup>
          </label>
          <div className={classNames('custom-select')}>
          <select
            id="qualificationTransferPricing"
            multiple={true}
            onChange={(e) => {
              const values = Array.from(e?.target?.selectedOptions, opt => opt?.value);
              setFormData({ ...formData, qualificationTransferPricing: values });
              clearError('qualificationTransferPricingError');
            }}
          >
            {qualificationTransferPricing.map((q, i) => (
              <option key={i} value={q}>
                {q}
              </option>
            ))}
          </select>
          </div>
          {errors.qualificationTransferPricingError && (
            <span className="error-message">{errors.qualificationTransferPricingError}</span>
          )}
        </div>
      </div>
    </div>
  );
};


const ViewDdxTablesModalContent = ({ workspaceOwner, workspaceDivision }) => {

  useEffect(() => {
    Tooltip.defaultSetup();
    SelectBox.defaultSetup();
  }, []);

  const [currentStep, setCurrentStep] = useState('step1');

  const [formData, setFormData] = useState({
    dataProviders: workspaceOwner ? [{
      id: workspaceOwner.id,
      firstName: workspaceOwner.firstName,
      lastName: workspaceOwner.lastName,
      department: workspaceOwner.department,
      email: workspaceOwner.email,
      isOwner: true,
    }] : [],
    technology: 'UnityCatalog',   
    cloudProvider: 'Azure',
  });

  useEffect(() => {
    if (workspaceDivision && BUSINESS_DOMAINS.includes(workspaceDivision)) {
      setFormData((prev) => ({
        ...prev,
        businessDomain: workspaceDivision,
      }));
    }
  }, [workspaceDivision]);

  const [errors, setErrors] = useState({});

  const clearError = (field) => {
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const validateCurrentStep = () => {
    const newErrors = {};

    if (currentStep === 'step1') {
      if (!formData.dataProductName?.trim()) {
        newErrors.nameError = '*Missing entry';
      } else if (!/^[A-Z]/.test(formData.dataProductName.trim())) {
        newErrors.nameError = 'First letter must be capital';
      }

      if (!formData.dataProductDescription || formData.dataProductDescription.trim().length < 50) {
        newErrors.descriptionError = '*Description must be at least 50 characters';
      }
    }

    if (currentStep === 'step2') {
      if (!formData.informationOwner?.trim()) {
        newErrors.informationOwnerError = '*Missing entry';
      }
      if (!formData.divisions) {
        newErrors.divisionError = '*Missing entry';
      }
      if (!formData.businessDomain) {
        newErrors.businessDomainError = '*Missing entry';
      }
      if (!formData.legalEntity?.trim()) {
        newErrors.legalEntityError = '*Missing entry';
      }
      if (!formData.dataProviders || formData.dataProviders.length < 2) {
        newErrors.dataProvidersError = '*At least 2 data providers required (including workspace owner)';
      }
    }

    if (currentStep === 'step3') {
      if (!formData.cdcDatabaseLink?.trim()) {
        newErrors.cdcDatabaseLinkError = '*Missing entry';
      }
      if (!formData.cloudProvider) {
        newErrors.cloudProviderError = '*Missing entry';
      }
      if (!formData.dataHubName) {
        newErrors.dataHubError = '*Missing entry';
      }
      if (!formData.technology) {
        newErrors.technologyError = '*Missing entry';
      }
    }

    if (currentStep === 'step4') {
      if (!formData.securityLevel) {
        newErrors.securityLevelError = '*Missing entry';
      }
      if (!formData.purposes?.length) {
        newErrors.purposesError = '*Missing entry';
      }
      if (typeof formData.price === 'undefined') {
        newErrors.priceError = '*Missing entry';
      }
      if (!formData.criteriaTransferPricing?.length) {
        newErrors.criteriaTransferPricingError = '*Missing entry';
      }
      if (!formData.qualificationTransferPricing?.length) {
        newErrors.qualificationTransferPricingError = '*Missing entry';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNext = () => {
    if (validateCurrentStep()) {
      if (currentStep === 'step1') setCurrentStep('step2');
      else if (currentStep === 'step2') setCurrentStep('step3');
      else if (currentStep === 'step3') setCurrentStep('step4');
    }
  };

  const handlePrev = () => {
    if (currentStep === 'step4') setCurrentStep('step3');
    else if (currentStep === 'step3') setCurrentStep('step2');
    else if (currentStep === 'step2') setCurrentStep('step1');
  };

  const handleSubmit = () => {
    if (!validateCurrentStep()) {
      return;
    }

    const securityLevel = formData.securityLevel === 'Secret' ? 'Confidential' : formData.securityLevel;

    const payload = {
      dataProductName: formData.dataProductName || '',
      dataProductDescription: formData.dataProductDescription || '',
      informationOwner: formData.informationOwner || '',
      cdcDatabaseLink: formData.cdcDatabaseLink || '',
      cdcDataProductLink: formData.cdcDataProductLink || '',
      securityLevel: securityLevel || '',
      dataHubName: formData.dataHubName || '',
      cloudProvider: formData.cloudProvider || '',
      technology: formData.technology || '',
      frequency: formData.frequency || '',
      purposes: formData.purposes || [],
      dataProviders: formData.dataProviders || [],
      divisions: formData.divisions || '',
      isTransferPricing: !!formData.isTransferPricing,
      criteriaTransferPricing: formData.criteriaTransferPricing || [],
      qualificationTransferPricing: formData.qualificationTransferPricing || [],
      legalEntity: formData.legalEntity || '',
      businessDomain: formData.businessDomain || ''
    };

    console.log('Payload ready for backend:', payload);
    //API here 
  };

  return (
    <div className={Styles.form}>
      <div className={Styles.stepsContainer}>
        <div
          className={classNames(
            Styles.step,
            (currentStep === 'step1' || currentStep === 'step2' || currentStep === 'step3' || currentStep === 'step4') && Styles.complete
          )}
        >
          <div className={classNames(Styles.stepIcon, currentStep === 'step1' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Basic Identification</div>
        </div>

        <div className={classNames(Styles.step, (currentStep === 'step2' || currentStep === 'step3' || currentStep === 'step4') && Styles.complete)}>
          <div className={classNames(Styles.stepIcon, currentStep === 'step2' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Ownership & Governance</div>
        </div>

        <div className={classNames(Styles.step, (currentStep === 'step3' || currentStep === 'step4') && Styles.complete)}>
          <div className={classNames(Styles.stepIcon, currentStep === 'step3' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Technical Metadata</div>
        </div>

        <div className={classNames(Styles.step, currentStep === 'step4' && Styles.complete)}>
          <div className={classNames(Styles.stepIcon, currentStep === 'step4' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Compliance & Usage</div>
        </div>
      </div>

      {currentStep === 'step1' && (
        <Step1_BasicIdentification
          formData={formData}
          setFormData={setFormData}
          errors={errors}
          clearError={clearError}
        />
      )}

      {currentStep === 'step2' && (
        <Step2_OwnershipGovernance
          formData={formData}
          setFormData={setFormData}
          errors={errors}
          clearError={clearError}
          workspaceOwner={workspaceOwner}
          workspaceDivision={workspaceDivision} 
        />
      )}

      {currentStep === 'step3' && (
        <Step3_TechnicalMetadata
          formData={formData}
          setFormData={setFormData}
          errors={errors}
          clearError={clearError}
        />
      )}

      {currentStep === 'step4' && (
        <Step4_ComplianceUsage
          formData={formData}
          setFormData={setFormData}
          errors={errors}
          clearError={clearError}
          purposes={PURPOSES || []}
          criteriaTransferPricing={CRITERIA_TRANSFER_PRICING || []}
          qualificationTransferPricing={QUALIFICATION_TRANSFER_PRICING || []}
        />
      )}

      <div className={Styles.formFooter}>
        {currentStep !== 'step1' && (
          <button className="btn btn-primary" type="button" onClick={handlePrev}>
            Prev
          </button>
        )}

        {currentStep !== 'step4' && (
          <button className="btn btn-tertiary" type="button" onClick={handleNext}>
            Next
          </button>
        )}

        {currentStep === 'step4' && (
          <button className="btn btn-tertiary" type="button" onClick={handleSubmit}>
            Submit
          </button>
        )}
      </div>
    </div>
  );
};

export default ViewDdxTablesModalContent;