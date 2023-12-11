import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './CodeSpaceRecipie.scss';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import TextBox from 'components/mbc/shared/textBox/TextBox';
import Modal from 'components/formElements/modal/Modal';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { Envs } from 'globals/Envs';
import { isValidGITRepoUrl } from '../../../../services/utils';
const classNames = cn.bind(Styles);
const CodeSpaceRecipe = () => {
  const requiredError = '*Missing entry';
  const softwareError = '*Please enter software name and version to save';
  const [recipeName, setRecipeName] = useState('');
  const [recipeType, setRecipeType] = useState('');
  const [gitUrl, setGitUrl] = useState('');
  const [diskSpace, setDiskSpace] = useState('');
  const [minCpu, setMinCpu] = useState('');
  const [maxCpu, setMaxCpu] = useState('');
  const [minRam, setMinRam] = useState('');
  const [maxRam, setMaxRam] = useState('');
  const [showAddSoftwareModel, setShowAddSoftwareModel] = useState(false);
  const [softwareName, setsoftwareName] = useState('');
  const [softwareVersion, setSoftwareVersion] = useState('');
  const [customSoftwareName, setCustomSoftwareName] = useState('');
  const [customSoftwareVersion, setCustomSoftwareVersion] = useState('');
  const [softwareList, setSoftwareList] = useState([]);
  const [softwareErrorText, setSoftwareErrorText] = useState(false);
  const [showDeletePopUp, setShowDeletePopUp] = useState(false);
  const [ItemIndexToDelete, setItemIndexToDelete] = useState(0);
  const [errorObj, setErrorObj] = useState({
    recipeName: '',
    recipieType: '',
    gitUrl: '',
    discSpace: '',
    minCpu: '',
    maxCpu: '',
    minRam: '',
    maxRam: '',
  });
  const isPublicRecipeChoosen = recipeType.startsWith('public');
  console.log(isPublicRecipeChoosen);
  const githubUrlValue = isPublicRecipeChoosen ? 'https://github.com/' : Envs.CODE_SPACE_GIT_PAT_APP_URL;
  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, []);

  const onRecipeNameChange = (e: React.FormEvent<HTMLInputElement>) => {
    const value = e.currentTarget.value;
    setRecipeName(value);
  };

  const onRecipeTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = e.currentTarget.value;
    setRecipeType(selectedOption);
    console.log(recipeType);
  };

  const onGitUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const githubUrlVal = e.currentTarget.value.trim();
    setGitUrl(githubUrlVal);
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

  const onDiskSpaceChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setDiskSpace(value);
  };

  const onMinCpuChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setMinCpu(value);
  };

  const onMaxCpuChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setMaxCpu(value);
  };

  const onMinRamChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setMinRam(value);
  };

  const onMaxRamChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setMaxRam(value);
  };

  const showAddSoftwareModelView = () => {
    setShowAddSoftwareModel(true);
    setTimeout(function () {
      SelectBox.defaultSetup(true);
    }, 10);
  };

  const onAddNewSoftwareCancel = () => {
    setShowAddSoftwareModel(false);
    setsoftwareName('');
    setSoftwareVersion('');
    setSoftwareErrorText(false);
    setCustomSoftwareName('');
    setCustomSoftwareVersion('');
  };

  const onSoftwareNameChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setsoftwareName(value);
  };

  const onSoftwareVersionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.currentTarget.value;
    setSoftwareVersion(value);
  };

  const onCustomSoftwareNameChange = (e: React.FormEvent<HTMLInputElement>) => {
    const value = e.currentTarget.value;
    setCustomSoftwareName(value);
  };

  const oncustomSoftwareVersionChange = (e: React.FormEvent<HTMLInputElement>) => {
    const value = e.currentTarget.value;
    setCustomSoftwareVersion(value);
  };

  const onSoftwareSave = () => {
    if (softwareName !== '') {
      if (softwareName !== 'others') {
        const software = {
          softwareName: softwareName,
          softwareVersion: softwareVersion,
        };
        setSoftwareList((preList) => [...preList, software]);
      } else {
        if (customSoftwareName !== '' && customSoftwareVersion !== '') {
          const software = {
            softwareName: customSoftwareName,
            softwareVersion: customSoftwareVersion,
          };
          setSoftwareList((preList) => [...preList, software]);
        } else {
          setSoftwareErrorText(true);
        }
      }
      setsoftwareName('');
      setSoftwareVersion('');
      setShowAddSoftwareModel(false);
      setSoftwareErrorText(false);
    } else {
      setSoftwareErrorText(true);
    }
  };

  const onDeleteSoftware = (index: any) => {
    setShowDeletePopUp(false);
    setSoftwareList((prevList) => {
      const updatedList = [...prevList.slice(0, index), ...prevList.slice(index + 1)];
      return updatedList;
    });
  };

  const onSave = () => {
    if (validateForm()) {
      console.log(errorObj);
    } else {
      console.log(errorObj);
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
        recipieType: requiredError,
      }));
    }
    if (gitUrl === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        gitUrl: requiredError,
      }));
    }
    if (diskSpace === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        discSpace: requiredError,
      }));
    }
    if (minCpu === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        minCpu: requiredError,
      }));
    }
    if (maxCpu === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        maxCpu: requiredError,
      }));
    }
    if (minRam === '') {
      isValid = false;
      setErrorObj((prevState) => ({
        ...prevState,
        minRam: requiredError,
      }));
    }
    if (maxRam === '') {
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
              <h3>Add New Recipe</h3>
              <div className={classNames(Styles.formWrapper)}>
                <div className={classNames(Styles.flexLayout)}>
                  <TextBox
                    type="text"
                    controlId={'recipeNameInput'}
                    labelId={'recipieNameLabel'}
                    label={'Recipie Name'}
                    placeholder={'Type here'}
                    value={recipeName}
                    errorText={errorObj.recipeName}
                    required={true}
                    maxLength={200}
                    onChange={onRecipeNameChange}
                  />
                  <div className={classNames('input-field-group include-error', errorObj.recipieType ? 'error' : '')}>
                    <label id="recipeLabel" className="input-label" htmlFor="recipeSelect">
                      Code Recipe Type<sup>*</sup>
                    </label>
                    <div id="selectBox" className="custom-select">
                      <select
                        id="recipeTypeField"
                        required={true}
                        required-error={errorObj.recipieType}
                        value={recipeType}
                        onChange={onRecipeTypeChange}
                      >
                        <option value=''>Choose</option>
                        <option value="public GitHub">Public GitHub</option>
                        <option value="private GitHub">Private GitHub</option>
                        <option value="from Storage">From Storage</option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.recipieType.length ? '' : 'hide')}>
                      {errorObj.recipieType}
                    </span>
                  </div>
                </div>
                <div className={classNames(Styles.flexLayout)}>
                  <div className={classNames(!recipeType || recipeType === '' ? Styles.disabledDiv : '')}>
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
                        <option value={1}> 1 GB</option>
                        <option value={2}> 2 GB</option>
                        <option value={3}> 3 GB</option>
                        <option value={4}> 4 GB</option>
                        <option value={5}> 5 GB</option>
                        <option value={6}> 6 GB</option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.discSpace.length ? '' : 'hide')}>
                      {errorObj.discSpace}
                    </span>
                  </div>
                </div>
                <div className={classNames(Styles.SelectBoxWrapper)}>
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
                        <option value={1}> 1 GB</option>
                        <option value={2}> 2 GB</option>
                        <option value={3}> 3 GB</option>
                        <option value={4}> 4 GB</option>
                        <option value={5}> 5 GB</option>
                        <option value={6}> 6 GB</option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.minCpu.length ? '' : 'hide')}>
                      {errorObj.minCpu}
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
                        <option value={1}> 1 GB</option>
                        <option value={2}> 2 GB</option>
                        <option value={3}> 3 GB</option>
                        <option value={4}> 4 GB</option>
                        <option value={5}> 5 GB</option>
                        <option value={6}> 6 GB</option>
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
                        <option value={1}> 1 GB</option>
                        <option value={2}> 2 GB</option>
                        <option value={3}> 3 GB</option>
                        <option value={4}> 4 GB</option>
                        <option value={5}> 5 GB</option>
                        <option value={6}> 6 GB</option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.minRam.length ? '' : 'hide')}>
                      {errorObj.minRam}
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
                        <option value={1}> 1 GB</option>
                        <option value={2}> 2 GB</option>
                        <option value={3}> 3 GB</option>
                        <option value={4}> 4 GB</option>
                        <option value={5}> 5 GB</option>
                        <option value={6}> 6 GB</option>
                      </select>
                    </div>
                    <span className={classNames('error-message', errorObj.maxCpu.length ? '' : 'hide')}>
                      {errorObj.maxCpu}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div className={classNames(Styles.firstPanel, 'addSoftware')}>
              <h3>Add Softwares required</h3>
              <div className={classNames(Styles.softwareListWrapper)}>
                <div className={classNames(Styles.addsoftwareWrapper)}>
                  <button id="AddTeamMemberBtn" onClick={showAddSoftwareModelView}>
                    <i className="icon mbc-icon plus" />
                    <span>Add software</span>
                  </button>
                </div>
              </div>
              {showAddSoftwareModel && (
                <div>
                  <Modal
                    title={''}
                    hiddenTitle={true}
                    showAcceptButton={true}
                    showCancelButton={false}
                    acceptButtonTitle="Save"
                    onAccept={onSoftwareSave}
                    modalWidth="800px"
                    buttonAlignment="right"
                    show={showAddSoftwareModel}
                    content={
                      <div>
                        <h3>Please Select the Required software and its Version</h3>
                        <div className={classNames(Styles.flexLayout)}>
                          <div
                            className={classNames('input-field-group include-error', softwareErrorText ? 'error' : '')}
                          >
                            <label id="softwareNameLable" className="input-label" htmlFor="softwareNameSelect">
                              Software Name
                            </label>
                            <div id="selectBox" className="custom-select">
                              <select
                                id="softwareNameField"
                                required={true}
                                required-error={requiredError}
                                value={softwareName}
                                onChange={onSoftwareNameChange}
                              >
                                <option value="">Choose</option>
                                <option value="Software 1">Software 1</option>
                                <option value="Software 2">Software 2</option>
                                <option value="Software 3">Software 3</option>
                                <option value="Software 4">Software 4</option>
                                <option value="others">others</option>
                              </select>
                            </div>
                          </div>
                          <div
                            className={classNames(
                              'input-field-group include-error',
                              softwareErrorText ? 'error' : '',
                              softwareName === 'others' ? 'disabled' : '',
                            )}
                          >
                            <label id="softwareVersionLable" className="input-label" htmlFor="softwareVersionSelect">
                              Software Version
                            </label>
                            <div
                              id="selectBox"
                              className={classNames('custom-select', softwareName === 'others' ? 'disabled' : '')}
                            >
                              <select
                                id="softwareVersionField"
                                required={true}
                                required-error={requiredError}
                                value={softwareVersion}
                                onChange={onSoftwareVersionChange}
                              >
                                <option value="">Choose</option>
                                <option value="Version 1">Version 1</option>
                                <option value="Version 2">Version 2</option>
                                <option value="Version 3">Version 3</option>
                                <option value="Version 4">Version 4</option>
                              </select>
                            </div>
                          </div>
                        </div>
                        <div>
                          {softwareName === 'others' ? (
                            <div className={classNames(Styles.flexLayout)}>
                              <TextBox
                                type="text"
                                controlId={'customSoftwareName'}
                                labelId={'customSoftwareNameLabel'}
                                label={' Custom Software Name'}
                                placeholder={'Type here'}
                                value={customSoftwareName}
                                required={false}
                                maxLength={200}
                                onChange={onCustomSoftwareNameChange}
                              />
                              <TextBox
                                type="text"
                                controlId={'customSoftwareVersion'}
                                labelId={'customSoftwareVersionLabel'}
                                label={'Custom Software Version'}
                                placeholder={'Type here'}
                                value={customSoftwareVersion}
                                required={false}
                                maxLength={200}
                                onChange={oncustomSoftwareVersionChange}
                              />
                            </div>
                          ) : null}
                        </div>
                        <span className="error-message">{softwareErrorText ? softwareError : ''}</span>
                        {/* <div className={Styles.btnConatiner}>
                          <button className="btn btn-primary" type="button" onClick={onSoftwareSave}>
                            Save
                          </button>
                        </div> */}
                      </div>
                    }
                    scrollableContent={false}
                    onCancel={onAddNewSoftwareCancel}
                  />
                </div>
              )}
              <div className={Styles.softwareList}>
                {softwareList ? (
                  softwareList.length > 0 ? (
                    <div>
                      <table className="ul-table users">
                        <thead>
                          <tr className="header-row">
                            <th>
                              <label>software Name</label>
                            </th>

                            <th>
                              <label>Version</label>
                            </th>

                            <th className="text-center">
                              <label>Action</label>
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          {softwareList &&
                            softwareList.map((software, index) => {
                              return (
                                <tr className="data-row" key={index}>
                                  <td>{software.softwareName}</td>
                                  <td>{software.softwareVersion}</td>
                                  <td className="text-center">
                                    <i
                                      onClick={() => {
                                        setItemIndexToDelete(index);
                                        setShowDeletePopUp(true);
                                      }}
                                      className={classNames(Styles.deleteIcon, 'icon delete')}
                                    />
                                  </td>
                                </tr>
                              );
                            })}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    ''
                  )
                ) : (
                  ''
                )}
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.btnConatiner}>
          <button className="btn btn-primary" type="button" onClick={onSave}>
            Save
          </button>
        </div>
        {showDeletePopUp && (
          <ConfirmModal
            title={''}
            showAcceptButton={true}
            showCancelButton={true}
            acceptButtonTitle={'Confirm'}
            cancelButtonTitle={'Cancel'}
            show={showDeletePopUp}
            removalConfirmation={true}
            content={
              <div style={{ margin: '35px 0', textAlign: 'center' }}>
                <div>Are you sure to remove the software?</div>
              </div>
            }
            onCancel={() => {
              setShowDeletePopUp(false);
            }}
            onAccept={() => {
              onDeleteSoftware(ItemIndexToDelete);
            }}
          />
        )}
      </div>
    </div>
  );
};

export default CodeSpaceRecipe;
