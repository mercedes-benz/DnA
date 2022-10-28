import classnames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './RowItem.scss';
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
import CircularProgressBar from '../../../shared/circularProgressBar/CircularProgressBar';
import ContextMenu from '../../../shared/contextMenu/ContextMenu';
import { regionalDateAndTimeConversionSolution } from '../../../../Utility/utils';

const classNames = classnames.bind(Styles);

const RowItem = (props) => {
  const item = props.item;

  const [isChecked, setIsChecked] = useState(false);
  const [isAvaialableInWithExceptionArray, setIsAvaialableInWithExceptionArray] = useState(false);

  useEffect(() => {
    Tooltip.defaultSetup();
    setIsAvaialableInWithExceptionArray(props.checkAllWithException);
    if (props.checkedAllCount > 0 && !props.checkedAll) {
      setIsChecked(false);
    }
  }, [props, isChecked]);

  const onChangeCheck = (event) => {
    if (!event.currentTarget.checked) {
      props.unCheckAll();
      props.deselectNotification(event.currentTarget.id);
    } else {
      props.selectNotification(event.currentTarget.id);
    }
    setIsChecked(event.currentTarget.checked);
    event.nativeEvent.stopImmediatePropagation();
  };

  const [showContextMenu, setShowContextMenu] = useState(false);

  const handleShowContextMenu = (value) => {
    setShowContextMenu(value);
  }
  const onRowClick = (event) => {
    console.log(event)
    props.openDetails(item);
  };

  const stopPropagation = (event) => {
    event.stopPropagation();
  };

  const onItemDelete = () => {
    props.showDeleteConfirmModal(props.item);
  };

  const contextMenuItems = [
    {
      title: 'Delete Run/Results',
      onClickFn: onItemDelete
    }
  ];

  return (
    <React.Fragment>
      <tr
        key={item.id}
        onClick={showContextMenu ? undefined : onRowClick}
        className={classNames('data-row', Styles.dataRow)}
      >
        <td>
          <label
            className={classNames('checkbox')}
            onClick={(event) => stopPropagation(event)}
          >
            <span className="wrapper">
              <input
                type="checkbox"
                className="ff-only"
                id={'checkbox-' + item.id}
                checked={
                  props.checkedAll ||
                  isChecked ||
                  (isAvaialableInWithExceptionArray && !props.currentItemNotToBeDeleted)
                }
                onChange={(event) => {
                  onChangeCheck(event);
                }}
              />
            </span>{' '}
          </label>
        </td>
        <td>
          {/* { item.new && <span className={Styles.badge}>New</span> }  */}
          {item.runName}
        </td>
        <td>
          {item.state.result_state === 'SUCCESS' && <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} tooltip-data={item.state.result_state} />}
          {item.state.result_state === 'CANCELED' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} tooltip-data={item.state.result_state} />}
          {item.state.result_state === 'FAILED' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} tooltip-data={item.state.result_state} />}
          {item.state.result_state === 'TIMEDOUT' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} tooltip-data={item.state.result_state} />}
          {item.state.result_state === 'WARNING' && <i className={classNames('icon mbc-icon alert circle', Styles.alertCircle)} tooltip-data={'Lorem ipsum some warning message here'} />}
          {/* <i className={classNames('icon mbc-icon alert circle', Styles.alertCircle)} tooltip-data={'Lorem ipsum some warning message here'} /> */}
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
          No
        </td>
        <td>
          <ContextMenu id={item.id} items={contextMenuItems} isMenuOpen={handleShowContextMenu} />
        </td>
      </tr>
    </React.Fragment>
  );
};

export default RowItem;
