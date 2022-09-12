import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import Styles from './styles.scss';

// import from DNA Container
import Modal from 'dna-container/Modal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';


import Notification from '../../../common/modules/uilab/js/src/notification';
import { IconAvatarNew } from '../../shared/icons/iconAvatarNew/IconAvatarNew';

// const MOCK_DETAILS = {
//   id: 1,
//   new: true,
//   name: '2022-07-29_Test-Run',
//   status: 'in progress',
//   datetime: '2022/07/27',
//   ranBy: 'JANNIC1',
//   inputFile: 'MS_tms_fc.xls',
//   forecastHorizon: '2033',
//   exogenousData: 'Yes' 
// };

const MOCK_MEMBERS = [
  {
    company: "Company1",
    department: "Department1",
    email: "ab@mock.com",
    firstName: "John",
    lastName: "Doe",
    mobileNumber: "+2839283928",
    shortId: "DEMOONE",
    teamMemberPosition: "Team1",
    userType: "internal",
  },
  {
    company: "Company2",
    department: "Department2",
    email: "cd@mock.com",
    firstName: "Jane",
    lastName: "Doe",
    mobileNumber: "+2839283928",
    shortId: "DEMOTWO",
    teamMemberPosition: "Team2",
    userType: "internal",
  },
];

const ProjectDetails = () => {
  // const [projectDetails, setProjectDetails] = useState();

  // useEffect(() => {
  //   setProjectDetails(MOCK_DETAILS);
  // }, []);

  const [createProject, setCreateProject] = useState(false);
  const [editProject, setEditProject] = useState(false);

  const [showApiKey, setShowApiKey] = useState(false);
  // const [forecast, setForecast] = useState();
  const [teamMembers, setTeamMembers] = useState();

  useEffect(() => {
    setTeamMembers(MOCK_MEMBERS);
  }, []);

  const copyApiKey = () => {
    navigator.clipboard.writeText('dummy api key').then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const onTeamMemberEdit = (index) => {
    console.log(index);
  };

  const onTeamMemberDelete = (index) => {
    const teamMembersTemp = [...teamMembers];
    teamMembersTemp.splice(index, 1);
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
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers?.length}
        onMoveUp={onTeamMemberMoveUp}
        onMoveDown={onTeamMemberMoveDown}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });

  const addTeamMemberModalRef = React.createRef();
  const [showAddTeamMemberModal, setShowAddTeamMemberModal] = useState(false);
  const showAddTeamMemberModalView = () => {
    setShowAddTeamMemberModal(true);
  }
  const onAddTeamMemberModalCancel = () => {
    setShowAddTeamMemberModal(false);
  }
  const updateTeamMemberList = () => {
    onAddTeamMemberModalCancel();
  }

  const methods = useForm();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = methods;

  const handleCreateProject = (values) => {
    console.log(values);
    setCreateProject(false);
  };
  const handleEditProject = (values) => {
    console.log(values);
    setEditProject(false);
  };

  const addProjectContent = (
    <FormProvider {...methods}>
      <div className={Styles.content}>
        <div className={Styles.formGroup}>
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
                    defaultValue={'Forecast project'}
                    {...register('name', { required: '*Missing entry' })}
                  />
                  <span className={classNames('error-message')}>{errors?.name?.message}</span>
                </div>
              </div>
            </div>
          </div>
          <div className={Styles.btnContainer}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit((values) => {
                createProject ? handleCreateProject(values) : handleEditProject(values);
              })}
            >
              {createProject ? 'Save Project' : editProject && 'Save Project'}
            </button>
          </div>
        </div>
      </div>
    </FormProvider>
  );

  return (
    <React.Fragment>
      <div className={Styles.content}>
        <div className={classNames(Styles.contextMenu)}>
          <span onClick={() => { setCreateProject(true) }} className={classNames('trigger', Styles.contextMenuTrigger)}>
            <i className="icon mbc-icon edit context" />
          </span>
        </div>
        <h3 id="productName">Project Details</h3>
        <div className={Styles.firstPanel}>
          <div className={Styles.formWrapper}>
            <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
              <div id="productDescription">
                <label className="input-label summary">Project Name</label>
                <br />                    
                Lorem Ipsum Dolor
              </div>
              <div id="tags">
                <label className="input-label summary">Created on</label>
                <br />
                27/03/2022
              </div>
              <div id="isExistingSolution">
                <label className="input-label summary">Created by</label>
                <br />
                John Doe
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.content}>
        <h3 id="productName">Collaborators</h3>
        <div className={Styles.firstPanel}>
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
              {/* <div className={classNames(Styles.teamsErrorMessage, teamMemberError.length ? '' : 'hide')}>
                <span className="error-message">{teamMemberError}</span>
              </div> */}
            </div>
            <div className={Styles.membersList}>
              {teamMembersList}
            </div>
          </div>
        </div>
        </div>
      </div>
      <div className={Styles.content}>
        <h3 id="productName">API Key</h3>
        <div className={Styles.firstPanel}>
          <div className={classNames(Styles.flexLayout)}>
            <div>
              <div className={Styles.apiKey}>
                <div className={Styles.appIdParentDiv}>
                  <div className={Styles.refreshedKey}>
                    { showApiKey ? (
                      <p>2983432j38293nf9sdjfsdhfs98</p>
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
            <div>
              <div className={Styles.apiKey}>
                <button className={Styles.generateApiKeyBtn} onClick={() => console.log('generate api key')}>
                  Generate API Key
                </button>
                <p className={Styles.oneApiLink}>or go to <a href="#">oneAPI</a></p>
              </div>
            </div>
          </div>
        </div>
      </div>
      { (createProject || editProject) &&
        <Modal
          title={'Edit Forecasting Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={createProject || editProject}
          content={addProjectContent}
          scrollableContent={false}
          onCancel={() => {
            setCreateProject(false);
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
          showOnlyInteral={true}
          // editMode={editTeamMember}
          showAddTeamMemberModal={showAddTeamMemberModal}
          // teamMember={teamMemberObj}
          onUpdateTeamMemberList={updateTeamMemberList}
          onAddTeamMemberModalCancel={onAddTeamMemberModalCancel}
          // validateMemebersList={validateMembersList}
        />
      )}
    </React.Fragment>
  );
}
export default ProjectDetails;

