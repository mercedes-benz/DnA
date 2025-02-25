import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './software-tab.scss';
// Container Components
import Modal from 'dna-container/Modal';
// import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';
import Notification from '../../common/modules/uilab/js/src/notification';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import { CodeSpaceApiClient } from '../../apis/codespace.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { getQueryParameterByName } from '../../Utility/utils';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants';
import SoftwareRow from './SoftwareRow';
import AddSoftwareForm from './AddSoftwareForm';

const SoftwareTab = () => {
  const [loading, setLoading] = useState(true);
  const [software, setSoftware] = useState([]);
  const [selectedSoftware, setSelectedSoftware] = useState();
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showAddSoftwareModal, setShowAddSoftwareModal] = useState(false);
  const [showEditSoftwareModal, setShowEditSoftwareModal] = useState(false);

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const getAllSoftware = () => {
    ProgressIndicator.show();
    setLoading(true);
    CodeSpaceApiClient.getAllSoftware()
      .then((res) => {
        setLoading(false);
        ProgressIndicator.hide();
        if(Array.isArray(res?.data?.data)) {
          const totalNumberOfPagesInner = Math.ceil(res?.data?.count / maxItemsPerPage);
          setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
          setTotalNumberOfPages(totalNumberOfPagesInner);
          setSoftware(res?.data?.data);
        } else {
          setSoftware([]);
        }
      })
      .catch((err) => {
        setLoading(false);
        ProgressIndicator.hide();
        Notification.show(err?.message || 'Something went wrong.', 'alert');
      });
  };

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

  useEffect(() => {
    const pageNumberOnQuery = getQueryParameterByName('page');
    const currentPageNumberTemp = pageNumberOnQuery ? parseInt(getQueryParameterByName('page'), 10) : 1;
    const currentPageOffsetTemp = pageNumberOnQuery ? (currentPageNumberTemp - 1) * maxItemsPerPage : 0;
    setCurrentPageOffset(currentPageOffsetTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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
    getAllSoftware();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const handleSoftwareDelete = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deleteSoftware(selectedSoftware?.id)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show("Software deleted successfully");
        setShowDeleteModal(false);
        getAllSoftware();
      }).catch((err) => {
        ProgressIndicator.hide();
        Notification.show(err?.response?.data?.errors[0]?.message, 'alert');
      });
  }

  return (
    <React.Fragment>
      <button className={classNames('btn btn-primary', Styles.actionBtn)} onClick={() => setShowAddSoftwareModal(true)}>
        <i className="icon mbc-icon plus"></i>
        <span>&nbsp;Add New Software</span>
      </button>
      <div className={Styles.content}>
        <div>
          <div>
            {!loading && (
              software?.length === 0 &&
                <div className={Styles.empty}>Software are not available</div>
            )}
            {!loading && software?.length > 0 &&
              <>
                <div>
                  <table className={classNames('ul-table')}>
                    <thead>
                      <tr className={classNames('header-row')}>
                        <th className={Styles.softwareColumn}>
                          <label>
                            ID
                          </label>
                        </th>
                        <th>
                          <label>
                            Software Name
                          </label>
                        </th>
                        <th className={Styles.softwareColumn} >
                          <label>
                            Created by
                          </label>
                        </th>
                        <th className={Styles.actionColumn}>
                          <label>Action</label>
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {software.map((softwareItem) =>
                        <SoftwareRow
                          key={softwareItem?.id}
                          software={softwareItem}
                          onRefresh={getAllSoftware}
                          onSelectSoftware={(software) => { 
                            setSelectedSoftware(software);
                            setShowDetailsModal(true);}
                          }
                          onEditSoftware={(software) => {
                            setSelectedSoftware(software);
                            setShowEditSoftwareModal(true);
                          }}
                          onDeleteSoftware={(software) => {
                            setSelectedSoftware(software);
                            setShowDeleteModal(true);
                          }}
                        />
                      )}
                    </tbody>
                  </table>
                </div>
                {!loading && software?.length > 0 &&
                  <Pagination
                    totalPages={totalNumberOfPages}
                    pageNumber={currentPageNumber}
                    onPreviousClick={onPaginationPreviousClick}
                    onNextClick={onPaginationNextClick}
                    onViewByNumbers={onViewByPageNum}
                    displayByPage={true}
                  />
                }
              </>
            }
          </div>
        </div>
      </div>
      {showDetailsModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="60vw"
          show={showDetailsModal}
          scrollableContent={true}
          content={'Details'}
          onCancel={() => {
            setShowDetailsModal(false);
          }}
        />
      )}
      {showAddSoftwareModal && (
        <Modal
          title={'Add Software'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'1100px'}
          buttonAlignment="right"
          show={showAddSoftwareModal}
          content={<AddSoftwareForm onAddSoftware={() => { setShowAddSoftwareModal(false); getAllSoftware() }} />}
          scrollableContent={true}
          onCancel={() => { setShowAddSoftwareModal(false) }}
        />
      )}
      {showEditSoftwareModal && (
        <Modal
          title={'Edit Software'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'1100px'}
          buttonAlignment="right"
          show={showEditSoftwareModal}
          content={<AddSoftwareForm edit={true} software={selectedSoftware} onAddSoftware={() => { setShowEditSoftwareModal(false); getAllSoftware() }} />}
          scrollableContent={true}
          onCancel={() => { setShowEditSoftwareModal(false) }}
        />
      )}
      {showDeleteModal && 
        <Modal
          title="Delete Software"
          show={showDeleteModal}
          showAcceptButton={false}
          showCancelButton={false}
          scrollableContent={false}
          hideCloseButton={true}
          content={
            <div>
              <header>
                <button className="modal-close-button" onClick={() => setShowDeleteModal(false)}><i className="icon mbc-icon close thin"></i></button>
              </header>
              <div>
                <p>The Software will be deleted permanently. Are you sure you want to delete it?</p>
              </div>
              <div className="btn-set footerRight">
                <button className="btn btn-primary" type="button" onClick={() => setShowDeleteModal(false)}>Cancel</button>
                <button className="btn btn-tertiary" type="button" onClick={handleSoftwareDelete}>Confirm</button>
              </div>
            </div>
          } 
        />
      }
    </React.Fragment>
  );
}
export default SoftwareTab;
