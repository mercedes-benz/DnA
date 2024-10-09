import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Styles from './BucketList.scss';
import classNames from 'classnames';
import { history } from '../../store/storeRoot';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';

import ConfirmModal from 'dna-container/ConfirmModal';
import InfoModal from 'dna-container/InfoModal';
import Modal from 'dna-container/Modal';
import BucketTOU from './BucketTOU';

import { ConnectionModal } from '../ConnectionInfo/ConnectionModal';
// import { setFiles } from '../Explorer/redux/fileExplorer.actions';
import { getConnectionInfo, hideConnectionInfo } from '../ConnectionInfo/redux/connection.actions';
import { bucketsApi } from '../../apis/buckets.api';
import { bucketActions } from './redux/bucket.actions';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { regionalDateAndTimeConversionSolution } from '../Utility/utils';
import { Envs } from '../Utility/envs';
import { Link } from 'react-router-dom';
import Popper from 'popper.js';

export const BucketList = (props) => {
  const dispatch = useDispatch();
  const { connect } = useSelector((state) => state.connectionInfo);
  const { bucketList } = useSelector((state) => state.bucket);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState({});
  const [currentSortOrder, setCurrentSortOrder] = useState('asc');
  const [nextSortOrder, setNextSortOrder] = useState('desc');
  const [currentColumnToSort, setCurrentColumnToSort] = useState('bucketName');
  const [showTermsModal, setShowTermsModal] = useState(false);
  const [showAccessModel, setShowAccessModel] = useState(false);
  const [currentBucketName, setCurrentBucketName] = useState(''); 

  const {pagination: { maxItemsPerPage, currentPageOffset }} = useSelector((state) => state.bucket);

  const isDataikuEnabled = Envs.ENABLE_DATAIKU;
  const isCardView = props.isCardView;

  let popperObj, tooltipElem = null;

  const sortByColumn = (columnName, sortOrder) => {
    return () => {
      let sortedArray = [];

      if (columnName === 'permission') {
        sortedArray = bucketList?.sort((a, b) => {
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
        sortedArray = bucketList?.sort((a, b) => {
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
        sortedArray = bucketList?.sort((a, b) => {
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
        type: 'BUCKET_DATA',
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
      <h5>It will delete all content from the bucket.</h5>
      {isDataikuEnabled && <h6>Dataiku project(s) connection if any, will be removed.</h6>}
    </div>
  );

  const publicAccessModelContent = (
    <div className={classNames(Styles.accessContent)}>
      <h5>
      public access for the bucket {currentBucketName} is enabled which makes the bucket publicly accessible without any authentication.<br/>
        You can use &nbsp;
        <strong>{Envs.BUCKET_PUBLIC_ACCESS_BASEURL + currentBucketName}</strong>
        &nbsp;as the base URL to read the contents from the bucket.<br/>
        Please make sure that you do not store any confidential data in this bucket as it is publicly accessible.
      </h5>
    </div>
  );

  const deleteBucketClose = () => {
    setDeleteModal(false);
  };

  const deleteBucketAccept = () => {
    ProgressIndicator.show();
    bucketsApi
      .deleteBucket(selectedItem.bucketName)
      .then(() => {
        dispatch(bucketActions.getBucketList(currentPageOffset, maxItemsPerPage));
        Notification.show(`Bucket ${selectedItem.bucketName} deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting a bucket. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  const onConnectionModalClose = () => {
    dispatch(hideConnectionInfo());
  };

  const displayPermission = (item) => {
    return Object.entries(item || {})
      ?.map(([key, value]) => {
        if (value === true) {
          return key;
        }
      })
      ?.filter((x) => x) // remove falsy values
      ?.map((perm) => perm?.charAt(0)?.toUpperCase() + perm?.slice(1)) // update first character to Uppercase
      ?.join(' / ');
  };

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
          <div className={Styles.newStorageCard} onClick={()=>{setShowTermsModal(true); }}>
            <div className={Styles.addicon}> &nbsp; </div>
            <label className={Styles.addlabel}>Create new Storage Bucket</label>
          </div>
          {bucketList?.map((item, index) => {
            const hasWriteAccess = item?.permission?.write;
            const isOwner = props.user?.id === item.createdBy?.id;
            const collaborators = item.collaborators?.filter((val) => val.accesskey !== item?.createdBy.id);
            const hasPublicAccess = item?.enablePublicAccess || false ;
            return (
              <div key={'card-' + index} className={classNames(Styles.storageCard)}>
                <div className={Styles.cardHead}>
                  <div className={classNames(Styles.cardHeadInfo)}>
                    <div
                      className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)}
                      onClick={() => history.push(`/explorer/${item.bucketName}`)}
                    >
                      {item.bucketName}
                    </div>
                  </div>
                </div>
                <hr />
                <div className={Styles.cardBodySection}>
                  <div>
                    <div>
                      <div>Created on</div>
                      <div>{item.createdDate ? regionalDateAndTimeConversionSolution(item.createdDate) : 'N/A'}</div>
                    </div>
                    <div>
                      <div>Last modified</div>
                      <div>{regionalDateAndTimeConversionSolution(item.lastModifiedDate)}</div>
                    </div>
                    <div>
                      <div>Classification</div>
                      <div>{item.classificationType || 'N/A'}</div>
                    </div>
                    <div>
                      <div>Permission</div>
                      <div>
                        {displayPermission(item?.permission) || 'N/A'}
                        {isOwner && ` (Owner)`}
                      </div>
                    </div>
                    <div>
                      <div>CreatedBy</div>
                      <div>{(item.createdBy.firstName !== null  && item.createdBy.lastName !== null )? item.createdBy.firstName + " "+item.createdBy.lastName : item.createdBy.id}</div>
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
                                  let lastName = bucketItem.lastName;
                                  if (lastName?.length > 12) {
                                    lastName = lastName.substring(0, 12) + " ...";
                                  }
                                return (
                                  <li key={'collab' + bucketIndex}>
                                    <span>
                                      {`${bucketItem.firstName} ${lastName}`}
                                      {item.createdBy?.id === bucketItem.accesskey ? ' (Owner)' : ''}
                                    </span>
                                    <span className={Styles.permission}>
                                      &nbsp;[{displayPermission(bucketItem?.permission)}]
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
                    <div>{hasPublicAccess && <span onClick={() => { setShowAccessModel(true); setCurrentBucketName(item.bucketName) }} className={classNames(Styles.AccessIndicator)}>Public</span>}</div>
                      <div className={Styles.btnGrp}>
                        {(hasWriteAccess && !item.bucketName.startsWith('dna-datalake')) && (
                          <>
                            <button
                              className={'btn btn-primary'}
                              type="button"
                              onClick={() => {
                                setSelectedItem(item);
                                history.push(`/editBucket/${item.bucketName}`);
                              }}
                            >
                              <i className="icon mbc-icon edit"></i>
                              <span>Edit</span>
                            </button>
                            {isOwner && hasWriteAccess ? (
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
                      ) }
                        <button
                          className={'btn btn-primary'}
                          type="button"
                          onClick={() => {
                            dispatch(getConnectionInfo(item.bucketName, item.createdBy));
                          }}
                        >
                          <i className="icon mbc-icon comparison"></i>
                          <span>Connect</span>
                        </button>
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
                    <div className={classNames(Styles.bucketTitleCol, Styles.bucketName)}>
                      <label
                        className={
                          'sortable-column-header ' + (currentColumnToSort === 'bucketName' ? currentSortOrder : '')
                        }
                        onClick={sortByColumn('bucketName', nextSortOrder)}
                      >
                        <i className="icon sort" />
                        Bucket Name
                      </label>
                    </div>
                    <div className={classNames(Styles.accessCol)}></div>
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
                {bucketList?.map((item, index) => {
                  const hasWriteAccess = item?.permission?.write;
                  const isOwner = props.user?.id === item.createdBy?.id;
                  const collaborators = item.collaborators?.filter((item) => item.accesskey !== props.user?.id);
                  const hasPublicAccess = item?.enablePublicAccess || false ;
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
                            <div className={classNames(Styles.bucketTitleCol, Styles.bucketName)}>
                              <Link to={`/explorer/${item.bucketName}`}>{item.bucketName}</Link>
                            </div>
                            <div className={classNames(Styles.accessCol)}>
                              {hasPublicAccess && <span onClick={(e) => { e.preventDefault(); setShowAccessModel(true); setCurrentBucketName(item.bucketName) }} className={classNames(Styles.AccessIndicator,Styles.accessIndicatorList)}>Public</span>}
                            </div>
                            <div className={Styles.bucketTitleCol}>
                              {displayPermission(item?.permission)}
                              {isOwner && ` (Owner)`}
                            </div>
                            <div className={Styles.bucketTitleCol}>
                              {regionalDateAndTimeConversionSolution(item.createdDate)}
                            </div>
                            <div className={Styles.bucketTitleCol}>
                              {regionalDateAndTimeConversionSolution(item.lastModifiedDate)}
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
                                  <div className={Styles.bucketTitleCol}>Name</div>
                                  <div className={Styles.bucketTitleCol}>Permission</div>
                                  <div className={Styles.bucketTitleCol}></div>
                                </div>
                                {collaborators?.map((bucketItem, bucketIndex) => {
                                  return (
                                    <div key={bucketIndex} className={Styles.bucketTile}>
                                      <div className={classNames(Styles.bucketTitleCol, Styles.expansionpanelFirstCol)}>
                                        {bucketItem.accesskey}{' '}
                                        {item.createdBy?.id === bucketItem.accesskey ? '(Owner)' : ''}
                                      </div>
                                      <div
                                        className={Styles.bucketTitleCol}
                                      >{`${bucketItem.firstName} ${bucketItem.lastName}`}</div>
                                      <div className={Styles.bucketTitleCol}>
                                        {displayPermission(bucketItem?.permission)}
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
                                {(hasWriteAccess && !item.bucketName.startsWith('dna-datalake')) && (
                                  <>
                                    <button
                                      className={'btn btn-primary'}
                                      type="button"
                                      onClick={() => {
                                        setSelectedItem(item);
                                        history.push(`/editBucket/${item.bucketName}`);
                                      }}
                                    >
                                      <i className="icon mbc-icon edit"></i>
                                      <span>Edit</span>
                                    </button>
                                    {isOwner && hasWriteAccess ? (
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
                                <button
                                  className={'btn btn-primary'}
                                  type="button"
                                  onClick={() => {
                                    dispatch(getConnectionInfo(item.bucketName, item.createdBy));
                                  }}
                                >
                                  <i className="icon mbc-icon comparison"></i>
                                  <span>Connect</span>
                                </button>
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
        <Modal
        title=""
        show={showTermsModal}
        showAcceptButton={false}
        showCancelButton={false}
        content={<BucketTOU setShowTermsModal={setShowTermsModal} /> }
        buttonAlignment="center"
        onCancel={() => setShowTermsModal(false)}
        modalStyle={{
          padding: '50px 90px 35px',
          minWidth: 'unset',
          width: '65%',
          maxWidth: '65%'
        }}
      />
      <ConfirmModal
        title=""
        show={showAccessModel}
        showAcceptButton={false}
        showCancelButton={true}
        cancelButtonTitle="OK"
        content={publicAccessModelContent}
        onCancel={() => {setShowAccessModel(false); setCurrentBucketName('');}}
      />
      
      {connect?.modal && (
        <InfoModal
          title="Connect"
          modalCSS={Styles.header}
          show={connect?.modal}
          content={<ConnectionModal user={props.user} />}
          hiddenTitle={true}
          onCancel={onConnectionModalClose}
        />
      )}
    </>
  );
};
