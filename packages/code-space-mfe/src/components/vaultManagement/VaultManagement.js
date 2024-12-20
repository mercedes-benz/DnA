import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './VaultManagement.scss';
import TextBox from 'dna-container/TextBox';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { Notification } from '../../common/modules/uilab/bundle/js/uilab.bundle';
import ConfirmModal from 'dna-container/ConfirmModal';
import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/theme-solarized_dark'; 

// export interface IVaultManagement {
//   projectName: string;
//   isStaging: boolean;
// }

const deleteCodeSpaceContent = (
  <div>
    <h3>Are you sure you want to delete this vault value.</h3>
  </div>
);

const VaultManagement = (props) => {
  const [key, setKey] = useState('');
  const [keyError, setKeyError] = useState('');
  const [value, setValue] = useState('');
  const [valueError, setValueError] = useState('');
  const [keyValue, setKeyvalue] = useState({ keyValueList: [] });
  const [editingMode, setEditingMode] = useState(false);
  const [originalKey, setOriginalKey] = useState('');
  const [originalValue, setOriginalValue] = useState('');
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [deleteKey, setDeleteKey] = useState('');
  const [deleteValue, setDeleteValue] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  //const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [filteredList, setFilteredList] = useState({ keyValueList: [] });
  const [showJson, setShowJson] = useState(false); 
  const [jsonData, setJsonData] = useState(JSON.stringify({ "": "" },null, 2));
  const [jsonError, setJsonError] = useState('');
  const [isJsonTouched, setIsJsonTouched] = useState(false);
  const [toggleError, setToggleError] = useState('');
  let formValid = true;
  const env = props.isStaging ? 'int' : 'prod';

  useEffect(() => {
    getValutValues();
  }, []);// eslint-disable-line react-hooks/exhaustive-deps

  const onKeyChange = (evnt) => {
    const keyVal = evnt.currentTarget.value;
    setKeyError('');
    setKey(keyVal);
    formValid = true;
  };

  const getValutValues = () =>{
    ProgressIndicator.show();
    CodeSpaceApiClient.read_secret(props.projectName, env)
      .then((response) => {
        ProgressIndicator.hide();
        const datalist = { keyValueList: [] };
        Object.keys(response.data).forEach((item) =>
          datalist.keyValueList.push({ key: item, value: response.data[item], visible: false }),
        );
        setKeyvalue({ ...datalist });
        setFilteredList({...datalist});
      })
      .catch((err) => {
        ProgressIndicator.hide();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
  }

  const onKeySearch = () => {
    let FilteredList = keyValue?.keyValueList;
    if (searchTerm?.length) {
      FilteredList = FilteredList.filter((val) => val.key.toLowerCase().includes(searchTerm.toLowerCase()));
    }
    setFilteredList({ keyValueList: FilteredList });
  }


  const handleJsonChange = (newValue) => {
    setIsJsonTouched(true);
    setJsonData(newValue);
    try {
        JSON.parse(newValue);
        setJsonError('');   
    } catch (e) {
        setJsonError(e.message); 
    }
  };

  useEffect(() => {
    onKeySearch();
  }, [searchTerm, keyValue]);// eslint-disable-line react-hooks/exhaustive-deps


  useEffect(() => {
    let transformedData = {};
    keyValue.keyValueList.forEach(item => {
      transformedData[item.key] = item.value;
    });
    setJsonData(JSON.stringify(transformedData, null,2));
  }, [keyValue])


  const onValueChange = (evnt) => {
    setValueError('');
    const value = evnt.currentTarget.value;
    setValue(value);
    formValid = true;
  };

  const onAdd = () => {
    let currentList = keyValue;
    const dataList = {};
    if (key.length === 0) {
      setKeyError('Missing entry');
      formValid = false;
    }
    if (value.length === 0) {
      setValueError('Missing entry');
      formValid = false;
    }
    keyValue.keyValueList.map((item) => {
      if (item.key === key) {
        setKeyError('Duplicate key');
        formValid = false;
      }
    });
    if (formValid) {
      if (currentList.keyValueList?.length === 0) {
        currentList = {
          keyValueList: [
            {
              key: key,
              value: value,
              visible: false,
            },
          ],
        };
      } else {
        if (currentList.keyValueList) {
          currentList.keyValueList.push({
            key: key,
            value: value,
            visible: false,
          });
        } else {
          currentList.keyValueList = [
            {
              key: key,
              value: value,
              visible: false,
            },
          ];
        }
      }
      setKeyvalue({ ...currentList });
      currentList.keyValueList.forEach((item) => {
        dataList[item.key] = item.value;
      });
      CodeSpaceApiClient.update_secret(props.projectName, dataList, env);
      setKey('');
      setValue('');
    }
  };

  const onEdit = (key, value) => {
    setOriginalKey(key);
    setOriginalValue(value);
    setKey(key);
    setValue(value);
    setEditingMode(true);
  };

  const onUpdate = () => {
    const dataList = {};
    if (key.length === 0) {
      setKeyError('Missing entry');
      formValid = false;
    }
    if (value.length === 0) {
      setValueError('Missing entry');
      formValid = false;
    }
    keyValue.keyValueList.map((item) => {
      if (item.key === key && item.key !== originalKey) {
        setKeyError('Duplicate key');
        formValid = false;
      }
    });
    if (formValid) {
      const newList = keyValue.keyValueList.map((item) => {
        if (item.key === originalKey && item.value === originalValue) {
          return { ...item, key: key, value: value };
        }
        return item;
      });
      setKeyvalue({ keyValueList: newList });
      newList.forEach((item) => {
        dataList[item.key] = item.value;
      });
      CodeSpaceApiClient.update_secret(props.projectName, dataList, env);
      setOriginalKey('');
      setOriginalValue('');
      setKey('');
      setValue('');
      setEditingMode(false);
    }
  };

  const onDelete = (key, value) => {
    const dataList = {};
    const updatedList = keyValue;
    updatedList?.keyValueList?.splice(
      keyValue?.keyValueList?.findIndex((item) => item.key === key && item.value === value),
      1,
    );
    setKeyvalue({ ...updatedList });
    updatedList.keyValueList.forEach((item) => {
      dataList[item.key] = item.value;
    });
    CodeSpaceApiClient.update_secret(props.projectName, dataList, env);
  };

  const onMagnify = (key, value) => {
    const modifiedList = keyValue.keyValueList.map((item) => {
      if (item.key === key && item.value === value) {
        return { ...item, key: key, value: value, visible: item.visible ? false : true };
      }
      return item;
    });
    setKeyvalue({ keyValueList: modifiedList });
  };

  const onSave = () =>{
    if(!jsonError?.length > 0 ) {
      CodeSpaceApiClient.update_secret(props.projectName, jsonData, env).then((response) => {
        Notification.show(response.data.message);
       })
       .catch((err) => {
         if (err?.response?.data?.errors?.length > 0) {
           err?.response?.data?.errors.forEach((err) => {
             Notification.show(err?.message || 'Something went wrong.', 'alert');
           });
         } else {
           Notification.show(err?.message || 'Something went wrong.', 'alert');
         }
       });
       setIsJsonTouched(false);
       getValutValues(); 
       setToggleError('');
    }
  }

  const onDiscard = () => {
    getValutValues(); 
    setIsJsonTouched(false); 
    setJsonError(''); 
    setToggleError('');
  }

  const handleToggle = () => {
    if(showJson && isJsonTouched){
      setToggleError("*Please save or discard your changes before you switch the view")
    }else{
      setShowJson((prev) => !prev);
    }
  }




  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.toggleSwitch}>
          <div>
          <label className={classNames("switch", showJson ? 'on' : '')}>
            <span className="label" style={{ marginRight: '5px' }}>
              Show JSON
            </span>
            <span className="wrapper">
              <input
                value={showJson}
                type="checkbox"
                className="ff-only"
                onChange={() => handleToggle()}
                checked={showJson}
                maxLength={63}
              />
            </span>
          </label>
          </div>
          {toggleError?.length > 0 &&<span className={classNames(Styles.toggleError)}>{toggleError}</span>}
        </div>
        {!showJson ? <div className='listView'>
          <div className={classNames(Styles.flexLayout)}>
            <div>
              <TextBox
                type="text"
                controlId={'keyInput'}
                labelId={'keyLabel'}
                label={'Key'}
                placeholder={'Type here'}
                value={key}
                errorText={keyError}
                required={true}
                //maxLength={39}
                onChange={onKeyChange}
              />
            </div>
            <div>
              <TextBox
                type="text"
                controlId={'valueInput'}
                labelId={'valueLabel'}
                label={'Value'}
                placeholder={'Type here'}
                value={value}
                errorText={valueError}
                //maxLength={}
                required={true}
                onChange={onValueChange}
              />
            </div>

            <div className={classNames(Styles.AddBtn)}>
              <button className={'btn btn-tertiary'} type="button" onClick={() => (editingMode ? onUpdate() : onAdd())}>
                <span>{editingMode ? 'Update' : 'Add'}</span>
              </button>
              {/* <span className={classNames('error-message', missingAddMesage.length ? '' : 'hide')}>
                    {missingAddMesage}
                  </span> */}
            </div>
          </div>
          <p><i className="icon mbc-icon alert circle"></i>Vault Key names starting with &apos;BUILD_&apos; can be used in the Codespace docker build file for handling secret values.</p>
          {keyValue?.keyValueList?.length > 0 && (
            <>
              <hr></hr>
              <div className={classNames(Styles.searchBox)}>
                <div className={classNames('input-field-group')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Search Key
                  </label>
                  <div className={classNames(Styles.searchPanel)}>
                    <input
                      type="text"
                      className={classNames('input-field', Styles.searchField)}
                      id="searchTerm"
                      placeholder="Type here"
                      autoComplete="off"
                      value={searchTerm}
                      onChange={(e) => { setSearchTerm(e.target.value) }}
                    />
                    <i
                      className={classNames('icon mbc-icon', searchTerm?.length > 0 ? 'close circle' : 'search', Styles.searchIcon)}
                      onClick={ searchTerm?.length > 0 ? setSearchTerm('') : null}
                    />
                  </div>
                </div>
              </div>
            </>
          )}
          {filteredList?.keyValueList?.length > 0 ? (
            <div className={classNames(Styles.allCodeSpace)}>
              <div className={classNames(Styles.allcodeSpaceListviewContent)}>
                <table className={classNames('ul-table solutions', Styles.codeSpaceMargininone)}>
                  <thead>
                    <tr className={classNames('header-row', Styles.codeSpaceRow)}>
                      <th>
                        <label>Key</label>
                      </th>
                      <th>
                        <label>Value</label>
                      </th>
                      <th>
                        <label>Actions</label>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredList?.keyValueList?.map((item) => (
                      <tr className={classNames('data-row')} key={item.key}>
                        <td>{item.key}</td>
                        <td>
                          <span className={classNames(Styles.action)}>
                            {' '}
                            <button
                              onClick={() => {
                                navigator.clipboard.writeText(item.value).then(() => {
                                  Notification.show('Copied to `Clipboard`');
                                });
                              }}
                              className={Styles.actionBtn + ' btn btn-primary'}
                              type="button"
                            >
                              <i className={classNames('icon mbc-icon copy', Styles.copyIcon)} />
                            </button>
                            {' '}
                            {item.visible ? (
                              <i
                                className="icon mbc-icon visibility-hide"
                                onClick={() => onMagnify(item.key, item.value)}
                              />
                            ) : (
                              <i
                                className="icon mbc-icon visibility-show"
                                onClick={() => onMagnify(item.key, item.value)}
                              />
                            )}{' '}
                          </span>{' '}
                          {item.visible ? item.value : '*******'}
                        </td>
                        <td>
                          <button
                            onClick={() => onEdit(item.key, item.value)}
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
          ) : (keyValue?.keyValueList?.length > 0 && searchTerm?.length) ?
            <div className={classNames(Styles.noData)}>No Data Found</div>
            : <div className={classNames(Styles.noData)}>{`You don't have any vault value for ${env} at this time. Please add a new one.`}</div>
          }

        </div> : <div className={classNames(Styles.jsonView)}>
            <div className={classNames(Styles.copyBtn)}>
              <button
                onClick={() => {
                  navigator.clipboard.writeText(jsonData).then(() => {
                    Notification.show('Copied to `Clipboard`');
                  });
                }}
                className={Styles.actionBtn + ' btn btn-primary'}
                type="button"
              > Copy JSON &nbsp;
                <i className={classNames('icon mbc-icon copy', Styles.copyIcon)} />
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
            showPrintMargin= {false}
            setOptions={{
              useWorker: false,
              showLineNumbers: true,
              tabSize: 1,
            }}
          />
          {jsonError?.length > 0 && (
            <div className={classNames(Styles.errorMsg)}>
               {jsonError}
            </div>
          )}
          <p className={classNames(Styles.info)}><i className="icon mbc-icon alert circle"></i>Vault Key names starting with &apos;BUILD_&apos; can be used in the Codespace docker build file for handling secret values.</p>
           <div className={classNames(Styles.saveBtn)}>
              {isJsonTouched && <button className={'btn btn-primary'} type="button" onClick={() => {onDiscard()}}>
                <span>Discard</span>
              </button>}
              <button className={'btn btn-tertiary'} type="button" onClick={() => onSave()}>
                <span>Save</span>
              </button>
            </div>
        </div>}

        
      </div>
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="Cancel"
        showAcceptButton={true}
        showCancelButton={true}
        show={showConfirmModal}
        content={deleteCodeSpaceContent}
        onCancel={() => {
          setDeleteKey('');
          setDeleteValue('');
          setShowConfirmModal(false);
        }}
        onAccept={() => {
          onDelete(deleteKey, deleteValue);
          setShowConfirmModal(false);
        }}
      />
    </React.Fragment>
  );
};

export default VaultManagement;
