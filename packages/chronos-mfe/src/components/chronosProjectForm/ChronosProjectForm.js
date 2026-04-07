import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { useHistory } from "react-router-dom";
import { useSelector } from 'react-redux';
// styles
import Styles from './ChronosProjectForm.scss';
// import from DNA Container
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import Tags from 'dna-container/Tags';
import SelectBox from 'dna-container/SelectBox';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { IconAvatarNew } from '../icons/iconAvatarNew/IconAvatarNew';
// Utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import { Envs } from '../../utilities/envs';
// Api
import { hostServer } from '../../server/api';
import { chronosApi } from '../../apis/chronos.api';

const ChronosProjectForm = ({ project, edit, onSave }) => {
  let history = useHistory();

  const projectR = useSelector(state => state.projectDetails);

  const [chronosProject] = useState(project !== undefined ? { ...project } : { ...projectR.data });

  const collabs = chronosProject.collaborators !== null && chronosProject.collaborators?.map((collab) => { return { ...collab, shortId: collab.id } });

  const [teamMembers, setTeamMembers] = useState(edit && chronosProject.collaborators !== null ? collabs : []);
  const [teamMembersOriginal, setTeamMembersOriginal] = useState(edit && chronosProject.collaborators !== null ? chronosProject.collaborators : []);
  const [editTeamMember, setEditTeamMember] = useState(false);
  const [selectedTeamMember, setSelectedTeamMember] = useState();
  const [editTeamMemberIndex, setEditTeamMemberIndex] = useState(0);
  const [addedCollaborators, setAddedCollaborators] = useState([]);
  const [removedCollaborators, setRemovedCollaborators] = useState([]);

  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  const addTeamMemberModalRef = React.createRef();
  const [showAddTeamMemberModal, setShowAddTeamMemberModal] = useState(false);
  const showAddTeamMemberModalView = () => {
    setShowAddTeamMemberModal(true);
    setEditTeamMember(false);
    setEditTeamMemberIndex(0);
  }

  const onAddTeamMemberModalCancel = () => {
    setShowAddTeamMemberModal(false);
    setEditTeamMember(false);
    setEditTeamMemberIndex(0);
  }

  const updateTeamMemberList = (teamMember) => {
    onAddTeamMemberModalCancel();
    const teamMemberTemp = { ...teamMember, id: teamMember.shortId, permissions: { 'read': true, 'write': true } };
    delete teamMemberTemp.teamMemberPosition;
    let teamMembersTemp = teamMembers !== null ? [...teamMembers] : [];
    let addedCollaboratorsTemp = addedCollaborators.length > 0 ? [...addedCollaborators] : [];
    let removedCollaboratorsTemp = removedCollaborators.length > 0 ? [...removedCollaborators] : [];
    if (editTeamMember) {
      const deletedMember = teamMembersTemp.splice(editTeamMemberIndex, 1);
      addedCollaboratorsTemp = checkMembers(addedCollaborators, deletedMember[0]);
      removedCollaboratorsTemp = checkMembers(removedCollaborators, teamMember);
      removedCollaboratorsTemp.push({ ...deletedMember[0], id: deletedMember[0].shortId ? deletedMember[0].shortId : deletedMember[0].id, permissions: { 'read': true, 'write': true } });
      teamMembersTemp.splice(editTeamMemberIndex, 0, teamMemberTemp);
      addedCollaboratorsTemp.push({ ...teamMember, id: teamMember.shortId ? teamMember.shortId : teamMember.id, permissions: { 'read': true, 'write': true } });
    } else {
      teamMembersTemp.push(teamMemberTemp);
      removedCollaboratorsTemp = checkMembers(removedCollaborators, teamMember);
      addedCollaboratorsTemp.push({ ...teamMember, id: teamMember.shortId ? teamMember.shortId : teamMember.id, permissions: { 'read': true, 'write': true } });
    }
    setAddedCollaborators(addedCollaboratorsTemp);
    setRemovedCollaborators(removedCollaboratorsTemp);
    setTeamMembers(teamMembersTemp);
  }

  const checkMembers = (members, member) => {
    let membersTemp = members.length > 0 ? [...members] : [];
    const isCommon = members.filter((mber) => mber.shortId === member.shortId);
    if (isCommon.length === 1) {
      membersTemp = members.filter((mber) => mber.shortId !== member.shortId);
      return membersTemp;
    } else {
      return members;
    }
  }

  const validateMembersList = (teamMemberObj) => {
    let duplicateMember = false;
    duplicateMember = teamMembers?.filter((member) => member.shortId === teamMemberObj.shortId)?.length ? true : false;
    return duplicateMember;
  };

  const onTeamMemberEdit = (index) => {
    setEditTeamMember(true);
    setShowAddTeamMemberModal(true);
    const teamMemberTemp = teamMembers[index];
    setSelectedTeamMember(teamMemberTemp);
    setEditTeamMemberIndex(index);
  };

  const onTeamMemberDelete = (index) => {
    const teamMembersTemp = [...teamMembers];
    const deletedMember = teamMembersTemp.splice(index, 1);

    const newCollabs = checkMembers(addedCollaborators, deletedMember[0]);
    setAddedCollaborators(newCollabs);

    const removedCollaboratorsTemp = removedCollaborators.length > 0 ? [...removedCollaborators] : [];
    removedCollaboratorsTemp.push({ ...deletedMember[0], id: deletedMember[0].shortId ? deletedMember[0].shortId : deletedMember[0].id, permissions: { 'read': true, 'write': true } });
    setRemovedCollaborators(removedCollaboratorsTemp);

    setTeamMembers(teamMembersTemp);
  };

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={{ ...member, shortId: member?.id, userType: 'internal' }}
        hidePosition={true}
        showInfoStacked={true}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers?.length}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });

  // lean governance fields
  const [nameOfProject, setNameOfProject] = useState(edit && chronosProject?.name !== null ? chronosProject?.name : '');

  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);

  const [division, setDivision] = useState(edit ? (chronosProject?.leanGovernanceFeilds?.divisionId ? chronosProject?.leanGovernanceFeilds?.divisionId + '@-@' + chronosProject?.leanGovernanceFeilds?.division : '0') : '');
  const [subDivision, setSubDivision] = useState(edit ? (chronosProject?.leanGovernanceFeilds?.subDivisionId ? chronosProject?.subDivisionId + '@-@' + chronosProject?.leanGovernanceFeilds?.subDivision : '0') : '');
  const [description, setDescription] = useState(edit && chronosProject?.leanGovernanceFeilds?.decription ? chronosProject?.leanGovernanceFeilds?.decription : '');
  const [departmentName, setDepartmentName] = useState(edit && chronosProject?.leanGovernanceFeilds?.department ? [chronosProject?.leanGovernanceFeilds?.department] : []);
  const [typeOfProject, setTypeOfProject] = useState(edit && chronosProject?.leanGovernanceFeilds?.typeOfProject ? chronosProject?.leanGovernanceFeilds?.typeOfProject : '0');
  const [dataClassification, setDataClassification] = useState(edit && chronosProject?.leanGovernanceFeilds?.dataClassification ? chronosProject?.leanGovernanceFeilds?.dataClassification : '0');
  const [PII, setPII] = useState(edit && chronosProject?.leanGovernanceFeilds?.piiData ? chronosProject?.leanGovernanceFeilds?.piiData : false);
  // const [tags, setTags] = useState(edit && chronosProject?.leanGovernanceFeilds?.tags !== null ? [...chronosProject?.leanGovernanceFeilds?.tags || undefined] : []);
  const [tags, setTags] = useState(null);
  const [archerId, setArcherID] = useState(edit && chronosProject?.leanGovernanceFeilds?.archerId ? chronosProject?.leanGovernanceFeilds?.archerId : '');
  const [procedureId, setProcedureID] = useState(edit && chronosProject?.leanGovernanceFeilds?.procedureId ? chronosProject?.leanGovernanceFeilds?.procedureId : '');
  const [termsOfUse, setTermsOfUse] = useState(edit && chronosProject?.leanGovernanceFeilds?.termsOfUse ? [chronosProject?.leanGovernanceFeilds?.termsOfUse] : false);

  useEffect(() => {
    ProgressIndicator.show();
    chronosApi.getLovData()
      .then((response) => {
        ProgressIndicator.hide();
        setDataClassificationDropdown(response[0]?.data?.data || []);
        setDivisions(response[1]?.data || []);
        setDepartments(response[2]?.data?.data || []);
        edit && setDivision(chronosProject?.leanGovernanceFeilds?.divisionId !== null ? chronosProject?.leanGovernanceFeilds?.divisionId + '@-@' + chronosProject?.leanGovernanceFeilds?.division : '0');
        // edit && setSubDivision(chronosProject?.leanGovernanceFeilds?.subDivisionId !== null ? chronosProject?.leanGovernanceFeilds?.subDivisionId + '@-@' + chronosProject?.leanGovernanceFeilds?.subDivision : '0');
        // edit && setSubDivisions(response[1]?.data.find((div) => div.id === division.split('@-@')[0])?.subDivisions);
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
    edit && setDivision(chronosProject?.leanGovernanceFeilds?.divisionId !== null ? chronosProject?.leanGovernanceFeilds?.divisionId + '@-@' + chronosProject?.leanGovernanceFeilds?.division : '0');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [divisions]);

  useEffect(() => {
    subDivisions.length > 0 &&
    edit && setSubDivision(chronosProject?.leanGovernanceFeilds?.subDivisionId !== null ? chronosProject?.leanGovernanceFeilds?.subDivisionId + '@-@' + chronosProject?.leanGovernanceFeilds?.subDivision : '0');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subDivisions]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [division, subDivision]);

  const handleCreateProject = (values) => {
    ProgressIndicator.show();
    const data = {
      collaborators: teamMembers,
      name: values.name,
      permission: {
        read: true,
        write: true
      },
      leanGovernanceFeilds: {
        // tags: tags,
        tags: null,
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
        dataClassification: values?.dataClassification
      }
    };
    chronosApi.createForecastProject(data).then((res) => {
      ProgressIndicator.hide();
      history.push(`/project/${res.data.data.id}`);
      Notification.show('Forecasting Project successfully created');
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating forecast project',
        'alert',
      );
    });
  };
  const handleEditProject = (values) => {
    const addedCollaboratorsTemp = addedCollaborators.map((member) => {
      if (member.id === null) {
        return { ...member, id: member.email }
      } else {
        return { ...member }
      }
    });
    let removedCollaboratorsTemp = teamMembersOriginal.filter((member) => {
      return removedCollaborators.some((collab) => {
        return member.id == collab.id;
      });
    });
    removedCollaboratorsTemp = removedCollaboratorsTemp.map((member) => {
      if (member.id === null) {
        return { ...member, id: member.email }
      } else {
        return { ...member }
      }
    });
    const data = {
      addCollaborators: addedCollaboratorsTemp,
      removeCollaborators: removedCollaboratorsTemp,
      leanGovernanceFeilds: {
        // tags: tags,
        tags: null,
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
        dataClassification: values?.dataClassification
      }
    }
    ProgressIndicator.show();
    chronosApi.updateForecastProjectCollaborators(data, chronosProject.id).then(() => {
      ProgressIndicator.hide();
      setTeamMembers([]);
      setTeamMembersOriginal([]);
      setAddedCollaborators([]);
      setRemovedCollaborators([]);
      setEditTeamMember(false);
      setEditTeamMemberIndex(0);
      Notification.show('Forecasting Project successfully updated');
      onSave();
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while updating forecast project',
        'alert',
      );
    });
  };

  // const onReportStatusFieldChange = (e)=> {
  //   console.log('eee', e)
  //   e.preventDefault();
  //   e.stopPropagation();
  //   setTypeOfProject(e.target.value);
  //   setTimeout(()=>{
  //     SelectBox.defaultSetup();
  //   }, 1000)
  // }
  return (
    <>
      <FormProvider {...methods}>
        <div className={classNames(Styles.content, 'mbc-scroll')}>
          <div className={Styles.formGroup}>
            {
              edit &&
              <div className={Styles.projectWrapper}>
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="productDescription">
                    <label className="input-label summary">Project Name</label>
                    <br />
                    {chronosProject.name}
                  </div>
                  <div id="tags">
                    <label className="input-label summary">Created on</label>
                    <br />
                    {chronosProject.createdOn !== undefined && regionalDateAndTimeConversionSolution(chronosProject.createdOn)}
                  </div>
                  <div id="isExistingSolution">
                    <label className="input-label summary">Created by</label>
                    <br />
                    {chronosProject.createdBy?.firstName} {chronosProject.createdBy?.lastName}
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
                    {(!edit || chronosProject?.leanGovernanceFeilds?.typeOfProject === 'Playground') && <option value={'Playground'}>Playground</option>}
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
                    className={classNames('input-field', Styles.projectNameField)}
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
                <div className={classNames((' hide'))}>
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
                          // tags={tags}
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
                    value={chronosProject?.leanGovernanceFeilds?.dataClassification}
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
                      className={classNames('input-field', Styles.projectNameField)}
                      id="archerId"
                      placeholder="Type here eg.[INFO-XXXXX]"
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={archerId}
                      {...register('archerId', { pattern: /^(INFO)-\d{1,10}$/, onChange: (e) => { setArcherID(e.target.value) } })}
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
                      className={classNames('input-field', Styles.projectNameField)}
                      id="procedureId"
                      placeholder="Type here eg.[PO-XXXXX / ITPLC-XXXXX]"
                      autoComplete="off"
                      maxLength={55}
                      defaultValue={procedureId}
                      {...register('procedureId', { pattern: /^(PO|ITPLC)-\d{1,10}$/, onChange: (e) => { setProcedureID(e.target.value) } })}
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
            
            <div className={Styles.collabContainer}>
              <h3 className={Styles.modalSubTitle}>Add Collaborators</h3>
              <div className={Styles.collabAvatar}>
                <div className={Styles.teamListWrapper}>
                  <div className={Styles.addTeamMemberWrapper}>
                    <IconAvatarNew className={Styles.avatarIcon} />
                    <button id="AddTeamMemberBtn"
                      onClick={showAddTeamMemberModalView}
                    >
                      <i className="icon mbc-icon plus" />
                      <span>Add team member</span>
                    </button>
                  </div>
                  {
                    teamMembers?.length > 0 &&
                    <div className={Styles.membersList}>
                      {teamMembersList}
                    </div>
                  }
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
      {showAddTeamMemberModal && (
        <AddTeamMemberModal
          ref={addTeamMemberModalRef}
          modalTitleText={'Collaborator'}
          showOnlyInteral={true}
          hideTeamPosition={true}
          editMode={editTeamMember}
          showAddTeamMemberModal={showAddTeamMemberModal}
          teamMember={selectedTeamMember}
          onUpdateTeamMemberList={updateTeamMemberList}
          onAddTeamMemberModalCancel={onAddTeamMemberModalCancel}
          validateMemebersList={validateMembersList}
        />
      )}
    </>
  );
}

export default ChronosProjectForm;