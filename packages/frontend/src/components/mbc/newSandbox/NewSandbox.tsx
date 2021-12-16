import cn from 'classnames';
import React, { useState, forwardRef, useImperativeHandle, Ref } from 'react';
import Styles from './NewSandbox.scss';
const classNames = cn.bind(Styles);
import { ApiClient } from '../../../services/ApiClient';

// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
import { trackEvent } from '../../../services/utils';
import { INotebookInfo } from '../../../globals/types';

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
          trackEvent('DnA Notebook', 'Create new workspace', solutionName);
          ProgressIndicator.hide();
          props.isNotebookCreationSuccess(false, response.data);
        })
        .catch((err) => err);
    }
  };
  const requiredError = '*Missing entry';

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
        <div className={classNames('input-field-group include-error', solutionNameErr.length ? 'error' : '')}>
          <label id="solutionNameLabel" htmlFor="solutionNameInput" className="input-label">
            Workspace Name<sup>*</sup>
          </label>
          <input
            type="text"
            className="input-field"
            required={true}
            required-error={requiredError}
            id="solutionNameInput"
            maxLength={200}
            placeholder="Type here"
            autoComplete="off"
            onChange={solutionNameGet}
            value={solutionName}
          />
          <span className={classNames('error-message', solutionNameErr.length ? '' : 'hide')}>{solutionNameErr}</span>
        </div>
        <div className={classNames('input-field-group include-error', descriptionErr.length ? 'error' : '')}>
          <label id="descriptionLabel" htmlFor="descriptionInput" className="input-label">
            Description<sup>*</sup>
          </label>
          <textarea
            className="input-field-area small"
            required={true}
            required-error={requiredError}
            id="descriptionInput"
            maxLength={200}
            autoComplete="off"
            onChange={descriptionGet}
            value={description}
          />
          <span className={classNames('error-message', descriptionErr.length ? 'aaa' : 'hide')}>{descriptionErr}</span>
        </div>
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
