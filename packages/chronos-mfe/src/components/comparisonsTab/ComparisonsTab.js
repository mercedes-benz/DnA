import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './comparisons-tab.scss';

// Container Components
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';

import Notification from '../../common/modules/uilab/js/src/notification';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

import { useHistory, useParams } from 'react-router-dom';
import { chronosApi } from '../../apis/chronos.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Spinner from '../spinner/Spinner';
import { getQueryParameterByName } from '../../utilities/utils';
import ComparisonRow from './comparisonRow/ComparisonRow';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';

const ComparisonsTab = () => {
  const { id: projectId } = useParams();

  const [loading, setLoading] = useState(true);
  const [forecastComparisons, setForecastComparisons] = useState([]);

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

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

  /* Sort */
  const [sortBy, setSortBy] = useState({
    name: 'createdOn',
    currentSortType: 'desc',
    nextSortType: 'asc',
  });
  
  const getProjectForecastComparisons = () => {
    ProgressIndicator.show();
    chronosApi.getForecastComparisons(projectId, currentPageOffset, maxItemsPerPage, sortBy.name, sortBy.currentSortType).then((res) => {
      if (res.status === 204) {
        setForecastComparisons([]);
      } else {
        setForecastComparisons(res.data.records);
        // setForecastComparisons(comparisons.records);
        const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
        setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
        setTotalNumberOfPages(totalNumberOfPagesTemp);
      }

      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(
        error?.response?.data?.errors?.[0]?.message || 'Error while fetching run comparisons',
        'alert',
      );
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  useEffect(() => {
    getProjectForecastComparisons();
  }, [sortBy]); // eslint-disable-line react-hooks/exhaustive-deps
  const sortResults = (propName, sortOrder) => {
    setSortBy({
      name: propName,
      currentSortType: sortOrder,
      nextSortType: sortBy.currentSortType,
    });
  };

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
    getProjectForecastComparisons();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset]);

  /* Delete */
  const [selectedComparisons, setSelectedComparisons] = useState([]);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  
  const removeSelected = () => {
    ProgressIndicator.show();
    chronosApi.deleteForecastComparisons(selectedComparisons.toString(), projectId)
      .then(() => {
        setSelectedComparisons([]);
        getProjectForecastComparisons();
        Notification.show('Comparison(s) deleted successfully.');
        ProgressIndicator.hide();
      })
      .catch(() => {
        Notification.show('Something went wrong', 'alert');
        ProgressIndicator.hide();
      });
  };

  const selectComparison = (id) => {
    const tempComId = id.split('box-')[1];
    setSelectedComparisons((prevArray) => [...prevArray, tempComId]);
  };

  const deselectComparison = (id) => {
    const tempComId = id.split('box-')[1];
    setSelectedComparisons(selectedComparisons.filter((item) => item !== tempComId));
  };

  const showDeleteConfirmModal = () => {
    setShowDeleteModal(true);
  };
  const onCancelDelete = () => {
    setShowDeleteModal(false);
  };
  const onAcceptDelete = () => {
    if(selectedComparisons.length > 0) {
      removeSelected();
    }
    setShowDeleteModal(false);
  }

  /* Row actions */
  const history = useHistory();
  const openComparisonResults = (comparisonId) => {
    history.push('/comparison-results/' + projectId + '/' + comparisonId);
  }

  // Error Modal
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [errorItem, setErrorItem] = useState();
  const handleOpenErrorModal = (item) => {
    setShowErrorModal(true);
    setErrorItem({...item});
  }

  return (
    <React.Fragment>
        <div className={Styles.content}>
          <div className={Styles.forecastResultListWrapper}>
            <div className={Styles.listContent}>
              {loading && <Spinner />}
              {!loading && (
                forecastComparisons?.length === 0 &&
                  <div className={Styles.forecastResultListEmpty}>Forecast Comparisons are not available</div>
              )}
              {!loading && forecastComparisons?.length > 0 &&
                <React.Fragment>
                  <div className={Styles.refreshContainer}>
                    { selectedComparisons.length > 0 ?
                      <button className={classNames('btn btn-primary', Styles.delBtn)} tooltip-data={'Delete'} onClick={() => setShowDeleteModal(true)}><i className="icon delete"></i></button> : <div>&nbsp;</div>
                    }
                    <button className='btn btn-primary' onClick={() => { getProjectForecastComparisons(); }}>
                      <i className="icon mbc-icon refresh" />
                      <span>Refresh</span>   
                    </button>
                  </div>
                  <div className={Styles.forecastResultList}>
                    <table className={'ul-table'}>
                      <thead>
                        <tr className="header-row">
                          <th className={Styles.checkboxCol}>&nbsp;</th>
                          <th 
                            onClick={() => sortResults('comparisonName', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' + 
                                (sortBy.name === 'comparisonName' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Name
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('status', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'status' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Status
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('createdOn', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'createdOn' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Date / Time
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('createdBy', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'createdBy' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Ran by
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('actualsFile', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'actualsFile' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Actuals File
                            </label>
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {forecastComparisons?.map((item) => {
                          return (
                            <ComparisonRow
                              item={item}
                              key={item.comparisonId}
                              showDeleteConfirmModal={showDeleteConfirmModal}
                              openDetails={() => openComparisonResults(item.comparisonId)}
                              selectedComparisons={selectedComparisons}
                              selectComparison={selectComparison}
                              deselectComparison={deselectComparison}
                              onOpenErrorModal={handleOpenErrorModal}
                            />
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </React.Fragment>
              }
            </div>
          </div>
        </div>
        {!loading && forecastComparisons?.length > 0 &&
          <Pagination
            totalPages={totalNumberOfPages}
            pageNumber={currentPageNumber}
            onPreviousClick={onPaginationPreviousClick}
            onNextClick={onPaginationNextClick}
            onViewByNumbers={onViewByPageNum}
            displayByPage={true}
          />
        }
        {
          showDeleteModal && (
            <ConfirmModal
              title={'Delete'}
              showAcceptButton={false}
              showCancelButton={false}
              show={showDeleteModal}
              removalConfirmation={true}
              showIcon={false}
              showCloseIcon={true}
              content={
                <div className={Styles.deleteForecastResult}>
                  <div className={Styles.closeIcon}>
                    <i className={classNames('icon mbc-icon close thin')} />
                  </div>
                  <div>
                    You are going to delete the comparisons.<br />
                    Are you sure you want to proceed?
                  </div>
                  <div className={Styles.deleteBtn}>
                    <button
                      className={'btn btn-secondary'}
                      type="button"
                      onClick={onAcceptDelete}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              }
              onCancel={onCancelDelete}
            />
          )
        }
        { showErrorModal &&
            <Modal
              title={errorItem.comparisonName}
              showAcceptButton={false}
              showCancelButton={false}
              modalWidth={'60%'}
              buttonAlignment="right"
              show={showErrorModal}
              content={
                <div className={Styles.modalContent}>
                  {errorItem.state.lifeCycleState === 'WARNINGS' ? 
                    <div className={Styles.errorDiv}>
                      <i className={classNames('icon mbc-icon alert circle', Styles.alertCircle)} />
                      <span>{errorItem.state.stateMessage}</span>
                    </div> :
                    <div className={Styles.errorDiv}>
                      <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} />
                      <span>{errorItem.state.stateMessage}</span>
                    </div>
                  }
                </div>
              }
              scrollableContent={false}
              onCancel={() => {
                setShowErrorModal(false)
              }}
              modalStyle={{
                padding: '50px 35px 35px 35px',
                minWidth: 'unset',
                width: '60%',
                maxWidth: '50vw'
              }}
            />
        }
    </React.Fragment>
  );
}
export default ComparisonsTab;
