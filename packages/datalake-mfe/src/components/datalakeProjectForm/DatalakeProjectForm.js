import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { v4 as uuidv4 } from 'uuid';
// import { useHistory } from "react-router-dom";
import { useDispatch } from 'react-redux';
// styles
import Styles from './datalake-project-form.scss';
// import from DNA Container
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import SelectBox from 'dna-container/SelectBox';
// App components
import Notification from '../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { IconAvatarNew } from '../icons/iconAvatarNew/IconAvatarNew';
// Utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
// Api
import { datalakeApi } from '../../apis/datalake.api';
import { addProject } from '../../redux/projectsSlice';
import { getProjects } from '../../redux/projects.services';

const DatalakeProjectForm = ({project, edit, onSave}) => {
  // let history = useHistory();

  const dispatch = useDispatch();
  console.log('datalake-project');
  console.log(project);

  const [teamMembers, setTeamMembers] = useState(edit && project?.data?.collaborators !== null ? project?.data?.collaborators : []);
  const [teamMembersOriginal, setTeamMembersOriginal] = useState(edit && project?.data?.collaborators !== null ? project?.data?.collaborators : []);
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

  const handleCreateProject = (values) => {
    // ProgressIndicator.show();
    const data = {
        id: uuidv4(),
        projectName: values.name,
        description: values.description,
        classificationType: dataClassification,
        piiData: PII,
        collaborators: teamMembers,
        createdOn: '2023-04-05T11:12:52.991+00:00',
        owner: {
          id: '***REMOVED***',
          firstName: 'Aniruddha',
          lastName: 'Khartade',
          mobile: '8975847136',
          email: 'aniruddha@gmail.com'
        },
        tables: []
    };
    dispatch(addProject(data));
    dispatch(getProjects());
    onSave();
    // datalakeApi.createForecastProject(data).then((res) => {
    //   ProgressIndicator.hide();
    //   history.push(`/project/${res.data.data.id}`);
    //   Notification.show('Forecasting Project successfully created');
    // }).catch(error => {
    //   ProgressIndicator.hide();
    //   Notification.show(
    //     error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating forecast project',
    //     'alert',
    //   );
    // });
  };
  const handleEditProject = () => {
    const addedCollaboratorsTemp = addedCollaborators.map((member) => {
      if(member.id === null) {
        return {...member, id: member.email}
      } else {
        return {...member}
      }
    });
    let removedCollaboratorsTemp = teamMembersOriginal.filter((member) => {
      return removedCollaborators.some((collab) => {
        return member.id == collab.id;
      });
    });
    removedCollaboratorsTemp = removedCollaboratorsTemp.map((member) => {
      if(member.id === null) {
        return {...member, id: member.email}
      } else {
        return {...member}
      }
    });
    const data = {
      addCollaborators: addedCollaboratorsTemp,
      removeCollaborators: removedCollaboratorsTemp
    }
    ProgressIndicator.show();
    datalakeApi.updateForecastProjectCollaborators(data, project?.data?.id).then(() => {
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
    const teamMemberTemp = {...teamMember, id: teamMember.shortId, permissions: { 'read': true, 'write': true }};
    delete teamMemberTemp.teamMemberPosition;
    let teamMembersTemp = teamMembers !== null ? [...teamMembers] : [];
    let addedCollaboratorsTemp = addedCollaborators.length > 0 ? [...addedCollaborators] : [];
    let removedCollaboratorsTemp = removedCollaborators.length > 0 ? [...removedCollaborators] : [];
    if(editTeamMember) {
      const deletedMember = teamMembersTemp.splice(editTeamMemberIndex, 1);
      addedCollaboratorsTemp = checkMembers(addedCollaborators, deletedMember[0]);
      removedCollaboratorsTemp = checkMembers(removedCollaborators, teamMember);
      removedCollaboratorsTemp.push({...deletedMember[0], id: deletedMember[0].shortId ? deletedMember[0].shortId : deletedMember[0].id, permissions: { 'read': true, 'write': true }});
      teamMembersTemp.splice(editTeamMemberIndex, 0, teamMemberTemp);
      addedCollaboratorsTemp.push({...teamMember, id: teamMember.shortId ? teamMember.shortId : teamMember.id, permissions: { 'read': true, 'write': true }});
    } else {
      teamMembersTemp.push(teamMemberTemp);
      removedCollaboratorsTemp = checkMembers(removedCollaborators, teamMember);
      addedCollaboratorsTemp.push({...teamMember, id: teamMember.shortId ? teamMember.shortId : teamMember.id, permissions: { 'read': true, 'write': true }});
    }
    setAddedCollaborators(addedCollaboratorsTemp);
    setRemovedCollaborators(removedCollaboratorsTemp);
    setTeamMembers(teamMembersTemp);
  }

  const checkMembers = (members, member) => {
    let membersTemp = members.length > 0 ? [...members] : [];
    const isCommon = members.filter((mber) => mber.shortId === member.shortId);
    if(isCommon.length === 1) {
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
    removedCollaboratorsTemp.push({...deletedMember[0], id: deletedMember[0].shortId ? deletedMember[0].shortId : deletedMember[0].id, permissions: { 'read': true, 'write': true }});
    setRemovedCollaborators(removedCollaboratorsTemp);

    setTeamMembers(teamMembersTemp);
  };

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={{...member, shortId: member?.id, userType: 'internal'}}
        hidePosition={true}
        showInfoStacked={true}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers?.length}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);
  
  const [dataClassification, setDataClassification] = useState(edit && project?.data?.classificationType !== null ? project?.data?.classificationType : '');
  const [dataClassificationError] = useState('');
  const [PII, setPII] = useState(edit && project?.data?.piiData !== null ? project?.data?.piiData : false);

  const handleDataClassification = (e) => {
    setDataClassification(e.target.value);
  };

  const handlePII = (e) => {
    setPII(e.target.value === 'true' ? true : false);
  };

  return (
    <>
      <FormProvider {...methods}>
        <div className={Styles.content}>
          <div className={Styles.formGroup}>
            {
              !edit &&
              <div className={Styles.flexLayout}>
                <div>
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
                        {...register('name', { required: '*Missing entry', pattern: /^[a-z0-9-.]+$/ })}
                      />
                      <span className={classNames('error-message')}>{errors?.name?.message}{errors.name?.type === 'pattern' && 'Project names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).'}</span>
                    </div>
                  </div>
                  
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
                    />
                    <span className={classNames('error-message')}>{errors?.description?.message}</span>
                  </div>
                </div>
                <div>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      dataClassificationError?.length ? 'error' : '',
                    )}
                  >
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Data Classification <sup>*</sup>
                    </label>
                    <div className={classNames('custom-select')}>
                      <select id="classificationField" 
                      onChange={handleDataClassification} 
                      value={dataClassification}
                      required={true}
                      >
                        
                            <option id="classificationOption" value={0}>Choose</option>
                            <option value={'Confidential'}>Confidential</option>
                            <option value={'Internal'}>Internal</option>
                            <option value={'Public'}>Public</option>
                            <option value={'Secret'}>Secret</option>
        
                      </select>
                    </div>
                    <span className={classNames('error-message', dataClassificationError?.length ? '' : 'hide')}>
                      {dataClassificationError}
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
                            onChange={handlePII}
                            checked={PII === true}
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
                            onChange={handlePII}
                            checked={PII === false}
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            }
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
                      <label className="input-label summary">Created on</label>
                      <br />
                      {project?.data?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.data?.createdOn)}
                    </div>
                    <div id="isExistingSolution">
                      <label className="input-label summary">Owner</label>
                      <br />
                      {project?.data?.owner?.firstName} {project?.data?.owner?.lastName}
                    </div>
                  </div>
                </div>
                <div className={Styles.flexLayout}>
                  <div
                    className={classNames(
                      'input-field-group include-error',
                      dataClassificationError?.length ? 'error' : '',
                    )}
                  >
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Data Classification <sup>*</sup>
                    </label>
                    <div className={classNames('custom-select')}>
                      <select id="classificationField" 
                      onChange={handleDataClassification} 
                      value={dataClassification}
                      required={true}
                      >
                        
                            <option id="classificationOption" value={0}>Choose</option>
                            <option value={'Confidential'}>Confidential</option>
                            <option value={'Internal'}>Internal</option>
                            <option value={'Public'}>Public</option>
                            <option value={'Secret'}>Secret</option>
        
                      </select>
                    </div>
                    <span className={classNames('error-message', dataClassificationError?.length ? '' : 'hide')}>
                      {dataClassificationError}
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
                            onChange={handlePII}
                            checked={PII === true}
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
                            onChange={handlePII}
                            checked={PII === false}
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                    </div>
                  </div>
                </div>
              </>
            }
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
                  edit ? handleEditProject() : handleCreateProject(values);
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

export default DatalakeProjectForm;