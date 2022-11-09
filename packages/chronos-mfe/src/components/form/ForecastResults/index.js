import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './styles.scss';

// Container Components
import ConfirmModal from 'dna-container/ConfirmModal';
import Pagination from 'dna-container/Pagination';

import Notification from '../../../common/modules/uilab/js/src/notification';

import RowItem from './rowItem/RowItem';
import { useHistory, useParams } from 'react-router-dom';
import { chronosApi } from '../../../apis/chronos.api';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Spinner from '../../shared/spinner/Spinner';

const ForecastResults = () => {
  const { id: projectId } = useParams();

  const [loading, setLoading] = useState(true);
  const [forecastRuns, setForecastRuns] = useState([]);
  useEffect(() => {
    getProjectForecastRuns();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectForecastRuns = () => {
    ProgressIndicator.show();
    chronosApi.getForecastRuns(projectId).then((res) => {
      if(res.status === 204) {
        setForecastRuns([]);
      } else {
        setForecastRuns(res.data.records);
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
  const getResults = async (action) => {
    const showProgressIndicator = ['pagination'].includes(action);
    console.log(action);

    showProgressIndicator && ProgressIndicator.show();

    let results = [];

    await chronosApi.getForecastRuns(projectId)
      .then((res) => {
        if(res.status === 204) {
          results = [];
        } else {
          if (res.data.records) {
            results = [...res.data.records];
          }
        }
        setLoading(false);
        ProgressIndicator.hide();
      })
      .catch((err) => {
        Notification.show(
          err?.response?.data?.errors?.[0]?.message || 'Error while fetching forecast projects',
          'alert',
        );
        setForecastRuns([]);
      });

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
    name: 'runName',
    currentSortType: 'desc',
    nextSortType: 'asc',
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

  const onChangeSelectAll = (event) => {
    if (event.currentTarget.checked) {
      setSelectedRuns(
        forecastRuns.filter((item) => item.state.result_state !== null).map((item) => item.id),
      );
      setCheckAll(!checkAll);
    } else {
      setSelectedRuns([]);
      setCheckAll(!checkAll);
    }
  };
  
  const removeSelected = () => {
    ProgressIndicator.show();
    chronosApi.deleteForecastRuns(selectedRuns, projectId)
      .then((res) => {
        console.log(res);
        setSelectedRuns([]);
        setCheckAll(false);
        getProjectForecastRuns();
        Notification.show('Run(s) deleted successfully.');
        ProgressIndicator.hide();
      })
      .catch((err) => {
        console.log(err);
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
          console.log(res);
          Notification.show('Run deleted');
          ProgressIndicator.hide();
          getProjectForecastRuns();
        }).catch(error => {
          Notification.show(
            error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || error?.response?.data?.errors?.[0]?.message || 'Error while creating forecast project',
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
                      <button className='btn btn-primary' onClick={() => getResults('pagination')}>
                        <i className="icon mbc-icon refresh" />
                        <span>Refresh</span>   
                      </button>
                    </div>
                    <table className={'ul-table'}>
                      <thead>
                        <tr className="header-row">
                          <th>
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
                          </th>
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
                          <th>
                            &nbsp;
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {forecastRuns?.map((item) => {
                          return (
                            <RowItem
                              item={item}
                              key={item.id}
                              showDeleteConfirmModal={showDeleteConfirmModal}
                              openDetails={() => openForecastingResults(item.id)}
                              checkedAll={checkAll}
                              selectedRuns={selectedRuns}
                              selectRun={selectRun}
                              deselectRun={deselectRun}
                              unCheckAll={unCheckAll}
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
    </React.Fragment>
  );
}
export default ForecastResults;
