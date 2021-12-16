import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './Editdetails.scss';
const classNames = cn.bind(Styles);

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { INotebookInfo, INotebookInfoData } from '../../../../globals/types';
import { ApiClient } from '../../../../services/ApiClient';

export interface IeditDetalsProps {
  notebookexistingDetails: INotebookInfo;
  isNotebookUpdateSuccess: (status: boolean, notebookdata: INotebookInfoData) => void;
}
export function Editdetails(props: IeditDetalsProps) {
  const [btndisable, setbtnDisable] = useState(false);
  const [solutionName, setSolutionName] = useState(props.notebookexistingDetails.name);
  const [description, setDescription] = useState(props.notebookexistingDetails.description);

  const [solutionNameOld] = useState(props.notebookexistingDetails.name);
  const [descriptionOld] = useState(props.notebookexistingDetails.description);
  const [solutionNameErr, setSolutionNameErr] = useState('');
  const [descriptionErr, setDescriptionErr] = useState('');

  const solutionNameGet = (e: any) => {
    setSolutionName(e.currentTarget.value);
    if (e.currentTarget.value === '') {
      setSolutionNameErr('*Missing entry');
    } else {
      setSolutionNameErr('');
    }
  };
  const descriptionGet = (e: any) => {
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
  const updateSandbox = () => {
    const sandBoxData = {
      name: solutionName,
      description,
    };
    if (validateDescriptionForm()) {
      ProgressIndicator.show();
      ApiClient.updateSandbox(sandBoxData)
        .then((response: INotebookInfoData) => {
          ProgressIndicator.hide();
          props.isNotebookUpdateSuccess(false, response);
          setbtnDisable(true);
        })
        .catch((err: any) => err);
    }
  };
  // Any updated happen
  useEffect(() => {
    if (solutionName !== solutionNameOld) {
      setbtnDisable(false);
    } else if (description !== descriptionOld) {
      setbtnDisable(false);
    } else if (solutionName === solutionNameOld || description === descriptionOld) {
      setbtnDisable(true);
    }
  }, [solutionName, description]);
  const requiredError = '*Missing entry';
  return (
    <React.Fragment>
      <div className={Styles.sandboxpanel}>
        <div className={Styles.editicon}>
          <i className="icon mbc-icon edit small " />
        </div>
        <h3> Edit Details </h3>
        {/* <p>
          Currently the dummy solution is only visible for you. It can be provisioned as an official solution when you
          enter a name and give a short description. (3 days left)
        </p> */}
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
          <span className={classNames('error-message', descriptionErr.length ? '' : 'hide')}>{descriptionErr}</span>
        </div>
        <div className={Styles.sandboxbtn}>
          <button
            className={
              btndisable || solutionNameErr.length || descriptionErr.length
                ? 'btn btn-tertiary disable'
                : 'btn btn-tertiary'
            }
            onClick={updateSandbox}
          >
            Update Workspace
          </button>
        </div>
      </div>
    </React.Fragment>
  );
}
