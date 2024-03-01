import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { useHistory } from "react-router-dom";
// styles
import Styles from './datalake-project-form.scss';
// import from DNA Container
import SelectBox from 'dna-container/SelectBox';
import Tags from 'dna-container/Tags';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// Api
import { hostServer } from '../../server/api';
import { datalakeApi } from '../../apis/datalake.api';

const DatalakeProjectForm = ({project, edit, onSave}) => {
  let history = useHistory();
  
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);
  
  const [dataClassification, setDataClassification] = useState(edit && project?.data?.classificationType !== null ? project?.data?.classificationType : 0);
  const [PII, setPII] = useState(edit && project?.data?.hasPii !== null ? project?.data?.hasPii : false);
  const [connectorType, setConnectorType] = useState(edit && project?.data?.connectorType !== null ? project?.data?.connectorType : 'Iceberg');

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [departmentName, setDepartmentName] = useState(edit && project?.data?.department !== null ? [project?.data?.department] : []);
  const [departmentError, setDepartmentError] = useState('');
  const [datalakeDivision, setDatalakeDivision] = useState(edit ? (project?.data?.divisionId !== null ? project?.data?.divisionId + '/' + project?.data?.divisionName : 0) : '');
  const [datalakeSubDivision, setDatalakeSubDivision] = useState(edit ? (project?.data?.subdivisionId !== null ? project?.data?.subdivisionId + '/' + project?.data?.subdivisionName : 0) : '');
  // const [statusValue, setStatusValue] = useState('');
  // const [statusError] = useState('');

  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  
  useEffect(() => {
    ProgressIndicator.show();
    datalakeApi.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDataClassificationDropdown(response[0]?.data?.data || []);                
        setDivisions(response[1]?.data || []);
        setDepartments(response[2]?.data?.data || []);
        edit && setDatalakeDivision(project?.data?.divisionId !== null ? project?.data?.divisionId + '/' + project?.data?.divisionName : 0);
        SelectBox.defaultSetup();
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
    edit && setDatalakeSubDivision(project?.data?.subdivisionId !== null ? project?.data?.subdivisionId + '/' + project?.data?.subdivisionName : 0);
  }, [datalakeSubDivision, edit, project]);

  useEffect(() => {
    const divId = datalakeDivision.includes('/') ? datalakeDivision.split('/')[0] : '';
    if (divId > '0') {
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
        ProgressIndicator.hide();
    }
      //eslint-disable-next-line react-hooks/exhaustive-deps
  }, [datalakeDivision]);

  const handleConnectorType = (e) => {
    setConnectorType(e.target.value);
  };

  // const statuses = [{
  //   id: 1,
  //   name: 'Active'
  //   }, {
  //       id: 2,
  //       name: 'In development'
  //   }, {
  //       id: 3,
  //       name: 'Sundowned'
  // }];

  // const onChangeStatus = (e) => {
  //   setStatusValue(e.target.value);
  // }

  const handleCreateProject = (values) => {
    ProgressIndicator.show();
    const data = {
    projectName: values.projectName,
    connectorType: connectorType,
    description: values.description,
    divisionId: values.datalakeDivision.includes('/') ? values.datalakeDivision.split('/')[0] : '',
    divisionName: values.datalakeDivision.includes('/') ? values.datalakeDivision.split('/')[1] : '',
    subdivisionId: values.datalakeSubDivision.includes('/') ? values.datalakeSubDivision.split('/')[0] : '',
    subdivisionName: values.datalakeSubDivision.includes('/') ? values.datalakeSubDivision.split('/')[1] : '',
    department: departmentName[0],
    status: '',
    classificationType: values.dataClassification,
    hasPii: values.pii
    }
    datalakeApi.createDatalakeProject(data).then((res) => {
    ProgressIndicator.hide();
    history.push(`/graph/${res.data.data.id}`);
    Notification.show(`Data Lakehouse Project - ${res.data.data.projectName} successfully created`);
    }).catch(error => {
    ProgressIndicator.hide();
    Notification.show(
    error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating data lakehouse project',
    'alert',
    );
    });
  };
  const handleEditProject = (values) => {
    const data = {
      projectName: project?.data?.projectName,
      connectorType: project?.data?.connectorType,
      description: values.description,
      divisionId: values.datalakeDivision.includes('/') ? values.datalakeDivision.split('/')[0] : '',
      divisionName: values.datalakeDivision.includes('/') ? values.datalakeDivision.split('/')[1] : '',
      subdivisionId: values.datalakeSubDivision.includes('/') ? values.datalakeSubDivision.split('/')[0] : '',
      subdivisionName: values.datalakeSubDivision.includes('/') ? values.datalakeSubDivision.split('/')[1] : '',
      department: departmentName[0],
      status: '',
      classificationType: values.dataClassification,
      hasPii: values.pii
    }
    ProgressIndicator.show();
    datalakeApi.updateDatalakeProject(project?.data?.id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Data Lakehouse Project successfully updated');
      onSave();
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while updating data lakehouse project',
        'alert',
      );
    });
  };
  
  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames('mbc-scroll', Styles.content)}>
          <div className={Styles.formGroup}>
            {
              edit &&
              <>
                <div className={Styles.projectWrapper}>
                  <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                    <div id="productDescription">
                      <label className="input-label summary">Project Name</label>
                      <br />                    
                      {project?.data?.projectName}
                    </div>
                    <div id="tags">
                      <label className="input-label summary">Connector Type</label>
                      <br />
                      {project?.data?.connectorType}
                    </div>
                    <div id="isExistingSolution">
                      <label className="input-label summary">Created By</label>
                      <br />
                      {project?.data?.createdBy?.firstName} {project?.data?.createdBy?.lastName}
                    </div>
                  </div>
                </div>
              </>
            }
            <div>
              { !edit && 
                <div className={Styles.flexLayout}>
                  <div className={classNames('input-field-group include-error', errors?.projectName ? 'error' : '')}>
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Name of Project <sup>*</sup>
                    </label>
                    <div>
                      <input
                        type="text"
                        className={classNames('input-field', Styles.projectNameField)}
                        id="projectName"
                        placeholder="Type here"
                        autoComplete="off"
                        maxLength={55}
                        {...register('projectName', { required: '*Missing entry', pattern: /^[a-z0-9-.]+$/ })}
                      />
                      <span className={classNames('error-message')}>{errors?.projectName?.message}{errors.projectName?.type === 'pattern' && 'Project names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).'}</span>
                    </div>
                  </div>
                  
                  <div className={classNames('input-field-group include-error')}>
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Connector Type <sup>*</sup>
                    </label>
                    <div className={Styles.pIIField}>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value={'Iceberg'}
                            name="connectorType"
                            onChange={handleConnectorType}
                            checked={connectorType === 'Iceberg'}
                          />
                        </span>
                        <span className="label">Iceberg</span>
                      </label>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value={'Delta Lake'}
                            name="connectorType"
                            onChange={handleConnectorType}
                            checked={connectorType === 'Delta Lake'}
                          />
                        </span>
                        <span className="label">Delta Lake</span>
                      </label>
                    </div>
                  </div>
                </div>
              }

              <div className={classNames('input-field-group include-error area', errors.description ? 'error' : '')}>
                <label id="description" className="input-label" htmlFor="description">
                  Description <sup>*</sup>
                </label>
                <textarea
                  id="description"
                  className="input-field-area"
                  type="text"
                  {...register('description', { required: '*Missing entry' })}
                  rows={50}
                  defaultValue={edit ? project?.data?.description : ''}
                />
                <span className={classNames('error-message')}>{errors?.description?.message}</span>
              </div>


              <div className={Styles.flexLayout}>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    errors?.datalakeDivision?.message ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Division <sup>*</sup>
                  </label>
                  <div className={classNames('custom-select')}>
                  <select
                        id="divisionField"
                        defaultValue={datalakeDivision}
                        value={datalakeDivision}
                        required-error={'*Missing entry'}
                        {...register('datalakeDivision', {
                          validate: (value) => value !== '0' || '*Missing entry',
                          onChange: (e) => { setDatalakeDivision(e.target.value)}
                        })}
                    >
                        <option id="divisionOption" value={0}>
                          Choose
                        </option>
                        {divisions?.map((obj) => {
                          return (
                          <option id={obj.name + obj.id} key={obj.id} value={obj.id + '/' + obj.name}>
                            {obj.name}
                          </option>
                          )
                        })}
                      </select>
                  </div>
                  <span className={classNames('error-message', errors?.datalakeDivision?.message ? '' : 'hide')}>
                    {errors?.datalakeDivision?.message}
                  </span>
                </div>

                <div
                  className={classNames(
                    'input-field-group include-error',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Sub Division 
                  </label>
                  <div className={classNames('custom-select')}>
                    
                    <select id="subDivisionField" 
                      defaultValue={datalakeSubDivision}
                      value={datalakeSubDivision}
                      {...register('datalakeSubDivision', {
                        onChange: (e) => { setDatalakeSubDivision(e.target.value) }
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
                              <option id={obj.name + obj.id} key={obj.id} value={obj.id + '/' + obj.name}>
                                {obj.name}
                              </option>
                            ))}
                          </>
                        )}
                    </select>
                    
                  </div>
                  {/* <span className={classNames('error-message', datalakeSubDivisionError?.length ? '' : 'hide')}>
                    {datalakeSubDivisionError}
                  </span> */}
                </div>
              </div>

              <div className={Styles.flexLayout}>
                <div
                  className={classNames(
                    Styles.bucketNameInputField,
                    'input-field-group include-error',
                    departmentError?.length ? 'error' : '',
                  )}
                >
                  <div>
                    <div className={Styles.departmentTags}>
                    
                        <Tags
                          title={'E2-Department'}
                          max={1}
                          chips={departmentName}
                          tags={departments}
                          setTags={(selectedTags) => {
                          let dept = selectedTags?.map((item) => item.toUpperCase());
                            setDepartmentName(dept);
                            setDepartmentError('');
                          }}
                          isMandatory={true}
                          showMissingEntryError={departmentError}
                          />
                        
                    </div>
                    <span className={classNames('error-message', departmentError?.length ? '' : 'hide')}>
                      {departmentError}
                    </span>
                    </div>
                    </div>
                {/* <div
                  className={classNames(
                    'input-field-group include-error',
                    statusError?.length ? 'error' : '',
                  )}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Status <sup>*</sup>
                  </label>
                  <div className={classNames('custom-select')}>
                    <select id="reportStatusField" 
                    onChange={onChangeStatus} 
                    value={statusValue}
                    required={true}
                    >
                      {statuses?.length
                      ?           
                      <>
                        <option id="reportStatusOption" value={0}>
                            Choose
                        </option>
                        {statuses?.map((obj) => (
                            <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                            </option>
                        ))}
                      </>
                        : null}
                    </select>
                  </div>
                  <span className={classNames('error-message', statusError?.length ? '' : 'hide')}>
                    {statusError}
                  </span>
                </div> */}
              </div>

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
                    <select id="classificationField" 
                      defaultValue={dataClassification}
                      value={project?.data?.classificationType !== null ? project?.data?.classificationType : 0}
                      required-error={'*Missing entry'}
                      {...register('dataClassification', {
                        validate: (value) => value !== '0' || '*Missing entry',
                        onChange: (e) => { setDataClassification(e.target.value)}
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
                            onChange: (e) => { setPII(e.target.value === 'true' ? true : false) }
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
                            onChange: (e) => { setPII(e.target.value === 'true' ? true : false) }
                          })}
                        />
                      </span>
                      <span className="label">No</span>
                    </label>
                  </div>
                </div>
              </div>
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

export default DatalakeProjectForm;