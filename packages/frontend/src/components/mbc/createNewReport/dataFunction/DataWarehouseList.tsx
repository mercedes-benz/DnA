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
  isSingleDataSourceContextMenuOpened: boolean;
  setDataWarehouseContextMenuStatus:(status: boolean) => void;
  setSingleDataSourceContextMenuStatus:(status: boolean) => void;
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
  isSingleDataSourceContextMenuOpened,
  setDataWarehouseContextMenuStatus,
  setSingleDataSourceContextMenuStatus
}: DataWarehouseProps) => {
  // let listRowElement: HTMLElement;
  let isTouch = false;
  const inputRef = useRef<HTMLTableRowElement>(null);
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState(0);
  const [contextMenuOffsetRight, setContextMenuOffsetRight] = useState(0);
  const [selectedContextMenu, setSelectedContextMenu] = useState('');
  
  useEffect(() => {
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('clicked', handleContextMenuOutside, true);
  });

  const toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>, index: number) => {
    e.stopPropagation();
    setSingleDataSourceContextMenuStatus(false);
    // const elemRect: ClientRect = e.currentTarget.getBoundingClientRect();
    // const relativeParentTable: ClientRect = document.querySelector('table.dataWarehouseList').getBoundingClientRect();
    const contextMenuStatus = showContextMenu;
    setDataWarehouseContextMenuStatus(true);
    // setContextMenuOffsetTop(elemRect.top - (relativeParentTable.top + 10));
    setContextMenuOffsetTop(-9);
    setContextMenuOffsetRight(10);
    setShowContextMenu(!contextMenuStatus);
    setSelectedContextMenu('#datawarehouse-'+index);
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
              Styles.dataWarehouseContextMenu,
              showContextMenu && selectedContextMenu == '#datawarehouse-'+index ? Styles.open : '',
            )}
          >
            <span onClick={(e: React.FormEvent<HTMLSpanElement>) => toggleContextMenu(e, index)} className={classNames('trigger', Styles.contextMenuTrigger)}>
              <i className="icon mbc-icon listItem context" />
            </span>
            {selectedContextMenu == '#datawarehouse-'+index && !isSingleDataSourceContextMenuOpened ?
              <div
                style={{
                  top: contextMenuOffsetTop + 'px',
                  right: contextMenuOffsetRight + 'px',
                }}
                className={classNames('contextMenuWrapper', showContextMenu ? Styles.contextMenuWrapperStyle : 'hide')}
              >
                <ul className="contextList">                  
                  <li className="contextListItem">
                    <span onClick={() => {setShowContextMenu(false); onEdit(datawarehouse, index);}}>Edit Data Warehouse</span>
                  </li>
                
                  <li className="contextListItem">
                    <span onClick={() => {setShowContextMenu(false); onDelete(true, index); }}>Delete Data Warehouse</span>
                  </li>
                </ul>
              </div>
            : ''}
          </div>
        </td>
      </tr>
      )}
      
      )}
      
      </tbody>
    </table>


  );
};
