import React, { useState } from 'react';
import cn from 'classnames';
import Styles from './NewCodeSpace.scss';
// import { ApiClient } from '../../../../services/ApiClient';

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import { Notification } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import SelectBox from 'components/formElements/SelectBox/SelectBox';

import { trackEvent } from '../../../../services/utils';
import TextBox from '../../shared/textBox/TextBox';
import { ICodeSpaceData } from '../CodeSpace';
import { useEffect } from 'react';
import { ICodeCollaborator, IUserDetails, IUserInfo } from 'globals/types';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import AddUser from '../../addUser/AddUser';
import { Envs } from 'globals/Envs';
import { recipesMaster } from '../../../../services/utils';

const classNames = cn.bind(Styles);

export interface ICodeSpaceProps {
  user: IUserInfo;
  onBoardingCodeSpace?: ICodeSpaceData;
  isCodeSpaceCreationSuccess?: (status: boolean, codeSpaceData: ICodeSpaceData) => void;
  toggleProgressMessage?: (show: boolean) => void;
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
  const [projectName, setProjectName] = useState('');
  const [projectNameError, setProjectNameError] = useState('');
  const [environment, setEnvironment] = useState('DHC-CaaS');
  const [recipeValue, setRecipeValue] = useState('0');
  const recipes = recipesMaster;

  const [recipeError, setRecipeError] = useState('');
  const [passwordError, setPasswordErr] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');
  const [passwordInput, setPasswordInput] = useState({
    password: '',
    confirmPassword: '',
  });
  const [githubToken, setGithubToken] = useState('');
  const [githubTokenError, setGithubTokenError] = useState('');
  const [codeSpaceCollaborators, setCodeSpaceCollaborators] = useState([]);
  // const [codeSpaceCollaboratorsError, setCodeSpaceCollaboratorsError] = useState('');


  const [livelinessInterval, setLivelinessInterval] = useState<NodeJS.Timer>();

  // const [createdCodeSpaceName, setCreatedCodeSpaceName] = useState('');

  const requiredError = '*Missing entry';
  const livelinessIntervalRef = React.useRef<NodeJS.Timer>();
  // let livelinessInterval: any = undefined;

  useEffect(() => {
    SelectBox.defaultSetup(true);
  }, []);

  useEffect(() => {
    livelinessIntervalRef.current = livelinessInterval;
    return () => {
      livelinessIntervalRef.current && clearInterval(livelinessIntervalRef.current);
    };
  }, [livelinessInterval]);

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
  
  const onGithubTokenOnChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const githubTokenVal = evnt.currentTarget.value.trim();
    setGithubToken(githubTokenVal);
    setGithubTokenError(githubTokenVal.length ? '' : githubTokenVal);
  };

  const onEnvironmentChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    setEnvironment(evnt.currentTarget.value.trim());
  };

  const onRecipeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = e.currentTarget.value;
    setRecipeValue(selectedOption);
    setRecipeError(selectedOption !== '0' ? '' : requiredError);
  };

  const handlePasswordChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const passwordInputValue = evnt.currentTarget.value.trim();
    const passwordInputFieldName = evnt.currentTarget.name;
    const NewPasswordInput = { ...passwordInput, [passwordInputFieldName]: passwordInputValue };
    setPasswordInput(NewPasswordInput);
  };

  const handleValidation = (evnt: React.FormEvent<HTMLInputElement>) => {
    const passwordInputValue = evnt.currentTarget.value.trim();
    const passwordInputFieldName = evnt.currentTarget.name;
    //for password
    if (passwordInputFieldName === 'password') {
      const oneLetterRegExp = /(?=.*?[a-zA-Z])/;
      // const uppercaseRegExp = /(?=.*?[A-Z])/;
      // const lowercaseRegExp = /(?=.*?[a-z])/;
      const digitsRegExp = /(?=.*?[0-9])/;
      // const specialCharRegExp = /(?=.*?[#?!@$%^&*-])/;
      const minLengthRegExp = /.{8,}/;
      const passwordLength = passwordInputValue.length;
      const atleastOneLetter = oneLetterRegExp.test(passwordInputValue);
      // const uppercasePassword = uppercaseRegExp.test(passwordInputValue);
      // const lowercasePassword = lowercaseRegExp.test(passwordInputValue);
      const digitsPassword = digitsRegExp.test(passwordInputValue);
      // const specialCharPassword = specialCharRegExp.test(passwordInputValue);
      const minLengthPassword = minLengthRegExp.test(passwordInputValue);
      let errMsg = '';
      if (passwordLength === 0) {
        errMsg = 'Password is empty';
      } else if (!atleastOneLetter) {
        errMsg = 'At least one letter';
      } else if (!digitsPassword) {
        /*else if (!uppercasePassword) {
        errMsg = 'At least one Uppercase';
      } else if (!lowercasePassword) {
        errMsg = 'At least one Lowercase';
      }*/
        errMsg = 'At least one digit';
      } else if (!minLengthPassword) {
        /*else if (!specialCharPassword) {
        errMsg = 'At least one Special Characters';
      }*/
        errMsg = 'At least minumum 8 characters';
      } else {
        errMsg = '';
      }
      setPasswordErr(errMsg);
    }
    // for confirm password
    if (
      passwordInputFieldName === 'confirmPassword' ||
      (passwordInputFieldName === 'password' && passwordInput.confirmPassword.length > 0)
    ) {
      if (passwordInput.confirmPassword !== passwordInput.password) {
        setConfirmPasswordError('Confirm password is not matched');
      } else {
        setConfirmPasswordError('');
      }
    }
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

  const onCollaboratorPermission = (e: React.ChangeEvent<HTMLInputElement>, userName: string) => {
    const codeSpaceCollaborator = codeSpaceCollaborators.find((item: ICodeCollaborator) => {
      return item.id == userName;
    });

    if (e.target.checked) {
      codeSpaceCollaborator.canDeploy = true;
    } else {
      codeSpaceCollaborator.canDeploy = false;
    }
    setCodeSpaceCollaborators([...codeSpaceCollaborators]);
  };

  const onCollabaratorDelete = (userName: string) => {
    return () => {
      const currentCollList = codeSpaceCollaborators.filter((item) => {
        return item.id !== userName;
      });
      setCodeSpaceCollaborators(currentCollList);
    };
  };

  const validateNewCodeSpaceForm = () => {
    let formValid = true;
    if (!projectName.length) {
      setProjectNameError(requiredError);
      formValid = false;
    }
    if (recipeValue === '0') {
      setRecipeError(requiredError);
      formValid = false;
    }
    if (passwordInput.password === '') {
      setPasswordErr(requiredError);
      formValid = false;
    }
    if (passwordInput.confirmPassword === '') {
      setConfirmPasswordError(requiredError);
      formValid = false;
    }
    if (githubToken === '') {
      setGithubTokenError(requiredError);
      formValid = false;
    }
    if (projectNameError !== '' || recipeError !== '' || passwordError !== '' || confirmPasswordError !== '' || githubTokenError !== '') {
      formValid = false;
    }
    return formValid;
  };

  const validateOnBoardCodeSpaceForm = () => {
    let formValid = true;
    if (passwordInput.password === '') {
      setPasswordErr(requiredError);
      formValid = false;
    }
    if (passwordInput.confirmPassword === '') {
      setConfirmPasswordError(requiredError);
      formValid = false;
    }
    if (githubToken === '') {
      setGithubTokenError(requiredError);
      formValid = false;
    }
    if (passwordError !== '' || confirmPasswordError !== '' || githubTokenError !== '') {
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
  //             url: `https://code-spaces.***REMOVED***/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
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
            recipeId: recipeValue
          }
        }
      },
      password: passwordInput.password,
      pat: githubToken
    };

    // const createCodeSpaceRequest = {
    //   data: {
    //     cloudServiceProvider: environment,
    //     name: projectName,
    //     recipeId: recipeValue,
    //   },
    //   password: passwordInput.password,
    // };

    if (validateNewCodeSpaceForm()) {
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
            Notification.show('Error in creating new code space. Please try again later.\n' + err, 'alert');
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

    const onBoardCodeSpaceRequest = {
      password: passwordInput.password,
      pat: githubToken
    };

    if (validateOnBoardCodeSpaceForm()) {
      ProgressIndicator.show();
      CodeSpaceApiClient.onBoardCollaborator(props.onBoardingCodeSpace.id, onBoardCodeSpaceRequest)
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
            Notification.show('Error in creating new code space. Please try again later.\n' + err, 'alert');
          }
        });
    }
  };

  const projectDetails = props.onBoardingCodeSpace?.projectDetails;
  return (
    <React.Fragment>
      {onBoadingMode ? (
        <div className={Styles.newCodeSpacePanel}>
          <div className={Styles.addicon}> &nbsp; </div>
          <h3>Hello {namePrefix}, On-board to Code Space - {projectDetails.projectName}</h3>
          <p>Protect your code space with the password of your own.</p>
          <div className={Styles.codeSpaceDetails}>
            <div className={Styles.flexLayout}>
              <div>
                <label>Name</label>
              </div>
              <div>
                {projectDetails.projectName}
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <label>Recipe</label>
              </div>
              <div>
                {recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId).name}
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <label>Environment</label>
              </div>
              <div>
                {projectDetails.recipeDetails.cloudServiceProvider}
              </div>
            </div>
          </div>
          <div className={Styles.flexLayout}>
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
          </div>
          <div>
            <div>
              <TextBox
                type="password"
                controlId={'githubTokenInput'}
                labelId={'githubTokenLabel'}
                label={`Your Github(${Envs.CODE_SPACE_GIT_PAT_APP_URL}) Personal Access Token`}
                infoTip="Not stored only used for Repo Creation"
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
          </div>
          <div className={Styles.flexLayout}>
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
                </div>
              </div>
            </div>
          </div>
          <div className={Styles.flexLayout}>
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
          </div>
          <div>
            <div>
              <TextBox
                type="password"
                controlId={'githubTokenInput'}
                labelId={'githubTokenLabel'}
                label={`Your Github(${Envs.CODE_SPACE_GIT_PAT_APP_URL}) Personal Access Token`}
                infoTip="Not stored only used for Repo Creation"
                placeholder={'Type here'}
                value={githubToken}
                errorText={githubTokenError}
                required={true}
                maxLength={50}
                onChange={onGithubTokenOnChange}
              />
            </div>
          </div>
          <div className={classNames('input-field-group include-error')}>
            <label htmlFor="userId" className="input-label">
              Find and add the collaborators you want to work with your code (Optional)
            </label>
            <div className={Styles.collaboratorSection}>
              <div className={Styles.collaboratorSectionList}>
                <div className={Styles.collaboratorSectionListAdd}>
                  <AddUser getCollabarators={getCollabarators} dagId={''} isRequired={false} isUserprivilegeSearch={false} />
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
                              <div className={Styles.collaboratorTitleCol}>{item.firstName + ' ' + item.lastName}</div>
                              <div className={Styles.collaboratorTitleCol}>
                              <div className={classNames('input-field-group include-error ' + Styles.inputGrp)}>
                                <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                                  <span className="wrapper">
                                    <input type="checkbox" className="ff-only" value="develop" checked={true} readOnly />
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
                                <div className={Styles.deleteEntry} onClick={onCollabaratorDelete(item.id)}>
                                  <i className="icon mbc-icon trash-outline" />
                                  Delete Entry
                                </div>
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
          <div className={Styles.newCodeSpaceBtn}>
            <button className={' btn btn-tertiary '} onClick={createCodeSpace}>
              Create Code Space
            </button>
          </div>
        </div>
      )}
    </React.Fragment>
  );
};

export default NewCodeSpace;
