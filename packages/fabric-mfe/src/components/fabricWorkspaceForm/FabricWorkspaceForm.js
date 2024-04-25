import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { useHistory } from "react-router-dom";
// styles
import Styles from './fabric-workspace-form.scss';
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
import { fabricApi } from '../../apis/fabric.api';

const FabricWorkspaceForm = ({ workspace, edit, onSave }) => {
  let history = useHistory();
  
  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  // lean governance fields
  const [nameOfWorkspace, setNameOfWorkspace] = useState(edit && workspace?.name !== null ? workspace?.name : '');

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);

  const [costCenter, setCostCenter] = useState(edit && workspace?.costCenter !== null ? workspace?.costCenter : '');
  const [internalOrder, setInternalOrder] = useState(edit && workspace?.internalOrder !== null ? workspace?.internalOrder : '');
  const [division, setDivision] = useState(edit ? (workspace?.divisionId ? workspace?.divisionId + '@-@' + workspace?.division : '0') : '');
  const [subDivision, setSubDivision] = useState(edit ? (workspace?.subDivisionId ? workspace?.subDivisionId + '@-@' + workspace?.subDivision : '0') : '');
  const [description, setDescription] = useState(edit && workspace?.description ? workspace?.description : '');
  const [departmentName, setDepartmentName] = useState(edit && workspace?.department ? [workspace?.department] : []);
  const [typeOfProject, setTypeOfProject] = useState(edit && workspace?.typeOfProject ? workspace?.typeOfProject : '0');
  const [dataClassification, setDataClassification] = useState(edit && workspace?.dataClassification ? workspace?.dataClassification : '0');
  const [PII, setPII] = useState(edit && workspace?.hasPii ? workspace?.hasPii : false);
  const [tags, setTags] = useState(edit && workspace?.tags !== null ? [...workspace?.tags || undefined] : []);
  // const [relatedSolutions, setRelatedSolutions] = useState(edit && workspace?.relatedSolutions !== null ? [...workspace?.relatedSolutions || undefined] : []);
  // const [relatedReports, setRelatedReports] = useState(edit && workspace?.relatedReports !== null ? [...workspace?.relatedReports || undefined] : []);
  const [archerId, setArcherID] = useState(edit && workspace?.archerId ? workspace?.archerId : '');
  const [procedureId, setProcedureID] = useState(edit && workspace?.procedureId ? workspace?.procedureId : '');
  const [termsOfUse, setTermsOfUse] = useState(edit && workspace?.termsOfUse ? [workspace?.termsOfUse] : false);

  useEffect(() => {
    ProgressIndicator.show();
    fabricApi.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDataClassificationDropdown(response[0]?.data?.data || []);
        setDivisions(response[1]?.data || []);
        setDepartments(response[2]?.data?.data || []);
        edit && setDivision(workspace?.divisionId !== null ? workspace?.divisionId + '@-@' + workspace?.division : '0');
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
    edit && setDivision(workspace?.divisionId !== null ? workspace?.divisionId + '@-@' + workspace?.division : '0');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [divisions]);

  useEffect(() => {
    subDivisions.length > 0 &&
    edit && setSubDivision(workspace?.subDivisionId !== null ? workspace?.subDivisionId + '@-@' + workspace?.subDivision : '0');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subDivisions]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [division, subDivision]);

  const handleCreateWorkspace = (values) => {
    ProgressIndicator.show();
    const data = {
      name: values.name,
      tags: tags,
      hasPii: values?.pii,
      archerId: values?.archerId,
      divisionId: values?.division?.includes('@-@') ? values?.division?.split('@-@')[0] : '',
      division: values?.division?.includes('@-@') ? values?.division?.split('@-@')[1] : '',
      subDivisionId: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[0] : '',
      subDivision: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[1] : '',
      description: values?.description,
      department: departmentName[0],
      procedureId: values?.procedureId,
      termsOfUse: values?.termsOfUse,
      typeOfProject: values?.typeOfProject,
      dataClassification: values?.dataClassification,
      costCenter: values?.costCenter,
      internalOrder: values?.internalOrder,
      // relatedSolutions: relatedSolutions,
      // relatedReports: relatedReports,
    };
    fabricApi.createFabricWorkspace(data).then((res) => {
      ProgressIndicator.hide();
      history.push(`/workspace/${res.data.data.id}`);
      Notification.show('Fabric Workspace successfully created');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating fabric workspace',
        'alert',
      );
    });
  };
  const handleEditWorkspace = (values) => {
    const data = {
      tags: tags,
      hasPii: values?.pii,
      archerId: values?.archerId,
      divisionId: values?.division?.includes('@-@') ? values?.division?.split('@-@')[0] : '',
      division: values?.division?.includes('@-@') ? values?.division?.split('@-@')[1] : '',
      subDivisionId: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[0] : '',
      subDivision: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[1] : '',
      description: values?.description,
      department: departmentName[0],
      procedureId: values?.procedureId,
      termsOfUse: values?.termsOfUse,
      typeOfProject: values?.typeOfProject,
      dataClassification: values?.dataClassification,
      costCenter: values?.costCenter,
      internalOrder: values?.internalOrder,
      // relatedSolutions: relatedSolutions,
      // relatedReports: relatedReports,
    }
    ProgressIndicator.show();
    fabricApi.updateFabricWorkspace(workspace.id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('Fabric workspace successfully updated');
      onSave();
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while updating fabric workspace',
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
                    <label className="input-label summary">Workspace Name</label>
                    <br />
                    {workspace.name}
                  </div>
                  <div id="tags">
                    <label className="input-label summary">Created on</label>
                    <br />
                    {workspace.createdOn !== undefined && regionalDateAndTimeConversionSolution(workspace.createdOn)}
                  </div>
                  <div id="isExistingSolution">
                    <label className="input-label summary">Created by</label>
                    <br />
                    {workspace.createdBy?.firstName} {workspace.createdBy?.lastName}
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
                    {(!edit || workspace?.typeOfProject === 'Playground') && <option value={'Playground'}>Playground</option>}
                    <option value={'Proof of Concept'}>Proof of Concept</option>
                    <option value={'Production'}>Production</option>
                  </select>
                </div>
                <p style={{ color: 'var(--color-orange)' }}
                  className={classNames((typeOfProject !== 'Playground' ? ' hide' : ''))}><i className="icon mbc-icon alert circle"></i> Playground workspaces are deleted after 2 months of not being used.</p>
                <span className={classNames('error-message', errors?.typeOfProject?.message ? '' : 'hide')}>
                  {errors?.typeOfProject?.message}
                </span>
              </div>
              <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Name of Workspace <sup>*</sup>
                </label>
                <div>
                  <input
                    type="text"
                    className={classNames('input-field', Styles.workspaceNameField)}
                    id="workspaceName"
                    placeholder="Type here"
                    autoComplete="off"
                    maxLength={55}
                    readOnly={edit}
                    defaultValue={nameOfWorkspace}
                    {...register('name', { required: '*Missing entry', pattern: /^[a-z0-9-.]+$/, onChange: (e) => { setNameOfWorkspace(e.target.value) } })}
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
                <div className={classNames('input-field-group include-error', errors?.costCenter ? 'error' : '')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Cost Center <sup>*</sup>
                  </label>
                  <div>
                    <input
                      type="text"
                      className={classNames('input-field', Styles.workspaceNameField)}
                      id="costCenter"
                      placeholder="Type here"
                      autoComplete="off"
                      maxLength={55}
                      readOnly={edit}
                      defaultValue={costCenter}
                      {...register('costCenter', { required: '*Missing entry', onChange: (e) => { setCostCenter(e.target.value) } })}
                    />
                    <span className={classNames('error-message')}>{errors?.costCenter?.message}</span>
                  </div>
                </div>
                <div className={classNames('input-field-group', errors?.internalOrder ? 'error' : '')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Internal Order <sup>*</sup>
                  </label>
                  <div>
                    <input
                      type="text"
                      className={classNames('input-field', Styles.workspaceNameField)}
                      id="internalOrder"
                      placeholder="Type here"
                      autoComplete="off"
                      maxLength={55}
                      readOnly={edit}
                      defaultValue={internalOrder}
                      {...register('internalOrder', { required: '*Missing entry', onChange: (e) => { setInternalOrder(e.target.value) } })}
                    />
                    <span className={classNames('error-message')}>{errors?.internalOrder?.message}</span>
                  </div>
                </div>
              </div>

              {/* <div className={Styles.flexLayout} >
                <div className={classNames(Styles.bucketNameInputField, 'input-field-group')}>
                  <div>
                    <div className={Styles.departmentTags}>
                      <Tags
                        title={'Related Solutions'}
                        max={100}
                        chips={relatedSolutions}
                        tags={solutions}
                        setTags={(selectedTags) => {
                          let solution = selectedTags?.map((item) => item.toUpperCase());
                          setRelatedSolutions(solution);
                        }}
                        isMandatory={false}
                      // {...register('department', {required: '*Missing entry'})}
                      />

                    </div>
                  </div>
                </div>
                <div>
                  <div className={classNames(Styles.bucketNameInputField, 'input-field-group')}>
                    <div>
                      <div className={Styles.departmentTags}>
                        <Tags
                          title={'Related Reports'}
                          max={100}
                          chips={relatedReports}
                          tags={reports}
                          setTags={(selectedTags) => {
                            let report = selectedTags?.map((item) => item.toUpperCase());
                            setRelatedReports(report);
                          }}
                          isMandatory={false}
                        //showMissingEntryError={errors?.tags?.message}
                        // {...register('tags', {required: '*Missing entry'})}
                        />

                      </div>
                    </div>
                  </div>
                </div>
              </div> */}

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
                    'input-field-group include-error',
                    // datalakeSubDivisionError?.length ? 'error' : '',
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
                  {/* <span className={classNames('error-message', subDivisionError?.length ? '' : 'hide')}>
                      {subDivisionError}
                    </span> */}
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
                    value={workspace?.dataClassification}
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
                  edit ? handleEditWorkspace(values) : handleCreateWorkspace(values);
                })}
              >
                {edit ? 'Save Workspace' : 'Create Workspace'}
              </button>
            </div>
          </div>
        </div>
      </FormProvider>
    </>
  );
}

export default FabricWorkspaceForm;