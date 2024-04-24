import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { useHistory } from "react-router-dom";
// styles
import Styles from './data-entry-project-form.scss';
// import from DNA Container
import Tags from 'dna-container/Tags';
import SelectBox from 'dna-container/SelectBox';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// Utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import { Envs } from '../../utilities/envs';
// Api
import { hostServer } from '../../server/api';
import { dataEntryApi } from '../../apis/dataentry.api';

const DataEntryProjectForm = ({ project, edit, onSave }) => {
  let history = useHistory();
  
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  // lean governance fields
  const [nameOfProject, setNameOfProject] = useState(edit && project?.name !== null ? project?.name : '');

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  
  const [division, setDivision] = useState(edit ? (project?.divisionId ? project?.divisionId + '@-@' + project?.division : '0') : '');
  const [subDivision, setSubDivision] = useState(edit ? (project?.subDivisionId ? project?.subDivisionId + '@-@' + project?.subDivision : '0') : '');
  const [description, setDescription] = useState(edit && project?.decription ? project?.decription : '');
  const [departmentName, setDepartmentName] = useState(edit && project?.department ? [project?.department] : []);
  const [typeOfProject, setTypeOfProject] = useState(edit && project?.typeOfProject ? project?.typeOfProject : '0');
  const [dataClassification, setDataClassification] = useState(edit && project?.dataClassification ? project?.dataClassification : '0');
  const [PII, setPII] = useState(edit && project?.piiData ? project?.piiData : false);
  const [tags, setTags] = useState(edit && project?.tags !== null ? [...project?.tags || undefined] : []);
  const [archerId, setArcherID] = useState(edit && project?.archerId ? project?.archerId : '');
  const [procedureId, setProcedureID] = useState(edit && project?.procedureId ? project?.procedureId : '');
  const [termsOfUse, setTermsOfUse] = useState(edit && project?.termsOfUse ? [project?.termsOfUse] : false);

  useEffect(() => {
    ProgressIndicator.show();
    dataEntryApi.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDataClassificationDropdown(response[0]?.data?.data || []);
        setDivisions(response[1]?.data || []);
        setDepartments(response[2]?.data?.data || []);
        edit && setDivision(project?.divisionId !== null ? project?.divisionId + '@-@' + project?.division : '0');
        !edit && SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        SelectBox.defaultSetup();
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

  useEffect(() => {
    const divId = division.includes('@-@') ? division.split('@-@')[0] : division;
    if (divId && divId!=='0' ) {
      ProgressIndicator.show();
      hostServer.get('/subdivisions/' + divId)
        .then((res) => {
          setSubDivisions(res?.data || []);
          SelectBox.defaultSetup();
          ProgressIndicator.hide();
        }).catch(() => {
          ProgressIndicator.hide();
        });
    } else {
      setSubDivisions([]);
    }
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [division]);

  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, [typeOfProject]);

  useEffect(() => {
    divisions.length > 0 && 
    edit && setDivision(project?.divisionId !== null ? project?.divisionId + '@-@' + project?.division : '0');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [divisions]);

  useEffect(() => {
    subDivisions.length > 0 &&
    edit && setSubDivision(project?.subDivisionId !== null ? project?.subDivisionId + '@-@' + project?.subDivision : '0');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subDivisions]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [division, subDivision]);

  const handleCreateProject = (values) => {
    ProgressIndicator.show();
    const data = {
      name: values.name,
      tags: tags,
      piiData: values?.pii,
      archerId: values?.archerId,
      divisionId: values?.division?.includes('@-@') ? values?.division?.split('@-@')[0] : '',
      division: values?.division?.includes('@-@') ? values?.division?.split('@-@')[1] : '',
      subDivisionId: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[0] : '',
      subDivision: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[1] : '',
      decription: values?.description,
      department: departmentName[0],
      procedureId: values?.procedureId,
      termsOfUse: values?.termsOfUse,
      typeOfProject: values?.typeOfProject,
      dataClassification: values?.dataClassification,
    };
    dataEntryApi.createDataEntryProject(data).then((res) => {
      ProgressIndicator.hide();
      history.push(`/data-entry-projects/${res.data.data.id}`);
      Notification.show('Data Entry Project successfully created');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating data entry project',
        'alert',
      );
    });
  };
  const handleEditProject = (values) => {
    const data = {
      tags: tags,
      piiData: values?.pii,
      archerId: values?.archerId,
      divisionId: values?.division?.includes('@-@') ? values?.division?.split('@-@')[0] : '',
      division: values?.division?.includes('@-@') ? values?.division?.split('@-@')[1] : '',
      subDivisionId: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[0] : '',
      subDivision: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[1] : '',
      decription: values?.description,
      department: departmentName[0],
      procedureId: values?.procedureId,
      termsOfUse: values?.termsOfUse,
      typeOfProject: values?.typeOfProject,
      dataClassification: values?.dataClassification,
    }
    ProgressIndicator.show();
    dataEntryApi.updateDataEntryProject(data, project.id).then(() => {
      ProgressIndicator.hide();
      Notification.show('Data entry project successfully updated');
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
        <div className={classNames(Styles.content, 'mbc-scroll')}>
          <div className={Styles.formGroup}>
            {
              edit &&
              <div className={Styles.workspaceWrapper}>
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="productDescription">
                    <label className="input-label summary">Project Name</label>
                    <br />
                    {project.name}
                  </div>
                  <div id="tags">
                    <label className="input-label summary">Created on</label>
                    <br />
                    {project.createdOn !== undefined && regionalDateAndTimeConversionSolution(project.createdOn)}
                  </div>
                  <div id="isExistingSolution">
                    <label className="input-label summary">Created by</label>
                    <br />
                    {project.createdBy?.firstName} {project.createdBy?.lastName}
                  </div>
                </div>
              </div>
            }

            <div className={Styles.flexLayout}>
              <div
                className={classNames(
                  'input-field-group include-error',
                  errors?.typeOfProject?.message ? 'error' : '',
                )}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Type of Project <sup>*</sup>
                </label>
                <div className={classNames('custom-select')}>
                  <select id="reportStatusField"
                    defaultValue={typeOfProject}
                    {...register('typeOfProject', {
                      required: '*Missing entry',
                      validate: (value) => value !== '0' || '*Missing entry',
                      onChange: (e) => { setTypeOfProject(e.target.value) }
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
                  className={classNames((typeOfProject !== 'Playground' ? ' hide' : ''))}><i className="icon mbc-icon alert circle"></i> Playground projects are deleted after 2 months of not being used.</p>
                <span className={classNames('error-message', errors?.typeOfProject?.message ? '' : 'hide')}>
                  {errors?.typeOfProject?.message}
                </span>
              </div>
              <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Name of Project <sup>*</sup>
                </label>
                <div>
                  <input
                    type="text"
                    className={classNames('input-field', Styles.workspaceNameField)}
                    id="projectName"
                    placeholder="Type here"
                    autoComplete="off"
                    maxLength={55}
                    readOnly={edit}
                    defaultValue={nameOfProject}
                    {...register('name', { required: '*Missing entry', pattern: /^[a-z0-9-.]+$/, onChange: (e) => { setNameOfProject(e.target.value) } })}
                  />
                  <span className={classNames('error-message')}>{errors?.name?.message}{errors.name?.type === 'pattern' && 'Project names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).'}</span>
                </div>
              </div>
            </div>
            {typeOfProject !== 'Playground' && <div>
              <div className={classNames('input-field-group include-error area', errors.description ? 'error' : '')}>
                <label id="description" className="input-label" htmlFor="description">
                  Description <sup>*</sup>
                </label>
                <textarea
                  id="description"
                  className="input-field-area"
                  type="text"
                  defaultValue={description}
                  {...register('description', { required: '*Missing entry', onChange: (e) => { setDescription(e.target.value) } })}
                  rows={50}
                />
                <span className={classNames('error-message')}>{errors?.description?.message}</span>
              </div>

              <div className={Styles.flexLayout}>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    errors?.division?.message ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Division <sup>*</sup>
                  </label>
                  <div className={classNames('custom-select')}>
                    <select
                      id="divisionField"
                      defaultValue={division}
                      value={division}
                      {...register('division', {
                        required: '*Missing entry',
                        validate: (value) => value !== '0' || '*Missing entry',
                        onChange: (e) => { setDivision(e.target.value) }
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

                <div
                  className={classNames(
                    'input-field-group include-error'
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Sub Division
                  </label>
                  <div className={classNames('custom-select')}>

                    <select id="subDivisionField"
                      defaultValue={subDivision}
                      value={subDivision}
                      required={false}
                      {...register('subDivision', {
                        onChange: (e) => { setSubDivision(e.target.value) }
                      })}
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

              <div className={Styles.flexLayout} >
                <div
                  className={classNames(
                    Styles.bucketNameInputField,
                    'input-field-group include-error',
                    errors?.department?.message ? 'error' : '',
                  )}
                >
                  <div>
                    <div className={Styles.departmentTags}>

                      <Tags
                        title={'Department'}
                        max={1}
                        chips={departmentName}
                        tags={departments}
                        setTags={(selectedTags) => {
                          let dept = selectedTags?.map((item) => item.toUpperCase());
                          setDepartmentName(dept);
                        }}
                        isMandatory={true}
                        showMissingEntryError={errors?.department?.message}
                      // {...register('department', {required: '*Missing entry'})}
                      />

                    </div>
                  </div>
                </div>
                <div>
                  <div
                    className={classNames(
                      Styles.bucketNameInputField,
                      'input-field-group include-error',
                      errors?.tags?.message ? 'error' : '',
                    )}
                  >
                    <div>
                      <div className={Styles.departmentTags}>

                        <Tags
                          title={'Tags'}
                          max={100}
                          chips={tags}
                          tags={tags}
                          setTags={(selectedTags) => {
                            let tag = selectedTags?.map((item) => item.toUpperCase());
                            setTags(tag);
                          }}
                          isMandatory={false}
                        //showMissingEntryError={errors?.tags?.message}
                        // {...register('tags', {required: '*Missing entry'})}
                        />

                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>}
            <div className={Styles.flexLayout}>
              <div
                className={classNames(
                  'input-field-group include-error',
                  errors?.dataClassification?.message ? 'error' : '',
                )}
              >
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Data Classification <sup>*</sup>
                </label>
                <div className={classNames('custom-select')}>
                  <select
                    id="classificationField"
                    defaultValue={dataClassification}
                    value={project?.dataClassification}
                    {...register('dataClassification', {
                      required: '*Missing entry',
                      validate: (value) => value !== '0' || '*Missing entry',
                      onChange: (e) => { setDataClassification(e.target.value) }
                    })}
                  >

                    <option id="classificationOption" value={0}>Choose</option>
                    {dataClassificationDropdown?.map((item) => (
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
              <div className={classNames('input-field-group include-error')}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  PII (Personally Identifiable Information) <sup>*</sup>
                </label>
                <div className={Styles.pIIField}>
                  <label className={classNames('radio')}>
                    <span className="wrapper">
                      <input
                        type="radio"
                        className="ff-only"
                        value={true}
                        name="pii"
                        defaultChecked={PII === true}
                        {...register('pii', {
                          required: '*Missing entry',
                          onChange: (e) => { setPII(e.target.value) }
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
                        value={false}
                        name="pii"
                        defaultChecked={PII === false}
                        {...register('pii', {
                          required: '*Missing entry',
                          onChange: (e) => { setPII(e.target.value) }
                        })}
                      />
                    </span>
                    <span className="label">No</span>
                  </label>
                </div>
              </div>
            </div>
            {typeOfProject !== 'Playground' && <div>

              <div className={Styles.flexLayout}>
                <div className={classNames('input-field-group include-error', errors?.archerId ? 'error' : '')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
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
                      defaultValue={archerId}
                      {...register('archerId', { pattern: /^(INFO)-\d{5}$/, onChange: (e) => { setArcherID(e.target.value) } })}
                    />
                    <span className={classNames('error-message')}>{errors.archerId?.type === 'pattern' && 'Archer ID should be of type INFO-XXXXX'}</span>
                  </div>
                </div>
                <div className={classNames('input-field-group include-error', errors?.procedureId ? 'error' : '')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
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
                      defaultValue={procedureId}
                      {...register('procedureId', { pattern: /^(PO|ITPLC)-\d{5}$/, onChange: (e) => { setProcedureID(e.target.value) } })}
                    />
                    <span className={classNames('error-message')}>{errors.procedureId?.type === 'pattern' && 'Procedure ID should be of type PO-XXXXX / ITPLC-XXXXX'}</span>
                  </div>
                </div>
              </div>
            </div>}
            <div className={classNames(Styles.termsOfUseContainer, errors?.termsOfUse?.message ? 'error' : '')}>
              <div className={Styles.termsOfUseContent}>
                <div>
                  <label className={classNames('checkbox', errors?.termsOfUse?.message ? 'error' : '')}>
                    <span className="wrapper">
                      <input
                        name="write"
                        type="checkbox"
                        className="ff-only"
                        defaultChecked={termsOfUse}
                        {...register('termsOfUse', {
                          required: 'Please agree to terms of use',
                          validate: (value) => {
                            value || 'Please agree to terms of use';
                          },
                          onChange: (e) => { e.target.value === 'on' ? setTermsOfUse(true) : setTermsOfUse(false) }
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
                  <div>I agree to <a href="#" target="_blank" rel="noopener noreferrer">terms of use</a></div>
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

            <div className={Styles.btnContainer}>
              <button
                className="btn btn-tertiary"
                type="button"
                onClick={handleSubmit((values) => {
                  edit ? handleEditProject(values) : handleCreateProject(values);
                })}
              >
                {edit ? 'Save Project' : 'Create Project'}
              </button>
            </div>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default DataEntryProjectForm;