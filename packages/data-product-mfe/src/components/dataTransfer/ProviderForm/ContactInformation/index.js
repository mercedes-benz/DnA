import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';

// components from container app
import SelectBox from 'dna-container/SelectBox';
import InfoModal from 'dna-container/InfoModal';
import Tags from 'dna-container/Tags';
import DatePicker from 'dna-container/DatePicker';
import TeamSearch from 'dna-container/TeamSearch';
import TypeAheadBox from 'dna-container/TypeAheadBox';

import { useFormContext, Controller } from 'react-hook-form';
import { hostServer } from '../../../../server/api';

import ProgressIndicator from '../../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../../common/modules/uilab/js/src/notification';
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';

import { useSelector } from 'react-redux';
import { dataTransferApi } from '../../../../apis/datatransfers.api';

// import dayjs from 'dayjs';
import { debounce } from 'lodash';

const ContactInformation = ({ 
  // onSave, 
  divisions, setSubDivisions, subDivisions, isDataProduct = false }) => {
  const {
    register,
    formState: { errors, 
      // isSubmitting,
      dirtyFields },
    watch,
    // handleSubmit,
    trigger,
    reset,
    setValue,
    control,
    getValues,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);
  const provideDataTransfers = useSelector((state) =>
    !isDataProduct ? state.provideDataTransfers : state.dataProduct,
  );

  const {
    division,
    department,
    complianceOfficer: selectedcomplianceOfficer,
    name,
    planningIT,
    informationOwner,
  } = watch();

  const [complianceOfficerList, setComplianceOfficerList] = useState({
    records: [],
    totalCount: 0,
  });
  const [complianceOfficer, setComplianceOfficer] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [selectedDepartment, setSelectedDepartment] = useState([]);

  const [planningITList, setPlanningITList] = useState([]);
  const [selectedPlanningIT, setSelectedPlanningIT] = useState([]);

  const [searchTerm, setSearchTerm] = useState('');
  const [fieldValue, setFieldValue] = useState('');

  const [informationOwnerSearchTerm, setInformationOwnerSearchTerm] = useState('');
  const [informationOwnerFieldValue, setInformationOwnerFieldValue] = useState('');

  // const minDate = dayjs().format();

  useEffect(() => {
    const id = watch('division');
    if (id > '0') {
      ProgressIndicator.show();
      hostServer.get('/subdivisions/' + id).then((res) => {
        setSubDivisions(res?.data || []);
        if (!dirtyFields.division && !dirtyFields.subDivision) {
          let selected = isDataProduct
            ? provideDataTransfers?.selectedDataProduct
            : provideDataTransfers.selectedDataTransfer;
          setValue('subDivision', selected.subDivision);
        } else {
          setValue('subDivision', '0');
        }
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
      });
    } else {
      setSubDivisions([]);
      if (dirtyFields.division) {
        setValue('subDivision', '0');
        SelectBox.defaultSetup();
      }
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [division]);

  useEffect(() => {
    Tooltip.defaultSetup();
    !isDataProduct && SelectBox.defaultSetup();
    reset(watch());
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    // if division is set to "Choose", reset sub divison dropdown to empty list
    division === '0' && SelectBox.defaultSetup();
  }, [division, subDivisions.length]);

  useEffect(() => {
    ProgressIndicator.show();
    dataTransferApi
      .getDataComplianceList(0, 0, 'entityId', 'asc')
      .then((res) => {
        res.data?.records?.map((item) => {
          const localComplianceOfficers = item.localComplianceOfficer.join(', ');
          const localComplianceResponsibles = item.localComplianceResponsible.join(', ');
          item['name'] =
            item.entityId + ' ' + item.entityName + ' ' + localComplianceOfficers + ' ' + localComplianceResponsibles;
          return item;
        });
        setComplianceOfficerList(res.data);
        ProgressIndicator.hide();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        Notification.show(
          e.response?.data?.errors?.[0]?.message || 'Error while fethcing data compliance officers list.',
          'alert',
        );
      });
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    if (selectedcomplianceOfficer?.length) {
      setComplianceOfficer(selectedcomplianceOfficer);
    }
  }, [selectedcomplianceOfficer]);

  useEffect(() => {
    if (planningIT?.length) {
      setSelectedPlanningIT(planningIT);
    }
  }, [planningIT]);

  useEffect(() => {
    ProgressIndicator.show();
    dataTransferApi
      .getDepartments()
      .then((res) => {
        setDepartments(res?.data?.data);
        ProgressIndicator.hide();
      })
      .catch((e) => {
        Notification.show(e?.response?.data?.errors[0]?.message || 'Error while fetching department list', 'alert');
        ProgressIndicator.hide();
      });
  }, []);

  useEffect(() => {
    setSelectedDepartment(department);
  }, [department]);

  useEffect(() => {
    let nameStr = typeof name === 'string' ? name : `${name?.firstName} ${name?.lastName}`;
    name && setFieldValue(nameStr);
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name]);

  useEffect(() => {
    let nameStr =
      typeof informationOwner === 'string'
        ? informationOwner
        : `${informationOwner?.firstName} ${informationOwner?.lastName}`;
    informationOwner && setInformationOwnerFieldValue(nameStr);
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [informationOwner]);

  const handleName = (field, value) => {
    let name = '';
    if (value) {
      value['addedByProvider'] = true;
      name = `${value.firstName} ${value.lastName}`;
    }
    field.onChange(value);
    setFieldValue(name);
  };

  const handleInformationOwner = (field, value) => {
    let name = '';
    if (value) {
      value['addedByProvider'] = true;
      name = `${value.firstName} ${value.lastName}`;
    }
    field.onChange(value);
    setInformationOwnerFieldValue(name);
  };

  const validateDate = () => {
    const value = getValues('dateOfDataProduct');
    if (typeof value === 'object') {
      const isValidDate = !isNaN(value?.get('date'));
      // const isBefore = dayjs(value).isBefore(minDate, 'date');
      const error =
        value === null || value === ''
          ? '*Missing entry'
          : !isValidDate
          ? 'Invalid Date Format'
          // : isBefore
          // ? 'Is before the minimum date'
          : null;
      // return (value === isValidDate && value !== isBefore) || error;
      return (value === isValidDate) || error;
    } else {
      return (value !== '' && value !== undefined) || '*Missing entry';
    }
  };

  const handlePlanningITSearch = debounce((searchTerm, showSpinner) => {
    if (searchTerm.length > 3) {
      showSpinner(true);
      dataTransferApi
        .getPlanningIT(searchTerm)
        .then((res) => {
          setPlanningITList(res.data.data || []);
          showSpinner(false);
        })
        .catch((e) => {
          showSpinner(false);
          Notification.show(
            e.response?.data?.errors?.[0]?.message || 'Error while fethcing planning IT list.',
            'alert',
          );
        });
    }
  }, 500);

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={classNames(Styles.firstPanel, 'descriptionSection')}>
          <div>
            <h3>Contact Information</h3>
            {showInfoModal && (
              <div className={Styles.infoIcon}>
                <i className={'icon mbc-icon info'} onClick={() => {}} />
              </div>
            )}
          </div>
          <div className={Styles.formWrapper}>
            {isDataProduct ? (
              <div className={classNames('input-field-group include-error', errors.informationOwner ? 'error' : '')}>
                <Controller
                  control={control}
                  name="informationOwner"
                  rules={{ required: '*Missing entry' }}
                  render={({ field }) => (
                    <TeamSearch
                      label={
                        <>
                          Responsible Manager (E3 +) <sup>*</sup>
                        </>
                      }
                      fieldMode={true}
                      fieldValue={informationOwnerFieldValue}
                      setFieldValue={(val) => setInformationOwnerFieldValue(val)}
                      onAddTeamMember={(value) => handleInformationOwner(field, value)}
                      btnText="Add User"
                      searchTerm={informationOwnerSearchTerm}
                      setSearchTerm={(value) => setInformationOwnerSearchTerm(value)}
                      showUserDetails={false}
                      setShowUserDetails={() => {}}
                    />
                  )}
                />
                <span className={classNames('error-message')}>{errors.informationOwner?.message}</span>
              </div>
            ) : (
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors.productName ? 'error' : '')}>
                  <label id="productNameLabel" htmlFor="productNameInput" className="input-label">
                    Data Product Name / Short description of the data transfer <sup>*</sup>
                  </label>
                  <input
                    {...register('productName', { required: '*Missing entry' })}
                    type="text"
                    className="input-field"
                    id="productNameInput"
                    maxLength={200}
                    placeholder="Type here"
                    autoComplete="off"
                  />
                  <span className={classNames('error-message')}>{errors.productName?.message}</span>
                </div>
                <div className={classNames('input-field-group include-error', errors.informationOwner ? 'error' : '')}>
                  <Controller
                    control={control}
                    name="informationOwner"
                    rules={{ required: '*Missing entry' }}
                    render={({ field }) => (
                      <TeamSearch
                        label={
                          <>
                            Data responsible IO and/or Business Owner for application <sup>*</sup>
                          </>
                        }
                        fieldMode={true}
                        fieldValue={informationOwnerFieldValue}
                        setFieldValue={(val) => setInformationOwnerFieldValue(val)}
                        onAddTeamMember={(value) => handleInformationOwner(field, value)}
                        btnText="Add User"
                        searchTerm={informationOwnerSearchTerm}
                        setSearchTerm={(value) => setInformationOwnerSearchTerm(value)}
                        showUserDetails={false}
                        setShowUserDetails={() => {}}
                      />
                    )}
                  />
                  <span className={classNames('error-message')}>{errors.informationOwner?.message}</span>
                </div>
              </div>
            )}
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.name ? 'error' : '')}>
                <Controller
                  control={control}
                  name="name"
                  rules={{ required: '*Missing entry' }}
                  render={({ field }) => (
                    <TeamSearch
                      label={
                        <>
                          Point of contact for data transfer e.g. Data Steward <sup>*</sup>
                        </>
                      }
                      fieldMode={true}
                      fieldValue={fieldValue}
                      setFieldValue={(val) => setFieldValue(val)}
                      onAddTeamMember={(value) => handleName(field, value)}
                      btnText="Add User"
                      searchTerm={searchTerm}
                      setSearchTerm={(value) => setSearchTerm(value)}
                      showUserDetails={false}
                      setShowUserDetails={() => {}}
                    />
                  )}
                />
                <span className={classNames('error-message')}>{errors.name?.message}</span>
              </div>
              <div className={classNames(Styles.flexLayout)}>
                <div className={Styles.divisionContainer}>
                  <div
                    className={classNames(
                      `input-field-group include-error ${errors?.division?.message ? 'error' : ''}`,
                    )}
                  >
                    <label id="divisionLabel" htmlFor="divisionField" className="input-label">
                      Division <sup>*</sup>
                    </label>
                    <div className="custom-select" onBlur={() => trigger('division')}>
                      <select
                        id="divisionField"
                        required={true}
                        required-error={'*Missing entry'}
                        {...register('division', {
                          validate: (value) => value !== '0' || '*Missing entry',
                        })}
                      >
                        <option id="divisionOption" value={0}>
                          Choose
                        </option>
                        {divisions?.map((obj) => (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                            {obj.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <span className={classNames('error-message')}>{errors?.division?.message}</span>
                  </div>
                </div>
                <div className={Styles.subDivisionContainer}>
                  <div className={classNames('input-field-group')}>
                    <label id="subDivisionLabel" htmlFor="subDivisionField" className="input-label">
                      Sub Division
                    </label>
                    <div className="custom-select">
                      <select id="subDivisionField" {...register('subDivision')}>
                        {subDivisions?.some((item) => item.id === '0' && item.name === 'None') ? (
                          <option id="subDivisionDefault" value={0}>
                            None
                          </option>
                        ) : (
                          <>
                            <option id="subDivisionDefault" value={0}>
                              Choose
                            </option>
                            {subDivisions?.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                {obj.name}
                              </option>
                            ))}
                          </>
                        )}
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.department ? 'error' : '')}>
                <Controller
                  control={control}
                  name="department"
                  rules={{ required: '*Missing entry' }}
                  render={({ field }) => (
                    <Tags
                      title={'Department'}
                      placeholder={'Choose from one of the existing or add new department'}
                      max={1}
                      chips={selectedDepartment}
                      tags={departments}
                      setTags={(selectedTags) => {
                        let dept = selectedTags?.map((item) => item.toUpperCase());
                        setSelectedDepartment(dept);
                        field.onChange(dept);
                      }}
                      isMandatory={true}
                      showMissingEntryError={errors.department?.message}
                    />
                  )}
                />
              </div>
              {isDataProduct ? (
                <div className={classNames('input-field-group include-error', errors.dateOfDataProduct ? 'error' : '')}>
                  <label id="dateOfDataProductLabel" htmlFor="dateOfDataProductrInput" className="input-label">
                    Publish Date of Data Product <sup>*</sup>
                  </label>
                  <Controller
                    control={control}
                    name="dateOfDataProduct"
                    rules={{
                      validate: validateDate,
                    }}
                    render={({ field }) => (
                      <DatePicker
                        label="Publish Date of Data Product"
                        value={watch('dateOfDataProduct')}
                        name={field.name}
                        // minDate={minDate}
                        onChange={(value) => {
                          field.onChange(value);
                        }}
                        requiredError={errors.dateOfDataProduct?.message}
                      />
                    )}
                  />
                  <span className={classNames('error-message')}>{errors.dateOfDataProduct?.message}</span>
                </div>
              ) : (
                null
              )}
            </div>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.complianceOfficer ? 'error' : '')}>
                <Controller
                  control={control}
                  name="complianceOfficer"
                  rules={{ required: '*Missing entry' }}
                  render={({ field }) => (
                    <TypeAheadBox
                      label={<>Corresponding Compliance Contact, i.e. Local Compliance Officer/ Responsible or Multiplier <a className='info' target="_blank" href='#/data/datacompliancenetworklist'><i className="icon mbc-icon info" tooltip-data="Click to view LCO/LCR Contacts" /></a></>}
                      placeholder={'Search for country, department etc.'}
                      defaultValue={complianceOfficer}
                      list={complianceOfficerList.records}
                      setSelected={(selectedTags) => {
                        setComplianceOfficer(
                          (selectedTags.localComplianceOfficer !== undefined &&
                            selectedTags.localComplianceOfficer
                              .concat(selectedTags.localComplianceResponsible)
                              .join(', ')) ||
                            [],
                        );
                        field.onChange(
                          selectedTags.localComplianceOfficer !== undefined &&
                            selectedTags.localComplianceOfficer
                              .concat(selectedTags.localComplianceResponsible)
                              .join(', '),
                        );
                      }}
                      required={true}
                      showError={errors.complianceOfficer?.message}
                      render={(item) => (
                        <div className={Styles.optionContainer}>
                          <span className={Styles.optionText}>Entity ID: {item?.entityId}</span>
                          <span className={Styles.optionText}>Entiry Name: {item?.entityName}</span>
                          <span className={Styles.optionText}>
                            LCO/LCR: {item?.localComplianceOfficer.concat(item?.localComplianceResponsible).join(', ')}
                          </span>
                        </div>
                      )}
                    />
                  )}
                />
              </div>
              <div className={classNames('input-field-group')}>
                <Controller
                  control={control}
                  name="planningIT"
                  render={({ field }) => (
                    <TypeAheadBox
                      label={'planningIT App-ID'}
                      placeholder={'Select App-ID (Enter minimum 4 characters)'}
                      defaultValue={selectedPlanningIT}
                      list={planningITList}
                      setSelected={(selectedTags) => {
                        setSelectedPlanningIT(selectedTags.id || []);
                        field.onChange(selectedTags.id);
                      }}
                      onInputChange={handlePlanningITSearch}
                      required={false}
                      showError={false}
                      render={(item) => (
                        <div className={Styles.optionContainer}>
                          <div>
                            <span className={Styles.optionText}>
                              {item?.id} {item.shortName ? `(${item?.shortName})` : null}
                            </span>
                            <span className={Styles.suggestionListBadge}>{item?.providerOrgShortname}</span>
                          </div>
                          <span className={Styles.optionText}>{item?.name}</span>
                        </div>
                      )}
                    />
                  )}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      {/* <div className="btnContainer">
        <button
          className="btn btn-primary"
          type="submit"
          disabled={isSubmitting}
          onClick={handleSubmit((values) => {
            const isPublished = watch('publish');
            setValue('notifyUsers', isPublished ? true : false);
            onSave(watch());
            reset(values, {
              keepDirty: false,
            });
          })}
        >
          Save & Next
        </button>
      </div> */}
      {showInfoModal && (
        <InfoModal
          title="Info Modal"
          show={showInfoModal}
          hiddenTitle={true}
          content={<div>Sample Info Modal</div>}
          onCancel={() => setShowInfoModal(false)}
        />
      )}
    </>
  );
};

export default ContactInformation;
