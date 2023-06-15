import classnames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './context-menu.scss';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';

const classNames = classnames.bind(Styles);
let isTouch = false;

const ContextMenu = (props) => {

  useEffect(() => {
    Tooltip.defaultSetup();
    return Tooltip.clear();
  }, []);

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
    const cardDivElement = document.querySelector('#card-' + props.id);
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
      props.isMenuOpen && props.isMenuOpen(false);
      setShowLocationsContextMenu(false);
    } else if (cardDivElement.contains(target) === false) {
      setShowContextMenu(false);
      props.isMenuOpen && props.isMenuOpen(false);
      setShowLocationsContextMenu(false);
    }

    if (!contextMenuWrapper?.contains(target)) {
      setShowContextMenu(false);
      props.isMenuOpen && props.isMenuOpen(false);
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
    props.isMenuOpen && props.isMenuOpen(!showContextMenu);
  };

  return (
    <div id={'card-' + props.id} className={Styles.actionMenus}>
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
            {
              props.items.length > 0 && props.items.map((item) => (
                <li key={item.title} className={classNames('contextListItem', item.disable && Styles.disableLink)} onClick={item.onClickFn}>
                  <span>{item.title}</span>
                </li>
              ))
            }
          </ul>
        </div>
      </div>
    </div>
  )
}

export default ContextMenu;