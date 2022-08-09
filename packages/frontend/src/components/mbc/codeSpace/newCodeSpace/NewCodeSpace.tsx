import React, { useState } from 'react';
import cn from 'classnames';
import Styles from './NewCodeSpace.scss';
import { ApiClient } from '../../../../services/ApiClient';

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import { Notification } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import SelectBox from '../../../formElements/SelectBox/SelectBox';

import { trackEvent } from '../../../../services/utils';
import TextBox from '../../shared/textBox/TextBox';
import { ICodeSpaceData } from '../CodeSpace';
import { useEffect } from 'react';
import { IUserInfo } from '../../../../globals/types';

const classNames = cn.bind(Styles);

export interface ICodeSpaceProps {
  user: IUserInfo;
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

  const [recipeValues, setRecipeValues] = useState([]);
  const recipes = [
    { id: 'ms-springboot', name: 'Microservice using Spring Boot (Debian 11 OS, 2GB RAM, 1CPU)' },
    { id: 'dna', name: 'DnA Workspace (Coming Soon)' },
    { id: 'chronos', name: 'CHRONOS Workspace (Coming Soon)' },
    { id: 'mean', name: 'MEAN Stack (Coming Soon)' },
    { id: 'mern', name: 'MERN Stack (Coming Soon)' },
  ];

  const [recipeError, setRecipeError] = useState('');
  const [passwordError, setPasswordErr] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');
  const [passwordInput, setPasswordInput] = useState({
    password: '',
    confirmPassword: '',
  });

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  const onRecipeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues:string[] = [];
    // this.props.onStateChange();
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValues.push(option.value);
      });
    }
    setRecipeValues(selectedValues);
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
      // const uppercaseRegExp = /(?=.*?[A-Z])/;
      // const lowercaseRegExp = /(?=.*?[a-z])/;
      const digitsRegExp = /(?=.*?[0-9])/;
      // const specialCharRegExp = /(?=.*?[#?!@$%^&*-])/;
      const minLengthRegExp = /.{8,}/;
      const passwordLength = passwordInputValue.length;
      // const uppercasePassword = uppercaseRegExp.test(passwordInputValue);
      // const lowercasePassword = lowercaseRegExp.test(passwordInputValue);
      const digitsPassword = digitsRegExp.test(passwordInputValue);
      // const specialCharPassword = specialCharRegExp.test(passwordInputValue);
      const minLengthPassword = minLengthRegExp.test(passwordInputValue);
      let errMsg = '';
      if (passwordLength === 0) {
        errMsg = 'Password is empty';
      }
      /*else if (!uppercasePassword) {
        errMsg = 'At least one Uppercase';
      } else if (!lowercasePassword) {
        errMsg = 'At least one Lowercase';
      }*/
      else if (!digitsPassword) {
        errMsg = 'At least one digit';
      } 
      /*else if (!specialCharPassword) {
        errMsg = 'At least one Special Characters';
      }*/
      else if (!minLengthPassword) {
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
  const requiredError = '*Missing entry';

  const validateNewCodeSpaceForm = () => {
    let formValid = true;
    if (!recipeValues.length) {
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
    if (passwordError !== '' || confirmPasswordError !== '') {
      formValid = false;
    }
    return formValid;
  };

  let livelinessInterval:any = undefined;
  const enableLivelinessCheck = () => {
    clearInterval(livelinessInterval);
    livelinessInterval = setInterval(() => {
      ApiClient.getCodeSpace().then((res: any) => {
        if(res.success === 'true') {
          props.toggleProgressMessage(false);
          ProgressIndicator.hide();
          clearInterval(livelinessInterval);
          props.isCodeSpaceCreationSuccess(true, { 
            url: `https://code-spaces.dev.dna.app.corpintra.net/${props.user.id.toLocaleLowerCase()}/`,
            running: true,
          });
          Notification.show('Code space succesfully created.');
        }
      }).catch((err: Error) => {
        clearInterval(livelinessInterval);
        props.toggleProgressMessage(false);
        ProgressIndicator.hide();
        Notification.show("Error in validating code space - " + err.message, 'error');
      });
    }, 2000);
  }

  const createCodeSpace = () => {
    const codeSpaceData = {
      recipeId: recipeValues.join(''),
      password: passwordInput.password,
    };
    if (validateNewCodeSpaceForm()) {
      ProgressIndicator.show();
      ApiClient.createCodeSpace(codeSpaceData)
        .then((res) => {
          trackEvent('DnA Code Space', 'Create', 'New code space');
          if(res.success === 'Success') {
            props.toggleProgressMessage(true);
            enableLivelinessCheck();
          } else {
            props.toggleProgressMessage(false);
            ProgressIndicator.hide();
            Notification.show('Error in creating new code space. Please try again later.\n' + res.errors[0].message, 'error');
          }
        })
        .catch((err: Error) => {
          props.toggleProgressMessage(false);
          ProgressIndicator.hide();
          Notification.show('Error in creating new code space. Please try again later.\n' + err, 'error');
        });
    }
  };
  
  return (
    <React.Fragment>
      <div className={Styles.newCodeSpacePanel}>
        <div className={Styles.addicon}> &nbsp; </div>
        <h3>Hello {namePrefix}, Create your Code Space</h3>
        <p>
          Protect your code space with the password of your own.
        </p>
        <p className={Styles.passwordInfo}>
          Note: Password should be minimum 8 chars in length and alpha numeric.
        </p>
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
              value={recipeValues.join('')}
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
          <span className={classNames('error-message', recipeError.length ? '' : 'hide')}>
            {recipeError}
          </span>
        </div>
        <TextBox
          type="password"
          controlId={'codeSpacePasswordInput'}
          name="password"
          labelId={'codeSpacePasswordLabel'}
          label={'Code Space Password'}
          placeholder={"Type here"}
          value={passwordInput.password}
          errorText={passwordError}
          required={true}
          maxLength={20}
          onChange={handlePasswordChange}
          onKeyUp={handleValidation}
        />
        <TextBox
          type="password"
          controlId={'codeSpaceConfirmPasswordInput'}
          name="confirmPassword"
          labelId={'codeSpaceConfirmPasswordLabel'}
          label={'Confirm Code Space Password'}
          placeholder={"Type here"}
          value={passwordInput.confirmPassword}
          errorText={confirmPasswordError}
          required={true}
          maxLength={20}
          onChange={handlePasswordChange}
          onKeyUp={handleValidation}
        />
        <div className={Styles.newCodeSpaceBtn}>
          <button className={' btn btn-tertiary '} onClick={createCodeSpace}>
              Create Code Space
          </button>
        </div>
      </div>
    </React.Fragment>
  );
};

export default NewCodeSpace;
