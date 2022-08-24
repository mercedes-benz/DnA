import React, { useState, useEffect } from 'react';
import Styles from './Editdetails.scss';

// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { INotebookInfo, INotebookInfoData } from '../../../../globals/types';
import { ApiClient } from '../../../../services/ApiClient';
import TextBox from '../../shared/textBox/TextBox';
import TextArea from '../../shared/textArea/TextArea';

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
  // const requiredError = '*Missing entry';
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
        <TextBox
          type="text"
          controlId={'solutionNameInput'}
          labelId={'solutionNameLabel'}
          label={'Workspace Name'}
          placeholder={"Type here"}
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
