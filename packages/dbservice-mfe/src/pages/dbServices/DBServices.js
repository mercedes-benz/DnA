import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './db-services.scss';
import { dbServiceApi } from '../../apis/dbservice.api';
// dna-container
import Caption from 'dna-container/Caption';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import InfoModal from 'dna-container/InfoModal';
import Pagination from 'dna-container/Pagination';
import { getQueryParameterByName } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';
import DBServiceCard from '../../components/dbServiceCard/DBServiceCard';
import DBServiceForm from '../../components/dbServiceForm/DBServiceForm';
import ConnectionModal from '../../components/connectionModal/ConnectionModal';
import DetailsModal from '../../components/detailsModal/DetailsModal';
import Notification from '../../common/modules/uilab/js/src/notification';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ExpansionPanel from '../../common/modules/uilab/js/src/expansion-panel';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { Link } from 'react-router-dom';

const DBServices = ({user}) => {
  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [dbservices, setDbServices] = useState([]);
  const [createDbService, setCreateDbService] = useState(false);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedDbService, setSelectedDbService] = useState({});
  const [editDbService, setEditDbService] = useState(false);
  const [showConnectionalModal, setShowConnectionModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [nextSortOrder, setNextSortOrder] = useState('desc');

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffsetTemp = (currentPageNumberTemp - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumberTemp);
    setCurrentPageOffset(currentPageOffsetTemp);
  };

  const onPaginationNextClick = () => {
    const currentPageOffsetTemp = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffsetTemp);
  };

  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPagesTemp = Math.ceil(dbservices.length / pageNum);
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setTotalNumberOfPages(totalNumberOfPagesTemp > 0 ? totalNumberOfPagesTemp : 1);
  };

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
  }, []);

  useEffect(() => {
    getDbServices();
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  useEffect(() => {
    ExpansionPanel.defaultSetup();
    Tooltip.defaultSetup();
  }, []);

  // delete db service
  const deleteDbServiceContent = (
    <div>
      <h3>Are you sure you want to delete {selectedDbService.name}? </h3>
      <h5>It will delete the DB Service.</h5>
    </div>
  );

  const sortByColumn = (columnName, sortOrder) => {
    return () => {
      const sorted = [...dbservices].sort((a, b) => {
        let valA, valB;
        if (columnName === 'permission') {
        valA = getPermissionValue(a?.permission, a?.projectOwner?.id, user?.id);
        valB = getPermissionValue(b?.permission, b?.projectOwner?.id, user?.id);
      }
         else if (columnName === 'createdOn' || columnName === 'modifiedOn') {
          valA = new Date(a[columnName]);
          valB = new Date(b[columnName]);
        } else if (columnName === 'dataGovernance?.classificationType') {
          valA = a?.dataGovernance?.classificationType?.toUpperCase() || '';
          valB = b?.dataGovernance?.classificationType?.toUpperCase() || '';
        } else {
          valA = a[columnName]?.toString().toUpperCase() || '';
          valB = b[columnName]?.toString().toUpperCase() || '';
        }

        if (valA < valB) return sortOrder === 'asc' ? -1 : 1;
        if (valA > valB) return sortOrder === 'asc' ? 1 : -1;
        return 0;
      });

      setDbServices(sorted);
      setNextSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    };
  };

  const getPermissionValue = (permission, ownerId, currentUserId) => {
  const isOwner = currentUserId === ownerId;
  const isAdmin = permission?.admin;
  const hasWrite = permission?.write;

  if (isOwner) return 3;
  if (isAdmin) return 2;
  if (hasWrite) return 1;
  return 0;
};

  const displayPermission = (collab, dbservice, isOwnerCheck = false) => {
  const isOwner = isOwnerCheck || collab?.id === dbservice?.projectOwner?.id;
  const isAdmin = collab?.permission?.admin;
  const hasWrite = collab?.permission?.write;

  if (isOwner) return 'Read/Write (Owner)';
  if (isAdmin) return 'Read/Write (Admin)';
  if (hasWrite) return 'Read/Write';
  return 'Read';
};


  const deleteDbServiceAccept = () => {
    ProgressIndicator.show();
    dbServiceApi
      .deleteDBService(selectedDbService.id)
      .then(() => {
        getDbServices();
        Notification.show(`DB service ${selectedDbService.serviceName} deleted successfully.`);
      })
      .catch((e) => {
        Notification.show(
          e.response.data.errors?.length
            ? e.response.data.errors[0].message
            : 'Error while deleting DB service. Try again later!',
          'alert',
        );
        ProgressIndicator.hide();
      });
    setDeleteModal(false);
  };

  const getDbServices = () => {
    ProgressIndicator.show();
    dbServiceApi
      .getDBServices(currentPageOffset, maxItemsPerPage)
      .then((res) => {
        if (res.status !== 204 && res?.data?.data) {
          // const sortedProjects = res?.data?.records.sort((x, y) => {
            //     let fx = x.name.toLowerCase(), fy = y.name.toLowerCase();
            //     if (fx < fy) {
            //         return -1;
            //     }
            //     if (fx > fy) {
            //         return 1;
            //     }
            //     return 0;
            // });
          setDbServices(res.data.data);
          const totalCount = res?.data?.totalCount ?? 0;
          const pageSize = maxItemsPerPage ?? 1;
          const totalNumberOfPagesTemp = Math.ceil(totalCount / pageSize);

          setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
          setTotalNumberOfPages(totalNumberOfPagesTemp > 0 ? totalNumberOfPagesTemp : 1);
        } else {
          setDbServices([]);
          setTotalNumberOfPages(1);
          setCurrentPageNumber(1);
        }
        ProgressIndicator.hide();
      })
      .catch((e) => {
        ProgressIndicator.hide();
        Notification.show(
          e?.response?.data?.errors?.[0]?.message || 'Fetching DB services failed!',
          'alert'
        );
      });
  };

  const handleConnectionModalClose = () => {
    setShowConnectionModal(false);
  }

  const handleSelectDbService = (dbservice) => {
    setSelectedDbService(dbservice);
    setShowConnectionModal(true);
  }

  const handleDetailsModalClose = () => {
    setShowDetailsModal(false);
  }

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Caption title="DB Services">
          <div className={classNames(Styles.listHeader)}>
            <div>
              <button className={classNames('btn btn-primary', Styles.refreshBtn)} tooltip-data="Refresh" onClick={getDbServices}>
                <i className="icon mbc-icon refresh"></i>
              </button>
            </div>
            <span className={Styles.dividerLine}> &nbsp; </span>
            <div tooltip-data="Card View">
              <span
                className={cardViewMode ? Styles.iconActive : Styles.iconInactive}
                onClick={() => {
                  setCardViewMode(true);
                  setListViewMode(false);
                  sessionStorage.removeItem('storageListViewModeEnable');
                }}
              >
                <i className="icon mbc-icon widgets" />
              </span>
            </div>
            <span className={Styles.dividerLine}> &nbsp; </span>
            <div tooltip-data="List View">
              <span
                className={listViewMode ? Styles.iconActive : Styles.iconInactive}
                onClick={() => {
                  setCardViewMode(false);
                  setListViewMode(true);
                  sessionStorage.setItem('storageListViewModeEnable', true);
                }}
              >
                <i className="icon mbc-icon listview big" />
              </span>
            </div>
          </div>
        </Caption>
        {dbservices?.length === 0 && (
          <div className={Styles.noProjectContainer}>
            <div className={Styles.messageContainer}>
              <p className={Styles.lead}>
                Hi <span>{user.firstName} {user.lastName}</span>, you don&apos;t have<br />any DB service.
              </p>
              <p>You can create one</p>
            </div>
            <div className={Styles.btnContainer}>
              <button className={'btn btn-tertiary'} onClick={() => setCreateDbService(true)}>
                <span>Create now</span>
              </button>
            </div>
          </div>
        )}
        {listViewMode && (
          <>
            {dbservices && dbservices?.length ? (
              <div className={Styles.createNewArea}>
                <button className={'btn btn-secondary'} type="button" onClick={() => setCreateDbService(true)}>
                  <span className={Styles.addCircle}>
                    <i className="icon mbc-icon plus" />
                  </span>
                  <span>Create new DB Service</span>
                </button>
              </div>
            ) : null}
          </>
        )}
        {dbservices?.length > 0 && (
          <div className={classNames(listViewMode ? Styles.listContainer : '')}>
            {cardViewMode && (
              <>
                <div className={classNames(Styles.projectsContainer)}>
                  <div className={Styles.createNewCard} onClick={() => setCreateDbService(true)}>
                    <div className={Styles.addicon}> &nbsp; </div>
                    <label className={Styles.addlabel}>Create new DB Service</label>
                  </div>
                  {dbservices.map((dbservice) => (
                    <DBServiceCard
                      key={dbservice.id}
                      user={user}
                      dbservice={dbservice}
                      onSelectDbService={handleSelectDbService}
                      onEditDbService={(dbservice) => {
                        setSelectedDbService(dbservice);
                        setEditDbService(true);
                      }}
                      onDeleteDbService={(dbservice) => {
                        setSelectedDbService(dbservice);
                        setDeleteModal(true);
                      }}
                      onShowDetailsModal={(dbservice) => {
                        setSelectedDbService(dbservice);
                        setShowDetailsModal(true);
                      }}
                    />
                  ))}
                </div>
              </>
            )}
            {listViewMode && (
              <div className={classNames('expanstion-table', Styles.dBList)}>
                <div className={Styles.dBGrp}>
                  <div className={Styles.dBGrpList}>
                    <div className={Styles.dBGrpListItem}>
                      <div className={Styles.dBCaption}>
                        <div className={Styles.dBTile}>
                          <div className={classNames(Styles.dBTitleCol, Styles.dBName)}>
                            <label
                              className={
                                'sortable-column-header '
                              }
                              onClick={sortByColumn('serviceName', nextSortOrder)}
                            >
                              <i className="icon sort" />
                              Name
                            </label>
                          </div>
                          <div className={classNames(Styles.accessCol)}></div>
                          <div className={Styles.dBTitleCol}>
                            <label
                              className={
                                'sortable-column-header '
                              }
                              onClick={sortByColumn('permission', nextSortOrder)}
                            >
                              <i className="icon sort" />
                              Permission
                            </label>
                          </div>
                          <div className={Styles.dBTitleCol}>
                            <label
                              className={
                                'sortable-column-header '
                              }
                              onClick={sortByColumn('createdOn', nextSortOrder)}
                            >
                              <i className="icon sort" />
                              Created On
                            </label>
                          </div>
                          <div className={Styles.dBTitleCol}>
                            <label
                              className={
                                'sortable-column-header '
                              }
                              onClick={sortByColumn('modifiedOn', nextSortOrder)}
                            >
                              <i className="icon sort" />
                              Last Modified On
                            </label>
                          </div>
                          <div className={Styles.dBTitleCol}>
                            <label
                              className={
                                'sortable-column-header '
                              }
                              onClick={sortByColumn('dataGovernance?.classificationType', nextSortOrder)}
                            >
                              <i className="icon sort" />
                              Data Classification
                            </label>
                          </div>
                          <div className={Styles.dBTitleCol}>Action</div>
                        </div>
                      </div>
                      {dbservices?.map((item, index) => {
                        const isOwner = user?.id === item?.projectOwner?.id;
                        return (
                          <div
                            key={index}
                            className={'expansion-panel-group airflowexpansionPanel ' + Styles.dBGrpListItemPanel}
                          >
                            <div className={classNames('expansion-panel ', index === 0 ? 'open' : '')}>
                              <span className="animation-wrapper"></span>
                              <input type="checkbox" className="ff-only" id={index + '1'} defaultChecked={index === 0} />
                              <label className={Styles.expansionLabel + ' expansion-panel-label '} htmlFor={index + '1'}>
                                <div className={Styles.dBTile}>
                                  <div className={classNames(Styles.dBTitleCol, Styles.dBName)}>
                                    {/* <Link to={`/explorer/${item.serviceName}`}>{item.serviceName}</Link> */}
                                    <Link to={`/${item.serviceName}`}>{item.serviceName}</Link>
                                  </div>
                                  <div className={classNames(Styles.accessCol)}>
                                    {/* {hasPublicAccess && <span onClick={(e) => { e.preventDefault(); setShowAccessModel(true); setCurrentBucketName(item.bucketName) }} className={classNames(Styles.AccessIndicator,Styles.accessIndicatorList)}>Public</span>} */}
                                  </div>
                                  <div className={Styles.dBTitleCol}>
                                    {displayPermission(item?.permission, user?.id === item?.projectOwner?.id)}
                                  </div>
                                  <div className={Styles.dBTitleCol}>
                                    {regionalDateAndTimeConversionSolution(item.createdOn)}
                                  </div>
                                  <div className={Styles.dBTitleCol}>
                                    {regionalDateAndTimeConversionSolution(item.modifiedOn)}
                                  </div>
                                  <div className={Styles.dBTitleCol}>
                                    {item?.dataGovernance?.classificationType}
                                  </div>
                                  <div className={Styles.dBTitleCol}></div>
                                </div>
                                <i tooltip-data="Expand" className="icon down-up-flip"></i>
                              </label>
                              <div className="expansion-panel-content">
                                <div className={Styles.dBColContent}>
                                  {/* {collaborators?.length ? (
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
                            )} */}

                                  <div className={Styles.projectListAction}>
                                    <div className={Styles.actionBtnGrp}>
                                      {(isOwner) && (
                                        <>
                                          <button
                                            className={'btn btn-primary'}
                                            type="button"
                                            onClick={() => {
                                              setSelectedDbService(item);
                                              setEditDbService(true);
                                            }}
                                          >
                                            <i className="icon mbc-icon edit"></i>
                                            <span>Edit</span>
                                          </button>
                                          {isOwner ? (
                                            <button
                                              className={'btn btn-primary'}
                                              type="button"
                                              onClick={() => {
                                                setSelectedDbService(item);
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
                                          setSelectedDbService(item);
                                          setShowConnectionModal(true);
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
          </div>
        )}
      </div>
      {dbservices?.length && (
        <Pagination
          totalPages={totalNumberOfPages}
          pageNumber={currentPageNumber}
          onPreviousClick={onPaginationPreviousClick}
          onNextClick={onPaginationNextClick}
          onViewByNumbers={onViewByPageNum}
          displayByPage={true}
        />
      )}
      {createDbService && (
        <Modal
          title={'Create DB Service'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={createDbService}
          content={<DBServiceForm user={user} edit={false} onSave={() => { setCreateDbService(false); getDbServices(); }} />}
          scrollableContent={true}
          onCancel={() => setCreateDbService(false)}
        />
      )}
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteDbServiceContent}
        onCancel={() => setDeleteModal(false)}
        onAccept={deleteDbServiceAccept}
      />
      {editDbService && (
        <Modal
          title={'Edit DB Service'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={editDbService}
          content={<DBServiceForm edit={true} dbservice={selectedDbService} onSave={() => { setEditDbService(false); getDbServices(); }} />}
          scrollableContent={true}
          onCancel={() => setEditDbService(false)}
        />
      )}
      {showConnectionalModal && (
        <InfoModal
          title="Connect"
          modalCSS={Styles.header}
          show={showConnectionalModal}
          modalWidth={'800px'}
          content={<ConnectionModal dbservice={selectedDbService} onOk={handleConnectionModalClose}/>}
          hiddenTitle={true}
          onCancel={handleConnectionModalClose}
        />
      )}
      {showDetailsModal && (
        <InfoModal
          title="Details"
          modalCSS={Styles.header}
          show={showDetailsModal}
          modalWidth={'800px'}
          content={<DetailsModal dbservice={selectedDbService} />}
          hiddenTitle={true}
          onCancel={handleDetailsModalClose}
        />
      )}
    </>
  );
};
export default DBServices;