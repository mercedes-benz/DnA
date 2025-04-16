import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './DeployAuditLogsModal.scss';
import Modal from 'dna-container/Modal';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import Pagination from 'dna-container/Pagination';
import SelectBox from 'dna-container/SelectBox';
import { Envs } from '../../Utility/envs';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants.js';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import { CodeSpaceApiClient } from '../../apis/codespace.api';

const DeployAuditLogsModal = (props) => {
  const [deployLogs, setDeployLogs] = useState([]);
  const [actionLogs, setActionLogs] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [currentSelection, setCurrentSelection] = useState('All');
  const totalNumberOfRecords = auditLogs.length;
  const totalNumberOfDeployRecords = deployLogs.length;
  const totalNumberOfActionRecords = actionLogs.length;
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [totalNumberOfDeployPages, setTotalNumberOfDeployPages] = useState(1);
  const [totalNumberOfActionPages, setTotalNumberOfActionPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentDeployPageNumber, setCurrentDeployPageNumber] = useState(1);
  const [currentActionPageNumber, setCurrentActionPageNumber] = useState(1);
  const [paginatedRecords, setPaginatedRecords] = useState([]);
  const [deployPaginatedRecords, setDeployPaginatedRecords] = useState([]);
  const [actionPaginatedRecords, setActionPaginatedRecords] = useState([]);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.AUDIT_LOGS_MAX_ITEMS_PER_PAGE), 10) || 5);

  useEffect(() => {
    ProgressIndicator.show();
    CodeSpaceApiClient.getBuildAndDeployLogs(props?.projectName)
      .then((res) => { 
        if(props?.deployedEnvInfo === 'Staging'){
          setAuditLogs([...(res?.data?.data?.intDeploymentAuditLogs ?? [])].reverse());
        }
        else{
          setAuditLogs([...(res?.data?.data?.prodDeploymentAuditLogs ?? [])].reverse());
        }
        ProgressIndicator.hide();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('Error in getting build audit logs - ' + err.message, 'alert');
      });
  }, []);// eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    let actionLogs = [];
    let deployLogs = [];
    auditLogs.forEach((item) => {
      if (!item.branch) {
        actionLogs.push(item);
      } else {
        deployLogs.push(item);
      }
    });
    setActionLogs(actionLogs);
    setDeployLogs(deployLogs);
    setTotalNumberOfPages(Math.ceil(auditLogs.length / maxItemsPerPage));
    setPaginatedRecords(auditLogs.slice(0, (0 + maxItemsPerPage)));
    setCurrentPageNumber(1);
    setTotalNumberOfDeployPages(Math.ceil(deployLogs.length / maxItemsPerPage));
    setDeployPaginatedRecords(deployLogs.slice(0, (0 + maxItemsPerPage)));
    setCurrentDeployPageNumber(1);
    setTotalNumberOfActionPages(Math.ceil(actionLogs.length / maxItemsPerPage));
    setActionPaginatedRecords(actionLogs.slice(0, (0 + maxItemsPerPage)));
    setCurrentActionPageNumber(1);
    SelectBox.defaultSetup();
  },[auditLogs]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = auditLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };

  const onDeployPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentDeployPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = deployLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentDeployPageNumber(currentPageNumberTemp);
    setDeployPaginatedRecords(modifiedData);
  };

  const onActionPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentActionPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = actionLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentActionPageNumber(currentPageNumberTemp);
    setActionPaginatedRecords(modifiedData);
  };

  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = auditLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };

  const onDeployPaginationNextClick = () => {
    let currentPageNumberTemp = currentDeployPageNumber;
    const currentPageOffset = currentDeployPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = deployLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentDeployPageNumber(currentPageNumberTemp);
    setDeployPaginatedRecords(modifiedData);
  };

  const onActionPaginationNextClick = () => {
    let currentPageNumberTemp = currentActionPageNumber;
    const currentPageOffset = currentActionPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = actionLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentActionPageNumber(currentPageNumberTemp);
    setActionPaginatedRecords(modifiedData);
  };

  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(auditLogs?.length / pageNum);
    const modifiedData = auditLogs.slice(0, pageNum);
    setPaginatedRecords(modifiedData);
    setTotalNumberOfPages(totalNumberOfPages);
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
    const totalNumberOfDeployPages = Math.ceil(deployLogs?.length / pageNum);
    const modifiedDeployData = deployLogs.slice(0, pageNum);
    setDeployPaginatedRecords(modifiedDeployData);
    setTotalNumberOfDeployPages(totalNumberOfDeployPages);
    setCurrentDeployPageNumber(1);
    const totalNumberOfActionPages = Math.ceil(actionLogs?.length / pageNum);
    const modifiedActionData = actionLogs.slice(0, pageNum);
    setActionPaginatedRecords(modifiedActionData);
    setTotalNumberOfActionPages(totalNumberOfActionPages);
    setCurrentActionPageNumber(1);
  };

  return (
    <Modal
      title={
        (currentSelection === 'Action'
          ? 'Action Audit Logs - '
          : currentSelection === 'Deploy'
          ? 'Deployment Audit Logs - '
          : 'Deployment & Action Audit Logs - ') + props.deployedEnvInfo
      }
      hiddenTitle={false}
      showAcceptButton={false}
      showCancelButton={false}
      modalWidth={'70%'}
      modalStyle={{ minHeight: '86%' }}
      buttonAlignment="center"
      show={props.show}
      content={
        <>
          <div className={classNames(Styles.allCodeSpace)}>
            <div className={Styles.flexLayout}>
              <div className="input-field-group">
                <label className="input-label">Type Of Logs</label>

                <div className="custom-select">
                  <select
                    id="logsField"
                    required={false}
                    onChange={(e) => {
                      setCurrentSelection(e.currentTarget.value);
                    }}
                    value={currentSelection}
                  >
                    <option value={'All'}>All</option>
                    <option value={'Deploy'}>Deploy logs</option>
                    <option value={'Action'}>Action logs</option>
                  </select>
                </div>
              </div>
              <div></div>
            </div>
            <div className={classNames(Styles.allcodeSpaceListviewContent)}>
              <table className={classNames('ul-table solutions', Styles.codeSpaceMargininone)}>
                <thead>
                  <tr className={classNames('header-row', Styles.codeSpaceRow)}>
                    <th>
                      <label>Branch</label>
                    </th>
                    <th>
                      <label>Deployed On</label>
                    </th>
                    <th>
                      <label>Triggered By</label>
                    </th>
                    <th>
                      <label>Triggered On</label>
                    </th>
                    <th>
                      <label>Deployment Status</label>
                    </th>
                    <th>
                      <label>Last Commit</label>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {(currentSelection === 'Deploy'
                    ? deployPaginatedRecords
                    : currentSelection === 'Action'
                    ? actionPaginatedRecords
                    : paginatedRecords) &&
                    (currentSelection === 'Deploy'
                      ? deployPaginatedRecords
                      : currentSelection === 'Action'
                      ? actionPaginatedRecords
                      : paginatedRecords
                    ).map((item, index) => (
                      <tr className={classNames('data-row')} key={index}>
                        <td>{item?.branch}</td>
                        <td>{item?.deployedOn && regionalDateAndTimeConversionSolution(item?.deployedOn)}</td>
                        <td>{item?.triggeredBy}</td>
                        <td>{regionalDateAndTimeConversionSolution(item?.triggeredOn)}</td>
                        <td>{item?.deploymentStatus}</td>
                        <td>
                          {item?.commitId ? (
                            <a
                              href={`${Envs.CODE_SPACE_GIT_PAT_APP_URL}/${Envs.CODE_SPACE_GIT_ORG_NAME}/${props.projectName}/commit/${item.commitId}`}
                              target="_blank"
                              rel="noreferrer"
                            >
                              {item.commitId}
                            </a>
                          ) : (
                            'N/A'
                          )}
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          </div>
          {currentSelection === 'Deploy' ? (
            totalNumberOfDeployRecords ? (
              <Pagination
                totalPages={totalNumberOfDeployPages}
                pageNumber={currentDeployPageNumber}
                onPreviousClick={onDeployPaginationPreviousClick}
                onNextClick={onDeployPaginationNextClick}
                onViewByNumbers={onViewByPageNum}
                displayByPage={true}
                startWithFive={true}
              />
            ) : null
          ) : currentSelection === 'Action' ? (
            totalNumberOfActionRecords ? (
              <Pagination
                totalPages={totalNumberOfActionPages}
                pageNumber={currentActionPageNumber}
                onPreviousClick={onActionPaginationPreviousClick}
                onNextClick={onActionPaginationNextClick}
                onViewByNumbers={onViewByPageNum}
                displayByPage={true}
                startWithFive={true}
              />
            ) : null
          ) : totalNumberOfRecords ? (
            <Pagination
              totalPages={totalNumberOfPages}
              pageNumber={currentPageNumber}
              onPreviousClick={onPaginationPreviousClick}
              onNextClick={onPaginationNextClick}
              onViewByNumbers={onViewByPageNum}
              displayByPage={true}
              startWithFive={true}
            />
          ) : null}
        </>
      }
      scrollableContent={true}
      onCancel={() => props.setShowAuditLogsModal(false)}
    />
  );
};

export default DeployAuditLogsModal;
