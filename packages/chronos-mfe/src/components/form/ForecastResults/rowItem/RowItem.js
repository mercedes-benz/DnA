import classnames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './RowItem.scss';
import Tooltip from '../../../../common/modules/uilab/js/src/tooltip';
import CircularProgressBar from '../../../shared/circularProgressBar/CircularProgressBar';

const classNames = classnames.bind(Styles);
let isTouch = false;

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

  const onRowClick = (event) => {
    console.log(event)
    props.openDetails(item);
  };

  const stopPropagation = (event) => {
    event.stopPropagation();
  };

  /* Context Menu */
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [showLocationsContextMenu, setShowLocationsContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState(0);

  useEffect(() => {
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);

    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleContextMenuOutside = (event) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }

    const target = event.target;
    const elemClasses = target.classList;
    const cardDivElement = document.querySelector('#card-' + item.id);
    const contextMenuWrapper = cardDivElement.querySelector('.contextMenuWrapper');
    const locationContextMenuWrapper = cardDivElement.querySelector('.contextMenuWrapper');

    if (
      cardDivElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      (showContextMenu || showLocationsContextMenu)
    ) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    } else if (cardDivElement.contains(target) === false) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    }

    if (!contextMenuWrapper?.contains(target)) {
      setShowContextMenu(false);
    }

    if (!locationContextMenuWrapper?.contains(target)) {
      setShowLocationsContextMenu(false);
    }

    if (
      (showContextMenu || showLocationsContextMenu) &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };
  const toggleContextMenu = (e) => {
    e.stopPropagation();
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 10);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 200);
    setShowLocationsContextMenu(false);
    setShowContextMenu(!showContextMenu);
  };
  const onItemDelete = () => {
    props.showDeleteConfirmModal(props.item);
  };

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
          { item.new && <span className={Styles.badge}>New</span> } {item.name}
        </td>
        <td>
          {item.status === 'success' && <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} />}
          {item.status === 'failed' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)} />}
          {item.status === 'in progress' && <CircularProgressBar />}
        </td>
        <td>
          {item.datetime}
        </td>
        <td>
          {item.ranBy}
        </td>
        <td>
          {item.inputFile}
        </td>
        <td>
          {item.forecastHorizon}
        </td>
        <td>
          {item.exogenousData}
        </td>
        <td>
          <div id={'card-' + item.id} className={Styles.actionMenus}>
            <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
              <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)} tooltip-data="More Action">
                <i className="icon mbc-icon listItem context" />
              </span>
              <div
                style={{
                  top: contextMenuOffsetTop + 'px',
                  left: contextMenuOffsetLeft + 'px',
                }}
                className={classNames('contextMenuWrapper', showContextMenu ? Styles.showMenu : 'hide')}
              >
                <ul className="contextList">
                  <li className="contextListItem" onClick={onItemDelete}>
                    <span>Delete Run/Results</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </td>
      </tr>
    </React.Fragment>
  );
};

export default RowItem;
