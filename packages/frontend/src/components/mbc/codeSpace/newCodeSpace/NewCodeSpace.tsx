import React, { useState, forwardRef, useImperativeHandle, Ref } from 'react';
import Styles from './NewCodeSpace.scss';
import { ApiClient } from '../../../../services/ApiClient';

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { trackEvent } from '../../../../services/utils';
import TextBox from '../../shared/textBox/TextBox';
import { ICodeSpaceData } from '../CodeSpace';

export interface ICodeSpaceProps {
  namePrefix: string;
  isCodeSpaceCreationSuccess?: (status: boolean, codeSpaceData: ICodeSpaceData) => void;
}

export interface ICodeSpaceRef {
  validateAndCreateCodeSpace: () => void;
}

export interface ICreateCodeSpaceData {
  password: string;
}

const NewCodeSpace = forwardRef((props: ICodeSpaceProps, ref: Ref<ICodeSpaceRef>) => {

  const [passwordError, setPasswordErr] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');
  const [passwordInput, setPasswordInput] = useState({
    password: '',
    confirmPassword: '',
  });

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
      const uppercaseRegExp = /(?=.*?[A-Z])/;
      const lowercaseRegExp = /(?=.*?[a-z])/;
      const digitsRegExp = /(?=.*?[0-9])/;
      const specialCharRegExp = /(?=.*?[#?!@$%^&*-])/;
      const minLengthRegExp = /.{8,}/;
      const passwordLength = passwordInputValue.length;
      const uppercasePassword = uppercaseRegExp.test(passwordInputValue);
      const lowercasePassword = lowercaseRegExp.test(passwordInputValue);
      const digitsPassword = digitsRegExp.test(passwordInputValue);
      const specialCharPassword = specialCharRegExp.test(passwordInputValue);
      const minLengthPassword = minLengthRegExp.test(passwordInputValue);
      let errMsg = '';
      if (passwordLength === 0) {
        errMsg = 'Password is empty';
      } else if (!uppercasePassword) {
        errMsg = 'At least one Uppercase';
      } else if (!lowercasePassword) {
        errMsg = 'At least one Lowercase';
      } else if (!digitsPassword) {
        errMsg = 'At least one digit';
      } else if (!specialCharPassword) {
        errMsg = 'At least one Special Characters';
      } else if (!minLengthPassword) {
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
  const namePrefix = props.namePrefix;

  const validateDescriptionForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (passwordInput.password === '') {
      setPasswordErr(errorMissingEntry);
      formValid = false;
    }
    if (passwordInput.confirmPassword === '') {
      setConfirmPasswordError(errorMissingEntry);
      formValid = false;
    }
    if (passwordError !== '' || confirmPasswordError !== '') {
      formValid = false;
    }
    return formValid;
  };

  const createCodeSpace = () => {
    const codeSpaceData = {
      password: passwordInput.password,
    };
    if (validateDescriptionForm()) {
      ProgressIndicator.show();
      ApiClient.createCodeSpace(codeSpaceData)
        .then((response) => {
          trackEvent('DnA Code Space', 'Create', 'New code space');
          ProgressIndicator.hide();
          props.isCodeSpaceCreationSuccess(false, response.data);
        })
        .catch((err: Error) => {
          err
        });
    }
  };
  // const requiredError = '*Missing entry';

  useImperativeHandle(ref, () => ({
    validateAndCreateCodeSpace() {
      createCodeSpace();
    },
  }));

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
              Start Code Space
          </button>
        </div>
      </div>
    </React.Fragment>
  );
});

export default NewCodeSpace;
