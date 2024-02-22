import React, { useState, useEffect } from 'react';
import Styles from './EntitlementSubList.scss';
// @ts-ignore
import ProgressIndicator from '../../../../../assets/modules/uilab/js/src/progress-indicator';
import Modal from 'components/formElements/modal/Modal';

// @ts-ignore
import Notification from '../../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ExpansionPanel from '../../../../../assets/modules/uilab/js/src/expansion-panel';
// @ts-ignore
import Tooltip from '../../../../../assets/modules/uilab/js/src/tooltip';
import classNames from 'classnames';
import EditOrCreateEntitlement from './EditOrCreateEntitlement';
import { CODE_SPACE_STATUS, HTTP_OPTIONS } from 'globals/constants';
import SelectBox from 'components/formElements/SelectBox/SelectBox';
import { Envs } from 'globals/Envs';

export interface IEntitlementSublistProps {
  listOfProject: any[];
  getRefreshedDagPermission: (name: string, apiPathMethodIndex: number) => void;
  getProjectSorted: (prjIdSortVal: any) => void;
  isProtectedByDna: boolean;
  entitelmentListResponse: any;
  updatedFinalEntitlementList: any;
  status: string;
  readOnlyMode: boolean;
  isCodeSpaceAdminPage: boolean;
  projectName?: string;
}

const EntitlementSubList = (props: IEntitlementSublistProps) => {
  const [collEditEntitlementModel, setCollEditEntitlementModel] = useState<boolean>(false);
  const [editPathMethodModal, setEditPathMethodModal] = useState<boolean>(false);
  const [currentSortOrder, setCurrentSortOrder] = useState<string>('desc');
  const [nextSortOrder, setNextSortOrder] = useState<string>('asc');
  const [currentColumnToSort, setCurrentColumnToSort] = useState<string>('name');
  const [editEntitlementList, setEditEntitlementList] = useState<any>([]);
  const [allEntitlementList, setAllEntitlementList] = useState<any>([]);
  const [entitlemenPath, setEntitlemenPath] = useState<string>('');
  const [httpMethod, setHttpMethod] = useState<string>('');
  const [entitlementPathErrorMessage, setEntitlementPathErrorMessage] = useState<string>('');
  const [entitlementHttpMethodErrorMessage, setEntitlementHttpMethodErrorMessage] = useState<string>('');
  const [currentEntitlementName, setCurrentEntitlementName] = useState('');
  const [originalApiPattern, setOriginalApiPattern] = useState('');
  const [originalHttpMethod, setOriginalHttpMethod] = useState('');
  const [entitelmentListResponse, setEntitelmentListResponse] = useState([]);

  const onEditEntitlement = (entitlement: any) => {
    setCollEditEntitlementModel(true);
    setEditEntitlementList(entitlement);
  };

  const onDeleteEntitlement = (entitlement: any) => {
    // Remove the entitlement from the list of allEntitlementList
    const updatedList = allEntitlementList.filter((item: any) => item.name !== entitlement.name);
    const updatedEntitelmentListResponse = entitelmentListResponse.filter(
      (item: any) => item.name !== entitlement.name,
    );
    setAllEntitlementList([...updatedList]);
    setEntitelmentListResponse([...updatedEntitelmentListResponse]);
    props.updatedFinalEntitlementList([...updatedEntitelmentListResponse]);
    props.getProjectSorted(updatedList);
  };

  const editPathMethod = (name: any, apiPattern: any, httpMethod: any, index: any) => {
    setEditPathMethodModal(true);
    setEntitlemenPath(apiPattern);
    setHttpMethod(httpMethod);

    // Set the original values
    setCurrentEntitlementName(name);
    setOriginalApiPattern(apiPattern);
    setOriginalHttpMethod(httpMethod);
  };

  const onChangeHttp = (e: any) => {
    setHttpMethod(e.currentTarget.value);
    setEntitlementHttpMethodErrorMessage('');
  };

  const ontEntitlPatOnChange = (e: any) => {
    setEntitlemenPath(e.currentTarget.value);
    setEntitlementPathErrorMessage('');
  };
  const validateEntitlPath = (value: any) => {
    const length = value.length;
      if (length >= 4 && !value.includes('/api')) {
        setEntitlementPathErrorMessage('API Path Should Start With /api');
      } else if (value[length - 2] === '=' && !(value[value.length - 1] === '{')) {
        setEntitlementPathErrorMessage('query params value should be enclosed in {}, eg: /api/books?bookName={value}');
      } else if (value.includes('{') && !value.includes('}')) {
        setEntitlementPathErrorMessage('query params value should be enclosed in {}, eg: /api/books?bookName={value}');
      }
  };
  useEffect(()=>{
    validateEntitlPath(entitlemenPath);
  },[entitlemenPath])

  const deletePathMethod = (name: any, apiPattern: any, httpMethod: any, index: any) => {
    // Iterate through allEntitlementList and update the apiList for the matched entitlement
    const updatedList = allEntitlementList.map((entitlement: any, entitlementIndex: any) => {
      if (entitlement.name === name) {
        // Filter out the api item that matches apiPattern and httpMethod
        const updatedApiList = entitlement.apiList.filter(
          (apiItem: any) => !(apiItem.apiPattern === apiPattern && apiItem.httpMethod === httpMethod),
        );

        // Return the updated entitlement with the modified apiList
        return { ...entitlement, apiList: updatedApiList };
      }
      return entitlement;
    });

    const updatedEntitelmentListResponse = entitelmentListResponse.map((entitlement: any, entitlementIndex: any) => {
      if (entitlement.name === name) {
        // Filter out the api item that matches apiPattern and httpMethod
        const updatedApiList = entitlement.apiList.filter(
          (apiItem: any) => !(apiItem.apiPattern === apiPattern && apiItem.httpMethod === httpMethod),
        );

        // Return the updated entitlement with the modified apiList
        return { ...entitlement, apiList: updatedApiList };
      }
      return entitlement;
    });

    // Update the allEntitlementList state
    setAllEntitlementList([...updatedList]);

    setEntitelmentListResponse([...updatedEntitelmentListResponse]);
    props.updatedFinalEntitlementList([...updatedEntitelmentListResponse]);
    // Optionally, propagate changes upwards if necessary
    props.getProjectSorted(updatedList);
  };

  const collPermissionModelClose = () => {
    setCollEditEntitlementModel(false);
  };

  const editPathModelClose = () => {
    setEditPathMethodModal(false);
    setEntitlementPathErrorMessage('');
  };

  const updateEntitlement = (editedEntitlement: any) => {
    collPermissionModelClose();
    const updatedList = allEntitlementList.map((entitlement: any) => {
      if (
        entitlement.name === editedEntitlement.name ||
        entitlement.name === editedEntitlement?.beforeUpdateEntitlName
      ) {
        return editedEntitlement;
      }
      return entitlement;
    });

    const updatedEntitelmentListResponse = entitelmentListResponse.map((entitlement: any) => {
      if (
        entitlement.name === editedEntitlement.name ||
        entitlement.name === editedEntitlement?.beforeUpdateEntitlName
      ) {
        return editedEntitlement;
      }
      return entitlement;
    });
    setEntitelmentListResponse([...updatedEntitelmentListResponse]);
    props.updatedFinalEntitlementList([...updatedEntitelmentListResponse]);
    setAllEntitlementList([...updatedList]);
    props.getProjectSorted([...updatedList]);
  };

  const updatePathHttpMethod = () => {
    if (!entitlemenPath || !httpMethod || httpMethod === 'Choose' || httpMethod === '0') {
      setEntitlementPathErrorMessage(!entitlemenPath ? 'Please enter a valid API Path/Pattern' : '');
      setEntitlementHttpMethodErrorMessage(
        !httpMethod || httpMethod === 'Choose' || httpMethod === '0' ? 'Please select an HTTP Method' : '',
      );
      return;
    }
    if (props.isProtectedByDna && (entitlemenPath.length < 4 || !entitlemenPath.includes('/api/') || entitlemenPath === '/api/')) {
      setEntitlementPathErrorMessage(
        'enter valid API path/pattern eg:/api/books or /api/books/{id} or /api/books?bookName={value}',
      );
      return;
    }

    const updatedList = allEntitlementList.map((entitlement: any) => {
      if (entitlement.name === currentEntitlementName) {
        return {
          ...entitlement,
          apiList: entitlement.apiList.map((apiItem: any) => {
            if (apiItem.apiPattern === originalApiPattern && apiItem.httpMethod === originalHttpMethod) {
              return { ...apiItem, apiPattern: entitlemenPath, httpMethod: httpMethod };
            }
            return apiItem;
          }),
        };
      }
      return entitlement;
    });

    const updatedEntitelmentListResponse = entitelmentListResponse.map((entitlement: any) => {
      if (entitlement.name === currentEntitlementName) {
        return {
          ...entitlement,
          apiList: entitlement.apiList.map((apiItem: any) => {
            if (apiItem.apiPattern === originalApiPattern && apiItem.httpMethod === originalHttpMethod) {
              return { ...apiItem, apiPattern: entitlemenPath, httpMethod: httpMethod };
            }
            return apiItem;
          }),
        };
      }
      return entitlement;
    });

    setAllEntitlementList([...updatedList]);
    setEditPathMethodModal(false);
    setEntitelmentListResponse([...updatedEntitelmentListResponse]);
    props.updatedFinalEntitlementList([...updatedEntitelmentListResponse]);
  };

  // Sorting table data
  const sortByColumn = (columnName: string, sortOrder: string) => {
    return () => {
      let sortedArray: any[] = [];
      sortedArray = allEntitlementList?.sort((a: any, b: any) => {
        const nameA = a[columnName].toString() ? a[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
        const nameB = b[columnName].toString() ? b[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
        if (nameA < nameB) {
          return sortOrder === 'asc' ? -1 : 1;
        } else if (nameA > nameB) {
          return sortOrder === 'asc' ? 1 : -1;
        }
        return 0;
      });
      setNextSortOrder(sortOrder == 'asc' ? 'desc' : 'asc');
      setCurrentSortOrder(sortOrder);
      setCurrentColumnToSort(columnName);
      props.getProjectSorted(sortedArray);
    };
  };

  useEffect(() => {
    Tooltip.defaultSetup();
    ExpansionPanel.defaultSetup();
  }, []);

  useEffect(() => {
    SelectBox.defaultSetup();
  }, [editPathMethodModal]);

  useEffect(() => {
    setAllEntitlementList(props.listOfProject);
  }, [props.listOfProject]);

  useEffect(() => {
    setEntitelmentListResponse(props.entitelmentListResponse);
  }, [props?.entitelmentListResponse]);

  return (
    <React.Fragment>
      {entitelmentListResponse.length > 0 ? (
        <div className={classNames('expanstion-table', Styles.airflowSubscriptionList)}>
          <div className={Styles.entGrp}>
            <div className={Styles.entGrpList}>
              <div className={Styles.entGrpListItem}>
                <div className={Styles.entCaption}>
                  <div className={Styles.entTile}>
                    <div className={Styles.dagTitleCol}>
                      <label
                        className={'sortable-column-header ' + (currentColumnToSort === 'name' ? currentSortOrder : '')}
                        onClick={sortByColumn('name', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Name
                      </label>
                    </div>
                    <div className={Styles.dagTitleCol + (props.readOnlyMode ? ' hidden' : '')}>Action</div>
                  </div>
                </div>
                {!props.isProtectedByDna &&
                  allEntitlementList?.map((item: any, index: number) => {
                    return (
                      <div
                        key={index}
                        className={'expansion-panel-group airflowexpansionPanel ' + Styles.dagGrpListItemPanel}
                      >
                        <div className={classNames('expansion-panel')}>
                          <div className={classNames(Styles.entTile)}>
                            <div className={Styles.dagTitleCol}>{!props.isCodeSpaceAdminPage? Envs.CODESPACE_SECURITY_APP_ID +'.'+item.name : item.name}</div>
                            <div className={Styles.dagTitleCol}>
                              <div className={Styles.prjListAction}>
                                <div className={Styles.actionBtnGrp}>
                                  <button
                                    onClick={() => onEditEntitlement(item)}
                                    className={
                                      Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')
                                    }
                                    type="button"
                                    title={
                                      !CODE_SPACE_STATUS.includes(props?.status)
                                        ? 'Once the config is in published state, can edit Entitlement.'
                                        : ''
                                    }
                                    disabled={!CODE_SPACE_STATUS.includes(props?.status)}
                                  >
                                    <i className="icon mbc-icon edit" />
                                  </button>{' '}
                                </div>
                                <div className={Styles.actionBtnGrp}>
                                  <button
                                    onClick={() => onDeleteEntitlement(item)}
                                    className={
                                      Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')
                                    }
                                    type="button"
                                    title={
                                      !CODE_SPACE_STATUS.includes(props?.status)
                                        ? 'Once the config is in published state, can delete Entitlement.'
                                        : ''
                                    }
                                    disabled={!CODE_SPACE_STATUS.includes(props?.status)}
                                  >
                                    <i className="icon mbc-icon trash-outline" />
                                  </button>{' '}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                {props.isProtectedByDna &&
                  allEntitlementList?.map((item: any, index: number) => {
                    return (
                      <div
                        key={index + item.id}
                        className={'expansion-panel-group airflowexpansionPanel ' + Styles.dagGrpListItemPanel}
                      >
                        <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                          <span className="animation-wrapper"></span>
                          <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                          <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                            <div className={classNames(Styles.entTile)}>
                              <div className={Styles.dagTitleCol}>{!props.isCodeSpaceAdminPage?( Envs.CODESPACE_SECURITY_APP_ID+'.'+ item.name ): item.name}</div>
                              <div className={Styles.dagTitleCol}></div>
                            </div>
                            <i tooltip-data="Expand" className="icon down-up-flip"></i>
                          </label>
                          <div className="expansion-panel-content">
                            <div className={Styles.dagCollContent}>
                              <div className={Styles.projectList}>
                                <div className={Styles.entTile + ' ' + Styles.entTileCption}>
                                  <div className={Styles.dagTitleCol}>API Path/Pattern</div>
                                  <div className={Styles.dagTitleCol}>Http Method</div>
                                  <div className={Styles.dagTitleCol}>Action</div>
                                </div>
                                <div className={Styles.projectDagList}>
                                  {item.apiList && item.apiList.length > 0 ? (
                                    item.apiList?.map((apliItem: any, apiPathMethodIndex: number) => {
                                      return (
                                        <div key={apiPathMethodIndex} className={Styles.entTile}>
                                          <div className={Styles.dagTitleCol}>{apliItem.apiPattern}</div>
                                          <div className={Styles.dagTitleCol}>{apliItem.httpMethod}</div>
                                          <div className={Styles.dagTitleCol}>
                                            <div className={Styles.actionBtnGrp}>
                                              <React.Fragment>
                                                <button
                                                  onClick={() =>
                                                    editPathMethod(
                                                      item.name,
                                                      apliItem.apiPattern,
                                                      apliItem.httpMethod,
                                                      apiPathMethodIndex,
                                                    )
                                                  }
                                                  className={
                                                    Styles.actionBtn +
                                                    ' btn btn-primary' +
                                                    (props.readOnlyMode ? ' hidden' : '')
                                                  }
                                                  type="button"
                                                  title={
                                                    !CODE_SPACE_STATUS.includes(props?.status)
                                                      ? 'Once the config is in published state, can edit API Path/Pattern and Method.'
                                                      : ''
                                                  }
                                                  disabled={!CODE_SPACE_STATUS.includes(props?.status)}
                                                >
                                                  <i className="icon mbc-icon edit" />
                                                  <span>Edit API Path/Pattern and Method </span>
                                                </button>
                                                &nbsp; &nbsp;
                                                <button
                                                  onClick={() =>
                                                    deletePathMethod(
                                                      item.name,
                                                      apliItem.apiPattern,
                                                      apliItem.httpMethod,
                                                      apiPathMethodIndex,
                                                    )
                                                  }
                                                  className={
                                                    Styles.actionBtn +
                                                    ' btn btn-primary' +
                                                    (props.readOnlyMode ? ' hidden' : '')
                                                  }
                                                  type="button"
                                                  title={
                                                    !CODE_SPACE_STATUS.includes(props?.status)
                                                      ? 'Once the config is in published state, can delete API Path/Pattern and Method.'
                                                      : ''
                                                  }
                                                  disabled={!CODE_SPACE_STATUS.includes(props?.status)}
                                                >
                                                  <i className="icon mbc-icon trash-outline" />
                                                </button>
                                              </React.Fragment>
                                            </div>
                                          </div>
                                        </div>
                                      );
                                    })
                                  ) : (
                                    <div className={Styles.centeredText}>No records found</div>
                                  )}
                                </div>
                              </div>
                              <div className={Styles.prjListAction}>
                                <div className={Styles.actionBtnGrp}>
                                  <button
                                    onClick={() => onEditEntitlement(item)}
                                    className={
                                      Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')
                                    }
                                    style={{ backgroundColor: 'transparent' }}
                                    type="button"
                                    title={
                                      !CODE_SPACE_STATUS.includes(props?.status)
                                        ? 'Once the config is in published state, can edit Entitlement.'
                                        : ''
                                    }
                                    disabled={!CODE_SPACE_STATUS.includes(props?.status)}
                                  >
                                    <i className="icon mbc-icon edit" />
                                    <span>Edit Entitlement</span>
                                  </button>{' '}
                                </div>
                                <div className={Styles.actionBtnGrp}>
                                  <button
                                    onClick={() => onDeleteEntitlement(item)}
                                    className={
                                      Styles.actionBtn + ' btn btn-primary' + (props.readOnlyMode ? ' hidden' : '')
                                    }
                                    style={{ backgroundColor: 'transparent' }}
                                    type="button"
                                    title={
                                      !CODE_SPACE_STATUS.includes(props?.status)
                                        ? 'Once the config is in published state, can delete Entitlement.'
                                        : ''
                                    }
                                    disabled={!CODE_SPACE_STATUS.includes(props?.status)}
                                  >
                                    <i className="icon mbc-icon trash-outline" />
                                    <span>Delete Entitlement</span>
                                  </button>{' '}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className={Styles.centeredText}>No Entitlements found.</div>
      )}
      {collEditEntitlementModel && (
        <Modal
          title={'Edit Entitlement'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'80%'}
          buttonAlignment="right"
          show={collEditEntitlementModel}
          content={
            <EditOrCreateEntitlement
              editEntitlementModal={true}
              editEntitlementList={editEntitlementList}
              isProtectedByDna={props.isProtectedByDna}
              projectName={props.projectName}
              submitEntitlement={updateEntitlement}
            />
          }
          scrollableContent={true}
          onCancel={collPermissionModelClose}
        />
      )}

      {editPathMethodModal && (
        <Modal
          title={'Edit API Path/Pattern and Method'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'40%'}
          buttonAlignment="right"
          show={editPathMethodModal}
          content={
            <div className={Styles.createEntitlementWrapper}>
              <div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    entitlementPathErrorMessage.length ? 'error' : '',
                  )}
                >
                  <label id="entitlementPathLabel" htmlFor="entitlementPathInput" className="input-label">
                    API Path/Pattern <sup>*</sup>
                  </label>
                  <input
                    type="text"
                    className="input-field"
                    id="EntitlementpathInput"
                    maxLength={200}
                    placeholder="Type here"
                    autoComplete="off"
                    value={entitlemenPath}
                    onChange={ontEntitlPatOnChange}
                  />
                  <span className={classNames('error-message')}>{entitlementPathErrorMessage}</span>
                </div>
              </div>

              <div>
                <div
                  className={classNames(
                    'input-field-group include-error',
                    entitlementHttpMethodErrorMessage.length ? 'error' : '',
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
                      {HTTP_OPTIONS?.map((obj: any) => (
                        <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                          {obj.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <span className={classNames('error-message', entitlementHttpMethodErrorMessage.length ? '' : 'hide')}>
                    {entitlementHttpMethodErrorMessage}
                  </span>
                </div>
              </div>

              <div className={Styles.submitButtton}>
                <button className={classNames('btn btn-tertiary')} type="button" onClick={updatePathHttpMethod}>
                  Update
                </button>
              </div>
            </div>
          }
          scrollableContent={true}
          onCancel={editPathModelClose}
        />
      )}
    </React.Fragment>
  );
};

export default EntitlementSubList;
