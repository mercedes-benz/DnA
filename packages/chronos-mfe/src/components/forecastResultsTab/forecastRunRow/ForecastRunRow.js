import classnames from 'classnames';
import React, { useState } from 'react';
import styles from './forecast-run-row.scss';
// import from DNA Container
import CircularProgressBar from '../../circularProgressBar/CircularProgressBar';
import ContextMenu from '../../contextMenu/ContextMenu';
import { regionalDateAndTimeConversionSolution } from '../../../utilities/utils';
import Notification from '../../../common/modules/uilab/js/src/notification';
import { Envs } from '../../../utilities/envs';

const classNames = classnames.bind(styles);

const ForecastRunRow = (props) => {
  const item = props.item;

  const [showContextMenu, setShowContextMenu] = useState(false);

  const handleShowContextMenu = (value) => {
    setShowContextMenu(value);
  }
  const onRowClick = (event) => {
    console.log(event)
    props.openDetails(item);
  };

  const onItemDelete = () => {
    props.showDeleteConfirmModal(props.item);
  };

  const onBrowseClick = () => {
    if(props.item.resultFolderPath) {
      const resultFolderPath = item.resultFolderPath.split('/');
      window.open(`${Envs.STORAGE_MFE_APP_URL}/explorer/${resultFolderPath[0]}/${resultFolderPath[2]}`);
    } else {
      Notification.show('No folder path available for the given run', 'alert');
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
    }
  ];

  const handleStatusClick = (e, item) => {
    e.stopPropagation();
    props.onOpenErrorModal(item);
  }

  return (
    <React.Fragment>
      <tr
        key={item.id}
        onClick={showContextMenu ? undefined : onRowClick}
        className={classNames('data-row', styles.dataRow)}
      >
        <td>
          {/* { item.new && <span className={styles.badge}>New</span> }  */}
          { item.comment === '' && <span>{item.runName}</span> }
          { item.comment !== '' && <span tooltip-data={item.comment}>{item.runName}</span> }
        </td>
        <td>
          {item.state.result_state === 'SUCCESS' && <i className={classNames('icon mbc-icon check circle', styles.checkCircle)} tooltip-data={item.state.result_state} />}
          {item.state.result_state === 'CANCELED' && <i className={classNames('icon mbc-icon close circle', styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'FAILED' && <i className={classNames('icon mbc-icon close circle', styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'TIMEDOUT' && <i className={classNames('icon mbc-icon close circle', styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'WARNINGS' && <i className={classNames('icon mbc-icon alert circle', styles.alertCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Warning'} />}
          {item.state.result_state === null && <div tooltip-data={'IN PROGRESS'} ><CircularProgressBar /></div>}
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
