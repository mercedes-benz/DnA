import React, { useEffect, useState } from 'react';
import { useDispatch, 
  useSelector 
} from 'react-redux';
import Styles from './MatomoList.scss';
import classNames from 'classnames';
import { history } from '../../store/storeRoot';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';

import ConfirmModal from 'dna-container/ConfirmModal';
// import InfoModal from 'dna-container/InfoModal';

// import { ConnectionModal } from '../ConnectionInfo/ConnectionModal';
// import { setFiles } from '../Explorer/redux/fileExplorer.actions';
// import { getConnectionInfo, hideConnectionInfo } from '../ConnectionInfo/redux/connection.actions';
import { matomoApi } from '../../apis/matamo.api';
import { matomoActions } from '../redux/matomo.actions';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { regionalDateAndTimeConversionSolution } from '../Utility/utils';
// import { Envs } from '../Utility/envs';
// import { Link } from 'react-router-dom';
import Popper from 'popper.js';

export const MatomoList = (props) => { 
  const dispatch = useDispatch();
  // const { connect } = useSelector((state) => state.connectionInfo);
  const { matomoList } = useSelector((state) => state.matomo);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});
  const [currentSortOrder, setCurrentSortOrder] = useState('asc');
  const [nextSortOrder, setNextSortOrder] = useState('desc');
  const [currentColumnToSort, setCurrentColumnToSort] = useState('siteName');

  const isCardView = props.isCardView;

  let popperObj, tooltipElem = null;

  // const matomoList = [
  //   {
  //     "classificationType": "string",
  //     "collaborators": [
  //       {
  //         "department": "string",
  //         "email": "string",
  //         "firstName": "string",
  //         "id": "string",
  //         "lastName": "string",
  //         "mobileNumber": "string",
  //         "permission": "view"
  //       }
  //     ],
  //     "createdBy": {
  //       "department": "string",
  //       "email": "string",
  //       "firstName": "string",
  //       "id": "string",
  //       "lastName": "string",
  //       "mobileNumber": "string"
  //     },
  //     "createdOn": "2023-09-12T06:16:49.306Z",
  //     "department": "string",
  //     "division": "string",
  //     "id": "string",
  //     "lastModified": "2023-09-12T06:16:49.306Z",
  //     "permission": "admin",
  //     "piiData": true,
  //     "siteId": "string",
  //     "siteName": "string",
  //     "siteUrl": "string",
  //     "status": "string",
  //     "subDivision": "string"
  //   },
  //   {
  //     "classificationType": "string",
  //     "collaborators": [
  //       {
  //         "department": "string",
  //         "email": "string",
  //         "firstName": "string",
  //         "id": "string",
  //         "lastName": "string",
  //         "mobileNumber": "string",
  //         "permission": "view"
  //       }
  //     ],
  //     "createdBy": {
  //       "department": "string",
  //       "email": "string",
  //       "firstName": "string",
  //       "id": "string",
  //       "lastName": "string",
  //       "mobileNumber": "string"
  //     },
  //     "createdOn": "2023-09-12T06:16:49.306Z",
  //     "department": "string",
  //     "division": "string",
  //     "id": "string",
  //     "lastModified": "2023-09-12T06:16:49.306Z",
  //     "permission": "admin",
  //     "piiData": true,
  //     "siteId": "string",
  //     "siteName": "string",
  //     "siteUrl": "string",
  //     "status": "string",
  //     "subDivision": "string"
  //   }
  // ]

  const sortByColumn = (columnName, sortOrder) => {
    return () => {
      let sortedArray = [];

      if (columnName === 'permission') {
        sortedArray = matomoList?.sort((a, b) => {
          const nameA = a[columnName];
          const nameB = b[columnName];
          if (nameA.write < nameB.write) {
            return sortOrder === 'asc' ? -1 : 1;
          } else if (nameA.write > nameB.write) {
            return sortOrder === 'asc' ? 1 : -1;
          }
          return 0;
        });
      } else if (columnName === 'createdDate' || columnName === 'lastModifiedDate') {
        sortedArray = matomoList?.sort((a, b) => {
          const nameA = new Date(a[columnName]);
          const nameB = new Date(b[columnName]);
          if (nameA < nameB) {
            return sortOrder === 'asc' ? -1 : 1;
          } else if (nameA > nameB) {
            return sortOrder === 'asc' ? 1 : -1;
          }
          return 0;
        });
      } else {
        sortedArray = matomoList?.sort((a, b) => {
          const nameA = a[columnName]?.toString() ? a[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          const nameB = b[columnName]?.toString() ? b[columnName].toString().toUpperCase() : ''; // ignore upper and lowercase
          if (nameA < nameB) {
            return sortOrder === 'asc' ? -1 : 1;
          } else if (nameA > nameB) {
            return sortOrder === 'asc' ? 1 : -1;
          }
          return 0;
        });
      }

      setNextSortOrder(sortOrder == 'asc' ? 'desc' : 'asc');
      setCurrentSortOrder(sortOrder);
      setCurrentColumnToSort(columnName);
      dispatch({
        type: 'MATOMO_DATA',
        payload: sortedArray,
      });
    };
  };

  useEffect(() => {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }, []);

  useEffect(() => {
    // on page load run sorting function with default values
    sortByColumn(currentColumnToSort, currentSortOrder);
    //eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const deleteBucketContent = (
    <div>
      <h3>Are you sure you want to delete {selectedItem.bucketName} ? </h3>
      <h5>It will delete all content.</h5>
    </div>
  );

  const deleteBucketClose = () => {
    setDeleteModal(false);
  };

  const deleteBucketAccept = () => {
    ProgressIndicator.show();
    matomoApi
      .deleteMatomo(selectedItem.id)
      .then(() => {
        dispatch(matomoActions.getMatomoList());
        Notification.show(`Site ${selectedItem.siteName} deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting a site. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  // const onConnectionModalClose = () => {
  //   dispatch(hideConnectionInfo());
  // };

  // const displayPermission = (item) => {
  //   return Object.entries(item || {})
  //     ?.map(([key, value]) => {
  //       if (value === true) {
  //         return key;
  //       }
  //     })
  //     ?.filter((x) => x) // remove falsy values
  //     ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1)) // update first character to Uppercase
  //     ?.join(' / ');
  // };

  const onCollabsIconMouseOver = (e) => {
    const targetElem = e.target;
    tooltipElem = targetElem.nextElementSibling;
    if (tooltipElem) {
      tooltipElem.classList.add('tooltip', 'show');
      tooltipElem.classList.remove('hide');
      popperObj = new Popper(targetElem, tooltipElem, {
        placement: 'top',
      });
    }
  };

  const onCollabsIconMouseOut = () => {
    if (tooltipElem) {
      tooltipElem.classList.add('hide');
      tooltipElem.classList.remove('tooltip', 'show');
    }
    popperObj?.destroy();
  };

  return (
    <>
      {isCardView ? (
        <>
          <div className={Styles.newStorageCard} onClick={()=> history.push('/createMatomo')}>
            <div className={Styles.addicon}> &nbsp; </div>
            <label className={Styles.addlabel}>Create new Matomo Project</label>
          </div>
          {matomoList?.map((item, index) => {
            const hasWriteAccess = item?.permission === 'admin' ? true : false;
            const isOwner = props.user?.id === item.createdBy?.id;
            const collaborators = item.collaborators?.filter((item) => item.id !== props.user?.id);
            return (
              <div key={'card-' + index} className={classNames(Styles.storageCard)}>
                <div className={Styles.cardHead}>
                  <div className={classNames(Styles.cardHeadInfo)}>
                    <div
                      className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
                      // onClick={() => history.push(`/explorer/${item.siteName}`)}
                    >
                      {item.siteName}
                    </div>
                  </div>
                </div>
                <hr />
                <div className={Styles.cardBodySection}>
                  <div>
                    <div>
                      <div>Site Id</div>
                      <div>{item.siteId}</div>
                    </div>
                    <div>
                      <div>Created on</div>
                      <div>{regionalDateAndTimeConversionSolution(item.createdOn)}</div>
                    </div>
                    <div>
                      <div>Last modified</div>
                      <div>{regionalDateAndTimeConversionSolution(item.lastModified)}</div>
                    </div>
                    <div>
                      <div>Classification</div>
                      <div>{item.classificationType || 'N/A'}</div>
                    </div>
                    <div>
                      <div>Permission</div>
                      <div className={Styles.firstLetterCapital}>
                        {item?.permission || 'N/A'}
                        {isOwner && ` (Owner)`}
                      </div>
                    </div>
                    <div>
                      <div>Website/App Url</div>
                      <div>{item.siteUrl || 'N/A'}</div>
                    </div>
                    <div className={Styles.cardCollabSection}>
                      <div>Collaborators</div>
                      {collaborators?.length > 0 ? (
                        <div>
                          <i className="icon mbc-icon profile"/>
                          <span className={Styles.cardCollabIcon} onMouseOver={onCollabsIconMouseOver} onMouseOut={onCollabsIconMouseOut}>
                            {collaborators?.length}
                          </span>
                          <div className={classNames(Styles.collabsList, 'hide')}>
                            <ul>
                              {collaborators?.map((bucketItem, bucketIndex) => {
                                  // Check if lastName is more than 12 characters
                                  {/* let lastName = bucketItem.lastName;
                                  if (lastName?.length > 12) {
                                    lastName = lastName.substring(0, 12) + " ...";
                                  } */}
                                return (
                                  <li key={'collab' + bucketIndex}>
                                    <span>
                                      {bucketItem.id}
                                      {item.createdBy?.id === bucketItem.id ? ' (Owner)' : ''}
                                    </span>
                                    <span className={classNames(Styles.permission, Styles.firstLetterCapital)}>
                                      &nbsp;[{bucketItem?.permission}]
                                    </span>
                                  </li>
                                );
                              })}
                            </ul>
                          </div>
                        </div>
                      ) : <div>None</div>}
                    </div>
                  </div>
                </div>
                <div className={Styles.cardFooter}>
                  <>
                    <div></div>
                    <div className={Styles.btnGrp}>
                      {hasWriteAccess && (
                        <>
                          <button
                            className={'btn btn-primary'}
                            type="button"
                            onClick={() => {
                              setSelectedItem(item);
                              history.push(`/editMatomo/${item.id}`);
                            }}
                          >
                            <i className="icon mbc-icon edit"></i>
                            <span>Edit</span>
                          </button>
                          
                          <button
                            className={'btn btn-primary'}
                            type="button"
                            onClick={() => {
                              setSelectedItem(item);
                              setDeleteModal(true);
                            }}
                          >
                            <i className="icon delete"></i>
                            <span>Delete</span>
                          </button>
                          
                        </>
                      )}
                      {/* <button
                        className={'btn btn-primary'}
                        type="button"
                        onClick={() => {
                          dispatch(getConnectionInfo(item.bucketName, item.createdBy));
                        }}
                      >
                        <i className="icon mbc-icon comparison"></i>
                        <span>Connect</span>
                      </button> */}
                    </div>
                  </>
                </div>
              </div>
            );
          })}
        </>
      ) : (
        <div className={classNames('expanstion-table', Styles.bucketList)}>
          <div className={Styles.bucketGrp}>
            <div className={Styles.bucketGrpList}>
              <div className={Styles.bucketGrpListItem}>
                <div className={Styles.bucketCaption}>
                  <div className={Styles.bucketTile}>
                    <div className={classNames(Styles.bucketTitleCol)}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'bucketId' ? currentSortOrder : '')
                        }
                        onClick={sortByColumn('siteId', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Site Id
                      </label>
                    </div>
                    <div className={classNames(Styles.bucketTitleCol, Styles.bucketName)}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'siteName' ? currentSortOrder : '')
                        }
                        onClick={sortByColumn('siteName', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Site Name
                      </label>
                    </div>
                    <div className={classNames(Styles.bucketTitleCol)}>
                      <label
                        className={
                          'sortable-column-header '
                        }
                        // onClick={sortByColumn('bucketName', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Site Url
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'permission' ? currentSortOrder : '')
                        }
                        onClick={sortByColumn('permission', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Permission
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'createdDate' ? currentSortOrder : '')
                        }
                        onClick={sortByColumn('createdDate', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Created On
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (currentColumnToSort === 'lastModifiedDate' ? currentSortOrder : '')
                        }
                        onClick={sortByColumn('lastModifiedDate', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Last Modified On
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (currentColumnToSort === 'classificationType' ? currentSortOrder : '')
                        }
                        onClick={sortByColumn('classificationType', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Data Classification
                      </label>
                    </div>
                    <div className={Styles.bucketTitleCol}>Action</div>
                  </div>
                </div>
                {matomoList?.map((item, index) => {
                  const hasWriteAccess = item?.permission === 'admin' ? true : false;
                  const isOwner = props.user?.id === item.createdBy?.id;
                  const collaborators = item.collaborators?.filter((item) => item.accesskey !== props.user?.id);
                  return (
                    <div
                      key={index}
                      className={'expansion-panel-group airflowexpansionPanel ' + Styles.bucketGrpListItemPanel}
                    >
                      <div className={classNames('expansion-panel ', index === 0 ? 'open' : '')}>
                        <span className="animation-wrapper"></span>
                        <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                        <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                          <div className={Styles.bucketTile}>
                            <div className={classNames(Styles.bucketTitleCol)}>
                              {/* <Link to={`/explorer/${item.siteName}`}>{item.siteId}</Link> */}
                              {item.siteId}
                            </div>
                            <div className={classNames(Styles.bucketTitleCol, Styles.bucketName)}>
                              {/* <Link to={`/explorer/${item.siteName}`}>{item.siteName}</Link> */}
                              {item.siteName}
                            </div>
                            <div className={Styles.bucketTitleCol}>
                              {item.siteUrl}
                            </div>
                            <div className={classNames(Styles.bucketTitleCol, Styles.firstLetterCapital)}>
                              {item?.permission}
                              {isOwner && ` (Owner)`}
                            </div>
                            <div className={Styles.bucketTitleCol}>
                              {regionalDateAndTimeConversionSolution(item.createdOn)}
                            </div>
                            <div className={Styles.bucketTitleCol}>
                              {regionalDateAndTimeConversionSolution(item.lastModified)}
                            </div>
                            <div className={Styles.bucketTitleCol}>{item.classificationType}</div>
                            <div className={Styles.bucketTitleCol}></div>
                          </div>
                          <i tooltip-data="Expand" className="icon down-up-flip"></i>
                        </label>
                        <div className="expansion-panel-content">
                          <div className={Styles.bucketColContent}>
                            {collaborators?.length ? (
                              <div className={Styles.projectList}>
                                <div className={Styles.bucketTile + ' ' + Styles.bucketTileCaption}>
                                  <div className={classNames(Styles.bucketTitleCol, Styles.expansionpanelFirstCol)}>
                                    User Id
                                  </div>
                                  {/* <div className={Styles.bucketTitleCol}>Name</div> */}
                                  <div className={Styles.bucketTitleCol}>Permission</div>
                                  <div className={Styles.bucketTitleCol}></div>
                                </div>
                                {collaborators?.map((bucketItem, bucketIndex) => {
                                  return (
                                    <div key={bucketIndex} className={Styles.bucketTile}>
                                      <div className={classNames(Styles.bucketTitleCol, Styles.expansionpanelFirstCol)}>
                                        {bucketItem.id}{' '}
                                        {item.createdBy?.id === bucketItem.id ? '(Owner)' : ''}
                                      </div>
                                      {/* <div
                                        className={Styles.bucketTitleCol}
                                      >{`${bucketItem.firstName} ${bucketItem.lastName}`}</div> */}
                                      <div className={classNames(Styles.bucketTitleCol, Styles.firstLetterCapital)}>
                                        {bucketItem?.permission}
                                      </div>

                                      <div className={Styles.bucketTitleCol}></div>
                                    </div>
                                  );
                                })}
                              </div>
                            ) : (
                              <div className={Styles.projectList}>
                                <div className={Styles.noCollaborators}>Collaborators Not Exist!</div>
                              </div>
                            )}

                            <div className={Styles.projectListAction}>
                              <div className={Styles.actionBtnGrp}>
                                {hasWriteAccess && (
                                  <>
                                    <button
                                      className={'btn btn-primary'}
                                      type="button"
                                      onClick={() => {
                                        setSelectedItem(item);
                                        history.push(`/editMatomo/${item.id}`);
                                      }}
                                    >
                                      <i className="icon mbc-icon edit"></i>
                                      <span>Edit</span>
                                    </button>
                                    {hasWriteAccess ? (
                                      <button
                                        className={'btn btn-primary'}
                                        type="button"
                                        onClick={() => {
                                          setSelectedItem(item);
                                          setDeleteModal(true);
                                        }}
                                      >
                                        <i className="icon delete"></i>
                                        <span>Delete</span>
                                      </button>
                                    ) : null}
                                  </>
                                )}
                                {/* <button
                                  className={'btn btn-primary'}
                                  type="button"
                                  onClick={() => {
                                    dispatch(getConnectionInfo(item.bucketName, item.createdBy));
                                  }}
                                >
                                  <i className="icon mbc-icon comparison"></i>
                                  <span>Connect</span>
                                </button> */}
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
      )}
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteBucketContent}
        onCancel={deleteBucketClose}
        onAccept={deleteBucketAccept}
      />
      {/* {connect?.modal && (
        <InfoModal
          title="Connect"
          modalCSS={Styles.header}
          show={connect?.modal}
          content={<ConnectionModal user={props.user} />}
          hiddenTitle={true}
          onCancel={onConnectionModalClose}
        />
      )} */}
    </>
  );
};
