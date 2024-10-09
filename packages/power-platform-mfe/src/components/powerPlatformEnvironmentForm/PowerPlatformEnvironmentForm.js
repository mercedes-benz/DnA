import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider, Controller } from 'react-hook-form';
import { useSelector } from 'react-redux';
// styles
import Styles from './power-platform-environment-form.scss';
// import from DNA Container
import Tags from 'dna-container/Tags';
import Modal from 'dna-container/Modal';
import AddUser from 'dna-container/AddUser';
import SelectBox from 'dna-container/SelectBox';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Tooltip from '../../../../chronos-mfe/src/common/modules/uilab/js/src/tooltip';
import SharedDevelopmentTou from '../sharedDevelopmentTou/SharedDevelopmentTou';
// Api
import { powerPlatformApi } from '../../apis/power-platform.api';

const PowerPlatformEnvironmentForm = ({ user, onCreateAccount }) => {

  const { departments } = useSelector(state => state.lovs);
  
  const [showTou, setShowTou] = useState(false);
  
  const methods = useForm({ 
    defaultValues: {
      name: '',
      subscriptionType: '',
      environment: 'shared-dev',
      envOwnerName: '',
      envOwnerId: '',
      dyEnvOwnerName: '',
      dyEnvOwnerId: '',
      department: [],
      billingContact: '',
      billingPlant: '',
      billingCostCentre: '',
      customRequirements: '',
      prodEnvAvailability: 'later',
      termsOfUse: false,
    }
  });
  const {
    register,
    handleSubmit,
    getValues,
    setValue,
    control,
    formState: { errors },
  } = methods;

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, []);

  const [userLincenses, setUserLicenses] = useState([]);

  const handleTouAccept = () => {
    setShowTou(false);
    setValue('termsOfUse', true);
  }

  const getDevelopers = (developer) => {
    const userLicenseData = {
      userDetails: {
        id: developer.shortId,
        userId: developer.shortId,
        firstName: developer.firstName,
        lastName: developer.lastName,
      },
      license: ''
    };

    let duplicateMember = false;
    duplicateMember = userLincenses?.filter((license) => license.userId === developer.userId)?.length ? true : false;

    const isCreator = user?.id === developer?.userId;

    if (duplicateMember) {
        Notification.show('User License already added.', 'warning');
    } else if (isCreator) {
        Notification.show(
            `${developer.givenName} ${developer.surName} is a creator. Creator can't be added to user lincense.`,
            'warning',
        );
    } else {
        userLincenses?.push(userLicenseData);
        setUserLicenses([...userLincenses]);
    }
  }

  const onUserLicenseClick = (value, userId) => {
    const updatedUserLincenses = userLincenses.map(userLicense => {
      if (userLicense?.userDetails?.userId === userId) {
        return Object.assign({}, userLicense, { license: value });
      }
      return userLicense;
    });
    setUserLicenses(updatedUserLincenses);
  };

  const onUserLicenseDelete = (userId) => {
    return () => {
      const updatedUserLicenses = userLincenses.filter((userLicense) => {
        return userLicense?.userDetails?.userId !== userId;
      });
      setUserLicenses(updatedUserLicenses);
    };
  };

  const formValues = (values) => {
    return {
      name: values?.name,
      envOwnerName: values?.envOwnerName,
      envOwnerId: values?.envOwnerId,
      dyEnvOwnerName: values?.dyEnvOwnerName,
      dyEnvOwnerId: values?.dyEnvOwnerId,
      department: values?.department,
      billingContact: values?.billingContact,
      billingPlant: values?.billingPlant,
      billingCostCentre: values?.billingCostCentre,
      customRequirements: values?.customRequirements,
      prodEnvAvailability: values?.prodEnvAvailability,
      developers: userLincenses
    }
  }

  const handleCreateProject = (values) => {
    ProgressIndicator.show();
    const data = formValues(values);
    powerPlatformApi.createPowerPlatformEnvironment(data).then(() => {
      ProgressIndicator.hide();
      // history.push(`/project/${res.data.data.id}`);
      onCreateAccount();
      Notification.show('Shared Developer Account created successfully created');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while creating shared developer account',
        'alert',
      );
    });
  };

  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames(Styles.form)}>
          <div className={Styles.formHeader}>
            <h3>Power Platform: Shared Development Account</h3>
            <p>Enter following information to start!</p>
          </div>
          <div className={Styles.flex}>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
                <label className={'input-label'}>
                  Name of Project <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="name"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('name', { required: '*Missing entry', pattern: /^(?!Admin monitoring$)(?!^\s+$)[\w\d -]+$/ })}
                />
                <span className={'error-message'}>{errors?.name?.message}{errors.name?.type === 'pattern' && 'Project names must contain characters only - is allowed. Admin monitoring name is not allowed.'}</span>
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Environment <sup>*</sup>
                </label>
                <div className={classNames('custom-select')}>
                  <select {...register('environment')}>
                    <option value={'shared-dev'}>Shared Development</option>
                    <option value={'shared-int'}>Shared Integration</option>
                    <option value={'dedicated-prod'}>Dedicated Production</option>
                  </select>
                </div>
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Environment Owner Name
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="envOwnerName"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('envOwnerName')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.envOwnerId ? 'error' : '')}>
                <label className={'input-label'}>
                  Environment Owner User ID <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="envOwnerId"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('envOwnerId', {required: '*Missing entry'})}
                />
                <span className={'error-message'}>{errors?.envOwnerId?.message}</span>
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Deputy Environment Owner Name
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="dyEnvOwnerName"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('dyEnvOwnerName')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Deputy Environment Owner User ID
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="dyEnvOwnerId"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('dyEnvOwnerId')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <Controller
                  control={control}
                  name="department"
                  rules={{
                    validate: (value) => {
                      return value === undefined || value.length !== 0 || '*Missing entry';
                    },
                  }}
                  render={({ field }) => (
                    <Tags
                      title={'Department'}
                      value={getValues('department')}
                      name={field.name}
                      max={1}
                      chips={getValues('department')}
                      tags={departments}
                      setTags={(selectedTags) => {
                        field.onChange(selectedTags);
                      }}
                      isMandatory={true}
                      showMissingEntryError={errors.department}
                    />
                  )}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Billing Contact
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="billingContact"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('billingContact')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.billingPlant ? 'error' : '')}>
                <label className={'input-label'}>
                  Billing Plant <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="billingPlant"
                  placeholder="Example 020"
                  autoComplete="off"
                  maxLength={256}
                  {...register('billingPlant', { required: '*Missing entry' })}
                />                
                {/* <span className={'error-message'}>{errors?.billingPlant?.message}{errors.billingPlant?.type === 'pattern' && 'Please enter 3 digit billing plant code'}</span> */}
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.billingCostCentre ? 'error' : '')}>
                <label className={'input-label'}>
                  Billing Cost Center <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="billingCostCentre"
                  placeholder="Example 000-1234"
                  autoComplete="off"
                  maxLength={256}
                  {...register('billingCostCentre', { required: '*Missing entry' })}
                />
                {/* <span className={'error-message'}>{errors?.billingCostCentre?.message}{errors.billingCostCentre?.type === 'pattern' && 'Please enter valid billing cost center code'}</span> */}
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error area', errors.customRequirements ? 'error' : '')}>
                <label id="customRequirements" className="input-label" htmlFor="customRequirements">
                  Custom Requirements
                </label>
                <textarea
                  id="customRequirements"
                  className={'input-field-area'}
                  type="text"
                  rows={50}
                  {...register('customRequirements', { pattern: /^(?!\s+$)(\s*\S+\s*)+$/ })}
                />
                <span className={'error-message'}>{errors?.customRequirements?.message}{errors.customRequirements?.type === 'pattern' && `Spaces (and special characters) not allowed as field value.`}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', errors?.prodEnvAvailability?.message ? 'error' : '')}>
                <label className={'input-label'}>
                  Do you want the PROD environment immediately or later? <sup>*</sup>
                </label>
                <div className={Styles.pIIField}>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value={'immediate'}
                        {...register('prodEnvAvailability', {
                          required: '*Missing entry'
                        })}
                      />
                    </span>
                    <span className="label">Immediately</span>
                  </label>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value={'later'}
                        {...register('prodEnvAvailability', {
                          required: '*Missing entry'
                        })}
                      />
                    </span>
                    <span className="label">Later</span>
                  </label>
                </div>
                <span className={classNames('error-message', errors?.prodEnvAvailability?.message ? '' : 'hide')}>
                  {errors?.prodEnvAvailability?.message}
                </span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error')}>
                <AddUser getCollabarators={getDevelopers} isRequired={false} isUserprivilegeSearch={false} title={'User Licenses to Add'} />
              </div>
              {userLincenses?.length === 0 &&
                <div className={Styles.noLincense}>
                  <p>No User License Selected</p>
                </div>
              }
              <div>
                {userLincenses?.length > 0 && (
                  <>
                    <div className={Styles.colHeader}>
                        <div className={Styles.column1}>User ID</div>
                        <div className={Styles.column2}>Name</div>
                        <div className={Styles.column3}>License</div>
                        <div className={Styles.column4}></div>
                    </div>
                    <div>
                        {userLincenses?.map((userLicense) => {
                          return (
                              <div key={userLicense?.userDetails?.userId} className={Styles.userRow}>
                                  <div className={Styles.column1}>
                                    <p>{userLicense?.userDetails?.userId}</p>
                                  </div>
                                  <div className={Styles.column2}>
                                    <p>{userLicense?.userDetails?.firstName + ' ' + userLicense?.userDetails?.lastName}</p>
                                  </div>
                                  <div className={classNames(Styles.column3, Styles.lincenseContainer)}>
                                    <div className={classNames(Styles.licenseRadio)}>
                                      <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            name={userLicense?.userDetails?.userId}
                                            value="Power-Virtual-Agent-User"
                                            onChange={() => onUserLicenseClick("Power-Virtual-Agent-User", userLicense?.userId)}
                                          />
                                        </span>
                                        <span>Power Virtual Agent User</span>
                                      </label>
                                    </div>
                                    <div className={classNames(Styles.licenseRadio)}>
                                      <label className={'checkbox'}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            name={userLicense?.userDetails?.userId}
                                            value="Power-Automate-Premium"
                                            onChange={() => onUserLicenseClick('Power-Automate-Premium', userLicense?.userDetails?.userId)}
                                          />
                                        </span>
                                        <span>Power Automate Premium</span>
                                      </label>
                                    </div>
                                    <div className={classNames(Styles.licenseRadio)}>
                                      <label className={'checkbox'}>
                                        <span className="wrapper">
                                          <input
                                            type="radio"
                                            className="ff-only"
                                            name={userLicense?.userDetails?.userId}
                                            value="Power-Apps-Premium-User"
                                            onChange={() => onUserLicenseClick('Power-Apps-Premium-User', userLicense?.userDetails?.userId)}
                                          />
                                        </span>
                                        <span>Power Apps Premium User</span>
                                      </label>
                                    </div>
                                  </div>
                                  <div className={Styles.column4}>
                                    <div className={Styles.deleteEntry} onClick={onUserLicenseDelete(userLicense?.userDetails?.userId)}>
                                      <i className="icon mbc-icon trash-outline" tooltip-data={'Delete'} />
                                    </div>
                                  </div>
                              </div>
                          );
                      })}
                    </div>
                  </>
                )}
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames(errors?.termsOfUse?.message ? 'error' : '')}>
                <div className={Styles.termsOfUseContent}>
                  <div>
                    <label className={classNames('checkbox', errors?.termsOfUse?.message ? 'error' : '')}>
                      <span className="wrapper">
                        <input
                          type="checkbox"
                          className="ff-only"
                          {...register('termsOfUse', {
                            required: 'Please agree to terms of use'
                          })}
                        />
                      </span>
                    </label>
                  </div>
                  <div
                    className={classNames(Styles.termsOfUseText)}
                    style={{
                        ...(errors?.termsOfUse?.message ? { color: '#e84d47' } : ''),
                    }}
                  >
                    <div>Accept <span onClick={() => setShowTou(true)}>terms of use</span></div>
                    <sup>*</sup>
                  </div>
                </div>
                <span
                  style={{ marginTop: 0 }}
                  className={classNames('error-message', errors?.termsOfUse?.message ? '' : 'hide')}
                >
                  {errors?.termsOfUse?.message}
                </span>
              </div>
            </div>
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit((values) => {
                handleCreateProject(values);
              })}
            >
              Order Account
            </button>
          </div>
        </div>
      </FormProvider>
      { showTou &&
        <Modal
          title={'Terms of Use'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={showTou}
          content={<SharedDevelopmentTou onAccept={handleTouAccept} />}
          scrollableContent={true}
          onCancel={() => setShowTou(false)}
        />
      }
    </>
  );
}

export default PowerPlatformEnvironmentForm;