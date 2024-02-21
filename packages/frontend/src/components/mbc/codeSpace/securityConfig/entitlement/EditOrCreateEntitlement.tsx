import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './EditOrCreateEntitlement.scss';
import { HTTP_OPTIONS } from 'globals/constants';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { Envs } from 'globals/Envs';
const classNames = cn.bind(Styles);

const EditOrCreateEntitlement = (props: any) => {
  const [EntitlId, setEntitlId] = useState<string>('');
  const [EntitlName, setEntitleName] = useState<string>(Envs.CODESPACE_SECURITY_APP_ID + '.' + props.projectName);
  const [beforeUpdateEntitlName, setbeforeUpdateEntitlName] = useState<string>('');
  const [missingEntryEntitlName, setmissingEntryEntitlName] = useState<string>('');
  const [entitlPath, setEntitlPath] = useState<string>('');
  const [missingEntryEntlPath, setmissingEntryEntlPath] = useState<string>('');
  const [missingEntryEntlMethod, setmissingEntryEntlMethod] = useState<string>('');
  const [missingAddMesage, setMissingAddMesage] = useState<string>('');
  const [httpMethod, setHttpMethod] = useState<string>('');
  const [currentEntitlement, setCurrentEntitlement] = useState<any>({ apiList: [] });
  
  const onEntitlementNameOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const value = e.currentTarget.value;
    validateEntitlementName(value);
    setEntitlId(value);
    setEntitleName(Envs.CODESPACE_SECURITY_APP_ID + '.' + props.projectName + '_' + value);
    setmissingEntryEntitlName('');
  };

  const validateEntitlementName = (value: any) => {
    const pattern = /^[a-zA-Z0-9][a-zA-Z0-9_-]*$/;
    const isValid = pattern.test(value);
    const spclCharValidation = /[^a-zA-Z0-9_-]/;
    if (!isValid) {
      setTimeout(() => {
        if (value.startsWith('-') || value.startsWith('_')) {
          setmissingEntryEntitlName('Entitlement Id cannot start with special character');
        } else if (value.includes(' ')) {
          setmissingEntryEntitlName('Entitlement Id cannot have whitespaces');
        } else if (spclCharValidation.test(value)) {
          setmissingEntryEntitlName('Entitlement Id cannot have special characters other than - and _');
        }
      });
    } else {
      setmissingEntryEntitlName('');
    }
  };

  const validateForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (EntitlId.length === 0) {
      setmissingEntryEntitlName(errorMissingEntry);
      formValid = false;
    }
    if (EntitlId.endsWith('_') || EntitlId.endsWith('-')) {
      setmissingEntryEntitlName('Entitlement Id cannot end with _ or -');
      formValid = false;
    }
    if (props.isProtectedByDna && entitlPath.length === 0) {
      setmissingEntryEntlPath(errorMissingEntry);
      formValid = false;
    }
    if (props.isProtectedByDna && (entitlPath.length < 4 || !entitlPath.includes('/api/') || entitlPath === '/api/')) {
      setmissingEntryEntlPath(
        'enter valid API path/pattern eg:/api/books or /api/books/{id} or /api/books?bookName={value}}',
      );
      formValid = false;
    }
    if (props.isProtectedByDna && (httpMethod === '0' || httpMethod?.trim()?.length === 0)) {
      setmissingEntryEntlMethod(errorMissingEntry);
      formValid = false;
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return formValid;
  };

  const onEntitlementSubmit = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (
      (props.isProtectedByDna && (currentEntitlement?.apiList?.length === 0 || !currentEntitlement)) ||
      entitlPath.length > 0 ||
      httpMethod !== '0'
    ) {
      formValid = validateForm();
      if ((httpMethod !== '0' || httpMethod?.trim()?.length > 0) && entitlPath?.length !== 0) {
        setMissingAddMesage(' <- Click on Add to add API Path/Pattern and Http Method.');
        formValid = false;
      }
    } else {
      if (EntitlId.length === 0) {
        setmissingEntryEntitlName(errorMissingEntry);
        formValid = false;
      }
    }
    if (formValid) {
      props.submitEntitlement({
        ...currentEntitlement,
        beforeUpdateEntitlName: beforeUpdateEntitlName,
        name: props.projectName + '_' + EntitlId,
      });
    }
  };
  const onExistingEntitlementSubmit = () => {
    setMissingAddMesage('');
    if (validateForm()) {
      let currentEntitlementList = currentEntitlement || { apiList: [] };
      if (currentEntitlementList?.apiList?.length === 0) {
        currentEntitlementList = {
          name: EntitlId,
          apiList: [
            {
              apiPattern: entitlPath,
              httpMethod: httpMethod,
            },
          ],
        };
      } else {
        if (currentEntitlementList.apiList) {
          currentEntitlementList.apiList.push({
            apiPattern: entitlPath,
            httpMethod: httpMethod,
          });
        } else {
          currentEntitlementList.apiList = [
            {
              apiPattern: entitlPath,
              httpMethod: httpMethod,
            },
          ];
        }
      }

      setCurrentEntitlement({ ...currentEntitlementList });
      setHttpMethod('');
      setEntitlPath('');
      setTimeout(() => {
        SelectBox.defaultSetup();
      }, 100);
    }
  };

  const ontEntitlPatOnChange = (e: any) => {
    setEntitlPath(e.currentTarget.value);
    validateEntitlPath(e.currentTarget.value);
    setmissingEntryEntlPath('');
  };

  const validateEntitlPath = (value: any) => {
    const length = value.length;
    setTimeout(() => {
      if (length >= 4 && !value.includes('/api')) {
        setmissingEntryEntlPath('API Path Should Start With /api');
      } else if (value[length - 2] === '=' && !(value[value.length - 1] === '{')) {
        setmissingEntryEntlPath('query params value should be enclosed in {}, eg: /api/books?bookName={value}');
      } else if (value.includes('{') && !value.includes('}')) {
        setmissingEntryEntlPath('query params value should be enclosed in {}, eg: /api/books?bookName={value}');
      }
    }, 10);
  };

  const deleteApiElement = (apiPattern: string, httpMethod: string) => {
    const updatedEntitlementList = currentEntitlement;
    updatedEntitlementList?.apiList?.splice(
      currentEntitlement?.apiList?.findIndex(
        (item: any) => item.apiPattern === apiPattern && item.httpMethod === httpMethod,
      ),
      1,
    );
    setCurrentEntitlement({ ...updatedEntitlementList });
  };

  const onChangeHttp = (e: any) => {
    setHttpMethod(e.currentTarget.value);
    setmissingEntryEntlMethod('');
  };

  useEffect(() => {
    if (props?.editEntitlementModal) {
      setEntitlId(props.editEntitlementList.name);
      setEntitleName(Envs.CODESPACE_SECURITY_APP_ID + '.' + props.editEntitlementList.name)
      setbeforeUpdateEntitlName(props.editEntitlementList.name);
    }
    setCurrentEntitlement(props.editEntitlementList);
  }, [props?.editEntitlementModal, props?.editEntitlementList?.name]);

  useEffect(() => {
    setmissingEntryEntitlName(props.entitlementNameErrorMessage);
  }, [props?.entitlementNameErrorMessage]);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, []);

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.content}>
          <div className={Styles.CreateNewAirflowForm}>
            <div className={Styles.row}>
              <div className={classNames(Styles.inputGrp)}>
                <div className={classNames(Styles.inlineTextField)}>
                  <div
                    className={classNames(
                      ' input-field-group include-error ',
                      missingEntryEntitlName?.length ? 'error' : '',
                    )}
                  >
                    <label id="PrjName" htmlFor="PrjId" className="input-label">
                      Entitlement Id<sup>*</sup>
                    </label>
                    <input
                      type="text"
                      className="input-field"
                      required={true}
                      id="PrjId"
                      maxLength={64}
                      placeholder="Type here"
                      autoComplete="off"
                      onChange={(e) => onEntitlementNameOnChange(e)}
                      value={EntitlId}
                    />
                    <span className={classNames('error-message', missingEntryEntitlName?.length ? '' : 'hide')}>
                      {missingEntryEntitlName}
                    </span>
                  </div>
                  <div className={classNames(' input-field-group disabled ')}>
                    <label id="PrjName" htmlFor="PrjName" className="input-label">
                      Entitlement Name
                    </label>
                    <input type="text" className="input-field" disabled={true} id="PrjName" value={EntitlName} />
                  </div>
                  <span className={classNames('error-message', missingEntryEntitlName?.length ? '' : 'hide')}> </span>
                </div>
                {props.isProtectedByDna && (
                  <>
                    <div className={classNames(Styles.fieldWrapper)}>
                      <div
                        className={classNames(
                          Styles.inputGrp + ' input-field-group include-error ',
                          missingEntryEntlPath.length ? 'error' : '',
                        )}
                      >
                        <label id="descriptionLabel" htmlFor="descriptionLabel" className="input-label">
                          API Path/Pattern<sup>*</sup>
                        </label>
                        <input
                          type="text"
                          className="input-field"
                          required={true}
                          id="descriptionLabel"
                          maxLength={200}
                          placeholder="Type here"
                          autoComplete="off"
                          onChange={ontEntitlPatOnChange}
                          value={entitlPath}
                        />
                        <span className={classNames('error-message', missingEntryEntlPath?.length ? '' : 'hide')}>
                          {missingEntryEntlPath}
                        </span>
                      </div>
                    </div>
                    <div className={classNames(Styles.fieldWrapper)}>
                      <div
                        className={classNames(
                          'input-field-group include-error',
                          missingEntryEntlMethod.length ? 'error' : '',
                        )}
                      >
                        <label
                          id="reportHttpLabel"
                          htmlFor="entitlementhttpInput"
                          className={classNames('input-label')}
                        >
                          Http Method <sup>{props.isProtectedByDna ? '*' : ''}</sup>
                        </label>
                        <div className="custom-select">
                          <select
                            id="entitlementhttpInput"
                            required={true}
                            required-error={'*Missing entry'}
                            onChange={onChangeHttp}
                            value={httpMethod}
                          >
                            <option id="entitlemenHttpOption" value={0}>
                              Choose
                            </option>
                            {HTTP_OPTIONS?.map((obj: any, index: number) => (
                              <option id={obj.name + obj.id} key={index} value={obj.name}>
                                {obj.name}
                              </option>
                            ))}
                          </select>
                        </div>
                      </div>
                    </div>
                    <div className={Styles.AddBtn}>
                      <button
                        className={'btn btn-tertiary'}
                        type="button"
                        onClick={() => onExistingEntitlementSubmit()}
                      >
                        <span>Add</span>
                      </button>
                      <span className={classNames('error-message', missingAddMesage.length ? '' : 'hide')}>
                        {missingAddMesage}
                      </span>
                    </div>
                    {currentEntitlement?.apiList?.length > 0 && (
                      <div className={Styles.entTileContainer}>
                        <div className={`${Styles.entTile} ${Styles.tableHeader}`}>
                          <div className={Styles.dagTitleCol}>API Path/Pattern</div>
                          <div className={Styles.dagTitleCol}> Http Method</div>
                          <div className={Styles.dagTitleCol}>Actions</div>
                        </div>
                        {currentEntitlement?.apiList?.map((item: any, index: number) => (
                          <div key={index} className={Styles.entTile}>
                            <div className={Styles.dagTitleCol}>{item.apiPattern}</div>
                            <div className={Styles.dagMethodCol}>{item.httpMethod}</div>
                            <div className={Styles.dagTitleCol}>
                              <div className={Styles.actionBtnGrp}>
                                <React.Fragment>
                                  <button
                                    onClick={() => deleteApiElement(item.apiPattern, item.httpMethod)}
                                    className={Styles.actionBtn + ' btn btn-primary'}
                                    type="button"
                                  >
                                    <i className="icon mbc-icon trash-outline" />
                                  </button>
                                </React.Fragment>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
        <div className={Styles.createBtn}>
          <button className={'btn btn-tertiary'} type="button" onClick={onEntitlementSubmit}>
            <span>{'Submit Changes'}</span>
          </button>
        </div>
      </div>
    </React.Fragment>
  );
};
export default EditOrCreateEntitlement;
