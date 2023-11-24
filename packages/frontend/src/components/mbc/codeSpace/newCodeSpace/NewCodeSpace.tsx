import React, { useState } from 'react';
import cn from 'classnames';
import Styles from './NewCodeSpace.scss';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../../../assets/modules/uilab/js/src/tooltip';
import SelectBox from 'components/formElements/SelectBox/SelectBox';

import { isValidGITRepoUrl, trackEvent } from '../../../../services/utils';
import TextBox from '../../shared/textBox/TextBox';
import { ICodeSpaceData } from '../CodeSpace';
import { useEffect } from 'react';
import { ICodeCollaborator, IUserDetails, IUserInfo } from 'globals/types';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import AddUser from '../../addUser/AddUser';
import { Envs } from 'globals/Envs';
import { recipesMaster } from '../../../../services/utils';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { DEPLOYMENT_DISABLED_RECIPE_IDS } from 'globals/constants';

const classNames = cn.bind(Styles);

export interface ICodeSpaceProps {
  user: IUserInfo;
  onBoardingCodeSpace?: ICodeSpaceData;
  onEditingCodeSpace?: ICodeSpaceData;
  isCodeSpaceCreationSuccess?: (status: boolean, codeSpaceData: ICodeSpaceData) => void;
  toggleProgressMessage?: (show: boolean) => void;
  onUpdateCodeSpaceComplete?: () => void;
}

export interface ICodeSpaceRef {
  validateAndCreateCodeSpace: () => void;
}

export interface ICreateCodeSpaceData {
  recipeId: string;
  password: string;
}

const NewCodeSpace = (props: ICodeSpaceProps) => {
  const onBoadingMode = props.onBoardingCodeSpace !== undefined;
  const onEditingMode = props.onEditingCodeSpace !== undefined;
  const [projectName, setProjectName] = useState('');
  const [projectNameError, setProjectNameError] = useState('');
  const [environment, setEnvironment] = useState('DHC-CaaS');
  const [recipeValue, setRecipeValue] = useState('0');
  const recipes = recipesMaster;

  const [recipeError, setRecipeError] = useState('');

  const [isUserDefinedGithubRecipe, setIsUserDefinedGithubRecipe] = useState(false);
  const [userDefinedGithubUrl, setUserDefinedGithubUrl] = useState('');
  const [userDefinedGithubUrlError, setUserDefinedGithubUrlError] = useState('');
  
  // const [githubUserName, setGithubUserName] = useState('');
  // const [githubUserNameError, setGithubUserNameError] = useState('');
  const [githubToken, setGithubToken] = useState('');
  const [githubTokenError, setGithubTokenError] = useState('');
  const [codeSpaceCollaborators, setCodeSpaceCollaborators] = useState([]);
  // const [codeSpaceCollaboratorsError, setCodeSpaceCollaboratorsError] = useState('');


  const [livelinessInterval, setLivelinessInterval] = useState<NodeJS.Timer>();

  // const [createdCodeSpaceName, setCreatedCodeSpaceName] = useState('');

  const [showConfirmModal, setShowConfirmModal] =  useState(false);
  const [collaboratorToDelete, setCollaboratorToDelete] =  useState<ICodeCollaborator>();
  const [collaboratorToTransferOwnership, setCollaboratorToTransferOwnership] =  useState<ICodeCollaborator>();

  const requiredError = '*Missing entry';
  const livelinessIntervalRef = React.useRef<NodeJS.Timer>();
  // let livelinessInterval: any = undefined;

  useEffect(() => {
    if (onEditingMode && props.onEditingCodeSpace.projectDetails?.projectCollaborators) {
      setCodeSpaceCollaborators([...props.onEditingCodeSpace.projectDetails?.projectCollaborators]);
    }
    SelectBox.defaultSetup(true);
  }, []);

  useEffect(() => {
    livelinessIntervalRef.current = livelinessInterval;
    return () => {
      livelinessIntervalRef.current && clearInterval(livelinessIntervalRef.current);
    };
  }, [livelinessInterval]);

  useEffect(() => {
    if (onEditingMode) {
      addNewCollaborator();
    }
    Tooltip.defaultSetup();
  }, [codeSpaceCollaborators]);

  const sanitizedRepositoryName = (name: string) => {
    return name.replace(/[^\w.-]/g, '-');
  };

  const onProjectNameOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const projectNameVal = sanitizedRepositoryName(evnt.currentTarget.value);
    setProjectName(projectNameVal);
    const noSpaceNoSpecialChars = /[A-Za-z0-9_.-]/.test(projectNameVal);
    setProjectNameError(
      !noSpaceNoSpecialChars
        ? projectNameVal.length
          ? 'Invalid name - Space and Special Chars not allowed'
          : requiredError
        : '',
    );
  };

  // const onGithubUserNameOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
  //   const githubUserNameVal = evnt.currentTarget.value.trim();
  //   setGithubUserName(githubUserNameVal);
  //   setGithubUserNameError(githubUserNameVal.length ? '' : requiredError);
  // };
  
  const onUserDefinedGithubUrlOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const githubUrlVal = evnt.currentTarget.value.trim();
    setUserDefinedGithubUrl(githubUrlVal);
    setUserDefinedGithubUrlError(
      githubUrlVal.length
        ? isValidGITRepoUrl(githubUrlVal, isPublicRecipeChoosen)
          ? ''
          : `Please provide valid ${isPublicRecipeChoosen ? 'https://github.com/' : Envs.CODE_SPACE_GIT_PAT_APP_URL} git repository clone url.`
        : requiredError,
    );
  };
  
  const onGithubTokenOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const githubTokenVal = evnt.currentTarget.value.trim();
    setGithubToken(githubTokenVal);
    setGithubTokenError(githubTokenVal.length ? '' : requiredError);
  };

  const onEnvironmentChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    setEnvironment(evnt.currentTarget.value.trim());
  };

  const onRecipeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = e.currentTarget.value;
    setRecipeValue(selectedOption);
    const isUserDefinedRecipe = selectedOption === 'public-user-defined' || selectedOption === 'private-user-defined';
    setIsUserDefinedGithubRecipe(isUserDefinedRecipe);
    if (!isUserDefinedRecipe) {
      setUserDefinedGithubUrl('');
      setUserDefinedGithubUrlError('');
    }
    setRecipeError(selectedOption !== '0' ? '' : requiredError);
  };

  // User Name
  const namePrefix = props.user.firstName;

  const getCollabarators = (collaborator: IUserDetails) => {
    const collabarationData = {
      firstName: collaborator.firstName,
      lastName: collaborator.lastName,
      id: collaborator.shortId,
      department: collaborator.department,
      email: collaborator.email,
      mobileNumber: collaborator.mobileNumber,
      gitUserName: collaborator.shortId,
      // permission: { read: true, write: false },
    };

    let duplicateMember = false;
    duplicateMember = codeSpaceCollaborators.filter((member: ICodeCollaborator) => member.id === collaborator.shortId)?.length
      ? true
      : false;
    const isCreator = props.user.id === collaborator.shortId;

    if (duplicateMember) {
      Notification.show('Collaborator Already Exist.', 'warning');
    } else if (isCreator) {
      Notification.show(
        `${collaborator.firstName} ${collaborator.lastName} is a creator. Creator can't be added as collaborator.`,
        'warning',
      );
    } else {
      codeSpaceCollaborators.push(collabarationData);
      setCodeSpaceCollaborators([...codeSpaceCollaborators]);
    }
  };

  const onCollaboratorPermission = (e: React.ChangeEvent<HTMLInputElement>, userId: string) => {
    const codeSpaceCollaborator = codeSpaceCollaborators.find((item: ICodeCollaborator) => {
      return item.id == userId;
    });

    if (e.target.checked) {
      codeSpaceCollaborator.canDeploy = true;
    } else {
      codeSpaceCollaborator.canDeploy = false;
    }
    setCodeSpaceCollaborators([...codeSpaceCollaborators]);
  };

  const addNewCollaborator = () => {
    if (props.onEditingCodeSpace.projectDetails?.projectCollaborators === null) {
      props.onEditingCodeSpace.projectDetails.projectCollaborators = [];
    }
    const existingColloborators = props.onEditingCodeSpace.projectDetails?.projectCollaborators || [];
    const newCollaborator = codeSpaceCollaborators.find((collab: ICodeCollaborator) => !existingColloborators.some((existCollab: ICodeCollaborator) => existCollab.id === collab.id));
    if (newCollaborator) {
      ProgressIndicator.show();
      CodeSpaceApiClient.addCollaborator(props.onEditingCodeSpace.id, newCollaborator).then((res) => {
        ProgressIndicator.hide();
        if (res.success === 'SUCCESS') {
          trackEvent('DnA Code Space', 'Add New Collaborator', 'Existing Code Space');
          props.onEditingCodeSpace.projectDetails?.projectCollaborators?.push(newCollaborator);
          Notification.show(
            `Collaborator '${newCollaborator.firstName}' has been added successfully to the Code Space.`,
          );
        } else {
          setCodeSpaceCollaborators([...existingColloborators]);
          Notification.show(
            `Error adding collaborator '${newCollaborator.firstName}' to the Code Space. Please try again later.`,
            'alert',
          );
        }
      })
      .catch((err: Error) => {
        ProgressIndicator.hide();
        if (err.message === 'User is already part of a collaborator') {
          Notification.show(
            `Colloborator '${newCollaborator.firstName}' already in the Code Space. Please use another user as colloborator.`,
            'alert',
          );
        } else {
          setCodeSpaceCollaborators([...existingColloborators]);
          Notification.show('Error in adding new colloborator code space. Please try again later.\n' + err.message, 'alert');
        }
      });
    }
  }

  const onCollaboratorDelete = (userId: string) => {
    return () => {
      if (onEditingMode) {
        setCollaboratorToDelete(codeSpaceCollaborators.find((collab: ICodeCollaborator) => collab.id === userId));
        setShowConfirmModal(true);
      } else {
        updateCollaborator(userId);
      }
    };
  };

  const updateCollaborator = (userId: string) => {
    const currentCollList = codeSpaceCollaborators.filter((item: ICodeCollaborator) => {
      return item.id !== userId;
    });
    setCodeSpaceCollaborators(currentCollList);
    return currentCollList;
  };

  const onCollaboratorConfirmModalCancel = () => {
    setCollaboratorToDelete(undefined);
    setCollaboratorToTransferOwnership(undefined);
    setShowConfirmModal(false);
  };

  const onCollaboratorConfirmModalAccept = () => {
    ProgressIndicator.show();
    collaboratorToDelete && processDeleteCollaborator();
    collaboratorToTransferOwnership && processTransferOwnership();
    setShowConfirmModal(false);
  };

  const processDeleteCollaborator = () => {
    CodeSpaceApiClient.deleteCollaborator(props.onEditingCodeSpace.id, collaboratorToDelete.id).then((res) => {
      ProgressIndicator.hide();
      if (res.success === 'SUCCESS') {
        trackEvent('DnA Code Space', 'Delete Collaborator', 'Existing Code Space');
        props.onEditingCodeSpace.projectDetails.projectCollaborators = [...updateCollaborator(collaboratorToDelete.id)];
        Notification.show(
          `Collaborator '${collaboratorToDelete.firstName}' has been removed successfully from the Code Space.`,
        );
      } else {
        Notification.show(
          `Error removing collaborator '${collaboratorToDelete.firstName}' from the Code Space. Please try again later.`,
          'alert',
        );
      }
    })
    .catch((err: Error) => {
      ProgressIndicator.hide();
      Notification.show('Error in removing colloborator from code space. Please try again later.\n' + err.message, 'alert');
    });
    setCollaboratorToDelete(undefined);
  };

  const processTransferOwnership = () => {
    CodeSpaceApiClient.transferOwnership(props.onEditingCodeSpace.id, {
      id: collaboratorToTransferOwnership.id,
    })
      .then((res) => {
        ProgressIndicator.hide();
        if (res.success === 'SUCCESS') {
          trackEvent('DnA Code Space', 'Transfer Ownership', 'Existing Code Space');
          Notification.show(
            `Code Space '${props.onEditingCodeSpace?.projectDetails?.projectName}' ownership successfully transferred to collaborator '${collaboratorToTransferOwnership.firstName}'.`,
          );
          props.onUpdateCodeSpaceComplete();
        } else {
          Notification.show(
            `Error transferring Code Space ownership to collaborator '${collaboratorToTransferOwnership.firstName}'. Please try again later.`,
            'alert',
          );
        }
      })
      .catch((err: Error) => {
        ProgressIndicator.hide();
        Notification.show(
          'Error in transferring Code Space ownership. Please try again later.\n' + err.message,
          'alert',
        );
      });
    setCollaboratorToTransferOwnership(undefined);
  };

  const onTransferOwnership = (userId: string) => {
    setCollaboratorToTransferOwnership(codeSpaceCollaborators.find((collab: ICodeCollaborator) => collab.id === userId));
    setShowConfirmModal(true);
  };

  const validateNewCodeSpaceForm = (isPublicRecipeChoosen: boolean) => {
    let formValid = true;
    if (!projectName.length) {
      setProjectNameError(requiredError);
      formValid = false;
    }
    if (recipeValue === '0') {
      setRecipeError(requiredError);
      formValid = false;
    }
    // if (isPublicRecipeChoosen && githubUserName === '') {
    //   setGithubUserNameError(requiredError);
    //   formValid = false;
    // } else {
    //   setGithubUserNameError('');
    // }

    if (isPublicRecipeChoosen && isUserDefinedGithubRecipe && userDefinedGithubUrl === '') {
      setUserDefinedGithubUrlError(requiredError);
      formValid = false;
    } else {
      if (isValidGITRepoUrl(userDefinedGithubUrl, isPublicRecipeChoosen)) setUserDefinedGithubUrlError('');
    }
    if (githubToken === '') {
      setGithubTokenError(requiredError);
      formValid = false;
    }
    if (projectNameError !== '' || recipeError !== '' || githubTokenError !== '' || (isPublicRecipeChoosen && isUserDefinedGithubRecipe && userDefinedGithubUrlError !== '')) {
      formValid = false;
    }
    return formValid;
  };

  const validateOnBoardCodeSpaceForm = (isPublicRecipeChoosen: boolean) => {
    let formValid = true;
    // if (isPublicRecipeChoosen && githubUserName === '') {
    //   setGithubUserNameError(requiredError);
    //   formValid = false;
    // } else {
    //   setGithubUserNameError('');
    // }
    if (githubToken === '') {
      setGithubTokenError(requiredError);
      formValid = false;
    }
    if (githubTokenError !== '') {
      formValid = false;
    }
    return formValid;
  };

  const enableLivelinessCheck = (id: string) => {
    clearInterval(livelinessInterval);
    const intervalId = setInterval(() => {
      CodeSpaceApiClient.getCodeSpaceStatus(id)
        .then((res: ICodeSpaceData) => {
          try {
            if (res.status === 'CREATED') {
              props.toggleProgressMessage(false);
              ProgressIndicator.hide();
              clearInterval(livelinessInterval);
              props.isCodeSpaceCreationSuccess(true, {
                ...res,
                running: true,
              });
              Notification.show('Code space succesfully created.');
            }
          } catch (err: any) {
            console.log(err);
          }
        })
        .catch((err: Error) => {
          clearInterval(livelinessInterval);
          props.toggleProgressMessage(false);
          ProgressIndicator.hide();
          Notification.show('Error in validating code space - ' + err.message, 'alert');
        });
    }, 2000);
    setLivelinessInterval(intervalId);
  };

  // let livelinessInterval: any = undefined;
  // const enableLivelinessCheck = () => {
  //   clearInterval(livelinessInterval);
  //   livelinessInterval = setInterval(() => {
  //     ApiClient.getCodeSpace()
  //       .then((res: any) => {
  //         if (res.success === 'true') {
  //           props.toggleProgressMessage(false);
  //           ProgressIndicator.hide();
  //           clearInterval(livelinessInterval);
  //           props.isCodeSpaceCreationSuccess(true, {
  //             url: `https://code-spaces.dev.dna.app.corpintra.net/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
  //             running: true,
  //           });
  //           Notification.show('Code space succesfully created.');
  //         }
  //       })
  //       .catch((err: Error) => {
  //         clearInterval(livelinessInterval);
  //         props.toggleProgressMessage(false);
  //         ProgressIndicator.hide();
  //         Notification.show('Error in validating code space - ' + err.message, 'alert');
  //       });
  //   }, 2000);
  // };

  const createCodeSpace = () => {
    const isPublicRecipeChoosen = recipeValue.startsWith('public');
    const recipe = recipesMaster.find((item: any) => item.id === recipeValue);

    if (validateNewCodeSpaceForm(isPublicRecipeChoosen)) {
      const createCodeSpaceRequest = {
        data: {
          gitUserName: props.user.id,
          projectDetails: {
            projectCollaborators: codeSpaceCollaborators,
            projectName: projectName,
            recipeDetails: {
              cloudServiceProvider: environment,
              cpuCapacity: '1',
              environment: 'Development', // Need to handled in backend
              operatingSystem: 'Debian-OS-11',
              ramSize: '1',
              recipeId: recipeValue,
              resource: recipe.resource
            }
          }
        },
        pat: githubToken
      };

      if (isPublicRecipeChoosen || isUserDefinedGithubRecipe || recipe.repodetails) {
        // createCodeSpaceRequest.data.gitUserName = githubUserName;
        // createCodeSpaceRequest.data.projectDetails.recipeDetails.recipeId = 'public';
        createCodeSpaceRequest.data.projectDetails.recipeDetails['repodetails'] = isUserDefinedGithubRecipe ? (userDefinedGithubUrl.split('://')[1] + ',') : recipe.repodetails;
      }

      ProgressIndicator.show();
      CodeSpaceApiClient.createCodeSpace(createCodeSpaceRequest)
        .then((res) => {
          trackEvent('DnA Code Space', 'Create', 'New code space');
          if (res.data.status === 'CREATE_REQUESTED') {
            // setCreatedCodeSpaceName(res.data.name);
            props.toggleProgressMessage(true);
            enableLivelinessCheck(res.data.workspaceId);
          } else {
            props.toggleProgressMessage(false);
            ProgressIndicator.hide();
            Notification.show(
              'Error in creating new code space. Please try again later.\n' + res.errors[0].message,
              'alert',
            );
          }
        })
        .catch((err: Error) => {
          props.toggleProgressMessage(false);
          ProgressIndicator.hide();
          if (err.message === 'Value or Item already exist!') {
            Notification.show(
              `Given Code Space Name '${projectName}' already in use. Please use another name.`,
              'alert',
            );
          } else {
            Notification.show('Error in creating new code space. Please try again later.\n' + err.message, 'alert');
          }
        });
    }

    // const codeSpaceData = {
    //   recipeId: recipeValues.join(''),
    //   password: passwordInput.password,
    // };
    // if (validateNewCodeSpaceForm()) {
    //   ProgressIndicator.show();
    //   ApiClient.createCodeSpace(codeSpaceData)
    //     .then((res) => {
    //       trackEvent('DnA Code Space', 'Create', 'New code space');
    //       if(res.success === 'Success') {
    //         props.toggleProgressMessage(true);
    //         enableLivelinessCheck();
    //       } else {
    //         props.toggleProgressMessage(false);
    //         ProgressIndicator.hide();
    //         Notification.show('Error in creating new code space. Please try again later.\n' + res.errors[0].message, 'alert');
    //       }
    //     })
    //     .catch((err: Error) => {
    //       props.toggleProgressMessage(false);
    //       ProgressIndicator.hide();
    //       Notification.show('Error in creating new code space. Please try again later.\n' + err, 'alert');
    //     });
    // }
  };

  const onBoardToCodeSpace = () => {
    const isPublicRecipeChoosen = recipeValue.startsWith('public');

    if (validateOnBoardCodeSpaceForm(isPublicRecipeChoosen)) {

      const onBoardCodeSpaceRequest = {
        pat: githubToken
      };

      // if (isPublicRecipeChoosen) {
      //   onBoardCodeSpaceRequest['gitUserName'] = githubUserName;
      // }

      ProgressIndicator.show();
      CodeSpaceApiClient.onBoardCollaborator(props.onBoardingCodeSpace.id, onBoardCodeSpaceRequest)
        .then((res) => {
          trackEvent('DnA Code Space', 'Create', 'New code space');
          if (res.data?.status === 'CREATE_REQUESTED') {
            // setCreatedCodeSpaceName(res.data.name);
            props.toggleProgressMessage(true);
            enableLivelinessCheck(res.data.workspaceId);
          } else {
            props.toggleProgressMessage(false);
            ProgressIndicator.hide();
            Notification.show(
              'Error in creating new code space. Please try again later.\n' + res.errors[0].message,
              'alert',
            );
          }
        })
        .catch((err: Error) => {
          props.toggleProgressMessage(false);
          ProgressIndicator.hide();
          if (err.message === 'Value or Item already exist!') {
            Notification.show(
              `Given Code Space Name '${projectName}' already in use. Please use another name.`,
              'alert',
            );
          } else {
            Notification.show('Error in creating new code space. Please try again later.\n' + err.message, 'alert');
          }
        });
    }
  };

  // const updateCodeSpace = () => {
  //   const existingColloborators = props.onEditingCodeSpace.projectDetails?.projectCollaborators || [];
  //   const newCollaborators = codeSpaceCollaborators.filter((collab: ICodeCollaborator) => !existingColloborators.some((existCollab: ICodeCollaborator) => existCollab.id === collab.id));
  // };

  const projectDetails = props.onBoardingCodeSpace?.projectDetails || props.onEditingCodeSpace?.projectDetails;
  const isPublicRecipeChoosen = recipeValue.startsWith('public');
  const githubUrlValue = isPublicRecipeChoosen ? 'https://github.com/' : Envs.CODE_SPACE_GIT_PAT_APP_URL;
  return (
    <React.Fragment>
      {onBoadingMode ? (
        <div className={Styles.newCodeSpacePanel}>
          <div className={Styles.addicon}> &nbsp; </div>
          <h3>
            Hello {namePrefix}, On-board to Code Space - {projectDetails.projectName}
          </h3>
          <p>Protect your code space with the password of your own.</p>
          <div className={Styles.codeSpaceDetails}>
            <div className={Styles.flexLayout}>
              <div>
                <label>Name</label>
              </div>
              <div>{projectDetails.projectName}</div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <label>Recipe</label>
              </div>
              <div>{recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId).name}</div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <label>Environment</label>
              </div>
              <div>{projectDetails.recipeDetails.cloudServiceProvider}</div>
            </div>
          </div>
          {/* <div className={Styles.flexLayout}>
            <div>
              <TextBox
                type="password"
                controlId={'codeSpacePasswordInput'}
                name="password"
                labelId={'codeSpacePasswordLabel'}
                label={'Code Space Password (min 8 chars & alpha numeric)'}
                placeholder={'Type here'}
                value={passwordInput.password}
                errorText={passwordError}
                required={true}
                maxLength={20}
                onChange={handlePasswordChange}
                onKeyUp={handleValidation}
              />
            </div>
            <div>
              <TextBox
                type="password"
                controlId={'codeSpaceConfirmPasswordInput'}
                name="confirmPassword"
                labelId={'codeSpaceConfirmPasswordLabel'}
                label={'Confirm Code Space Password'}
                placeholder={'Type here'}
                value={passwordInput.confirmPassword}
                errorText={confirmPasswordError}
                required={true}
                maxLength={20}
                onChange={handlePasswordChange}
                onKeyUp={handleValidation}
              />
            </div>
          </div> */}
          {/* {isPublicRecipeChoosen && (
            <div>
              <div>
                <TextBox
                  type="text"
                  controlId={'githubTokenInput'}
                  labelId={'githubTokenLabel'}
                  label={`Your Github(https://github.com/) Username`}
                  infoTip="Not stored only used for Code Space initial setup"
                  placeholder={'Type here'}
                  value={githubUserName}
                  errorText={githubUserNameError}
                  required={true}
                  maxLength={50}
                  onChange={onGithubUserNameOnChange}
                />
              </div>
            </div>
          )} */}
          <div>
            <div>
              <TextBox
                type="password"
                controlId={'githubTokenInput'}
                labelId={'githubTokenLabel'}
                label={`Your Github(${githubUrlValue}) Personal Access Token`}
                infoTip="Not stored only used for Code Space initial setup"
                placeholder={'Type here'}
                value={githubToken}
                errorText={githubTokenError}
                required={true}
                maxLength={50}
                onChange={onGithubTokenOnChange}
              />
            </div>
          </div>
          <div className={Styles.newCodeSpaceBtn}>
            <button className={' btn btn-tertiary '} onClick={onBoardToCodeSpace}>
              On-board to Code Space
            </button>
          </div>
        </div>
      ) : (
        <div className={Styles.newCodeSpacePanel}>
          {!onEditingMode ? (
            <>
              <div className={Styles.addicon}> &nbsp; </div>
              <h3>Hello {namePrefix}, Create your Code Space</h3>
              <p>Protect your code space with the password of your own.</p>
              {/* <p className={Styles.passwordInfo}>Note: Password should be minimum 8 chars in length and alpha numeric.</p> */}
              <div
                id="recipeContainer"
                className={classNames('input-field-group include-error', recipeError.length ? 'error' : '')}
              >
                <label id="recipeLabel" className="input-label" htmlFor="recipeSelect">
                  Code Recipe<sup>*</sup>
                </label>
                <div id="recipe" className="custom-select">
                  <select
                    id="recipeSelect"
                    multiple={false}
                    required={true}
                    required-error={requiredError}
                    onChange={onRecipeChange}
                    value={recipeValue}
                  >
                    <option id="defaultStatus" value={0}>
                      Select Code Recipe
                    </option>
                    {recipes.map((obj: any) => (
                      <option key={obj.id} id={obj.name + obj.id} value={obj.id}>
                        {obj.name}
                      </option>
                    ))}
                  </select>
                </div>
                <span className={classNames('error-message', recipeError.length ? '' : 'hide')}>{recipeError}</span>
                <div>
                  <button className={classNames(Styles.addNewItemButton)}>
                    <i className="icon mbc-icon plus" />
                    &nbsp;
                    <span>Add new code space recipe (Coming Soon)</span>
                  </button>
                </div>
              </div>
              <div>
                <TextBox
                  type="text"
                  controlId={'productNameInput'}
                  labelId={'productNameLabel'}
                  label={'Code Space Name'}
                  placeholder={'Type here'}
                  value={projectName}
                  errorText={projectNameError}
                  required={true}
                  maxLength={39}
                  onChange={onProjectNameOnChange}
                />
              </div>
              <div>
                <div id="environmentContainer" className={classNames('input-field-group include-error')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Environment<sup>*</sup>
                  </label>
                  <div>
                    <label className={classNames('radio')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value={'DHC-CaaS'}
                          name="environment"
                          onChange={onEnvironmentChange}
                          checked={true}
                        />
                      </span>
                      <span className="label">DHC CaaS</span>
                    </label>
                    <label className={classNames('radio')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value="azure"
                          name="environment"
                          onChange={onEnvironmentChange}
                          checked={false}
                          disabled={true}
                        />
                      </span>
                      <span className="label">Azure (Coming Soon)</span>
                    </label>
                    <label className={classNames('radio')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value="extollo"
                          name="environment"
                          onChange={onEnvironmentChange}
                          checked={false}
                          disabled={true}
                        />
                      </span>
                      <span className="label">eXtollo (Coming Soon)</span>
                    </label>
                    <label className={classNames('radio')}>
                      <span className="wrapper">
                        <input
                          type="radio"
                          className="ff-only"
                          value="aws"
                          name="environment"
                          onChange={onEnvironmentChange}
                          checked={false}
                          disabled={true}
                        />
                      </span>
                      <span className="label">AWS (Coming Soon)</span>
                    </label>
                  </div>
                </div>
              </div>
              {/* <div className={Styles.flexLayout}>
                <div>
                  <TextBox
                    type="password"
                    controlId={'codeSpacePasswordInput'}
                    name="password"
                    labelId={'codeSpacePasswordLabel'}
                    label={'Code Space Password (min 8 chars & alpha numeric)'}
                    placeholder={'Type here'}
                    value={passwordInput.password}
                    errorText={passwordError}
                    required={true}
                    maxLength={20}
                    onChange={handlePasswordChange}
                    onKeyUp={handleValidation}
                  />
                </div>
                <div>
                  <TextBox
                    type="password"
                    controlId={'codeSpaceConfirmPasswordInput'}
                    name="confirmPassword"
                    labelId={'codeSpaceConfirmPasswordLabel'}
                    label={'Confirm Code Space Password'}
                    placeholder={'Type here'}
                    value={passwordInput.confirmPassword}
                    errorText={confirmPasswordError}
                    required={true}
                    maxLength={20}
                    onChange={handlePasswordChange}
                    onKeyUp={handleValidation}
                  />
                </div>
              </div> */}
              {/* {isPublicRecipeChoosen && (
                <div>
                  <div>
                    <TextBox
                      type="text"
                      controlId={'githubTokenInput'}
                      labelId={'githubTokenLabel'}
                      label={`Your Github(https://github.com/) Username`}
                      infoTip="Not stored only used for Code Space initial setup"
                      placeholder={'Type here'}
                      value={githubUserName}
                      errorText={githubUserNameError}
                      required={true}
                      maxLength={50}
                      onChange={onGithubUserNameOnChange}
                    />
                  </div>
                </div>
              )} */}
              {isUserDefinedGithubRecipe && (
                <div>
                  <div>
                    <TextBox
                      type="text"
                      controlId={'userDefinedGithubUrlInput'}
                      labelId={'userDefinedGithubUrlInputLabel'}
                      label={`Provide Your Github Clone Url (Ex. ${githubUrlValue}orgname-or-username/your-repo-name.git)`}
                      placeholder={`${githubUrlValue}orgname-or-username/your-repo-name.git`}
                      value={userDefinedGithubUrl}
                      errorText={userDefinedGithubUrlError}
                      required={true}
                      maxLength={300}
                      onChange={onUserDefinedGithubUrlOnChange}
                    />
                  </div>
                </div>
              )}
              <div>
                <div>
                  <TextBox
                    type="password"
                    controlId={'githubTokenInput'}
                    labelId={'githubTokenLabel'}
                    label={`Your Github(${githubUrlValue}) Personal Access Token`}
                    infoTip="Not stored only used for Code Space initial setup"
                    placeholder={'Type here'}
                    value={githubToken}
                    errorText={githubTokenError}
                    required={true}
                    maxLength={50}
                    onChange={onGithubTokenOnChange}
                  />
                </div>
              </div>
            </>
          ) : (
            <>
              <div className={Styles.editicon}>
                <i className="icon mbc-icon edit small " />
              </div>
              <h3>Edit Code Space - {projectDetails.projectName}</h3>
              <div className={Styles.codeSpaceDetails}>
                <div className={Styles.flexLayout}>
                  <div>
                    <label>Name</label>
                  </div>
                  <div>{projectDetails.projectName}</div>
                </div>
                <div className={Styles.flexLayout}>
                  <div>
                    <label>Recipe</label>
                  </div>
                  <div>{recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId).name}</div>
                </div>
                <div className={Styles.flexLayout}>
                  <div>
                    <label>Environment</label>
                  </div>
                  <div>{projectDetails.recipeDetails.cloudServiceProvider}</div>
                </div>
              </div>
              <ConfirmModal
                title={''}
                acceptButtonTitle="Yes"
                cancelButtonTitle="No"
                showAcceptButton={true}
                showCancelButton={true}
                show={showConfirmModal}
                content={
                  <>
                    {collaboratorToDelete && (
                      <div>
                        Are you sure to delete colloborator '{collaboratorToDelete?.firstName}'?
                        <p>
                          Removing collaborator from codespace make him/her deny the acccess to the code space and code.
                        </p>
                      </div>
                    )}
                    {collaboratorToTransferOwnership && (
                      <div>
                        Are you sure to transfer code space ownership to the colloborator '
                        {collaboratorToTransferOwnership?.firstName}'?
                        <p>
                          Transfering ownership to another collaborator will deny you to acccess this code space edit
                          mode.
                        </p>
                      </div>
                    )}
                  </>
                }
                onCancel={onCollaboratorConfirmModalCancel}
                onAccept={onCollaboratorConfirmModalAccept}
              />
            </>
          )}
          {!isPublicRecipeChoosen && !isUserDefinedGithubRecipe && !DEPLOYMENT_DISABLED_RECIPE_IDS.includes(recipeValue) && (
            <div className={classNames('input-field-group include-error')}>
              <label htmlFor="userId" className="input-label">
                Find and add the collaborators you want to work with your code (Optional)
              </label>
              <div className={Styles.collaboratorSection}>
                <div className={Styles.collaboratorSectionList}>
                  <div className={Styles.collaboratorSectionListAdd}>
                    <AddUser
                      getCollabarators={getCollabarators}
                      dagId={''}
                      isRequired={false}
                      isUserprivilegeSearch={false}
                    />
                  </div>
                  <div className={Styles.collaboratorList}>
                    {codeSpaceCollaborators?.length > 0 ? (
                      <React.Fragment>
                        <div className={Styles.collaboratorTitle}>
                          <div className={Styles.collaboratorTitleCol}>User ID</div>
                          <div className={Styles.collaboratorTitleCol}>Name</div>
                          <div className={Styles.collaboratorTitleCol}>Permission</div>
                          <div className={Styles.collaboratorTitleCol}></div>
                        </div>
                        <div className={classNames('mbc-scroll', Styles.collaboratorContent)}>
                          {codeSpaceCollaborators?.map((item, collIndex) => {
                            return (
                              <div key={collIndex} className={Styles.collaboratorContentRow}>
                                <div className={Styles.collaboratorTitleCol}>{item.id}</div>
                                <div className={Styles.collaboratorTitleCol}>
                                  {item.firstName + ' ' + item.lastName}
                                </div>
                                <div className={Styles.collaboratorTitleCol}>
                                  <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                    <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="develop"
                                          checked={true}
                                          readOnly
                                        />
                                      </span>
                                      <span className="label">Develop</span>
                                    </label>
                                  </div>
                                  &nbsp;&nbsp;&nbsp;
                                  <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                    <label className={'checkbox'}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="deploy"
                                          checked={true}
                                          readOnly
                                          // checked={item?.permission !== null ? item?.canDeploy : false}
                                          onChange={(e) => onCollaboratorPermission(e, item.id)}
                                        />
                                      </span>
                                      <span className="label">Deploy</span>
                                    </label>
                                  </div>
                                </div>
                                <div className={Styles.collaboratorTitleCol}>
                                  <span
                                    tooltip-data={'Remove Collaborator'}
                                    className={Styles.deleteEntry}
                                    onClick={onCollaboratorDelete(item.id)}
                                  >
                                    <i className="icon mbc-icon trash-outline" />
                                  </span>
                                  {onEditingMode && (
                                    <>
                                      &nbsp;| &nbsp;
                                      <span
                                        tooltip-data={'Transfer Ownership'}
                                        className={Styles.deleteEntry}
                                        onClick={() => onTransferOwnership(item.id)}
                                      >
                                        <i className="icon mbc-icon comparison" />
                                      </span>
                                    </>
                                  )}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </React.Fragment>
                    ) : (
                      <div className={Styles.collaboratorSectionEmpty}>
                        <h6> Collaborators Not Exist!</h6>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}
          <div className={Styles.newCodeSpaceBtn}>
            {!onEditingMode && (
              <button className={' btn btn-tertiary '} onClick={createCodeSpace}>
                Create Code Space
              </button>
            )}
          </div>
        </div>
      )}
    </React.Fragment>
  );
};

export default NewCodeSpace;
