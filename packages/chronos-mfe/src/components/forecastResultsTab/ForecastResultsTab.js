import classNames from 'classnames';
import React, { useState, useEffect, useRef } from 'react';
import Styles from './forecast-results-tab.scss';

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
import ForecastRunRow from './forecastRunRow/ForecastRunRow';
import { SESSION_STORAGE_KEYS } from '../../utilities/constants';

const ForecastResultsTab = ({ onRunClick }) => {
  const { id: projectId } = useParams();

  const [loading, setLoading] = useState(true);
  const [forecastRuns, setForecastRuns] = useState([]);

  const comparisonNameInput = useRef();
  const fileInput = useRef();

  // Pagination 
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentPageOffset, setCurrentPageOffset] = useState(0);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15);

  // compare
  const [showCompareModal, setShowCompareModal] = useState(false);
  const [actualsFile, setActualsFile] = useState('');

  const isValidFile = (file) => ['csv', 'xlsx'].includes(file?.name?.split('.')[1]);
  const onDrop = (e) => {
    const file = e.dataTransfer.files;
    const isValid = isValidFile(file?.[0]);
    if (!isValid) {
      Notification.show('File is not valid. Only .xlsx files allowed.', 'alert');
    } else {
      setActualsFile(file);
      console.log('hehe');
    }
  };
  const onFileDrop = (e) => {
    e.preventDefault();
    if (e.type === 'drop') {
      onDrop?.(e);
    }
  };

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

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
  
  const getProjectForecastRuns = () => {
    ProgressIndicator.show();
    chronosApi.getForecastRuns(projectId, currentPageOffset, maxItemsPerPage, sortBy.name, sortBy.currentSortType).then((res) => {
      if (res.status === 204) {
        setForecastRuns([]);
      } else {
        setForecastRuns(res.data.records);
        const totalNumberOfPagesTemp = Math.ceil(res.data.totalCount / maxItemsPerPage);
        setCurrentPageNumber(currentPageNumber > totalNumberOfPagesTemp ? 1 : currentPageNumber);
        setTotalNumberOfPages(totalNumberOfPagesTemp);
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
  
  // useEffect(() => {
  //   getProjectForecastRuns();
  // }, [sortBy]); // eslint-disable-line react-hooks/exhaustive-deps
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
    getProjectForecastRuns();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maxItemsPerPage, currentPageNumber, currentPageOffset, sortBy]);

  /* Delete */
  const [selectedRuns, setSelectedRuns] = useState([]);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [runToBeDeleted, setRunToBeDeleted] = useState();
  
  const removeSelected = () => {
    ProgressIndicator.show();
    chronosApi.deleteForecastRuns(selectedRuns, projectId)
      .then(() => {
        setSelectedRuns([]);
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

  const onSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData();
    // if(data.file !== undefined) {
    //   formData.append('actualsFile', '');
    // } else {
    //   if(data.file.length === 1) {
    //     formData.append('actualsFile', data.file[0]);
    //   } else {
    //     formData.append('actualsFile', data.droppedFile[0]);
    //   }
    // }

    if(actualsFile.length !== 0) {
      formData.append('actualsFile', actualsFile[0]);
    }
    formData.append('comparisonName', comparisonNameInput.current.value);
    formData.append('runCorelationIds', selectedRuns.toString());

    ProgressIndicator.show();
    chronosApi.createForecastComparison(formData, projectId).then(() => {
        Notification.show('Comparison created successfully');
        onRunClick();
        ProgressIndicator.hide();
        setActualsFile('');
      }).catch(error => {
        ProgressIndicator.hide();
        Notification.show(
          error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while creating comparison',
          'alert',
        );
        setActualsFile('');
      });
  }

  const handleCancelRun = (run) => {
    ProgressIndicator.show();
    chronosApi.cancelForecastRun(projectId, run.id).then(() => {
      Notification.show('Run cancelled successfully');
      getProjectForecastRuns();
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(error.message, 'alert');
      ProgressIndicator.hide();
    });
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
          <div className={Styles.forecastResultListWrapper}>
            <div className={Styles.listContent}>
              {loading && <Spinner />}
              {!loading && (
                forecastRuns?.length === 0 &&
                  <div className={Styles.forecastResultListEmpty}>Forecast Runs are not available</div>
              )}
              {!loading && forecastRuns?.length > 0 &&
                <React.Fragment>
                  <div className={Styles.refreshContainer}>
                    <button className={classNames('btn btn-primary', Styles.delBtn, selectedRuns.length > 1 && selectedRuns.length < 13 ? '' : Styles.disableBtn)} tooltip-data={'Compare Runs'} onClick={() => setShowCompareModal(true)}><i className="icon mbc-icon data-sharing"></i></button>
                    <button className='btn btn-primary' onClick={() => { getProjectForecastRuns(); }}>
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
                            onClick={() => sortResults('exogenData', sortBy.nextSortType)}
                            >
                            <label
                              className={
                                'sortable-column-header ' +
                                (sortBy.name === 'exogenData' ? sortBy.currentSortType : '')
                              }
                            >
                              <i className="icon sort" />
                              Exogenous Data
                            </label>
                          </th>
                          <th>
                            <label className={'sortable-column-header'}>
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
                              selectedRuns={selectedRuns}
                              selectRun={selectRun}
                              deselectRun={deselectRun}
                              onOpenErrorModal={handleOpenErrorModal}
                              onCancelRun={handleCancelRun}
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
        
        { showCompareModal &&
          <Modal
            title={''}
            showAcceptButton={false}
            showCancelButton={false}
            modalWidth={'40%'}
            buttonAlignment="center"
            show={showCompareModal}
            content={
              <div className={Styles.modalContent}>
                <div className={Styles.mHeader}>
                  <div className={Styles.circleOutline}>
                    <i className="icon mbc-icon data-sharing"></i> 
                  </div>
                  <h2>Compare Runs</h2>
                </div>
                <form>
                  <div className={Styles.infoBox}>
                    <i className="icon mbc-icon info"></i>
                    <div className={Styles.cap}>
                      <p>Use this feature to compare different forecasts. You can use this to evaluate the forecasts of a KPI over time, or to see upper/lower bounds of different forecasts. All selected forecasting runs should build on the same KPI, otherwise results might be meaningless.</p>
                      <p>Optionally, you can provide actuals data in the for form of a csv file to get an idea of how Chronos performed. This file should contain the complete history up to the most recent forecast.</p>
                    </div>
                  </div>
                  <div className={Styles.inputBox}>
                    <div className={classNames('input-field-group')}>
                      <label id="compareNameLabel" htmlFor="compareNameInput" className="input-label">
                        Comparison Name
                      </label>
                      <input
                        ref={comparisonNameInput}
                        type="text"
                        className="input-field"
                        id="compareNameInput"
                        maxLength={55}
                        placeholder="Type here..."
                        autoComplete="off"
                      />
                      {/* <span className={classNames('error-message')}>{errors.compareName?.type === 'pattern' && 'Compare names can consist only of lowercase letters, numbers, dots ( . ), and hyphens ( - ).'}</span> */}
                    </div>
                  </div>
                  <div className={Styles.fileUploadBox}>
                    
                    <div className={actualsFile.length !== 0 ? Styles.hide : ''}>
                      <p>Upload Actuals (optional)</p>
                      <div 
                        onDrop={onFileDrop}
                        onDragOver={onFileDrop}
                        onDragLeave={onFileDrop}
                        className={classNames('upload-container', Styles.uploadContainer)}
                      >
                        <input type="file" id="actualsfile" name="actualsfile" 
                          ref={fileInput}
                          onInputCapture={(e) => {
                            const isValid = isValidFile(e.target.files[0]);
                            if (!isValid) {
                              Notification.show('File is not valid. Only .xlsx files allowed.', 'alert');
                            } else {
                              setActualsFile(e.target.files);
                            }
                          }}
                          accept=".csv, .xlsx"
                          />
                        <div className={Styles.dragDrop}>
                          <div className={Styles.browseHelperText}>
                            Drag&apos;n&apos;Drop or <label htmlFor="actualsfile" className={Styles.selectExisitingFiles}>select</label> 
                          </div>
                        </div>
                      </div>
                    </div>
                      <div className={classNames(Styles.selectedFile, actualsFile.length === 0 ? Styles.hide : '')}>
                        <div>
                          <span>Actuals File</span>
                          <span>{actualsFile[0]?.name}</span>
                        </div>
                        <div className={Styles.msgContainer}>
                          <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} />
                          <span>File is ready to use.</span>
                        </div>
                      </div>
                  </div>
                  <div className={Styles.cenBtn}>
                    <button className={'btn btn-tertiary'} onClick={(e) => onSubmit(e)}>Run Comparison</button>
                  </div>
                </form>
              </div>
            }
            scrollableContent={false}
            onCancel={() => {
              setShowCompareModal(false)
            }}
            modalStyle={{
              padding: '50px 35px 35px 35px',
              minWidth: 'unset',
              width: '40%',
              maxWidth: '40vw'
            }}
          />
        }
    </React.Fragment>
  );
}
export default ForecastResultsTab;
