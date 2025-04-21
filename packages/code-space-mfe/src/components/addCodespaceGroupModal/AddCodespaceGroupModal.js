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
        <h3>{codespace?.projectDetails?.projectName}</h3>
      </div>
    </div>
  )
}

const AddCodespaceGroupModal = ({ edit, group, onSave }) => {
  const [groupName, setGroupName] = useState(edit ? group?.name : '');
  const [codeSpaces, setCodeSpaces] = useState([]);
  const [selectedCodeSpaces, setSelectedCodeSpaces] = useState([]);
  const [errors, setErrors] = useState({ groupName: '', codespaces: [] });

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  useEffect(() => {
    ProgressIndicator.show();
      CodeSpaceApiClient.getCodeSpacesList()
        .then((res) => {
          if(res?.data?.records) {
            const modifiedWorkspaces = edit ? group?.workspaces : [];
            setCodeSpaces([...res.data.records, ...modifiedWorkspaces]);
            setSelectedCodeSpaces(modifiedWorkspaces);
            SelectBox.defaultSetup();
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
  }, [edit, group]);

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
    const selectedCodespaces = codeSpaces?.filter(codeSpace => selectedValues.includes(codeSpace?.projectDetails?.projectName));
    setSelectedCodeSpaces(selectedCodespaces);
    selectedCodeSpaces.length > 0 && setErrors(prevError => { return {...prevError, codespaces: ''}});
  };

  const findCodespaceChanges = (codespaceList, codespaces, selectedCodespaces) => {
    const codespaceSet = new Set(codespaces.map(cs => cs.workspaceId));
    const selectedSet = new Set(selectedCodespaces.map(cs => cs.workspaceId));

    // Find added codespaces (in selected but not in original codespaces)
    const addedCodespaces = codespaceList.filter(cs => selectedSet.has(cs.workspaceId) && !codespaceSet.has(cs.workspaceId));

    // Find removed codespaces (in original codespaces but not in selected)
    const removedCodespaces = codespaceList.filter(cs => codespaceSet.has(cs.workspaceId) && !selectedSet.has(cs.workspaceId));

    return { addedCodespaces, removedCodespaces };
  }

  const validate = () => {
    let success = true;
    if(groupName.length === 0) {
      success = false;
      setErrors(prevError => { return {...prevError, groupName: '*Missing entry'}});
    }
    if(selectedCodeSpaces.length === 0) {
      success = false;
      setErrors(prevError => { return {...prevError, codespaces: '*Missing entry'}});
    }
    return success;
  }

  const handleEditGroup = () => {
    const { addedCodespaces, removedCodespaces } = findCodespaceChanges(codeSpaces, group?.workspaces, selectedCodeSpaces);
    const data = {
      groupId: group?.groupId,
      name: group?.name,
      order: 0,
      wsAdded: addedCodespaces?.map((codespace) => { return { name: codespace?.projectDetails?.projectName, order: 0, wsId: codespace?.workspaceId }}),
      wsRemoved: removedCodespaces?.map((codespace) => { return { name: codespace?.projectDetails?.projectName, order: 0, wsId: codespace?.workspaceId }}),
    }
    if(validate()) {
      ProgressIndicator.show();
      CodeSpaceApiClient.editCodeSpaceGroup(data)
        .then(() => {
          Notification.show(`Code Space Group edited successfully`);
          onSave();
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Editing code space group failed!',
            'alert',
          );
        });
    }
  }

  const handleCreateGroup = () => {
    const data = {
      groupId: '',
      name: groupName,
      order: 0,
      workspaces: selectedCodeSpaces?.map((codespace) => { return { name: codespace?.projectDetails?.projectName, order: 0, workspaceId: codespace?.workspaceId }})
    }
    if(validate()) {
      ProgressIndicator.show();
      CodeSpaceApiClient.createCodeSpaceGroup(data)
        .then((res) => {
          Notification.show(`Code Space Group ${res?.data?.data?.name} created successfully`);
          onSave();
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Creating code space group failed!',
            'alert',
          );
        });
    }
  }

  const onGroupNameChange = (e) => {
    setGroupName(e.target.value);
    e.target.value.length > 0 && setErrors(prevError => { return {...prevError, groupName: ''}});
  }

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
            <div className={classNames('input-field-group include-error', errors.groupName?.length > 0 && 'error')}>
              <label className={'input-label'}>
                Group Name <sup>*</sup>
              </label>
              <input
                type="text"
                className={'input-field'}
                id="groupName"
                placeholder="Type here"
                autoComplete="off"
                maxLength={256}
                disabled={edit}
                defaultValue={groupName}
                onChange={onGroupNameChange}
              />
              <span className={'error-message'}>{errors.groupName?.length > 0 && '*Missing entry'}</span>
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
                  value={selectedCodeSpaces?.map(codeSpace => codeSpace?.projectDetails?.projectName)}
                >
                  {codeSpaces?.map(codeSpace => 
                    <option key={codeSpace?.projectDetails?.projectName} value={codeSpace?.projectDetails?.projectName}>{codeSpace?.projectDetails?.projectName}</option>
                  )}
                </select>
              </div>
              <span className={'error-message'}>{errors.codespaces?.length > 0 && '*Missing entry'}</span>
            </div>
          </div>
        </div>
        <div className={Styles.codespaceList}>
          {selectedCodeSpaces?.map(codeSpace => <CodespaceItem key={codeSpace?.workspaceId} codespace={codeSpace} />)}
        </div>
        <button className={classNames('btn btn-tertiary', Styles.btnSubmit)} onClick={() => { edit ? handleEditGroup() : handleCreateGroup()}}>{edit ? 'Update Group' : 'Create Group'}</button>
      </div>
    </div>
  );
}

export default AddCodespaceGroupModal;
