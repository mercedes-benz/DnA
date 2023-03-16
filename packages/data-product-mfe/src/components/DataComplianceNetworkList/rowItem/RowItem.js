import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './RowItem.scss';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';

const classNames = cn.bind(Styles);

let isTouch = false;

const RowItem = (props) => {
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [showLocationsContextMenu, setShowLocationsContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState(0);

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
    const contextMenuWrapper = cardDivElement?.querySelector('.contextMenuWrapper');
    const locationContextMenuWrapper = cardDivElement?.querySelector('.contextMenuWrapper');

    if (
      cardDivElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper?.contains(target) === false &&
      (showContextMenu || showLocationsContextMenu)
    ) {
      setShowContextMenu(false);
      setShowLocationsContextMenu(false);
    } else if (cardDivElement?.contains(target) === false) {
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

  useEffect(() => {
    Tooltip.defaultSetup();
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);

    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const toggleContextMenu = (e) => {
    e.stopPropagation();
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 10);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 200);
    setShowLocationsContextMenu(false);
    setShowContextMenu(!showContextMenu);
  };

  const item = props.item;

  const onItemDelete = () => {
    props.showDeleteConfirmModal(props.item);
  };
  const onItemUpdate = () => {
    props.showUpdateConfirmModal(props.item);
  };

  return (
    <tr id={item.id} className="data-row">
      <td className="wrap-text">{item.entityId}</td>
      <td className="wrap-text">{item.entityName}</td>
      <td className="wrap-text">{item.entityCountry}</td>
      <td className="wrap-text">
        {item.localComplianceOfficer !== undefined &&
          (item.localComplianceOfficer.length > 0 ? item.localComplianceOfficer.join('; ') : 'N/A')}
      </td>
      <td className="wrap-text">
        {item.localComplianceResponsible !== undefined &&
          (item.localComplianceResponsible.length > 0 ? item.localComplianceResponsible.join('; ') : 'N/A')}
      </td>
      {/* <td className="wrap-text">{item.dataProtectionCoordinator !== undefined && (item.dataProtectionCoordinator.length > 0 ? item.dataProtectionCoordinator.join('; ') : 'N/A')}</td> */}
      <td className="wrap-text">
        {item.localComplianceSpecialist !== undefined &&
          (item.localComplianceSpecialist.length > 0 ? item.localComplianceSpecialist.join('; ') : 'N/A')}
      </td>
      {props.isAdmin && (
        <td className={'wrap-text ' + classNames(Styles.actionLinksTD)}>
          <div id={'card-' + item.id} className={Styles.actionMenus}>
            <div className={classNames(Styles.contextMenu, showContextMenu ? Styles.open : '')}>
              <span
                onClick={toggleContextMenu}
                className={classNames('trigger', Styles.contextMenuTrigger)}
                tooltip-data="More Action"
              >
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
                  <li className="contextListItem" onClick={onItemUpdate}>
                    <span>Update</span>
                  </li>
                  <li className="contextListItem" onClick={onItemDelete}>
                    <span>Delete</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </td>
      )}
    </tr>
  );
};

export default RowItem;
