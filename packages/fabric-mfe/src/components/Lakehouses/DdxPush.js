import React, { useState, useEffect } from 'react';
import classNames from 'classnames';
import Styles from './DdxPush.scss';
import AddUser from 'dna-container/AddUser';
import SelectBox from 'dna-container/SelectBox';
import TypeAheadBox from 'dna-container/TypeAheadBox';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { fabricApi } from '../../apis/fabric.api';
import { DIVISIONS, BUSINESS_DOMAINS, CLOUD_PROVIDERS, TECHNOLOGIES, PURPOSES, CRITERIA_TRANSFER_PRICING, QUALIFICATION_TRANSFER_PRICING, SECURITY_LEVELS, UPDATE_FREQUENCIES } from '../../utilities/constants';
import { Envs } from '../../utilities/envs';

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

  const [legalEntityOptions, setLegalEntityOptions] = useState([]);

  const handleLegalEntitySearch = (searchTerm, showSpinner) => {
    clearError('legalEntityError');
    if (searchTerm.length > 3) {
      showSpinner && showSpinner(true);
      fabricApi.getLegalEntities(searchTerm)
        .then((response) => {
          showSpinner && showSpinner(false);
          setLegalEntityOptions(response?.data || []); 
        })
        .catch((err) => {
          showSpinner && showSpinner(false);
          Notification.show(err?.response?.data?.errors?.[0]?.message || 'Something went wrong.', 'alert');
        });
    } else {
      setLegalEntityOptions([]);
    }
  };

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
    // SelectBox.defaultSetup();
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
            Information Owner <sup>*</sup> (Please provide ShortID.Kindly find more information
            <a href={Envs.INFORMATION_OWNER_URL} target="_blank" rel="noopener noreferrer"> here</a>)
          </label>
          <input
            id="informationOwner"
            className="input-field"
            autoComplete="off"
            value={formData.informationOwner || ''}
            onChange={(e) => {
              setFormData({ ...formData, informationOwner: e.target.value.toUpperCase() });
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
              defaultValue={formData.businessDomain || ''}
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
          <TypeAheadBox
            label={'Legal Entity (Which Legal Entity owns the data Product? Usually, this is the legal entity of the information Owner of the data product.)'}
            placeholder={'Enter minimum 4 characters to search'}
            defaultValue={formData.legalEntity?.legalName ? `${formData.legalEntity.legalName} (${formData.legalEntity.companyCode})` : ''}
            list={legalEntityOptions}
            maxOptions={1}
            setSelected={(selected) => {
              setFormData((prev) => ({
                ...prev,
                legalEntity: { legalName: selected.legalName, companyCode: selected.companyCode },
              }));
              clearError('legalEntityError');
            }}
            onInputChange={handleLegalEntitySearch}
            required={true}
            showError={errors.legalEntityError}
            render={(item) => (
              <div className={Styles.optionContainer}>
                  <span className={Styles.optionText}>
                    {`${item.legalName} (${item.companyCode})`}
                  </span>
              </div>
            )}
          />
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.dataProvidersError ? 'error' : '')}>
          <label className="input-label">
            Data Providers <sup>*</sup> &nbsp;
            (Minimum of two Data Providers are required)
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
  // setFormData,
  errors,
  // clearError,
}) => {

  useEffect(() => {
    // SelectBox.defaultSetup();
  }, []);

  return (
    <div className={Styles.stepForm}>
      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.dataHubError ? 'error' : '')}>
          <label htmlFor="dataHub" className="input-label">
            Data Hub <sup>*</sup> &nbsp;
            (Choose the name of the Data Hub where the data product is stored)
          </label>
          <input
            id="dataHub"
            className="input-field"
            value="oneFabric"
            disabled
            readOnly
          />
          {errors.dataHubError && <span className="error-message">{errors.dataHubError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.fulfillsDataCloudFrameworkError ? 'error' : '')}>
          <label className="input-label">
            Fulfills Data Cloud Framework <sup>*</sup> &nbsp;
            (We highly recommend to conduct a Data@Cloud assessment of your data product as described <a href={Envs.DATA_AT_CLOUD_URL} target="_blank" rel="noopener noreferrer"> here</a>)
          </label>
          <div className={Styles.boolean}>
            <label className="radio">
              <span className="wrapper">
                <input
                  type="radio"
                  className="ff-only"
                  name="fulfillsDataCloudFramework"
                  value="true"
                  checked={true}
                  disabled
                  readOnly
                />
              </span>
              <span className="label">Yes</span>
            </label>
            <label className="radio">
              <span className="wrapper">
                <input
                  type="radio"
                  className="ff-only"
                  name="fulfillsDataCloudFramework"
                  value="false"
                  checked={false}
                  disabled
                  readOnly
                />
              </span>
              <span className="label">No</span>
            </label>
          </div>
          {errors.fulfillsDataCloudFrameworkError && <span className="error-message">{errors.fulfillsDataCloudFrameworkError}</span>}
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
                <label key={t} className="checkbox">
                  <span className="wrapper">
                    <input
                      type="checkbox"
                      className="ff-only"
                      value={t}
                      checked={true}
                      disabled
                      readOnly
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
  // securityLevel,
  purposes,
  updateFrequencies,
}) => {

  useEffect(() => {
    // SelectBox.defaultSetup();
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
              value="Public"
              onChange={() => {}}
            >
              {SECURITY_LEVELS.map((sl) => (
                <option key={sl} value={sl} disabled={sl !== 'Public'}>
                  {sl}
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
            Purposes <sup>*</sup> &nbsp;
            (Please specify the allowed purposes for processing this data)
          </label>
          <div className={classNames('custom-select')}>
          <select
            id="purposes"
            multiple={true}
            value={formData.purposes || []}
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
        <div className={classNames('input-field-group include-error', errors.personalDataContainedError ? 'error' : '')}>
          <label className="input-label">
            Personal Data Contained <sup>*</sup>
          </label>
          <div className={Styles.boolean}>
            <label className="radio">
              <span className="wrapper">
                <input
                  type="radio"
                  className="ff-only"
                  name="personalDataContained"
                  value="true"
                  defaultChecked={formData.personalDataContained === true}
                  onChange={() => {
                    setFormData({ ...formData, personalDataContained: true });
                    clearError('personalDataContainedError');
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
                  name="personalDataContained"
                  value="false"
                  defaultChecked={formData.personalDataContained === false}
                  onChange={() => {
                    setFormData({ ...formData, personalDataContained: false });
                    clearError('personalDataContainedError');
                  }}
                />
              </span>
              <span className="label">No</span>
            </label>
          </div>
          {errors.personalDataContainedError && <span className="error-message">{errors.personalDataContainedError}</span>}
        </div>
      </div>

      <div className={Styles.col}>
        <div className={classNames('input-field-group include-error', errors.updateFrequencyError ? 'error' : '')}>
          <label htmlFor="updateFrequency" className="input-label">
            Update Frequency <sup>*</sup> (Frequency by which data from source system will be updated.)
          </label>
          <div className={classNames('custom-select')}>
            <select
              id="updateFrequency"
              defaultValue={formData.frequency || ''}
              onChange={(e) => {
                setFormData({ ...formData, frequency: e?.target?.value });
                clearError('updateFrequencyError');
              }}
            >
              <option value="">Choose</option>
              {updateFrequencies.map((f, i) => (
                <option key={i} value={f}>{f}</option>
              ))}
            </select>
          </div>
          {errors.updateFrequencyError && <span className="error-message">{errors.updateFrequencyError}</span>}
        </div>
      </div>
    </div>
  );
};


const Step5_PersonalData = ({ formData, setFormData, errors, clearError, criteriaTransferPricing, qualificationTransferPricing }) => {

  useEffect(() => {
    // SelectBox.defaultSetup();
  }, []);

  return (
  <div className={Styles.stepForm}>
    <div className={Styles.col}>
      <div className={classNames('input-field-group include-error', errors.priceError ? 'error' : '')}>
        <label className="input-label">Free of Charge <sup>*</sup>  &nbsp;
        (The data is available in the public space as well for consumers outside of the Mercedes-Benz environment and does not have a price tag.
        For further informmation click <a href={Envs.DDX_PRICING_URL} target="_blank" rel="noopener noreferrer"> here</a>)
        </label>
        <div className={Styles.boolean}>
          <label className="radio">
            <span className="wrapper">
              <input
                type="radio"
                className="ff-only"
                name="isPricing"
                value="true"
                checked={true}
                disabled
                readOnly
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
                checked={false}
                disabled
                readOnly
              />
            </span>
            <span className="label">No</span>
          </label>
        </div>
        {errors.priceError && <span className="error-message">{errors.priceError}</span>}
      </div>
    </div>

    <div className={Styles.col}>
      <div className={classNames('input-field-group include-error', errors.criteriaTransferPricingError ? 'error' : '')}>
        <label htmlFor="criteriaTransferPricing" className="input-label">
          Criteria Transfer Pricing <sup>*</sup> &nbsp;
          (Choose the criteria which are relevant for your data product to define if it is relevant for transfer pricing between legal entities)
        </label>
        <div className={classNames('custom-select')}>
        <select
          id="criteriaTransferPricing"
          multiple={true}
          value={formData.criteriaTransferPricing || []}
          onChange={(e) => {
            const values = Array.from(e?.target?.selectedOptions, opt => opt?.value);
            setFormData({ ...formData, criteriaTransferPricing: values });
            clearError('criteriaTransferPricingError');
          }}
        >
          {criteriaTransferPricing.map((c, i) => (
            <option key={i} value={c}>{c}</option>
          ))}
        </select>
        </div>
        {errors.criteriaTransferPricingError && <span className="error-message">{errors.criteriaTransferPricingError}</span>}
      </div>
    </div>

    <div className={Styles.col}>
      <div className={classNames('input-field-group include-error', errors.qualificationTransferPricingError ? 'error' : '')}>
        <label htmlFor="qualificationTransferPricing" className="input-label">
          Qualification Transfer Pricing <sup>*</sup>
        </label>
        <div className={classNames('custom-select')}>
        <select
          id="qualificationTransferPricing"
          multiple={true}
          value={formData.qualificationTransferPricing || []}
          onChange={(e) => {
            const values = Array.from(e?.target?.selectedOptions, opt => opt?.value);
            setFormData({ ...formData, qualificationTransferPricing: values });
            clearError('qualificationTransferPricingError');
          }}
        >
          {qualificationTransferPricing.map((q, i) => (
            <option key={i} value={q}>{q}</option>
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

const ViewDdxTablesModalContent = ({ workspaceId, workspaceName, workspaceOwner, workspaceDivision, lakehouseId, lakehouseName, ddxPublishedLakeHouseDetails, onRefreshWorkspace }) => {

  const isDdxAlreadyPushed = ddxPublishedLakeHouseDetails?.some(d => d.lakeHouseId === lakehouseId);

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const [currentStep, setCurrentStep] = useState('step1');
  const [hasSubmittedOnce, setHasSubmittedOnce] = useState(false);

  useEffect(() => {
    SelectBox.defaultSetup();    
  }, [currentStep]);

  const [formData, setFormData] = useState({
    dataProviders: workspaceOwner ? [{
      id: workspaceOwner.id,
      firstName: workspaceOwner.firstName,
      lastName: workspaceOwner.lastName,
      department: workspaceOwner.department,
      email: workspaceOwner.email,
      isOwner: true,
    }] : [],
    technology: ['UnityCatalog', 'Fabric'],   
    cloudProvider: 'Azure',
    dataHubName: 'oneFabric',
    securityLevel: 'Public',
    fulfillsDataCloudFramework: true,
    price: true,
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
      if (!formData.legalEntity?.legalName) {
        newErrors.legalEntityError = '*Missing entry';
      }
      if (!formData.dataProviders || formData.dataProviders.length < 2) {
        newErrors.dataProvidersError = '*At least 2 data providers required (including workspace owner)';
      }
    }

    if (currentStep === 'step3') {
      if (!formData.cloudProvider) {
        newErrors.cloudProviderError = '*Missing entry';
      }
      if (!formData.dataHubName) {
        newErrors.dataHubError = '*Missing entry';
      }
      if (!formData.technology) {
        newErrors.technologyError = '*Missing entry';
      }
      if (typeof formData.fulfillsDataCloudFramework === 'undefined') {
        newErrors.fulfillsDataCloudFrameworkError = '*Missing entry';
      }
    }

    if (currentStep === 'step4') {
      if (!formData.securityLevel) {
        newErrors.securityLevelError = '*Missing entry';
      }
      if (!formData.purposes?.length) {
        newErrors.purposesError = '*Missing entry';
      }
      if (typeof formData.personalDataContained === 'undefined') {
        newErrors.personalDataContainedError = '*Missing entry';
      }
      if (!formData.frequency) {
        newErrors.updateFrequencyError = '*Missing entry';
      }
    }

    if (currentStep === 'step5') {
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
      else if (currentStep === 'step4') setCurrentStep('step5');
    }
  };

  const handlePrev = () => {
    if (currentStep === 'step5') setCurrentStep('step4');
    else if (currentStep === 'step4') setCurrentStep('step3');
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
      purposes: formData.purposes || [],
      dataProviders: (formData.dataProviders || []).map((u) => u.id),
      divisions: (formData.divisions || '').replace(/-/g, ' ') || '',

      isTransferPricing: !!formData.isTransferPricing,
      criteriaTransferPricing: formData.criteriaTransferPricing || [],
      qualificationTransferPricing: formData.qualificationTransferPricing || [],
      legalEntity: formData.legalEntity
        ? { label: formData.legalEntity.legalName, value: formData.legalEntity.companyCode }
        : {},
      fulfillsDataCloudFramework: !!formData.fulfillsDataCloudFramework,
      businessDomain: formData.businessDomain || '',
      personalDataContained: !!formData.personalDataContained,
      dataProductConnections: [
        {
          dataHubName: formData.dataHubName || '',
          storingCountries: [],
          cloudRegion: '',
          formatType: '',
          technology: 'UnityCatalog',
          frequency: formData.frequency || '',
          cloudProvider: formData.cloudProvider || '',
          dataProductConnectionString: {
            catalogName: '',
            schemaName: '',
            fullSchema: true,
          },
          dataSources: [],
        },
        {
          dataHubName: formData.dataHubName || '',
          storingCountries: [],
          cloudRegion: '',
          formatType: '',
          technology: 'Fabric',
          frequency: formData.frequency || '',
          cloudProvider: formData.cloudProvider || '',
          dataProductConnectionString: {
            workspaceName: workspaceName || '',
            lakehouseName: lakehouseName || '',
            fullLakehouse: true,
            workspaceId: workspaceId || '',
            lakehouseId: lakehouseId || '',
          },
          dataSources: [],
        },
      ],
    };
    //remove for testing
    console.log('Submit payload:', JSON.stringify(payload, null, 2));

    ProgressIndicator.show();
    fabricApi.publishDdxDataProduct(workspaceId, lakehouseId, payload)
      .then((response) => {
        ProgressIndicator.hide();
        const dataProductId = response?.data?.dataProductId;
        const baseUrl = (Envs.DDX_DOF_BASE_URL || '').replace(/\/$/, '');
        const dofUrl = `${baseUrl}/myDataProducts/onboardingForm/${dataProductId}`;
        Notification.show('Data product onboarded successfully.', 'success');
        setHasSubmittedOnce(true);
        if (dataProductId) {
          window.open(dofUrl, '_blank', 'noopener,noreferrer');
        }
        onRefreshWorkspace && onRefreshWorkspace();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show(err?.response?.data?.errors?.[0]?.message || 'Failed to onboard data product.', 'alert');
      });
  };

  return (
    <div className={Styles.form}>
      <div className={Styles.stepsContainer}>
        <div
          className={classNames(
            Styles.step,
            (currentStep === 'step1' || currentStep === 'step2' || currentStep === 'step3' || currentStep === 'step4' || currentStep === 'step5') && Styles.complete
          )}
        >
          <div className={classNames(Styles.stepIcon, currentStep === 'step1' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Basic Identification</div>
        </div>

        <div className={classNames(Styles.step, (currentStep === 'step2' || currentStep === 'step3' || currentStep === 'step4' || currentStep === 'step5') && Styles.complete)}>
          <div className={classNames(Styles.stepIcon, currentStep === 'step2' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Ownership & Governance</div>
        </div>

        <div className={classNames(Styles.step, (currentStep === 'step3' || currentStep === 'step4' || currentStep === 'step5') && Styles.complete)}>
          <div className={classNames(Styles.stepIcon, currentStep === 'step3' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Technical Metadata</div>
        </div>

        <div className={classNames(Styles.step, (currentStep === 'step4' || currentStep === 'step5') && Styles.complete)}>
          <div className={classNames(Styles.stepIcon, currentStep === 'step4' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Compliance & Usage</div>
        </div>

        <div className={classNames(Styles.step, currentStep === 'step5' && Styles.complete)}>
          <div className={classNames(Styles.stepIcon, currentStep === 'step5' && Styles.activeIcon)}>
            <i className="icon mbc-icon tools-mini" />
          </div>
          <div className={Styles.stepLabel}>Personal Data</div>
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
          securityLevel={SECURITY_LEVELS || []}
          purposes={PURPOSES || []}
          updateFrequencies={UPDATE_FREQUENCIES || []}
        />
      )}

      {currentStep === 'step5' && (
        <Step5_PersonalData
          formData={formData}
          setFormData={setFormData}
          errors={errors}
          clearError={clearError}
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

        {currentStep !== 'step5' && (
          <button className="btn btn-tertiary" type="button" onClick={handleNext}>
            Next
          </button>
        )}

        {currentStep === 'step5' && (
          <button className={(hasSubmittedOnce || isDdxAlreadyPushed) ? 'btn btn-primary' : 'btn btn-tertiary'} type="button" disabled={hasSubmittedOnce || isDdxAlreadyPushed} onClick={handleSubmit} title={isDdxAlreadyPushed ? 'A DDX data product has already been published for this lakehouse' : ''}>
            Submit
          </button>
        )}
      </div>
    </div>
  );
};

export default ViewDdxTablesModalContent;