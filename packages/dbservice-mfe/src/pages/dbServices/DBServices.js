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
import DBServiceRow from '../../components/dbServiceRow/DBServiceRow';
import DBServiceForm from '../../components/dbServiceForm/DBServiceForm';
import ConnectionModal from '../../components/connectionModal/ConnectionModal';
import DetailsModal from '../../components/detailsModal/DetailsModal';

const DBServices = ({user}) => {
  const listViewSelected = sessionStorage.getItem('storageListViewModeEnable') || false;
  const [cardViewMode, setCardViewMode] = useState(!listViewSelected);
  const [listViewMode, setListViewMode] = useState(listViewSelected);
  const [dbservices, setDbServices] = useState([]);
  const [createDbService, setCreateDbService] = useState(false);
  const [showDeleteModal, setDeleteModal] = useState(false);
  const [selectedDbService, setSelectedDbService] = useState({});
  const [editDbService, setEditDbService]  = useState(false);

  const [showConnectionalModal, setShowConnectionModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);
  
  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffsetTemp = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onPaginationNextClick = () => {
    const currentPageOffsetTemp = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffsetTemp);
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    getDbServices();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  // delete db service
  const deleteDbServiceContent = (
    <div>
      <h3>Are you sure you want to delete {selectedDbService.name}? </h3>
      <h5>It will delete the DB Service.</h5>
    </div>
  );

  const deleteDbServiceAccept = () => {
    ProgressIndicator.show();
    dbServiceApi
      .deleteFabricWorkspace(selectedDbService.id)
      .then(() => {
        getDbServices();
        Notification.show(`DB service ${selectedDbService.name} deleted successfully.`);
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
          if(res.status !== 204) {
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
            setDbServices(res?.data?.data);
            const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
            setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
            setTotalNumberOfPages(totalNumberOfPagesTemp);
          } else {
            setDbServices([]);
          }
          ProgressIndicator.hide();
        })
        .catch((e) => {
          ProgressIndicator.hide();
          Notification.show(
            e.response.data.errors?.length
              ? e.response.data.errors[0].message
              : 'Fetching DB services failed!',
            'alert',
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
        {dbservices?.length === 0 && 
          <div className={Styles.noProjectContainer}>
            <div className={Styles.messageContainer}>
              <p className={Styles.lead}>Hi <span>{user.firstName} {user.lastName}</span>, you don&apos;t have<br/>any DB service.</p>
              <p>You can create one</p>
            </div>
            <div className={Styles.btnContainer}>
              <button className={'btn btn-tertiary'} onClick={() => setCreateDbService(true)}>
                <span>Create now</span>
              </button>
            </div>
          </div>
        }
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
            {cardViewMode &&
              <>
                <div className={classNames(Styles.projectsContainer)}>
                  <div className={Styles.createNewCard} onClick={() => setCreateDbService(true)}>
                    <div className={Styles.addicon}> &nbsp; </div>
                    <label className={Styles.addlabel}>Create new DB Service</label>
                  </div>
                  {dbservices.map((dbservice) => 
                    <DBServiceCard
                      key={dbservice.id}
                      user={user}
                      dbservice={dbservice}
                      onSelectDbService={handleSelectDbService}
                      onEditDbService={(dbservice) => { 
                        setSelectedDbService(dbservice);
                        setEditDbService(true);}
                      }
                      onDeleteDbService={(dbservice) => {
                        setSelectedDbService(dbservice);
                        setDeleteModal(true);
                      }}
                      onShowDetailsModal={(dbservice) => {
                        setSelectedDbService(dbservice);
                        setShowDetailsModal(true);
                      }}
                    />
                  )}
                </div>
              </>
            }
            {listViewMode && 
              <div className={Styles.projectTable}>
                <div className={Styles.tableHeader}>
                  <div className={Styles.col1}>
                    <span>Name</span>
                  </div>
                  <div className={Styles.col2}>
                    <span>Permission</span>
                  </div>
                  <div className={Styles.col3}>
                    <span>Created on</span>
                  </div>
                  <div className={Styles.col4}>
                    <span>Last Modified on</span>
                  </div>
                  <div className={Styles.col5}>
                    <span>Data Classification</span>
                  </div>
                  <div className={Styles.col6}>
                    <span>Action</span>
                  </div>
                </div>
                {dbservices?.map((dbservice) => 
                  <DBServiceRow
                    key={dbservice.id}
                    user={user}
                    dbservice={dbservice}
                    onSelectDbService={(dbservice) => { 
                      setSelectedDbService(dbservice);
                      }
                    }
                    onEditDbService={(dbservice) => { 
                      setSelectedDbService(dbservice);
                      setEditDbService(true);}
                    }
                    onDeleteDbService={(dbservice) => {
                      setSelectedDbService(dbservice);
                      setDeleteModal(true);
                    }}
                    onShowDetailsModal={(dbservice) => {
                      setSelectedDbService(dbservice);
                      setShowDetailsModal(true);
                    }}
                  />
                )}
              </div>
            }
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
          </div>
        )}
      </div>
      { createDbService &&
        <Modal
          title={'Create DB Service'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={createDbService}
          content={<DBServiceForm user={user} edit={false} onSave={() => {setCreateDbService(false); getDbServices();}} />}
          scrollableContent={true}
          onCancel={() => setCreateDbService(false)}
        />
      }
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
      { editDbService &&
        <Modal
          title={'Edit DB Service'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={editDbService}
          content={<DBServiceForm edit={true} workspace={selectedDbService} onSave={() => {setEditDbService(false); getDbServices(); }} />}
          scrollableContent={true}
          onCancel={() => setEditDbService(false)}
        />
      }
      {showConnectionalModal && (
        <InfoModal
          title="Connect"
          modalCSS={Styles.header}
          show={showConnectionalModal}
          modalWidth={'800px'}
          content={<ConnectionModal user={user} />}
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
