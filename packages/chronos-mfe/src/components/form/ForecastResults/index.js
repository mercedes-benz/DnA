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

  /* Pagination */
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  useEffect(() => {
    setTotalNumberOfPages(1);
    setCurrentPageNumber(1);
  }, []);
  const onPaginationPreviousClick = () => {
    /* previous click */
  };
  const onPaginationNextClick = () => {
    /* next click */
  };
  const onViewByPageNum = (pageNum) => {
    /* page number click */
    console.log(pageNum);
  };

  /* Sort */
  const [sortBy, setSortBy] = useState({
    name: 'name',
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
        setForecastRuns(res.records);
      }
      setLoading(false);
      ProgressIndicator.hide();
    }).catch(error => {
      console.log(error.message);
      setLoading(false);
      ProgressIndicator.hide();
    });
  };

  /* Delete */
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const showDeleteConfirmModal = (item) => {
    setShowDeleteModal(true);
    console.log(item);
    ProgressIndicator.show();
    chronosApi.deleteForecastRun(projectId, item.runId).then((res) => {
      // setProject(res);
      console.log(res);
      ProgressIndicator.hide();
    }).catch(error => {
      console.log(error.message);
      ProgressIndicator.hide();
    });
  };
  const onCancelDelete = () => {
    setShowDeleteModal(false);
  };
  const onAcceptDelete = () => {
    setShowDeleteModal(false);
    Notification.show('Run deleted');
  }

  /* Row actions */
  const history = useHistory();
  const openForecastingResults = () => {
    history.push('/results');
  }
  return (
    <React.Fragment>
        <div className={Styles.content}>
          {/* <div>
            <div className={Styles.removeBlock} 
              onClick={openDeleteModal}
              >
                {checkAll || selectedNotifications.length > 0 ? (
                  <React.Fragment>
                    <i className={classNames('icon delete')} />
                    Remove selected
                  </React.Fragment>
                ) : (
                  ''
                )}
            </div>
          </div> */}

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
                                    // checked={checkAll}
                                    // onChange={(event) => onChangeSelectAll(event)}
                                  />
                                </span>
                              </label>
                            </div>
                          </th>
                          <th 
                            onClick={() => sortResults('name', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' + 
                                (sortBy.name === 'name' ? sortBy.currentSortType : '')
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
                            onClick={() => sortResults('datetime', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'datetime' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Date / Time
                            </label>
                          </th>
                          <th 
                            onClick={() => sortResults('ranBy', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'ranBy' ? sortBy.currentSortType : '')
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
                              openDetails={openForecastingResults}
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
