import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useForm, FormProvider } from 'react-hook-form';
import Styles from './styles.scss';

// import from DNA Container
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import Modal from 'dna-container/Modal';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';

import Notification from '../../../common/modules/uilab/js/src/notification';
import { IconAvatarNew } from '../../shared/icons/iconAvatarNew/IconAvatarNew';
import { regionalDateAndTimeConversionSolution } from '../../../Utility/utils';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { chronosApi } from '../../../apis/chronos.api';
import Spinner from '../../shared/spinner/Spinner';
import { Envs } from '../../../Utility/envs';

const ProjectDetails = () => {
  const {id: projectId} = useParams();
  const [editProject, setEditProject] = useState(false);
  const [generateNewApiKey, setGenerateNewApiKey] = useState(false);

  const methods = useForm();
  const {
    handleSubmit
  } = methods;
  
  const [loading, setLoading] = useState(true);
  const [project, setProject] = useState();
  const [apiKey, setApiKey] = useState('');
  const [teamMembers, setTeamMembers] = useState([]);
  const [teamMembersOriginal, setTeamMembersOriginal] = useState([]);
  const [showApiKey, setShowApiKey] = useState(false);
  // const [generateApiKey, setGenerateApiKey] = useState(true);
  const [editTeamMember, setEditTeamMember] = useState(false);
  const [selectedTeamMember, setSelectedTeamMember] = useState();
  const [editTeamMemberIndex, setEditTeamMemberIndex] = useState(0);
  const [addedCollaborators, setAddedCollaborators] = useState([]);
  const [removedCollaborators, setRemovedCollaborators] = useState([]);

  useEffect(() => {
    getProjectById();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectById = () => {
    ProgressIndicator.show();
      chronosApi.getForecastProjectById(projectId).then((res) => {
      setProject(res.data);
      if(res.data.collaborators !== null) {
        const members = res.data.collaborators.map(member => ({...member, shortId: member.id, userType: 'internal'}));
        setTeamMembers(members);
        setTeamMembersOriginal(members);
      }
      setLoading(false);
      ProgressIndicator.hide();
      getApiKeyById();
    }).catch(error => {
      Notification.show(error.message, 'alert');
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  const generateApiKey = () => {
    ProgressIndicator.show();
    chronosApi.generateApiKeyById(projectId).then((res) => {
      if (res?.data?.data?.apiKey) {
        setApiKey(res.data.data.apiKey);
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(error.message, 'alert');
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  const getApiKeyById = () => {
    ProgressIndicator.show();
    chronosApi.getApiKeyById(projectId).then((res) => {
      if (res?.data?.data?.apiKey) {
        setApiKey(res.data.data.apiKey);
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(error.message, 'alert');
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  const copyApiKey = () => {
    navigator.clipboard.writeText(apiKey).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const copyApiID = () => {
    navigator.clipboard.writeText(project?.id).then(() => {
      Notification.show('Copied to Clipboard');
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
    console.log('addedCollaborators');
    console.log(addedCollaboratorsTemp);
    console.log('removedCollaborators');
    console.log(removedCollaboratorsTemp);
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
    setTeamMembers(teamMembersTemp);
    console.log('addedCollaborators');
    console.log(newCollabs);
    console.log('removedCollaborators');
    console.log(removedCollaboratorsTemp);
  };

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

  const handleEditProjectCancel = () => {
    if(project.collaborators !== null) {
      const members = project.collaborators.map(member => ({...member, userType: 'internal'}));
      setTeamMembers(members);
      setAddedCollaborators([]);
      setRemovedCollaborators([]);
    }
    setEditProject(false);
    setEditTeamMember(false);
    setEditTeamMemberIndex(0);
  }

  const onTeamMemberMoveUp = (index) => {
    const teamMembersTemp = [...teamMembers];
    const teamMember = teamMembersTemp.splice(index, 1)[0];
    teamMembersTemp.splice(index - 1, 0, teamMember);
    setTeamMembers(teamMembersTemp);
  };

  const onTeamMemberMoveDown = (index) => {
    const teamMembersTemp = [...teamMembers];
    const teamMember = teamMembersTemp.splice(index, 1)[0];
    teamMembersTemp.splice(index + 1, 0, teamMember);
    setTeamMembers(teamMembersTemp);
  };

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        hidePosition={true}
        hideContextMenu={!editProject}
        showInfoStacked={true}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers?.length}
        onMoveUp={onTeamMemberMoveUp}
        onMoveDown={onTeamMemberMoveDown}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });

  const generateNewApiKeyContent = (
    <div className={Styles.modalContent}>
      <div className={Styles.projectWrapper}>
        <div>
          Old API Key will be invalidated once you proceed. Do you want to continue?.
        </div>
        <br />
        <div className={Styles.btnContainer}>
          <button
            className="btn btn-tertiary"
            type="button"
            onClick={(() => {
              generateApiKey();
              setGenerateNewApiKey(false);
            })}
          >
            {'Generate a New API Key'}
          </button>
        </div>
      </div>
    </div>
  )

  const editProjectContent = (
    <FormProvider {...methods}>
      <div className={Styles.modalContent}>
        <div className={Styles.formGroup}>
          <div className={Styles.projectWrapper}>
            <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
              <div id="productDescription">
                <label className="input-label summary">Project Name</label>
                <br />                    
                {project?.name}
              </div>
              <div id="tags">
                <label className="input-label summary">Created on</label>
                <br />
                {project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}
              </div>
              <div id="isExistingSolution">
                <label className="input-label summary">Created by</label>
                <br />
                {project?.createdBy?.firstName} {project?.createdBy?.lastName}
              </div>
            </div>
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
                handleEditProject(values);
              })}
            >
              {'Save Project'}
            </button>
          </div>
        </div>
      </div>
    </FormProvider>
  );

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
    chronosApi.updateForecastProjectCollaborators(data, project?.id).then(() => {
      ProgressIndicator.hide();
      setTeamMembers([]);
      setTeamMembersOriginal([]);
      setAddedCollaborators([]);
      setRemovedCollaborators([]);
      setEditProject(false);
      setEditTeamMember(false);
      setEditTeamMemberIndex(0);
      Notification.show('Forecasting Project successfully updated');
      getProjectById();
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while updating forecast project',
        'alert',
      );
      setTeamMembers([]);
      setTeamMembersOriginal([]);
      setAddedCollaborators([]);
      setRemovedCollaborators([]);
      setEditProject(false);
      setEditTeamMember(false);
      setEditTeamMemberIndex(0);
      getProjectById();
    });
  };

  return (
    <React.Fragment>
      <div className={Styles.content}>
        <div className={classNames(Styles.contextMenu)}>
          <span className={classNames('trigger', Styles.contextMenuTrigger)} onClick={() => setEditProject(true)}>
            <i className="icon mbc-icon edit context" />
          </span>
        </div>
        <h3 id="productName">Project Details</h3>
        { loading && <Spinner /> }
        { !loading && 
          <div className={Styles.firstPanel}>
            <div className={Styles.formWrapper}>
              <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                <div id="productDescription">
                  <label className="input-label summary">Project Name</label>
                  <br />                    
                  {project?.name}
                </div>
                <div id="tags">
                  <label className="input-label summary">Created on</label>
                  <br />
                  {project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}
                </div>
                <div id="isExistingSolution">
                  <label className="input-label summary">Created by</label>
                  <br />
                  {project?.createdBy?.firstName} {project?.createdBy?.lastName}
                </div>
              </div>
            </div>
          </div>
        }
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Collaborators</h3>
        <div className={Styles.firstPanel}>
        <div className={Styles.collabAvatar}>
          <div className={Styles.teamListWrapper}>
            <div className={Styles.membersList}>
              {loading && <Spinner />}
              {!loading && teamMembers.length === 0 && <p className={Styles.noCollaborator}>No Collaborators</p>}
              {!loading && teamMembersList}
            </div>
          </div>
        </div>
        </div>
      </div>
      <div className={Styles.content + ' '}>
        <h3 id="productName">Access Details for Chronos Forecasting</h3>
        <div className={Styles.firstPanel}>
          <div className={classNames(Styles.flexLayout)}>
          { apiKey && <div>
            <div className={Styles.apiId}>
                <div className={Styles.appIdParentDiv}>
                  <div className={Styles.refreshedAppId}>
                  Api Id: <span className={Styles.refreshedAppId}>{!loading && project?.id}</span>
                  </div>
                  <div className={Styles.refreshedKeyIcon}>
                    <i
                      className={Styles.cpyStyle + ' icon mbc-icon copy'}
                      onClick={copyApiID}
                      tooltip-data="Copy"
                    />
                  </div>
                </div>
              </div>
              <div className={Styles.apiKey}>
                <div className={Styles.appIdParentDiv}>
                  <div className={Styles.refreshedKey}>
                  Api Key: { showApiKey ? (
                      <span className={Styles.refreshedKey}>{!loading && apiKey}</span>
                    ) : (
                      <React.Fragment>
                         <span className={Styles.refreshedKey}>&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;</span>
                      </React.Fragment>
                    )}
                  </div>
                  <div className={Styles.refreshedKeyIcon}>
                    {showApiKey ? (
                      <React.Fragment>
                        <i
                          className={Styles.showAppId + ' icon mbc-icon visibility-hide'}
                          onClick={() => { setShowApiKey(!showApiKey) }}
                          tooltip-data="Hide"
                        />
                      </React.Fragment>
                    ) : (
                      <React.Fragment>
                        <i
                          className={Styles.showAppId + ' icon mbc-icon visibility-show ' + Styles.visiblityshow}
                          onClick={() => { setShowApiKey(!showApiKey) }}
                          tooltip-data="Show"
                        />
                      </React.Fragment>
                    )}
                    <i
                      className={Styles.cpyStyle + ' icon mbc-icon copy'}
                      onClick={copyApiKey}
                      tooltip-data="Copy"
                    />
                  </div>
                </div>
              </div>
            </div> }
            <div>
              <div className={Styles.apiKey}>
                { apiKey ? 
                <button className={Styles.generateApiKeyBtn} onClick={() => setGenerateNewApiKey(true)}>
                  Generate a New API Key
                </button> : 
                <button className={Styles.generateApiKeyBtn} onClick={() => generateApiKey()}>
                  Generate a API Key
                </button> }
                { Envs.ENABLE_CHRONOS_ONEAPI && <p className={Styles.oneApiLink}>or go to <a href="#">oneAPI</a></p> }
              </div>
            </div>
          </div>
        </div>
      </div>
      { generateNewApiKey &&
        <Modal
          title={"Are you sure you want to generate a New API Key ?"}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={generateNewApiKey}
          content={generateNewApiKeyContent}
          scrollableContent={false}
          onCancel={() => {
            setGenerateNewApiKey(false);
          }}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
      { editProject &&
        <Modal
          title={'Edit Forecasting Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editProject}
          content={editProjectContent}
          scrollableContent={false}
          onCancel={() => {
            handleEditProjectCancel();
          }}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
      {showAddTeamMemberModal && (
        <AddTeamMemberModal
          ref={addTeamMemberModalRef}
          modalTitleText={'Collaborator'}
          hideTeamPosition={true}
          showOnlyInteral={true}
          editMode={editTeamMember}
          showAddTeamMemberModal={showAddTeamMemberModal}
          teamMember={selectedTeamMember}
          onUpdateTeamMemberList={updateTeamMemberList}
          onAddTeamMemberModalCancel={onAddTeamMemberModalCancel}
          validateMemebersList={validateMembersList}
        />
      )}
    </React.Fragment>
  );
}
export default ProjectDetails;

