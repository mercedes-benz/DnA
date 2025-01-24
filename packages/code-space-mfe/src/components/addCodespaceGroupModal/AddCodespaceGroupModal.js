import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './add-codespace-group-modal.scss';
import SelectBox from 'dna-container/SelectBox';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const CodespaceItem = ({ codespace }) => {
  return (
    <div className={Styles.workspaceItemContainer}>
      <div className={Styles.workspaceIcon}>
        <i className="icon mbc-icon workspace"></i>
      </div>
      <div className={Styles.workspaceContent}>
        <h3>{codespace?.name}Angular-Project</h3>
      </div>
    </div>
  )
}

const AddCodespaceGroupModal = ({ edit }) => {
  const [codeSpaces, setCodeSpaces] = useState([]);
  const [selectedCodeSpaces, setSelectedCodeSpaces] = useState([]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
      CodeSpaceApiClient.getCodeSpaceGroups()
        .then((res) => {
          if(res.status !== 204) {
            setCodeSpaces(res?.data?.records);
          } else {
            setCodeSpaces([]);
          }
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching code spaces failed!',
            'alert',
          );
        });
  }, []);

  const onCodeSpaceChange = (e) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        let temp = '';
        temp = option.value;
        selectedValues.push(temp);
      });
    }
    const selectedCodespaces = codeSpaces?.filter(codeSpace => selectedValues.includes(codeSpace.name));
    setSelectedCodeSpaces(selectedCodespaces);
  };

  return (
    <div className={classNames(Styles.form)}>
      <div className={Styles.formHeader}>
        <h3>Add Code Spaces to Group</h3>
        <p>Enter group name and add code spaces to start!</p>
      </div>
      {/* Search and request workspace */}
      <div className={Styles.searchContainer}>
        <div className={Styles.flex}>
          <div className={Styles.col}>
            <div className={classNames('input-field-group include-error')}>
              <label className={'input-label'}>
                Group Name <sup>*</sup>
              </label>
              <input
                type="text"
                className={'input-field'}
                id="workspaceName"d
                placeholder="Type here"
                autoComplete="off"
                maxLength={256}
                disabled={edit}
                // defaultValue={nameOfWorkspace}
                // {...register('name', { required: '*Missing entry', pattern: /^(?!Admin monitoring$)(?!^\s+$)[\w\d-_]+$/, onChange: (e) => { setNameOfWorkspace(e.target.value) } })}
              />
              {/* <span className={'error-message'}>{errors?.name?.message}{errors.name?.type === 'pattern' && 'Workspace names must contain alphanumeric characters only - and _ are allowed. \'Admin monitoring\' name and spaces are not allowed.'}</span> */}
            </div>
          </div>
          <div className={Styles.col}>
            <div className={classNames('input-field-group')}>
              <label className="input-label" htmlFor="codespaceSelect">
                Select Code Spaces <sup>*</sup>
              </label>
              <div className="custom-select">
                <select
                  id="codespaceSelect"
                  multiple={true}
                  required={true}
                  onChange={onCodeSpaceChange}
                  value={selectedCodeSpaces?.map(codeSpace => codeSpace?.name)}
                >
                  {codeSpaces?.map(codeSpace => 
                    <option key={codeSpace?.id} value={codeSpace?.name}>{codeSpace?.name}Angular-Project</option>
                  )}
                  <option>Angular-Project</option>
                  <option>Angular-Project</option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.codespaceList}>
          {selectedCodeSpaces.map(codeSpace => <CodespaceItem key={codeSpace?.id} />)}
        </div>
        <button className={classNames('btn btn-tertiary', Styles.btnSubmit)}>{edit ? 'Update Group' : 'Create Group'}</button>
      </div>
    </div>
  );
}

export default AddCodespaceGroupModal;
