import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './additional-services-tab.scss';
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
import AddAdditionalServiceForm from './AddAdditionalServiceForm';
import AdditionalServiceRow from './AdditionalServiceRow';

const AdditionalServicesTab = () => {
  const [loading, setLoading] = useState(true);
  const [additionalServices, setAdditionalServices] = useState([]);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedAdditionalService, setSelectedAdditionalService] = useState({});
  const [showAddAdditionalServiceModal, setShowAddAdditionalServiceModal] = useState(false);
  const [showEditAdditionalServiceModal, setShowEditAdditionalServiceModal] = useState(false);

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const getAllAdditionalServices = () => {
    ProgressIndicator.show();
    setLoading(true);
    CodeSpaceApiClient.getAdditionalServicesLov()
      .then((res) => {
        setLoading(false);
        ProgressIndicator.hide();
        if(Array.isArray(res?.data?.data)) {
          const totalNumberOfPagesInner = Math.ceil(res?.data?.data?.length / maxItemsPerPage);
          setCurrentPageNumber(currentPageNumber > totalNumberOfPagesInner ? 1 : currentPageNumber);
          setTotalNumberOfPages(totalNumberOfPagesInner);
          setAdditionalServices(res?.data?.data);
        } else {
          setAdditionalServices([]);
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
  
  const startIndex = (currentPageNumber - 1) * maxItemsPerPage;
  const currentItems = additionalServices?.slice(startIndex, startIndex + maxItemsPerPage);

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
    getAllAdditionalServices();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  const handleAdditionalServiceDelete = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deleteCodeSpaceRecipe(selectedAdditionalService?.id)
      .then(() => {
        ProgressIndicator.hide();
        Notification.show("Recipe Deleted Successfully");
        setShowDeleteModal(false);
        getAllAdditionalServices();
      }).catch((err) => {
        ProgressIndicator.hide();
        Notification.show(err?.response?.data?.errors[0]?.message, 'alert');
      });
  }

  const handleCopyJson = (json) => {
    navigator.clipboard.writeText(JSON.stringify(json, null, 2)).then(() => {
      Notification.show('Copied to Clipboard');
    });
  }

  const moreInfoContent = (
    <div className={Styles.modal}>
      <div className={Styles.header}>
        <h3>{selectedAdditionalService?.serviceName} <span>v{selectedAdditionalService?.version}</span></h3>
        <div className={Styles.copyJSON} onClick={() => handleCopyJson(selectedAdditionalService?.additionalProperties)}><i className="icon mbc-icon copy"></i> Copy JSON</div>
      </div>
      <div className={Styles.content}>
        {selectedAdditionalService?.additionalProperties?.env &&
          <div className={classNames(Styles.itemContainer)}>
            <p className={classNames(Styles.itemTitle)}>Env(s)</p>
            {selectedAdditionalService?.additionalProperties?.env?.map(item => 
              <div key={item.name} className={classNames(Styles.item)}>
                <p className={classNames(Styles.name)}>
                  {item.name}
                </p>
                <p className={classNames(Styles.value)}>
                  {item.value}
                </p>
              </div>
            )}
          </div>
        }

        {selectedAdditionalService?.additionalProperties?.ports &&
          <div className={classNames(Styles.itemContainer)}>
            <p className={classNames(Styles.itemTitle)}>Port(s)</p>
            {selectedAdditionalService?.additionalProperties?.ports?.map((item, index) => {
              return ( 
                <div key={index} className={classNames(Styles.item)}>
                  <p className={classNames(Styles.name)}>
                    Protocol: {item.protocol}
                  </p>
                  <p className={classNames(Styles.value)}>
                    Container Port: {item.containerPort}
                  </p>
                </div>
              )
            })}
          </div>
        }
      </div>
      <div className={Styles.footer}>
        <button
          className="btn btn-tertiary"
          type="button"
          onClick={() => setShowDetailsModal(false)}
        >
          Okay
        </button>
      </div>
    </div>
  );

  return (
    <React.Fragment>
      <button className={classNames('btn btn-primary', Styles.actionBtn)} onClick={() => setShowAddAdditionalServiceModal(true)}>
        <i className="icon mbc-icon plus"></i>
        <span>&nbsp;Add New Additional Service</span>
      </button>
      <div className={Styles.content}>
        <div>
          <div>
            {!loading && (
              additionalServices?.length === 0 &&
                <div className={Styles.csempty}>Additional Services are not available. Add one.</div>
            )}
            {!loading && additionalServices?.length > 0 &&
              <>
                <div>
                  <table className={classNames('ul-table')}>
                    <thead>
                      <tr className={classNames('header-row')}>
                        {/* <th className={Styles.softwareColumn}>
                          <label>
                            ID
                          </label>
                        </th> */}
                        <th>
                          <label>
                            Service Name
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
                      {currentItems.map((service) =>
                        <AdditionalServiceRow
                          key={service?.id}
                          service={service}
                          onRefresh={getAllAdditionalServices}
                          onSelectAdditionalService={(additionalService) => { 
                            setSelectedAdditionalService(additionalService);
                            setShowDetailsModal(true);}
                          }
                          onEditAdditionalService={(additionalService) => {
                            setSelectedAdditionalService(additionalService);
                            setShowEditAdditionalServiceModal(true);
                          }}
                          onDeleteAdditionalService={(additionalService) => {
                            setSelectedAdditionalService(additionalService);
                            setShowDeleteModal(true);
                          }}
                        />
                      )}
                    </tbody>
                  </table>
                </div>
                {!loading && currentItems?.length > 0 &&
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
          title={'Software Details'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'800px'}
          buttonAlignment="right"
          show={showDetailsModal}
          content={moreInfoContent}
          scrollableContent={true}
          onCancel={() => setShowDetailsModal(false)}
        />
      )}
      {showAddAdditionalServiceModal && (
        <Modal
          title={'Add Additional Service'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'1100px'}
          buttonAlignment="right"
          show={showAddAdditionalServiceModal}
          content={<AddAdditionalServiceForm onAddAdditionalService={() => { setShowAddAdditionalServiceModal(false); getAllAdditionalServices() }} />}
          scrollableContent={true}
          onCancel={() => { setShowAddAdditionalServiceModal(false) }}
        />
      )}
      {showEditAdditionalServiceModal && (
        <Modal
          title={'Edit Additional Service'}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'1100px'}
          buttonAlignment="right"
          show={showEditAdditionalServiceModal}
          content={<AddAdditionalServiceForm edit={true} additionalService={selectedAdditionalService} onAddAdditionalService={() => { setShowEditAdditionalServiceModal(false); getAllAdditionalServices() }} />}
          scrollableContent={true}
          onCancel={() => { setShowEditAdditionalServiceModal(false) }}
        />
      )}
      {showDeleteModal && 
        <Modal
          title="Delete Additional Service"
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
                <p>The Additional service will be deleted permanently. Are you sure you want to delete it?</p>
              </div>
              <div className="btn-set footerRight">
                <button className="btn btn-primary" type="button" onClick={() => setShowDeleteModal(false)}>Cancel</button>
                <button className="btn btn-tertiary" type="button" onClick={handleAdditionalServiceDelete}>Confirm</button>
              </div>
            </div>
          } 
        />
      }
    </React.Fragment>
  );
}
export default AdditionalServicesTab;
