import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './CodeSpaceRecipe.scss';
import SelectBox from 'dna-container/SelectBox';
import TextBox from 'dna-container/TextBox';
import AddTeamMemberModal from 'dna-container/AddTeamMemberModal';
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import IconAvatarNew from 'dna-container/IconAvatarNew';
import { Envs } from '../../Utility/envs';
// import { ICodeCollaborator, IUserInfo } from 'globals/types';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { Notification } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import { isValidGITRepoUrl } from '../../Utility/utils';
import { useHistory } from 'react-router-dom';
const classNames = cn.bind(Styles);
// export interface ICreateRecipe {
//   createdBy: IUserInfo;
//   recipeName: string;
//   recipeType: string;
//   gitUrl: string;
//   diskSpace: string;
//   minCpu: string;
//   maxCpu: string;
//   minRam: string;
//   maxRam: string;
//   isPublic: boolean;
//   users: ICodeCollaborator[];
//   software: string[];
//   plugins: string[];
  
// }
// export interface Isoftware {
//   name: string;
//   version: string;
// }

// export interface IUserInfoProps {
//   user: IUserInfo;
// }

const CodeSpaceRecipe = (props) => {
  const requiredError = '*Missing entry';
  const repeatedError = '*Recipe name already exists';
  const history = useHistory();

  const [softwares, setSoftwares] = useState([]);

  const [recipeName, setRecipeName] = useState('');
  const [recipeType, setRecipeType] = useState('');
  const [gitUrl, setGitUrl] = useState('');
  const [diskSpace, setDiskSpace] = useState('');
  const [minCpu, setMinCpu] = useState('');
  const [maxCpu, setMaxCpu] = useState('');
  const [minRam, setMinRam] = useState('');
  const [maxRam, setMaxRam] = useState('');
  const [software, setSoftware] = useState([]);
  const [isPublic, setIsPublic] = useState(true);
  

  const [teamMembers, setTeamMembers] = useState([]);
  // const [teamMembersOriginal, setTeamMembersOriginal] = useState([]);
  const [editTeamMember, setEditTeamMember] = useState(false);
  const [selectedTeamMember, setSelectedTeamMember] = useState();
  const [editTeamMemberIndex, setEditTeamMemberIndex] = useState(0);
  const [addedCollaborators, setAddedCollaborators] = useState([]);
  const [removedCollaborators, setRemovedCollaborators] = useState([]);

  const addTeamMemberModalRef  = React.createRef();
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
    console.log(teamMember);
    const teamMemberTemp = { ...teamMember, id: teamMember.shortId, email: teamMember.email, lastName: teamMember.lastName, firstName: teamMember.firstName, department: teamMember.department, mobileNumber: teamMember.mobileNumber };
    delete teamMemberTemp.teamMemberPosition;
    const teamMembersTemp = teamMembers !== null ? [...teamMembers] : [];
    let addedCollaboratorsTemp = addedCollaborators.length > 0 ? [...addedCollaborators] : [];
    let removedCollaboratorsTemp = removedCollaborators.length > 0 ? [...removedCollaborators] : [];
    if (editTeamMember) {
      const deletedMember = teamMembersTemp.splice(editTeamMemberIndex, 1);
      addedCollaboratorsTemp = checkMembers(addedCollaborators, deletedMember[0]);
      removedCollaboratorsTemp = checkMembers(removedCollaborators, teamMember);
      removedCollaboratorsTemp.push({ ...deletedMember[0], id: deletedMember[0].shortId ? deletedMember[0].shortId : deletedMember[0].id, email: deletedMember[0].email, lastName: deletedMember[0].lastName, firstName: deletedMember[0].firstName, department: deletedMember[0].department, mobileNumber: deletedMember[0].mobileNumber });
      teamMembersTemp.splice(editTeamMemberIndex, 0, teamMemberTemp);
      addedCollaboratorsTemp.push({ ...teamMember, id: teamMember.shortId ? teamMember.shortId : teamMember.id, email: teamMember.email, lastName: teamMember.lastName, firstName: teamMember.firstName, department: teamMember.department, mobileNumber: teamMember.mobileNumber });
    } else {
      teamMembersTemp.push(teamMemberTemp);
      removedCollaboratorsTemp = checkMembers(removedCollaborators, teamMember);
      addedCollaboratorsTemp.push({ ...teamMember, id: teamMember.shortId ? teamMember.shortId : teamMember.id, email: teamMember.email, lastName: teamMember.lastName, firstName: teamMember.firstName, department: teamMember.department, mobileNumber: teamMember.mobileNumber });
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
    removedCollaboratorsTemp.push({ ...deletedMember[0], id: deletedMember[0].shortId ? deletedMember[0].shortId : deletedMember[0].id, email: deletedMember[0].email, lastName: deletedMember[0].lastName, firstName: deletedMember[0].firstName, department: deletedMember[0].department, mobileNumber: deletedMember[0].mobileNumber });
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
        showMoveUp={false}
        showMoveDown={false}
        onMoveUp={() => {}}
        onMoveDown={() => {}}
        onEdit={onTeamMemberEdit}
        onDelete={onTeamMemberDelete}
      />
    );
  });

  const [errorObj, setErrorObj] = useState({
    recipeName: '',
    recipeType: '',
    gitUrl: '',
    discSpace: '',
    minCpu: '',
    maxCpu: '',
    minRam: '',
    maxRam: '',
  });
  const isPublicRecipeChoosen = recipeType.startsWith('public');
  const githubUrlValue = isPublicRecipeChoosen ? 'https://github.com/' : Envs.CODE_SPACE_GIT_PAT_APP_URL;

  useEffect(() => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getSoftwareLov()
      .then((response) => {
        ProgressIndicator.hide();
        console.log(JSON.stringify(response.data.data));
        setSoftwares(response.data.data);
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
  }, []);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  const onRecipeNameChange = (e) => {
    const value = e.currentTarget.value;
    setRecipeName(value);
    setErrorObj((prevState) => ({
      ...prevState,
      recipeName: '',
    }));
  };

  const validateGitUrl = (githubUrlVal) => {
    const errorText = githubUrlVal.length
      ? isValidGITRepoUrl(githubUrlVal, isPublicRecipeChoosen)
        ? ''
        : `provide valid ${isPublicRecipeChoosen ? 'https://github.com/' : Envs.CODE_SPACE_GIT_PAT_APP_URL} git url.`
      : requiredError;
    setErrorObj((prevState) => ({
      ...prevState,
      gitUrl: errorText,
    }));
  };

  const onRecipeTypeChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setRecipeType(selectedOption);
    if (gitUrl) {
      validateGitUrl(gitUrl);
    }
  };

  const onGitUrlChange = (e) => {
    const githubUrlVal = e.currentTarget.value.trim();
    setGitUrl(githubUrlVal);
    validateGitUrl(githubUrlVal);
  };

  const onDiskSpaceChange = (e) => {
    const value = e.currentTarget.value;
    setDiskSpace(value);
  };

  const onMinCpuChange = (e) => {
    const value = e.currentTarget.value;
    setMinCpu(value);
  };

  const onMaxCpuChange = (e) => {
    const value = e.currentTarget.value;
    setMaxCpu(value);
  };

  const onMinRamChange = (e) => {
    const value = e.currentTarget.value;
    setMinRam(value);
  };

  const onMaxRamChange = (e) => {
    const value = e.currentTarget.value;
    console.log(value);
    setMaxRam(value);
  };

  const onSoftwareChange = (e) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValues.push(option.label);
      });
    }
    setSoftware(selectedValues);
  };

  const onIsPublicChange = (e) => {
    const currentValue = e.currentTarget.value;
    if (currentValue === 'true') {
      setIsPublic(true);
    } else {
      setIsPublic(false);
    }
  };
  
  const onRequest = () => {
    console.log(software);
    if (validateForm()) {
      const CreateNewRecipe = {
        createdBy: {
          department: props.user.department,
          email: props.user.email,
          firstName: props.user.firstName,
          gitUserName: props.user.id,
          id: props.user.id,
          lastName: props.user.lastName,
          mobileNumber: props.user.mobileNumber,
        },
        diskSpace: diskSpace,
        maxCpu: maxCpu,
        minCpu: minCpu,
        minRam: minRam,
        maxRam: maxRam,
        oSName: 'Debian-OS-11',
        osname: 'Debian-OS-11',
        plugins: ['string'],
        recipeName: recipeName,
        recipeType: recipeType,
        repodetails: gitUrl,
        software: software,
        isPublic: isPublic,
        users: teamMembers
      };
      ProgressIndicator.show();
      CodeSpaceApiClient.createCodeSpaceRecipe(CreateNewRecipe)
        .then((res) => {
          ProgressIndicator.hide();
          history.push('/codespaces');
          Notification.show('New Recipe Created successfully');
        })
        .catch((err) => {
          ProgressIndicator.hide();
          Notification.show(err.message, 'alert');
          if (err.message === 'Value or Item already exist!') {
            setErrorObj((prevState) => ({
              ...prevState,
              recipeName: repeatedError,
            }));
          }
        });
    }
  };

  const validateForm = () => {
    let isValid = true;
    if (recipeName === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        recipeName: requiredError,
      }));
    }
    if (recipeType === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        recipeType: requiredError,
      }));
    }
    if (gitUrl === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        gitUrl: requiredError,
      }));
    }
    if (diskSpace === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        discSpace: requiredError,
      }));
    }
    if (minCpu === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        minCpu: requiredError,
      }));
    }
    if (maxCpu === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        maxCpu: requiredError,
      }));
    }
    if (minRam === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        minRam: requiredError,
      }));
    }
    if (maxRam === '0') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        maxRam: requiredError,
      }));
    }
    return isValid;
  };

  return (
    <div>
      <div className={classNames(Styles.mainPanel)}>
        <div>
          <h3>Recipe Management</h3>
          <div className={classNames(Styles.wrapper)}>
            <div className={classNames(Styles.firstPanel, 'addRecipe')}>
              <h3>Recipe Details</h3>
              <div className={classNames(Styles.formWrapper)}>
                <div className={classNames(Styles.flexLayout)}>
                  <TextBox
                    type="text"
                    controlId={'recipeNameInput'}
                    labelId={'recipeNameLabel'}
                    label={'Recipe Name'}
                    placeholder={'Type here'}
                    value={recipeName}
                    errorText={errorObj.recipeName}
                    required={true}
                    maxLength={200}
                    onChange={onRecipeNameChange}
                  />
                  <div className={classNames('input-field-group include-error')}>
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Publicly Available <sup>*</sup>
                    </label>
                    <div className={Styles.pIIField}>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value="true"
                            name="isPublic"
                            defaultChecked={isPublic === true}
                            onChange={onIsPublicChange}
                          />
                        </span>
                        <span className="label">Yes</span>
                      </label>
                      <label className={classNames('radio')}>
                        <span className="wrapper">
                          <input
                            type="radio"
                            className="ff-only"
                            value="false"
                            name="isPublic"
                            defaultChecked={isPublic === false}
                            onChange={onIsPublicChange}
                          />
                        </span>
                        <span className="label">No</span>
                      </label>
                    </div>
                  </div>
                </div>
                <div className={classNames(Styles.flexLayout)}>
                  <div className={classNames('input-field-group include-error', errorObj.recipeType ? 'error' : '')}>
                    <label id="recipeLabel" className="input-label" htmlFor="recipeSelect">
                      Recipe Location<sup>*</sup>
                    </label>
                    <div id="selectBox" className="custom-select">
                      <select
                        id="recipeTypeField"
                        required={true}
                        required-error={errorObj.recipeType}
                        value={recipeType}
                        onChange={onRecipeTypeChange}
                      >
                        <option value="">Choose</option>
                        <option value="public">Public GitHub</option>
                        <option value="private">Private GitHub</option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.recipeType.length ? '' : 'hide')}>
                      {errorObj.recipeType}
                    </span>
                  </div>
                  <div>
                    <TextBox
                      type="text"
                      controlId={'gitUrlInput'}
                      labelId={'gitUrlLabel'}
                      label={
                        !recipeType || recipeType === '' || recipeType === 'from Storage'
                          ? 'GitHub Url'
                          : `GitHub Url (Ex. ${githubUrlValue}orgname-or-username/your-repo-name.git)`
                      }
                      placeholder={'Type here'}
                      value={gitUrl}
                      errorText={errorObj.gitUrl}
                      required={true}
                      maxLength={200}
                      onChange={onGitUrlChange}
                    />
                  </div>
                </div>
              </div>
            </div>
            {!isPublic && (
              <div className={classNames(Styles.firstPanel, 'addRecipe')}>
                <h3>Collaborator Details</h3>
                <div className={classNames(Styles.formWrapper)}>
                  <div className={classNames('input-field-group include-error')}>
                    <div className={Styles.collabContainer}>
                      {/* <h3 className={Styles.modalSubTitle}>Add Users (Optional)</h3> */}
                      <div className={Styles.collabAvatar}>
                        <div className={Styles.teamListWrapper}>
                          <div className={Styles.addTeamMemberWrapper}>
                            <IconAvatarNew className={Styles.avatarIcon} />
                            <button id="AddTeamMemberBtn" onClick={showAddTeamMemberModalView}>
                              <i className="icon mbc-icon plus" />
                              <span>Add user</span>
                            </button>
                          </div>
                          {teamMembers?.length > 0 && <div className={Styles.membersList}>{teamMembersList}</div>}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
            <div className={classNames(Styles.firstPanel, 'addRecipe')}>
              <h3>Hardware Configurations</h3>
              <div className={classNames(Styles.formWrapper)}>
                <div className={classNames(Styles.SelectBoxWrapper)}>
                  <div className={classNames('input-field-group include-error', errorObj.discSpace ? 'error' : '')}>
                    <label id="diskSpaceLable" className="input-label" htmlFor="diskSpaceSelect">
                      Disk-Space(in GB)<sup>*</sup>
                    </label>
                    <div id="selectBox" className="custom-select">
                      <select
                        id="diskSpaceField"
                        required={true}
                        required-error={errorObj.discSpace}
                        value={diskSpace}
                        onChange={onDiskSpaceChange}
                      >
                        <option value={0}>Choose</option>
                        <option value={2}> 2 </option>
                        <option value={5}> 5 </option>
                        <option value={10}> 10 </option>
                        <option value={15}> 15 </option>
                        <option value={20}> 20 </option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.discSpace.length ? '' : 'hide')}>
                      {errorObj.discSpace}
                    </span>
                  </div>
                  <div className={classNames('input-field-group include-error', errorObj.maxCpu ? 'error' : '')}>
                    <label id="maxCpuLable" className="input-label" htmlFor="maxCpuSelect">
                      MAX CPU<sup>*</sup>
                    </label>
                    <div id="selectBox" className="custom-select">
                      <select
                        id="maxCpuField"
                        required={true}
                        required-error={errorObj.maxCpu}
                        value={maxCpu}
                        onChange={onMaxCpuChange}
                      >
                        <option value={0}>Choose</option>
                        <option value={0.5}> 0.5 </option>
                        <option value={1}> 1 </option>
                        <option value={1.5}> 1.5 </option>
                        <option value={2}> 2 </option>
                        <option value={2.5}> 2.5 </option>
                        <option value={3}> 3 </option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.maxCpu.length ? '' : 'hide')}>
                      {errorObj.maxCpu}
                    </span>
                  </div>
                  <div className={classNames('input-field-group include-error', errorObj.minCpu ? 'error' : '')}>
                    <label id="minCpuLable" className="input-label" htmlFor="minCpuSelect">
                      MIN CPU<sup>*</sup>
                    </label>
                    <div id="selectBox" className="custom-select">
                      <select
                        id="minCpuField"
                        required={true}
                        required-error={errorObj.minCpu}
                        value={minCpu}
                        onChange={onMinCpuChange}
                      >
                        <option value={0}>Choose</option>
                        <option value={0.5}> 0.5 </option>
                        <option value={1}> 1 </option>
                        <option value={1.5}> 1.5 </option>
                        <option value={2}> 2 </option>
                        <option value={2.5}> 2.5 </option>
                        <option value={3}> 3 </option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.minCpu.length ? '' : 'hide')}>
                      {errorObj.minCpu}
                    </span>
                  </div>
                  <div className={classNames('input-field-group include-error', errorObj.maxRam ? 'error' : '')}>
                    <label id="maxRamLable" className="input-label" htmlFor="maxRamSelect">
                      MAX RAM<sup>*</sup>
                    </label>
                    <div id="selectBox" className="custom-select">
                      <select
                        id="maxRamField"
                        required={true}
                        required-error={errorObj.maxCpu}
                        value={maxRam}
                        onChange={onMaxRamChange}
                      >
                        <option value={0}>Choose</option>
                        <option value={2}> 2 </option>
                        <option value={3}> 3 </option>
                        <option value={4}> 4 </option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.maxCpu.length ? '' : 'hide')}>
                      {errorObj.maxCpu}
                    </span>
                  </div>
                  <div className={classNames('input-field-group include-error', errorObj.minRam ? 'error' : '')}>
                    <label id="minRamLable" className="input-label" htmlFor="minCpuSelect">
                      MIN RAM<sup>*</sup>
                    </label>
                    <div id="selectBox" className="custom-select">
                      <select
                        id="minRamField"
                        required={true}
                        required-error={errorObj.minRam}
                        value={minRam}
                        onChange={onMinRamChange}
                      >
                        <option value={0}>Choose</option>
                        <option value={2}> 2 </option>
                        <option value={3}> 3 </option>
                        <option value={4}> 4 </option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.minRam.length ? '' : 'hide')}>
                      {errorObj.minRam}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className={classNames(Styles.firstPanel, 'addRecipe')}>
              <h3>Software Configurations</h3>
              <div className={classNames(Styles.formWrapper)}>
                <div className={classNames(Styles.flexLayout, Styles.MultiSelectBoxWrapper)}>
                  {/* <Tags
                    title={'Software (optional)'}
                    max={100}
                    chips={software}
                    tags={softwares}
                    setTags={(selectedTags) => {
                      console.log(selectedTags);
                      setSoftware(selectedTags);
                    }}
                    isMandatory={false}
                    showMissingEntryError={false}
                  /> */}
                  <div className={classNames('input-field-group include-error')}>
                    <label id="softwareLable" className="input-label" htmlFor="softwareSelect">
                      Software
                    </label>
                    <div id="software" className="custom-select">
                      <select
                        id="softwareSelect"
                        multiple={true}
                        required={false}
                        required-error={requiredError}
                        onChange={onSoftwareChange}
                        value={software}
                      >
                        {softwares?.map((item) => (
                          <option id={item.softwareName} key={item.softwareName} value={item.softwareName}>
                            {item.softwareName}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className={Styles.btnConatiner}>
            <button className="btn btn-tertiary" type="button" onClick={onRequest}>
              Request
            </button>
          </div>
        </div>
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
      </div>
    </div>
  );
};

export default CodeSpaceRecipe;
