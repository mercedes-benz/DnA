import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './DeployAuditLogsModal.scss';
import Modal from 'dna-container/Modal';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import Pagination from 'dna-container/Pagination';
import SelectBox from 'dna-container/SelectBox';
import { Envs } from '../../Utility/envs';

const DeployAuditLogsModal = (props) => {
  let deployLogs = [];
  let actionLogs = [];
  const auditLogs = [...props.logsList].reverse();
  auditLogs.forEach((item) => {
    if (!item.branch) {
      actionLogs.push(item);
    } else {
      deployLogs.push(item);
    }
  });
  const [currentSelection, setCurrentSelection] = useState('All');
  const totalNumberOfRecords = auditLogs.length;
  const totalNumberOfDeployRecords = deployLogs.length;
  const totalNumberOfActionRecords = actionLogs.length;
  const totalNumberOfPages = Math.ceil(auditLogs.length / 5);
  const totalNumberOfDeployPages = Math.ceil(deployLogs.length / 5);
  const totalNumberOfActionPages = Math.ceil(actionLogs.length / 5);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [currentDeployPageNumber, setCurrentDeployPageNumber] = useState(1);
  const [currentActionPageNumber, setCurrentActionPageNumber] = useState(1);
  const [paginatedRecords, setPaginatedRecords] = useState([]);
  const [deployPaginatedRecords, setDeployPaginatedRecords] = useState([]);
  const [actionPaginatedRecords, setActionPaginatedRecords] = useState([]);
  console.log('records:', paginatedRecords);

  useEffect(() => {
    setPaginatedRecords(auditLogs.slice(0, 5));
    setDeployPaginatedRecords(deployLogs.slice(0, 5));
    setActionPaginatedRecords(actionLogs.slice(0, 5));
    SelectBox.defaultSetup();
  }, []);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * 5;
    const modifiedData = auditLogs.slice(currentPageOffset, 5 * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };

  const onDeployPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentDeployPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * 5;
    const modifiedData = deployLogs.slice(currentPageOffset, 5 * currentPageNumberTemp);
    setCurrentDeployPageNumber(currentPageNumberTemp);
    setDeployPaginatedRecords(modifiedData);
  };

  const onActionPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentActionPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * 5;
    const modifiedData = actionLogs.slice(currentPageOffset, 5 * currentPageNumberTemp);
    setCurrentActionPageNumber(currentPageNumberTemp);
    setActionPaginatedRecords(modifiedData);
  };

  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * 5;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = auditLogs.slice(currentPageOffset, 5 * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };

  const onDeployPaginationNextClick = () => {
    let currentPageNumberTemp = currentDeployPageNumber;
    const currentPageOffset = currentDeployPageNumber * 5;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = deployLogs.slice(currentPageOffset, 5 * currentPageNumberTemp);
    setCurrentDeployPageNumber(currentPageNumberTemp);
    setDeployPaginatedRecords(modifiedData);
  };

  const onActionPaginationNextClick = () => {
    let currentPageNumberTemp = currentActionPageNumber;
    const currentPageOffset = currentActionPageNumber * 5;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = actionLogs.slice(currentPageOffset, 5 * currentPageNumberTemp);
    setCurrentActionPageNumber(currentPageNumberTemp);
    setActionPaginatedRecords(modifiedData);
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
                displayByPage={false}
              />
            ) : null
          ) : currentSelection === 'Action' ? (
            totalNumberOfActionRecords ? (
              <Pagination
                totalPages={totalNumberOfActionPages}
                pageNumber={currentActionPageNumber}
                onPreviousClick={onActionPaginationPreviousClick}
                onNextClick={onActionPaginationNextClick}
                displayByPage={false}
              />
            ) : null
          ) : totalNumberOfRecords ? (
            <Pagination
              totalPages={totalNumberOfPages}
              pageNumber={currentPageNumber}
              onPreviousClick={onPaginationPreviousClick}
              onNextClick={onPaginationNextClick}
              displayByPage={false}
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
