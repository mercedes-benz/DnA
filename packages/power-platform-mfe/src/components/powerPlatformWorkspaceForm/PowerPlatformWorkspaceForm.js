import classNames from 'classnames';
import React, { useState } from 'react';
import { useForm, FormProvider, Controller } from 'react-hook-form';
import { useHistory } from "react-router-dom";
import { useSelector } from 'react-redux';
// styles
import Styles from './power-platform-workspace-form.scss';
// import from DNA Container
import Tags from 'dna-container/Tags';
import Modal from 'dna-container/Modal';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import SharedDevelopmentTou from '../sharedDevelopmentTou/SharedDevelopmentTou';
// Api
import { powerPlatformApi } from '../../apis/power-platform.api';

const PowerPlatformWorkspaceForm = () => {
  let history = useHistory();

  const { departments } = useSelector(state => state.lovs);
  
  const [showTou, setShowTou] = useState(false);
  
  const methods = useForm({ 
    defaultValues: {
      name: '',
      environmentOwnerName: '',
      environmentOwnerUserId: '',
      deputyEnvironmentOwnerName: '',
      deputyEnvironmentOwnerUserId: '',
      department: [],
      billingContact: '',
      billingPlant: '',
      billingCostCenter: '',
      customRequirements: '',
      isImmediate: 'false',
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

  const handleTouAccept = () => {
    setShowTou(false);
    setValue('termsOfUse', true);
  }

  const formValues = (values) => {
    return {
      name: values?.name,
      environmentOwnerName: values?.environmentOwnerName,
      environmentOwnerUserId: values?.environmentOwnerUserId,
      deputyEnvironmentOwnerName: values?.deputyEnvironmentOwnerName,
      deputyEnvironmentOwnerUserId: values?.deputyEnvironmentOwnerUserId,
      department: values?.department,
      billingContact: values?.billingContact,
      billingPlant: values?.billingPlant,
      billingCostCenter: values?.billingCostCenter,
      customRequirements: values?.customRequirements,
      isImmediate: values?.isImmediate,
      termsOfUse: values?.termsOfUse ? true : false,
    }
  }

  const handleCreateProject = (values) => {
    ProgressIndicator.show();
    const data = formValues(values);
    powerPlatformApi.createPowerPlatformWorkspace(data).then((res) => {
      ProgressIndicator.hide();
      history.push(`/project/${res.data.data.id}`);
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
            <div className={Styles.col}>
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
                  Environment Owner Name
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="environmentOwnerName"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('environmentOwnerName')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.environmentOwnerUserId ? 'error' : '')}>
                <label className={'input-label'}>
                  Environment Owner User ID <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="environmentOwnerUserId"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('environmentOwnerUserId', {required: '*Missing entry'})}
                />
                <span className={'error-message'}>{errors?.environmentOwnerUserId?.message}</span>
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
                  id="deputyEnvironmentOwnerName"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('deputyEnvironmentOwnerName')}
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
                  id="deputyEnvironmentOwnerUserId"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('deputyEnvironmentOwnerUserId')}
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
                  maxLength={3}
                  {...register('billingPlant', { required: '*Missing entry', pattern: /^\d{3}$/ })}
                />                
                <span className={'error-message'}>{errors?.billingPlant?.message}{errors.billingPlant?.type === 'pattern' && 'Please enter 3 digit billing plant code'}</span>
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.billingCostCenter ? 'error' : '')}>
                <label className={'input-label'}>
                  Billing Cost Center <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="billingCostCenter"
                  placeholder="Example 000-1234"
                  autoComplete="off"
                  maxLength={9}
                  {...register('billingCostCenter', { required: '*Missing entry', pattern: /^\d{3}-\d{4}$/ })}
                />
                <span className={'error-message'}>{errors?.billingCostCenter?.message}{errors.billingCostCenter?.type === 'pattern' && 'Please enter valid billing cost center code'}</span>
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
              <div className={classNames('input-field-group include-error', errors?.isImmediate?.message ? 'error' : '')}>
                <label className={'input-label'}>
                  Do you want the PROD environment immediately or later? <sup>*</sup>
                </label>
                <div className={Styles.pIIField}>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value={'true'}
                        {...register('isImmediate', {
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
                        value={'false'}
                        {...register('isImmediate', {
                          required: '*Missing entry'
                        })}
                      />
                    </span>
                    <span className="label">Later</span>
                  </label>
                </div>
                <span className={classNames('error-message', errors?.isImmediate?.message ? '' : 'hide')}>
                  {errors?.isImmediate?.message}
                </span>
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

export default PowerPlatformWorkspaceForm;