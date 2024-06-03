import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider, Controller } from 'react-hook-form';
import { useHistory } from "react-router-dom";
import { useDispatch, useSelector } from 'react-redux';
// styles
import Styles from './power-platform-workspace-form.scss';
// import from DNA Container
import Tags from 'dna-container/Tags';
import SelectBox from 'dna-container/SelectBox';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// Utils
import { Envs } from '../../utilities/envs';
// Api
import { powerPlatformApi } from '../../apis/power-platform.api';
import { getSubDivisions, resetSubDivisions } from '../../redux/lovsSlice';

const PowerPlatformWorkspaceForm = ({ project, edit, onSave }) => {
  let history = useHistory();

  const dispatch = useDispatch();
  const { divisions, subDivisions, departments, classifications, loading, tagsLov } = useSelector(state => state.lovs);
  
  const methods = useForm({ 
    defaultValues: { 
      typeOfProject: edit && project?.typeOfProject ? project?.typeOfProject : '0',
      name: edit && project?.name !== null ? project?.name : '',
      description: edit && project?.decription ? project?.decription : '',
      division: edit ? (project?.divisionId ? project?.divisionId + '@-@' + project?.division : '0') : '0',
      subDivision: '0',
      department: edit && project?.department ? [project?.department] : '',
      dataClassification: edit && project?.dataClassification ? project?.dataClassification : '0',
      hasPii: edit && project?.hasPii ? project?.hasPii?.toString() : 'false',
      archerId: edit && project?.archerId ? project?.archerId : '',
      procedureId: edit && project?.procedureId ? project?.procedureId : '',
      termsOfUse: edit && project?.termsOfUse ? project?.termsOfUse : false,
    }
  });
  const {
    register,
    handleSubmit,
    getValues,
    setValue,
    watch,
    control,
    formState: { errors },
  } = methods;
  const selectedDivision = watch('division');
  const selectedSubDivision = watch('subDivision');
  const selectedTypeOfProject = watch('typeOfProject');

  // lean governance fields
  const [departmentName, setDepartmentName] = useState(edit && project?.department ? [project?.department] : []);
  const [departmentError, setDepartmentError] = useState(false);
  const [tags, setTags] = useState(edit && project?.tags !== null ? [...project.tags] : []);

  useEffect(() => {
    return () => {
      dispatch(resetSubDivisions());
    }
  }, [dispatch]);
  
  
  useEffect(() => {
    const divId = selectedDivision.includes('@-@') ? selectedDivision.split('@-@')[0] : selectedDivision;
    if (divId && divId!=='0' ) {
      dispatch(getSubDivisions(divId));
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedDivision]);

  useEffect(() => {
    if(loading) {
      ProgressIndicator.show()
    } else {
      ProgressIndicator.hide();
      edit && setValue('subDivision', (project?.subDivisionId ? project?.subDivisionId + '@-@' + project?.subDivision : '0'));
    }
  }, [loading, edit, project, setValue]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [loading, selectedDivision, selectedSubDivision]);

  const formValues = (values) => {
    return {
      typeOfProject: values?.typeOfProject,
      name: values.name.trim(),
      description: values?.description.trim(),
      divisionId: values?.division?.includes('@-@') ? values?.division?.split('@-@')[0] : '',
      division: values?.division?.includes('@-@') ? values?.division?.split('@-@')[1] : '',
      subDivisionId: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[0] : '',
      subDivision: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[1] : '',
      department: values?.department,
      tags: tags,
      dataClassification: values?.dataClassification,
      hasPii: values?.hasPii,
      archerId: values?.archerId,
      procedureId: values?.procedureId,
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
            <h3>{edit ? 'Edit' : 'Create'} your Power Platform Workspace</h3>
            <p>{edit ? 'Edit the information and save!' : 'Enter the information to start creating!'}</p>
          </div>
          <div className={Styles.flex}>
            <div className={Styles.col2}>
              <div className={
                    classNames(
                      'input-field-group include-error',
                      errors?.typeOfProject?.message ? 'error' : ''
                  )}
              >
                <label className={'input-label'}>
                  Type of Project <sup>*</sup>
                </label>
                <div className={classNames('custom-select')}>
                  <select id="reportStatusField"
                    {...register('typeOfProject', {
                      required: '*Missing entry',
                      validate: (value) => value !== '0' || '*Missing entry'
                    })}
                  >
                    <option id="typeOfProjectOption" value={0}>
                      Choose
                    </option>
                    {(!edit || project?.typeOfProject === 'Playground') && <option value={'Playground'}>Playground</option>}
                    <option value={'Proof of Concept'}>Proof of Concept</option>
                    <option value={'Production'}>Production</option>
                  </select>
                </div>
                <p style={{ color: 'var(--color-orange)' }}
                  className={classNames((selectedTypeOfProject !== 'Playground' ? ' hide' : ''))}><i className="icon mbc-icon alert circle"></i> Playground projects are deleted after 2 months of not being used.</p>
                <span className={classNames('error-message', errors?.typeOfProject?.message ? '' : 'hide')}>
                  {errors?.typeOfProject?.message}
                </span>
              </div>
            </div>
            <div className={Styles.col2}>
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
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error area', errors.description ? 'error' : '')}>
                <label id="description" className="input-label" htmlFor="description">
                  Description <sup>*</sup>
                </label>
                <textarea
                  id="description"
                  className={'input-field-area'}
                  type="text"
                  rows={50}
                  {...register('description', { required: '*Missing entry', pattern: /^(?!\s+$)(\s*\S+\s*)+$/ })}
                />
                <span className={'error-message'}>{errors?.description?.message}{errors.description?.type === 'pattern' && `Spaces (and special characters) not allowed as field value.`}</span>
              </div>
            </div>
            <div className={Styles.col2}>
              <div
                className={classNames(
                  'input-field-group include-error',
                  errors?.division?.message ? 'error' : '',
                )}
              >
                <label className={'input-label'}>
                  Division <sup>*</sup>
                </label>
                <div className={classNames('custom-select')}>
                  <select
                    id="divisionField"
                    {...register('division', {
                      required: '*Missing entry',
                      validate: (value) => value !== '0' || '*Missing entry'
                    })}
                  >
                    <option id="divisionOption" value={0}>
                      Choose
                    </option>
                    {divisions?.map((obj) => {
                      return (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.id + '@-@' + obj.name}>
                          {obj.name}
                        </option>
                      )
                    })}
                  </select>
                </div>
                <span className={classNames('error-message', errors?.division?.message ? '' : 'hide')}>
                  {errors?.division?.message}
                </span>
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={'input-field-group'}>
                <label className={'input-label'}>
                  Sub Division
                </label>
                <div className={classNames('custom-select')}>
                  <select id="subDivisionField"
                    {...register('subDivision')}
                  >
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
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id + '@-@' + obj.name}>
                            {obj.name}
                          </option>
                        ))}
                      </>
                    )}
                  </select>
                </div>
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group')}>
                <Controller
                  control={control}
                  name="department"
                  rules={{
                    validate: (value) => {
                      (value === undefined || value === '') ? setDepartmentError(true) : setDepartmentError(false);
                    },
                  }}
                  render={({ field }) => (
                    <Tags
                      title={'Department'}
                      value={getValues('department')}
                      name={field.name}
                      max={1}
                      chips={departmentName}
                      tags={departments}
                      setTags={(selectedTags) => {
                        let dept = selectedTags?.map((item) => item.toUpperCase());
                        setDepartmentName(dept);
                        field.onChange(selectedTags[0]);
                      }}
                      isMandatory={true}
                      showMissingEntryError={departmentError}
                    />
                  )}
                />
              </div>
            </div>
            <div className={Styles.col2}>
              {selectedTypeOfProject !== 'Playground' &&
                <div className={'input-field-group'}>
                  <Tags
                    title={'Tags'}
                    max={100}
                    chips={tags}
                    tags={tagsLov}
                    setTags={(selectedTags) => {
                      let tag = selectedTags?.map((item) => item.toUpperCase());
                      setTags(tag.trim());
                    }}
                    isMandatory={false}
                  />
                </div>
              }
            </div>
            <div className={Styles.col2}>
              <div
                className={classNames(
                  'input-field-group include-error',
                  errors?.dataClassification?.message ? 'error' : '',
                )}
              >
                <label className={'input-label'}>
                  Data Classification <sup>*</sup>
                </label>
                <div className={classNames('custom-select')}>
                  <select
                    id="classificationField"
                    {...register('dataClassification', {
                      validate: (value) => value !== '0' || '*Missing entry'
                    })}
                  >
                    <option value={'0'}>Choose</option>
                    {classifications?.map((item) => (
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
                <span className={classNames('error-message', errors?.dataClassification?.message ? '' : 'hide')}>
                  {errors?.dataClassification?.message}
                </span>
              </div>
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.hasPii?.message ? 'error' : '')}>
                <label className={'input-label'}>
                  PII (Personally Identifiable Information) <sup>*</sup>
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
                    <span className="label">Yes</span>
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
                    <span className="label">No</span>
                  </label>
                </div>
                <span className={classNames('error-message', errors?.hasPii?.message ? '' : 'hide')}>
                  {errors?.hasPii?.message}
                </span>
              </div>
            </div>
            {selectedTypeOfProject !== 'Playground' &&
              <>
                <div className={Styles.col2}>
                  <div className={classNames('input-field-group include-error', errors?.archerId ? 'error' : '')}>
                    <label className={'input-label'}>
                      Archer ID
                    </label>
                    <div>
                      <input
                        type="text"
                        className={classNames('input-field', Styles.workspaceNameField)}
                        id="archerId"
                        placeholder="Type here eg.[INFO-XXXXX]"
                        autoComplete="off"
                        maxLength={55}
                        {...register('archerId', { pattern: /^(INFO)-\d{5}$/ })}
                      />
                      <span className={'error-message'}>{errors.archerId?.type === 'pattern' && 'Archer ID should be of type INFO-XXXXX'}</span>
                    </div>
                  </div>
                </div>
                <div className={Styles.col2}>
                  <div className={classNames('input-field-group include-error', errors?.procedureId ? 'error' : '')}>
                    <label className={'input-label'}>
                      Procedure ID
                    </label>
                    <div>
                      <input
                        type="text"
                        className={classNames('input-field', Styles.workspaceNameField)}
                        id="procedureId"
                        placeholder="Type here eg.[PO-XXXXX / ITPLC-XXXXX]"
                        autoComplete="off"
                        maxLength={55}
                        {...register('procedureId', { pattern: /^(PO|ITPLC)-\d{5}$/ })}
                      />
                      <span className={'error-message'}>{errors.procedureId?.type === 'pattern' && 'Procedure ID should be of type PO-XXXXX / ITPLC-XXXXX'}</span>
                    </div>
                  </div>
                </div>
              </>
            }
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
              {edit ? 'Save' : 'Create'} Workspace
            </button>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default PowerPlatformWorkspaceForm;