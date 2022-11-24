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

const ProjectDetails = () => {
  const {id: projectId} = useParams();
  const [editProject, setEditProject] = useState(false);

  const methods = useForm();
  const {
    handleSubmit
  } = methods;
  
  const [loading, setLoading] = useState(true);
  const [project, setProject] = useState();
  const [teamMembers, setTeamMembers] = useState([]);
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
        const members = res.data.collaborators.map(member => ({...member, userType: 'internal'}));
        setTeamMembers(members);
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
    navigator.clipboard.writeText(project?.apiKey).then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const addTeamMemberModalRef = React.createRef();
  const [showAddTeamMemberModal, setShowAddTeamMemberModal] = useState(false);
  const showAddTeamMemberModalView = () => {
    setShowAddTeamMemberModal(true);
  }
  const onAddTeamMemberModalCancel = () => {
    setShowAddTeamMemberModal(false);
  }
  const updateTeamMemberList = (teamMember) => {
    onAddTeamMemberModalCancel();
    const teamMemberTemp = {...teamMember, id: teamMember.shortId, permissions: { 'read': true, 'write': true }};
    delete teamMemberTemp.teamMemberPosition;
    let teamMembersTemp = teamMembers !== null ? [...teamMembers] : [];
    const addedCollaboratorsTemp = addedCollaborators.length > 0 ? [...addedCollaborators] : [];
    const removedCollaboratorsTemp = removedCollaborators.length > 0 ? [...removedCollaborators] : [];
    if(editTeamMember) {
      const deletedMember = teamMembersTemp.splice(editTeamMemberIndex, 1);
      removedCollaboratorsTemp.push(deletedMember[0]);
      teamMembersTemp.splice(editTeamMemberIndex, 0, teamMemberTemp);
      addedCollaboratorsTemp.push(teamMember);
    } else {
      teamMembersTemp.push(teamMemberTemp);
      addedCollaboratorsTemp.push(teamMember);
    }
    setAddedCollaborators(addedCollaboratorsTemp);
    setRemovedCollaborators(removedCollaboratorsTemp);
    setTeamMembers(teamMembersTemp);
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
    const removedCollaboratorsTemp = removedCollaborators.length > 0 ? [...removedCollaborators] : [];
    removedCollaboratorsTemp.push(deletedMember[0]);
    setRemovedCollaborators(removedCollaboratorsTemp);
    setTeamMembers(teamMembersTemp);
  };

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

  const handleEditProject = (values) => {
    console.log(values);
    const data = {
      addCollaborators: addedCollaborators,
      removeCollaborators: removedCollaborators
    }
    ProgressIndicator.show();
    chronosApi.updateForecastProjectCollaborators(data, project?.id).then(() => {
      ProgressIndicator.hide();
      setTeamMembers([]);
      setAddedCollaborators([]);
      setRemovedCollaborators([]);
      setEditProject(false);
      Notification.show('Forecasting Project successfully updated');
      getProjectById();
    }).catch(error => {
      ProgressIndicator.hide();
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while updating forecast project',
        'alert',
      );
      setTeamMembers([]);
      setAddedCollaborators([]);
      setRemovedCollaborators([]);
      setEditProject(false);
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
      <div className={Styles.content + ' ' + Styles.hide}>
        <h3 id="productName">API Key</h3>
        <div className={Styles.firstPanel}>
          <div className={classNames(Styles.flexLayout)}>
            <div>
              <div className={Styles.apiKey}>
                <div className={Styles.appIdParentDiv}>
                  <div className={Styles.refreshedKey}>
                    { showApiKey ? (
                      <p>{!loading && project?.apiKey}</p>
                    ) : (
                      <React.Fragment>
                        &bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;
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
            </div>
            {/* <div>
              <div className={Styles.apiKey}>
                <button className={Styles.generateApiKeyBtn} onClick={() => console.log('generate api key')}>
                  Generate API Key
                </button>
                <p className={Styles.oneApiLink}>or go to <a href="#">oneAPI</a></p>
              </div>
            </div> */}
          </div>
        </div>
      </div>
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
            setEditProject(false);
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

