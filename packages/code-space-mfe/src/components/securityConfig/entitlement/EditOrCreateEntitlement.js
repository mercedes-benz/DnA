import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './EditOrCreateEntitlement.scss';
import { HTTP_OPTIONS } from '../../../Utility/constants';
import SelectBox from 'dna-container/SelectBox';
import Tags from 'dna-container/Tags';
const classNames = cn.bind(Styles);

const EditOrCreateEntitlement = (props) => {
  const [entitlementName, setEntitlementName] = useState([]);
  const [entitlementNameError, setEntitlementNameError] = useState('');
  // const [beforeUpdateEntitlName, setbeforeUpdateEntitlName] = useState([]);
  const [entitlPath, setEntitlPath] = useState('');
  const [missingEntryEntlPath, setmissingEntryEntlPath] = useState('');
  const [missingEntryEntlMethod, setmissingEntryEntlMethod] = useState('');
  const [httpMethod, setHttpMethod] = useState('');
  const [currentEntitlement, setCurrentEntitlement] = useState({});

  const validateForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (entitlementName.length === 0) {
      setEntitlementNameError(errorMissingEntry);
      formValid = false;
    }
    if (entitlPath.length === 0) {
      setmissingEntryEntlPath(errorMissingEntry);
      formValid = false;
    } else if (entitlPath.length < 4 || !entitlPath.startsWith('/api/') || entitlPath === '/api/') {
      setmissingEntryEntlPath(
        'enter valid API path/pattern eg:/api/books or /api/books/{id} or /api/books?bookName={value}',
      );
      formValid = false;
    }
    if (httpMethod === '0' || httpMethod?.trim()?.length === 0) {
      setmissingEntryEntlMethod(errorMissingEntry);
      formValid = false;
    }
    if (missingEntryEntlMethod?.length || missingEntryEntlPath?.length || entitlementNameError?.length) {
      formValid = false;
    }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return formValid;
  };

  const onEntitlementSubmit = () => {
    if (validateForm()) {
      let currentEntitlementList = currentEntitlement || {};
      currentEntitlementList = {
        name: entitlementName,
        apiPattern: entitlPath,
        httpMethod: httpMethod,
      };
      setCurrentEntitlement({ ...currentEntitlementList });
      props.submitEntitlement({
        ...currentEntitlementList,
        name: entitlementName,
        // beforeUpdateEntitlName: beforeUpdateEntitlName,
      });
    }
  };

  const ontEntitlPatOnChange = (e) => {
    setEntitlPath(e.currentTarget.value);
    validateEntitlPath(e.currentTarget.value);
    setmissingEntryEntlPath('');
  };

  const validateEntitlPath = (value) => {
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

  const onChangeHttp = (e) => {
    setHttpMethod(e.currentTarget.value);
    setmissingEntryEntlMethod('');
  };

  useEffect(() => {
    if (props?.editEntitlementModal) {
      setEntitlementName(props.editEntitlementList.name);
      setEntitlPath(props.editEntitlementList.apiPattern);
      setHttpMethod(props.editEntitlementList.httpMethod);
      // setbeforeUpdateEntitlName(props.editEntitlementList.name);
    }
    setCurrentEntitlement(props.editEntitlementList);
  }, [props?.editEntitlementModal, props?.editEntitlementList?.name]);// eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    setTimeout(() => {
      SelectBox.defaultSetup();
    }, 200);
    setEntitlementNameError('');
  }, []);

  return (
    <React.Fragment>
      <div className={classNames(Styles.mainPanel)}>
        <div className={Styles.content}>
          <div className={Styles.CreateNewAirflowForm}>
            <div className={Styles.row}>
              <div className={classNames(Styles.inputGrp)}>
                <div className={classNames(Styles.inlineTextField)}>
                  <Tags
                    title={'Entitlement Names'}
                    max={100}
                    chips={entitlementName}
                    setTags={(selectedTags) => {
                      setEntitlementName(selectedTags);
                      selectedTags.length ? setEntitlementNameError('') : setEntitlementNameError('missing');
                    }}
                    tags={[]}
                    isMandatory={true}
                    showMissingEntryError={entitlementNameError !== ''}
                  />
                </div>
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
                      <label id="reportHttpLabel" htmlFor="entitlementhttpInput" className={classNames('input-label')}>
                        Http Method <sup>*</sup>
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
                          {HTTP_OPTIONS?.map((obj, index) => (
                            <option id={obj.name + obj.id} key={index} value={obj.name}>
                              {obj.name}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>
                </>
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