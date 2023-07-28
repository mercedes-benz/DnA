import classnames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './forecast-run-row.scss';
// import from DNA Container
import CircularProgressBar from '../../circularProgressBar/CircularProgressBar';
import ContextMenu from '../../contextMenu/ContextMenu';
import { regionalDateAndTimeConversionSolution } from '../../../utilities/utils';
import Notification from '../../../common/modules/uilab/js/src/notification';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { Envs } from '../../../utilities/envs';
import { chronosApi } from '../../../apis/chronos.api';

const classNames = classnames.bind(Styles);

const ForecastRunRow = (props) => {
  const item = props.item;

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
    //eslint-disable-next-line
  }, []);

  const [showContextMenu, setShowContextMenu] = useState(false);

  const handleShowContextMenu = (value) => {
    setShowContextMenu(value);
  }
  const onRowClick = () => {
    props.openDetails(item);
  };

  const onItemDelete = () => {
    props.showDeleteConfirmModal(props.item);
  };

  const onBrowseClick = () => {
    if(props.item.resultFolderPath) {
      const resultFolderPath = item.resultFolderPath.split('/');
      window.open(`${Envs.CONTAINER_APP_URL}/#/storage/explorer/${resultFolderPath[0]}/${resultFolderPath[2]}`);
    } else {
      Notification.show('No folder path available for the given run', 'alert');
    }
  }

  const downloadPrediction = () => {
    ProgressIndicator.show();
    if(props.item.resultFolderPath) {
      const resultFolderPath = item.resultFolderPath.split('/');
      chronosApi.getFile(`${resultFolderPath[0]}`, `${resultFolderPath[2]}`, 'y_pred.csv').then((res) => {
        let csvContent = "data:text/csv;charset=utf-8," + res.data;
        let encodedUri = encodeURI(csvContent);
        let link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", "y_pred.csv");
        document.body.appendChild(link);
        link.click();
        link.remove();
        ProgressIndicator.hide();
      }).catch(() => {
        Notification.show('Unable to download the file', 'alert');
        ProgressIndicator.hide();
      });
    }
  }

  const downloadExcel = () => {
    ProgressIndicator.show();
    if(props.item.resultFolderPath) {
      const resultFolderPath = item.resultFolderPath.split('/');
      chronosApi.getExcelFile(`${resultFolderPath[0]}`, `${resultFolderPath[2]}`, 'RESULT.xlsx').then((res) => {
        var excelBlob = new Blob([res.data]);     
        var url = window.URL.createObjectURL(excelBlob);

        let link = document.createElement("a");
        link.setAttribute("href", url);
        link.setAttribute("download", "RESULT.xlsx");
        document.body.appendChild(link);
        link.click();
        link.remove();
        ProgressIndicator.hide();
      }).catch(() => {
        Notification.show('Unable to download the file', 'alert');
        ProgressIndicator.hide();
      });
    }
  }

  const contextMenuItems = [
    {
      title: 'Delete Run/Results',
      onClickFn: onItemDelete
    },
    {
      title: 'Browse in Storage',
      onClickFn: onBrowseClick,
    },
    {
      title: 'Download as Excel',
      onClickFn: downloadExcel,
    },
    {
      title: 'Download prediction as .csv',
      onClickFn: downloadPrediction,
    }
  ];

  const [isChecked, setIsChecked] = useState(false);

  const onChangeCheck = (e) => {
    if (!e.currentTarget.checked) {
      props.deselectRun(e.currentTarget.id);
    } else {
      props.selectRun(e.currentTarget.id);
    }
    setIsChecked(e.currentTarget.checked);
    e.nativeEvent.stopImmediatePropagation();
  };

  const handleStatusClick = (e, item) => {
    e.stopPropagation();
    props.onOpenErrorModal(item);
  }

  const stopPropagation = (e) => {
    e.stopPropagation();
  };

  const handleRunCancel = (e, run) => {
    stopPropagation(e);
    props.onCancelRun(run);
  }

  return (
    <React.Fragment>
      <tr
        key={item.id}
        onClick={showContextMenu ? undefined : onRowClick}
        className={classNames('data-row', Styles.dataRow)}
      >
        <td>
          <label
            className={classNames('checkbox', Styles.checkboxItem)}
            onClick={(e) => stopPropagation(e)}
          >
            <span className="wrapper">
            <input
                type="checkbox"
                className="ff-only"
                id={'checkbox-' + item.id}
                checked={isChecked}
                onChange={(e) => {
                  e.stopPropagation();
                  onChangeCheck(e, item.id);
                }}
                disabled={item.state.result_state === null || item.state.result_state === 'FAILED' || item.state.result_state === 'CANCELED'}
              />
            </span>{' '}
          </label>
        </td>
        <td>
          {/* { item.new && <span className={Styles.badge}>New</span> }  */}
          { item.comment === '' && <span>{item.runName}</span> }
          { item.comment !== '' && <span tooltip-data={item.comment}>{item.runName}</span> }
        </td>
        <td>
          {item.state.result_state === 'SUCCESS' && <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} tooltip-data={item.state.result_state} />}
          {item.state.result_state === 'CANCELED' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'FAILED' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'TIMEDOUT' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'WARNINGS' && <i className={classNames('icon mbc-icon alert circle', Styles.alertCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Warning'} />}
          {item.state.result_state === null && <><div tooltip-data={'IN PROGRESS'} ><CircularProgressBar /></div> <button className={classNames('btn', Styles.cancelBtn)} onClick={(e) => handleRunCancel(e, item)}>Cancel</button></>}
        </td>
        <td>
          {regionalDateAndTimeConversionSolution(item.triggeredOn)}
        </td>
        <td>
          {item.triggeredBy}
        </td>
        <td>
          {item.inputFile.split("/")[2]}
        </td>
        <td>
          {item.forecastHorizon}
        </td>
        <td>
          {item.state.result_state === null ? '...' : item.exogenData ? 'Yes' : 'No'}
        </td>
        <td>
          {item.hierarchy === '' || item.hierarchy === undefined ? 'No Hierarchy' : item.hierarchy}
        </td>
        <td>
          {item.state.result_state !== null && <ContextMenu id={item.id} items={contextMenuItems} isMenuOpen={handleShowContextMenu} />}
        </td>
      </tr>
    </React.Fragment>
  );
};

export default ForecastRunRow;
