import React, { useEffect, useRef, useState } from 'react';
import cn from 'classnames';
import Styles from './DataFunction.scss';
import { IDataWarehouseInUse, ISingleDataSources } from 'globals/types';

const classNames = cn.bind(Styles);

interface DataWarehouseProps {
  dataWarehouselist: IDataWarehouseInUse[];
  currentColumnToSort: string;
  currentSortOrder: string;
  onEdit: (singleDataSources: IDataWarehouseInUse, index: number) => void;
  onDelete: (isDatawarehouse: boolean, index: number) => void;
  singleDataSourceList: ISingleDataSources[];
  showDataSourceModal: () => void;
  dataAndFunctionTabError: string;
}

export const DataWarehouseList = ({
  singleDataSourceList,
  dataWarehouselist,
  showDataSourceModal,
  currentColumnToSort,
  currentSortOrder,
  onEdit,
  onDelete,
  dataAndFunctionTabError,
}: DataWarehouseProps) => {
  // let listRowElement: HTMLElement;
  let isTouch = false;
  const inputRef = useRef<HTMLTableRowElement>(null);
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetRight, setContextMenuOffsetRight] = useState(0);
  
  useEffect(() => {
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('clicked', handleContextMenuOutside, true);
  });

  const toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    const elemRect: ClientRect = e.currentTarget.getBoundingClientRect();
    const relativeParentTable: ClientRect = document.querySelector('table.dataWarehouseList').getBoundingClientRect();
    setContextMenuOffsetTop(elemRect.top - (relativeParentTable.top + 10));
    setContextMenuOffsetRight(10);
    setShowContextMenu(!showContextMenu);
  };

  // const listRow = (element: HTMLTableRowElement) => {
  //   listRowElement = element;
  // };

  const handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }

    const target = event.target as Element;
    const elemClasses = target.classList;
    const listRowElement = inputRef;
    const contextMenuWrapper = listRowElement.current.querySelector('.contextMenuWrapper');
    if (
      listRowElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      (showContextMenu)
    ) {      
        setShowContextMenu(false)
    } else if (listRowElement.current.contains(target) === false) {
        setShowContextMenu(false);
    }

    if (
      (showContextMenu) &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };
  


  return (
    <table
    className={classNames(
      'ul-table dataWarehouseList',
      Styles.dataWarehouseTable,
      dataWarehouselist?.length === 0 ? 'hide' : '',
    )}
    >
      <tbody>
      {dataWarehouselist?.map((datawarehouse: IDataWarehouseInUse, index: number) => {
      return (
        <tr
        id={'datawarehouse-'+index}
        key={index}
        className={classNames(
          'data-row',
          Styles.reportRow,
          showContextMenu ? Styles.contextOpened : null,
        )}
        // ref={node => { listRowElement = node; }}
        ref={inputRef}
        // onClick={this.goToSummary}
      >
        <td className={'wrap-text ' + classNames(Styles.reportName)}>
          <div className={Styles.solIcon}>
            {datawarehouse?.dataWarehouse}
          </div>
        </td>
        <td className="wrap-text">{datawarehouse?.connectionType || 'NA'}</td>
        <td className="wrap-text">{datawarehouse?.dataClassification  || 'NA'}</td>
        <td>
          <div
            className={classNames(
              Styles.contextMenu,
              showContextMenu ? Styles.open : '',
            )}
          >
            <span onClick={toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)}>
              <i className="icon mbc-icon listItem context" />
            </span>
            <div
              style={{
                top: contextMenuOffsetTop + 'px',
                right: contextMenuOffsetRight + 'px',
              }}
              className={classNames('contextMenuWrapper', showContextMenu ? '' : 'hide')}
            >
              <ul className="contextList">                  
                <li className="contextListItem">
                  <span onClick={() => onEdit(datawarehouse, index)}>Edit KPI</span>
                </li>
              
                <li className="contextListItem">
                  <span onClick={() => onDelete(true, index)}>Delete KPI</span>
                </li>
              </ul>
            </div>
          </div>
        </td>
      </tr>
      )}
      
      )}
      
      </tbody>
    </table>


  );
};
