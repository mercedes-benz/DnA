import React, { useState, useEffect } from 'react';
import Styles from './EntitlementSubList.scss';
// @ts-ignore
// import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Modal from 'dna-container/Modal';

// @ts-ignore
// import Notification from '../../../common/modules/uilab/js/src/notification';
// @ts-ignore
import ExpansionPanel from '../../../common/modules/uilab/js/src/expansion-panel';
// @ts-ignore
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import classNames from 'classnames';
import EditOrCreateEntitlement from './EditOrCreateEntitlement';

// export interface IEntitlementSublistProps {
//   listOfProject: any[];
//   getRefreshedDagPermission: (name: string, apiPathMethodIndex: number) => void;
//   getProjectSorted: (prjIdSortVal: any) => void;
//   entitelmentListResponse: any;
//   updatedFinalEntitlementList: any;
//   readOnlyMode: boolean;
//   projectName?: string;
//   env: string;
// }

const EntitlementSubList = (props) => {
  const [collEditEntitlementModel, setCollEditEntitlementModel] = useState(false);
  const [currentSortOrder, setCurrentSortOrder] = useState('desc');
  const [nextSortOrder, setNextSortOrder] = useState('asc');
  const [currentColumnToSort, setCurrentColumnToSort] = useState('name');
  const [editEntitlementList, setEditEntitlementList] = useState([]);
  const [allEntitlementList, setAllEntitlementList] = useState([]);
  const [entitelmentListResponse, setEntitelmentListResponse] = useState([]);

  const onEditEntitlement = (entitlement) => {
    setCollEditEntitlementModel(true);
    setEditEntitlementList(entitlement);
  };

  const onDeleteEntitlement = (entitlement) => {
    // Remove the entitlement from the list of allEntitlementList
    const updatedList = allEntitlementList.filter((item) => item.name !== entitlement.name);
    const updatedEntitelmentListResponse = entitelmentListResponse.filter(
      (item) => item.name !== entitlement.name,
    );
    setAllEntitlementList([...updatedList]);
    setEntitelmentListResponse([...updatedEntitelmentListResponse]);
    props.updatedFinalEntitlementList([...updatedEntitelmentListResponse]);
    props.getProjectSorted(updatedList);
  };

  const collPermissionModelClose = () => {
    setCollEditEntitlementModel(false);
  };

  const updateEntitlement = (editedEntitlement) => {
    collPermissionModelClose();
    const updatedList = allEntitlementList.map((entitlement) => {
      if (
        entitlement.name === editedEntitlement.name ||
        entitlement.name === editedEntitlement?.beforeUpdateEntitlName
      ) {
        return editedEntitlement;
      }
      return entitlement;
    });

    const updatedEntitelmentListResponse = entitelmentListResponse.map((entitlement) => {
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

  // Sorting table data
  const sortByColumn = (columnName, sortOrder) => {
    return () => {
      let sortedArray = [];
      sortedArray = allEntitlementList?.sort((a, b) => {
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
                {allEntitlementList?.map((item, index) => {
                  return (
                    <div
                      key={`${item.id}-${index}`}
                      className={'expansion-panel-group airflowexpansionPanel ' + Styles.dagGrpListItemPanel}
                    >
                      <div className={classNames('expansion-panel', index === 0 ? 'open' : '')}>
                        <span className="animation-wrapper"></span>
                        <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                        <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                          <div className={classNames(Styles.entTile)}>
                            <div className={Styles.dagTitleCol}>
                              {item?.name && item?.name?.length
                                ? item?.name?.map((chip) => {
                                    return (
                                      <>
                                        <label className="chips read-only">{chip}</label>&nbsp;&nbsp;
                                      </>
                                    );
                                  })
                                : 'N/A'}
                            </div>
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
                                <div className={Styles.dagTitleCol}></div>
                              </div>
                              <div className={Styles.projectDagList}>
                                {item ? (
                                  <div className={Styles.entTile}>
                                    <div className={Styles.dagTitleCol}>{item.apiPattern}</div>
                                    <div className={Styles.dagTitleCol}>{item.httpMethod}</div>
                                    <div className={Styles.dagTitleCol}></div>
                                  </div>
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
              projectName={props.projectName}
              submitEntitlement={updateEntitlement}
              env={props.env}
            />
          }
          scrollableContent={true}
          onCancel={collPermissionModelClose}
        />
      )}
    </React.Fragment>
  );
};

export default EntitlementSubList;
