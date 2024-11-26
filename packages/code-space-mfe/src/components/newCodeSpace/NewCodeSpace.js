import React, { useState } from 'react';
import { useHistory } from 'react-router-dom';
import cn from 'classnames';
import Styles from './NewCodeSpace.scss';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import SelectBox from 'dna-container/SelectBox';

import { isValidGITRepoUrl, trackEvent } from '../../Utility/utils';
import TextBox from 'dna-container/TextBox';
// import { ICodeSpaceData } from '../CodeSpace';
import { useEffect } from 'react';
// import { ICodeCollaborator, IUserDetails, IUserInfo } from 'globals/types';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import { hostServer } from '../../server/api';
// import { ApiClient } from '../../../../services/ApiClient';
import AddUser from 'dna-container/AddUser';
import { Envs } from '../../Utility/envs';
// import { recipesMaster } from '../../Utility/utils';
import ConfirmModal from 'dna-container/ConfirmModal';
import { DEPLOYMENT_DISABLED_RECIPE_IDS } from '../../Utility/constants';
import Tags from 'dna-container/Tags';

const classNames = cn.bind(Styles);

// export interface ICodeSpaceProps {
//   user: IUserInfo;
//   onBoardingCodeSpace?: ICodeSpaceData;
//   onEditingCodeSpace?: ICodeSpaceData;
//   isRetryRequest?: boolean;
//   isCodeSpaceCreationSuccess?: (status: boolean, codeSpaceData: ICodeSpaceData) => void;
//   toggleProgressMessage?: (show: boolean) => void;
//   onUpdateCodeSpaceComplete?: () => void;
// }

// export interface ICodeSpaceRef {
//   validateAndCreateCodeSpace: () => void;
// }

// export interface ICreateCodeSpaceData {
//   recipeId: string;
//   password: string;
// }

const NewCodeSpace = (props) => {
  const history = useHistory();

  const onBoadingMode = props.onBoardingCodeSpace !== undefined;
  const onEditingMode = props.onEditingCodeSpace !== undefined;
  const projectDetails = props.onBoardingCodeSpace?.projectDetails || props.onEditingCodeSpace?.projectDetails;
  const [divisions, setDivisions] = useState([]);
  const [subDivisions, setSubDivisions] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dataClassificationDropdown, setDataClassificationDropdown] = useState([]);

  const [projectName, setProjectName] = useState('');
  const [projectNameError, setProjectNameError] = useState('');
  const [environment, setEnvironment] = useState('DHC-CaaS');
  const [recipeValue, setRecipeValue] = useState(projectDetails?.recipeDetails?.Id ? projectDetails?.recipeDetails?.Id : '0');
  // const recipes = recipesMaster;
  const [recipesMaster, setRecipeMaster] = useState([]);
  const [recipeSearchTerm, setRecipeSearchTerm] = useState('');
  const [filteredRecipe, setFilteredRecipe] = useState();
  const [selectedRecipe, setSelectedRecipe] = useState(null);


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


  const [livelinessInterval, setLivelinessInterval] = useState();

  // const [createdCodeSpaceName, setCreatedCodeSpaceName] = useState('');

  const [showConfirmModal, setShowConfirmModal] =  useState(false);
  const [collaboratorToDelete, setCollaboratorToDelete] =  useState();
  const [collaboratorToTransferOwnership, setCollaboratorToTransferOwnership] =  useState();


  const [typeOfProject, setTypeOfProject] = useState(projectDetails?.dataGovernance?.typeOfProject ? projectDetails?.dataGovernance?.typeOfProject : '0');
  const [typeOfProjectError, setTypeOfProjectError] = useState('');
  const isPlayground = typeOfProject === 'Playground';

  const [description, setDescription] = useState(projectDetails?.dataGovernance?.description ? projectDetails?.dataGovernance?.description : '');
  const [descriptionError, setDescriptionError] = useState('');

  const [division, setDivision] = useState(projectDetails?.dataGovernance?.division ? projectDetails?.dataGovernance?.divisionId+'@-@'+projectDetails?.dataGovernance?.division : '0');
  const [divisionError, setDivisionError] = useState('');

  const [subDivision, setSubDivision] = useState(projectDetails?.dataGovernance?.subDivision ? projectDetails?.dataGovernance?.subDivisionId+'@-@'+projectDetails?.dataGovernance?.subDivision : '0');
  
  const [department, setDepartment] = useState(onEditingMode && projectDetails?.dataGovernance?.department ? [projectDetails?.dataGovernance?.department] : []);
  const [departmentError, setDepartmentError] = useState(false);

  const [classificationType, setClassificationType] = useState(projectDetails?.dataGovernance?.classificationType? projectDetails?.dataGovernance?.classificationType : '0');
  const [classificationTypeError, setClassificationTypeError] = useState('');

  const [PII, setPII] = useState(projectDetails?.dataGovernance?.piiData ? true : false);

  const tags = [];

  const [archerId, setArcherId] = useState(projectDetails?.dataGovernance?.archerId ? projectDetails?.dataGovernance?.archerId : '');
  const [archerIdError, setArcherIdError] = useState('');

  const [procedureID, setProcedureID] = useState(projectDetails?.dataGovernance?.procedureID ? projectDetails?.dataGovernance?.procedureID : '');
  const [procedureIDError, setProcedureIDError] = useState('');
  const [showProgressIndicator, setShowProgressIndicator] = useState(false);


  const requiredError = '*Missing entry';
  const livelinessIntervalRef = React.useRef();
  // let livelinessInterval: any = undefined;

  useEffect(() => {
    if (!onBoadingMode) {
      ProgressIndicator.show();
      CodeSpaceApiClient.getLovData()
        .then((response) => {
          ProgressIndicator.hide();
          setDataClassificationDropdown(response[0]?.data.data || []);
          setDivisions(response[1]?.data || []);
          setDepartments(response[2]?.data.data || []);
          onEditingMode && setDivision(projectDetails?.dataGovernance?.division ? projectDetails?.dataGovernance?.divisionId + '@-@' + projectDetails?.dataGovernance?.division : '0');
          onEditingMode && setClassificationType(projectDetails?.dataGovernance?.classificationType ? projectDetails?.dataGovernance?.classificationType : '0');
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
    }
    ProgressIndicator.show();
    CodeSpaceApiClient.getRecipeLov()
      .then((res) => {
        setRecipeMaster(res.data.data);
        SelectBox.defaultSetup();
        ProgressIndicator.hide();
      })
      .catch(() => {
        ProgressIndicator.hide();
      });  
  }, []);// eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    setFilteredRecipe(recipesMaster);
  }, [recipesMaster])

  useEffect(() => {
    const divId = division.includes('@-@') ? division.split('@-@')[0] : division;
    if (divId && divId!=='0') {
      ProgressIndicator.show();
      hostServer.get('/subdivisions/' + divId)
        .then((res) => {
          setSubDivisions(res?.data || []);
          onEditingMode && setSubDivision(projectDetails?.dataGovernance?.subDivision ? projectDetails?.dataGovernance?.subDivisionId + '@-@' + projectDetails?.dataGovernance?.subDivision : '0');
          SelectBox.defaultSetup();
          ProgressIndicator.hide();
        }).catch(() => {
          ProgressIndicator.hide();
        });
    } else {
      setSubDivisions([]);
    }
  }, [division]);// eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, [typeOfProject]);

  useEffect(() => {
    if (onEditingMode && props.onEditingCodeSpace.projectDetails?.projectCollaborators) {
      setCodeSpaceCollaborators([...props.onEditingCodeSpace.projectDetails?.projectCollaborators || {}]);
    }
    SelectBox.defaultSetup(true);
  }, []);// eslint-disable-line react-hooks/exhaustive-deps

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
  }, [codeSpaceCollaborators]);// eslint-disable-line react-hooks/exhaustive-deps

  const sanitizedRepositoryName = (name) => {
    return name.replace(/[\s_-]/g, '-');
  };

  const onProjectNameOnChange = (evnt) => {
    const projectNameVal = sanitizedRepositoryName(evnt.currentTarget.value);
    setProjectName(projectNameVal);
    const hasSpecialChars = /[^A-Za-z0-9-]/.test(projectNameVal);
    const startsOrEndswith = /^-|-$|(--)|^\d+$/i.test(projectNameVal);
    if (hasSpecialChars) {
      setProjectNameError('Invalid name: Should not contain any special characters except for "-".');
    }
    else if (!projectNameVal.length) {
      setProjectNameError(requiredError);
    }
    else if (startsOrEndswith) {
      setProjectNameError('Invalid name: Should not start or end with "-" or name contains only numbers.');
    }
    else {
      setProjectNameError('');
    }
  };

  // const onGithubUserNameOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
  //   const githubUserNameVal = evnt.currentTarget.value.trim();
  //   setGithubUserName(githubUserNameVal);
  //   setGithubUserNameError(githubUserNameVal.length ? '' : requiredError);
  // };

  const onUserDefinedGithubUrlOnChange = (evnt) => {
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

  const onGithubTokenOnChange = (evnt) => {
    const githubTokenVal = evnt.currentTarget.value.trim();
    setGithubToken(githubTokenVal);
    setGithubTokenError(githubTokenVal.length ? '' : requiredError);
  };

  const onEnvironmentChange = (evnt) => {
    setEnvironment(evnt.currentTarget.value.trim());
  };

  const onTypeOfProjectChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setTypeOfProject(selectedOption);
  };

  const onDescriptionChange = (e) => {
    const currentValue = e.currentTarget.value;
    setDescription(currentValue);
    setDescriptionError(currentValue.length !== 0 ? '' : requiredError);
  };

  const onDivisionChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setDivision(selectedOption);
  }

  const onSubDivisionChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setSubDivision(selectedOption);
  }

  const onClassificationChange = (e) => {
    const selectedOption = e.currentTarget.value;
    setClassificationType(selectedOption);
  };

  const onPIIChange = (e) => {
    const currentValue = e.currentTarget.value;
    if (currentValue === 'true') {
      setPII(true);
    } else {
      setPII(false);
    }
  };

  const onArcherIdChange = (e) => {
    const currentValue = e.currentTarget.value;
    setArcherId(currentValue);
    const pattern = /^(INFO)-\d{5}$/.test(currentValue);
    setArcherIdError(currentValue.length && !pattern ? 'Archer ID should be of type INFO-XXXXX' : '');
  };

  const onProcedureIDChange = (e) => {
    const currentValue = e.currentTarget.value;
    setProcedureID(currentValue);
    const pattern = /^(PO|ITPLC)-\d{5}$/.test(currentValue);
    setProcedureIDError(currentValue.length && !pattern ? 'Procedure ID should be of type PO-XXXXX / ITPLC-XXXXX' : '');
  };

  const getRecipeDetails = (id) => {
    setShowProgressIndicator(true);
    CodeSpaceApiClient.getCodeSpaceRecipe(id)
    .then((res)=>{
      setShowProgressIndicator(false);
      setSelectedRecipe(res.data.data);
    }).catch(()=>{
      setShowProgressIndicator(false);
      Notification.show('Failed to fetch recipe details', 'error');
    })
  };

  const onRecipeChange = (obj) => {
    const selectedOption = obj.id;
    const recipe = recipesMaster.find((item) => item.id === recipeValue);
    setRecipeValue(selectedOption);
    getRecipeDetails(obj?.id);
    setRecipeError('');
    const isUserDefinedRecipe = recipe?.aliasId === 'public-user-defined' || recipe?.aliasId === 'private-user-defined';
    setIsUserDefinedGithubRecipe(isUserDefinedRecipe);
    if (!isUserDefinedRecipe) {
      setUserDefinedGithubUrl('');
      setUserDefinedGithubUrlError('');
    }
  };

  // User Name
  const namePrefix = props.user.firstName;

  const getCollabarators = (collaborator) => {
    const collabarationData = {
      firstName: collaborator.firstName,
      lastName: collaborator.lastName,
      id: collaborator.shortId,
      department: collaborator.department,
      email: collaborator.email,
      mobileNumber: collaborator.mobileNumber,
      gitUserName: collaborator.shortId,
      isAdmin: collaborator.isAdmin,
      // permission: { read: true, write: false },
    };

    let duplicateMember = false;
    duplicateMember = codeSpaceCollaborators.filter((member) => member.id === collaborator.shortId)?.length
      ? true
      : false;
    const isCreator = props.user.id === collaborator.shortId || (onEditingMode && props?.onEditingCodeSpace?.projectDetails?.projectOwner?.id === collaborator?.shortId);

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

  const onCollaboratorPermission = (e, userId) => {
    const codeSpaceCollaborator = codeSpaceCollaborators.find((item) => {
      return item.id == userId;
    });

    if (e.target.checked) {
      codeSpaceCollaborator.isAdmin = true;
      if (onEditingMode) {
        CodeSpaceApiClient.assignAdminRole(props.onEditingCodeSpace.id, userId, true);
      }
    } else {
      codeSpaceCollaborator.isAdmin = false;
      if (onEditingMode) {
        CodeSpaceApiClient.assignAdminRole(props.onEditingCodeSpace.id, userId, false);
      }
    }
    setCodeSpaceCollaborators([...codeSpaceCollaborators]);
  };

  const addNewCollaborator = () => {
    if (props.onEditingCodeSpace.projectDetails?.projectCollaborators === null) {
      props.onEditingCodeSpace.projectDetails.projectCollaborators = [];
    }
    const existingColloborators = props.onEditingCodeSpace.projectDetails?.projectCollaborators || [];
    const newCollaborator = codeSpaceCollaborators.find((collab) => !existingColloborators.some((existCollab) => existCollab.id === collab.id));
    if (newCollaborator) {
      ProgressIndicator.show();
      CodeSpaceApiClient.addCollaborator(props.onEditingCodeSpace.id, newCollaborator).then((res) => {
        ProgressIndicator.hide();
        if (res.data.success === 'SUCCESS') {
          trackEvent('DnA Code Space', 'Add New Collaborator', 'Existing Code Space');
          props.onEditingCodeSpace.projectDetails?.projectCollaborators?.push(newCollaborator);
          Notification.show(
            `Collaborator '${newCollaborator.firstName}' has been added successfully to the Code Space.`,
          );
        } else {
          setCodeSpaceCollaborators([...existingColloborators]);
          Notification.show(
            `Error adding collaborator '${newCollaborator.firstName}' to the Code Space. Please try again later.\n ${res.data.errors[0].message}`,
            'alert',
          );
        }
      })
      .catch((err) => {
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

  const onCollaboratorDelete = (userId) => {
    Tooltip.clear();
    return () => {
      if (onEditingMode) {
        setCollaboratorToDelete(codeSpaceCollaborators.find((collab) => collab.id === userId));
        setShowConfirmModal(true);
      } else {
        updateCollaborator(userId);
      }
    };
  };

  const updateCollaborator = (userId) => {
    const currentCollList = codeSpaceCollaborators.filter((item) => {
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
      if (res.data.success === 'SUCCESS') {
        trackEvent('DnA Code Space', 'Delete Collaborator', 'Existing Code Space');
        props.onEditingCodeSpace.projectDetails.projectCollaborators = [...updateCollaborator(collaboratorToDelete.id)];
        Notification.show(
          `Collaborator '${collaboratorToDelete.firstName}' has been removed successfully from the Code Space.`,
        );
      } else {
        Notification.show(
          `Error removing collaborator '${collaboratorToDelete.firstName}' from the Code Space. Please try again later.\n ${res.errors[0].message}`,
          'alert',
        );
      }
    })
    .catch((err) => {
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
        if (res.data.success === 'SUCCESS') {
          trackEvent('DnA Code Space', 'Transfer Ownership', 'Existing Code Space');
          Notification.show(
            `Code Space '${props.onEditingCodeSpace?.projectDetails?.projectName}' ownership successfully transferred to collaborator '${collaboratorToTransferOwnership.firstName}'.`,
          );
          props.onUpdateCodeSpaceComplete();
        } else {
          Notification.show(
            `Error transferring Code Space ownership to collaborator '${collaboratorToTransferOwnership.firstName}'\n ${res?.data?.errors[0]?.message} Please try again later.`,
            'alert',
          );
        }
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show(
          'Error in transferring Code Space ownership. Please try again later.\n' + err.message,
          'alert',
        );
      });
    setCollaboratorToTransferOwnership(undefined);
  };

  const onTransferOwnership = (userId) => {
    setCollaboratorToTransferOwnership(codeSpaceCollaborators.find((collab) => collab.id === userId));
    setShowConfirmModal(true);
  };

  const validateEditCodeSpaceForm = () => {
    let formValid = true;
    if (typeOfProject === '0') {
      setTypeOfProjectError(requiredError);
      formValid = false;
    }
    if (!description.length) {
      setDescriptionError(requiredError);
      formValid = false;
    }
    if (!isPlayground && division === '0') {
      setDivisionError(requiredError);
      formValid = false;
    }
    if (!department.length) {
      setDepartmentError(true);
      formValid = false;
    }
    if (!isPlayground && classificationType === '0') {
      setClassificationTypeError(requiredError);
      formValid = false;
    }
    return formValid;
  };

  const validateNewCodeSpaceForm = (isPublicRecipeChoosen) => {
    let formValid = true;
    if (!projectName.length) {
      setProjectNameError(requiredError);
      formValid = false;
    }
    if (recipeValue === '0') {
      setRecipeError(requiredError);
      formValid = false;
    }
    if (typeOfProject === '0') {
      setTypeOfProjectError(requiredError);
      formValid = false;
    }
    if (!description.length) {
      setDescriptionError(requiredError);
      formValid = false;
    }
    if (!isPlayground && division === '0') {
      setDivisionError(requiredError);
      formValid = false;
    }
    if (!department.length) {
      setDepartmentError(true);
      formValid = false;
    }
    if (!isPlayground && classificationType === '0') {
      setClassificationTypeError(requiredError);
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
    if (githubToken === '' && recipe?.aliasId !== 'default') {
      setGithubTokenError(requiredError);
      formValid = false;
    }
    if (projectNameError !== '' || recipeValue === '0' || githubTokenError !== '' || (isPublicRecipeChoosen && isUserDefinedGithubRecipe && userDefinedGithubUrlError !== '')) {
      formValid = false;
    }
    return formValid;
  };

  const validateOnBoardCodeSpaceForm = () => {
    const recipe = recipesMaster.find((item) => item.id === recipeValue);
    let formValid = true;
    // if (isPublicRecipeChoosen && githubUserName === '') {
    //   setGithubUserNameError(requiredError);
    //   formValid = false;
    // } else {
    //   setGithubUserNameError('');
    // }
    if (githubToken === '' && recipe?.aliasId !== 'default') {
      setGithubTokenError(requiredError);
      formValid = false;
    }
    if (githubTokenError !== '') {
      formValid = false;
    }
    return formValid;
  };

  const enableLivelinessCheck = (id) => {
    clearInterval(livelinessInterval);
    const intervalId = window.setInterval(() => {
      CodeSpaceApiClient.workSpaceStatus()
        .then((response) => {
          try {
            if (response.data.status.includes(id)) {
              CodeSpaceApiClient.getCodeSpaceStatus(id)
                .then((res) => {
                  props.toggleProgressMessage(false);
                  ProgressIndicator.hide();
                  clearInterval(livelinessInterval);
                  props.isCodeSpaceCreationSuccess(true, {
                    ...res.data,
                    running: true,
                  });
                  Notification.show('Code space succesfully created and started.');
                })
                .catch((err) => {
                  clearInterval(livelinessInterval);
                  props.toggleProgressMessage(false);
                  ProgressIndicator.hide();
                  Notification.show(
                    'Error getting codespace status. Please refresh and try again - ' + err.message,
                    'alert',
                  );
                });
            }
          } catch (err) {
            console.log(err);
            clearInterval(livelinessInterval);
            props.toggleProgressMessage(false);
            ProgressIndicator.hide();
            Notification.show(
              'Error getting codespace status. Please refresh and try again.',
              'alert',
            );
          }
        })
        .catch((err) => {
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
  //             url: `https://xxxx/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
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

  const editCodeSpace = () => {
    if (validateEditCodeSpaceForm()){
      const editCodeSpaceRequest = {
        data: {
          typeOfProject: typeOfProject,
          description: description,
          divisionId: division.split('@-@')[0],
          division: division.split('@-@')[1],
          subDivisionId: subDivision.split('@-@')[0],
          subDivision: subDivision.split('@-@')[1],
          department: department[0],
          classificationType: classificationType,
          piiData: PII,
          tags: tags,
          archerId: archerId,
          procedureID: procedureID,
        },
      };

      ProgressIndicator.show();
      CodeSpaceApiClient.editCodeSpace(props.onEditingCodeSpace.id, editCodeSpaceRequest)
      .then(() => {
        Notification.show('Code space updated successfully');
        ProgressIndicator.hide();
        props.onUpdateCodeSpaceComplete();
    
        })
        .catch((err) => {
          ProgressIndicator.hide();

          Notification.show('Error in updating code space. Please try again later.\n' + err.message, 'alert');

        });
    }

  };

  const createCodeSpace = () => {
    const recipe = recipesMaster.find((item) => item.id === recipeValue);
    const isPublicRecipeChoosen = recipe?.aliasId && recipe?.aliasId?.startsWith('public');

    if (validateNewCodeSpaceForm(isPublicRecipeChoosen)) {
      const createCodeSpaceRequest = {
        data: {
          gitUserName: props.user.id,
          projectDetails: {
            projectCollaborators: codeSpaceCollaborators,
            projectName: projectName,
            recipeName: recipeValue,
            // gitRepoName: null,
            dataGovernance: {
              typeOfProject: typeOfProject,
              description: description,
              divisionId: division.split('@-@')[0],
              division: division.split('@-@')[1],
              subDivisionId: subDivision.split('@-@')[0],
              subDivision: subDivision.split('@-@')[1],
              department: department[0],
              classificationType: classificationType,
              piiData: PII,
              archerId: archerId,
              procedureID: procedureID,
            },
          },
        },
        pat: githubToken
      };

      if (isUserDefinedGithubRecipe) {
        // createCodeSpaceRequest.data.gitUserName = githubUserName;
        // createCodeSpaceRequest.data.projectDetails.recipeDetails.recipeId = 'public';
        createCodeSpaceRequest.data.projectDetails['gitRepoName'] = userDefinedGithubUrl.split('://')[1] + ',';
      }

      ProgressIndicator.show();
      CodeSpaceApiClient.createCodeSpace(createCodeSpaceRequest)
        .then((res) => {
          trackEvent('DnA Code Space', 'Create', 'New code space');
          if (res.data.data.status === 'CREATED' || res.data.data.status === 'CREATE_REQUESTED') {
            // setCreatedCodeSpaceName(res.data.name);
            props.toggleProgressMessage(true);
            enableLivelinessCheck(res.data.data.workspaceId);
          } else {
            props.toggleProgressMessage(false);
            ProgressIndicator.hide();
            Notification.show(
              'Error in creating new code space. Please try again later.\n' + res.data.errors[0].message,
              'alert',
            );
          }
        })
        .catch((err) => {
          props.toggleProgressMessage(false);
          ProgressIndicator.hide();
          if (err.response.status === 409) {
            Notification.show(
              `Given Code Space Name '${projectName}' already in use. Please use another name.`,
              'alert',
            );
          } else {
            Notification.show('Error in creating new code space. Please try again later.\n' + err.response.data.errors[0].message, 'alert');
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
    // const isPublicRecipeChoosen = recipeValue.startsWith('public');
    const recipe = recipesMaster.find((item) => item.id === recipeValue);
    const isPublicRecipeChoosen = recipe?.aliasId && recipe?.aliasId?.startsWith('public');

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
          if (res.data?.data?.status === 'CREATED' || res.data?.data?.status === 'CREATE_REQUESTED') {
            // setCreatedCodeSpaceName(res.data.name);
            props.toggleProgressMessage(true);
            enableLivelinessCheck(res.data.data.workspaceId);
          } else {
            props.toggleProgressMessage(false);
            ProgressIndicator.hide();
            Notification.show(
              'Error in creating new code space. Please try again later.\n' + res.data.errors[0].message,
              'alert',
            );
          }
        })
        .catch((err) => {
          props.toggleProgressMessage(false);
          ProgressIndicator.hide();
          if (err.response.status === 409) {
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

  const recipe = recipesMaster.find((item) => item.id === recipeValue);

  const isPublicRecipeChoosen = recipe?.aliasId && recipe?.aliasId?.startsWith('public');
  const githubUrlValue = isPublicRecipeChoosen ? 'https://github.com/' : Envs.CODE_SPACE_GIT_PAT_APP_URL;
  const resources = projectDetails?.recipeDetails?.resource?.split(',');
  return (
    <React.Fragment>
      {onBoadingMode ? (
        <div className={Styles.newCodeSpacePanel}>
          <div className={Styles.addicon}> &nbsp; </div>
          <h3>
            Hello {namePrefix}, {props.isRetryRequest ? 'Retry creation of' : 'On-board to'} Code Space
          </h3>
          <div className={Styles.codeSpaceDetails}>
            <div className={Styles.flexLayout}>
              <div>
                <label>Name</label>
              </div>
              <div>{projectDetails.projectName}</div>
              <div>
                <label>Type of Project</label>
              </div>
              <div>{projectDetails?.dataGovernance?.typeOfProject ? projectDetails?.dataGovernance?.typeOfProject : 'N/A'}</div>
            </div>
            <div className={Styles.flexLayout}>
              <div style={{ width: '25%' }}>
                <label>Description</label>
              </div>
              <div style={{ width: '75%' }}>{projectDetails?.dataGovernance?.description ? projectDetails?.dataGovernance?.description : 'N/A'}</div>
            </div>
            {projectDetails?.dataGovernance?.typeOfProject !== 'Playground' && <div className={Styles.flexLayout}>
              <div>
                <label>Division</label>
              </div>
              <div>{projectDetails?.dataGovernance?.division ? projectDetails?.dataGovernance?.division : 'N/A'}</div>
              <div>
                <label>Sub Division</label>
              </div>
              <div>{projectDetails?.dataGovernance?.subDivision ? projectDetails?.dataGovernance?.subDivision : 'N/A'}</div>
            </div>}
            <div className={Styles.flexLayout}>
              <div>
                <label>Department</label>
              </div>
              <div>{projectDetails?.dataGovernance?.department ? projectDetails?.dataGovernance?.department : 'N/A'}</div>
              <div></div>
              <div></div>
            </div>
            {projectDetails?.dataGovernance?.typeOfProject !== 'Playground' && <div className={Styles.flexLayout}>
              <div>
                <label>Data Classification</label>
              </div>
              <div>{projectDetails?.dataGovernance?.classificationType ? projectDetails?.dataGovernance?.classificationType : 'N/A'}</div>
              <div>
                <label>PII</label>
              </div>
              <div>{projectDetails?.dataGovernance?.piiData ? 'Yes' : 'No'}</div>
            </div>}
            <div className={Styles.flexLayout}>
              <div>
                <label>Archer ID</label>
              </div>
              <div>{projectDetails?.dataGovernance?.archerId ? projectDetails?.dataGovernance?.archerId : 'N/A'}</div>
              <div>
                <label>Procedure ID</label>
              </div>
              <div>{projectDetails?.dataGovernance?.procedureID ? projectDetails?.dataGovernance?.procedureID : 'N/A'}</div>
            </div>
            <div className={Styles.flexLayout}>
              <div style={{ width: '25%' }}>
                <label>Recipe</label>
              </div>
              <div style={{ width: '75%' }}>
                {projectDetails?.recipeDetails?.recipeName ? projectDetails?.recipeDetails?.recipeName + '( ' + projectDetails?.recipeDetails?.operatingSystem + ', ' + (resources[3]?.split('M')[0]) / 1000 + 'GB RAM, ' + resources[4] + 'CPU)' : 'N/A'}
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <label>Environment</label>
              </div>
              <div>{projectDetails.recipeDetails.cloudServiceProvider || environment}</div>
              <div></div>
              <div></div>
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
          {recipe?.aliasId !== 'default' && <>
            <p>Enter the information to start creating!</p>
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
          </>}
          <div className={Styles.newCodeSpaceBtn}>
            <button className={' btn btn-tertiary '} onClick={onBoardToCodeSpace}>
              {props.isRetryRequest ? 'Create' : 'On-board to'} Code Space
            </button>
          </div>
        </div>
      ) : (
        <div className={Styles.newCodeSpacePanel}>
          {!onEditingMode ? (
            <>
              <div className={Styles.addicon}> &nbsp; </div>
              <h3>Hello {namePrefix}, Create your Code Space</h3>
              <p>Enter the information to start creating!</p>
              {/* <p className={Styles.passwordInfo}>Note: Password should be minimum 8 chars in length and alpha numeric.</p> */}
              <div className={Styles.flexLayout}>
                <div
                  className={classNames('input-field-group include-error', typeOfProjectError?.length ? 'error' : '')}
                >
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Type of Project <sup>*</sup>
                  </label>

                  <div className={classNames('custom-select')}>
                    <select
                      id="reportStatusField"
                      defaultValue={typeOfProject}
                      required={true}
                      required-error={requiredError}
                      onChange={onTypeOfProjectChange}
                      value={typeOfProject}
                    >
                      <option id="typeOfProjectOption" value={0}>
                        Choose
                      </option>
                      <option value={'Playground'}>Playground</option>
                      <option value={'Proof of Concept'}>Proof of Concept</option>
                      <option value={'Production'}>Production</option>
                    </select>
                  </div>
                  <p
                    style={{ color: 'var(--color-orange)' , marginLeft:'-143px'}}
                    className={classNames(typeOfProject !== 'Playground' ? ' hide' : '')}
                  >
                    <i className="icon mbc-icon alert circle"></i> Playground projects are deleted after 2 months of not
                    being used.
                  </p>
                  <span className={classNames('error-message', typeOfProjectError.length ? '' : 'hide')}>
                    {typeOfProjectError}
                  </span>
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
              </div>
              <div className={Styles.flexLayout}>
                <div
                  className={classNames('input-field-group include-error area', descriptionError.length ? 'error' : '')}
                >
                  <label id="description" className="input-label" htmlFor="description">
                    Description <sup>*</sup>
                  </label>
                  <textarea
                    id="description"
                    className="input-field-area"
                    // type="text"
                    defaultValue={description}
                    required={true}
                    required-error={requiredError}
                    onChange={onDescriptionChange}
                    rows={50}
                  />
                  <span className={classNames('error-message', descriptionError.length ? '' : 'hide')}>
                    {descriptionError}
                  </span>
                </div>
                <div>

                  {!isPlayground && <div>
                    <div className={Styles.flexLayout}>
                      <div
                        className={classNames('input-field-group include-error',
                          divisionError.length ? 'error' : '',
                        )}
                      >
                        <label className={classNames(Styles.inputLabel, 'input-label')}>
                          Division <sup>*</sup>
                        </label>
                        <div className={classNames('custom-select')}>
                          <select
                            id="divisionField"
                            defaultValue={division}
                            required={true}
                            required-error={requiredError}
                            onChange={onDivisionChange}
                            value={division}
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
                        <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                          {divisionError}
                        </span>
                      </div>

                      <div className={classNames('input-field-group include-error')}>
                        <label className={classNames(Styles.inputLabel, 'input-label')}>Sub Division</label>
                        <div className={classNames('custom-select')}>
                          <select
                            id="subDivisionField"
                            defaultValue={subDivision}
                            value={subDivision}
                            required={false}
                            onChange={onSubDivisionChange}
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
                  </div>}
                  <div className={Styles.flexLayout}>
                    <div className={classNames('input-field-group include-error', archerIdError.length ? 'error' : '')}>
                      <label className={classNames(Styles.inputLabel, 'input-label')}>Archer ID</label>
                      <div>
                        <input
                          type="text"
                          className={classNames('input-field', Styles.projectNameField)}
                          id="archerId"
                          placeholder="Type here eg.[INFO-XXXXX]"
                          autoComplete="off"
                          maxLength={55}
                          defaultValue={archerId}
                          onChange={onArcherIdChange}
                        />
                        <span className={classNames('error-message', archerIdError.length ? '' : 'hide')}>
                          {archerIdError}
                        </span>
                      </div>
                    </div>
                    <div className={classNames('input-field-group include-error', procedureIDError.length ? 'error' : '')}>
                      <label className={classNames(Styles.inputLabel, 'input-label')}>Procedure ID</label>
                      <div>
                        <input
                          type="text"
                          className={classNames('input-field', Styles.projectNameField)}
                          id="procedureID"
                          placeholder="Type here eg.[PO-XXXXX / ITPLC-XXXXX]"
                          autoComplete="off"
                          maxLength={55}
                          defaultValue={procedureID}
                          onChange={onProcedureIDChange}
                        />
                        <span className={classNames('error-message', procedureIDError.length ? '' : 'hide')}>
                          {procedureIDError}
                        </span>
                      </div>
                    </div>
                  </div>
                  {isPlayground && <div
                    className={classNames(
                      Styles.bucketNameInputField,
                      'input-field-group include-error',
                      departmentError ? 'error' : '',
                    )}
                  >
                    <div>
                      <div className={Styles.departmentTags}>
                        <Tags
                          title={'Department'}
                          max={1}
                          chips={department}
                          tags={departments}
                          setTags={(selectedTags) => {
                            const dept = selectedTags?.map((item) => item.toUpperCase());
                            setDepartment(dept);
                            setDepartmentError(false);
                          }}
                          isMandatory={true}
                          showMissingEntryError={departmentError}

                        />

                      </div>
                    </div>
                  </div>}

                </div>
              </div>



              {!isPlayground && <div className={Styles.flexLayout}>
                <div
                  className={classNames(
                    Styles.bucketNameInputField,
                    'input-field-group include-error',
                    departmentError ? 'error' : '',
                  )}
                >
                  <div>
                    <div className={Styles.departmentTags}>
                      <Tags
                        title={'Department'}
                        max={1}
                        chips={department}
                        tags={departments}
                        setTags={(selectedTags) => {
                          const dept = selectedTags?.map((item) => item.toUpperCase());
                          setDepartment(dept);
                          setDepartmentError(false);
                        }}
                        isMandatory={true}
                        showMissingEntryError={departmentError}

                      />

                    </div>
                  </div>
                </div>
                <div>
                  <div className={Styles.flexLayout}>
                    <div
                      className={classNames(
                        'input-field-group include-error',
                        classificationTypeError?.length ? 'error' : '',
                      )}
                    >
                      <label className={classNames(Styles.inputLabel, 'input-label')}>
                        Data Classification <sup>*</sup>
                      </label>
                      <div className={classNames('custom-select')}>
                        <select
                          id="classificationField"
                          defaultValue={classificationType}
                          required={true}
                          required-error={requiredError}
                          onChange={onClassificationChange}
                          value={classificationType}
                        >
                          <option id="classificationOption" value={0}>
                            Choose
                          </option>
                          {dataClassificationDropdown?.map((item) => (
                            <option id={item.id} key={item.id} value={item.name}>
                              {item.name}
                            </option>
                          ))}
                        </select>
                      </div>
                      <span className={classNames('error-message', classificationTypeError.length ? '' : 'hide')}>
                        {classificationTypeError}
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
                              value="true"
                              name="pii"
                              defaultChecked={PII === true}
                              onChange={onPIIChange}
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
                              name="pii"
                              defaultChecked={PII === false}
                              onChange={onPIIChange}
                            />
                          </span>
                          <span className="label">No</span>
                        </label>
                      </div>
                    </div>
                  </div>

                </div>

              </div>}

              <div
                id="recipeContainer"
                className={classNames('input-field-group include-error', recipeError.length ? 'error' : '', Styles.recipeContainer)}
              >
                  <div className={classNames(Styles.recipeLable)}>
                    <label id="recipeLabel" className="input-label" htmlFor="recipeSelect">
                      Select Code Recipe<sup>*</sup>
                    </label>
                    <button className={classNames(Styles.addNewItemButton)} onClick={() => history.push('/codespaceRecipes/codespace')}>                      <i className="icon mbc-icon plus" />
                      &nbsp;
                      <span>Add new recipe</span>
                    </button>
                  </div>
                <div className={classNames(Styles.recipeWrapper, recipeError.length ? Styles.recipeError : '')}>
                  <div className={classNames(Styles.leftPane)}>
                    <div className={classNames(Styles.recipeHeaderSec)}>
                      <div className={classNames(Styles.recipeSearch)}>
                        <input
                          type="text"
                          className={classNames(Styles.searchInputField)}
                          placeholder="Search Recipe"
                          maxLength={100}
                          value={recipeSearchTerm}
                          onChange={(e) => {
                            const value = e.target.value;
                            setRecipeSearchTerm(value);
                            const filteredRecipes = recipesMaster.filter((val) => val.recipeName.toLowerCase().includes(value.toLowerCase()));
                            setFilteredRecipe(filteredRecipes)
                          }}
                          />
                          <i
                            className={classNames('icon mbc-icon', recipeSearchTerm?.length ? 'close circle' : 'search', Styles.searchIcon)}
                            onClick={() => {
                              if (recipeSearchTerm?.length) {
                                setRecipeSearchTerm("");
                                setFilteredRecipe(recipesMaster);
                              }
                            }}
                          />
                        </div>
                    </div>
                    <div className={classNames(Styles.recipeSection)}>
                      <div className={classNames(Styles.recipeTiles)}>
                        {filteredRecipe?.map((obj) => (
                          <div key={obj?.id} className={classNames(Styles.recipeTile, recipeValue === obj?.id ? Styles.selected : '')} onClick={() => onRecipeChange(obj)}>
                            <i className={classNames("icon mbc-icon ", recipeValue === obj?.id ? "check-mark" : "tools-mini", recipeValue === obj?.id ? Styles.iconSelected : '')}></i>
                            <div className={classNames(Styles.recipeName)}><h5>{obj?.recipeName?.length > 60 ? obj?.recipeName.slice(0,57)+'...' : obj?.recipeName }</h5></div>
                          </div>
                        ))}
                      </div>
                    </div>

                  </div>
                    <div className={classNames(Styles.rightPane)}>
                      {showProgressIndicator ?
                        <div className={classNames('text-center', Styles.progressIndicator)}>
                          <div className="progress infinite" />
                        </div>
                        : selectedRecipe === null ? <div className={classNames(Styles.noRecipeSelected)}><p>No Recipe Selected </p></div> :
                          <div className={classNames(Styles.selectedRecipeWrapper)}>
                            <div className={classNames(Styles.selectedRecipeCard)}>
                              <div className={classNames(Styles.cardHeader)}>
                                <div className={classNames(Styles.cardTitle)}>
                                  <i className={classNames("icon mbc-icon tools-mini")}></i>
                                  <p>
                                    {selectedRecipe?.recipeName?.length > 60
                                      ? selectedRecipe.recipeName.slice(0, 57) + "..."
                                      : selectedRecipe.recipeName}
                                  </p>
                                </div>
                                {!selectedRecipe?.isPublic && <div className={classNames(Styles.privateRecipeTag)}>Private</div>}
                              </div>
                              <hr />
                              <div className={classNames(Styles.cardBodySection)}>
                                <div>
                                  <div>
                                    <div>Hardware Config</div>
                                    <div> {selectedRecipe?.oSName + ' , ' + selectedRecipe?.maxCpu + 'CPU , ' + (selectedRecipe?.maxRam / 1000) + 'GB RAM'}</div>
                                  </div>
                                  <div>
                                    <div>Software Config</div>
                                    <div>
                                      {selectedRecipe?.software?.map((val) => {
                                        return (
                                          <label key={val} className={classNames('chips', Styles.chips)}>
                                            {val}
                                          </label>
                                        );
                                      })}
                                    </div>
                                  </div>
                                  {selectedRecipe?.additionalServices[0]?.length > 0 && <div>
                                    <div>Additional Services</div>
                                    <div>
                                      {selectedRecipe?.additionalServices?.map((val) => {
                                        return (
                                          <label key={val} className={classNames('chips', Styles.chips)}>
                                            {val}
                                          </label>
                                        );
                                      })}
                                    </div>
                                  </div>}
                                  <div>
                                    <div>Created By</div>
                                    <div>{selectedRecipe?.createdBy?.firstName + ' ' + selectedRecipe?.createdBy?.lastName}</div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                      }
                    </div>

                </div>
                <span className={classNames('error-message', recipeError.length ? '' : 'hide')}>{recipeError}</span>
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
              {recipe?.aliasId !== 'default' && (
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
              )}
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
                  <div>{projectDetails?.recipeDetails?.recipeName ? projectDetails?.recipeDetails?.recipeName + '( ' + projectDetails?.recipeDetails?.operatingSystem + ', ' + (resources[3]?.split('M')[0]) / 1000 + 'GB RAM, ' + resources[4] + 'CPU)' : 'N/A'}</div>                </div>
                <div className={Styles.flexLayout}>
                  <div>
                    <label>Environment</label>
                  </div>
                  <div>{projectDetails.recipeDetails.cloudServiceProvider}</div>
                </div>

                <div className={Styles.flexLayout}>
                  <div
                    className={classNames('input-field-group include-error', typeOfProjectError?.length ? 'error' : '')}
                  >
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Type of Project <sup>*</sup>
                    </label>

                    <div className={classNames('custom-select')}>
                      <select
                        id="reportStatusField"
                        defaultValue={typeOfProject}
                        required={true}
                        required-error={requiredError}
                        onChange={onTypeOfProjectChange}
                        value={typeOfProject}
                      >
                        <option id="typeOfProjectOption" value={0}>
                          Choose
                        </option>
                        {(projectDetails?.dataGovernance?.typeOfProject === 'Playground' || !projectDetails?.dataGovernance?.typeOfProject) && <option value={'Playground'}>Playground</option>}
                        <option value={'Proof of Concept'}>Proof of Concept</option>
                        <option value={'Production'}>Production</option>
                      </select>
                    </div>
                    <p
                      style={{ color: 'var(--color-orange)' , marginLeft:'-143px'}}
                      className={classNames(typeOfProject !== 'Playground' ? ' hide' : '')}
                    >
                      <i className="icon mbc-icon alert circle"></i> Playground projects are deleted after 2 months of not
                      being used.
                    </p>
                    <span className={classNames('error-message', typeOfProjectError.length ? '' : 'hide')}>
                      {typeOfProjectError}
                    </span>
                  </div>
                  <div
                    className={classNames(
                      Styles.bucketNameInputField,
                      'input-field-group include-error',
                      departmentError ? 'error' : '',
                    )}
                  >
                    <div>
                      <div className={Styles.departmentTags}>
                        <Tags
                          title={'Department'}
                          max={1}
                          chips={department}
                          tags={departments}
                          setTags={(selectedTags) => {
                            const dept = selectedTags?.map((item) => item.toUpperCase());
                            setDepartment(dept);
                            setDepartmentError(false);
                          }}
                          isMandatory={true}
                          showMissingEntryError={departmentError}

                        />

                      </div>
                    </div>
                  </div>
                </div>

                <div className={Styles.flexLayout}>
                  <div
                    className={classNames('input-field-group include-error area', descriptionError.length ? 'error' : '')}
                  >
                    <label id="description" className="input-label" htmlFor="description">
                      Description1 <sup>*</sup>
                    </label>
                    <textarea
                      id="description"
                      className="input-field-area"
                      defaultValue={description}
                      value={description}
                      required={true}
                      required-error={requiredError}
                      onChange={onDescriptionChange}
                      rows={50}
                    />
                    <span className={classNames('error-message', descriptionError.length ? '' : 'hide')}>
                      {descriptionError}
                    </span>
                  </div>
                  <div>
                    <div className={!isPlayground ? Styles.flexLayout : ''}>
                      <div className={classNames('input-field-group include-error', archerIdError.length ? 'error' : '')}>
                        <label className={classNames(Styles.inputLabel, 'input-label')}>Archer ID</label>
                        <div>
                          <input
                            type="text"
                            className={classNames('input-field', Styles.projectNameField)}
                            id="archerId"
                            placeholder="Type here eg.[INFO-XXXXX]"
                            autoComplete="off"
                            maxLength={55}
                            defaultValue={archerId}
                            onChange={onArcherIdChange}
                          />
                          <span className={classNames('error-message', archerIdError.length ? '' : 'hide')}>
                            {archerIdError}
                          </span>
                        </div>
                      </div>
                      <div className={classNames('input-field-group include-error', procedureIDError.length ? 'error' : '')}>
                        <label className={classNames(Styles.inputLabel, 'input-label')}>Procedure ID</label>
                        <div>
                          <input
                            type="text"
                            className={classNames('input-field', Styles.projectNameField)}
                            id="procedureID"
                            placeholder="Type here eg.[PO-XXXXX / ITPLC-XXXXX]"
                            autoComplete="off"
                            maxLength={55}
                            defaultValue={procedureID}
                            onChange={onProcedureIDChange}
                          />
                          <span className={classNames('error-message', procedureIDError.length ? '' : 'hide')}>
                            {procedureIDError}
                          </span>
                        </div>
                      </div>
                    </div>
                    {!isPlayground && <div>
                      <div className={Styles.flexLayout}>
                        <div
                          className={classNames('input-field-group include-error',
                            divisionError.length ? 'error' : '',
                          )}
                        >
                          <label className={classNames(Styles.inputLabel, 'input-label')}>
                            Division <sup>*</sup>
                          </label>
                          <div className={classNames('custom-select')}>
                            <select
                              id="divisionField"
                              defaultValue={division}
                              required={true}
                              required-error={requiredError}
                              onChange={onDivisionChange}
                              value={division}
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
                          <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                            {divisionError}
                          </span>
                        </div>
                        <div className={classNames('input-field-group include-error')}>
                          <label className={classNames(Styles.inputLabel, 'input-label')}>Sub Division</label>
                          <div className={classNames('custom-select')}>
                            <select
                              id="subDivisionField"
                              defaultValue={subDivision}
                              value={subDivision}
                              required={false}
                              onChange={onSubDivisionChange}
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
                    </div>}
                  </div>

                </div>


                {!isPlayground && <div>
                  <div className={Styles.flexLayout}>
                    <div
                      className={classNames(
                        'input-field-group include-error',
                        classificationTypeError?.length ? 'error' : '',
                      )}
                    >
                      <label className={classNames(Styles.inputLabel, 'input-label')}>
                        Data Classification <sup>*</sup>
                      </label>
                      <div className={classNames('custom-select')}>
                        <select
                          id="classificationField"
                          defaultValue={classificationType}
                          required={true}
                          required-error={requiredError}
                          onChange={onClassificationChange}
                          value={classificationType}
                        >
                          <option id="classificationOption" value={0}>
                            Choose
                          </option>
                          {dataClassificationDropdown?.map((item) => (
                            <option id={item.id} key={item.id} value={item.name}>
                              {item.name}
                            </option>
                          ))}
                        </select>
                      </div>
                      <span className={classNames('error-message', classificationTypeError.length ? '' : 'hide')}>
                        {classificationTypeError}
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
                              value="true"
                              name="pii"
                              defaultChecked={PII === true}
                              onChange={onPIIChange}
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
                              name="pii"
                              defaultChecked={PII === false}
                              onChange={onPIIChange}
                            />
                          </span>
                          <span className="label">No</span>
                        </label>
                      </div>
                    </div>
                  </div>
                </div>}


                <div className={Styles.newCodeSpaceBtn}>
                  <button className={' btn btn-tertiary '} onClick={editCodeSpace}>
                    Save
                  </button>
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
                        Are you sure to delete colloborator &apos;{collaboratorToDelete?.firstName}&apos;?
                        <p>
                          Removing collaborator from codespace make him/her deny the acccess to the code space and code.
                        </p>
                      </div>
                    )}
                    {collaboratorToTransferOwnership && (
                      <div>
                        Are you sure to transfer code space ownership to the colloborator &apos;
                        {collaboratorToTransferOwnership?.firstName}&apos;?
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
          {!isPublicRecipeChoosen && !isUserDefinedGithubRecipe && !DEPLOYMENT_DISABLED_RECIPE_IDS.includes(recipe?.aliasId) && (
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
                                          disabled
                                        />
                                      </span>
                                      <label className={Styles.permissionContent}>Develop</label>
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
                                          disabled
                                        // checked={item?.permission !== null ? item?.canDeploy : false}
                                        // onChange={(e) => onCollaboratorPermission(e, item.id)}
                                        />
                                      </span>
                                      <label className={Styles.permissionContent}>Deploy</label>
                                    </label>
                                  </div>
                                  &nbsp;&nbsp;&nbsp;
                                  <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                    <label className={'checkbox'}>
                                      <span className="wrapper">
                                        <input
                                          type="checkbox"
                                          className="ff-only"
                                          value="admin"
                                          disabled={props.user.id === item.id}
                                          checked={item?.isAdmin || false}
                                          onChange={(e) => onCollaboratorPermission(e, item?.id)}
                                        />
                                      </span>
                                      <label className={Styles.permissionContent}>Admin</label>
                                    </label>
                                  </div>
                                </div>
                                <div className={Styles.collaboratorTitleCol}>
                                  {item.id !== props.user.id && <span
                                    tooltip-data={'Remove Collaborator'}
                                    className={Styles.deleteEntry}
                                    onClick={onCollaboratorDelete(item.id)}
                                  >
                                    <i className="icon mbc-icon trash-outline" />
                                  </span>}
                                  {onEditingMode && (props?.onEditingCodeSpace?.projectDetails?.projectOwner?.id === props?.user?.id) && (
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
