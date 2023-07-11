import React, { useState, forwardRef, useImperativeHandle, Ref } from 'react';
import Styles from './NewSandbox.scss';
import { ApiClient } from '../../../services/ApiClient';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { trackEvent } from '../../../services/utils';
import { INotebookInfo } from 'globals/types';
import TextBox from '../shared/textBox/TextBox';
import TextArea from '../shared/textArea/TextArea';

export interface INewSanboxProps {
  namePrefix: string;
  isNotebookCreationSuccess?: (status: boolean, notebookdata: INotebookInfo) => void;
  inComputeTab?: boolean;
}

export interface INewSandBoxRef {
  validateAndCreateSandBox: () => void;
}

const Newsandbox = forwardRef((props: INewSanboxProps, ref: Ref<INewSandBoxRef>) => {
  // User Name
  const namePrefix = props.namePrefix;

  const [solutionName, setSolutionName] = useState(namePrefix + ' workspace');
  const [description, setDescription] = useState('Jupyter Notebook workspace of ' + namePrefix);

  const [solutionNameErr, setSolutionNameErr] = useState('');
  const [descriptionErr, setDescriptionErr] = useState('');

  const solutionNameGet = (e: React.FormEvent<HTMLInputElement>) => {
    setSolutionName(e.currentTarget.value);
    if (e.currentTarget.value === '') {
      setSolutionNameErr('*Missing entry');
    } else {
      setSolutionNameErr('');
    }
  };
  const descriptionGet = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setDescription(e.currentTarget.value);
    if (e.currentTarget.value === '') {
      setDescriptionErr('*Missing entry');
    } else {
      setDescriptionErr('');
    }
  };
  const validateDescriptionForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (solutionName === '') {
      setSolutionNameErr(errorMissingEntry);
      formValid = false;
    }
    if (description === '') {
      setDescriptionErr(errorMissingEntry);
      formValid = false;
    }
    return formValid;
  };

  const createSandbox = () => {
    const sandBoxData = {
      name: solutionName,
      description,
    };
    if (validateDescriptionForm()) {
      ProgressIndicator.show();
      ApiClient.createNewSandbox(sandBoxData)
        .then((response) => {
          trackEvent('DnA Notebook', 'Create new notebook workspace', solutionName);
          ProgressIndicator.hide();
          props.isNotebookCreationSuccess(false, response.data);
        })
        .catch((error: Error) => {
          ProgressIndicator.hide();
          Notification.show('Error creating Jupyter Notebook workspace - ' + error.message, 'alert');
        });
    }
  };
  // const requiredError = '*Missing entry';

  useImperativeHandle(ref, () => ({
    validateAndCreateSandBox() {
      createSandbox();
    },
  }));

  return (
    <React.Fragment>
      <div className={Styles.sandboxpanel}>
        {props.inComputeTab ? (
          ''
        ) : (
          <>
            <div className={Styles.addicon}> &nbsp; </div>
            <h3> Start My Workspace </h3>
            <p>
              For working with Jupyter Notebooks, a dummy solution is required. It is only visible for you and can be
              provisioned as an official solution within 4 days.
            </p>
          </>
        )}
        <TextBox
          type="text"
          controlId={'solutionNameInput'}
          labelId={'solutionNameLabel'}
          label={'Workspace Name'}
          placeholder={'Type here'}
          value={solutionName}
          errorText={solutionNameErr}
          required={true}
          maxLength={200}
          onChange={solutionNameGet}
        />
        <TextArea
          controlId={'descriptionInput'}
          labelId={'descriptionLabel'}
          label={'Description'}
          value={description}
          errorText={descriptionErr}
          required={true}
          maxlength={200}
          onChange={descriptionGet}
          small={true}
        />
        {props.inComputeTab ? (
          <p className={Styles.computeInfo}>
            On Save &amp; Next new Notebook will be created and linked to this solution.
          </p>
        ) : (
          <div className={Styles.sandboxbtn}>
            <button className={' btn btn-tertiary '} onClick={createSandbox}>
              Start Workspace
            </button>
          </div>
        )}
      </div>
    </React.Fragment>
  );
});

export default Newsandbox;
