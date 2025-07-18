import classNames from 'classnames';
import React, { useState, useEffect, ReactNode } from 'react';
import Styles from './VaultManagement.scss';
import TextBox from '/home/coder/app/packages/frontend/src/components/mbc/shared/textBox/TextBox';
import { PipelineApiClient } from '/home/coder/app/packages/frontend/src/services/PipelineApiClient';
import ProgressIndicator from '/home/coder/app/packages/frontend/src/assets/modules/uilab/js/src/progress-indicator';
import { Notification } from '/home/coder/app/packages/frontend/src/assets/modules/uilab/bundle/js/uilab.bundle';
import ConfirmModal from '/home/coder/app/packages/frontend/src/components/formElements/modal/confirmModal/ConfirmModal';
import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/theme-solarized_dark';

type KeyValueItem = {
  key: string;
  value: string;
  visible: boolean;
};

interface VaultManagementProps {
   projectName: string;
  // isStaging: boolean;
  // dagName: string;
}

const deleteCodeSpaceContent: ReactNode = (
  <div>
    <h3>Are you sure you want to delete this vault value?</h3>
  </div>
);

const VaultManagement: React.FC<VaultManagementProps> = ({ projectName}) => {
  const [key, setKey] = useState('');
  const [keyError, setKeyError] = useState('');
  const [value, setValue] = useState('');
  const [valueError, setValueError] = useState('');
  const [keyValue, setKeyValue] = useState<{ keyValueList: KeyValueItem[] }>({ keyValueList: [] });
  const [editingMode, setEditingMode] = useState(false);
  const [originalKey, setOriginalKey] = useState('');
  //const [originalValue, setOriginalValue] = useState('');
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [deleteKey, setDeleteKey] = useState('');
  const [deleteValue, setDeleteValue] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredList, setFilteredList] = useState<{ keyValueList: KeyValueItem[] }>({ keyValueList: [] });
  const [showJson, setShowJson] = useState(false);
  const [jsonData, setJsonData] = useState(JSON.stringify({ "": "" }, null, 2));
  const [jsonError, setJsonError] = useState('');
  const [isJsonTouched, setIsJsonTouched] = useState(false);
  const [toggleError, setToggleError] = useState('');

  // const env = isStaging ? 'int' : 'prod';

  useEffect(() => {
    loadVaultValues();
  }, []);

  // useEffect(() => {
  //   if (!dagName) return;
  //   loadVaultValues();
  // }, [dagName]);

  const loadVaultValues = () => {
    ProgressIndicator.show();
    console.log("project",projectName);
    PipelineApiClient.getVaultSecret(projectName)
      .then((response) => {
        const items: KeyValueItem[] = Object.entries(response.data).map(([k, v]) => ({
          key: k,
          value: v as string,
          visible: false,
        }));
        setKeyValue({ keyValueList: items });
        setFilteredList({ keyValueList: items });
      })
      .catch((err) => {
        const errors = err?.response?.data?.errors;
        if (Array.isArray(errors)) {
          errors.forEach((e: any) => Notification.show(e.message || 'Something went wrong.', 'alert'));
        } else {
          Notification.show(err.message || 'Something went wrong.', 'alert');
        }
      })
      .finally(() => {
        ProgressIndicator.hide();
      });
  };

  const refreshFilteredList = () => {
    const filtered = keyValue.keyValueList.filter((item) =>
      item.key.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredList({ keyValueList: filtered });
  };

  useEffect(() => {
    refreshFilteredList();
  }, [searchTerm, keyValue]);

  useEffect(() => {
    const updated: Record<string, string> = {};
    keyValue.keyValueList.forEach((i) => {
      updated[i.key] = i.value;
    });
    setJsonData(JSON.stringify(updated, null, 2));
  }, [keyValue]);

  const handleJsonChange = (txt: string) => {
    setIsJsonTouched(true);
    setJsonData(txt);
    try {
      JSON.parse(txt);
      setJsonError('');
    } catch (e: any) {
      setJsonError(e.message);
    }
  };

  const handleAddOrUpdate = () => {
    let valid = true;
    if (!key) {
      setKeyError('Missing entry');
      valid = false;
    }
    if (!value) {
      setValueError('Missing entry');
      valid = false;
    }
    if (
      keyValue.keyValueList.some(
        (item) => item.key === key && (!editingMode || item.key !== originalKey)
      )
    ) {
      setKeyError('Duplicate key');
      valid = false;
    }
    if (!valid) return;

    
    const updatedList = editingMode
      ? keyValue.keyValueList.map((item) =>
        item.key === originalKey ? { key, value, visible: item.visible } : item
      )
      : [...keyValue.keyValueList, { key, value, visible: false }];

    const data: Record<string, string> = {};
    updatedList.forEach((item) => {
      data[item.key] = item.value;
    });

    PipelineApiClient.putVaultSecret(projectName, data);
    setKeyValue({ keyValueList: updatedList });
    setKey('');
    setValue('');
    setEditingMode(false);
  };

  const handleEdit = (key: string, value: string) => {
    setOriginalKey(key);
    //setOriginalValue(value);
    setKey(key);
    setValue(value);
    setEditingMode(true);
  };

  const handleDelete = () => {
    const updated = keyValue.keyValueList.filter(
      (item) => !(item.key === deleteKey && item.value === deleteValue)
    );
    const data: Record<string, string> = {};
    updated.forEach((item) => {
      data[item.key] = item.value;
    });
    PipelineApiClient.putVaultSecret(projectName, data);
    setKeyValue({ keyValueList: updated });
    setShowConfirmModal(false);
  };

  // const toggleVisibility = (key: string) => {
  //   const updated = keyValue.keyValueList.map((item) =>
  //     item.key === key ? { ...item, visible: !item.visible } : item
  //   );
  //   setKeyValue({ keyValueList: updated });
  // };

  const handleSaveJson = () => {
    if (jsonError) return;
    PipelineApiClient.putVaultSecret(projectName, JSON.parse(jsonData))
      .then((res) => Notification.show('Saved JSON'))
      .catch((err) => Notification.show('Failed to save JSON', 'alert'));
    setIsJsonTouched(false);
    loadVaultValues();
    setToggleError('');
  };

  const onMagnify = (key: string) => {
    const updated = keyValue.keyValueList.map((item) =>
      item.key === key ? { ...item, visible: !item.visible } : item
    );
    setKeyValue({ keyValueList: updated });
  };

  const handleDiscard = () => {
    loadVaultValues();
    setIsJsonTouched(false);
    setJsonError('');
    setToggleError('');
  };

  const toggleJsonView = () => {
    if (showJson && isJsonTouched) {
      setToggleError('*Please save or discard your changes before you switch the view');
    } else {
      setToggleError('');
      setShowJson(!showJson);
    }
  };
  
  const onKeyChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setKey(e.target.value);
    if (e.target.value.trim()) {
      setKeyError('');
    }
  };
  
  const onValueChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value);
    if (e.target.value.trim()) {
      setValueError('');
    }
  };

  return (
    <>
      <div className={Styles.mainPanel}>
        <div className={Styles.toggleSwitch}>
          <label className={classNames('switch', showJson ? 'on' : '')}>
            <span className="label" style={{ marginRight: '5px' }}>Show JSON</span>
            <span className="wrapper">
              <input
                type="checkbox"
                className="ff-only"
                checked={showJson}
                onChange={toggleJsonView}
              />
            </span>
          </label>
          {toggleError && <span className={Styles.toggleError}>{toggleError}</span>}
        </div>

        {!showJson ? (
          <div className="listView">
            <div className={classNames(Styles.flexLayout)}>
              <div>
                <TextBox
                  type="text"
                  controlId="keyInput"
                  labelId="keyLabel"
                  label="Key"
                  placeholder="Type here"
                  value={key}
                  errorText={keyError}
                  required
                  onChange={onKeyChange}
                />
              </div>
              <div>
                <TextBox
                  type="text"
                  controlId="valueInput"
                  labelId="valueLabel"
                  label="Value"
                  placeholder="Type here"
                  value={value}
                  errorText={valueError}
                  required
                  onChange={onValueChange}
                />
              </div>

              <div className={Styles.AddBtn}>
                <button
                  className="btn btn-tertiary"
                  type="button"
                  onClick={handleAddOrUpdate}
                >
                  <span>{editingMode ? 'Update' : 'Add'}</span>
                </button>
              </div>
            </div>

            <p>
              <i className="icon mbc-icon alert circle"></i>
              Vault Key names starting with &apos;BUILD_&apos; can be used in the Codespace docker build file for handling secret values.
            </p>

            {keyValue?.keyValueList?.length > 0 && (
              <>
                <hr />
                <div className={Styles.searchBox}>
                  <div className="input-field-group">
                    <label className={classNames(Styles.inputLabel, 'input-label')}>
                      Search Key
                    </label>
                    <div className={Styles.searchPanel}>
                      <input
                        type="text"
                        className={classNames('input-field', Styles.searchField)}
                        id="searchTerm"
                        placeholder="Type here"
                        autoComplete="off"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                      />
                      <i
                        className={classNames(
                          'icon mbc-icon',
                          searchTerm?.length > 0 ? 'close circle' : 'search',
                          Styles.searchIcon
                        )}
                        onClick={() => {
                          if (searchTerm.length > 0) setSearchTerm('');
                        }}
                      />
                    </div>
                  </div>
                </div>
              </>
            )}

            {filteredList?.keyValueList?.length > 0 ? (
              <div className={Styles.allCodeSpace}>
                <div className={Styles.allcodeSpaceListviewContent}>
                  <table className={classNames('ul-table solutions', Styles.codeSpaceMargininone)}>
                    <thead>
                      <tr className={classNames('header-row', Styles.codeSpaceRow)}>
                        <th><label>Key</label></th>
                        <th><label>Value</label></th>
                        <th><label>Actions</label></th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredList.keyValueList.map((item) => (
                        <tr className="data-row" key={item.key}>
                          <td>{item.key}</td>
                          <td>
                            <span className={Styles.action}>
                              <button
                                onClick={() => {
                                  navigator.clipboard.writeText(item.value).then(() => {
                                    Notification.show('Copied to Clipboard');
                                  });
                                }}
                                className={Styles.actionBtn + ' btn btn-primary'}
                                type="button"
                              >
                                <i className={classNames('icon mbc-icon copy', Styles.copyIcon)} />
                              </button>
                              {item.visible ? (
                                <i
                                  className="icon mbc-icon visibility-hide"
                                  onClick={() => onMagnify(item.key)}
                                />
                              ) : (
                                <i
                                  className="icon mbc-icon visibility-show"
                                  onClick={() => onMagnify(item.key)}
                                />
                              )}
                            </span>{' '}
                            {item.visible ? item.value : '*******'}
                          </td>
                          <td>
                            <button
                              onClick={() => handleEdit(item.key, item.value)}
                              className={Styles.actionBtn + ' btn btn-primary'}
                              type="button"
                            >
                              <i className="icon mbc-icon edit" />
                            </button>
                            <button
                              onClick={() => {
                                setDeleteKey(item.key);
                                setDeleteValue(item.value);
                                setShowConfirmModal(true);
                              }}
                              className={Styles.actionBtn + ' btn btn-primary'}
                              type="button"
                            >
                              <i className="icon mbc-icon trash-outline" />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ) : keyValue?.keyValueList?.length > 0 && searchTerm?.length > 0 ? (
              <div className={Styles.noData}>No Data Found</div>
            ) 
            : (
              <div className={Styles.noData}>
                {/* {`You don't have any vault value for ${env} at this time. Please add a new one.`} */}
              </div>
            )}
          </div>
        ) : (

          <div className={Styles.jsonView}>
            <div className={Styles.copyBtn}>
              <button
                onClick={() => navigator.clipboard.writeText(jsonData)}
                className={Styles.actionBtn + ' btn btn-primary'}
              >
                Copy JSON &nbsp;<i className="icon mbc-icon copy" />
              </button>
            </div>
            <AceEditor
              mode="json"
              theme="solarized_dark"
              onChange={handleJsonChange}
              value={jsonData}
              fontSize={15}
              name="json_editor"
              editorProps={{ $blockScrolling: true }}
              width="100%"
              height="400px"
              showPrintMargin={false}
              setOptions={{ useWorker: false, showLineNumbers: true, tabSize: 1 }}
            />
            {jsonError && <div className={Styles.errorMsg}>{jsonError}</div>}
            <div className={Styles.saveBtn}>
              {isJsonTouched && (
                <button className="btn btn-primary" type="button" onClick={handleDiscard}>
                  Discard
                </button>
              )}
              <button className="btn btn-tertiary" type="button" onClick={handleSaveJson}>
                Save
              </button>
            </div>
          </div>
        )}
      </div>

      <ConfirmModal
        title=""
        acceptButtonTitle="Yes"
        cancelButtonTitle="Cancel"
        showAcceptButton
        showCancelButton
        show={showConfirmModal}
        content={deleteCodeSpaceContent}
        onCancel={() => setShowConfirmModal(false)}
        onAccept={handleDelete}
      />
    </>
  );
};

export default VaultManagement;
