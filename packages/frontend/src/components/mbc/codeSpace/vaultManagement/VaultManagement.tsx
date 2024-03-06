import classNames from 'classnames';
import React, { useState, useEffect} from 'react';
import Styles from './VaultManagement.scss';
import TextBox from 'components/mbc/shared/textBox/TextBox';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { Notification } from '../../../../assets/modules/uilab/bundle/js/uilab.bundle';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';

export interface IVaultManagement {
  projectName: string;
  isStaging: boolean;
}

const deleteCodeSpaceContent = (
  <div>
    <h3>
      Are you sure you want to delete this vault value.
    </h3>
  </div>
);

const VaultManagement = (props: IVaultManagement) => {
  const [key, setKey] = useState('');
  const [keyError, setKeyError] = useState('');
  const [value, setValue] = useState('');
  const [valueError, setValueError] = useState('');
  const [keyValue, setKeyvalue] = useState({ keyValueList: [] });
  const [editingMode, setEditingMode] = useState(false);
  const [originalKey, setOriginalKey] = useState('');
  const [originalValue, setOriginalValue] = useState('');
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  //const [deleteConfirm, setDeleteConfirm] = useState(false);
  let formValid = true;
  const env = props.isStaging ? 'int' : 'prod';

  useEffect(() => {
    ProgressIndicator.show();
    CodeSpaceApiClient.read_secret(props.projectName, env)
      .then((response) => {
        ProgressIndicator.hide();
        const datalist = keyValue;
        Object.keys(response).forEach((item) => datalist.keyValueList.push({ key: item, value: response[item], visible: false}))
        setKeyvalue({...datalist});
      })
      .catch((err) => {
        ProgressIndicator.hide();
        if (err?.response?.data?.errors?.length > 0) {
          err?.response?.data?.errors.forEach((err: any) => {
            Notification.show(err?.message || 'Something went wrong.', 'alert');
          });
        } else {
          Notification.show(err?.message || 'Something went wrong.', 'alert');
        }
      });
  }, []);

  const onKeyChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    const keyVal = evnt.currentTarget.value;
    setKeyError('');
    setKey(keyVal);
    formValid = true;
  };

  const onValueChange = (evnt: React.FormEvent<HTMLInputElement>) => {
    setValueError('');
    const value = evnt.currentTarget.value;
    setValue(value);
    formValid = true;
  };

  const onAdd = () => {
    let currentList = keyValue ;
    const dataList = {};
    if(key.length === 0){
      setKeyError('Missing entry');
      formValid = false;
    }
    if(value.length === 0){
      setValueError('Missing entry');
      formValid = false;
    }
    keyValue.keyValueList.map((item: any) => {
      if(item.key === key){
        setKeyError('Duplicate key');
        formValid = false;
      }
    })
    if(formValid){
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
    currentList.keyValueList.forEach((item) => {dataList[item.key] = item.value;});
    CodeSpaceApiClient.update_secret(props.projectName, dataList , env);
    setKey('');
    setValue('');
  }
  };

  const onEdit = (key: string, value: string) => {
    setOriginalKey(key);
    setOriginalValue(value);
    setKey(key);
    setValue(value);
    setEditingMode(true);
  };

  const onUpdate = () => {
    const dataList = {};
    if(key.length === 0){
      setKeyError('Missing entry');
      formValid = false;
    }
    if(value.length === 0){
      setValueError('Missing entry');
      formValid = false;
    }
    keyValue.keyValueList.map((item: any) => {
      if(item.key === key && item.key!== originalKey){
        setKeyError('Duplicate key');
        formValid = false;
      }
    })
    if(formValid){
    const newList = keyValue.keyValueList.map((item: any) => {
      if (item.key === originalKey && item.value === originalValue) {
        return { ...item, key: key, value: value };
      }
      return item;
    });
    setKeyvalue({ keyValueList: newList });
    newList.forEach((item) => {dataList[item.key] = item.value;});
    CodeSpaceApiClient.update_secret(props.projectName, dataList , env);
    setOriginalKey('');
    setOriginalValue('');
    setKey('');
    setValue('');
    setEditingMode(false);
  }
  };

  const onDelete = (key: string, value: string) => {
    const dataList = {};
    const updatedList = keyValue;
    updatedList?.keyValueList?.splice(
      keyValue?.keyValueList?.findIndex((item: any) => item.key === key && item.value === value),
      1,
    );
    setKeyvalue({ ...updatedList });
    updatedList.keyValueList.forEach((item) => {dataList[item.key] = item.value;});
    CodeSpaceApiClient.update_secret(props.projectName, dataList , env);
  };

  const onMagnify = (key: string, value: string) => {
    const modifiedList = keyValue.keyValueList.map((item: any) => {
      if (item.key === key && item.value === value) {
        return { ...item, key: key, value: value, visible: item.visible ? false : true };
      }
      return item;
    });
    setKeyvalue({ keyValueList: modifiedList });
  };

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
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

        {keyValue.keyValueList?.length > 0 && (
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
                  {keyValue?.keyValueList?.map((item: any) => (
                    <tr className={classNames('data-row')} key={item.key}>
                      <td>{item.key}</td>
                      <td>
                        <span className={classNames(Styles.action)}>
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
                          onClick={() =>setShowConfirmModal(true)}
                          className={Styles.actionBtn + ' btn btn-primary'}
                          type="button"
                        >
                          <i className="icon mbc-icon trash-outline" />
                        </button>
                        <ConfirmModal
                          title={''}
                          acceptButtonTitle="Yes"
                          cancelButtonTitle="Cancel"
                          showAcceptButton={true}
                          showCancelButton={true}
                          show={showConfirmModal}
                          content={deleteCodeSpaceContent}
                          onCancel={()=>setShowConfirmModal(false)}
                          onAccept={()=> {onDelete(item.key, item.value); setShowConfirmModal(false);}}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
      
    </React.Fragment>
  );
};

export default VaultManagement;
