import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './forecast-results-tab.scss';

// Container Components
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';

import Notification from '../../common/modules/uilab/js/src/notification';

import { useHistory, useParams } from 'react-router-dom';
import { chronosApi } from '../../apis/chronos.api';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Spinner from '../spinner/Spinner';
import ForecastRunRow from './forecastRunRow/ForecastRunRow';

const ForecastResultsTab = () => {
  const { id: projectId } = useParams();

  const [loading, setLoading] = useState(true);
  const [forecastRuns, setForecastRuns] = useState([]);
  const [originalResults, setOriginalResults] = useState([]);
  useEffect(() => {
    getProjectForecastRuns();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectForecastRuns = () => {
    ProgressIndicator.show();
    chronosApi.getForecastRuns(projectId).then((res) => {
      if(res.status === 204) {
        setForecastRuns([]);
        setOriginalResults([]);
      } else {
        setForecastRuns(res.data.records);
        setOriginalResults([...res.data.records]);
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(
        error?.response?.data?.errors?.[0]?.message || 'Error while fetching forecast projects',
        'alert',
      );
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(15);

  /* getResults */
  const getResults = (action) => {
    const showProgressIndicator = ['pagination'].includes(action);

    showProgressIndicator && ProgressIndicator.show();

    let results = originalResults;

    if (sortBy) {
      if (sortBy.name === 'runName') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.runName.toLowerCase() === b.runName.toLowerCase() ? 0 : -1;
          } else {
            return a.runName.toLowerCase() === b.runName.toLowerCase() ? -1 : 0;
          }
        });
      } else if (sortBy.name === 'result_state') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.result_state.toLowerCase() === b.result_state.toLowerCase() ? 0 : -1;
          } else {
            return a.result_state.toLowerCase() === b.result_state.toLowerCase() ? -1 : 0;
          }
        });
      } else if (sortBy.name === 'triggeredOn') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.triggeredOn.toLowerCase() === b.triggeredOn.toLowerCase() ? 0 : -1;
          } else {
            return a.triggeredOn.toLowerCase() === b.triggeredOn.toLowerCase() ? -1 : 0;
          }
        });
      } else if (sortBy.name === 'triggeredBy') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.triggeredBy.toLowerCase() === b.triggeredBy.toLowerCase() ? 0 : -1;
          } else {
            return a.triggeredBy.toLowerCase() === b.triggeredBy.toLowerCase() ? -1 : 0;
          }
        });
      } else if (sortBy.name === 'inputFile') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.inputFile.toLowerCase() === b.inputFile.toLowerCase() ? 0 : -1;
          } else {
            return a.inputFile.toLowerCase() === b.inputFile.toLowerCase() ? -1 : 0;
          }
        });
      } else if (sortBy.name === 'forecastHorizon') {
        results = results.sort((a, b) => {
          if (sortBy.currentSortType === 'asc') {
            return a.forecastHorizon.toLowerCase() === b.forecastHorizon.toLowerCase() ? 0 : -1;
          } else {
            return a.forecastHorizon.toLowerCase() === b.forecastHorizon.toLowerCase() ? -1 : 0;
          }
        });
      }
    }

    setForecastRuns(
      results.slice(
        currentPageOffset > results.length ? 0 : currentPageOffset,
        currentPageOffset + maxItemsPerPage < results.length ? currentPageOffset + maxItemsPerPage : results.length,
      ),
    );
    setTotalNumberOfPages(Math.ceil(results.length / maxItemsPerPage) === 0 ? 1 : Math.ceil(results.length / maxItemsPerPage));
    setCurrentPageNumber(
      currentPageNumber > Math.ceil(results.length / maxItemsPerPage)
        ? Math.ceil(results.length / maxItemsPerPage) > 0
          ? Math.ceil(results.length / maxItemsPerPage)
          : 1
        : currentPageNumber,
    );
    showProgressIndicator && ProgressIndicator.hide();
  };

  useEffect(() => {
    getResults('pagination');
  }, [maxItemsPerPage, currentPageOffset, currentPageNumber]); // eslint-disable-line react-hooks/exhaustive-deps

  const onPaginationPreviousClick = () => {
    const currentPageNum = currentPageNumber - 1;
    const currentPageOffset = (currentPageNum - 1) * maxItemsPerPage;
    setCurrentPageNumber(currentPageNum);
    setCurrentPageOffset(currentPageOffset);
  };
  const onPaginationNextClick = () => {
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    setCurrentPageNumber(currentPageNumber + 1);
    setCurrentPageOffset(currentPageOffset);
  };
  const onViewByPageNum = (pageNum) => {
    setCurrentPageNumber(1);
    setCurrentPageOffset(0);
    setMaxItemsPerPage(pageNum);
  };

  /* Sort */
  const [sortBy, setSortBy] = useState({
    name: 'triggeredOn',
    currentSortType: 'asc',
    nextSortType: 'desc',
  });
  const sortResults = (propName, sortOrder) => {
    const tempSortBy = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: sortBy.currentSortType,
    };
    setSortBy(tempSortBy);
  };

  useEffect(() => {
    getResults('sort');
  }, [sortBy]); // eslint-disable-line react-hooks/exhaustive-deps

  /* Delete */
  const [checkAll, setCheckAll] = useState(false);
  const [selectedRuns, setSelectedRuns] = useState([]);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [runToBeDeleted, setRunToBeDeleted] = useState();
  
  const removeSelected = () => {
    ProgressIndicator.show();
    chronosApi.deleteForecastRuns(selectedRuns, projectId)
      .then(() => {
        setSelectedRuns([]);
        setCheckAll(false);
        getProjectForecastRuns();
        Notification.show('Run(s) deleted successfully.');
        ProgressIndicator.hide();
      })
      .catch(() => {
        Notification.show('Something went wrong', 'alert');
        ProgressIndicator.hide();
      });
  };

  const selectRun = (id) => {
    const tempNotifId = id.split('box-')[1];
    setSelectedRuns((prevArray) => [...prevArray, tempNotifId]);
  };

  const deselectRun = (id) => {
    const tempNotifId = id.split('box-')[1];
    setSelectedRuns(selectedRuns.filter((item) => item !== tempNotifId));
  };

  const unCheckAll = () => {
    setCheckAll(false);
  };

  const showDeleteConfirmModal = (run) => {
    setShowDeleteModal(true);
    setRunToBeDeleted(run);
  };
  const onCancelDelete = () => {
    setShowDeleteModal(false);
  };
  const onAcceptDelete = () => {
    if(selectedRuns.length > 0) {
      removeSelected();
    } else {
      if(runToBeDeleted.id !== '' || runToBeDeleted.id !== null) {
        ProgressIndicator.show();
        chronosApi.deleteForecastRun(projectId, runToBeDeleted.id).then((res) => {
          if(res.data.success === 'FAILED') {
            Notification.show(res?.data?.errors[0]?.message, 'alert');
            ProgressIndicator.hide();
          } else {
            Notification.show('Run deleted');
            ProgressIndicator.hide();
            getProjectForecastRuns();
          }
        }).catch(error => {
          Notification.show(
            error?.response?.data?.response?.errors[0]?.message || error?.response?.data?.response?.warnings[0]?.message || error?.response?.data?.errors[0]?.message || 'Error while deleting run',
            'alert',
          );
          ProgressIndicator.hide();
        });
      }
    }
    setShowDeleteModal(false);
  }

  /* Row actions */
  const history = useHistory();
  const openForecastingResults = (runId) => {
    history.push('/results/' + projectId + '/' + runId);
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
          <div>
            <div className={Styles.removeBlock} 
              onClick={showDeleteConfirmModal}
              >
                {checkAll || selectedRuns.length > 0 ? (
                  <React.Fragment>
                    <i className={classNames('icon delete')} />
                    Remove selected
                  </React.Fragment>
                ) : null }
            </div>
          </div>

          <div className={Styles.forecastResultListWrapper}>
            <div className={Styles.listContent}>
              {loading && <Spinner />}
              {!loading && (
                forecastRuns?.length === 0 &&
                  <div className={Styles.forecastResultListEmpty}>Forecast Runs are not available</div>
              )}
              {!loading && forecastRuns?.length > 0 &&
                <React.Fragment>
                  <div className={Styles.forecastResultList}>
                    <div className={Styles.refreshContainer}>
                      <button className='btn btn-primary' onClick={() => { getProjectForecastRuns(); }}>
                        <i className="icon mbc-icon refresh" />
                        <span>Refresh</span>   
                      </button>
                    </div>
                    <table className={'ul-table'}>
                      <thead>
                        <tr className="header-row">
                          {/* <th>
                            <div>
                              <label className={classNames('checkbox', Styles.checkboxItem)}>
                                <span className={classNames('wrapper', Styles.thCheckbox)}>
                                  <input
                                    type="checkbox"
                                    className="ff-only"
                                    checked={checkAll}
                                    onChange={(event) => onChangeSelectAll(event)}
                                  />
                                </span>
                              </label>
                            </div>
                          </th> */}
                          <th 
                            onClick={() => sortResults('runName', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' + 
                                (sortBy.name === 'runName' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Name
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('resultState', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'resultState' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Status
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('triggeredOn', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'triggeredOn' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Date / Time
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('triggeredBy', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'triggeredBy' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Ran by
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('inputFile', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'inputFile' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Input File
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('forecastHorizon', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'forecastHorizon' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Forecast Horizon
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('exogenousData', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'exogenousData' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Exogenous Data
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('hierarchy', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'hierarchy' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Levels of Hierarchy
                            </label>
                          </th>
                          <th>
                            &nbsp;
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {forecastRuns?.map((item) => {
                          return (
                            <ForecastRunRow
                              item={item}
                              key={item.id}
                              showDeleteConfirmModal={showDeleteConfirmModal}
                              openDetails={() => openForecastingResults(item.id)}
                              checkedAll={checkAll}
                              selectedRuns={selectedRuns}
                              selectRun={selectRun}
                              deselectRun={deselectRun}
                              unCheckAll={unCheckAll}
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
        {!loading && forecastRuns?.length > 0 &&
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
                    You are going to delete the Results.<br />
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
              title={errorItem.runName}
              showAcceptButton={false}
              showCancelButton={false}
              modalWidth={'60%'}
              buttonAlignment="right"
              show={showErrorModal}
              content={
                <div className={Styles.modalContent}>
                  {errorItem.state.result_state === 'WARNINGS' ? 
                    <div className={Styles.errorDiv}>
                      <i className={classNames('icon mbc-icon alert circle', Styles.alertCircle)} />
                      <span>{errorItem.warnings}</span>
                    </div> :
                    <div className={Styles.errorDiv}>
                      <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} />
                      <span>{errorItem.state.state_message}</span>
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
export default ForecastResultsTab;
