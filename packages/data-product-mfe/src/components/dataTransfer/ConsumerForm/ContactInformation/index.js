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
import { dataTransferApi } from '../../../../apis/datatransfers.api';

import ProgressIndicator from '../../../../common/modules/uilab/js/src/progress-indicator';
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
import { useSelector } from 'react-redux';
import { debounce } from 'lodash';

const ContactInformation = ({
  onSave,
  divisions,
  setSubDivisions,
  subDivisions,
  isFormMounted,
  isDataProduct = false,
}) => {
  const {
    register,
    formState: { errors, isSubmitting, dirtyFields },
    watch,
    handleSubmit,
    trigger,
    reset,
    control,
    setValue,
    getValues,
    clearErrors,
  } = useFormContext();
  const [showInfoModal, setShowInfoModal] = useState(false);

  const {
    division,
    department,
    complianceOfficer: selectedcomplianceOfficer,
    businessOwnerName,
    planningIT,
    lcoNeeded,
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

  const provideDataTransfers = useSelector((state) =>
    !isDataProduct ? state.provideDataTransfers : state.dataProduct,
  );

  useEffect(() => {
    if (lcoNeeded === 'No') {
      setComplianceOfficer([]);
      setValue('complianceOfficer', null);
      clearErrors('complianceOfficer');
    }
  }, [lcoNeeded, setValue, clearErrors]);

  useEffect(() => {
    const id = watch('division');
    if (id > '0') {
      ProgressIndicator.show();
      hostServer.get('/subdivisions/' + id).then((res) => {
        setSubDivisions(res?.data || []);
        if (!dirtyFields.division && !dirtyFields.subDivision) {
          setValue('subDivision', provideDataTransfers.selectedDataTransfer.consumer.subDivision);
        } else {
          setValue('subDivision', '0');
        }
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
      });
    } else {
      setSubDivisions([]);
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [division]);

  useEffect(() => {
    reset(watch());
    Tooltip.defaultSetup();
    SelectBox.defaultSetup();
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    if (isFormMounted) {
      ProgressIndicator.show();
      dataTransferApi
        .getDataComplianceList(0, 0, 'entityId', 'asc')
        .then((res) => {
          res.data?.records?.map((item) => {
            const localComplianceOfficers = item.localComplianceOfficer.join(', ');
            const localComplianceResponsibles = item.localComplianceResponsible.join(', ');
            item['name'] = item.entityId + ' ' + item.entityName + ' ' + localComplianceOfficers + ' ' + localComplianceResponsibles;
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
    }
    //eslint-disable-next-line
  }, [isFormMounted]);

  useEffect(() => {
    // if division is set to "Choose", reset sub divison dropdown to empty list
    division === '0' && SelectBox.defaultSetup();
  }, [division, subDivisions.length]);

  useEffect(() => {
    if (selectedcomplianceOfficer?.length) {
      if (lcoNeeded === 'No') {
        setComplianceOfficer([]);
      } else {
        setComplianceOfficer(selectedcomplianceOfficer);
      }
    }
  }, [selectedcomplianceOfficer, lcoNeeded]);

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
    let nameStr =
      typeof businessOwnerName === 'string'
        ? businessOwnerName
        : `${businessOwnerName?.firstName} ${businessOwnerName?.lastName}`;
    businessOwnerName && setFieldValue(nameStr);
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [businessOwnerName]);

  useEffect(() => {
    if (planningIT?.length) {
      setSelectedPlanningIT(planningIT);
    }
  }, [planningIT]);

  const handleBusinessOwner = (field, value) => {
    let name = '';
    if (value) {
      value['addedByProvider'] = true;
      name = `${value.firstName} ${value.lastName}`;
    }
    field.onChange(value);
    setFieldValue(name);
  };

  const validateDate = () => {
    const value = getValues('dateOfDataTransfer');
    if (typeof value === 'object') {
      const isValidDate = !isNaN(value?.get('date'));
      const error =
        value === null || value === ''
          ? '*Missing entry'
          : !isValidDate
          ? 'Invalid Date Format'
          : null;
      return (value === isValidDate && value) || error;
    } else {
      return (value !== '' && value !== undefined) || '*Missing entry';
    }
  };

  const isLCORequired = (value) =>
    watch('lcoNeeded') === 'No' || (watch('lcoNeeded') === 'Yes' && value?.length > 0) || '*Missing entry';

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
                <i className={'icon mbc-icon info'} onClick={() => setShowInfoModal(true)} />
              </div>
            )}
          </div>
          <div className={Styles.formWrapper}>
            {!!isDataProduct && (
              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors.dataTransferName ? 'error' : '')}>
                  <label id="dataTransferNameLabel" htmlFor="dataTransferNameInput" className="input-label">
                    Short description of the data transfer <sup>*</sup>
                  </label>
                  <div className={Styles.dataTransferName}>
                    <span>{`${provideDataTransfers?.selectedDataProduct?.productName}`}</span>
                    <input
                      {...register('dataTransferName', {
                        required: '*Missing entry',
                      })}
                      type="text"
                      className="input-field"
                      id="dataTransferNameInput"
                      maxLength={200}
                      placeholder="Type here"
                      autoComplete="off"
                    />
                  </div>
                  <span className={classNames('error-message')}>{errors.dataTransferName?.message}</span>
                </div>
              </div>
            )}
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.businessOwnerName ? 'error' : '')}>
                <Controller
                  control={control}
                  name="businessOwnerName"
                  rules={{ required: '*Missing entry' }}
                  render={({ field }) => (
                    <TeamSearch
                      label={
                        <>
                          Responsible Manager (E3 +) <sup>*</sup>
                        </>
                      }
                      fieldMode={true}
                      fieldValue={fieldValue}
                      setFieldValue={(val) => setFieldValue(val)}
                      onAddTeamMember={(value) => handleBusinessOwner(field, value)}
                      btnText="Add User"
                      searchTerm={searchTerm}
                      setSearchTerm={(value) => setSearchTerm(value)}
                      showUserDetails={false}
                      setShowUserDetails={() => {}}
                    />
                  )}
                />
                <span className={classNames('error-message')}>{errors.businessOwnerName?.message}</span>
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
              <div className={Styles.flexLayout}>
                <div
                  className={classNames('input-field-group include-error', errors.dateOfDataTransfer ? 'error' : '')}
                >
                  <label id="dateOfDataTransferLabel" htmlFor="dateOfDataTransferInput" className="input-label">
                    Date of Data Transfer <sup>*</sup>
                  </label>
                  <Controller
                    control={control}
                    name="dateOfDataTransfer"
                    rules={{
                      validate: validateDate,
                    }}
                    render={({ field }) => (
                      <DatePicker
                        label="Date of Data Transfer"
                        value={watch('dateOfDataTransfer')}
                        name={field.name}
                        // minDate={minDate}
                        onChange={(value) => {
                          field.onChange(value);
                        }}
                        requiredError={errors.dateOfDataTransfer?.message}
                      />
                    )}
                  />
                  <span className={classNames('error-message')}>{errors.dateOfDataTransfer?.message}</span>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div
                className={classNames(`input-field-group include-error ${errors?.lcoNeeded ? 'error' : ''}`)}
                style={{ minHeight: '50px' }}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Is LCO/LCR needed to be involved / has to check legal basis of usage of personal data? <sup>*</sup>
                </label>
                <div className={Styles.radioBtns}>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('lcoNeeded', {
                          required: '*Missing entry',
                        })}
                        type="radio"
                        className="ff-only"
                        name="lcoNeeded"
                        value="No"
                      />
                    </span>
                    <span className="label">No</span>
                  </label>
                  <label className={'radio'}>
                    <span className="wrapper">
                      <input
                        {...register('lcoNeeded', { required: '*Missing entry' })}
                        type="radio"
                        className="ff-only"
                        name="lcoNeeded"
                        value="Yes"
                      />
                    </span>
                    <span className="label">Yes</span>
                  </label>
                </div>
                <span className={classNames('error-message')}>{errors?.lcoNeeded?.message}</span>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div
                className={classNames(
                  'input-field-group include-error',
                  lcoNeeded && errors.complianceOfficer ? 'error' : '',
                  lcoNeeded === 'No' || !lcoNeeded ? 'disabled' : '',
                )}
              >
                <Controller
                  control={control}
                  name="complianceOfficer"
                  rules={{ validate: isLCORequired }}
                  render={({ field }) => (
                    <TypeAheadBox
                      label={<>Corresponding Compliance Contact, i.e. Local Compliance Officer/ Responsible or Multiplier <a className='info' target="_blank" href='#/data/datacompliancenetworklist'><i className="icon mbc-icon info" tooltip-data="Click to view LCO/LCR Contacts" /></a></>}
                      placeholder={'Search for country, department etc.'}
                      defaultValue={complianceOfficer}
                      list={complianceOfficerList.records}
                      setSelected={(selectedTags) => {
                        setComplianceOfficer(selectedTags.localComplianceOfficer !== undefined && selectedTags.localComplianceOfficer.concat(selectedTags.localComplianceResponsible).join(', ') || []);
                        field.onChange(selectedTags.localComplianceOfficer !== undefined && selectedTags.localComplianceOfficer.concat(selectedTags.localComplianceResponsible).join(', '));
                      }}
                      required={watch('lcoNeeded') === 'Yes'}
                      showError={errors.complianceOfficer?.message}
                      render={(item) => (
                        <div className={Styles.optionContainer}>
                          <span className={Styles.optionText}>Entity ID: {item?.entityId}</span>
                          <span className={Styles.optionText}>Entiry Name: {item?.entityName}</span>
                          <span className={Styles.optionText}>LCO: {item?.localComplianceOfficer.concat(item?.localComplianceResponsible).join(', ')}</span>
                        </div>
                      )}
                      disabled={lcoNeeded === 'No' || !lcoNeeded}
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
                      showError={errors.planningIT?.message}
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
      <div className="btnContainer">
        <button
          className="btn btn-primary"
          type="submit"
          disabled={isSubmitting}
          onClick={handleSubmit(() => {
            const isPublished = watch('publish');
            setValue('notifyUsers', isPublished ? true : false);
            onSave(watch());
            reset(watch(), {
              keepDirty: false,
            });
          })}
        >
          Save & Next
        </button>
      </div>
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
