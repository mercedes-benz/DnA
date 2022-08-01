import React, { useState } from 'react';
import Styles from './PipelineSubModel.scss';

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { MalwarescanApiClient } from '../../../../services/MalwarescanApiClient';
import TextBox from '../../shared/textBox/TextBox';
import TextArea from '../../shared/textArea/TextArea';

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
  // const requiredError = '*Missing entry';
  return (
    <React.Fragment>
      <div className={Styles.MalwarescanPopContent}>
        <br />
        <TextBox
          type="text"
          controlId={'ApplicationNameInput'}
          labelId={'ApplicationName'}
          label={'Application Name'}
          placeholder={"Type here"}
          value={solutionName}
          errorText={solutionNameErr}
          required={true}
          maxLength={200}
          onChange={solutionNameGet}
        />
        <TextArea
          controlId={'ApplicationDescriptionInput'}
          labelId={'ApplicationDescription'}
          label={'Description'}
          value={description}
          errorText={descriptionErr}
          required={true}
          maxlength={200}
          onChange={descriptionGet}
          small={true}
        />
        <div className={Styles.sandboxbtn}>
          <button className={'btn btn-tertiary'} onClick={addAPIkey}>
            Subscribe
          </button>
        </div>
      </div>
    </React.Fragment>
  );
}
