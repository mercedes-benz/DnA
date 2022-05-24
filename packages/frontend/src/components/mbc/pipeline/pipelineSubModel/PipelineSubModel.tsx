import cn from 'classnames';
import React, { useState } from 'react';
import Styles from './PipelineSubModel.scss';
const classNames = cn.bind(Styles);

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { MalwarescanApiClient } from '../../../../services/MalwarescanApiClient';

export interface IPipelineSubModelProps {
  addAirflowSuccessFn: (action: String) => void;
}
export default function PipelineSubModel(props: IPipelineSubModelProps) {
  const [solutionName, setSolutionName] = useState('');
  const [description, setDescription] = useState('');

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
  const addAPIkey = () => {
    const addAPIkeyData = {
      data: {
        appName: solutionName,
        description,
      },
    };
    if (validateDescriptionForm()) {
      ProgressIndicator.show();
      MalwarescanApiClient.generateNewApiKey(addAPIkeyData)
        .then((response) => {
          console.log(response);
          ProgressIndicator.hide();
          props.addAirflowSuccessFn(response.data.id);
        })
        .catch((err: any) => err);
    }
  };
  // Any updated happen
  const requiredError = '*Missing entry';
  return (
    <React.Fragment>
      <div className={Styles.MalwarescanPopContent}>
        <br />
        <div className={classNames('input-field-group include-error', solutionNameErr.length ? 'error' : '')}>
          <label id="ApplicationName" htmlFor="ApplicationNameInput" className="input-label">
            Application Name<sup>*</sup>
          </label>
          <input
            type="text"
            className="input-field"
            required={true}
            required-error={requiredError}
            id="ApplicationNameInput"
            maxLength={200}
            placeholder="Type here"
            autoComplete="off"
            onChange={solutionNameGet}
            value={solutionName}
          />
          <span className={classNames('error-message', solutionNameErr.length ? '' : 'hide')}>{solutionNameErr}</span>
        </div>
        <div className={classNames('input-field-group include-error', descriptionErr.length ? 'error' : '')}>
          <label id="ApplicationDescription" htmlFor="ApplicationDescriptionInput" className="input-label">
            Description<sup>*</sup>
          </label>
          <textarea
            className="input-field-area small"
            required={true}
            required-error={requiredError}
            id="ApplicationDescriptionInput"
            maxLength={200}
            autoComplete="off"
            onChange={descriptionGet}
            value={description}
            placeholder="Type here"
          />
          <span className={classNames('error-message', descriptionErr.length ? '' : 'hide')}>{descriptionErr}</span>
        </div>
        <div className={Styles.sandboxbtn}>
          <button className={'btn btn-tertiary'} onClick={addAPIkey}>
            Subscribe
          </button>
        </div>
      </div>
    </React.Fragment>
  );
}
