import classNames from 'classnames';
import React from 'react';
import { useForm, FormProvider, Controller } from 'react-hook-form';
import { useHistory } from "react-router-dom";
import { useSelector } from 'react-redux';
// styles
import Styles from './power-platform-workspace-form.scss';
// import from DNA Container
import Tags from 'dna-container/Tags';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// Utils
import { Envs } from '../../utilities/envs';
// Api
import { powerPlatformApi } from '../../apis/power-platform.api';

const PowerPlatformWorkspaceForm = ({ project, edit, onSave }) => {
  let history = useHistory();

  const { departments } = useSelector(state => state.lovs);
  
  const methods = useForm({ 
    defaultValues: { 
      name: edit && project?.name !== null ? project?.name : '',
      dcustomRequirements: edit && project?.customRequirements ? project?.customRequirements : '',
      department: edit && project?.department ? [project?.department] : [],
      hasPii: edit && project?.hasPii ? project?.hasPii?.toString() : 'false',
      termsOfUse: edit && project?.termsOfUse ? project?.termsOfUse : false,
    }
  });
  const {
    register,
    handleSubmit,
    getValues,
    control,
    formState: { errors },
  } = methods;

  const formValues = (values) => {
    return {
      typeOfProject: values?.typeOfProject,
      name: values.name.trim(),
      customRequirements: values?.customRequirements.trim(),
      department: values?.department,
      hasPii: values?.hasPii,
      termsOfUse: values?.termsOfUse ? true : false,
    }
  }

  const handleCreateProject = (values) => {
    ProgressIndicator.show();
    const data = formValues(values);
    powerPlatformApi.createDataEntryProject(data).then((res) => {
      ProgressIndicator.hide();
      history.push(`/project/${res.data.data.id}`);
      Notification.show('Data Entry Project successfully created');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while creating data entry project',
        'alert',
      );
    });
  };
  const handleEditProject = (values) => {
    const data = formValues(values);
    ProgressIndicator.show();
    powerPlatformApi.updateDataEntryProject(project.id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Data Entry project successfully updated');
      onSave();
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while updating data entry project',
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
                  id="workspaceName"
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
                  id="akfield"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('akfield')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Environment Owner User ID
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="akfield"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('akfield')}
                />
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
                  id="akfield"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('akfield')}
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
                  id="akfield"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('akfield')}
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
                  id="akfield"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('akfield')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Billing Plant
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="akfield"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('akfield')}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <label className={'input-label'}>
                  Billing Cost Center
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="akfield"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  {...register('akfield')}
                />
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
              <div className={classNames('input-field-group include-error', errors?.hasPii?.message ? 'error' : '')}>
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
                        {...register('hasPii', {
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
                        {...register('hasPii', {
                          required: '*Missing entry'
                        })}
                      />
                    </span>
                    <span className="label">Later</span>
                  </label>
                </div>
                <span className={classNames('error-message', errors?.hasPii?.message ? '' : 'hide')}>
                  {errors?.hasPii?.message}
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
                    <div dangerouslySetInnerHTML={{ __html: Envs.TOU_HTML }}></div>
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
                edit ? handleEditProject(values) : handleCreateProject(values);
              })}
            >
              Order Account
            </button>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default PowerPlatformWorkspaceForm;