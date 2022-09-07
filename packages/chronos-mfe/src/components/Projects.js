import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './Projects.style.scss';
import { useDispatch, useSelector } from 'react-redux';
import { useForm, FormProvider } from 'react-hook-form';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
import Modal from 'dna-container/Modal';
import AddUser from 'dna-container/AddUser';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';

import { setProjects, setPagination } from './redux/projectsSlice';
import { GetProjects } from './redux/projects.services';
import ProjectsCardItem from './ProjectCardItem';
import { IconAvatarNew } from './shared/icons/iconAvatarNew/IconAvatarNew';
import FirstRun from './shared/firstRun/FirstRun';
import Notification from '../common/modules/uilab/js/src/notification';

const MOCK_FORECAST = {
  id: 1,
  projectName: 'Forecast 1',
  collaborators: [
    {
      id: 1,
      username: 'DEMO1',
      firstName: 'John',
      lastName: 'Doe',
    },
    {
      id: 2,
      username: 'DEMO2',
      firstName: 'Jane',
      lastName: 'Doe',
    },
    {
      id: 3,
      username: 'DEMO3',
      firstName: 'Harry',
      lastName: 'Potter',
    }
  ]
};

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

const ForeCastingProjects = () => {
  const [createProject, setCreateProject] = useState(false);
  const [editProject, setEditProject] = useState(false);

  const methods = useForm();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = methods;

  const dispatch = useDispatch();
  const {
    projects,
    pagination: { projectListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.projects);

  useEffect(() => {
    dispatch(GetProjects());
  }, [dispatch]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = projectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setProjects(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = projectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setProjects(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(projectListResponse?.length / pageNum);
    const modifiedData = projectListResponse.slice(0, pageNum);
    dispatch(setProjects(modifiedData));
    dispatch(
      setPagination({
        totalNumberOfPages,
        maxItemsPerPage: pageNum,
        currentPageNumber: 1,
      }),
    );
  };

  const handleCreateProject = (values) => {
    values.permission = values.permission.reduce((acc, curr) => {
      acc[curr] = true;
      return acc;
    }, {});
    // build mock data
    const names = ['John Doe', 'Josip Skafar'];
    const [firstName, lastName] = names[Math.floor(Math.random() * names.length)].split(' ');
    values.createdDate = new Date().toISOString();
    values.createdBy = {
      firstName,
      lastName,
    };
    values.id = Math.floor(Math.random() * 10);
    dispatch(setProjects([...projects, values]));
    setCreateProject(false);
    reset({ name: '' });
  };
  const handleEditProject = (values) => {
    values.permission = values.permission.reduce((acc, curr) => {
      acc[curr] = true;
      return acc;
    }, {});
    const copyProjects = [...projects];
    const findIndex = copyProjects.findIndex((item) => item.id === values.id);
    copyProjects[findIndex] = values;
    dispatch(setProjects(copyProjects));
    setEditProject(false);
    reset({ name: '' });
  };

  /* new changes */
  const [showAddTeamMemberBar, setShowAddTeamMemberBar] = useState(false);
  const [generateApiKey, setGenerateApiKey] = useState(true);
  const [showApiKey, setShowApiKey] = useState(false);
  const [forecast, setForecast] = useState();
  const [teamMembers, setTeamMembers] = useState();

  useEffect(() => {
    setForecast(MOCK_FORECAST);
  }, []);

  useEffect(() => {
    setTeamMembers(MOCK_MEMBERS);
  }, []);

  const copyApiKey = () => {
    navigator.clipboard.writeText('dummy api key').then(() => {
      Notification.show('Copied to Clipboard');
    });
  };

  const onCollabaratorDelete = (dagName, index) => {
    console.log(dagName, index);
  };
  const onPermissionEdit = (dagId, index) => {
    console.log(dagId, index);
  };

  const getCollabarators = (collaborators, dagId) => {
    console.log(collaborators, dagId);
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
                    {...register('name', { required: '*Missing entry' })}
                  />
                  <span className={classNames('error-message')}>{errors?.name?.message}</span>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <div className={classNames('input-field-group include-error')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Permission <sup>*</sup>
                  </label>
                  <div className={Styles.permissionField}>
                    <div className={Styles.checkboxContainer}>
                      <label className={classNames('checkbox')}>
                        <span className="wrapper">
                          <input
                            name="read"
                            type="checkbox"
                            className="ff-only"
                            {...register('permission')}
                            value="read"
                            checked={true}
                          />
                        </span>
                        <span className="label">Read</span>
                      </label>
                    </div>
                    <div className={Styles.checkboxContainer}>
                      <label className={classNames('checkbox')}>
                        <span className="wrapper">
                          <input
                            name="write"
                            type="checkbox"
                            className="ff-only"
                            {...register('permission')}
                            value="write"
                            checked={true}
                          />
                        </span>
                        <span className="label">Write</span>
                      </label>
                    </div>
                  </div>
                </div>
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
                  {/* <div className={classNames(Styles.teamsErrorMessage, teamMemberError.length ? '' : 'hide')}>
                    <span className="error-message">{teamMemberError}</span>
                  </div> */}
                </div>
                <div className={Styles.membersList}>
                  {teamMembersList}
                </div>
              </div>
            </div>
            <div className={Styles.apiKeySection}>
              <h3 className={Styles.modalSubTitle}>Generate API Key</h3>
              {
                generateApiKey &&
                <div className={Styles.apiKey}>
                  <p className={Styles.label}>API Key</p>
                  <button className={Styles.generateApiKeyBtn} onClick={() => setGenerateApiKey(false)}>
                    Generate API Key
                  </button>
                  <p className={Styles.oneApiLink}>or go to <a href="#">oneAPI</a></p>
                </div>
              }
              {
                !generateApiKey &&
                  <div className={Styles.apiKey}>
                    <p className={Styles.label}>API Key</p>
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
              }
            </div>
            <div className={Styles.collabContent}>
              <div className={Styles.collabContentList}>
                <div className={Styles.collabUsersList}>
                  {forecast?.collaborators?.length > 0 ? (
                    <React.Fragment>
                      <div className={Styles.collUserTitle}>
                        <div className={Styles.collUserTitleCol}>User ID</div>
                        <div className={Styles.collUserTitleCol}>Name</div>
                        <div className={Styles.collUserTitleCol}>Permission</div>
                        <div className={Styles.colAction}></div>
                      </div>
                      <div className={Styles.collUserContent}>
                        {forecast?.collaborators?.map(
                          (item, collIndex) => {
                            return (
                              <div
                                key={collIndex}
                                className={Styles.collUserContentRow}
                              >
                                <div className={Styles.collUserTitleCol}>{item.username}</div>
                                <div className={Styles.collUserTitleCol}>
                                  {item.firstName + ' ' + item.lastName}
                                </div>
                                <div className={Styles.collUserTitleCol}>
                                  <div
                                    className={classNames(
                                      'input-field-group include-error ' + Styles.inputGrp,
                                    )}
                                  >
                                    <label className={'checkbox ' + Styles.checkBoxDisable}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="can_dag_read"
                                          checked={true}
                                        />
                                      </span>
                                      <span className="label">Read</span>
                                    </label>
                                  </div>
                                  &nbsp;&nbsp;&nbsp;
                                  <div
                                    className={classNames(
                                      'input-field-group include-error ' + Styles.inputGrp,
                                    )}
                                  >
                                    <label className={'checkbox ' + Styles.writeAccess}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="can_dag_edit"
                                          // checked={
                                          //   item.permissions !== null
                                          //     ? item.permissions.includes('can_dag_edit')
                                          //     : false
                                          // }
                                          onClick={onPermissionEdit(forecast.projectName, collIndex)}
                                        />
                                      </span>
                                      <span className="label">Write</span>
                                    </label>
                                  </div>
                                </div>
                                <div className={Styles.colAction}>
                                  <div
                                    className={Styles.deleteEntry}
                                    onClick={onCollabaratorDelete(forecast.projectName, collIndex)}
                                  >
                                    <i className="icon mbc-icon trash-outline" />
                                  </div>
                                </div>
                              </div>
                            );
                          },
                        )}
                      </div>
                    </React.Fragment>
                  ) : (
                    <div className={Styles.collabContentEmpty}>
                      <h6> Collaborators Not Exist!</h6>
                    </div>
                  )}
                </div>
                <div className={Styles.addCollabBtn}>
                  <div className={classNames(Styles.addItemButton)}>
                    <button onClick={() => { setShowAddTeamMemberBar(!showAddTeamMemberBar) }}>
                      <i className="icon mbc-icon plus" />
                      <span>Add Collaborator</span>
                    </button>
                  </div>
                </div>
                { showAddTeamMemberBar &&
                  <div className={Styles.collabContentListAdd}>
                    <AddUser getCollabarators={getCollabarators} />
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
                createProject ? handleCreateProject(values) : handleEditProject(values);
              })}
            >
              {createProject ? 'Create Project' : editProject && 'Save Project'}
            </button>
          </div>
        </div>
      </div>
    </FormProvider>
  );

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          {projects?.length === 0 ? (
            <FirstRun openCreateProjectModal={() => setCreateProject(true)} />
          ) : (
            <>
              <div className={classNames(Styles.breadcrumb)}>
                <ol>
                  <li><a href='#/'>Start</a></li>
                  <li><a href='#/services'>My Services</a></li>
                  <li>Chronos Forecasting</li>
                </ol>
              </div>

              <div className={classNames(Styles.caption)}>
                <h3>My Forecasting Projects</h3>
                <div className={classNames(Styles.listHeader)}>
                  {projects?.length ? (
                    <React.Fragment>
                      <button
                        className={projects?.length === null ? Styles.btnHide : 'btn btn-primary'}
                        type="button"
                        onClick={() => setCreateProject(true)}
                      >
                        <i className="icon mbc-icon plus" />
                        <span>Create Forecasting Project</span>
                      </button>
                    </React.Fragment>
                  ) : null}
                </div>
              </div>

              <div className={Styles.allProjectContent}>
                <div className={Styles.newProjectCard} onClick={() => setCreateProject(true)}>
                  <div className={Styles.addicon}> &nbsp; </div>
                  <label className={Styles.addlabel}>Create new project</label>
                </div>
                {projects?.map((project, index) => {
                  return (
                    <ProjectsCardItem
                      key={index}
                      project={project}
                      onEdit={(val) => {
                        setEditProject(true);
                        reset(val);
                      }}
                    />
                  );
                })}
              </div>

              {projects?.length ? (
                <Pagination
                  totalPages={totalNumberOfPages}
                  pageNumber={currentPageNumber}
                  onPreviousClick={onPaginationPreviousClick}
                  onNextClick={onPaginationNextClick}
                  onViewByNumbers={onViewByPageNum}
                  displayByPage={true}
                />
              ) : null}
          </>
          )}
        </div>
      </div>
      { (createProject || editProject) &&
        <Modal
          title={createProject ? 'Create new Forecasting Project' : editProject && 'Edit Forecasting Project'}
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
    </>
  );
};
export default ForeCastingProjects;
