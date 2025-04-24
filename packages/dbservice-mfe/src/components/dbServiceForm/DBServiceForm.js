import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { useHistory, useParams } from "react-router-dom";
// styles
import Styles from './db-service-form.scss';
// import from DNA Container
import Tags from 'dna-container/Tags';
import SelectBox from 'dna-container/SelectBox';
import AddUser from 'dna-container/AddUser';
import ConfirmModal from 'dna-container/ConfirmModal';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
// Utils
import { Envs } from '../../utilities/envs';
// Api
import { hostServer } from '../../server/api';
import { dbServiceApi } from '../../apis/dbservice.api';

const DBServiceForm = ({ user, workspace, edit, onSave }) => {
  let history = useHistory();
  const { id } = useParams();
  
  const methods = useForm();
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = methods;

  const isOwner = user?.id === user?.id;

  // lean governance fields
  const [nameOfWorkspace, setNameOfWorkspace] = useState(edit && workspace?.name !== null ? workspace?.name : '');

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);
  const [fabricTags] = useState([]);

  const [division, setDivision] = useState(edit ? (workspace?.divisionId ? workspace?.divisionId + '@-@' + workspace?.division : '0') : '');
  const [subDivision, setSubDivision] = useState(edit ? (workspace?.subDivisionId ? workspace?.subDivisionId + '@-@' + workspace?.subDivision : '0') : '');
  const [description, setDescription] = useState(edit && workspace?.description ? workspace?.description : '');
  const [departmentName, setDepartmentName] = useState(edit && workspace?.department ? [workspace?.department] : []);
  const [typeOfProject, setTypeOfProject] = useState(edit && workspace?.typeOfProject ? workspace?.typeOfProject : '0');
  const [dataClassification, setDataClassification] = useState(edit && workspace?.dataClassification ? workspace?.dataClassification : '0');
  const [PII, setPII] = useState(edit && workspace?.hasPii ? workspace?.hasPii : false);
  const [tags, setTags] = useState(edit && workspace?.tags !== null ? [...workspace.tags] : []);
  const [archerId, setArcherID] = useState(edit && workspace?.archerId ? workspace?.archerId : '');
  const [procedureId, setProcedureID] = useState(edit && workspace?.procedureId ? workspace?.procedureId : '');
  const [termsOfUse, setTermsOfUse] = useState(edit && workspace?.termsOfUse ? [workspace?.termsOfUse] : false);

  const [collaborators, setCollaborators] = useState([]);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [bucketName] = useState(edit && workspace?.name !== null ? workspace?.name : '');
  const [ownerId, setOwnerId] = useState();

  useEffect(() => {
    Tooltip.defaultSetup();
  }, []);

  const getDevelopers = (developer) => {
    const userLicenseData = {
      userDetails: {
        id: developer?.shortId,
        firstName: developer?.firstName,
        lastName: developer?.lastName,
        department: developer?.department,
        mobileNumber: developer?.mobileNumber,
        email: developer?.email,
      },
      license: 'POWER-APPS-PREMIUM-USER'
    };

    let duplicateMember = false;
    duplicateMember = collaborators?.filter((license) => license.userDetails.id === developer.shortId)?.length ? true : false;

    const isCreator = user?.id === developer?.id;

    if (duplicateMember) {
        Notification.show('User License already added.', 'warning');
    } else if (isCreator) {
        Notification.show(
            `${developer.firstName} ${developer.lastName} is a creator. Creator can't be added to user lincense.`,
            'warning',
        );
    } else {
        collaborators?.push(userLicenseData);
        setCollaborators([...collaborators]);
    }
  }

  // const onUserLicenseClick = (value, userId) => {
  //   const updatedUserLincenses = collaborators.map(userLicense => {
  //     if (userLicense?.userDetails?.id === userId) {
  //       return Object.assign({}, userLicense, { license: value });
  //     }
  //     return userLicense;
  //   });
  //   setCollaborators(updatedUserLincenses);
  // };

  const onCollaboratorPermission = (e, userName) => {
    const bucketList = collaborators.find((item) => {
      return item.accesskey == userName;
    });

    if (e.target.checked) {
      bucketList.permission.write = true;
    } else {
      bucketList.permission.write = false;
    }
    setCollaborators([...collaborators]);
  };

  const onUserLicenseDelete = (userId) => {
    return () => {
      const updatedUserLicenses = collaborators.filter((userLicense) => {
        return userLicense?.userDetails?.id !== userId;
      });
      setCollaborators(updatedUserLicenses);
    };
  };

  useEffect(() => {
    ProgressIndicator.show();
    dbServiceApi.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDataClassificationDropdown(response[0]?.data?.data || []);
        setDivisions(response[1]?.data || []);
        setDepartments(response[2]?.data?.data || []);
        edit && setDivision(workspace?.divisionId !== null ? workspace?.divisionId + '@-@' + workspace?.division : '0');
        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
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

  useEffect(() => {
    setValue('department', departmentName[0]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [departmentName[0]]);

  const [bucketPermission, setBucketPermission] = useState({
    read: true,
    write: true,
  });

  const handleCheckbox = (e) => {
    if (e.target.checked) {
      setBucketPermission({
        ...bucketPermission,
        [e.target.name]: true,
      });
    } else {
      setBucketPermission({
        ...bucketPermission,
        [e.target.name]: false,
      });
    }
  };

  const onTransferOwnership = (userId) => {
    setOwnerId(userId);
    setShowConfirmModal(true);
  };

  const onAcceptTransferOwnership = () => {
    ProgressIndicator.show();
    dbServiceApi
      .transferOwnership(bucketName, ownerId)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show('Ownership transferred successfully.');
        setShowConfirmModal(false);
        history.push('/');
      })
      .catch(() => {
        ProgressIndicator.hide();
        Notification.show('Error while transferring ownership', 'alert');
      });
  };

  const handleCreateWorkspace = (values) => {
    ProgressIndicator.show();
    const data = {
      name: values.name.trim(),
      tags: tags,
      hasPii: values?.pii,
      archerId: values?.archerId,
      divisionId: values?.division?.includes('@-@') ? values?.division?.split('@-@')[0] : '',
      division: values?.division?.includes('@-@') ? values?.division?.split('@-@')[1] : '',
      subDivisionId: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[0] : '',
      subDivision: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[1] : '',
      description: values?.description.trim(),
      department: departmentName[0],
      procedureId: values?.procedureId,
      termsOfUse: values?.termsOfUse,
      typeOfProject: values?.typeOfProject,
      dataClassification: values?.dataClassification,
    };
    dbServiceApi.createDBService(data).then((res) => {
      ProgressIndicator.hide();
      history.push(`/workspace/${res.data.data.id}`);
      Notification.show('DB Service successfully created');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.responses?.errors?.[0]?.message || 'Error while creating fabric workspace',
        'alert',
      );
    });
  };
  const handleEditWorkspace = (values) => {
    const data = {
      name: values.name.trim(),
      tags: tags,
      hasPii: values?.pii,
      archerId: values?.archerId,
      divisionId: values?.division?.includes('@-@') ? values?.division?.split('@-@')[0] : '',
      division: values?.division?.includes('@-@') ? values?.division?.split('@-@')[1] : '',
      subDivisionId: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[0] : '',
      subDivision: values?.subDivision?.includes('@-@') ? values?.subDivision?.split('@-@')[1] : '',
      description: values?.description.trim(),
      department: departmentName[0],
      procedureId: values?.procedureId,
      termsOfUse: values?.termsOfUse,
      typeOfProject: values?.typeOfProject,
      dataClassification: values?.dataClassification,
    }
    ProgressIndicator.show();
    dbServiceApi.updateDBService(workspace.id, data).then(() => {
      ProgressIndicator.hide();
      Notification.show('DB Service successfully updated');
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
        <div className={classNames(Styles.form)}>
          <div className={Styles.formHeader}>
            <h3>{edit ? 'Edit' : 'Create'} your DB Service</h3>
            <p>{edit ? 'Edit the information and save!' : 'Enter the information to create your PostgreSQL DB!'}</p>
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
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
                <label className={'input-label'}>
                  Name <sup>*</sup>
                </label>
                <input
                  type="text"
                  className={'input-field'}
                  id="workspaceName"
                  placeholder="Type here"
                  autoComplete="off"
                  maxLength={256}
                  defaultValue={nameOfWorkspace}
                  {...register('name', { required: '*Missing entry', validate: {
                    minLength: (value) =>
                      value.length >= 3 || 'DB name should be minimum 3 characters.',
                    validChars: (value) =>
                      /^[a-z\d.-]*$/.test(value) ||
                      'DB name can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).',
                    startsCorrectly: (value) =>
                      /^[a-z\d]/.test(value) ||
                      'DB name must start with a lowercase letter or number.',
                    noTrailingHyphen: (value) =>
                      !/-$/.test(value) || 'DB name must end with letter or a number.',
                    noTrailingDot: (value) =>
                      !/\.$/.test(value) || 'DB name must end with letter or a number.',
                    noConsecutiveDots: (value) =>
                      !/\.+\./.test(value) || `DB name can't have consecutive dots.`,
                    notIpAddress: (value) =>
                      !/^(?:(?:^|\.)(?:2(?:5[0-5]|[0-4]\d)|1?\d?\d)){4}$/.test(value) ||
                      'DB name can’t be an IP address.',
                  }, onChange: (e) => { setNameOfWorkspace(e.target.value) } })}
                />
                <span className={'error-message'}>{errors?.name?.message}</span>
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
                  defaultValue={description}
                  rows={50}
                  {...register('description', { required: '*Missing entry', pattern: /^(?!\s+$)(\s*\S+\s*)+$/, onChange: (e) => { setDescription(e.target.value) } })}
                />
                <span className={'error-message'}>{errors?.description?.message}{errors.description?.type === 'pattern' && `Spaces (and special characters) not allowed as field value.`}</span>
              </div>
            </div>
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error', id && !isOwner ? 'disabled' : '',)}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Permission <sup>*</sup>
                </label>
                <div className={Styles.permissionField}>
                  <div className={Styles.checkboxContainer}>
                    <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                      <span className="wrapper">
                        <input
                          name="read"
                          type="checkbox"
                          className="ff-only"
                          checked={!!bucketPermission?.read}
                          onChange={handleCheckbox}
                        />
                      </span>
                      <span className="label">Read</span>
                    </label>
                  </div>
                  <div className={Styles.checkboxContainer}>
                    <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                      <span className="wrapper">
                        <input
                          name="write"
                          type="checkbox"
                          className="ff-only"
                          checked={!!bucketPermission?.write}
                          onChange={handleCheckbox}
                        />
                      </span>
                      <span className="label">Write</span>
                    </label>
                  </div>
                </div>
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
            </div>
            <div className={Styles.col2}>
              <div className={'input-field-group'}>
                <label className={'input-label'}>
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
            <div className={Styles.col2}>
              <div
                className={classNames(
                  'input-field-group include-error',
                  Object.keys(errors).length > 0 && (departmentName.length === 0 ? 'error' : ''),
                )}
              >
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
                  showMissingEntryError={Object.keys(errors).length > 0 && departmentName.length === 0}
                />
                {/* workaround for validating department Tags field */}
                <input type={'hidden'} defaultValue={departmentName[0]} value={departmentName[0]} {...register('department', {required: '*Missing entry'})} />
              </div>
            </div>
            <div className={Styles.col2}>
              {typeOfProject !== 'Playground' &&
                <div className={'input-field-group'}>
                  <Tags
                    title={'Tags'}
                    max={100}
                    chips={tags}
                    tags={fabricTags}
                    setTags={(selectedTags) => {
                      let tag = selectedTags?.map((item) => item.toUpperCase().trim());
                      setTags(tag);
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
            </div>
            <div className={Styles.col2}>
              <div className={classNames('input-field-group include-error')}>
                <label className={'input-label'}>
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
            {typeOfProject !== 'Playground' &&
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
                        defaultValue={archerId}
                        {...register('archerId', { pattern: /^(INFO)-\d{5}$/, onChange: (e) => { setArcherID(e.target.value) } })}
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
                        defaultValue={procedureId}
                        {...register('procedureId', { pattern: /^(PO|ITPLC)-\d{5}$/, onChange: (e) => { setProcedureID(e.target.value) } })}
                      />
                      <span className={'error-message'}>{errors.procedureId?.type === 'pattern' && 'Procedure ID should be of type PO-XXXXX / ITPLC-XXXXX'}</span>
                    </div>
                  </div>
                </div>
              </>
            }
            <div className={Styles.col}>
              <div className={classNames('input-field-group include-error')}>
                <AddUser getCollabarators={getDevelopers} isRequired={false} isUserprivilegeSearch={false} title={'Users'} />
              </div>
              {collaborators?.length === 0 &&
                <div className={Styles.noLincense}>
                  <p>No Users Selected</p>
                </div>
              }
              <div>
                {collaborators?.length > 0 && (
                  <>
                    <div className={Styles.colHeader}>
                        <div className={Styles.column1}>User ID</div>
                        <div className={Styles.column2}>Name</div>
                        <div className={Styles.column3}>Permissions</div>
                        <div className={Styles.column4}>Actions</div>
                    </div>
                    <div>
                        {collaborators?.map((userLicense) => {
                          return (
                              <div key={userLicense?.userDetails?.id} className={Styles.userRow}>
                                  <div className={Styles.column1}>
                                    <p>{userLicense?.userDetails?.id}</p>
                                  </div>
                                  <div className={Styles.column2}>
                                    <p>{userLicense?.userDetails?.firstName + ' ' + userLicense?.userDetails?.lastName}</p>
                                  </div>
                                  <div className={classNames(Styles.column3, Styles.lincenseContainer)}>
                                    <div className={classNames('input-field-group include-error ', Styles.inputGrp)}>
                                      <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                        <span className="wrapper">
                                          <input
                                            type="checkbox"
                                            className="ff-only"
                                            value="read"
                                            checked={true}
                                            readOnly
                                          />
                                        </span>
                                        <span className="label">Read</span>
                                      </label>
                                    </div>
                                    &nbsp;&nbsp;&nbsp;
                                    <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                      <label className={'checkbox'}>
                                        <span className="wrapper">
                                          <input
                                            type="checkbox"
                                            className="ff-only"
                                            value="write"
                                            checked={userLicense?.permission !== null ? userLicense?.permission?.write : false}
                                            onChange={(e) => onCollaboratorPermission(e, userLicense.accesskey)}
                                          />
                                        </span>
                                        <span className="label">Write</span>
                                      </label>
                                    </div>
                                  </div>
                                  <div className={Styles.column4}>
                                    {edit && isOwner &&
                                      <div className={Styles.deleteEntry} onClick={() => onTransferOwnership(userLicense?.accesskey)}>
                                        <i className="icon mbc-icon comparison" />
                                        Transfer Ownership
                                      </div>
                                    }
                                    <div className={Styles.deleteEntry} onClick={onUserLicenseDelete(userLicense?.userDetails?.id)}>
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
            </div>
          </div>
          <div className={Styles.formFooter}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit((values) => {
                edit ? handleEditWorkspace(values) : handleCreateWorkspace(values);
              })}
            >
              {edit ? 'Save DB Service' : 'Create DB Service'}
            </button>
          </div>
        </div>
      </FormProvider>
      {showConfirmModal && (
        <ConfirmModal
          title={'Transfer Ownership'}
          showAcceptButton={false}
          showCancelButton={false}
          show={showConfirmModal}
          removalConfirmation={true}
          showIcon={false}
          showCloseIcon={true}
          content={
            <div className={Styles.transferOwnership}>
              <div className={Styles.transferIcon}>
                <i className={classNames('icon mbc-icon comparison')} />
              </div>
              <div>
                You are going to transfer ownership.<br />You will no longer be the owner of this Database.<br />
                Are you sure you want to proceed?
              </div>
              <div className={Styles.yesBtn}>
                <button
                  className={'btn btn-secondary'}
                  type="button"
                  onClick={onAcceptTransferOwnership}
                >
                  Yes
                </button>
              </div>
            </div>
          }
          onCancel={() => setShowConfirmModal(false)}
        />
      )}
    </>
  );
}

export default DBServiceForm;